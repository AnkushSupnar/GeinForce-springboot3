package com.geinforce.service;


//import org.openbabel.OBConversion;
//import org.openbabel.OBMol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geinforce.model.Job;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Service
public class FileService {
    @Value("${file.upload.pdb}")
    String uploadPDBPath;
    @Value("${file.user}")
    String userDirectory;
    @Value("${tool.path.pdbqtTopdb}")
    String pdbqtTopdb;
    @Value("${tool.path.openbabel}")
    String openBabelPath;
    @Autowired
    JobService jobService;

    @Autowired
    private ResourceLoader resourceLoader;
    public String readPDBFile(String filePath) {
        try {
            // Read the file content into a byte array
            Path path = Paths.get(filePath);
            byte[] fileBytes = Files.readAllBytes(path);

            // Convert the byte array to a string using UTF-8 encoding
            String pdbContent = new String(fileBytes, StandardCharsets.UTF_8);

            return pdbContent;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String uploadProteinFile(MultipartFile file, String email, String jobName, String fileType) {
        try {
            System.out.println("upload protein file path from prp=" + uploadPDBPath);
            Instant now = Instant.now();
            if (!file.isEmpty()) {
                String fileName = file.getOriginalFilename();
                String uploadFilePath = userDirectory + File.separator + email + File.separator + jobName
                        + File.separator + fileType + File.separator + jobName + "_" + fileName;
                String uploadPath = userDirectory + File.separator + email + File.separator + jobName + File.separator
                        + fileType;
                System.out.println("upload file path = " + uploadFilePath);
                System.out.println("upload path = " + uploadPath);
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    // uploadDir.mkdir();
                    uploadDir.mkdirs();
                }
                File dest = new File(uploadFilePath);
                file.transferTo(dest);
                System.out.println("file copied to " + uploadFilePath);
                return "Success:" + dest.getAbsolutePath();
            } else {
                return "failed:File is empty";
            }
        } catch (Exception e) {
            System.out.println("Error in uploading file" + e.getMessage());
            return "failed:File not uploaded";
        }
    }

    public String writePDBQT(String filePath, String content) {
        try {
            byte[] fileBytes = content.getBytes(StandardCharsets.UTF_8);

            Path path = Paths.get(filePath);
            Files.write(path, fileBytes);
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getFileType(String filePath) {
        String fileType = "";
        try {
            Path path = Paths.get(filePath);
            long atomCount = Files.lines(path)
                    .filter(line -> line.startsWith("ATOM"))
                    .count();
            long hetatmCount = Files.lines(path)
                    .filter(line -> line.startsWith("HETATM"))
                    .count();
            if (atomCount > hetatmCount) {
                System.out.println("This is likely a protein file.");
                fileType = "protein";
            } else if (hetatmCount > 0) {
                System.out.println("This is likely a ligand file.");
                fileType = "ligand";
            } else {
                System.out.println("Unable to determine file type.");
                fileType = "unknown";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileType;
    }

    public String createUserDirectory(String email) {
        Path directory = Paths.get(userDirectory + File.separator + email);
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
                System.out.println("User Directory was created");
                return "Success:user directory created";
            } catch (IOException e) {
                System.out.println("Failed to create directory");
                e.printStackTrace();
                return "Failed:user directory not created";
            }
        } else {
            System.out.println("User Directory already exists");
            return "Success:user directory exist";
        }
    }

    public String getFilePath(String jobName, String email, String fileType) {
        Job job = jobService.getByNameAndEmail(jobName, email);
        if (fileType.equalsIgnoreCase("protein"))
            return userDirectory + File.separator + email + File.separator + jobName + File.separator + fileType
                    + File.separator + job.getProteinFile();
        else if (fileType.equalsIgnoreCase("ligand"))
            return userDirectory + File.separator + email + File.separator + jobName + File.separator + fileType +
                    File.separator + job.getLigandFiles();
        else if (fileType.equalsIgnoreCase("output")) {
            return userDirectory + File.separator + email + File.separator + jobName + File.separator + fileType
                    + File.separator + job.getOutputFile();
        }
        else if(fileType.equalsIgnoreCase("complex_lig")) {
        	 return userDirectory + File.separator + email + File.separator + jobName +File.separator+"output"+
        			 File.separator + fileType;
        }
        else if (fileType.equalsIgnoreCase("vinaJSON")) {
            return userDirectory +File.separator +
                    email + File.separator+
                    jobName + File.separator +
                    "vinaconfig"+ File.separator +
                    job.getJobName()+"_out.json";
        }
        else
            return "";
    }

    public void decompressGzipFile(String gzipFile, String newFile) {

        System.out.println("gzip file=" + gzipFile);
        String outputFilePath = "";
        try {
            FileInputStream fis = new FileInputStream(gzipFile);
            GZIPInputStream gis = new GZIPInputStream(fis);
            FileOutputStream fos = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }

            // close resources
            fos.close();
            gis.close();
        } catch (IOException e) {
            System.out.println("error in decompressing file" + e.getMessage());
            // e.printStackTrace();
        }
    }

    public String renameFileExtensionEntToPDB(String originalFilePath, String newExtension) {
        try {
            Path originalPath = Paths.get(originalFilePath);
            String fileNameWithoutExtension = originalPath.getFileName().toString().replaceFirst("[.][^.]+$", "");
            Path parentDir = originalPath.getParent();
            Path newPath = Paths.get(parentDir.toString(), fileNameWithoutExtension + newExtension);
            Files.move(originalPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            return fileNameWithoutExtension + newExtension;
        } catch (IOException e) {
            System.out.println("Error renaming file: " + e.getMessage());
        }
        return "";
    }

    public String convertPdbToMol2(String pdbFile, String mol2File) {

        try {
            // Here "obabel" is assumed to be in the system PATH.
            // If not, you may need to provide full path to the obabel executable.
            ProcessBuilder processBuilder = new ProcessBuilder(openBabelPath, pdbFile, "-O", mol2File);
            Process process = processBuilder.start();
            // Get process output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            // Wait for the process to finish and get the exit code
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Error occurred while converting PDB to MOL2. Exit code: " + exitCode);
                System.out.println("Error message: " + output.toString());
                return "Error";
            }

            return "Conversion to MOL2 completed successfully!";
        } catch (IOException | InterruptedException e) {
            System.out.println("Exception occurred during PDB to MOL2 conversion: " + e.getMessage());
            e.printStackTrace();
            return "Error";
        }
    }

    public String convertLigandToSMILES(String inputFilePath) throws IOException, InterruptedException {
        try {
            String command = openBabelPath + " " + inputFilePath + " -osmi";
            Process process = Runtime.getRuntime().exec(command);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine(); // Assuming one SMILES string per file
                return line.split("\\s")[0]; // Split by whitespace and get the first token
            }
        } catch (Exception e) {
            System.out.println("Error in getting smiles=" + e.getMessage());
        }
        return "";
    }

    public String convertSdfToPdbqt(String inputFilePath, String outputFilePath)
            throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(openBabelPath, "-isdf", inputFilePath, "-opdbqt",
                outputFilePath);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Conversion process exited with error code: " + exitCode);
        }
        return outputFilePath;
    }

    public String convertFileSdfToPdb(String inputFilePath, String outputFilePath)
            throws IOException, InterruptedException {
    	System.out.println("inoutFile Path "+inputFilePath+" \n outputFilePath="+outputFilePath);
        ProcessBuilder processBuilder = new ProcessBuilder(
            openBabelPath,
            "-isdf",
            inputFilePath,
            "-opdb",
            outputFilePath,
            "--gen3d"
        );
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Conversion process exited with error code: " + exitCode);
        }
        return outputFilePath;
    }
    
    public String convertFileMolToPdb(String inputFilePath, String outputFilePath)
            throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
            openBabelPath,
            "-imol",
            inputFilePath,
            "-opdb",
            outputFilePath,
            "--gen3d"
        );
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Conversion process exited with error code: " + exitCode);
        }
        return outputFilePath;
    }
    //convert .sdf .mol .mol2 to pdb
    public String convertFilePdb(String inputFilePath, String outputFilePath)
            throws IOException, InterruptedException {
    	
      
        ProcessBuilder processBuilder = new ProcessBuilder(
                "obabel",
                inputFilePath,
                "-O", 
                outputFilePath,
                "--gen3D",
                "--addh",
                "--minimize"
            );
       // processBuilder.redirectErrorStream(true);
        processBuilder.inheritIO();
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("OpenBabel process exited with error code: " + exitCode);
        }
        File outputFile = new File(outputFilePath);
        if (!outputFile.exists()) {
            throw new IOException("Output file was not created: " + outputFile.getAbsolutePath());
        }
        return outputFilePath;
    }
    public String saveSDFFile(MultipartFile file, String email, String jobName) throws IOException {
        String uploadDir = userDirectory + File.separator + email + File.separator + "ligand";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String fileName = file.getOriginalFilename();
        try {
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());
            return filePath.toString();
        } catch (IOException ioe) {
            throw new IOException("Could not save file: " + fileName, ioe);
        }
    }

    public File getLigandInPdb(File inputFile, String outputFile) throws IOException, InterruptedException {
        System.out.println("Input file path: " + inputFile.getParent());
        System.out.println("Input file path get: " + inputFile.getPath());

        if (!inputFile.exists()) {
            System.out.println("Input file does not exist: " + inputFile.getPath());
            // return null;
        }
        String parentDir = inputFile.getParent().replace("\\", "/");
        String fileNameWithoutExt = inputFile.getName().replaceFirst("[.][^.]+$", "");
        String pdbFilePath = parentDir + "/" + fileNameWithoutExt + ".pdb";
        pdbFilePath = outputFile + File.separator + fileNameWithoutExt + ".pdb";
        String command = "\"" + openBabelPath + "\" -i sdf \"" + inputFile.getPath().replace("\\", "/") + "\" -o pdb \""
                + pdbFilePath + "\"";
        System.out.println("Executing command: " + command);

        Process process = Runtime.getRuntime().exec(command);

        // Capture error stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("process = " + line);
        }

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            System.out.println("Conversion successful. Output file: " + pdbFilePath);
            return new File(pdbFilePath);
        } else {
            System.out.println("Conversion failed");
            return null;
        }
    }

    public Resource loadAsResource(String filename) throws MalformedURLException {
        Path file = Paths.get(filename);
        Resource resource = new UrlResource(file.toUri());
        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("Could not read file: " + filename);
        }
    }

    // working
    public String convertSdfToPdb(String sdfContent) {
        if (sdfContent == null || sdfContent.trim().isEmpty()) {
            return null; // Return null if the input content is empty.
        }

        Scanner scanner = new Scanner(sdfContent);
        StringBuilder pdbBuilder = new StringBuilder();

        // Skip the first three lines of the SDF content
        for (int i = 0; i < 3; i++) {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            } else {
                scanner.close();
                return null; // Return null if the file does not have enough lines.
            }
        }

        // Check if there is a counts line available
        if (!scanner.hasNextLine()) {
            scanner.close();
            return null;
        }

        String countsLine = scanner.nextLine();
        int numAtoms;
        try {
            numAtoms = Integer.parseInt(countsLine.substring(0, 3).trim());
        } catch (NumberFormatException e) {
            scanner.close();
            return null; // Return null if the counts line is not in the expected format.
        }

        // Parse the atom block
        for (int i = 1; i <= numAtoms; i++) {
            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 4) {
                    String x = String.format("%8.3f", Double.parseDouble(parts[0]));
                    String y = String.format("%8.3f", Double.parseDouble(parts[1]));
                    String z = String.format("%8.3f", Double.parseDouble(parts[2]));
                    String atomSymbol = parts[3];

                    // Construct the PDB HETATM line
                    String pdbLine = String.format(
                            "HETATM%5d %-4s MOL     1    %s%s%s  1.00  0.00           %2s\n",
                            i, atomSymbol, x, y, z, atomSymbol);
                    pdbBuilder.append(pdbLine);
                } else {
                    scanner.close();
                    return null; // Return null if the atom line format is not as expected.
                }
            } else {
                scanner.close();
                return null; // Return null if there are not enough atom lines.
            }
        }

        pdbBuilder.append("END\n");
        scanner.close();
        return pdbBuilder.toString();
    }

    public String convertMolToPdb(String molContent) {
        if (molContent == null || molContent.trim().isEmpty()) {
            return null; // Return null if the input content is empty.
        }
        Scanner scanner = new Scanner(molContent);
        StringBuilder pdbBuilder = new StringBuilder();

        // Skip header lines (usually 3 lines in the V2000 format)
        for (int i = 0; i < 3; i++) {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            } else {
                scanner.close();
                return null; // Return null if the file does not have enough lines.
            }
        }

        // Check if there is a counts line available
        if (!scanner.hasNextLine()) {
            scanner.close();
            return null;
        }

        String countsLine = scanner.nextLine();
        int numAtoms;
        try {
            numAtoms = Integer.parseInt(countsLine.substring(0, 3).trim());
        } catch (NumberFormatException e) {
            scanner.close();
            return null; // Return null if the counts line is not in the expected format.
        }

        // Parse the atom block
        for (int i = 1; i <= numAtoms; i++) {
            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 4) {
                    String x = String.format("%8.3f", Double.parseDouble(parts[0]));
                    String y = String.format("%8.3f", Double.parseDouble(parts[1]));
                    String z = String.format("%8.3f", Double.parseDouble(parts[2]));
                    String atomSymbol = parts[3];

                    // Construct the PDB HETATM line
                    String pdbLine = String.format(
                            "HETATM%5d %-4s MOL     1    %s%s%s  1.00  0.00           %2s\n",
                            i, atomSymbol, x, y, z, atomSymbol);
                    pdbBuilder.append(pdbLine);
                } else {
                    scanner.close();
                    return null; // Return null if the atom line format is not as expected.
                }
            } else {
                scanner.close();
                return null; // Return null if there are not enough atom lines.
            }
        }

        pdbBuilder.append("END\n");
        scanner.close();
        return pdbBuilder.toString();
    }

    public String convertMol2ToPdb(String mol2Content) {
        if (mol2Content == null || mol2Content.trim().isEmpty()) {
            return null; // Return null if the input content is empty.
        }

        Scanner scanner = new Scanner(mol2Content);
        StringBuilder pdbBuilder = new StringBuilder();
        boolean inAtomSection = false;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            // Check for the start of the atom section
            if (line.startsWith("@<TRIPOS>ATOM")) {
                inAtomSection = true;
                continue;
            }

            // Check for the end of the atom section
            if (line.startsWith("@<TRIPOS>") && inAtomSection) {
                break;
            }

            // Process atom lines
            if (inAtomSection) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 6) {
                    int atomId = Integer.parseInt(parts[0]);
                    String atomSymbol = parts[5].substring(0, 1); // Assuming the first character is the element symbol
                    String x = String.format("%8.3f", Double.parseDouble(parts[2]));
                    String y = String.format("%8.3f", Double.parseDouble(parts[3]));
                    String z = String.format("%8.3f", Double.parseDouble(parts[4]));

                    // Construct the PDB HETATM line
                    String pdbLine = String.format(
                            "HETATM%5d %-4s MOL     1    %s%s%s  1.00  0.00           %2s\n",
                            atomId, atomSymbol, x, y, z, atomSymbol);
                    pdbBuilder.append(pdbLine);
                }
            }
        }

        pdbBuilder.append("END\n");
        scanner.close();
        return pdbBuilder.toString();
    }

    public int moveFile(String sourceFile, String destinationDirectoryPath) {
        System.out.println("source to move " + sourceFile);
        System.out.println("destination to move " + destinationDirectoryPath);
        Path sourcePath = Paths.get(sourceFile);
        Path destinationDirectory = Paths.get(destinationDirectoryPath);
        Path destinationPath = destinationDirectory.resolve(sourcePath.getFileName());

        try {
            // Check if the destination directory exists, create it if not
            if (!Files.exists(destinationDirectory)) {
                Files.createDirectories(destinationDirectory);
            }

            Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File moved successfully.");
            return 1;
        } catch (IOException e) {
            System.out.println("Error occurred while moving the file.");
            e.printStackTrace();
            return 0;
        }
    }

    public String convertPDBQTtoPDB(String pdbqtFilePath, String pdbFilePath) {
        System.out.println("got input file to pdbqt to pdb = " + pdbqtFilePath);
        System.out.println("got output file to pdbqt to pdb = " + pdbFilePath);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(openBabelPath, "-ipdbqt", pdbqtFilePath, "-opdb",
                    pdbFilePath);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Conversion failed with exit code " + exitCode);
            }
        } catch (Exception e) {
            return "Error:" + e.getMessage();
        }

        return "Sucess:conversion success";

         }

    
          
    public String getFileNameFromPath(String filePath, boolean withExt) {
        File file = new File(filePath);
        String fileNameWithExtension = file.getName();

        int lastDotIndex = fileNameWithExtension.lastIndexOf('.');
        if (withExt) {
            return fileNameWithExtension;
        } else {
            return lastDotIndex == -1 ? fileNameWithExtension
                    : fileNameWithExtension.substring(0, lastDotIndex);
        }
    }

    public String createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            boolean result = directory.mkdirs();
            if (!result) {
                System.err.println("Failed to create directory: " + path);
                return null;
            }
        }
        return directory.getAbsolutePath();
    }

    public List<String> readPdbqt(String filename) throws IOException {
        return Files.lines(Paths.get(filename))
                .filter(line -> line.startsWith("ATOM") || line.startsWith("HETATM"))
                .collect(Collectors.toList());
    }

    public boolean isFolderEmpty(String path) {
        File folder = new File(path);
        if (folder.exists() && folder.isDirectory()) {
            String[] files = folder.list();
            return (files == null || files.length == 0);
        }
        return true;
    }

    public String getFolderPath(String fullPath) {
        File file = new File(fullPath);
        return file.getParent();
    }

    public List<String> findFilesWithPrefix(String directoryPath, String prefix) {
        File directory = new File(directoryPath);
        List<String> filePaths = new ArrayList<>();

        // Check if directory exists and is indeed a directory
        if (directory.exists() && directory.isDirectory()) {
            // Filename filter to match files starting with the specified prefix
            FilenameFilter filter = (dir, name) -> name.startsWith(prefix);

            // List the files using the filter
            File[] files = directory.listFiles(filter);

            if (files != null) {
                for (File file : files) {
                    filePaths.add(file.getAbsolutePath());
                }
            }
        }

        return filePaths;
    }

    public List<String>getDockedLigandFilePaths(String jobName,String email){
        List<String>liganFilePaths = new ArrayList<>();
        try {
            System.out.println("user directory ="+userDirectory);
            String folderPath = userDirectory+File.separator+email+File.separator+jobName+File.separator+"output"+File.separator+"complex_lig";
            System.out.println("folder path= "+folderPath);
            File folder = new File(folderPath);
            if(folder.exists()) {
            	System.out.println("is exist="+folderPath);
            	 String[] contents = folder.list();
            	 if (contents == null || contents.length == 0) {
                     System.out.println("The folder is empty.");
                 } else {
                     System.out.println("The folder is not empty.");
                     System.out.println("Content of folder = "+contents);
                 }
            }
            
            
            
            Files.walk(Paths.get(folderPath))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(jobName) && path.getFileName().toString().endsWith(".pdb"))
                    .forEach(path -> liganFilePaths.add(path.toString()));
            System.out.println("file paths = "+liganFilePaths);

        }catch (Exception e){
            return null;
        }
        return liganFilePaths;
    }

    public String getFilePathFromName(String jobName,String email,String fileType,String fileName){
        Job job = jobService.getByNameAndEmail(jobName, email);
        if (fileType.equalsIgnoreCase("protein"))
            return userDirectory + File.separator + email + File.separator + jobName + File.separator + fileType+ File.separator+fileName;
                    //+ File.separator + job.getProteinFile();
        else if (fileType.equalsIgnoreCase("ligand"))
            return userDirectory + File.separator + email + File.separator + jobName + File.separator + fileType +
                    File.separator + fileName;
        else if (fileType.equalsIgnoreCase("output")) {
            return userDirectory + File.separator + email + File.separator + jobName + File.separator + fileType
                    + File.separator +"complex_lig"+File.separator+fileName;
        }
        else if(fileType.equalsIgnoreCase("clean")) {
        	String proteinFile = userDirectory + File.separator + email + File.separator + jobName + File.separator + "protein"+ File.separator+fileName;
        	System.out.println("Protein file Name="+proteinFile);
        	System.out.println("CLean File Name "+userDirectory + File.separator + email + File.separator + jobName + File.separator + "protein"+ File.separator+fileName.replace(".pdb", "_clean.pdb"));
        	return userDirectory + File.separator + email + File.separator + jobName + File.separator + "protein"+ File.separator+fileName.replace(".pdb", "_clean.pdb");
        }
        else if (fileType.equalsIgnoreCase("vinaJSON")) {
            return userDirectory +File.separator +
                    email + File.separator+
                    jobName + File.separator +
                    "vinaconfig"+ File.separator +
                    job.getJobName()+"_out.json";
        }
        else
            return "";
    }

    public String convertProteinPDBQTtoPDB(String inputFilePath, String outputFilePath) throws IOException, InterruptedException {
        String output = "";
        Path tempScript = null;
        try {
        	/*listResourceContents();
            // Get the path to the Python script
           // Resource resource = resourceLoader.getResource("classpath:scripts/proteinPDBQTtoPDB.py");
            Resource resource = resourceLoader.getResource(pdbqtTopdb);
            System.out.println("Resource path: " + resource.getURL().getPath());
            if (!resource.exists()) {
                throw new FileNotFoundException("Python script not found in classpath");
            }
            
            tempScript = Files.createTempFile("converter", ".py");
            Files.copy(resource.getInputStream(), tempScript, StandardCopyOption.REPLACE_EXISTING);
*/
            // Build the command
            ProcessBuilder processBuilder = new ProcessBuilder("python", pdbqtTopdb, inputFilePath, outputFilePath);
            processBuilder.redirectErrorStream(true);

            // Start the process
            Process process = processBuilder.start();

            // Read the output
            output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // Wait for the process to complete
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Python script execution failed. Exit code: " + exitCode + ". Output: " + output);
            }
        } catch (Exception e) {
            System.err.println("Exception in converting file pdbqt to pdb: " + e.getMessage());
            e.printStackTrace();  // This will print the full stack trace for debugging
            return "";
        } finally {
            // Delete the temporary script file
            if (tempScript != null) {
                try {
                    Files.deleteIfExists(tempScript);
                } catch (IOException e) {
                    System.err.println("Failed to delete temporary script file: " + e.getMessage());
                }
            }
        }

        return output;
    }
    private void listResourceContents() {
        try {
            Resource resource = resourceLoader.getResource("classpath:scripts");
            File file = resource.getFile();
            if (file.isDirectory()) {
                System.out.println("Contents of scripts directory:");
                for (File f : file.listFiles()) {
                    System.out.println(f.getName());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to list resource contents: " + e.getMessage());
        }
    }

	public String getScore(String jsonFilePath,String ligandFileName) {
		try {
			int mode = extractNumberUsingRegex(ligandFileName);
			 ObjectMapper objectMapper = new ObjectMapper();
			 List<Map<String, Object>> jsonList = objectMapper.readValue(new File(jsonFilePath),
	                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));

			 for (Map<String, Object> item : jsonList) {
	                if (item.get("mode").equals(mode)) {
	                    //double affinity = (double) item.get("affinity");
	                    return ""+ item.get("affinity");
	                    //return String.format("%.1f", affinity);
	                }
	            }

	            System.out.println("Mode " + mode + " not found in the JSON file.");
	            return "";
		} catch (Exception e) {
			System.out.println("Error in getting docking score "+e.getMessage());
			return "";
		}
	}
	 public int extractNumberUsingRegex(String fileName) {
	        Pattern pattern = Pattern.compile("_ligand_(\\d+)\\.pdb$");
	        Matcher matcher = pattern.matcher(fileName);
	        if (matcher.find()) {
	        	int mode = Integer.parseInt(matcher.group(1));
	            //return matcher.group(1);
	        	return mode;
	        }
	        return 0;
	    }
    }

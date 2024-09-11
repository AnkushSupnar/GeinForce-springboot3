package com.geinforce.service;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class MGLToolsService {
    @Value("${file.upload.pdb}")
    String uploadPDBPath;
    @Value("${tool.path.python}")
    String pythonPath;
    @Value("${tool.path.prepare_ligand4}")
    String ligandPath;
    @Value("${tool.path.prepare_receptor4}")
    String receptorPath;
    @Value("${file.user}")
    String userDirectory;
    @Autowired
    JobService jobService;

    public String convertProteinToPDBQT(String filePath) {
        String outputFile = "";
        try {
            System.out.println("pdbFilePath=" + filePath);
            String newExtension = ".pdbqt";
            Path inFilePath = Paths.get(filePath);
            String filenameWithoutExtension = inFilePath.getFileName().toString().replaceFirst("[.][^.]+$", "");
            Path folderPath = inFilePath.getParent();
            String newFilePath = folderPath.resolve(filenameWithoutExtension + newExtension).toString();
            System.out.println("out file path=" + newFilePath);
            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonPath,
                    receptorPath,
                    "-r",
                    filePath,
                    "-o",
                    newFilePath);
            System.out.println(processBuilder.command());
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("reader line=" + line);
            }
            if (exitCode != 0) {
                return "Server Error";
            }
            System.out.println("Exit Code=" + exitCode);
            System.out.println("Get errors");
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println(errorLine);
            }
            outputFile = newFilePath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputFile;
    }

    public String convertLigandToPDBQT(String filePath) {
        String outputFile = "";
        try {
            System.out.println("pdbFilePath=" + filePath);
            System.out.println("python path=" + pythonPath);
            System.out.println("ligandPath path=" + ligandPath);
            String newExtension = ".pdbqt";
            Path inFilePath = Paths.get(filePath);
            String filenameWithoutExtension = inFilePath.getFileName().toString().replaceFirst("[.][^.]+$", "");
            Path folderPath = inFilePath.getParent();
            String newFilePath = folderPath.resolve(filenameWithoutExtension + newExtension).toString();
            System.out.println("out file path=" + newFilePath);
            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonPath,
                    ligandPath,
                    "-l",
                    inFilePath.getFileName().toString(),
                    "-o",
                    newFilePath);
            // processBuilder.directory(new File("D:\\RMI\\uploads"));
            processBuilder.directory(folderPath.toFile());
            System.out.println(processBuilder.command());
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            System.out.println("process exit code=" + exitCode);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            if (exitCode != 0) {
                return "Server Error";
            }
            System.out.println("Get errors");
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println(errorLine);
            }
            outputFile = newFilePath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputFile;
    }

    String createVinaConfig(String email, String jobName, String receptorPath, String ligandPath,
            double centerX, double centerY, double centerZ,
            double sizeX, double sizeY, double sizeZ) {

        double[] center = findActiveSite(receptorPath);
        String fileName = jobName + "_vinaconfig.txt";
        String directoryPath = userDirectory + File.separator + email + File.separator + jobName + File.separator
                + "vinaconfig";
        String outputPath = directoryPath + File.separator + fileName;
        Path dirPath = Paths.get(directoryPath);
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to create directory: " + dirPath);
            }
        }
        try {
            writeConfigToFile(receptorPath, ligandPath, center, outputPath);
            String coordinates = " {\"center\":{\"x\":"+center[0]+",\"y\":"+center[1]+",\"z\":"+center[2]+"},\"size\":{\"x\":40,\"y\":40,\"z\":40}}";
            jobService.updateJobCoordinates(jobName, email, coordinates);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputPath;
        /*
         * String fileName=jobName+"_vinaconfig.txt";
         * String directoryPath = userDirectory + File.separator + email +
         * File.separator + "vinaconfig";
         * String outputPath = directoryPath + File.separator + fileName;
         * Path dirPath = Paths.get(directoryPath);
         * if (!Files.exists(dirPath)) {
         * try {
         * Files.createDirectories(dirPath);
         * } catch (IOException e) {
         * e.printStackTrace();
         * throw new RuntimeException("Failed to create directory: " + dirPath);
         * }
         * }
         * try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath)))
         * {
         * writer.write("receptor = " + receptorPath + "\n");
         * writer.write("ligand = " + ligandPath + "\n\n");
         * writer.write("center_x = " + centerX + "\n");
         * writer.write("center_y = " + centerY + "\n");
         * writer.write("center_z = " + centerZ + "\n\n");
         * writer.write("size_x = " + sizeX + "\n");
         * writer.write("size_y = " + sizeY + "\n");
         * writer.write("size_z = " + sizeZ + "\n\n");
         * writer.write("exhaustiveness = 8\n");
         * writer.write("num_modes = 9\n");
         * writer.write("energy_range = 3\n");
         * } catch (IOException e) {
         * e.printStackTrace();
         * }
         * return outputPath;
         */
    }

    public static double[] findActiveSite(String pdbqtFile) {
        PDBFileReader pdbReader = new PDBFileReader();
        double[] center = new double[3];
        try {
            Structure structure = pdbReader.getStructure(pdbqtFile);
            List<Atom> atoms = List.of(StructureTools.getAllAtomArray(structure));
            double sumX = 0, sumY = 0, sumZ = 0;

            for (Atom atom : atoms) {
                sumX += atom.getX();
                sumY += atom.getY();
                sumZ += atom.getZ();
            }

            center[0] = sumX / atoms.size();
            center[1] = sumY / atoms.size();
            center[2] = sumZ / atoms.size();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return center;
    }

    public static void writeConfigToFile(String proteinFile, String ligandFile, double[] center, String outpputpath)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outpputpath));
        writer.write("receptor = " + proteinFile + "\n");
        writer.write("ligand = " + ligandFile + "\n\n");
        // writer.write("out = out.pdbqt\n\n");
        writer.write("center_x = " + center[0] + "\n");
        writer.write("center_y = " + center[1] + "\n");
        writer.write("center_z = " + center[2] + "\n\n");
        writer.write("size_x = 40\n");
        writer.write("size_y = 40\n");
        writer.write("size_z = 40\n\n");
        writer.write("exhaustiveness = 5\n");
        writer.close();
    }

}

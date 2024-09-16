package com.geinforce.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Model;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureIO;
import org.biojava.nbio.structure.StructureImpl;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.io.FileConvert;
import org.biojava.nbio.structure.io.LocalPDBDirectory.FetchBehavior;
import org.biojava.nbio.structure.io.PDBFileReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ComplexFile {
    @Autowired
    OpenBabelService openBabelService;

    @Autowired
    FileService fileService;
    
    @Value("${tool.path.protein-ligand-complex}")
    String complexScript;

    public void writeStringToFile(String content, String filePath) throws IOException {
        Files.write(Paths.get(filePath), content.getBytes());
    }

    public void generatingComplexFile(String email, String jobName) {
        // get all ligand pdbqt files
        String outputFilePath = fileService.getFilePath(jobName, email, "output");
        String fileName = fileService.getFileNameFromPath(outputFilePath, false);
        String folderPath = fileService.getFolderPath(outputFilePath);
        List<String> ligandFileList = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            //ligandFileList.add(folderPath + File.separator + fileName + "_ligand_" + i + ".pdbqt");
        	ligandFileList.add(folderPath + File.separator + fileName + "_ligand_" + i + ".pdbqt");
        }
        System.out.println("list of files " + ligandFileList);

        System.out.println("output file path for complex" + outputFilePath);
        System.out.println("output file Name for complex" + fileName);
        System.out.println("generating complex file");
        String proteinFilePath = "D:\\RMI\\ankushsupnar@gmail.com\\1705484403283\\protein\\1705484403283_protein.pdb";
        List<String> ligandFilePaths = Arrays.asList(
                "D:\\RMI\\ankushsupnar@gmail.com\\1705484403283\\output\\1705484403283_ligand_out_ligand_1.pdbqt",
                "D:\\RMI\\ankushsupnar@gmail.com\\1705484403283\\output\\1705484403283_ligand_out_ligand_2.pdbqt",
                "D:\\RMI\\ankushsupnar@gmail.com\\1705484403283\\output\\1705484403283_ligand_out_ligand_3.pdbqt",
                "D:\\RMI\\ankushsupnar@gmail.com\\1705484403283\\output\\1705484403283_ligand_out_ligand_4.pdbqt",
                "D:\\RMI\\ankushsupnar@gmail.com\\1705484403283\\output\\1705484403283_ligand_out_ligand_5.pdbqt",
                "D:\\RMI\\ankushsupnar@gmail.com\\1705484403283\\output\\1705484403283_ligand_out_ligand_6.pdbqt",
                "D:\\RMI\\ankushsupnar@gmail.com\\1705484403283\\output\\1705484403283_ligand_out_ligand_7.pdbqt",
                "D:\\RMI\\ankushsupnar@gmail.com\\1705484403283\\output\\1705484403283_ligand_out_ligand_8.pdbqt",
                "D:\\RMI\\ankushsupnar@gmail.com\\1705484403283\\output\\1705484403283_ligand_out_ligand_9.pdbqt");
        String complexFilePath = "D:\\RMI\\ankushsupnar@gmail.com\\1705484403283\\output\\complex.pdb";
        try {
            // String complexContent = generateComplexFile(proteinFilePath,
            // ligandFilePaths);
            String complexContent = generateComplexFile(jobName, folderPath + File.separator + "complex_lig",
                    ligandFileList);
            // writeStringToFile(complexContent, complexFilePath);

            System.out.println("writting complex file done");
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception
        }

    }

    public String generateComplexFile(String jobName, String complexFolderPath, List<String> ligandFilePaths)
            throws IOException {
        StringBuilder complexContent = new StringBuilder();
        File directory = new File(complexFolderPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
      //  int i = 0;
        for (String ligandFilePath : ligandFilePaths) {
            System.out.println("ligand file path in complex file=" + ligandFilePath);
            
            complexContent.append("\n").append(readFileAsString(ligandFilePath));

            String fileName  = Paths.get(ligandFilePath).getFileName().toString();
            System.out.println("ligand file Nme--------------------------------=" + fileName);
            System.out.println("ligand file Nme--------------------------------=" + fileName.replace("_out_ligand", ""));
            String ligandPDB = complexFolderPath+File.separator+fileName.replace("_out_ligand", "");

            openBabelService.convertPDBQTtoPDB(ligandFilePath,
            		ligandPDB.replace(".pdbqt", "")+ ".pdb");

//            openBabelService.convertPDBQTtoPDB(ligandFilePath,
//                    complexFolderPath + File.separator + jobName + "_" + (++i) + ".pdb");

        }

        return complexContent.toString();
    }

    public String generateComplexFile1(String proteinFilePath, List<String> ligandFilePaths) throws IOException {
        StringBuilder complexContent = new StringBuilder();
        complexContent.append(readFileAsString(proteinFilePath));
        int i = 0;
        for (String ligandFilePath : ligandFilePaths) {
            System.out.println("ligand file path in complex file=" + ligandFilePath);
            complexContent.append("\n").append(readFileAsString(ligandFilePath));

            openBabelService.convertPDBQTtoPDB(ligandFilePath,
                    "D:\\RMI\\ankushsupnar@gmail.com\\1705484403283\\output\\complex\\lig_" + (++i) + ".pdb");
        }

        return complexContent.toString();
    }

    public String readFileAsString(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    public String combineProteinAndLigand(String proteinFilePath, String ligandFilePath, String outputFolder) {
    	
        try {
            List<String> proteinContent = Files.readAllLines(Paths.get(proteinFilePath));
            List<String> ligandContent = Files.readAllLines(Paths.get(ligandFilePath));

            List<String> cleanedProteinContent = new ArrayList<>();
            cleanedProteinContent.add("REMARK  Generated by using GeinDock Suite v1.0\n");

            for (String line : proteinContent) {
                if (!(line.startsWith("REMARK   BIOVIA") || line.startsWith("REMARK   Created:"))) {
                    cleanedProteinContent.add(line);
                }
            }

            List<String> combinedContent = new ArrayList<>(cleanedProteinContent);
            combinedContent.addAll(ligandContent);

            Path outputPath = Paths.get(outputFolder, "protein-ligand.pdb");
            Files.write(outputPath, combinedContent);

            System.out.println("Combined PDB file created at: " + outputPath);
            return outputPath.toString();

        } catch (IOException e) {
            System.err.println("Error in combining PDB files: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String combinePDBFiles(String proteinPath, String ligandPath, String outputPath) throws IOException, InterruptedException {
        // Construct the command to run the Python script
        String pythonScript = complexScript;
        System.out.println("Got Script to run complex pdb: " + pythonScript);
        System.out.println("Got proteinPath to run complex pdb: " + proteinPath);
        System.out.println("Got ligandPath to run complex pdb: " + ligandPath);
        System.out.println("Got outputPath to run complex pdb: " + outputPath);

        // Replace backslashes with forward slashes
        proteinPath = proteinPath.replace("\\", "/");
        ligandPath = ligandPath.replace("\\", "/");
        outputPath = outputPath.replace("\\", "/");

        // Construct the command array
        String[] command = {"python", pythonScript, proteinPath, ligandPath, "-o", outputPath};

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        // Redirect error stream to output stream
        processBuilder.redirectErrorStream(true);

        // Start the process
        Process process = processBuilder.start();

        // Read the output from the Python script
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            output.append(line).append("\n");
        }

        // Wait for the process to complete
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Python script execution failed with exit code: " + exitCode + "\nOutput: " + output.toString());
        }

        // Verify that the output file was created
        Path outputFilePath = Paths.get(outputPath);
        if (!Files.exists(outputFilePath)) {
            throw new IOException("Output file was not created: " + outputPath);
        }

        return outputPath;
    }
}

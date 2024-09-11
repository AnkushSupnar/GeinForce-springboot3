package com.geinforce.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ComplexFile {
    @Autowired
    OpenBabelService openBabelService;

    @Autowired
    FileService fileService;

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

}

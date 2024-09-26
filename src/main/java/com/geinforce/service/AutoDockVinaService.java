package com.geinforce.service;


import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.geinforce.controller.RESTDockindSSEController;
import com.geinforce.model.JobStatus;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AutoDockVinaService {
    @Autowired
    FileService fileService;
    @Autowired
    MGLToolsService mglToolsService;
    @Autowired
    private RESTDockindSSEController sseController;

    @Autowired
    JobService jobService;
    @Value("${tool.path.autodockvina}")
    String autoDockVinaPath;
    @Value("${tool.path.autoDockVinaSplit}")
    String vinaSplitPath;

    @Autowired
    OpenBabelService openBabelService;

    public String blindDocking(String jobName, String email) {
        System.out.println("Docking using AUTODOCK vina");
        /*
         * Tasks to perform
         * 1.get the filen name of uploaded files
         * 2. convert the files in pdbqt fomat
         * 3. generate config file
         * 4. perform docking
         */
        try {
            String proteinFilePath = fileService.getFilePath(jobName, email, "protein");
            String ligandFilePath = fileService.getFilePath(jobName, email, "ligand");
            // sseController.sendUpdateToClients("File Path Reading success");
            sseController.sendUpdateToClient(
                    "{\"jobName\":\"" + jobName + "\",\"message\":\"File Path Reading success\",\"status\":2}",jobName);
            jobService.updateJobStatus(jobName, email, JobStatus.RUNNING);
            System.out.println("ligandFile path" + ligandFilePath);
            System.out.println("proteinFilePath" + proteinFilePath);
            if(ligandFilePath.endsWith(".sdf")||ligandFilePath.endsWith(".mol")||ligandFilePath.endsWith(".mol2")) {
            	System.out.println("Got ligand not in pdb");
            	ligandFilePath = fileService.convertFileSdfToPdb(ligandFilePath,ligandFilePath.replaceAll("\\.(mol2|mol|sdf)$", ".pdb"));
            }
         /*   if (ligandFilePath.endsWith(".sdf1")) {
            	System.out.println("Got ligand in .sdf");
               // File ligandFile = new File(ligandFilePath);
//                ligandFilePath = fileService.convertSdfToPdbqt(ligandFilePath,
//                        ligandFile.getParent() + File.separator + ligandFile.getName());
                ligandFilePath = fileService.convertFileSdfToPdb(ligandFilePath,ligandFilePath.replace(".sdf", ".pdb"));
                System.out.println("Got ligand in .sdf to pdb= "+ligandFilePath);
            }
            if (ligandFilePath.endsWith(".mol1")) {
            	System.out.println("Got ligand in .mol");
            	 //File ligandFile = new File(ligandFilePath);
            	 ligandFilePath = fileService.convertFileMolToPdb(ligandFilePath,ligandFilePath.replace(".mol", ".pdb"));
                 System.out.println("Got ligand in .sdf to pdb= "+ligandFilePath);
            }
            //create clean protein file
            
          //  String file[] = proteinFilePath.split(".");
           // String filepath = 
           */ 
            
            
            
            String protienResult = mglToolsService.convertProteinToPDBQT(proteinFilePath);
            
           
            String cleanProteinName = proteinFilePath.replace(".pdb","_clean.pdb");
            System.out.println("Cleaned file Name "+cleanProteinName);
            String clenedFileName =  fileService.convertProteinPDBQTtoPDB(protienResult, cleanProteinName);
            System.out.println("Cleaned File path = "+clenedFileName);
         
            System.out.println("protein result=" + protienResult);
            String ligandResult = mglToolsService.convertLigandToPDBQT(ligandFilePath);

            sseController.sendUpdateToClient(
                    "{\"jobName\":\"" + jobName + "\",\"message\":\"File Converting success\",\"status\":3}",jobName);
            String configFilePath = mglToolsService.createVinaConfig(
                    email, jobName,
                    protienResult, ligandResult,
                    10, 20, 30, 20, 20, 20);
            sseController.sendUpdateToClient(
                    "{\"jobName\":\"" + jobName + "\",\"message\":\"File Config creating success\",\"status\":4}",jobName);
            // storing output file path in db
            jobService.updateJobOutputPath(jobName, email);
            return autoDockVinaDock(configFilePath, jobName, email);
        } catch (Exception e) {
            System.out.println("error in docking=" + e.getMessage());
            sseController.sendUpdateToClient(
                    "{\"jobName\":\"" + jobName + "\",\"message\":\"Error in docking\",\"status\":4}",jobName);
            jobService.updateJobStatus(jobName, email, JobStatus.ERROR);
            return "Error in converting file";
        }
    }

    public String autoDockVinaDock(String configFilePath, String jobName, String email) {
        System.out.println("Got out put file path in vina=" + configFilePath);
        sseController.sendUpdateToClient(
                "{\"jobName\":\"" + jobName + "\",\"message\":\"File Docking Starte\",\"status\":5}",jobName);
        StringBuilder output = new StringBuilder();
        try {
            String[] command = {
                    autoDockVinaPath,
                    "--config", configFilePath
            };
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            System.out.println("Getting Output");
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                // System.out.println("ankush=" + line);
                if (line.startsWith("mode") || !line.startsWith("Writing")) {
                    System.out.println(line);
                }
            }
            System.out.println("output=" + output);
            int exitCode = process.waitFor();

            System.out.println("Vina exited with code: " + exitCode);
            if (exitCode == 0) {
                extractTableAndWriteToJson(output.toString(),
                        new File(configFilePath).getParent() + File.separator + jobName + "_out.json");
                sseController.sendUpdateToClient("{\"jobName\":\"" + jobName
                        + "\",\"message\":\"Docking result extracted success\",\"status\":6}",jobName);
                sseController.sendUpdateToClient(
                        "{\"jobName\":\"" + jobName + "\",\"message\":\"Docking Completed\",\"status\":7}",jobName);
                String ligandFilePath = fileService.getFilePath(jobName, email, "ligand");
                File ligandFile = new File(ligandFilePath);
                String ligandDir = ligandFile.getParent();
                System.out.println("ligand file dir path = " + ligandDir);
                String outputFileName = jobService.getJobOutputFile(jobName);
                System.out.println("Output file Name----------"+outputFileName);

                Path path = Paths.get(ligandDir);
                int moveResult = fileService.moveFile(ligandDir + File.separator + outputFileName,
                        path.getParent() + File.separator + "output");
                if (moveResult == 1) {
                    sseController.sendUpdateToClient(
                            "{\"jobName\":\"" + jobName + "\",\"message\":\"Moved Output File\",\"status\":8}",jobName);
                    splitVinaResults(path.getParent() + File.separator + "output" + File.separator + outputFileName,
                            path.getParent() + File.separator + "output");
                    System.out.println("Vina split Done-------------------");
                }
                // convert output to pdb
                // String pdbqtToPdb = fileService.convertPDBQTtoPDB(
                // path.getParent() + File.separator + "output" + File.separator +
                // outputFileName,
                // path.getParent() + File.separator + "output" + File.separator
                // + outputFileName.substring(0, outputFileName.length() - "pdbqt".length()) +
                // "pdb"

                // );
                
                System.out.println("input file path for conversion "+(path.getParent() + File.separator + "output" + File.separator + outputFileName));
                System.out.println("output file path for conversion "+(path.getParent() + File.separator + "output" + File.separator
                        + outputFileName.substring(0, outputFileName.length() - "pdbqt".length()) + "pdb"));
                openBabelService.convertPDBQTtoPDB(
                        path.getParent() + File.separator + "output" + File.separator + outputFileName,
                        path.getParent() + File.separator + "output" + File.separator
                                + outputFileName.substring(0, outputFileName.length() - "pdbqt".length()) + "pdb");

                // System.out.println("result of conversion = " + pdbqtToPdb);

                jobService.updateJobStatus(jobName, email, JobStatus.COMPLETED);
                sseController.sendUpdateToClient(
                        "{\"jobName\":\"" + jobName + "\",\"message\":\"Convert output file \",\"status\":9}",jobName);
                
                sseController.sendUpdateToClient(
                        "{\"jobName\":\"" + jobName + "\",\"message\":\"Task completed. Closing connection. \",\"status\":10}",jobName);
                sseController.closeConnection(jobName);
                return "Docking completed";
            } else {
                jobService.updateJobStatus(jobName, email, JobStatus.ERROR);
                return "Error in Docking";
            }
        } catch (Exception e) {
            System.out.println("Error in vina docking=" + e.getMessage());
            jobService.updateJobStatus(jobName, email, JobStatus.ERROR);
            e.printStackTrace();
            return "Error in Docking";
        }
    }

    private void extractTableAndWriteToJson(String output, String jsonFilePath) {
        Pattern pattern = Pattern.compile("(\\d+)\\s+(-?\\d+\\.\\d+)\\s+(\\d+\\.\\d+)\\s+(\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(output);

        JSONArray jsonArray = new JSONArray();
        while (matcher.find()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mode", Integer.parseInt(matcher.group(1)));
            jsonObject.put("affinity", Double.parseDouble(matcher.group(2)));
            jsonObject.put("dist_from_best_mode_rmsd_l.b.", Double.parseDouble(matcher.group(3)));
            jsonObject.put("dist_from_best_mode_rmsd_u.b.", Double.parseDouble(matcher.group(4)));
            jsonArray.put(jsonObject);
        }
        System.out.println("Json op= " + jsonArray);
        try (FileWriter file = new FileWriter(jsonFilePath)) {
            file.write(jsonArray.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void splitVinaResults(String pdbqtFilePath, String storageDirectoryPath) {
        try {
            System.out.println("pdbqt file to spit " + pdbqtFilePath);
            System.out.println("destination file path " + storageDirectoryPath);
            System.out.println("vina split path " + vinaSplitPath);

            // Run vina_split
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(vinaSplitPath, "--input", pdbqtFilePath);
            Process process = builder.start();

            // Read the output and error streams
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = outputReader.readLine()) != null) {
                System.out.println(line);
            }
            while ((line = errorReader.readLine()) != null) {
                System.err.println("Erorr in splitting-----------"+line);
            }

            // Wait for the process to complete
            process.waitFor();

            // Get the directory of the pdbqt file
            File pdbqtFile = new File(pdbqtFilePath);
            File parentDir = pdbqtFile.getParentFile();

            // Move the split files to the storage directory
            File[] files = parentDir.listFiles();
            
            if (files != null) {
                for (File file : files) {
                    // Check if the file is a split result
                    if (file.getName().startsWith("ligand") && file.getName().endsWith(".pdbqt")) {
                    	System.out.println("Vina split file Name-----------------"+file.getName());
                        Path sourcePath = file.toPath();
                        Path destinationPath = Paths.get(storageDirectoryPath, file.getName());
                        Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Moved file: " + file.getName());
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String generateComplexFile(String proteinFile, String ligandFile) {

        try {
            List<String> ligandAtomLines = fileService.readPdbqt(ligandFile);
            List<String> proteinAtomLines = fileService.readPdbqt(proteinFile);

            String complexPdbContent = mergeAndFormatPdbqt(ligandAtomLines, proteinAtomLines);

            File ligFile = new File(ligandFile);
            String complexFilePath = fileService
                    .createDirectoryIfNotExists(ligFile.getParent() + File.separator + "complex");

            Path outputPath = Paths.get(
                    complexFilePath + File.separator + fileService.getFileNameFromPath(ligandFile, false) + ".pdb");
            Files.write(outputPath, complexPdbContent.getBytes());
            System.out.println("Complex PDB file created at: " + outputPath.toAbsolutePath());
            return outputPath.toAbsolutePath().toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    public String mergeAndFormatPdbqt(List<String> ligandAtomLines, List<String> proteinAtomLines) {
        String pdbContent = "REMARK   Generated Complex Structure\n";
        pdbContent += String.join("\n", proteinAtomLines) + "\nTER\n";
        pdbContent += String.join("\n", ligandAtomLines) + "\nTER\nEND\n";
        return pdbContent;
    }
}

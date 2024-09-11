package com.geinforce.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
//import org.openbabel.OBConversion;
//import org.openbabel.OBMol;
import org.springframework.stereotype.Service;

@Service
public class OpenBabelService {

    @Value("${tool.path.openbabel}")
    String openBabelPath;

    public void convertPDBQTtoPDB(String inputFilePath, String outputFilePath) {
        // try {
        /*
         * System.out.println("OpenBabelService convertPDBQTtoPDB input file: " +
         * inputFilePath);
         * System.out.println("OpenBabelService convertPDBQTtoPDB output file: " +
         * outputFilePath);
         * // Constructing the command to execute
         * // String command = openBabelPath + " -i pdbqt " + inputFilePath + " -o pdb "
         * +
         * // outputFilePath;
         * String command = openBabelPath + " -ipdbqt " + inputFilePath + " -opdb -O " +
         * outputFilePath;
         * // Executing the command
         * Process process = Runtime.getRuntime().exec(command);
         * 
         * // Waiting for the process to finish
         * process.waitFor();
         * 
         * System.out.println("Conversion completed: " + outputFilePath);
         */
        StringBuilder pdbLines = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ATOM") || line.startsWith("HETATM")) {
                    String pdbLine = line.substring(0, 66); // Extract the PDB part of the PDBQT line
                    pdbLines.append(pdbLine).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // return pdbLines.toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write(pdbLines.toString());
            System.out.println("File saved successfully at " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // } catch (IOException | InterruptedException e) {
        // e.printStackTrace();
        // System.out.println("Error during conversion: " + e.getMessage());
        // }
    }

}

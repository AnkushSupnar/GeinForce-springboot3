package com.geinforce.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test {

	public static void main(String[] args) {
		Test test = new Test();
		try {
			test.generateComplex("D:\\RMI\\ankushsupnar@gmail.com\\1726251077956\\protein\\1726251077956_protein_clean.pdb",
					"D:\\RMI\\ankushsupnar@gmail.com\\1726251077956\\output\\complex_lig\\1726251077956_M1_1.pdb",
					"D:\\RMI\\ankushsupnar@gmail.com\\1726251077956\\output\\complex_protein\\op.pdb");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	  public  String generateComplex(String proteinPath, String ligandPath, String outputPath) throws IOException {
	        List<String> proteinLines = readPDBFile(proteinPath);
	        List<String> ligandLines = readPDBFile(ligandPath);
	        List<String> conectRecords = extractConectRecords(ligandPath);
	        
	        List<String> complexLines = new ArrayList<>(proteinLines);
	        complexLines.add("TER");
	        complexLines.addAll(ligandLines);
	        complexLines.addAll(conectRecords);
	        
	        // Add MASTER and END records
	        complexLines.add(generateMasterRecord(complexLines));
	        complexLines.add("END");
	        
	        writePDBFile(outputPath, complexLines);
	        return outputPath;
	    }
	    private  List<String> readPDBFile(String filePath) throws IOException {
	        List<String> lines = new ArrayList<>();
	        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                if (line.startsWith("ATOM") || line.startsWith("HETATM")) {
	                    lines.add(line);
	                }
	            }
	        }
	        return lines;
	    }
	    private  List<String> extractConectRecords(String filePath) throws IOException {
	        List<String> conectRecords = new ArrayList<>();
	        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                if (line.startsWith("CONECT")) {
	                    conectRecords.add(line);
	                }
	            }
	        }
	        return conectRecords;
	    }
	    private  String generateMasterRecord(List<String> lines) {
	        int numRemark = 0;
	        int numHet = 0;
	        int numHelix = 0;
	        int numSheet = 0;
	        int numTurn = 0;
	        int numSite = 0;
	        int numXform = 0;
	        int numCoord = 0;
	        int numTer = 0;
	        int numConect = 0;
	        int numSeq = 0;

	        for (String line : lines) {
	            if (line.startsWith("REMARK")) numRemark++;
	            else if (line.startsWith("HET")) numHet++;
	            else if (line.startsWith("HELIX")) numHelix++;
	            else if (line.startsWith("SHEET")) numSheet++;
	            else if (line.startsWith("TURN")) numTurn++;
	            else if (line.startsWith("SITE")) numSite++;
	            else if (line.startsWith("ORIGX") || line.startsWith("SCALE") || line.startsWith("MTRIX")) numXform++;
	            else if (line.startsWith("ATOM") || line.startsWith("HETATM")) numCoord++;
	            else if (line.startsWith("TER")) numTer++;
	            else if (line.startsWith("CONECT")) numConect++;
	            else if (line.startsWith("SEQRES")) numSeq++;
	        }

	        return String.format("MASTER    %5d%5d%5d%5d%5d%5d%5d%5d%5d%5d%5d%5d",
	                numRemark, 0, numHet, numHelix, numSheet, numTurn, numSite, numXform,
	                numCoord, numTer, numConect, numSeq);
	    }
	    
	    private  void writePDBFile(String filePath, List<String> lines) throws IOException {
	        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
	            for (String line : lines) {
	                writer.write(line);
	                writer.newLine();
	            }
	        }
	    }

}

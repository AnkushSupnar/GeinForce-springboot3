package com.geinforce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geinforce.dto.Coordinates;
import com.geinforce.model.Job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DockService {

    @Autowired
    JobService jobService;
    @Autowired
    TemplateBasedDockingService templateBasedDockingService;
    @Autowired
    AutoDockVinaService autoDockVinaService;
    @Autowired
    FileService fileService;

    @Async
    public Coordinates getGrid(String jobName, String email) {
        Job job = jobService.getByNameAndEmail(jobName, email);
        System.out.println("Job to docking" + job);
        String grid = job.getCoordinates();
        Coordinates coordinate = null;
        if (!grid.isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                coordinate = objectMapper.readValue(grid, Coordinates.class);
                System.out.println("Got coordinates=" + coordinate);
            } catch (Exception e) {
                System.out.println("Exception in converting coordinates" + e.getMessage());
            }
        }
        System.out.println("Job to docking");
        return coordinate;
    }

    // @Async
    public String startDocking(String jobName, String email) {
        Coordinates coordinate = getGrid(jobName, email);
        boolean isGrid = coordinate != null && coordinate.getCenter().getX() != 0 && coordinate.getCenter().getY() != 0
                && coordinate.getCenter().getZ() != 0;

        return autoDockVinaService.blindDocking(jobName, email);
        /*
         * if(isGrid){
         * //structure based docking
         * return "structure based";
         * }
         * else{
         * //template based docking
         * return templateBasedDockingService.templateBasedDocking(jobName,email);
         * // return "template based";
         * }
         */
    }

	public String getProteinComplexPath(String fileName, String  jobName,String email) {
		try {
			Job job = jobService.getJobByName(jobName);
			String proteinFilePath = fileService.getFilePath(jobName,email,"protein");
			System.out.println("Protein file path to make complex======"+proteinFilePath);
			String ligandComplexFolderPath = fileService.getFilePath(jobName,email,"complex_lig");
			System.out.println("Ligand_complex path=========="+ligandComplexFolderPath);
			
			String ligandFilePath = ligandComplexFolderPath+(fileName.replace("protein_", ""));
			System.out.println("Got ligand file path ==========="+ligandFilePath);
			
			
			String proteinComplexFolder = ligandComplexFolderPath.replace("complex_lig","complex_protein");
			System.out.println("complex_protein path ========================="+proteinComplexFolder);
			Path path = Paths.get(proteinComplexFolder).toAbsolutePath();
			//create complex_protein folder if not there
			 if (!Files.exists(path)) {
		            try {
		                Files.createDirectories(path);
		                System.out.println("Folder created successfully: " + path);
		            } catch (IOException e) {
		                System.err.println("Error creating folder: " + e.getMessage());
		            }
		        } else {
		            System.out.println("Folder already exists: " + path);
		        }
			
			
		}catch(Exception e) {
			System.out.println("Error in getProteinComplexPath --------------"+e.getMessage());
			return "";
		}
		
		
		return "";
	}

}

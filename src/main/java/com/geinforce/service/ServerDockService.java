package com.geinforce.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class ServerDockService {
	 @Autowired
	    private FileService fileService;

	
	 public String uploadFile(MultipartFile file, String email, String jobName, String fileType) {
	        return fileService.uploadProteinFile(file, email, jobName, fileType);
	    }
	 public ResponseEntity<Resource> getProteinFile(String jobName, String email) {
	        String proteinFilePath = fileService.getFilePath(jobName, email, "protein");
	        System.out.println("use protein file path for output " + proteinFilePath);

	        File file = new File(proteinFilePath);
	        InputStreamResource resource = null;
	        try {
	            resource = new InputStreamResource(new FileInputStream(file));
	            return ResponseEntity.ok()
	                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
	                    .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
	                    .body(resource);
	        } catch (FileNotFoundException e) {
	            return ResponseEntity.ok().body(new ByteArrayResource(e.getMessage().getBytes()));
	        }

	    }

	 public ResponseEntity<String> getOutputTable(String jobName, String email) {
	        try {
	        	
	        	System.out.println("Get Output table Job Name="+jobName+" email="+email);
	            String vinaJsonFilePath = fileService.getFilePath(jobName, email, "vinaJSON");
	            String proteinFilePath = fileService.getFilePath(jobName, email, "protein");
	            List<String> ligandFilesPath = fileService.getDockedLigandFilePaths(jobName, email);
	            	
	            ObjectMapper objectMapper = new ObjectMapper();
	            List<Map<String, Object>> myData = objectMapper.readValue(new File(vinaJsonFilePath), new TypeReference<>() {});
	            JSONArray jsonArray = new JSONArray();

	            int index = 0; // Use counter to track current index
	            for (Map<String, Object> map : myData) {
	                JSONObject obj = new JSONObject();
	                map.forEach(obj::put); // Directly add all key-value pairs to the JSONObject
	                obj.put("ligand", ligandFilesPath.get(index));
	                obj.put("complex", ligandFilesPath.get(index));
	                jsonArray.put(obj);
	                index++; // Increment index for each iteration
	            }

	            // Consider using a logger here instead of System.out
	          //  System.out.println("JSON Array: " + jsonArray.toString());
	            return ResponseEntity.ok(jsonArray.toString()); // Returning the JSON array as a response entity

	        } catch (Exception e) {
	            // Log the error or send a custom error response
	            System.err.println("Error processing output table: " + e.getMessage());
	            return ResponseEntity.internalServerError().body("Error processing output table");
	        }
	    }


}

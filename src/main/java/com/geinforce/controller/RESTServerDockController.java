package com.geinforce.controller;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.geinforce.model.Job;
import com.geinforce.model.JobStatus;
import com.geinforce.service.ComplexFile;
import com.geinforce.service.DockService;
import com.geinforce.service.FileService;
import com.geinforce.service.JobService;
import com.geinforce.service.ServerDockService;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@RestController
@RequestMapping("/api")
public class RESTServerDockController {
	@Autowired
    private ServerDockService serverDockService;

	 @Autowired
	 FileService fileService;
    @Autowired
    private JobService jobService;
    @Autowired
    private RESTDockindSSEController sseController;
    @Autowired
    DockService dockService;
    @Autowired
    ComplexFile complexFile;

   
    
    @PostMapping("/serverDockUpload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobName") String jobName,
            @RequestParam("coordinates") String coordinatesJson
    ) {
        // Get the authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        System.out.println("email=" + email);
        System.out.println("jobName=" + jobName);
        System.out.println("coordinates=" + coordinatesJson);

        Map<String, String> response = new HashMap<>();

        response.put("ProteinUpload", serverDockService.uploadFile(file, email, jobName, "protein"));
        if (response.get("ProteinUpload").contains("Success")) {
            Job job = jobService.getJobByName(jobName);
            job.setEmail(email);
            job.setProteinFile(jobName + "_" + file.getOriginalFilename());
            job.setCoordinates(coordinatesJson);
            job.setStatus(JobStatus.PENDING);

            System.out.println("Got Job=" + job);
            jobService.saveJob(job);
            response.put("JobCreate", "Success");
            return ResponseEntity.ok().body(response);
        } else {
            return ResponseEntity.internalServerError().body(response);
        }
    }
    @PostMapping("/serverDockUploadLigand")
    public ResponseEntity<?> uploadLigandFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobName") String jobName
    ) {
        // Get the authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        System.out.println("email=" + email);
        System.out.println("jobName=" + jobName);

        Map<String, String> response = new HashMap<>();

        // Check file format
        String originalFileName = file.getOriginalFilename();
        System.out.println("ligand file original= " + originalFileName);
        if (originalFileName != null && !(originalFileName.endsWith(".sdf") || originalFileName.endsWith(".pdb")
                || originalFileName.endsWith(".mol") || originalFileName.endsWith(".mol2"))) {
            response.put("Error", "Invalid file format. Only .sdf, .pdb, .mol, .mol2 files are accepted.");
            return ResponseEntity.badRequest().body(response);
        }

        response.put("LigandUpload", serverDockService.uploadFile(file, email, jobName, "ligand"));
        if (response.get("LigandUpload").contains("Success")) {
            Job job = jobService.getJobByName(jobName);
            if (job == null) {
                job = new Job(); // Create a new job if it doesn't exist
                job.setJobName(jobName);
            }
            job.setEmail(email);
            job.setLigandFiles(jobName + "_" + file.getOriginalFilename());
            job.setStatus(JobStatus.PENDING);

            System.out.println("Got Job=" + job);
            jobService.saveJob(job);
            response.put("JobCreate", "Success");
            return ResponseEntity.ok().body(response);
        } else {
            return ResponseEntity.internalServerError().body(response);
        }
    }
    @GetMapping("/checkUpload")
    public ResponseEntity<?> checkFileIsUploaded(@RequestParam("jobName") String jobName) {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	String email = authentication.getName();
        return ResponseEntity.ok().body(jobService.checkFileIsUploaded(jobName, email));
    }
    @GetMapping("/getJobByName")
    public ResponseEntity<Job> getJobByName(@RequestParam("jobName") String jobName) {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	//String email = authentication.getName();
        return ResponseEntity.ok().body(jobService.getJobByName(jobName));
    }
    @PostMapping("/serverDock")
    public String processFile(@RequestParam("jobName") String jobName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        CompletableFuture.runAsync(() -> {
            try {
                // Give some time for the SSE connection to be established
               // Thread.sleep(1000);
                
                sseController.sendUpdateToClient("{\"jobName\":\"" + jobName + "\",\"message\":\"Docking process started\",\"status\":1}", jobName);
                
                // Simulate some steps in your docking process
                Thread.sleep(2000);
                sseController.sendUpdateToClient("{\"jobName\":\"" + jobName + "\",\"message\":\"Step 1 completed\",\"status\":2}", jobName);
                
                
                String dockingResult = dockService.startDocking(jobName, email);
                System.out.println("docking result=" + dockingResult);
                // Add more steps as needed
                
            } catch (InterruptedException e) {
            	sseController.sendUpdateToClient("{\"jobName\":\"" + jobName + "\",\"message\":\"Job failed \""+ e.getMessage()+"\"}", jobName);
            	
            }
        finally {
            // Close the SSE connection when the job is done (success or failure)
            sseController.closeConnection(jobName);
        }
        });
       // sseController.sendUpdateToClients("{\"jobName\":\"" + jobName + "\",\"message\":\"step done\",\"status\":1}", email);
       
        return "redirect:/?message=File processing started.";
    }
    @GetMapping("/getJobs")
    public ResponseEntity<List<Job>> getJobsByEmail() {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        List<Job> jobs = jobService.getByEmail(email);
        return ResponseEntity.ok(jobs);
    }
    @GetMapping("/download")//download the file from server
    public ResponseEntity<Resource> downloadFile(@RequestParam("jobName") String jobName,                                                 
                                                 @RequestParam("fileType") String fileType,
                                                 @RequestParam("fileName") String fileName) {
        try {
        	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
        	
            if(fileType.equals("protein-complex")) {
            	System.out.println("Got download complex file for protein====="+fileName);
            	
            	String filePathString = dockService.getProteinComplexPath(fileName,jobName,email);
            	return null;
            }
            // Use FileService to construct the file path
            String filePathString = fileService.getFilePathFromName(jobName, email, fileType, fileName);
            System.out.println("File Path String "+filePathString);
            Path filePath = Paths.get(filePathString).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
            	System.out.println("Resource is exist");
                String contentType = "application/octet-stream"; // Default content type

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
        	System.out.println("Error in downloading "+ex.getMessage());
            return ResponseEntity.badRequest().body(null);
        }

    }
    @GetMapping("/getProteinFile")
    public ResponseEntity<Resource> getProteinFile(@RequestParam("jobName") String jobName) {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        System.out.println("got jobName to get protein file " + jobName);
       // complexFile.generatingComplexFile(email, jobName);
        return serverDockService.getProteinFile(jobName, email);
    }
    @GetMapping("/getOutputTable")
    public ResponseEntity<String>getOutputTable(@RequestParam("jobName") String jobName){
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
    	complexFile.generatingComplexFile(email, jobName);
    	return serverDockService.getOutputTable(jobName,email);
    }



    
    
    
    
    
}

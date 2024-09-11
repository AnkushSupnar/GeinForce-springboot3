package com.geinforce.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.geinforce.model.Job;
import com.geinforce.model.JobStatus;
import com.geinforce.repository.JobRepository;
import com.geinforce.util.CommonUtility;

import jakarta.persistence.EntityNotFoundException;


import java.util.List;

@Service
public class JobService {
    @Autowired
    private  JobRepository jobRepository;

    @Autowired
    private CommonUtility commonUtility;


    public Job getJobByName(String jobName){
        Job job= jobRepository.findByJobName(jobName);
        if(job==null){
            job = new Job();
            job.setJobName(jobName);
        }
        return job;
    }
    public Job getByNameAndEmail(String jobName,String email){
       return jobRepository.findByJobNameAndEmail(jobName,email);
    }
    public Job saveJob(Job job){
        return jobRepository.save(job);
    }
    public String checkFileIsUploaded(String jobName,String email){

        System.out.println("jobName"+jobName);

       Job jobProtein = jobRepository.findByEmailAndProteinFileLike(email,jobName);
       Job jobLigand = jobRepository.findByEmailAndLigandFilesLike(email,jobName);
        System.out.println("protein"+jobProtein);
        System.out.println("ligand"+jobLigand);
       if(jobProtein==null){
           return "Failed:Please Upload Protein file";
       }
       else if(jobLigand==null){
           return "Failed:Please Upload Ligand file";
       }
       else if(!jobProtein.getStatus().equals(JobStatus.PENDING)){
           return "Failed: Already Running";
       }
       else{
           return "Success:Files Uploaded";
       }

    }
    public Job updateJobOutputPath(String jobName, String email) {
        Job job = jobRepository.findByJobNameAndEmail(jobName, email);
        if (job == null) {
            throw new EntityNotFoundException("Job not found");
        }

        job.setOutputFile(getFileNameWithoutExtension(job.getLigandFiles())+"_out.pdbqt");
        return jobRepository.save(job);
    }
    public Job updateJobStatus(String jobName, String email, JobStatus status) {
        Job job = jobRepository.findByJobNameAndEmail(jobName, email);
        if (job == null) {
            throw new EntityNotFoundException("Job not found");
        }
        job.setStatus(status);
        if(job.getSubmitDateTime()==null){
            job.setSubmitDateTime(commonUtility.getDateAndTime(job.getJobName()));
        }
        return jobRepository.save(job);
    }


    public List<Job> getByEmail(String email){
        return jobRepository.findByEmail(email);
    }
    public String getJobOutputFile(String jobName){
        return getJobByName(jobName).getOutputFile();
    }
    public String getFileNameWithoutExtension(String fileName){
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }
    public Job updateJobCoordinates(String jobName, String email, String coordinates) {
        Job job = jobRepository.findByJobNameAndEmail(jobName, email);
        if (job == null) {
            throw new EntityNotFoundException("Job not found");
        }
        job.setCoordinates(coordinates);
        if(job.getSubmitDateTime()==null){
            job.setSubmitDateTime(commonUtility.getDateAndTime(job.getJobName()));
        }
        return jobRepository.save(job);
    }

}

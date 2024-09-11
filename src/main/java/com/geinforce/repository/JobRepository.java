package com.geinforce.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.geinforce.model.Job;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    @Query("select j from Job j where j.jobName = ?1")
    Job findByJobName(String jobName);

    @Query("select j from Job j where j.jobName = ?1 and j.email = ?2")
    Job findByJobNameAndEmail(String jobName, String email);
    @Query("select j from Job j where j.email = ?1 and j.proteinFile like ?2%")
    Job findByEmailAndProteinFileLike(String email, String proteinFile);

    @Query("select j from Job j where j.email = ?1 and j.ligandFiles like ?2%")
    Job findByEmailAndLigandFilesLike(String email, String ligandFiles);

    @Query("select j from Job j where j.email = ?1")
    List<Job> findByEmail(String email);




}

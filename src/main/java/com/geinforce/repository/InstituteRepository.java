package com.geinforce.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.geinforce.model.Institute;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstituteRepository extends JpaRepository<Institute,Long> {
    @Query("select i from Institute i where i.instituteEmail = ?1 and i.otp = ?2")
    Optional<Institute> findByInstituteEmailAndOtp(String instituteEmail, String otp);

    @Query("select i.instituteName from Institute i where i.verified = ?1 order by i.instituteName")
    List<String> getVerifiedInstituteNames(Boolean verified);

    @Query("select i from Institute i where i.instituteId = ?1")
    Optional<Institute> findByInstituteId(Long instituteId);

    @Query("select i from Institute i where i.instituteName = ?1")
    Optional<Institute> findByInstituteName(String instituteName);

    @Query("select i from Institute i where i.instituteEmail = ?1")
    Optional<Institute> findByInstituteEmail(String instituteEmail);

}

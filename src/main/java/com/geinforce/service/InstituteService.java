package com.geinforce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.geinforce.model.Institute;
import com.geinforce.repository.InstituteRepository;
import com.geinforce.util.SendEmail;

import java.util.List;

@Service
public class InstituteService {
    @Autowired
    InstituteRepository repository;
    @Autowired
    SendEmail sendEmail;
    public String saveInstitute(Institute institute)
    {
        if(institute.getVerified()==null){
            institute.setVerified(false);
        }
        if(institute.getVerificationMethod()==null){
            institute.setVerificationMethod("email");
        }
        String otpResult =  sendEmail.sendOTPEmail(institute.getInstituteEmail(),institute.getInstituteName());
        if(otpResult.equalsIgnoreCase("Failed")){
            return "Fail:OTP Send Failed";
        }
        else {
            institute.setOtp(otpResult);
            repository.save(institute);
            return "success:Institute Save Success";
        }
    }
    public Institute updateInstitute(Institute institute){
        return repository.save(institute);
    }
    public Institute getInstituteByEmailAndOtp(String email,String otp){
        return repository.findByInstituteEmailAndOtp(email,otp).orElse(null);
    }
    public Institute verifyInstitute(String email, String otp){
        System.out.println("in service otp ="+otp);
        System.out.println("in service email="+email);
        Institute institute = getInstituteByEmailAndOtp(email,otp);
        if(institute !=null){
            institute.setVerified(Boolean.TRUE);
            return updateInstitute(institute);
        }
        else {
            System.out.println("failed:Not Found");
            return null;
        }
    }
    public List<String> getVerifiedInstituteNames(){
        return repository.getVerifiedInstituteNames(true);
    }
    public Institute findByInstitute_id(Long instituteId){
        return repository.findByInstituteId(instituteId).orElse(null);
    }
    public Institute findByInstitute_name(String name)
    {
        return repository.findByInstituteName(name).orElse(null);
    }
    public Boolean findByInstituteEmail(String email){
        return repository.findByInstituteEmail(email).orElse(null)!=null;
    }
    public Institute getByEmail(String email){
        return repository.findByInstituteEmail(email).orElse(null);
    }

}

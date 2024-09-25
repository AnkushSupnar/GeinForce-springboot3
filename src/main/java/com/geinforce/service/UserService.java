package com.geinforce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.geinforce.model.User;
import com.geinforce.repository.UserRepository;
import com.geinforce.util.SendEmail;

@Service
public class UserService {
    @Autowired
    UserRepository repository;
    @Autowired
    SendEmail sendEmail;
    
    public String saveUser(User user){
        if(user.getVerificationMethod()==null){
            user.setVerificationMethod("email");
        }
        if(user.getVerified()==null){
            user.setVerified(false);
        }
        String otpResult =  sendEmail.sendOTP(user.getEmail(),user.getFirstName()+" "+user.getLastName());
        if(otpResult.equalsIgnoreCase("Failed")){
            return "Fail:OTP Send Failed";
        }
        else {
            user.setOtp(otpResult);
            repository.save(user);
            return "success:User Save Success";
        }
    }
    public User updateUser(User user){
        return repository.save(user);
    }


    public boolean checkEmailIdExist(String  email){
        return repository.findByEmail(email) != null;
    }
    public User getByEmail(String email){
        return repository.findByEmail(email);
    }


    public User verifyUser(String email, String otp){
        System.out.println("in service otp ="+otp);
        System.out.println("in service email="+email);
        User user = getNoneRegisterUserByEmail(email,otp);
        if(user !=null){
        	
            user.setVerified(true);
            return updateUser(user);
        }
        else {
            System.out.println("failed:Not found");
            return null;
        }
    }
    public User getNoneRegisterUserByEmail(String email, String otp){
        return repository.findNonVerifiedUserByEmail(email,otp);
        //return null;
    }


}

package com.geinforce.dto;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.geinforce.model.Institute;
import com.geinforce.model.Student;
import com.geinforce.model.User;
import com.geinforce.service.InstituteService;

@Component
public class DTOUtil {
    @Autowired
    InstituteService instituteService;

    public User getUser(RegisterUserDTO dto){
        if(dto.getIs_institute()) return null;
        return User.builder()
                .email(dto.getEmail())
                .city(dto.getCity())
                .city(dto.getCity())
                .country(dto.getCountry())
                .countryCode(dto.getCountry_code())
                .state(dto.getState())
                .department(dto.getDepartment())
                .firstName(dto.getFirst_name())
                .mobile(dto.getMobile())
                .lastName(dto.getLast_name())
                .purpose(dto.getPurpose())
                .institute(dto.getInstitute())
                .isStudent(dto.getIs_student())
                .verificationMethod(dto.getVerification_method()).build();

    }
    public Institute getInstitute(RegisterUserDTO dto){
        System.out.println("dto found="+dto);
        if(!dto.getIs_institute()) return null;
        return Institute.builder()
                .instituteCity(dto.getCity())
                .instituteCode(dto.getCountry_code())
                .instituteEmail(dto.getEmail())
                .instituteCountry(dto.getCountry())
                .instituteMobile(dto.getMobile())
                .instituteName(dto.getInstitute_name())
                .instituteState(dto.getState())
                .otp(dto.getOtp())
                .instituteDepartment(dto.getInstitute_department())
                .build();
    }
    public Student getStudent(RegisterUserDTO dto){
        if(!dto.getIs_student())return null;
        return Student.builder()
                .email(dto.getEmail())
                .verified(dto.getVerified())
                .otp(dto.getOtp())
                .city(dto.getCity())
                .countryCode(dto.getCountry_code())
                .lastName(dto.getLast_name())
                .mobile(dto.getMobile())
                .state(dto.getState())
                .purpose(dto.getPurpose())
                .verificationMethod(dto.getVerification_method())
                .institute(instituteService.findByInstitute_name(dto.getSelect_institute()))
                .department(dto.getDepartment())
                .firstName(dto.getFirst_name())
                .country(dto.getCountry())
                .verified(false)
                .build();
    }
    public LoginUserDTO getLoginDTOFromUser(User user){
        return LoginUserDTO.builder()
                .user_id(user.getUserId())
                .entity_type("user")
                .name(user.getFirstName()+" "+user.getLastName())
                .build();
    }
}

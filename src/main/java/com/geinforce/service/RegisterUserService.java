package com.geinforce.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.geinforce.dto.DTOUtil;
import com.geinforce.dto.RegisterUserDTO;
import com.geinforce.model.Institute;
import com.geinforce.model.LoginUser;
import com.geinforce.model.Role;
import com.geinforce.model.Student;
import com.geinforce.model.User;
import com.geinforce.util.PasswordUtility;

@Service
public class RegisterUserService {

	@Autowired
	DTOUtil dtoUtil;
	@Autowired
	InstituteService instituteService;
	@Autowired
	StudentService studentService;
	@Autowired
	UserService userService;
	@Autowired
	PasswordUtility passwordUtility;
	@Autowired
	LoginUserService loginUserService;

	public String saveRegisterUser(RegisterUserDTO user)
	{
	    if (userService.checkEmailIdExist(user.getEmail()) || 
	        instituteService.findByInstituteEmail(user.getEmail()) || 
	        studentService.checkEmailExist(user.getEmail())) {
	        return "failed: An account with this email address is already registered with us";
	    }
	    else {

	    if (user.getIs_institute()) {
	        Institute institute = dtoUtil.getInstitute(user);
	        return institute != null ? instituteService.saveInstitute(institute) : "failed: Institute Not Created";
	    } 

	    if (user.getIs_student()) {
	        Student student = dtoUtil.getStudent(user);
	        return student != null ? studentService.saveStudent(student) : "failed: Student Not Created";
	    }
	   

	    User indUser = dtoUtil.getUser(user);
	    return indUser != null ? userService.saveUser(indUser) : "failed: User Not Created";
	    }
	   
	}

	public ResponseEntity<String> verifyUser(String email, String otp, String password) {
	    System.out.println("in rest otp=" + otp);
	    System.out.println("in rest email=" + email);

	    String result = "failed: Not Found";
	    Set<Role> roleSet = new HashSet<>();
	    String userType = "";
	    Long userTypeId = null;
	    String verifiedEmail = null;
	    String userOtp = null;

	    if (userService.checkEmailIdExist(email)) {
	        User user = userService.verifyUser(email, otp);
	        if (user == null) {
	            return ResponseEntity.status(HttpStatus.OK).body("failed: OTP Not Matched");
	        }
	        roleSet.add(new Role("USER"));
	        userType = "user";
	        userTypeId = user.getUserId();
	        verifiedEmail = user.getEmail();
	        userOtp = user.getOtp();
	    } else if (instituteService.findByInstituteEmail(email)) {
	        Institute institute = instituteService.verifyInstitute(email, otp);
	        if (institute == null) {
	            return ResponseEntity.status(HttpStatus.OK).body("failed: OTP Not Matched");
	        }
	        roleSet.add(new Role("INSTITUTE"));
	        userType = "institute";
	        userTypeId = institute.getInstituteId();
	        verifiedEmail = institute.getInstituteEmail();
	        userOtp = institute.getOtp();
	    } else if (studentService.checkEmailExist(email)) {
	        Student student = studentService.findByEmailAndOtp(email, otp);
	        if (student == null) {
	            return ResponseEntity.status(HttpStatus.OK).body("failed: OTP Not Matched");
	        }
	        roleSet.add(new Role("STUDENT"));
	        userType = "student";
	        userTypeId = student.getStudentId();
	        verifiedEmail = student.getEmail();
	        userOtp = student.getOtp();
	    } else {
	        return ResponseEntity.status(HttpStatus.OK).body("failed: OTP Not Matched");
	    }

	    LoginUser loginUser = LoginUser.builder()
	            .userType(userType)
	            .userTypeId(userTypeId)
	            .otp(userOtp)
	            .email(verifiedEmail)
	            .roles(roleSet)
	            .isActive(false)
	            .password(passwordUtility.encodePassword(password))
	            .build();

	    result = loginUserService.saveLoginUser(loginUser);

	    return ResponseEntity.status(HttpStatus.OK).body(result);
	}
}

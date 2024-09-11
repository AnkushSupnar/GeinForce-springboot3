package com.geinforce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class RegisterUserDTO {

	 private Long id;
	    private String first_name;
	    private String institute_name;
	    private String institute_department;
	    private String last_name ;
	    private String email;
	    private String country_code;
	    private String mobile;
	    private String institute;
	    private String purpose;
	    private String department;
	    private String country;
	    private String state;
	    private String city;
	    private String otp;
	    private Boolean verified;
	    private String verification_method;
	    private Boolean is_institute;
	    private Boolean is_student;
	    private String select_institute;

}

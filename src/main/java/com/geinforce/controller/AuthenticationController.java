package com.geinforce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.geinforce.dto.RegisterUserDTO;
import com.geinforce.model.AuthenticationRequest;
import com.geinforce.model.AuthenticationResponse;
import com.geinforce.model.LoginUser;
import com.geinforce.service.CustomUserDetailsService;
import com.geinforce.service.LoginUserService;
import com.geinforce.service.RegisterUserService;
import com.geinforce.util.JwtUtil;

@RestController

@RequestMapping("/api")
public class AuthenticationController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	LoginUserService loginUserService;

	@Autowired
	RegisterUserService registerUserService;

	@PostMapping("/register")
	public String register(@RequestBody RegisterUserDTO user) {

		System.out.println("Register user DTO="+user);
		return registerUserService.saveRegisterUser(user);
	}

	@PostMapping("/verifyUser")
	public ResponseEntity<String> verifyUser(@RequestParam String email, @RequestParam String otp,
			@RequestParam String password) {

		return registerUserService.verifyUser(email, otp, password);
	}

	@PostMapping("/login")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
	    try {
	        authenticationManager.authenticate(
	            new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword())
	        );

	        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());
	        System.out.println("Is User Enable="+userDetails.isEnabled());
	        LoginUser loginUser = loginUserService.getByEmail(userDetails.getUsername());
	        System.out.println("LoginUser is Active "+loginUser.getIsActive());
	        if(loginUser.getIsActive()) {
	        final String jwt = jwtUtil.generateToken(userDetails.getUsername());

	        return ResponseEntity.ok(new AuthenticationResponse(jwt));
	        }
	        else {
	        	return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Activated");
	        }

	    } catch (BadCredentialsException e) {
	    	System.out.println("Error in Login "+e.getMessage());
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect Credentials");
	    } catch (UsernameNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
	    }
	}
	
	@PostMapping("/validateUser")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authHeader) {
		System.out.println("Validating User token------------------------------------");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid Authorization header");
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        if (jwtUtil.validateToken(token)) {
            return ResponseEntity.ok("Token is valid");
        } else {
            return ResponseEntity.ok("Token is invalid or has expired");
        }
    }
}
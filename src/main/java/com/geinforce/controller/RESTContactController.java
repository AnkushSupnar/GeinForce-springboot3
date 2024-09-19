package com.geinforce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.geinforce.model.Contact;
import com.geinforce.service.ContactService;
import com.geinforce.util.SendEmail;


@RestController
@RequestMapping("/api/contacts")
public class RESTContactController {
	@Autowired
    SendEmail sendEmail;

	@Autowired 
	private ContactService contactService;
	 
	 @PostMapping
	    public ResponseEntity<Contact> submitContact(@RequestBody Contact contact) {
	        Contact savedContact = contactService.saveContact(contact);
	      // String result =  sendEmail.sendForgotPasswordEmail("Ankush", "ankush.supnar@gmail.com");
	        String result = sendEmail.sendContactFormConfirmationEmail(savedContact);
	       System.out.println("Email send result "+result);
	      String support = sendEmail.sendSupportTeamNotification(savedContact);
	      System.out.println("Email send support "+support);
	        return new ResponseEntity<>(savedContact, HttpStatus.CREATED);
	    }
}

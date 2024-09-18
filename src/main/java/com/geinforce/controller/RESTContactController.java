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

@RestController
@RequestMapping("/api/contacts")
public class RESTContactController {

	@Autowired 
	private ContactService contactService;
	 
	 @PostMapping
	    public ResponseEntity<Contact> submitContact(@RequestBody Contact contact) {
	        Contact savedContact = contactService.saveContact(contact);
	        return new ResponseEntity<>(savedContact, HttpStatus.CREATED);
	    }
}

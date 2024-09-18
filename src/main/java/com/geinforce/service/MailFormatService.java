package com.geinforce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.geinforce.model.MailFormats;
import com.geinforce.repository.MailFormatRepository;

@Service
public class MailFormatService {

	@Autowired
	private MailFormatRepository mailFormatRepository;

	public MailFormats saveContact(MailFormats format) {
		return mailFormatRepository.save(format);
	}
	public MailFormats getBymailFormatName(String format){
		return mailFormatRepository.findByMailFormatName(format);
	}
}

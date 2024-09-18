package com.geinforce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.geinforce.model.MailFormats;

@Repository
public interface MailFormatRepository  extends JpaRepository<MailFormats,Integer> {

	MailFormats findByMailFormatName(String mailFormatName);

	
}

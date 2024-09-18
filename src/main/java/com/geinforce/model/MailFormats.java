package com.geinforce.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mailFormats")
public class MailFormats {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;
	String mailFormatName;
	String mailFormat;
	public MailFormats() {
		super();
		// TODO Auto-generated constructor stub
	}
	public MailFormats(String mailFormatName, String mailFormat) {
		super();		
		this.mailFormatName = mailFormatName;
		this.mailFormat = mailFormat;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMailFormatName() {
		return mailFormatName;
	}
	public void setMailFormatName(String mailFormatName) {
		this.mailFormatName = mailFormatName;
	}
	public String getMailFormat() {
		return mailFormat;
	}
	public void setMailFormat(String mailFormat) {
		this.mailFormat = mailFormat;
	}
	@Override
	public String toString() {
		return "MailFormats [id=" + id + ", mailFormatName=" + mailFormatName + ", mailFormat=" + mailFormat + "]";
	}
	
}

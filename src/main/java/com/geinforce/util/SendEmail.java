package com.geinforce.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.geinforce.model.Contact;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;

@Component
@PropertySource("classpath:email.properties")
public class SendEmail {
	@Autowired
	PasswordUtility passwordUtility;
	@Value("${email.username}")
	String userName;
	@Value("${email.password}")
	String password;
	@Value("${support.team.email}")
	private String supportTeamEmail;

	public String sendOTPEmail1(String receiver, String name) {
		String otp = passwordUtility.generateOTP();
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.hostinger.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, password);
			}
		});
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(userName));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
			message.setSubject("One Time Password");
			message.setText("Dear " + name + "," + "\n\n Your One-Time Password (OTP) for login is:" + otp
					+ " Please enter this OTP on the registration screen to proceed."
					+ " Note: This OTP is valid for only 5 minutes.");

			Transport.send(message);

			System.out.println("Email Sent");
			return otp;
		} catch (MessagingException e) {
			System.out.println("Error in email send" + e.getMessage());
			// e.printStackTrace();
			return "Failed";
			// throw new RuntimeException(e);
		}
	}

	public Message getMessage() {
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.hostinger.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, password);
			}
		});
		try {
			Message message = new MimeMessage(session);

			message.setFrom(new InternetAddress(userName));
			return message;
		} catch (MessagingException e) {
			System.out.println("Error in getting message obj " + e.getMessage());
			return null;
		}

	}

	public String sendForgotPasswordEmail(String receiverName, String receiverEmail) {
		try {
			String otp = passwordUtility.generateOTP();
			Message message = getMessage();

			if (message == null) {
				System.out.println("Failed to create message object");
				return "Failed to create message";
			}

			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiverEmail));
			message.setSubject("Geinforce - OTP Verification");

			String htmlBody = """
					<html>
					<head>
					    <meta charset="UTF-8">
					    <style>
					        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
					        .container { width: 100%%; max-width: 600px; margin: 0 auto; padding: 20px; }
					        .header { background-color: #f4f4f4; padding: 10px; text-align: center; }
					        .content { padding: 20px 0; }
					        .otp { font-size: 24px; font-weight: bold; color: #007bff; }
					        .footer { background-color: #f4f4f4; padding: 10px; text-align: center; font-size: 12px; }
					    </style>
					</head>
					<body>
					    <div class="container">
					        <div class="header">
					            <h2>Geinforce- Forgot Password</h2>
					        </div>
					        <div class="content">
					            <p>Dear %s,</p>
					            <p>Thank you for registering with Geinforce. To ensure the security of your account, we require a one-time password (OTP) verification.</p>
					            <p>Please find your OTP below:</p>
					            <p class="otp">OTP: %s</p>
					            <p>To complete the registration process, please visit our website <a href="https://www.geinforce.com/">https://www.geinforce.com/</a> and enter the OTP provided above in the designated field. This OTP is valid for 10 minutes only. If you do not enter the OTP within the specified timeframe, you will need to request a new OTP.</p>
					            <p>If you did not initiate this registration request, please disregard this email. Your account will not be activated unless the OTP is entered on the registration page.</p>
					            <p>Should you require any assistance or have any questions, please feel free to reach out to our customer support team at <a href="mailto:contact@geinforce.com">contact@geinforce.com</a>. We are here to help.</p>
					            <p>Thank you for choosing our product. We look forward to supporting your research efforts and contributing to your scientific breakthroughs.</p>
					            <p>Sincerely,<br>Geinforce Team</p>
					        </div>
					        <div class="footer">
					            <p>Geinforce Technology Private Limited<br>
					            Website: <a href="https://www.geinforce.com/">https://www.geinforce.com/</a><br>
					            Email: <a href="mailto:contact@geinforce.com">contact@geinforce.com</a></p>
					        </div>
					    </div>
					</body>
					</html>
					"""
					.formatted(receiverName, otp);

			// Create a MimeBodyPart for the HTML content
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(htmlBody, "text/html; charset=utf-8");

			// Create a Multipart and add the MimeBodyPart to it
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimeBodyPart);

			// Set the Multipart as the message's content
			message.setContent(multipart);

			// Send the message
			Transport.send(message);

			System.out.println("Forgot Password Email Sent Successfully");
			return "Success";
		} catch (MessagingException e) {
			System.out.println("MessagingException in sendForgotPasswordEmail: " + e.getMessage());
			e.printStackTrace();
			return "Failed: Messaging Error";
		} catch (Exception e) {
			System.out.println("Unexpected error in sendForgotPasswordEmail: " + e.getMessage());
			e.printStackTrace();
			return "Failed: Unexpected Error";
		}
	}

	public String sendDockingCompletedEmail(String receiverName, String receiverEmail, String resultLink,
			String attachmentPath) {
		try {
			Message message = getMessage();

			if (message == null) {
				System.out.println("Failed to create message object");
				return "Failed to create message";
			}

			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiverEmail));
			message.setSubject("GeinDock Suite - Docking Completed");

			// Calculate expiration time (12 hours from now)
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date expirationTime = new Date(System.currentTimeMillis() + 12 * 60 * 60 * 1000);

			String htmlBody = """
					<html>
					<head>
					    <meta charset="UTF-8">
					    <style>
					        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
					        .container { width: 100%%; max-width: 600px; margin: 0 auto; padding: 20px; }
					        .header { background-color: #f4f4f4; padding: 10px; text-align: center; }
					        .content { padding: 20px 0; }
					        .footer { background-color: #f4f4f4; padding: 10px; text-align: center; font-size: 12px; }
					    </style>
					</head>
					<body>
					    <div class="container">
					        <div class="header">
					            <h2>GeinDock Suite - Docking Completed</h2>
					        </div>
					        <div class="content">
					            <p>Dear <strong>%s</strong>,</p>
					            <p>Your molecular docking process with GeinDock Suite has been successfully completed. We're pleased to let you know that the analysis you initiated is now finished, and the results are ready for your review.</p>
					            <p>The results of the docking are available at the link below:</p>
					            <p><a href="%s">%s</a></p>
					            <p>Please note, the link will remain active until %s, after which it will expire.</p>
					            <p>The docking files are attached to this email.</p>
					            <p>Thank you for choosing GeinDock Suite for your molecular docking analysis. We are here to support your research efforts and are eager to contribute to your future scientific breakthroughs.</p>
					            <p><strong>Regards,</strong><br>
					            Geinforce Team</p>
					        </div>
					        <div class="footer">
					            <p>Geinforce Technology Private Limited<br>
					            Website: <a href="https://www.geinforce.com/">https://www.geinforce.com/</a><br>
					            Email: <a href="mailto:contact@geinforce.com">contact@geinforce.com</a> | <a href="mailto:support@geinforce.com">support@geinforce.com</a></p>
					        </div>
					    </div>
					</body>
					</html>
					"""
					.formatted(receiverName, resultLink, resultLink, dateFormat.format(expirationTime));

			// Create a multipart message
			Multipart multipart = new MimeMultipart();

			// Create the HTML part
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(htmlBody, "text/html; charset=utf-8");
			multipart.addBodyPart(messageBodyPart);

			// Create the attachment part
			messageBodyPart = new MimeBodyPart();
			javax.activation.DataSource source = new javax.activation.FileDataSource(attachmentPath);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName("DockingResults.zip");
			multipart.addBodyPart(messageBodyPart);

			// Set the complete message parts
			message.setContent(multipart);

			// Send the message
			Transport.send(message);

			System.out.println("Docking Completed Email Sent Successfully");
			return "Success";
		} catch (MessagingException e) {
			System.out.println("MessagingException in sendDockingCompletedEmail: " + e.getMessage());
			e.printStackTrace();
			return "Failed: Messaging Error";
		} catch (Exception e) {
			System.out.println("Unexpected error in sendDockingCompletedEmail: " + e.getMessage());
			e.printStackTrace();
			return "Failed: Unexpected Error";
		}
	}

	public String sendInstituteRegistrationEmail(String recipientName, String recipientEmail, String blogUrl) {
		try {
			Message message = getMessage();

			if (message == null) {
				System.out.println("Failed to create message object");
				return "Failed to create message";
			}

			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
			message.setSubject("Institute Registration Successful - Welcome to Geinforce!");

			String htmlBody = """
					<html>
					<head>
					    <meta charset="UTF-8">
					    <style>
					        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
					        .container { width: 100%%; max-width: 600px; margin: 0 auto; padding: 20px; }
					        .header { background-color: #f4f4f4; padding: 10px; text-align: center; }
					        .content { padding: 20px 0; }
					        .footer { background-color: #f4f4f4; padding: 10px; text-align: center; font-size: 12px; }
					    </style>
					</head>
					<body>
					    <div class="container">
					        <div class="header">
					            <h2>Welcome to Geinforce!</h2>
					        </div>
					        <div class="content">
					            <p>Dear <strong>%s</strong>,</p>
					            <p>We are delighted to inform you that your institute's registration with Geinforce has been successfully processed. On behalf of our team, we extend a warm welcome to your institute and look forward to supporting your scientific research efforts.</p>
					            <p>As a registered institute, you now have access to our advanced portal, where you can manage your students and faculty members, as well as grant access to various tools. This portal empowers you to efficiently oversee and facilitate the research activities within your institute.</p>
					            <p>To access your institute's portal, please visit our website <a href="https://www.geinforce.com/">https://www.geinforce.com/</a> and log in using the credentials provided during the registration process. Once logged in, you will find a user-friendly interface that allows you to add students and faculty members, assign tool access, and monitor their progress.</p>
					            <p>Please do not hesitate to reach out to our dedicated customer support team at <a href="mailto:contact@geinforce.com">contact@geinforce.com</a>. We are here to assist you every step of the way.</p>
					            <p>We invite you to explore the capabilities and features of our tools to advance your research goals. Stay updated with the latest developments, tutorials, and tips by following our blog <a href="%s">%s</a>.</p>
					            <p><strong>Sincerely,</strong><br>
					            Geinforce Team</p>
					        </div>
					        <div class="footer">
					            <p>Geinforce Technology Private Limited<br>
					            Website: <a href="https://www.geinforce.com/">https://www.geinforce.com/</a><br>
					            Email: <a href="mailto:contact@geinforce.com">contact@geinforce.com</a></p>
					        </div>
					    </div>
					</body>
					</html>
					"""
					.formatted(recipientName, blogUrl, blogUrl);

			// Create a MimeBodyPart for the HTML content
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(htmlBody, "text/html; charset=utf-8");

			// Create a Multipart and add the MimeBodyPart to it
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimeBodyPart);

			// Set the Multipart as the message's content
			message.setContent(multipart);

			// Send the message
			Transport.send(message);

			System.out.println("Institute Registration Email Sent Successfully");
			return "Success";
		} catch (MessagingException e) {
			System.out.println("MessagingException in sendInstituteRegistrationEmail: " + e.getMessage());
			e.printStackTrace();
			return "Failed: Messaging Error";
		} catch (Exception e) {
			System.out.println("Unexpected error in sendInstituteRegistrationEmail: " + e.getMessage());
			e.printStackTrace();
			return "Failed: Unexpected Error";
		}
	}

	public String sendOTP( String recipientEmail,String recipientName) {
		try {
			String otp = passwordUtility.generateOTP();
			Message message = getMessage();

			if (message == null) {
				System.out.println("Failed to create message object");
				return "Failed to create message";
			}

			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
			message.setSubject("Geinforce - OTP Verification");

			String htmlBody = """
					<html>
					<head>
					    <meta charset="UTF-8">
					    <style>
					        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
					        .container { width: 100%%; max-width: 600px; margin: 0 auto; padding: 20px; }
					        .header { background-color: #f4f4f4; padding: 10px; text-align: center; }
					        .content { padding: 20px 0; }
					        .otp { font-size: 24px; font-weight: bold; color: #007bff; }
					        .footer { background-color: #f4f4f4; padding: 10px; text-align: center; font-size: 12px; }
					    </style>
					</head>
					<body>
					    <div class="container">
					        <div class="header">
					            <h2>Geinforce - OTP Verification</h2>
					        </div>
					        <div class="content">
					            <p>Dear <strong>%s</strong>,</p>
					            <p>Thank you for registering with Geinforce. To ensure the security of your account, we require a one-time password (OTP) verification.</p>
					            <p>Please find your OTP below:</p>
					            <p class="otp">OTP: %s</p>
					            <p>To complete the registration process, please visit our website <a href="http://www.geinforce.com">www.geinforce.com</a> and enter the OTP provided above in the designated field. This OTP is valid for <strong>10 minutes</strong> only. If you do not enter the OTP within the specified timeframe, you will need to request a new OTP.</p>
					            <p>If you did not initiate this registration request, please disregard this email. Your account will not be activated unless the OTP is entered on the registration page.</p>
					            <p>Should you require any assistance or have any questions, please feel free to reach out to our customer support team at <a href="mailto:contact@geinforce.com">contact@geinforce.com</a>. We are here to help.</p>
					            <p>Thank you for choosing our Product. We look forward to supporting your research efforts and contributing to your scientific breakthroughs.</p>
					            <p><strong>Best regards,</strong><br>
					            Geinforce Technology Private Limited</p>
					        </div>
					        <div class="footer">
					            <p>Website: <a href="http://www.geinforce.com">www.geinforce.com</a><br>
					            Email: <a href="mailto:contact@geinforce.com">contact@geinforce.com</a></p>
					        </div>
					    </div>
					</body>
					</html>
					"""
					.formatted(recipientName, otp);

			// Create a MimeBodyPart for the HTML content
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(htmlBody, "text/html; charset=utf-8");

			// Create a Multipart and add the MimeBodyPart to it
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimeBodyPart);

			// Set the Multipart as the message's content
			message.setContent(multipart);

			// Send the message
			Transport.send(message);

			System.out.println("OTP Verification Email Sent Successfully");
			return "Success";
		} catch (MessagingException e) {
			System.out.println("MessagingException in sendOTPVerificationEmail: " + e.getMessage());
			e.printStackTrace();
			return "Failed: Messaging Error";
		} catch (Exception e) {
			System.out.println("Unexpected error in sendOTPVerificationEmail: " + e.getMessage());
			e.printStackTrace();
			return "Failed: Unexpected Error";
		}
	}

	public String sendAccountSubmissionConfirmationEmail(String recipientName, String recipientEmail) {
		try {
			Message message = getMessage();

			if (message == null) {
				System.out.println("Failed to create message object");
				return "Failed to create message";
			}

			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
			message.setSubject("Account Submission Confirmation");

			String htmlBody = """
					<html>
					<head>
					    <meta charset="UTF-8">
					    <style>
					        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
					        .container { width: 100%%; max-width: 600px; margin: 0 auto; padding: 20px; }
					        .header { background-color: #f4f4f4; padding: 10px; text-align: center; }
					        .content { padding: 20px 0; }
					        .footer { background-color: #f4f4f4; padding: 10px; text-align: center; font-size: 12px; }
					    </style>
					</head>
					<body>
					    <div class="container">
					        <div class="header">
					            <h2>Account Submission Confirmation</h2>
					        </div>
					        <div class="content">
					            <p>Dear <strong>%s</strong>,</p>
					            <p>Thank you for submitting your account details. We are pleased to inform you that your information has been successfully received.</p>
					            <p>Your account is currently under review by our Admin Center. Once the review process is complete and your account is approved, you will receive a notification and be able to log in.</p>
					            <p>We appreciate your patience during this process. If you have any questions or require further assistance, please do not hesitate to contact us.</p>
					            <p><strong>Sincerely,</strong><br>
					            Geinforce Team</p>
					        </div>
					        <div class="footer">
					            <p>Geinforce Technology Private Limited<br>
					            Website: <a href="https://www.geinforce.com/">https://www.geinforce.com/</a><br>
					            Email: <a href="mailto:contact@geinforce.com">contact@geinforce.com</a> | <a href="mailto:support@geinforce.com">support@geinforce.com</a></p>
					        </div>
					    </div>
					</body>
					</html>
					"""
					.formatted(recipientName);

			// Create a MimeBodyPart for the HTML content
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(htmlBody, "text/html; charset=utf-8");

			// Create a Multipart and add the MimeBodyPart to it
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimeBodyPart);

			// Set the Multipart as the message's content
			message.setContent(multipart);

			// Send the message
			Transport.send(message);

			System.out.println("Account Submission Confirmation Email Sent Successfully");
			return "Success";
		} catch (MessagingException e) {
			System.out.println("MessagingException in sendAccountSubmissionConfirmationEmail: " + e.getMessage());
			e.printStackTrace();
			return "Failed: Messaging Error";
		} catch (Exception e) {
			System.out.println("Unexpected error in sendAccountSubmissionConfirmationEmail: " + e.getMessage());
			e.printStackTrace();
			return "Failed: Unexpected Error";
		}
	}

	public String sendRegistrationConfirmationEmail(String recipientName, String recipientEmail, String username,
			String password) {
		try {
			Message message = getMessage();

			if (message == null) {
				System.out.println("Failed to create message object");
				return "Failed to create message";
			}

			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
			message.setSubject("Registration Successful - Welcome to Geinforce!");

			String htmlBody = """
					<html>
					<head>
					    <meta charset="UTF-8">
					    <style>
					        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
					        .container { width: 100%%; max-width: 600px; margin: 0 auto; padding: 20px; }
					        .header { background-color: #f4f4f4; padding: 10px; text-align: center; }
					        .content { padding: 20px 0; }
					        .credentials { background-color: #e9e9e9; padding: 10px; margin: 10px 0; }
					        .footer { background-color: #f4f4f4; padding: 10px; text-align: center; font-size: 12px; }
					    </style>
					</head>
					<body>
					    <div class="container">
					        <div class="header">
					            <h2>Welcome to Geinforce!</h2>
					        </div>
					        <div class="content">
					            <p>Dear <strong>%s</strong>,</p>
					            <p>Congratulations! Your registration with Geinforce has been successfully processed. We are excited to welcome you to our community of researchers and provide you with a powerful tool to advance the Drug Discovery process.</p>
					            <p>As a registered user, you now have access to all tools and software offered by Geinforce. To get started, please visit our website <a href="https://www.geinforce.com/">https://www.geinforce.com/</a> and log in using the credentials you provided during the registration process.</p>
					            <div class="credentials">
					                <p>Here are your login details:</p>
					                <p><strong>Username:</strong> %s<br>
					                <strong>Password:</strong> %s</p>
					            </div>
					            <p>We recommend changing your password after the initial login to ensure the security of your account. Additionally, please keep your login credentials confidential and do not share them with others.</p>
					            <p>Geinforce offers an intuitive user interface and comprehensive documentation to assist you to use all tools and software offered by Geinforce. If you need any guidance or have any questions, our support team is always ready to assist you. Simply reach out to us at <a href="mailto:contact@geinforce.com">contact@geinforce.com</a>, and we will be happy to help.</p>
					            <p><strong>Sincerely,</strong><br>
					            Geinforce Team</p>
					        </div>
					        <div class="footer">
					            <p>Geinforce Technology Private Limited<br>
					            Website: <a href="https://www.geinforce.com/">https://www.geinforce.com/</a><br>
					            Email: <a href="mailto:contact@geinforce.com">contact@geinforce.com</a> | <a href="mailto:support@geinforce.com">support@geinforce.com</a></p>
					        </div>
					    </div>
					</body>
					</html>
					"""
					.formatted(recipientName, username, password);

			// Create a MimeBodyPart for the HTML content
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(htmlBody, "text/html; charset=utf-8");

			// Create a Multipart and add the MimeBodyPart to it
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimeBodyPart);

			// Set the Multipart as the message's content
			message.setContent(multipart);

			// Send the message
			Transport.send(message);

			System.out.println("Registration Confirmation Email Sent Successfully");
			return "Success";
		} catch (MessagingException e) {
			System.out.println("MessagingException in sendRegistrationConfirmationEmail: " + e.getMessage());
			e.printStackTrace();
			return "Failed: Messaging Error";
		} catch (Exception e) {
			System.out.println("Unexpected error in sendRegistrationConfirmationEmail: " + e.getMessage());
			e.printStackTrace();
			return "Failed: Unexpected Error";
		}
	}

	public String sendContactFormConfirmationEmail(Contact contact) {
		try {
			Message message = getMessage();

			if (message == null) {
				System.out.println("Failed to create message object");
				return "Failed to create message";
			}

			//message.setFrom(new InternetAddress(supportTeamEmail));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(contact.getEmail()));
			message.setSubject("Thank You for Contacting Geinforce");

			// Set submission time if it's null
			if (contact.getSubmissionTime() == null) {
				contact.setSubmissionTime(LocalDateTime.now());
			}

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			String formattedDateTime = contact.getSubmissionTime().format(formatter);

			String htmlBody = """
					<html>
					<head>
					    <meta charset="UTF-8">
					    <style>
					        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
					        .container { width: 100%%; max-width: 600px; margin: 0 auto; padding: 20px; }
					        .header { background-color: #f4f4f4; padding: 10px; text-align: center; }
					        .content { padding: 20px 0; }
					        .query-details { background-color: #e9e9e9; padding: 10px; margin: 10px 0; }
					        .footer { background-color: #f4f4f4; padding: 10px; text-align: center; font-size: 12px; }
					    </style>
					</head>
					<body>
					    <div class="container">
					        <div class="header">
					            <h2>Thank You for Contacting Geinforce</h2>
					        </div>
					        <div class="content">
					            <p>Dear <strong>%s %s</strong>,</p>
					            <p>Thank you for reaching out to Geinforce. We have received your query and appreciate you taking the time to contact us. Our team is committed to providing you with the best possible assistance.</p>
					            <p>We have recorded your query details as follows:</p>
					            <div class="query-details">
					                <p><strong>Name:</strong> %s %s<br>
					                <strong>Email:</strong> %s<br>
					                <strong>Organization:</strong> %s<br>
					                <strong>Department:</strong> %s<br>
					                <strong>Message:</strong> %s<br>
					                <strong>Submission Time:</strong> %s</p>
					            </div>
					            <p>We want to assure you that our team is reviewing your inquiry and will respond as soon as possible. We typically aim to address all queries within 24-48 hours, but please allow up to 3 business days for a comprehensive response, especially for more complex inquiries.</p>
					            <p>If you have any additional information or questions in the meantime, please don't hesitate to reply to this email. We're here to help and look forward to assisting you.</p>
					            <p>Thank you once again for your interest in Geinforce. We value your feedback and questions as they help us improve our services and better meet the needs of our users.</p>
					            <p><strong>Best regards,</strong><br>
					            Geinforce Support Team</p>
					        </div>
					        <div class="footer">
					            <p>Geinforce Technology Private Limited<br>
					            Website: <a href="https://www.geinforce.com/">https://www.geinforce.com/</a><br>
					            Email: <a href="mailto:contact@geinforce.com">contact@geinforce.com</a> | <a href="mailto:support@geinforce.com">support@geinforce.com</a></p>
					        </div>
					    </div>
					</body>
					</html>
					"""
					.formatted(contact.getFirstName(), contact.getLastName(), contact.getFirstName(),
							contact.getLastName(), contact.getEmail(), contact.getOrganization(),
							contact.getDepartment(), contact.getMessage(), formattedDateTime);

			// Create a MimeBodyPart for the HTML content
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(htmlBody, "text/html; charset=utf-8");

			// Create a Multipart and add the MimeBodyPart to it
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimeBodyPart);

			// Set the Multipart as the message's content
			message.setContent(multipart);

			// Send the message
			Transport.send(message);

			System.out.println("Contact Form Confirmation Email Sent Successfully");
			return "Success";
		} catch (MessagingException e) {
			System.out.println("MessagingException in sendContactFormConfirmationEmail: " + e.getMessage());
			e.printStackTrace();
			return "Failed: Messaging Error";
		} catch (Exception e) {
			System.out.println("Unexpected error in sendContactFormConfirmationEmail: " + e.getMessage());
			e.printStackTrace();
			return "Failed: Unexpected Error";
		}
	}

	 public String sendSupportTeamNotification(Contact contact) {
	        try {
	            Message message = getMessage();

	            if (message == null) {
	                System.out.println("Failed to create message object");
	                return "Failed to create message";
	            }
	           
	            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(supportTeamEmail));
	            message.setSubject("New Contact Form Submission - Action Required");

	            // Set submission time if it's null
	            if (contact.getSubmissionTime() == null) {
	                contact.setSubmissionTime(LocalDateTime.now());
	            }

	            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	            String formattedDateTime = contact.getSubmissionTime().format(formatter);

	            String htmlBody = """
	                <html>
	                <head>
	                    <meta charset="UTF-8">
	                    <style>
	                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
	                        .container { width: 100%%; max-width: 600px; margin: 0 auto; padding: 20px; }
	                        .header { background-color: #f4f4f4; padding: 10px; text-align: center; }
	                        .content { padding: 20px 0; }
	                        .contact-details { background-color: #e9e9e9; padding: 10px; margin: 10px 0; }
	                        .footer { background-color: #f4f4f4; padding: 10px; text-align: center; font-size: 12px; }
	                    </style>
	                </head>
	                <body>
	                    <div class="container">
	                        <div class="header">
	                            <h2>New Contact Form Submission</h2>
	                        </div>
	                        <div class="content">
	                            <p>A new contact form has been submitted on the Geinforce website. Please review the details below and take appropriate action.</p>
	                            <div class="contact-details">
	                                <h3>Contact Details:</h3>
	                                <p><strong>Name:</strong> %s %s</p>
	                                <p><strong>Email:</strong> %s</p>
	                                <p><strong>Organization:</strong> %s</p>
	                                <p><strong>Department:</strong> %s</p>
	                                <p><strong>Submission Time:</strong> %s</p>
	                                <h3>Message:</h3>
	                                <p>%s</p>
	                            </div>
	                            <p>Please respond to this inquiry within our standard 24-48 hour timeframe. If the query is complex and requires more time, please send an initial acknowledgment to the user.</p>
	                            <p>Remember to update the CRM with the details of this interaction and any follow-up actions taken.</p>
	                        </div>
	                        <div class="footer">
	                            <p>This is an automated message from the Geinforce Contact Form System. Please do not reply directly to this email.</p>
	                        </div>
	                    </div>
	                </body>
	                </html>
	                """.formatted(
	                    contact.getFirstName(),
	                    contact.getLastName(),
	                    contact.getEmail(),
	                    contact.getOrganization(),
	                    contact.getDepartment(),
	                    formattedDateTime,
	                    contact.getMessage()
	                );

	            // Create a MimeBodyPart for the HTML content
	            MimeBodyPart mimeBodyPart = new MimeBodyPart();
	            mimeBodyPart.setContent(htmlBody, "text/html; charset=utf-8");

	            // Create a Multipart and add the MimeBodyPart to it
	            Multipart multipart = new MimeMultipart();
	            multipart.addBodyPart(mimeBodyPart);

	            // Set the Multipart as the message's content
	            message.setContent(multipart);

	            // Send the message
	            Transport.send(message);

	            System.out.println("Support Team Notification Email Sent Successfully");
	            return "Success";
	        } catch (MessagingException e) {
	            System.out.println("MessagingException in sendSupportTeamNotification: " + e.getMessage());
	            e.printStackTrace();
	            return "Failed: Messaging Error";
	        } catch (Exception e) {
	            System.out.println("Unexpected error in sendSupportTeamNotification: " + e.getMessage());
	            e.printStackTrace();
	            return "Failed: Unexpected Error";
	        }
	    }

}
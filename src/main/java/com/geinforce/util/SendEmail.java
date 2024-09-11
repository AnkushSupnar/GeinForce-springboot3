package com.geinforce.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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
    public String sendEmail(String receiver,String name){
        String otp = passwordUtility.generateOTP();
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.hostinger.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(userName, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(userName));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(receiver));
            message.setSubject("One Time Password");
            message.setText("Dear "+ name+","
                    + "\n\n Your One-Time Password (OTP) for login is:"+otp
                    +" Please enter this OTP on the registration screen to proceed."
            +" Note: This OTP is valid for only 5 minutes.");

            Transport.send(message);

            System.out.println("Email Sent");
            return otp;
        } catch (MessagingException e) {
            System.out.println("Error in email send"+e.getMessage());
          //  e.printStackTrace();
            return "Failed";
           // throw new RuntimeException(e);

        }

    }

}

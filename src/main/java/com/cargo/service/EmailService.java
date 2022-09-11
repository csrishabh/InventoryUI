package com.cargo.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.cargo.document.Product;
import com.cargo.document.User;
import com.cargo.utility.Config;

@Component
public class EmailService {

	@Autowired
	public JavaMailSender emailSender;

	@Value("#{'${alert.user.mail}'.split(',')}")
	private List<String> emailIds;

	@Value(value = "${alert.user.subject}")
	private String subject;

	@Value(value = "${alert.user.text}")
	private String text;

	public void sendAlertMail(Product product) {
		try {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(emailIds.toArray(new String[emailIds.size()]));
		message.setSubject(subject);
		message.setText(String.format(text, product.getName(),
				Config.format(product.getQtyAblBack(), Config.QTY_FORMATTER) + " " + product.getUnit().getName(),
				Config.format(product.getAlertBack(),Config.QTY_FORMATTER) + " " + product.getUnit().getName()));
		emailSender.send(message);
		}
		catch (Exception e) {
			System.out.println("Unable to send mail "+ product.getName());
		}

	}
	
	public void sendVendorReport(Workbook workbook, User vendor) throws MessagingException, IOException {
		
		// Define message
	    MimeMessage message = emailSender.createMimeMessage();
	    emailIds.forEach(email ->{
	    	 try {
				message.addRecipient(Message.RecipientType.TO,
					      new InternetAddress(email));
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
	    });
	    message.setSubject("Vendor Report : "+vendor.getFullname());

	    // Create the message part
	    BodyPart messageBodyPart = new MimeBodyPart();

	    // Fill the message
	    messageBodyPart.setText("Hi, PFA");

	    Multipart multipart = new MimeMultipart();
	    multipart.addBodyPart(messageBodyPart);
	    
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    workbook.write(bos); // write excel data to a byte array
	    bos.close();
	    workbook.close();
	    // Part two is attachment
	    messageBodyPart = new MimeBodyPart();
	    DataSource source = new ByteArrayDataSource(bos.toByteArray(), "application/vnd.ms-excel");
	    messageBodyPart.setDataHandler(new DataHandler(source));
	    messageBodyPart.setFileName(vendor.getFullname()+" Report");
	    multipart.addBodyPart(messageBodyPart);

	    // Put parts in message
	    message.setContent(multipart);
	    
	    emailSender.send(message);
		
	}

}

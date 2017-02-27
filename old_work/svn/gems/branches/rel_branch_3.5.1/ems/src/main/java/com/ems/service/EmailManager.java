package com.ems.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.EmailConfigurationDao;
import com.ems.model.EmailConfiguration;
import com.ems.model.EventsAndFault;
import com.ems.service.helper.thread.Queue;

@Service("emailManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmailManager {

	private static final Logger log = Logger.getLogger(EmailManager.class);

	@Autowired
	private EmailConfigurationDao emailConfigurationDao;
	
	@Resource
	private EventsAndFaultManager eventsAndFaultManager;
	
	private static final Object LOCK = new Object();
	
	private final Queue<EmailDTO> emailQueue ; 
	private final static int noOfThreads =  Runtime.getRuntime().availableProcessors() + 2;
	private final ExecutorService es = Executors.newFixedThreadPool(noOfThreads);
	//private final EmailThread thread ;
	public EmailManager(){
		emailQueue = new Queue<EmailDTO>(250,this); 
		//thread = new EmailThread(emailQueue);
		for(int i =1; i <= noOfThreads; i++){
			es.submit(new EmailThread(emailQueue));
		}
	}
	
	public void save(final EmailConfiguration o){
		emailConfigurationDao.saveObjectUpload(o);
	}
	
	public EmailConfiguration loadEmailConfig(){
		return emailConfigurationDao.loadEmailConfiguration();
	}
	
	public void addNewThread(){
		es.submit(new EmailThread(emailQueue));
	}
	public void doSendEmail(final EmailDTO dto) {
		log.info("Current queue size is "+ emailQueue.size());
		emailQueue.enqueue(dto);
	}

	public static class EmailDTO {
		private String commaSeperatedRecipientList;
		private String htmlSubject;
		private String htmlMessage; 
		private String fromEmail;
		
		private List<AttachmentInfo> attachments = new ArrayList<AttachmentInfo>();
		
		public static class AttachmentInfo{
			private InputStream fileInputStream;
			private String fileName;
			private String mimeType = "text/plain";
			public InputStream getFileInputStream() {
				return fileInputStream;
			}
			public void setFileInputStream(InputStream fileInputStream) {
				this.fileInputStream = fileInputStream;
			}
			public String getFileName() {
				return fileName;
			}
			public void setFileName(String fileName) {
				this.fileName = fileName;
			}
			public String getMimeType() {
				if(mimeType == null){
					mimeType = "text/plain";
				}
				return mimeType;
			}
			public void setMimeType(String mimeType) {
				this.mimeType = mimeType;
			}
			
		}
		
		public String getCommaSeperatedRecipientList() {
			return commaSeperatedRecipientList;
		}
		public void setCommaSeperatedRecipientList(String commaSeperatedRecipientList) {
			this.commaSeperatedRecipientList = commaSeperatedRecipientList;
		}
		public String getHtmlSubject() {
			return htmlSubject;
		}
		public void setHtmlSubject(String htmlSubject) {
			this.htmlSubject = htmlSubject;
		}
		public String getHtmlMessage() {
			return htmlMessage;
		}
		public void setHtmlMessage(String htmlMessage) {
			this.htmlMessage = htmlMessage;
		}
		public String getFromEmail() {
			return fromEmail;
		}
		public void setFromEmail(String fromEmail) {
			this.fromEmail = fromEmail;
		}
		public List<AttachmentInfo> getAttachments() {
			return attachments;
		}
		public void setAttachments(List<AttachmentInfo> attachments) {
			this.attachments = attachments;
		}
		
	}
	private class EmailThread extends Thread{
		
		private final Queue<EmailDTO> emailQueue;
		public EmailThread(final Queue<EmailDTO> emailQueue){
			this.emailQueue = emailQueue;
		}
		public void run(){
			while(true){
				try{
					 EmailDTO dto = emailQueue.dequeue();
					 final Properties props = emailConfigurationDao.loadEmailConfigurationProperties();
					 final Session session = Session.getInstance(props,
					 new javax.mail.Authenticator() {
					 protected PasswordAuthentication getPasswordAuthentication() {
					 return new PasswordAuthentication(props
					 .getProperty("username"), props
					 .getProperty("password"));
					 }
					 });
					final MimeMessage msg = new MimeMessage(session);
					//final MimeMessage msg = mailSender.createMimeMessage();
					String from = "";
					if (StringUtils.isEmpty(dto.getFromEmail())) {
						from = props.getProperty("username");
					} else {
						from = dto.getFromEmail();
					}
			        msg.setFrom(new InternetAddress(from));
			        //InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
			        msg.setRecipients(Message.RecipientType.TO, dto.getCommaSeperatedRecipientList());
			        msg.setSubject(dto.getHtmlSubject());
			        msg.setSentDate(new Date());
			        // creates message part
			        MimeBodyPart messageBodyPart = new MimeBodyPart();
			        messageBodyPart.setContent(dto.getHtmlMessage(), "text/html");
			 
			        // creates multi-part
			        Multipart multipart = new MimeMultipart();
			        multipart.addBodyPart(messageBodyPart);
			        if(dto.getAttachments().size() > 0){
			        	
				        for (EmailDTO.AttachmentInfo attInfo : dto.getAttachments()){
				        	 MimeBodyPart attachment= new MimeBodyPart();
					        ByteArrayDataSource ds = null;
					        String mimeType = attInfo.getMimeType();
					        ds = new ByteArrayDataSource(attInfo.getFileInputStream(), mimeType); 
					        attachment.setDataHandler((new DataHandler(ds)));
					        attachment.setFileName(attInfo.getFileName());
					        multipart.addBodyPart(attachment);
				        } 
			        }
			        // sets the multi-part as e-mail's content
			        msg.setContent(multipart);
			        //mailSender.send(msg);
			        Transport.send(msg);
			        log.info(this.getId()+":Email sent successfully "+"Current queue size is "+ emailQueue.size());
			        //Update the event alarm
			        synchronized(LOCK){
			        	eventsAndFaultManager.addUpdateSingleAlarm(null, EventsAndFault.EMAIL_NOTIFICATION_EVENT_TYPE, false);
			        }
				} catch (Exception e) {
					log.error("PROBLEM IN SENDING MAIL:", e);
					synchronized(LOCK){
			        	eventsAndFaultManager.addUpdateSingleAlarm(e.getMessage(), EventsAndFault.EMAIL_NOTIFICATION_EVENT_TYPE, true);
			        }
				}
			}
		}
	}

}

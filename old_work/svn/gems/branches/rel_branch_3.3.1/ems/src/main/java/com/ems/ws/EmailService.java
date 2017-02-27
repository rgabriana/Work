/**
 * 
 */
package com.ems.ws;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.EmailManager;
import com.sun.jersey.core.header.ContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

@Transactional(propagation = Propagation.REQUIRED)
@Controller
@Path("/org/email/v1")
public class EmailService {
	
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	@Context
	private ServletContext context;
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
    @Resource(name = "emailManager")
    private EmailManager emailManager;
    private static final Logger logger = Logger.getLogger(EmailService.class);

    public EmailService() {

    }
    
/**
 * Client can attach multiple files using the FormDataMultiPart form.
 * @param form
 * @return
 * @throws Exception
 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("send")
	public String sendEmail(
			FormDataMultiPart form
//			@FormDataParam("file") InputStream fileInputStream,
//			@FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
//			@FormDataParam("subject") String subject,
//			@FormDataParam("message") String htmlMessage,
//			@FormDataParam("recipient") String commaSeperatedRecipients,
//			@FormDataParam("from") String from
			) throws Exception {
		Response res = null;
		try {
			FormDataBodyPart filePart = null;
			filePart = form.getField("recipient");
			final String commaSeperatedRecipients = filePart == null ? null : filePart.getValue();
			filePart = form.getField("subject");
			final String subject = filePart == null ? null : filePart.getValue();
			filePart = form.getField("message");
			final String htmlMessage = filePart == null ? null : filePart.getValue();
			filePart = form.getField("from");
			final String from = filePart == null ? null : filePart.getValue();
			
			
			if(StringUtils.isEmpty(commaSeperatedRecipients)){
				res = Response.serverError().entity("Recipients is empty")
						.status(Response.Status.NOT_ACCEPTABLE).build();
			}
			
			final EmailManager.EmailDTO dto = new EmailManager.EmailDTO();
			dto.setCommaSeperatedRecipientList(commaSeperatedRecipients);
			if(form.getFields("file") != null){
				for (FormDataBodyPart part : form.getFields("file")){
					filePart = part;
					ContentDisposition contentDispositionHeader =  filePart.getContentDisposition();
					InputStream fileInputStream = filePart.getValueAs(InputStream.class);
					if(fileInputStream != null){
						
						final EmailManager.EmailDTO.AttachmentInfo attachment = new EmailManager.EmailDTO.AttachmentInfo();
						
						final String fileName = contentDispositionHeader == null ? null
								: contentDispositionHeader.getFileName();
						final String extn = FilenameUtils.getExtension(fileName);
						byte[] bytes = IOUtils.toByteArray(fileInputStream);
						ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
						attachment.setFileInputStream(bis);
						attachment.setFileName(fileName);
						final String mimeType = context.getMimeType(fileName);
						if(!StringUtils.isEmpty(mimeType)){
							attachment.setMimeType(mimeType);
						}
						dto.getAttachments().add(attachment);
					}
				}
			}
			dto.setFromEmail(from);
			dto.setHtmlMessage(htmlMessage);
			dto.setHtmlSubject(subject);
			emailManager.doSendEmail(dto);
		} catch (Exception e) {
			logger.error("", e);
			res = Response.serverError()
					.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		if(res == null){
			res = Response.ok().entity("SUCCESS").build();
		}		
		return res.getEntity().toString();
	}

}

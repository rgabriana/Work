package com.ems.ws;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;

import java.util.List;

import com.ems.hvac.utils.CryptographyUtil;
import com.ems.model.User;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.EmsAuthenticatedUser;
import com.ems.server.ServerMain;
import com.ems.service.UserManager;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.util.Constants;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * All services here will be escaped without authentication
 * @author admin
 *
 */
@Controller
@Path("/org/public/nonsecure")
public class UnauthenticatedService {
	
	private static final Logger logger = Logger.getLogger("WSLogger");
	public static final String ERR_NOACCESS_NONADMIN="error.access.nonadmin.forgotpassword";
	public static final String USER_NOT_EXISTS="error.nouser.forgotpassword";
	
	@Context
	private HttpServletRequest httpRequest;
	
	@Context 
	private HttpServletResponse httpResponse;
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Autowired
    protected MessageSource messageSource;
	
	@Resource
	UserManager userManager;
	
	
	@GET
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	@Path("download/{email}")
	public Response downloadFile(@PathParam("email") String email) {
		String supportKey = null;
		if(StringUtils.isEmpty(email)){
			return Response.status(Response.Status.BAD_REQUEST).entity("Email is Empty").build();
		}
		try {
			final User dbUser = userManager.loadUserByUserName(email);
			if (dbUser == null){
				final String msg = messageSource.getMessage(USER_NOT_EXISTS,
						null, LocaleContextHolder.getLocale());
				return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			}else if(dbUser.getRole().getRoleType() != RoleType.Admin && dbUser.getRole().getRoleType() != RoleType.FacilitiesAdmin){
				final String msg = messageSource.getMessage(ERR_NOACCESS_NONADMIN,
						null, LocaleContextHolder.getLocale());
				return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			}
			
			final Date d = new Date();
			final String identifier = String.valueOf(d.getTime());
			supportKey = CryptographyUtil.getEncryptedString(Constants.EM_PUB_FILE, identifier+ServerMain.getInstance().getMacAddressByInterfaceName(Constants.NETWK_INTERFACE_ETH0));
			// convert String into InputStream
			InputStream is = new ByteArrayInputStream(supportKey.getBytes());
			httpResponse.setContentLength((int) supportKey.length());
			httpResponse.setHeader("Content-Disposition", "attachment; filename="
	                + "support.key");
	        ServletOutputStream outStream = httpResponse.getOutputStream();
	        byte[] bbuf = new byte[supportKey.length() + 1024];
	        DataInputStream in = new DataInputStream(is);
	        int length = 0;
	        while ((in != null) && ((length = in.read(bbuf)) != -1)) {
	            outStream.write(bbuf, 0, length);
	        }
	        in.close();
	        outStream.flush();
	        
	        dbUser.setForgotPasswordIdentifier(identifier);
			//dbUser.setNoOfLoginAttempts(0l);
			userManager.save(dbUser);
			EmsAuthenticatedUser authenticatedUser = new EmsAuthenticatedUser(
					dbUser);
			UsernamePasswordAuthenticationToken authenticated = new UsernamePasswordAuthenticationToken(
					authenticatedUser, null,
					authenticatedUser.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(
					authenticated);
			HttpSession session = httpRequest.getSession();
			if (session != null) {
				session.setAttribute(
						HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
						SecurityContextHolder.getContext());
			}
			userAuditLoggerUtil.log("User "+email+" has requested for forgot password", UserAuditActionType.Forgot_Password.getName());
			
	        return Response.ok().build();
	        
		} catch (Exception e) {
			logger.error("***EXCEPTION OCCURED****", e);
			e.printStackTrace();
			return Response.serverError()
					.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("upload")
	public Response uploadFile(
			@FormDataParam("file") InputStream fileInputStream, @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,@FormDataParam("email") String email) throws Exception {
		// if project is checked in then only allowed
		// Chk whether customerId, projectId and lineItemId exists in the
		// database for the said project
		try {
			
			//Validate the file uploaded
			final String fileName = contentDispositionHeader == null ? null
					: contentDispositionHeader.getFileName();
			if(!isValidSupporFile(fileName)){
				return Response.status(Response.Status.BAD_REQUEST).entity("File Uploaded is not valid").build();
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
	        StringBuilder out = new StringBuilder();
	        int value=0;
	         // reads to the end of the stream 
	         while((value = reader.read()) != -1)	         {
	        	 final char c = (char)value;
	        	 out.append(c);
	         }
			 final String encryptedTempPass = out.toString();
			 return Response.ok(encryptedTempPass).build();
		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
			return Response.serverError()
					.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	private boolean isValidSupporFile(final String fileName){
		boolean bReturn = false;
		if(StringUtils.isEmpty(fileName)){
			return false;
		}
		final List<String> allowedFiles = Arrays.asList(Constants.FORGOT_PW_FILE_EXTENSIONS_ALLOWED.split(Constants.COMMA));
		final String fileExtn = FilenameUtils.getExtension(fileName);
		if(StringUtils.isEmpty(fileExtn)){
			return false;
		}
		final String lowercaseFileExn = fileExtn.toLowerCase();
		if(allowedFiles.contains(lowercaseFileExn)){
			return true;
		}
		return bReturn;
		
		
	}
}

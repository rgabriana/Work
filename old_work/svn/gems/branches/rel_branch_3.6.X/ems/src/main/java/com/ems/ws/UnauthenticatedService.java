package com.ems.ws;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import com.ems.hvac.utils.CryptographyUtil;
import com.ems.model.NetworkInterfaceMapping;
import com.ems.model.User;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.EmsAuthenticatedUser;
import com.ems.server.ServerMain;
import com.ems.service.NetworkSettingsManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.UserManager;
import com.ems.types.NetworkType;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.util.Constants;
import com.ems.utils.AdminUtil;
import com.sun.jersey.core.header.ContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

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
	
	@Resource
	SystemConfigurationManager systemConfigurationManager;
	
	@Resource
	NetworkSettingsManager networkSettingsManager;
	
	
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
			
			//If external user then also not allowed to use this facility
			if(systemConfigurationManager.isExternalUser(dbUser)){
				final String msg = messageSource.getMessage(Constants.EXTERNAL_USER_NOT_ALLOWED,
						null, LocaleContextHolder.getLocale());
				return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			}
			
			final Date d = new Date();
			final String identifier = String.valueOf(d.getTime());
			NetworkInterfaceMapping nimCorporate = networkSettingsManager.loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Corporate.getName());
			String corporateMapping=Constants.NETWK_INTERFACE_ETH0;
			if(nimCorporate != null && nimCorporate.getNetworkSettings()!= null && nimCorporate.getNetworkSettings().getName() != null){
				corporateMapping = nimCorporate.getNetworkSettings().getName();
			}
			
			supportKey = CryptographyUtil.getEncryptedString(Constants.EM_PUB_FILE, identifier+ServerMain.getInstance().getMacAddressByInterfaceName(corporateMapping));
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
	@Produces(MediaType.TEXT_PLAIN)
	@Path("retrievepasskey")
	public Response retrievePasswordKeyFromUSB(FormDataMultiPart form) throws Exception {
		InputStream targetStream = null;
		String fileName = null;
		String msg = "";
		FormDataBodyPart filePart = null;
		
		//Check username exists in db
		msg = messageSource.getMessage(Constants.ERROR_LOGIN_ATTEMPTS_GENERAL,
				null, LocaleContextHolder.getLocale());
		filePart = form.getField("email");
		final String email = filePart == null ? null : filePart.getValue();
		User user = null;
		if(!StringUtils.isEmpty(email)){
			user = userManager.loadUserByUserName(email);
			if(user == null){
				return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			}
			//If external user then also not allowed to use this facility
			if(systemConfigurationManager.isExternalUser(user)){
				msg = messageSource.getMessage(Constants.EXTERNAL_USER_NOT_ALLOWED,
						null, LocaleContextHolder.getLocale());
				return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			}
			if(!user.isActive()){
				msg = messageSource.getMessage(Constants.INACTIVE_ACCT,
					null, LocaleContextHolder.getLocale());
				return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			}
		}else{
			return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
		}
				
		if(form.getFields("file") == null){
			//form.bodyPart(new FileDataBodyPart("file", new File("D:\\enlighted\\em_public.key"),MediaType.MULTIPART_FORM_DATA_TYPE));
			//get the support file name 
			filePart = form.getField("supportFileName");
			fileName = filePart == null ? null : filePart.getValue();
			if(StringUtils.isEmpty(fileName)){
				return Response.status(Response.Status.BAD_REQUEST).entity("Support FileName does not exists.").build();
			}
			//Iterate of USB and find the file name
			final ArrayList<String> usbListing = AdminUtil.getMountedUsbSticks();
			//final ArrayList<String> usbFilesString = getAllFilesInMountedUSBs(usbListing, null);
			final ArrayList<String> usbFilesString = getAllFilesInMountedUSBs(usbListing, fileName); // Delete above line
			if (usbFilesString != null && usbFilesString.size() > 0) {
				for (String fileString : usbFilesString) {
					final File file = new File(fileString);
					final FileDataBodyPart part = new FileDataBodyPart("file", file);
					targetStream = new FileInputStream(file);
					form.bodyPart(part);
					break;
				}
			}
		}
		msg = messageSource.getMessage(Constants.USB_ERROR_FORGOTPASS,
					null, LocaleContextHolder.getLocale());
		if(targetStream == null || StringUtils.isEmpty(fileName)){
			return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
		}
		
		final Response res = getEncryptedPass(targetStream, fileName);
		if(res.getStatus() == Response.Status.OK.getStatusCode()){
			final String msgLog = "User "+email+" trying to reset his password using forgot password functionality. The support string is not yet validated.";
			logAuditUnauthService(msgLog, user);
		}
		return  res;
	}


	/**
	 * To log message for audit purpose for unauthenticated service where you are needed to explicitely create the EmsAuthenticatedUser and 
	 * apply the security context on session to work it properly during logging to database
	 * 
	 * @param message
	 * @param user
	 */
	private void logAuditUnauthService(final String message, User user) {
		EmsAuthenticatedUser authenticatedUser = new EmsAuthenticatedUser(
				user);
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
		userAuditLoggerUtil.log(message, UserAuditActionType.Forgot_Password.getName());
	}
	
	public ArrayList<String>  getAllFilesInMountedUSBs(final ArrayList<String> usbListing, final String fileNameToSearch){
		ArrayList<String> fileList = new ArrayList<String>();
		Runtime rt = Runtime.getRuntime();
		Process proc;
		String fullUsbPath = "";
		for (String usbname: usbListing) {
			fullUsbPath = "/media/" + usbname;
			try {
				String[] cmdArr = { "bash", ServerMain.getInstance().getTomcatLocation() + "adminscripts/checkfileindirectory.sh", fullUsbPath,  fileNameToSearch};
				proc = rt.exec(cmdArr);
				AdminUtil.readStreamInThread(proc.getErrorStream(), true);
				BufferedReader outputStream = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));
				String output = null;
	
				while ((output = outputStream.readLine()) != null) {
					//output += "#" + fullUsbPath;
					fileList.add(output);
				}
			} catch (IOException ioe) {
				logger.error("ERROR: While getting files from usb ", ioe);
			}
		}
		return fileList;
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("upload")
	public Response uploadFile(FormDataMultiPart form) throws Exception {
			//@FormDataParam("file") InputStream fileInputStream, @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,@FormDataParam("email") String email) throws Exception {
		return retrieveTempPass(form);
	}

	private Response retrieveTempPass(FormDataMultiPart form) {
		try {
			FormDataBodyPart filePart = null;
			if(form.getFields("file") != null){
				for (FormDataBodyPart part : form.getFields("file")){
					filePart = part;
					break;
				}
			}
			if(filePart == null){
				return Response.status(Response.Status.BAD_REQUEST).entity("No file to upload").build();
			}
			ContentDisposition contentDispositionHeader =  filePart.getContentDisposition();
			InputStream fileInputStream = filePart.getValueAs(InputStream.class);
			if(fileInputStream == null){
				return Response.status(Response.Status.BAD_REQUEST).entity("No inputStream from file").build();
			}

			//Validate the file uploaded
			final String fileName = contentDispositionHeader == null ? null
					: contentDispositionHeader.getFileName();
			return getEncryptedPass(fileInputStream, fileName);
		} catch (Exception e) {
			logger.error("", e);
			e.printStackTrace();
			return Response.serverError()
					.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}


	private Response getEncryptedPass(InputStream fileInputStream,
			final String fileName) throws IOException {
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

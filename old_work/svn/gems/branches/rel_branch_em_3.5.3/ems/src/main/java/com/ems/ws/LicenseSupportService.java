package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.annotation.Resource;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import com.ems.service.LicenseSupportManager;

@Controller
@Path("/org/licenseSupportService")
public class LicenseSupportService {
	
	@Resource
    private LicenseSupportManager licenseSupportManager;
	
	@Path("uploadlicense")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public String uploadLicense(String encryptedString) {
	   return licenseSupportManager.uploadLicenseFile(encryptedString);
    }
	
}

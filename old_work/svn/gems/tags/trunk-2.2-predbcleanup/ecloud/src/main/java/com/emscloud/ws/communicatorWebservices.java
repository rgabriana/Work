package com.emscloud.ws;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import com.emscloud.ws.util.WebServiceUtils;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Controller
@Path("/org/communicate")
public class communicatorWebservices {

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	/* @Value("${path.save.communicator.jason.file}") */
	String jasonFilebasePath = "G://uploaded/";

	@Path("em/data")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response communicate(
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {

		String uploadedFileLocation = jasonFilebasePath
				+ fileDetail.getFileName();

		WebServiceUtils.writeToFile(uploadedInputStream, uploadedFileLocation);
		try {
			uploadedInputStream.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String output = "File uploaded to Cloud ";

		return Response.status(200).entity(output).build();

	}

}

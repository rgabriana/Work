package com.emsmgmt.utils;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.emsmgmt.utils.RequestUtil;


public class FirmwareUpgradeDownloadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String userPath= System.getProperty("user.dir");
		File inFolder=new File(userPath+"/UpgradeImgaes");
		if(!inFolder.exists()){
			inFolder.mkdir();
		}
		File outFolder=new File(userPath+"/UpgradeImgaes.zip");
		RequestUtil.zipFolder(inFolder, outFolder);
		ServletOutputStream servletOutputStream = null;
		try{
			servletOutputStream = resp.getOutputStream();
			resp.setContentType("application/octet-stream");
		    resp.addHeader("Content-Disposition", "attachment; filename="+ " FirmwareUpgrade.zip");
		    byte[] bytes = RequestUtil.getBytesFromFile(outFolder);
		    resp.setContentLength((int) bytes.length);
			servletOutputStream.write(bytes);
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				servletOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}

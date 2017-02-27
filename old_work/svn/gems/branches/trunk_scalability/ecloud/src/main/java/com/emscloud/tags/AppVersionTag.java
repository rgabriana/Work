package com.emscloud.tags;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.io.IOUtils;

public class AppVersionTag extends TagSupport {

	private static final long serialVersionUID = 12342342342234L;
	private String appName = "ecloud";
    private String appVersion = null;

    public int doStartTag() throws JspException {

        // Populate version if it's not already done. As this info
        // does not changes, leet's do it only once
        if (appVersion == null) {
            appVersion = "v: DEVELOPMENT";
            populateAppVersion();
        }
        try {
            JspWriter out = pageContext.getOut();
            out.print(appVersion);
        } catch (IOException ioe) {

        }
        return (SKIP_BODY);
    }

    /**
     * It reads the Manifest file and populates the version details from there
     */
    private void populateAppVersion() {
    	FileInputStream fis = null;
        try {
            String appServerHome = pageContext.getServletContext().getRealPath("/");
            File manifestFile = new File(appServerHome, "META-INF/MANIFEST.MF");
            Manifest mf = new Manifest();
            fis = new FileInputStream(manifestFile);
            mf.read(new FileInputStream(manifestFile));
            Attributes atts = mf.getAttributes(appName);
            if (atts != null) {
                appVersion = "v " + atts.getValue("Implementation-Version") + "." + atts.getValue("Build-Version");
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
        finally {
        	IOUtils.closeQuietly(fis);
        }
    }

    public int doEndTag() {
        return EVAL_PAGE;
    }
}

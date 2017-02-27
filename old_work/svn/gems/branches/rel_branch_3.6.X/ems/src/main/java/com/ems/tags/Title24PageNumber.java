package com.ems.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class Title24PageNumber extends TagSupport {

	public static int pageNum =0;
	
	public int doStartTag() throws JspException {
        JspWriter out = pageContext.getOut();
        try {
        	pageNum++;
        	String hexValue = Integer.toHexString(pageNum);
        	out.print(hexValue);
        } catch (IOException e) {
		}
        return (SKIP_BODY);
    }

}

package com.ems.tags;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class ConvertDecimalToHexTag extends TagSupport{
	protected int decimalValue = 0;
	
	public int getDecimalValue() {
	   return (this.decimalValue);
	}
	
	public void setDecimalValue(int decimalValue) {
	   this.decimalValue = decimalValue;
	}
		
    public int doStartTag() throws JspException {
        JspWriter out = pageContext.getOut();
        try {
        	String hexValue = Integer.toHexString(this.decimalValue);
        	out.print(hexValue);
        } catch (IOException e) {
		}
        return (SKIP_BODY);
    }
}

package com.ems.tags;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class SecondsIntervalStringTag extends TagSupport {
	protected long seconds;
	
	public void setSeconds(long seconds){
		this.seconds = seconds;
	}

	public long getSeconds(){
		return this.seconds;
	}
	
	 public int doStartTag() throws JspException {
	        JspWriter out = pageContext.getOut();
	        try {
	        	long millis = this.seconds*1000;
	        	out.print(getIntervalString(millis));
   	        } catch (IOException e) {
			}
	        return (SKIP_BODY);
	    }

    private String getIntervalString(long millis)
	    {
	    	if (millis <= 0) {
				return "0 sec ago";
			}

			long x = millis / 1000;
	    	long seconds = x % 60;
			x /= 60;
	    	long minutes = x % 60;
			x /= 60;
	    	long hours = x % 24;
			x /= 24;
	    	long days = x;
	    	
			if (days > 0) {
				if (hours > 0) {
					return days + " days, " + hours + " hrs ago";
				} else {
					return days + " days ago";
				}
			} else if (hours > 0) {
				if (minutes > 0) {
					return hours + " hrs, " + minutes + " min ago";
				} else {
					return hours + " hrs ago";
				}
			} else if (minutes > 0) {
				if (seconds > 0) {
					return minutes + " min, " + seconds + " sec ago";
				} else {
					return minutes + " sec ago";
				}
			} else {
				return seconds + " sec ago";
			}
	    }
	    
	    public int doEndTag() {
	        return EVAL_PAGE;
	    }
}

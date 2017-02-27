package com.ems.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class Title24PageNumberStartCounter extends TagSupport {

	private static Logger syslog = Logger.getLogger("SysLog");
	public Integer startPageNumValueint = null;
	public String startPageNumValue = null;
	
	public int doStartTag() throws JspException {
    	if (startPageNumValueint != null){
    		Title24PageNumber.pageNum = startPageNumValueint - 1;
    	}else{
    		Title24PageNumber.pageNum = 0;
    	}
        return (SKIP_BODY);
    }




	public String getStartPageNumValue() {
		return startPageNumValue;
	}



	public void setStartPageNumValue(String startPageNumValue) {
		if (StringUtils.isEmpty(startPageNumValue)){
			this.startPageNumValueint = null;
		}else{
			try {
				int i = Integer.parseInt(startPageNumValue);
				this.startPageNumValueint = i;
			} catch (NumberFormatException e) {
				syslog.error("Exception in setting start pagenumber value",e);
				this.startPageNumValueint = 0;
			}
		}
	}

}

package com.emscloud.tags;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class DateIntervalTag extends TagSupport {
	protected String dateValue = null;
	protected String datePattern = "yyyy-MM-dd";
	
	public String getDateValue() {
	   return (this.dateValue);
	}
	public void setDateValue(String dateValue) {
	   this.dateValue = dateValue;
	}
	
	public String getDatePattern() {
		   return (this.datePattern);
		}
	public void setDatePattern(String datePattern) {
	   this.datePattern = datePattern;
	}
		
    public int doStartTag() throws JspException {
        JspWriter out = pageContext.getOut();
        try {
        	SimpleDateFormat sdf = new SimpleDateFormat(this.datePattern);
        	Date paramDate = sdf.parse(this.dateValue);
        	
        	long diff = (new Date().getTime()) - (paramDate.getTime());
        	out.print(getIntervalString(diff));
//        	out.print(this.dateValue + " >> " +this.datePattern + " >> " +paramDate+ " >> " +new Date()+ " >> " +diff+ "  >>  "+ getIntervalString(diff));
        } catch (ParseException pe) {
        	try {
				out.print("failed to parse date value");
			} catch (IOException e) {
			}
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
//		if (millis <= 0) {
//			return "0 sec ago";
//		}
//
//		Calendar cal = Calendar.getInstance();
//		cal.setTimeInMillis(millis);
//
//		if (cal.get(Calendar.DAY_OF_MONTH) > 0) {
//			if (cal.get(Calendar.HOUR_OF_DAY) > 0) {
//				return cal.get(Calendar.DAY_OF_MONTH) + " days, " + cal.get(Calendar.HOUR_OF_DAY) + " hrs ago";
//			} else {
//				return cal.get(Calendar.DAY_OF_MONTH) + " days ago";
//			}
//		} else if (cal.get(Calendar.HOUR_OF_DAY) > 0) {
//			if (cal.get(Calendar.MINUTE) > 0) {
//				return cal.get(Calendar.HOUR_OF_DAY) + " hrs, " + cal.get(Calendar.MINUTE) + " min ago";
//			} else {
//				return cal.get(Calendar.HOUR_OF_DAY) + " hrs ago";
//			}
//		} else if (cal.get(Calendar.MINUTE) > 0) {
//			if (cal.get(Calendar.SECOND) > 0) {
//				return cal.get(Calendar.MINUTE) + " min, " + cal.get(Calendar.SECOND) + " sec ago";
//			} else {
//				return cal.get(Calendar.MINUTE) + " sec ago";
//			}
//		} else {
//			return cal.get(Calendar.SECOND) + " sec ago";
//		}
        
//        Date date = new Date(0, 0, 0, 0, 0, strOccValue, 0);
//        if(date.getDay() > 0)
//        {
//            if(date.getHours() > 0)
//                return date.getDay().toString() + " days, " + date.getHours().toString() + " hrs ago";
//            else
//                return date.getDay().toString() + " days ago";
//        }
//        else if(date.getHours() > 0)
//        {
//            if(date.getMinutes() > 0)
//                return date.getHours().toString() + " hrs, " + date.getMinutes().toString() + " min ago";
//            else
//                return date.getHours().toString() + " hrs ago";
//        }
//        else if(date.getMinutes() > 0)
//        {
//            if(date.getSeconds() > 0)
//                return date.getMinutes().toString() + " min, " + date.getSeconds().toString() + " sec ago";
//            else
//                return date.getMinutes().toString() + " min ago";
//        }
//        else
//            return date.getSeconds().toString() + " sec ago";
    }
    
    public int doEndTag() {
        return EVAL_PAGE;
    }
}

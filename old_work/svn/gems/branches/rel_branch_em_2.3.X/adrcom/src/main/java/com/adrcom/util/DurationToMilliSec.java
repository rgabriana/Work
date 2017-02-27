package com.adrcom.util;

public class DurationToMilliSec {
	
	public static long getMilliSecFromDuration(String duration) {
		boolean isNegative = false;
		long output = 0;
		
		if(duration != null && !"".equals(duration)) {
			if(duration.startsWith("+P") || duration.startsWith("P")) {
				duration = duration.split("P")[1];
				output = parseAndReturn(duration);
			}
			else if(duration.startsWith("-P")) {
				duration = duration.split("P")[1];
				output = parseAndReturn(duration);
				isNegative = true;
			}
			else if(duration.matches("^\\d+W$")) {
				Long weeks = Long.parseLong(duration.split("W")[0]);
				output = output + weeks*7*24*60*60*1000;
			}
		}
		if(isNegative) {
			output = -1 * output;
		}
		return output;
	}
	
	private static long parseAndReturn(String duration) {
		long output = 0;
		String[] arr;
		if(duration.matches("^\\d+Y.*")) {
			arr = duration.split("Y");
			Long years = Long.parseLong(arr[0]);
			output = output + years*365*24*60*60*1000;
			if(arr.length > 1) {
				duration = duration.split("Y")[1];
			}
			else {
				duration = "";
			}
		}
		if(duration.matches("^\\d+M.*T.*")) {
			arr = duration.split("M");
			Long months = Long.parseLong(arr[0]);
			output = output + months*30*24*60*60*1000;
			if(arr.length > 1) {
				duration = duration.replaceFirst("M", "Month").split("Month")[1];
			}
			else {
				duration = "";
			}
		}
		if(duration.matches("^\\d+D.*")) {
			arr = duration.split("D");
			Long days = Long.parseLong(arr[0]);
			output = output + days*24*60*60*1000;
			if(arr.length > 1) {
				duration = duration.split("D")[1];
			}
			else {
				duration = "";
			}
		}
		if(duration.matches("^T.*")) {
			arr = duration.split("T");
			if(arr.length > 1) {
				duration = arr[1];
			}
			else {
				duration = "";
			}
		}
		if(duration.matches("^\\d+H.*")) {
			arr = duration.split("H");
			Long hours = Long.parseLong(arr[0]);
			output = output + hours*60*60*1000;
			if(arr.length > 1) {
				duration = duration.split("H")[1];
			}
			else {
				duration = "";
			}
		}
		if(duration.matches("^\\d+M.*")) {
			arr = duration.split("M");
			Long minutes = Long.parseLong(arr[0]);
			output = output + minutes*60*1000;
			if(arr.length > 1) {
				duration = duration.split("M")[1];
			}
			else {
				duration = "";
			}
		}
		if(duration.matches("^\\d+S$")) {
			Long seconds = Long.parseLong(duration.split("S")[0]);
			output = output + seconds*1000;
		}
		return output;
	}

}

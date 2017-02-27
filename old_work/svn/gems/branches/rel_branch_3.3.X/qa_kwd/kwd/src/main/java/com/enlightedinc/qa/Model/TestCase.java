package com.enlightedinc.qa.Model;

import org.apache.log4j.Logger;




public class TestCase{


    static Logger log = Logger.getLogger(TestCase.class);

    String name;
	String testCaseFileName;
	int linesPass = 0;
	int linesFail = 0;
	boolean isPassed=true;

	public TestCase(String testCaseLocation, String testCaseFileName) {
		super();
        this.testCaseFileName = testCaseLocation + testCaseFileName;
        this.name = testCaseFileName.replace("/", "_");
	}

	public String getTestCaseFileName() {
		return testCaseFileName;
	}
	
	public void incrementLinesPass(){
		linesPass++;
	}
	
	public void incrementLinesFail(){
		linesFail++;
	}

	public int getLinesPass() {
		return linesPass;
	}

	public int getLinesFail() {
		return linesFail;
	}
	
	public void setFail(){
		isPassed = false;
	}
	
	public boolean isPassed(){
		return isPassed;
	}
	

	public String getName() {
        return name;
    }
} 
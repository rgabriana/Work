package com.enlightedinc.qa;

import org.apache.log4j.Logger;
import com.enlightedinc.qa.Model.TestSuite;

/**
 * 
 * @author lalit
 * 
 */
public class KwdMain {

    private static Logger log = Logger.getLogger(KwdMain.class);

	public static void main(String[] args) throws Throwable {
		

		try {

			String serverUrl = "";
			String browserType = "";
			String testSuite = "";
			String testCasesFolder = "";
			String testResultLocation = "";
			//String dataFileLocation = "";
			//String dataSuiteName="";

			if (args == null || args[0] == null || args[1] == null
					|| args[2] == null || args[3] == null || args[4] == null ) {
				log.fatal("4 argument needed in form : <Server Url> <Browser Type> <Test Suite excel> <Test Cases Location> <Result Location> <Data File Location>");
			}

			if (args[0] != null) {
				serverUrl = args[0];
			}

			if (args[1] != null) {
				browserType = args[1];
			}

			if (args[2] != null) {
				testSuite = args[2];
			}

			if (args[3] != null) {
				testCasesFolder = args[3];
			}
			
			if (args[4] != null) {
			    testResultLocation = args[4];
            }
         	ExecutionContext context = new ExecutionContext(serverUrl,
					browserType);
			TestSuite suite = new TestSuite(testSuite, testCasesFolder, testResultLocation, context);
			suite.executeTestSuite();
		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

package com.enlightedinc.qa.Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.enlightedinc.qa.ExecutionContext;
import com.enlightedinc.qa.testcase.Executor;

public class TestSuite {

    static Logger log = Logger.getLogger(TestSuite.class);

    String suiteName;
    String testCasesLocation;
    String testResultLocation;
    List<TestCase> testCaseList = new ArrayList<TestCase>();;
    ExecutionContext context;
    int testCasesPassed = 0;
    int testCasesFailed = 0;

    public TestSuite(String suitName, String testCasesLocation, String testResultLocation, ExecutionContext context) {
        this.suiteName = suitName;
        this.testCasesLocation = testCasesLocation;
        this.testResultLocation = testResultLocation;
        this.context = context;
    }

    public void incrementTestCasesPassed() {
        testCasesPassed++;
    }

    public void incrementTestCasesFailed() {
        testCasesFailed++;
    }

    public int getTestCasesPassed() {
        return testCasesPassed;
    }

    public int getTestCasesFailed() {
        return testCasesFailed;
    }

    public void executeTestSuite() throws Throwable {

        FileInputStream suiteRunner = new FileInputStream(suiteName);
        XSSFWorkbook testCaseWB = new XSSFWorkbook(suiteRunner);
        
        XSSFSheet suiteSheet = testCaseWB.getSheet("Suite");
        Iterator rowIter1 = ((Sheet) suiteSheet).rowIterator();

        while (rowIter1.hasNext()) {
            XSSFRow testRow = (XSSFRow) rowIter1.next();
            XSSFCell testCaseCell = testRow.getCell(0);
            XSSFCell modeCell = testRow.getCell(1);
            String testCaseName = testCaseCell.getStringCellValue();
            String mode = modeCell.getStringCellValue();

            if (mode.equalsIgnoreCase("y")) {
                TestCase testCase = new TestCase(this.testCasesLocation, testCaseName);
                testCaseList.add(testCase);
            } else if (mode.equalsIgnoreCase("n")) {

            }
        }
        
        //Let's make the test result workbook       
        XSSFWorkbook testResultWb = new XSSFWorkbook();
        XSSFSheet summaryResultSheet = testResultWb.createSheet("Suite");
        int rowCounter = 0;
        
        XSSFRow summaryPassTestCasesRow = summaryResultSheet.createRow(rowCounter++);
        XSSFCell passTestLabelCell = summaryPassTestCasesRow.createCell(1);
        passTestLabelCell.setCellValue("Test Cases Passed:");
        
        XSSFRow summaryFailsTestCasesRow = summaryResultSheet.createRow(rowCounter++);
        XSSFCell failTestLabelCell = summaryFailsTestCasesRow.createCell(1);
        failTestLabelCell.setCellValue("Test Cases Failed:");
        
        XSSFRow summaryTotalTestCasesRow = summaryResultSheet.createRow(rowCounter++);
        XSSFCell totalTestLabelCell = summaryTotalTestCasesRow.createCell(1);
        totalTestLabelCell.setCellValue("Total Test Cases:");
        
        //Heading for Test case summary
        XSSFRow headerTestCasesDetail = summaryResultSheet.createRow(rowCounter++);
        headerTestCasesDetail.createCell(0).setCellValue("Sl. No.");
        headerTestCasesDetail.createCell(1).setCellValue("Test Case");
        headerTestCasesDetail.createCell(2).setCellValue("Line Pass");
        headerTestCasesDetail.createCell(3).setCellValue("Line Fail");
        
        int numberOfTextCases = 1;
        
        for (TestCase testCase : testCaseList) {
            
            //Create a sheet in the test result Wb
            XSSFSheet resultSheet = testResultWb.createSheet(testCase.name);
            Executor executor = new Executor();
            executor.run(context, testCase, resultSheet);
            if (testCase.isPassed) {
                incrementTestCasesPassed();

            } else {
                incrementTestCasesFailed();
            }
            log.info(testCase.getTestCaseFileName() + ":" + " Lines Passed: " + testCase.getLinesPass()
                    + ", Lines Failed: " + testCase.getLinesFail());
            
            XSSFRow testCaseResultRow = summaryResultSheet.createRow(rowCounter++);
            testCaseResultRow.createCell(0).setCellValue(numberOfTextCases++);
            testCaseResultRow.createCell(1).setCellValue(testCase.getName());
            testCaseResultRow.createCell(2).setCellValue(testCase.getLinesPass());
            testCaseResultRow.createCell(3).setCellValue(testCase.getLinesFail());
        }
        
        log.info("Finished Test Suite Running " + "Test Cases Passed: " + this.getTestCasesPassed()
                + ", Test Cases Failed:" + this.getTestCasesFailed());
        
        summaryPassTestCasesRow.createCell(2).setCellValue(this.getTestCasesPassed()); 
        summaryFailsTestCasesRow.createCell(2).setCellValue(this.getTestCasesFailed()); 
        summaryTotalTestCasesRow.createCell(2).setCellValue(this.getTestCasesPassed() + this.getTestCasesFailed());         
           
        //LEt's write the result of test result workbook to a file
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
        File file = new File(this.suiteName);
        String resultWorkbookName =  format.format(new Date()) + "_Result_" + file.getName();
        File resultFile = new File(testResultLocation, resultWorkbookName);
        resultFile.createNewFile();
       
        FileOutputStream fout = new FileOutputStream(resultFile);
        testResultWb.write(fout);        
        fout.flush();
        fout.close();

    }

}

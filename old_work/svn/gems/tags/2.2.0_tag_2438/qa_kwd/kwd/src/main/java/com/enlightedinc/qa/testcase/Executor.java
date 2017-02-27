package com.enlightedinc.qa.testcase;

import java.io.FileInputStream;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.HasInputDevices;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Mouse;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.support.ui.Select;

import com.enlightedinc.qa.ExecutionContext;
import com.enlightedinc.qa.GlobalConstants;
import com.enlightedinc.qa.Model.TestCase;
import com.enlightedinc.qa.kewywords.Keyword;

/**
 * 
 * @author lalit
 * 
 */
public class Executor {

    private static Logger log = Logger.getLogger(Executor.class);

    private WebDriver webDriver;

    public void run(ExecutionContext context, TestCase testCase, XSSFSheet resultSheet) throws Exception {

        if (context.getBrowserType().equalsIgnoreCase("IEXPLORE")) {
            webDriver = new InternetExplorerDriver();
            log.debug("Internet Explorer Browser");

        } else if (context.getBrowserType().equalsIgnoreCase("CHROME")) {
            webDriver = new ChromeDriver();
            log.debug("Chrome Browser");

        } else if (context.getBrowserType().equalsIgnoreCase("FIREFOX")) {
            webDriver = new FirefoxDriver();
            log.debug("Firefox Browser");

        } else {
            throw new IllegalArgumentException("Browser type not identified");
        }
        webDriver.manage().window().maximize();
        webDriver.get(context.getServerUrl());
        executeTest(testCase, resultSheet);
    }

    private void executeTest(TestCase testCase, XSSFSheet resultSheet) throws Exception {
        FileInputStream testCaseFile = new FileInputStream(testCase.getTestCaseFileName());
        XSSFWorkbook testCaseWB = new XSSFWorkbook(testCaseFile);
        XSSFSheet testCaseSheet = testCaseWB.getSheet("Sheet1");
        Iterator rowIter = testCaseSheet.rowIterator();
        int rowCount = 0;

        while (rowIter.hasNext()) {

            XSSFRow testRow = (XSSFRow) rowIter.next();

            // Write the row to resultSheet
            XSSFRow resultRow = resultSheet.createRow(rowCount++);
            for (int i = 0; i < testRow.getPhysicalNumberOfCells(); i++) {
                XSSFCell resultCell = resultRow.createCell(i);
                if(testRow.getCell(i) != null){
                resultCell.setCellValue(testRow.getCell(i).getStringCellValue());
                }
            }

            XSSFCell typeCell = testRow.getCell(0);
            String type = typeCell.getStringCellValue();

            try {
                if (type.equalsIgnoreCase(Keyword.BUTTON)) {
                    handleButton(testRow);
                } else if (type.equalsIgnoreCase(Keyword.CHOICE)) {
                    handleChoice(testRow);
                } else if (type.equalsIgnoreCase(Keyword.LIST)) {
                    handleList(testRow);
                } else if (type.equalsIgnoreCase(Keyword.LINK)) {
                    handleLink(testRow);
                } else if (type.equalsIgnoreCase(Keyword.TEXT)) {
                    handleText(testRow);
                } else if (type.equalsIgnoreCase(Keyword.VERIFY_TEXT_PRESENT)) {
                    handleverifyTextPresent(testRow);
                } else if (type.equalsIgnoreCase(Keyword.WAIT)) {
                    wait(testRow);
                } else if (type.equalsIgnoreCase(Keyword.JS_ALERT)) {
                    handleJavaScriptAlert(testRow);
                } else if (type.equalsIgnoreCase(Keyword.RADIO)) {
                    handleRadio(testRow);
                }else if(type.equalsIgnoreCase(Keyword.MOUSEHOVER)){
                	mouseHover(testRow);
                }else if(type.equalsIgnoreCase(Keyword.ASSERTFALSE)){
                	assertFalse(testRow);
                }else if(type.equalsIgnoreCase(Keyword.ASSERTTRUE)){
                	assertTrue(testRow);
                }else if (type.equalsIgnoreCase(Keyword.FINISH)) {
                    closeBrowser();
                    break;
                }
                testCase.incrementLinesPass();
            } catch (Exception e) {
                testCase.incrementLinesFail();
                testCase.setFail();

                XSSFCell errorCell = resultRow.createCell(GlobalConstants.RESULT_COLUMN);
                errorCell.setCellValue(e.getMessage());

                XSSFCellStyle failStyle = resultSheet.getWorkbook().createCellStyle();
                failStyle.setFillBackgroundColor(IndexedColors.RED.getIndex());

                for (int i = 0; i < resultRow.getPhysicalNumberOfCells(); i++) {
                    XSSFCell resultCell = resultRow.getCell(i);
                    if (resultCell != null) {
                        resultCell.setCellStyle(failStyle);
                    }
                }

                log.info(e.getMessage());
            }
        }

        testCaseFile.close();
    }

    private void handleChoice(XSSFRow testRow) {
        // TODO Auto-generated method stub

    }

    private void handleRadio(XSSFRow testRow) {

    }

    private void handleJavaScriptAlert(XSSFRow testRow) throws Exception {
        String accessType = testRow.getCell(1).getStringCellValue();

        if (accessType.equalsIgnoreCase(Keyword.Ok)) {
            webDriver.switchTo().alert().accept();
        } else if (accessType.equalsIgnoreCase(Keyword.Cancel)) {
            webDriver.switchTo().alert().dismiss();
        }
    }

    private void handleverifyTextPresent(XSSFRow testRow) {
        String accessType = testRow.getCell(1).getStringCellValue();
        String accessValue = testRow.getCell(2).getStringCellValue();
        WebElement element = getElement(accessType, accessValue);
        String dataValue = testRow.getCell(3).getStringCellValue();
        // String ValueExpected = testRow.getCell(4).getStringCellValue();
        if (element != null) {
            equals((dataValue));
        }

    }

    private void assertFalse(XSSFRow testRow) {
        String accessType = testRow.getCell(1).getStringCellValue();
        String accessValue = testRow.getCell(2).getStringCellValue();
        WebElement element = getElement(accessType, accessValue);
        String dataValue = testRow.getCell(3).getStringCellValue();
        assertFalse(webDriver.findElement(By.id(accessValue)));
       
    }
    
    private void assertTrue(XSSFRow testRow) {
        String accessType = testRow.getCell(1).getStringCellValue();
        String accessValue = testRow.getCell(2).getStringCellValue();
        WebElement element = getElement(accessType, accessValue);
        String dataValue = testRow.getCell(3).getStringCellValue();
        assertTrue(webDriver.findElement(By.id(accessValue)));
       
    }
	private void assertTrue(WebElement findElement) {
		// TODO Auto-generated method stub
		
	}

	private void assertFalse(WebElement findElement) {
		// TODO Auto-generated method stub
		
	}

	private void handleLink(XSSFRow testRow) {
        String accessType = testRow.getCell(1).getStringCellValue();
        String accessValue = testRow.getCell(2).getStringCellValue();
        WebElement element = getElement(accessType, accessValue);
        if (element != null) {
            element.click();
        }
    }

    private void handleButton(XSSFRow testRow) {
        String accessType = testRow.getCell(1).getStringCellValue();
        String accessValue = testRow.getCell(2).getStringCellValue();
        WebElement element = getElement(accessType, accessValue);
        if (element != null) {
            element.sendKeys(Keys.ENTER);
            element.click();
        }
    }
    private void mouseHover(XSSFRow testRow) {
        String accessType = testRow.getCell(1).getStringCellValue();
        String accessValue = testRow.getCell(2).getStringCellValue();
        WebElement element = getElement(accessType, accessValue);
        WebElement menuOption = webDriver.findElement(By.id(accessValue)); 
        Locatable hoverItem = (Locatable) element;
        Mouse mouse = ((HasInputDevices) webDriver).getMouse();
        mouse.mouseMove(hoverItem.getCoordinates()); 
        //Actions builder = new Actions(webDriver);
        //WebElement tagElement = webDriver.findElement(By.id(accessValue));
        //builder.moveToElement(tagElement).build().perform();
        //menuOption.click();
        //webDriver.findElement(By.id(accessValue)).click();
    }

    private void handleList(XSSFRow testRow) {
        String accessType = testRow.getCell(1).getStringCellValue();
        String accessValue = testRow.getCell(2).getStringCellValue();
        WebElement element = getElement(accessType, accessValue);
        String dataValue = testRow.getCell(3).getStringCellValue();
        if (element != null && element.isEnabled()) {
            new Select(element).selectByVisibleText(dataValue);
        }

    }

    private void handleText(XSSFRow testRow) {
        String accessType = testRow.getCell(1).getStringCellValue();
        String accessValue = testRow.getCell(2).getStringCellValue();
        WebElement element = getElement(accessType, accessValue);
        String dataValue = testRow.getCell(3).getStringCellValue();
        if (element != null && element.isEnabled()) {
            element.clear();
            element.sendKeys(dataValue);
        }
    }

    private WebElement getElement(String getBy, String accessValue) {
        WebElement element = null;
        if (getBy.equalsIgnoreCase("xpath")) {
            element = webDriver.findElement(By.xpath(accessValue));
        } else if (getBy.equalsIgnoreCase("name")) {
            element = webDriver.findElement(By.name(accessValue));
        } else if (getBy.equalsIgnoreCase("id")) {
            element = webDriver.findElement(By.id(accessValue));
        } else if (getBy.equalsIgnoreCase("class")) {
            element = webDriver.findElement((By.className(accessValue)));
        }
        return element;
    }

    private void wait(XSSFRow testRow) throws Exception {
        String accessType = testRow.getCell(1).getStringCellValue();
        int i = Integer.parseInt(accessType);
        i = i * 1000;
        Thread.sleep(i);
    }

    public void closeBrowser() {
        webDriver.quit();

    }
}

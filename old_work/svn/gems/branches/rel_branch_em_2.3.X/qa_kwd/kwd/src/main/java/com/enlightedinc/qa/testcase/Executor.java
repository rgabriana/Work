package com.enlightedinc.qa.testcase;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
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
 **/
public class Executor
{
	private static Logger log = Logger.getLogger(Executor.class);
    public static WebDriver webDriver;
    private List<WebElement> profilelist = null;
    private List<WebElement> profileclick = null;
    private static Connection con = null;
    private static Connection conn = null;
    
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
                }else if (type.equalsIgnoreCase(Keyword.IFCLICKHERE)) {
                	ifClickHere(testRow);
                }else if(type.equalsIgnoreCase(Keyword.VERIFYPROFILE)){
                	verifyProfile(testRow);	
                }else if(type.equalsIgnoreCase(Keyword.CLICKONPROFILE)){
                	clickOnProfile(testRow);
                }else if (type.equalsIgnoreCase(Keyword.CLEAR)){
                	clear(testRow);
                }else if (type.equalsIgnoreCase(Keyword.SWITCHTO)){
                	switchTo(testRow);
                }else if (type.equalsIgnoreCase(Keyword.SWITCHBACK)){
                	switchBack(testRow);
                }else if (type.equalsIgnoreCase(Keyword.CREATE_PROFILE)){
                	try {
						profileManagmentTests.Create_Profile();
					} catch (Throwable e) { e.printStackTrace();
					}
                }else if (type.equalsIgnoreCase(Keyword.HANDLEWINDOW)){
                	HandleWindow();
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
    	String accessType = testRow.getCell(1).getStringCellValue();
        String accessValue = testRow.getCell(2).getStringCellValue();
        WebElement element = getElement(accessType, accessValue);
        String dataValue = testRow.getCell(4).getStringCellValue();
        webDriver.findElement(By.name(dataValue)).click();
        webDriver.findElement(By.name(dataValue)).isSelected();
    }
    private void HandleWindow() throws Exception {
    	String HandleBefore = webDriver.getWindowHandle();
        webDriver.switchTo().window(HandleBefore);
    }
    private void switchTo(XSSFRow testRow) {
    	String accessValue = testRow.getCell(1).getStringCellValue();
    	WebElement frame = webDriver.findElement(By.id(accessValue));
    	webDriver.switchTo().frame(frame);  
    }
    private void switchBack(XSSFRow testRow) {
    	webDriver.switchTo().defaultContent();  
    }
    
    private void clear(XSSFRow testRow){
    	String accessType = testRow.getCell(1).getStringCellValue();
        String accessValue = testRow.getCell(2).getStringCellValue();
        WebElement element = getElement(accessType, accessValue);
        String dataValue = testRow.getCell(3).getStringCellValue();
        webDriver.findElement(By.id(accessValue)).clear();
    	
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
        boolean assertFalse =  webDriver.findElements(By.id(accessValue)).size() < 1;
     }
     
   private void assertTrue(XSSFRow testRow) {
        String accessType = testRow.getCell(1).getStringCellValue();
        String accessValue = testRow.getCell(2).getStringCellValue();
        boolean assertTrue =  webDriver.findElements(By.id(accessValue)).size() > 1;
    }
	private void assertTrue(WebElement findElement) {
		
	}
    
    private void handleLink(XSSFRow testRow) {
        String accessType = testRow.getCell(1).getStringCellValue();
        String accessValue = testRow.getCell(2).getStringCellValue();
        WebElement element = getElement(accessType, accessValue);
        if (element != null) {
            element.click();
        }
    }
	private void verifyProfile(XSSFRow testRow)
	{
		String accessType = testRow.getCell(1).getStringCellValue();
        String accessValue = testRow.getCell(2).getStringCellValue();
		profilelist = webDriver.findElements(By.className("outermostdiv"));
		 for (int i = 0; i < profilelist.size(); i++) 
		  {
			if(profilelist.get(i).getText().equals(accessValue))
			{
				assertTrue(webDriver.findElement(By.name(accessValue)));
			}
		}
	}
	private void clickOnProfile(XSSFRow testRow)
	{
		String accessType = testRow.getCell(1).getStringCellValue();
        String accessValue = testRow.getCell(2).getStringCellValue();
        profileclick = webDriver.findElements(By.className("accinner ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom ui-accordion-content-active"));
		for(int i=0; i< profileclick.size(); i++){
		if(profileclick.get(i).getText().equals(accessValue)){
			profileclick.get(i).click();
		}
		}
	}

	private void ifClickHere(XSSFRow testRow) throws Exception 
	{
		if(webDriver.findElements( By.id("clickHere") ).size() != 0 )
		{
			String HandleBefore = webDriver.getWindowHandle();
			webDriver.findElement(By.id("clickHere")).click();
            Thread.sleep(3000);
            webDriver.switchTo().window(HandleBefore);
            webDriver.findElement(By.id("acceptTerms")).click();
            Thread.sleep(4000);
            if(webDriver.findElements( By.id("name") ).size() != 0)
            {
            	webDriver.findElement( By.id("name")).click();
            	
            }else if(webDriver.findElements( By.id("facilitiesMenu") ).size() != 0)
            {
            	webDriver.findElement( By.id("facilitiesMenu") ).click();
            }
            
		}
		else
		{
			webDriver.findElement(By.id("administrationMenu")).click();
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
    
    public void closeBrowser()
    {
        webDriver.quit();
    }

     
  // Database connection for POSTGRES Server
   public static void setDbConnection() throws SQLException
    {
        try {
        	 Class.forName("org.postgresql.Driver");
        	 Connection connection = null;
        	 connection = DriverManager.getConnection("jdbc:postgresql://localhost/ems?characterEncoding=utf-8","postgres", "postgres");
        	 Statement s = connection.createStatement();
        	 if (connection != null)
        	 {
	        	String query="select * from groups";
	        	ResultSet rs=s.executeQuery(query);
	        	while(rs.next())
	        	{
	        		System.out.print(" "+rs.getString(2));
	        	}
        	 }
        	}
        	catch(Exception e){
        		e.printStackTrace();
        	}

        
    }
}
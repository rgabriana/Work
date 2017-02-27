package com.enlightedinc.qa.testcase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.HasInputDevices;
import org.openqa.selenium.Mouse;
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
        
        executeTest(testCase, resultSheet,parseTestCase(testCase, resultSheet));        
        
    }
    
    private XSSFSheet parseTestCase(TestCase testCase, XSSFSheet resultSheet)
    {
		XSSFSheet testCaseParsedSheet = null;
		try {
			FileInputStream testCaseFile = new FileInputStream(
					testCase.getTestCaseFileName());
			XSSFWorkbook testCaseWB = new XSSFWorkbook(testCaseFile);
			XSSFSheet testCaseSheet = testCaseWB.getSheet("Sheet1");
			Iterator rowIter = testCaseSheet.rowIterator();
			int rowCount = 0;			

			testCaseParsedSheet = testCaseWB.createSheet("ResultSheet");

			while (rowIter.hasNext()) {
				XSSFRow testRow = (XSSFRow) rowIter.next();

				XSSFCell typeCell = testRow.getCell(0);
				String type = typeCell.getStringCellValue();

				try {

					if (type.equalsIgnoreCase(Keyword.FOR_START)) {
						String startValue = testRow.getCell(1)
								.getStringCellValue();
						String stopValue = testRow.getCell(2)
								.getStringCellValue();
						String dataSheetName = testRow.getCell(3)
								.getStringCellValue();
						Integer start = Integer.parseInt(startValue);
						Integer end = Integer.parseInt(stopValue);

						HashMap<String, ArrayList<String>> idValuesMap = new HashMap<String, ArrayList<String>>();
						ArrayList<String> copyDataArray = new ArrayList<String>();
						// Fill the idValues pair
						// System.out.println("Print the data sheet");
						if (!dataSheetName.equalsIgnoreCase("")
								&& dataSheetName != null
								&& !dataSheetName.equalsIgnoreCase("None")) {
							XSSFSheet testInputSheet = testCaseWB
									.getSheet(dataSheetName);
							Iterator rowInputIter = testInputSheet
									.rowIterator();
							int inputRowCounter = 0;
							while (rowInputIter.hasNext()) {
								XSSFRow testRowInput = (XSSFRow) rowInputIter
										.next();
								if (inputRowCounter == 0) {
									// copy all the ids
									XSSFCell cell;
									Iterator cellIterator = testRowInput
											.cellIterator();
									while (cellIterator.hasNext()) {
										ArrayList<String> insertList = new ArrayList<String>();
										cell = (XSSFCell) cellIterator.next();
										idValuesMap.put(
												cell.getStringCellValue(),
												insertList);
										// System.out.print(cell.getStringCellValue()
										// + " ");
									}
									//System.out.println();
								} else {

									XSSFCell cell;
									Iterator cellIterator = testRowInput
											.cellIterator();
									while (cellIterator.hasNext()) {
										ArrayList<String> insertList = new ArrayList<String>();
										cell = (XSSFCell) cellIterator.next();
										// idValuesMap.put(cell.getStringCellValue(),
										// insertList);
										// System.out.print(cell.getStringCellValue()
										// + " ");
										copyDataArray.add(cell
												.getStringCellValue());
									}
									// System.out.println();
								}
								// System.out.println("Incrementing the counter");
								inputRowCounter++;
							}

							// Manipulate the data values in the HashMap list
							Integer mapKeyCounter = 0;
							for (Entry<String, ArrayList<String>> entry : idValuesMap
									.entrySet()) {
								ArrayList<String> mapList = entry.getValue();
								Integer dataCounter = mapKeyCounter;
								while (dataCounter < copyDataArray.size()) {
									// System.out.println(copyDataArray.get(dataCounter));
									mapList.add(copyDataArray.get(dataCounter));
									dataCounter = dataCounter
											+ idValuesMap.size();
								}
								entry.setValue(mapList);
								mapKeyCounter++;
							}
						}
						// End manipulation
						// Print via hashmap
						/*
						 * System.out.println("Print via hashmap"); Iterator it
						 * = idValuesMap.entrySet().iterator(); while
						 * (it.hasNext()) { Map.Entry pairs =
						 * (Map.Entry)it.next();
						 * System.out.println(pairs.getKey() + " = " +
						 * pairs.getValue()); //it.remove(); // avoids a
						 * ConcurrentModificationException }
						 */

						// End loop
						// System.out.println("End print data sheet");
						// End filling the pair

						ArrayList<XSSFRow> mRowList = new ArrayList<XSSFRow>();
						while (!type.equalsIgnoreCase(Keyword.FOR_END)) {
							testRow = (XSSFRow) rowIter.next();
							typeCell = testRow.getCell(0);
							type = typeCell.getStringCellValue();
							if (!type.equalsIgnoreCase(Keyword.FOR_END)) {

								XSSFRow rowCopied = testCaseParsedSheet
										.createRow(rowCount++);
								copyRow(testRow, rowCopied);
								if (type.equalsIgnoreCase(Keyword.TEXT)) {
									String idName = testRow.getCell(2)
											.getStringCellValue();
									if (idValuesMap.containsKey(idName)) {
										XSSFCell tempCell = rowCopied
												.createCell(3);
										ArrayList<String> tempList = idValuesMap
												.get(idName);
										tempCell.setCellValue(tempList.get(0));
										// System.out.println("---"+tempList.get(0));
										tempList.remove(0);
										idValuesMap.remove(idName);
										idValuesMap.put(idName, tempList);
									}
								}
								mRowList.add(rowCopied);
							}
						}
						for (int i = start; i <= (end - 1); i++) {
							for (Iterator iterator = mRowList.iterator(); iterator
									.hasNext();) {
								XSSFRow xssfRow = (XSSFRow) iterator.next();
								XSSFRow rowToCopy = testCaseParsedSheet
										.createRow(rowCount++);
								copyRow(xssfRow, rowToCopy);
								String type2 = xssfRow.getCell(0)
										.getStringCellValue();
								if (type2.equalsIgnoreCase(Keyword.TEXT)) {
									String idName = xssfRow.getCell(2)
											.getStringCellValue();
									if (idValuesMap.containsKey(idName)) {
										XSSFCell tempCell = rowToCopy
												.createCell(3);
										ArrayList<String> tempList = idValuesMap
												.get(idName);
										tempCell.setCellValue(tempList.get(0));
										// System.out.println("---"+tempList.get(0));
										tempList.remove(0);
										idValuesMap.remove(idName);
										idValuesMap.put(idName, tempList);
									}
								}
							}

						}
					} else if (type.equalsIgnoreCase(Keyword.FINISH)) {
						// closeBrowser();
						XSSFRow rowCopied = testCaseParsedSheet
								.createRow(rowCount);
						copyRow(testRow, rowCopied);
						break;
					} else {
						XSSFRow rowCopied = testCaseParsedSheet
								.createRow(rowCount);
						copyRow(testRow, rowCopied);
					}
					testCase.incrementLinesPass();
				} catch (Exception e) {
					// System.out.println(""+e.getStackTrace());
					// System.out.println(e.getMessage());
					testCase.incrementLinesFail();
					testCase.setFail();

				}
				rowCount++;
			}

			// Print the result data
			/*
			 * Iterator rowIterParsed = testCaseParsedSheet.rowIterator();
			 * System.out.println("Printing the copied rows");
			 * while(rowIterParsed.hasNext()) {
			 * 
			 * XSSFRow printRow = (XSSFRow) rowIterParsed.next(); Iterator
			 * cellIterator = printRow.cellIterator();
			 * while(cellIterator.hasNext()) { XSSFCell cell = (XSSFCell)
			 * cellIterator.next(); System.out.print(cell.getStringCellValue()+
			 * " "); } System.out.println(); }
			 */
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return testCaseParsedSheet;
    }

    private void executeTest(TestCase testCase, XSSFSheet resultSheet,XSSFSheet sheetInput) throws Exception {    	
    	FileInputStream testCaseFile = new FileInputStream(testCase.getTestCaseFileName());
        XSSFWorkbook testCaseWB = new XSSFWorkbook(testCaseFile);
        XSSFSheet testCaseSheet = sheetInput;//testCaseWB.getSheet("Sheet1");
        Iterator rowIter = testCaseSheet.rowIterator();       
      
        rowIter = testCaseSheet.rowIterator();
      
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
                }else if(type.equalsIgnoreCase(Keyword.INSERT_DATE)){
                	insertDate(testRow);
                }else if(type.equalsIgnoreCase(Keyword.COMMISSION_FIXTURES)){
                	commissionFixtures(testRow);
                }else if(type.equalsIgnoreCase(Keyword.UPLOAD_CERT)){
                	handleUploadCert(testRow);
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
	
	/**
	 * Method to commission the fixtures
	 * @param testRow
	 * @throws Exception
	 */
	private void commissionFixtures(XSSFRow testRow) throws Exception
	{
		for(int i=0; i < 3 ; i++)
		{
			webDriver.findElement( By.id("bulkFixtureCommissionButton")).click();
			Thread.sleep(3000);
			HandleWindow();
			Thread.sleep(3000);
			webDriver.findElement( By.id("fcsi-ok-btn")).click();
			Thread.sleep(2000);
			HandleWindow();
			Thread.sleep(4000);			
			//try commissioning 3 times			
			try{
				webDriver.switchTo().alert();
				webDriver.switchTo().alert().dismiss();
			}catch (Exception Ex) {
				// TODO: handle exception
			}			
			if(webDriver.findElements( By.id("2") ).size() != 0 )
			{				
				System.out.println("Phase 2");
				// It has got into commissioing mode.
				webDriver.findElement( By.id("2")).click();				
				Thread.sleep(5000);
				//Add fixture class
				webDriver.findElement( By.id("addFxClass")).click();
				Thread.sleep(2000);
				WebElement element = getElement("id", "fixtureClassName");
			    String dataValue = "Fx1";
			        if (element != null && element.isEnabled()) {
			            element.clear();
			            element.sendKeys(dataValue);
			        }
				Thread.sleep(1000);
				//Add fixture class.
				webDriver.findElement( By.xpath("//div[@id='mForm']/div[8]/div[2]/input")).click();
				Thread.sleep(1000);
				
				// Commission four(4) fixtures
				for(int j = 0 ; j < 4 ; j++)
				{
				webDriver.findElement( By.id("2")).click();
				Thread.sleep(1000);
				element = getElement("id", "fixtureClass");
			    dataValue = "Fx1";
			    if (element != null && element.isEnabled()) {
			            new Select(element).selectByVisibleText(dataValue);
			    }
				Thread.sleep(1000);
				webDriver.findElement( By.id("fxcd-commission-btn")).click();
				Thread.sleep(3500);
				}
				Thread.sleep(5000);
				break;
			}				
			else
			{
				Thread.sleep(1000);
				//webDriver.switchTo().alert().accept();
				Thread.sleep(5000);
				logout();
				login();				
				//Try commissioning again
				webDriver.findElement( By.xpath("//*[@id='floor_1']/a")).click();
				Thread.sleep(2000);				
				webDriver.findElement( By.xpath("//*[@id='in']/span")).click();
				Thread.sleep(2000);				
				WebElement frame = webDriver.findElement(By.id("installFrame"));
		    	webDriver.switchTo().frame(frame);  
		    	Thread.sleep(2000);		    	
		    	webDriver.findElement( By.xpath("//*[@id='fixtures']/span")).click();
		    	Thread.sleep(2000);		    	
		    	frame = webDriver.findElement(By.id("fixturesFrame"));
		    	webDriver.switchTo().frame(frame);  
		    	Thread.sleep(2000);
		    	if(i==2) {
		    		throw new Exception();
		    	}
			}
		}
	}
	
	private void logout()
	{
		
		 String accessType = "id";
	     String accessValue = "logoutMenu";
	        WebElement element = getElement(accessType, accessValue);
	        if (element != null) {
	          element.sendKeys(Keys.ENTER);
	          element.click();
	        }
		
	}
	
	private void login() throws InterruptedException
	{
		Thread.sleep(1000);
		String accessType = "id";
		String accessValue = "userNameTextBox";
		WebElement element = getElement(accessType, accessValue);
		String dataValue = "admin";
		if (element != null && element.isEnabled()) {
			element.clear();
			element.sendKeys(dataValue);
		}
		
		Thread.sleep(1000);
		
		accessType = "id";
		accessValue = "passwordTextBox";
		element = getElement(accessType, accessValue);
		dataValue = "admin";
		if (element != null && element.isEnabled()) {
			element.clear();
			element.sendKeys(dataValue);
		}
		
		Thread.sleep(1000);
		
		accessType = "id";
		accessValue = "loginButton";
		element = getElement(accessType, accessValue);
		if (element != null) {
			element.sendKeys(Keys.ENTER);
			element.click();
		}
		
		Thread.sleep(2000);
	}
	
	private void insertDate(XSSFRow testRow) throws Exception 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yyyy");
		String date = sdf.format(new Date());

		String accessType = testRow.getCell(1).getStringCellValue();
		String accessValue = testRow.getCell(2).getStringCellValue();
		WebElement element = getElement(accessType, accessValue);
		String dataValue = date.toString();
		if (element != null && element.isEnabled()) {
			element.clear();
			element.sendKeys(dataValue);
			Thread.sleep(500);
		}				
	}
	
	private void handleUploadCert(XSSFRow testRow) {
		String accessType = testRow.getCell(1).getStringCellValue();
        String accessValue = testRow.getCell(2).getStringCellValue();
        WebElement element = getElement(accessType, accessValue);        
        
        File[] files = new File(testRow.getCell(3).getStringCellValue()).listFiles();        
        String certFile = getFile(files);
		if (certFile != null) {
			if (element != null && element.isEnabled()) {
				//element.clear();
				element.sendKeys(certFile.toString());
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String getFile(File[] files) {
	    for (File file : files) {
	        if (file.isDirectory()) {
	            //showFiles(file.listFiles()); // Calls same method again.
	        } else {
	        	return file.getAbsolutePath().toString();
	        }
	    }
	    return null;
	}
	
	private void copyRow(XSSFRow testRow,XSSFRow copyRow)
	{
		XSSFCell cell,targetCell;
		Iterator cellIterator = testRow.cellIterator();
		Integer columnIndex = 0;
		while(cellIterator.hasNext())
		{
			cell = (XSSFCell) cellIterator.next();
			targetCell = copyRow.createCell(columnIndex++);
			targetCell.setCellValue(cell.getStringCellValue());
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
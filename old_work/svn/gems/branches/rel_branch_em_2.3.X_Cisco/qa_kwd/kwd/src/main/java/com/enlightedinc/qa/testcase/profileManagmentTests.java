package com.enlightedinc.qa.testcase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;

import com.enlightedinc.qa.Model.TestSuite;
import com.enlightedinc.qa.kewywords.Keyword;
 

public class profileManagmentTests extends Executor  
{
	
    public static void Create_Profile() throws Throwable  
	{   
           Iterator rowIter2 = TestSuite.executeTestSuiteDataFile();
			while (rowIter2.hasNext()) 
		         {
		        	 XSSFRow dataRow = (XSSFRow) rowIter2.next();
		             XSSFCell typeCell = dataRow.getCell(0);
		             String value = typeCell.getStringCellValue();
	                 try {
		             	
		                 if (value!= null  )
		                 {
		                	      String profilename =value;
			                	  String message = add_profile(profilename);
			                	  if(message.equalsIgnoreCase("Profile created successfully."))
			                        {
			                       	 webDriver.findElement(By.xpath("html/body/div[13]/div[1]/a/span")).click(); // close the Popup window
			                        }
			                        else if(message.equalsIgnoreCase("Maximum 255 Profile can be created per user"))
			                        {
			                       	 webDriver.findElement(By.xpath("html/body/div[13]/div[1]/a/span")).click(); // close the Popup window
			                       	 break;
			                        }
			                        Thread.sleep(2000);
	                       }
		                 else if (value.equalsIgnoreCase(Keyword.FINISH)) {
		 	                break;
		 	            }
		             }catch (Exception e) {
		     			e.printStackTrace();
		     		}
		         }
			} 

	public static String add_profile(String profilename) throws Exception
		{
		 String name= profilename;
		 webDriver.switchTo().frame("templateFrame");
       	 Thread.sleep(2000);
       	 webDriver.findElement(By.id("addProfileBtn")).click();
       	 Thread.sleep(2000);
       	 boolean True =  webDriver.findElements(By.id("ui-dialog-title-profileFormDialog")).size() > 1;
       	 String HandlePopUp = webDriver.getWindowHandle();
         webDriver.switchTo().window(HandlePopUp);
         Thread.sleep(2000);
         webDriver.findElement(By.xpath("html/body/div[13]/div[2]/div/form/div[2]/div[1]/div/div/div[2]/div/input")).sendKeys(name);
         webDriver.findElement(By.id("newbtn")).click(); // click on Save button
         Thread.sleep(2000);
         String message =  webDriver.findElement(By.id("basic_message")).getText();
        
         return(message);
         
       }
	public static void setDBConnection() throws SQLException
    {
        try 
        {
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
        }catch(Exception e)
        {
          e.printStackTrace();
        }
    }
	
	    
 }

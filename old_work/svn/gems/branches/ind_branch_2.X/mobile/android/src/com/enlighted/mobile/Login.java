package com.enlighted.mobile;

//import android.webkit.WebView;
//import com.phonegap.DroidGap;

public class Login {
	
	
	//private WebView mAppView;
    //private DroidGap mGap;
   
    /*public Login(DroidGap gap, WebView view)
    {
        //mAppView = view;
        //mGap = gap;
    }*/
    
    
    
    public  String login(String loginXML , String url){
    	
    	String loginResponceXML = "";
    	
    	Communication scObj = new Communication(url);
    	
    	if(Globals.state == 100){
    		
    		if ( scObj.sendData(loginXML) )
    		{
    			if ( scObj.recvData() )	{
    					
    				loginResponceXML = Globals.buffer;
    				
    			} else {
    					;
    				
    				loginResponceXML = Integer.toString(Globals.state);
    			}
    		} else {
    			
    			loginResponceXML = Integer.toString(Globals.state);
    			
    		}
    		
    	}
    	else{
    		loginResponceXML = Integer.toString(Globals.state);
    	}
    	
    	return loginResponceXML;
    }
    
}

package com.enlighted.mobile;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;


public class LoginPlugin extends Plugin {

	public static final String ACTION="nativeLogin";
	
	private PluginResult result;
	
	private Login login;
	
	@Override
	public PluginResult execute(String arg0, JSONArray arg1, String arg2) {
		// TODO Auto-generated method stub
		result = null;
		String loginResponce = null;
		 if (ACTION.equals(arg0)) {
			login = new Login();
			try {
				loginResponce = login.login(arg1.getString(0), arg1.getString(1));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			result = new PluginResult(Status.OK, loginResponce);
		 }
		 else{
			 Log.v("ERROR", "somr error in loginplugin.java file") ;
			 result = new PluginResult(Status.INVALID_ACTION);
		 }
		
		return result;
	}

}

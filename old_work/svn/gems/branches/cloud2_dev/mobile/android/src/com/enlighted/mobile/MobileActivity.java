package com.enlighted.mobile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.phonegap.DroidGap;

public class MobileActivity extends DroidGap {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /*super.init();
    	
       Login enlightedLogin = new Login(this, appView);
    	
    	appView.addJavascriptInterface(enlightedLogin, "enlightedLoginJS");*/
        super.deleteDatabase("webview.db");
        super.deleteDatabase("webviewCache.db");
        super.clearCache();
        
        Globals.versionName = getVersionName();
        Globals.versionCode = getVersionCode();
        storeVersionDetails(Globals.versionName , Globals.versionCode);
        //Log.d("Version1", Globals.versionName);
        //Log.d("versionCode1", Integer.toString(Globals.versionCode) );
        
        super.loadUrl("file:///android_asset/www/index.html");
    }
    
    
    private String getVersionName() {
        String version = "??";
        try {        	
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pi.versionName;
            Log.d("Version", version);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("EnlightedInc1", "Version name not found in package", e);
        }
        return version;
    }

    private int getVersionCode() {
        int version = -1;
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pi.versionCode;
            Log.d("Version Code", Integer.toString(version));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("EnlightedInc1", "Version number not found in package", e);
        }
        return version;
    }
    
    /*private void storeVersionDetails(String versionName , int versionCode){
        String FILENAME = "version_file.txt";
        String version = versionName;

        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            fos = openFileOutput(FILENAME, Context.MODE_WORLD_WRITEABLE);
            osw = new OutputStreamWriter(fos); 
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            osw.write(version);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            fos.close();
            osw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }*/
    
    
    /*private void storeVersionDetails1(String versionName , int versionCode){
        
        FileWriter fWriter;
        try{
             fWriter = new FileWriter("/sdcard/textVersione.txt");
             fWriter.write("Sampath100");
             fWriter.flush();
             fWriter.close();
         }catch(Exception e){
                  e.printStackTrace();
         }
        
    }*/
    
    private void storeVersionDetails(String versionName , int versionCode){
       
        String completeVersion = versionName+"."+Integer.toString(versionCode);
        try {
            File myFile = new File("/sdcard/enlightedMobile.txt");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = 
                                    new OutputStreamWriter(fOut);
            myOutWriter.append(completeVersion);
            myOutWriter.close();
            fOut.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    
}
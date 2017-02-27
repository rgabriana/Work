package com.ems;


import com.ems.db.SimDBHelper;

public class SimDBCmdline {
	
	
	public static void displayUsage() {
    	System.out.println("java -jar simDBCmdline.jar <Sensor ID> [options]");
    	System.out.println("\t\t<Sensor Id> is required. in the form of 06:d4:32, leading zeros are not required.");
    	System.out.println("\t-HBtrigger[=<value>]");
    	System.out.println("\t\tHBtrigger is used to read or write the HBtrigger value to the db. When issued without =<value> the current db value is read.");
    	System.out.println("\t\tWhen =<value> is attached. The Hbtrigger value is set to the new value. This is a 1 byte value.");
    	System.out.println("\t-state[=<value>]");
    	System.out.println("\t\tstate is used to read or write the current State value to the db. When issued without =<value> the current db value is read.");
    	System.out.println("\t\tWhen =<value> is attached. The current State value is set to the new value. This is a 1 byte value.");
    	
    	System.out.println("\t\tValied values are 2:auto, and 7:manual");
    }
    
    
	
    public static void main(String[] args) {
    
    	int hbtriggerval = -1;
        int currState = -1;
    	String sensorId = null;
    
    	byte data;
    	
    	for(String arg: args) {
    		if(arg.contains("-HBtrigger")){
    			String[] argvalue = arg.toString().split("=");
    			if(argvalue.length != 2) {
    				hbtriggerval=-2;
    			}
    			else {
    				hbtriggerval = Integer.parseInt(argvalue[1]);
    				System.out.println("HBtriggerval: "+hbtriggerval);
    			}
    		}
    		if(arg.contains("-state")){
    			String[] argvalue = arg.toString().split("=");
    			if(argvalue.length != 2) {
    				currState=-2;
    			}
    			else {
    			currState = Integer.parseInt(argvalue[1]);
    			System.out.println("Type: "+currState);
    			}
    		}
    		if(arg.contains(":")){
    			String[] argvalue = arg.toString().split(":");
    			if(argvalue.length == 3) {
    				sensorId = arg.toString();
    				System.out.println("Sensor: "+sensorId);
    			} else {
    				System.out.println("Incorrect Sensor specification:" + arg.toString());
    			}
    			
    		}
    		
    		if(arg.contains("-h")) {
    			SimDBCmdline.displayUsage();
    			System.exit(0);
    		}
    	}

	
		
		   SimDBHelper test = new SimDBHelper();
	       if(sensorId == null) {
	    	   System.out.println("Sensor Id not specified");
	    	   SimDBCmdline.displayUsage();
   			   System.exit(1);
	       }
		   
	       if (hbtriggerval == -2) {
	    	   data = test.getHBTrigger(sensorId);
	    	   System.out.println("HBtrigger Value is: "+ data);
	    	   
	       } else {
	    	   if (hbtriggerval != -1) {
	    		   test.setHBTrigger(sensorId, (byte) hbtriggerval);
	    		  
	    	   }
	       }
	       
	       if (currState == -2) {
	    	   data = test.getCurrState(sensorId);
	    	   System.out.println("Current State Value is: "+ data);
	    	   
	       } else {
	    	   if (currState != -1) {
	    		   test.setHBTrigger(sensorId, (byte) currState);
	    		 
	    	   }
	       }
	       
	       System.exit(0);
	}

}

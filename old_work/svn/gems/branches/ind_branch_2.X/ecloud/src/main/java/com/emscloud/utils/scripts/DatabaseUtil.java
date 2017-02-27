package com.emscloud.utils.scripts;


public class DatabaseUtil {
	
	public static void createDatabase(String databaseName) {

        Process pr = null;
             try {
            String[] cmdArr = { "/bin/bash",  "/home/enlighted/ecloud/scripts/create_eminstance_db.sh", databaseName };
            pr = Runtime.getRuntime().exec(cmdArr);
            pr.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end of

}

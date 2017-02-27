
package com.ems.crontask.hourtask;

import com.ems.Globals;

/**
 * @author SAMEER SURJIKAR
 *	This will give you instance for the object which are implementing HourTask interface.
 */
public class GetInstance 
{
	public HourTask getInstance(String name)
	{
		HourTask  instance = null ;
		if(name.equalsIgnoreCase("SendDashBoardDetailsHourlyData"))
		{
			Globals.log.info("Getting instance of task : " + name  );
			return new SendDashBoardDetailsHourlyData() ;
		}
		return instance ;
	}
}

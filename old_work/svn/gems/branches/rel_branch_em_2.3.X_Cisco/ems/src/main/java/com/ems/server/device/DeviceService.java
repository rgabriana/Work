/**
 * 
 */
package com.ems.server.device;

/**
 * @author Sreedhar Kamishetti
 *
 */
public interface DeviceService {

  public void dimFixtures(int[] fixtureArr, int percentage, int time);
  
  public void dimFixture(int fixtureId, int percentage, int time);
    
} //end of interface DeviceService

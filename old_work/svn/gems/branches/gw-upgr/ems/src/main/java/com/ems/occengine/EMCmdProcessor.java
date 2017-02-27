/**
 * 
 */
package com.ems.occengine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.ems.occengine.utils.CommonUtils;
import com.ems.server.util.ServerUtil;

public class EMCmdProcessor implements Runnable {
	public static final Logger logger = Logger.getLogger(EMCmdProcessor.class
			.getName());
	
	private CacheManager m_cacheManager = CacheManager.getInstance();

	private int count = 0;
	private Date starttime = new Date();
	private final int countResetAt = 5000;


	@Override
	public void run() {
		byte[] emPacket = null;
		while(true) {
			try {
				emPacket = OccupancyEngine.getInstance().getQueue().take();
				System.out.println("Packet from EM: " + CommonUtils.getPacket(emPacket));
				if (emPacket != null) {
					if (emPacket.length < 11) // then unknown packet!
						continue;
					String sName = CommonUtils.getSnapAddr(emPacket[8],
							emPacket[9], emPacket[10]);
					System.out.println("sName: " + sName);
					count++;
	
					if (count == countResetAt) {
						logger.info(count
								+ " sensor heartbeat packets processed between "
								+ starttime + " and " + new Date());
						count = 0;
						starttime = new Date();
					}
					SensorEventVO oSensor = m_cacheManager.getSensor(sName);
					if (oSensor == null) {
						if(logger.isDebugEnabled()) {
							logger.debug("No Zone mapped to Sensor: " + sName);
						}
						emPacket = null;
						continue;
					}
					if(logger.isDebugEnabled()) {
						logger.debug("Packet from EM: "
							+ CommonUtils.getPacket(emPacket));
					}
					int msgType = (emPacket[11] & 0xFF);
					switch (msgType) {
						case CommandsConstants.REPT_PM_DATA_MSG_TYPE:
							processD9(oSensor, emPacket);
							break;
						default:
							break;
					}
				}
				else {
					logger.info("no packets in the queue");
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				emPacket = null;
			}
		}
	}

	private void processD9(SensorEventVO oSensor, byte[] packet) {
		if(logger.isDebugEnabled()) {
			logger.debug("Processing D9 for " + oSensor.getMac() + "::::"
				+ CommonUtils.getPacket(packet));
		}

		byte[] tempShortByteArr = new byte[2];
		byte[] tempIntByteArr = new byte[4];

		int index = CommandsConstants.RES_CMD_PKT_MSG_START_POS;
		if (packet[0] == CommandsConstants.FRAME_START_MARKER) { // old packet
			index = 3;
		}
		index++;
		
		// byte 3 is min voltage
		index++;

		// byte 4 is max voltage
		index++;

		// byte 5, 6 is avg voltage
		index += 2;

		// byte 7 is last voltage
		byte lastVolts = packet[index++];
		if (lastVolts > 100) {
			lastVolts = 100;
		}

		// bytes 8, 9 is min amb
		index += 2;

		// bytes 10, 11 is max amb
		index += 2;

		// bytes 12, 13, 14, 15 is avg amb
		System.arraycopy(packet, index, tempIntByteArr, 0,
				tempIntByteArr.length);
		int avgAmb = ServerUtil.byteArrayToInt(tempIntByteArr);
		index += 4;

		// byte 16 is min temp
		index++;

		// byte 17 is max temp
		index++;

		// byte 18, 19 is avg temp
		System.arraycopy(packet, index, tempShortByteArr, 0,
				tempShortByteArr.length);
		short avgTemp = (short) ServerUtil.byteArrayToShort(tempShortByteArr);
		index += 2;

		// bytes 20 is last temperature
		index++;

		// bytes 21, 22 is energy_calib value
		index += 2;

		// bytes 23, 24 is energy ticks
		System.arraycopy(packet, index, tempShortByteArr, 0,
				tempShortByteArr.length);
		int power = CommonUtils.extractShortFromByteArray(
				tempShortByteArr, 0);
		index += 2;

		// bytes 25, 26, 27, 28 is energy_cum
		// this need to be used to compute energy of missing buckets
		index += 4;

		// bytes 29,30 is light on sec
		index += 2;

		// bytes 31, 32 is light on to off
		index += 2;

		// bytes 33, 34 is light off to on
		index += 2;

		// bytes 35, 36 is motion off to on
		index += 2;

		// bytes 37, 38 is motion on to off
		/*System.arraycopy(packet, index, tempShortByteArr, 0,
				tempShortByteArr.length);
		short motionOnToOff = (short) ServerUtil
				.byteArrayToShort(tempShortByteArr);*/
		index += 2;

		byte[] motionBits = new byte[8];
		// bytes 39, 40, 41, 42 is motion mask1
		System.arraycopy(packet, index, tempIntByteArr, 0,
				tempIntByteArr.length);
		int motionMask1 = ServerUtil.byteArrayToInt(tempIntByteArr);
		System.arraycopy(packet, index, motionBits, 4, 4);
		index += 4;

		// bytes 43, 44, 45, 46 is motion mask2
		System.arraycopy(packet, index, tempIntByteArr, 0,
				tempIntByteArr.length);
		int motionMask2 = ServerUtil.byteArrayToInt(tempIntByteArr);
		System.arraycopy(packet, index, motionBits, 0, 4);
		index += 4;
		
		List<Integer> motionSecAgo = getLastOccupancySeconds(motionMask1, motionMask2);

		//int motionSecAgo = getLastOccupancySec(motionMask1, motionMask2);
        /*if (motionSecAgo == 300) { // no occupancy seen
        	motionSecAgo =  motionSecAgo+ 300;
        } */


		// byte 47
		// 0 - none
		// 1 - occupancy
		// 2 - ambient
		// 3 - individual(tune_up)
		// 4 - manual
		index++;

		// byte 48 is current state
		index++;

		// byte 49, 50, 51, 52 is sys up time
		index += 4;

		// byte 53, 54 is last reset reason
		index += 2;
		// byte 55, 56 is cuStatus
		index += 2;

		// byte 57, 58 is num reset by cu
		index += 2;

		// byte 59 is app
		index++;

		if (packet.length > index + 1) {
			// byte 60 is global profile checksum
			index++;
			// byte 61 scheduled profile checksum
			index++;
			// byte 62 profile group id
			index++;
		}

		if (packet.length > index + 1) {
			// byte 63, 64 groups checksum
			index += 2;
		}

		if (packet.length > index + 1) {
			// new time field
			// byte 65, 66, 67, 68 time in sec
			index += 4;

			// byte 69, 70 are duration in sec
			index += 2;
		}

		byte avgTempPrecision =0;
		if (packet.length > index + 1) {
			// 71 The tenths precision of the current
			avgTempPrecision = packet[index++];
		}


		if (packet.length > index + 1) {
			// 72,73 The ambient calib value uint16_t ambient_calib; // The
			// current ambient calibration level.
		}
		index += 2;

		if (packet.length > index + 1) {
			index += 1; // groups
			index++;
		}
		int rem = lastVolts % 5;
		if (rem > 2) {
			lastVolts += (5 - rem);
		} else {
			lastVolts -= rem;
		}

		
		
		SensorProcessor.updateMotionSecAgo_5Mnt(oSensor, motionSecAgo);
		oSensor.updateLastPMStatCommunication();
		if(oSensor.getLastUpdated() == null || oSensor.getLastRealTimeStateChange() == null || oSensor.getLastUpdated().after(oSensor.getLastRealTimeStateChange())) {
			oSensor.setFxTemp((short) (((new Float(avgTemp + "." + avgTempPrecision)) * 9 / 5) + 32 - 8));
			oSensor.setAmbLight((short)avgAmb);
			oSensor.setCurrVolt((int) lastVolts);
			oSensor.setPower(power);
		}

	}
	
	
	/*
	 * mask1 is 1st word, mask2 is 2nd word mask2 represents most recent word in
	 * the 5 minute interval 300 seconds are represented by 60 bits of which 32
	 * bits are in mask1, 28 bits are in mask2 remaining 4 most significant bits
	 * of word 2 are ignored in a word, more recent 5 second bits are msb. for
	 * ease of calculation, bits are numbered 0 through 31 from msb instead of
	 * lsb first mask2 is looked into to find the most recent occupancy from 4th
	 * bit and then mask1 is looked into if occupancy is not observed in mask2
	 * from msb
	 */
	public List<Integer> getLastOccupancySeconds(int mask1, int mask2) {

		List<Integer> lastSeen = new ArrayList<Integer>();
		byte[] mask2ByteArray = ServerUtil.intToByteArray(mask2);
		int i = 4;
		boolean ifBreak = false;
		for (i = 4; i < 24; i++) {
			if (getBit(mask2ByteArray, i) == 1) {
				ifBreak = true;
				lastSeen.add(5*(i-3));
			}
		}
		if(!ifBreak) {
			lastSeen.add(101);
		}
		ifBreak = false;
		
		for (i = 24; i < 32; i++) {
			if (getBit(mask2ByteArray, i) == 1) {
				ifBreak = true;
				lastSeen.add(5*(i - 3 - 24));
			}
		}
		byte[] maskByteArray = ServerUtil.intToByteArray(mask1);
		for (i = 0; i < 12; i++) {
			if (!ifBreak && getBit(maskByteArray, i) == 1) {
				ifBreak = true;
				lastSeen.add(5 * (i + 5));
			}
		}
		if(!ifBreak) {
			lastSeen.add(101);
		}
		ifBreak = false;
		
		for (i = 12; i < 32; i++) {
			if (getBit(maskByteArray, i) == 1) {
				ifBreak = true;
				lastSeen.add(5 * (i - 19));
			}
		}
		if(!ifBreak) {
			lastSeen.add(101);
		}

		return lastSeen;

	} // end of method getLastOccupancySec

	private int getBit(byte[] data, int pos) {
		int posByte = pos / 8;
		int posBit = pos % 8;
		byte valByte = data[posByte];
		int valInt = valByte >> (8 - (posBit + 1)) & 0x0001;
		return valInt;
	}

}

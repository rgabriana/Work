package com.ems.server.upgrade;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.ems.server.util.ServerUtil;

public class GWFirmware {
	private static final Logger logger = Logger.getLogger("ImageUpgrade");

	public byte[] fileArray;
	public String fileName;
	public String filePath;
	public int fileSize;
	public int targetBuildNo;
	public byte[] standardUpgPkt;
	public byte[] newUpgPkt;
	public byte[] xferInitPkt;

	/**
	 * Creates the upgrade packet, which only contains the absolute or relative path of
	 * the new firmware. If the xfer_init has failed then we will send the absolute path
	 * and the gw will do an scp. If the xfer has succeeded, we will send the relative path
	 * to trigger the update with the already uploaded file.
	 */
	public byte[] createUpgradePkt(boolean isShort) {
		byte[] dataPkt;
		if (isShort) {
			dataPkt = fileName.getBytes();
		}
		else {
			dataPkt = filePath.getBytes();
		}
		// md5sum + 2 bytes for storing path length + actual file path
		byte[] upgradePkt = new byte[20 + 2 + dataPkt.length];
		short pathLength = (short)dataPkt.length;
		byte[] pathLengthArr = ServerUtil.shortToByteArray(pathLength);

		byte[] fileSHA1sumArr = ServerUtil.createSHA1Checksum(filePath);
		if (fileSHA1sumArr == null) {
			// Should not happen.
			logger.error("Could not compute SHA1 sum for file " + filePath + ". Upgrade will fail.");
			fileSHA1sumArr = new byte[20];
			Arrays.fill(fileSHA1sumArr, (byte) '0');
		}
		System.arraycopy(fileSHA1sumArr, 0, upgradePkt, 0, fileSHA1sumArr.length);
		System.arraycopy(pathLengthArr, 0, upgradePkt, fileSHA1sumArr.length, pathLengthArr.length);
		System.arraycopy(dataPkt, 0, upgradePkt, fileSHA1sumArr.length + pathLengthArr.length, dataPkt.length);
		return upgradePkt;
	}

	/**
	 * Creates a xfer_init packet, which only contains the filename.
	 * @param fileName
	 * @return
	 */
	public byte[] createXferInitPkt() {
		byte[] dataPkt = new File(fileName).getName().getBytes();
		short pathLength = (short) dataPkt.length;
		byte[] pathLengthArr = ServerUtil.shortToByteArray(pathLength);

		// Path len + path
		byte[] upgradePkt = new byte[2 + dataPkt.length];

		System.arraycopy(pathLengthArr, 0, upgradePkt, 0, 2);
		System.arraycopy(dataPkt, 0, upgradePkt, 2, dataPkt.length);

		return upgradePkt;
	}

	public GWFirmware(String filePath) throws IOException {
		this.filePath = filePath;
		File f = new File(filePath);
		this.fileName = f.getName();
		this.fileArray = ImageUpgradeSO.getBytesFromFile(f);
		this.fileSize = this.fileArray.length;
		this.targetBuildNo = Integer.parseInt(fileName.substring(0, 
				fileName.indexOf("_")));
		
		logger.info("Parsed the firmware " + this.fileName + ": " + this.fileSize + "bytes");

		// Creates the xfer init packet		
		this.xferInitPkt = createXferInitPkt();

		// Creates the old style upgrade packet
		this.standardUpgPkt = createUpgradePkt(false);
		
		// Creates the new style upgrade packet
		this.newUpgPkt = createUpgradePkt(true);
	}
}
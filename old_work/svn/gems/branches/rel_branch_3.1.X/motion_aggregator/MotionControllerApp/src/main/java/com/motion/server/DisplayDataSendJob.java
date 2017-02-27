package com.motion.server;

import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.motion.dao.DisplayDataDao;
import com.motion.dao.FixtureDao;
import com.motion.dao.MotionPacketDao;
import com.motion.utils.CommonUtils;
import com.motion.utils.MotionAppConstants;
import com.motion.utils.SpringContext;
import com.motion.vo.BlobVO;
import com.motion.vo.DrawVO;
import com.motion.vo.LineVO;

public class DisplayDataSendJob implements Job {

	public static final Logger logger = Logger
			.getLogger(DisplayDataSendJob.class.getName());
	private MotionPacketDao motionPacketDao = null;
	private FixtureDao fixtureDao = null;
	private DisplayDataDao displayDataDao = null;

	public void DisplayDataSendJob() {

	}

	private void startSending() {
		motionPacketDao = (MotionPacketDao) SpringContext
				.getBean("motionPacketDao");
		fixtureDao = (FixtureDao) SpringContext.getBean("fixtureDao");
		displayDataDao = (DisplayDataDao) SpringContext
				.getBean("displayDataDao");
		try {

			DatagramSocket dsocket = new DatagramSocket();
			InetAddress address = InetAddress
					.getByName(MotionAppConstants.REMOTE_HOST);
			String composedMessage = null;
			byte[] message = null;
			logger.info("Display data Sending job to IP "
					+ MotionAppConstants.REMOTE_HOST + ". On Port "
					+ MotionAppConstants.REMOTE_PORT + " Started....");
			while (true) {
				composedMessage = getLatestDisplayData();

				if (composedMessage != null) {
					logger.debug("Message sent over udp :- " + composedMessage);
					message = composedMessage.getBytes();
					DatagramPacket packet = new DatagramPacket(message,
							message.length, address,
							MotionAppConstants.REMOTE_PORT);
					dsocket.send(packet);
					displayDataDao.saveDisplayData(composedMessage);
				}
				composedMessage = null;
				message = null;
				Thread.sleep(Integer
						.parseInt(MotionAppConstants.DATA_SEND_DELAY));
			}

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}

	}

	private String getLatestDisplayData() {
		String message = null;
		try {
			if (!MotionAppConstants.displayDataSendQueue.isEmpty()) {
				// create a map<blobl_id , <array List of its respective packets
				// in that time span>>
				ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
				HashMap<String, ArrayList<HashMap<String, String>>> indiviualBlobMaps = new HashMap<String, ArrayList<HashMap<String, String>>>();
				MotionAppConstants.displayDataSendQueue.drainTo(temp);
				Iterator<HashMap<String, String>> itr = temp.iterator();
				while (itr.hasNext()) {
					HashMap<String, String> temp1 = itr.next();
					// insert blob color if blob not present at first.
					if (!MotionAppConstants.blobColorMap.containsKey(temp1
							.get("blob_id"))) {
						MotionAppConstants.blobColorMap.put(
								temp1.get("blob_id"),
								CommonUtils.randomColorNameGenerator());
					}
					ArrayList<HashMap<String, String>> temp2 = null;
					// insert new value in the indiviualBlobMaps only if it is
					// not present otherwise update the array list.
					if (indiviualBlobMaps.containsKey(temp1.get("blob_id"))) {
						temp2 = indiviualBlobMaps.get(temp1.get("blob_id"));
						temp2.add(temp1);
						indiviualBlobMaps.put(temp1.get("blob_id"), temp2);
					} else {
						temp2 = new ArrayList<HashMap<String, String>>();
						temp2.add(temp1);
						indiviualBlobMaps.put(temp1.get("blob_id"), temp2);
					}
				}				
				applyBlobUnificationAlgo(indiviualBlobMaps);	
				message = getXMLStringDisplayData(indiviualBlobMaps);

			} else {
				// logger.debug("Display data queue is empty");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
		return message;
	}

	/*
	 * This method unify the blob if it is from different sensors and distance
	 * between them is less than scaling factor defined in the config.properties
	 * file. Algo is :- step 1 - Take all the blob from the display queue step 2
	 * - Compute distance between each blob for each sensor. ( This can later
	 * change to only those sensor who are adjacent to each other) step 3 -
	 * check if the distance is less than the bloblUnifactionFactor defined in
	 * config.properties file. step 4 - if yes - Find the average between two. -
	 * update each blob with new co-ordinate. if no do nothing.
	 */
	private void applyBlobUnificationAlgo(
			HashMap<String, ArrayList<HashMap<String, String>>> indiviualBlobMaps) {

		logger.debug("Started Applying Blob Unification Algo to the blobs set.");
		long startTime = System.currentTimeMillis();
		Iterator<String> keys = indiviualBlobMaps.keySet().iterator();
		int count = 1;
		while (keys.hasNext()) {

			Iterator<String> key = indiviualBlobMaps.keySet().iterator();
			key.next() ;
			ArrayList<HashMap<String, String>> fromBlobs = indiviualBlobMaps.get(keys.next());
			for (int i = count; i < indiviualBlobMaps.keySet().size(); i++) {	
				ArrayList<HashMap<String, String>> toBlobs = indiviualBlobMaps.get(key.next());
				for (int j = 0; j < fromBlobs.size(); j++) {
					for (int k = 0; k < toBlobs.size(); k++) {
						Long distance = eucludianDistanceFinder(fromBlobs.get(j).get("global_x"),fromBlobs.get(j).get("global_y"), toBlobs.get(k).get("global_x"), toBlobs.get(k).get("global_y"));				
						if (distance.longValue() < Long.parseLong(MotionAppConstants.BLOB_UNIFICATION_FACTOR)) 
						{
							logger.debug("Blog ids "
									+ fromBlobs.get(j).get("blob_id") + " and "
									+ toBlobs.get(k).get("blob_id")
									+ "  are  getting unified");
							
							String newGlobalX = avg(fromBlobs.get(j).get("global_x"), toBlobs.get(k).get("global_x"));
							String newGlobalY = avg(fromBlobs.get(j).get("global_y"), toBlobs.get(k).get("global_y"));
							logger.debug("Blob Id " +fromBlobs.get(j).get("blob_id")
									+ " original co-ordinates were ("
									+ fromBlobs.get(j).get("global_x") + " , "
									+ fromBlobs.get(j).get("global_x")
									+ ") changed to (" + newGlobalX + ","
									+ newGlobalY + ")");
							logger.debug("Blob Id "+ toBlobs.get(k).get("blob_id")
									+ " original co-ordinates were ("
									+ toBlobs.get(k).get("global_x") + " , "
									+ toBlobs.get(k).get("global_x")
									+ ") changed to (" + newGlobalX + ","
									+ newGlobalY + ")");
							fromBlobs.get(j).put("global_x", newGlobalX);
							fromBlobs.get(j).put("global_y", newGlobalY);
							toBlobs.get(k).put("global_x", newGlobalX);
							toBlobs.get(k).put("global_y", newGlobalY);

						}
					}
				}
			}
			count++;
		}
		logger.debug("Time Taken for blobl unification Algo = "
				+ (System.currentTimeMillis() - startTime) + " ms. ");

	}

	private String avg(String x1, String x2) {

		Double avg = (Integer.parseInt(x1) + Integer.parseInt(x2)) / 2.0;
		return String.valueOf(avg.longValue());
	}

	private Long eucludianDistanceFinder(String x1, String y1, String x2,
			String y2) {
		Double a1 = Math.pow(Double.parseDouble(x2) - Double.parseDouble(x1), 2);
		Double a2 = Math.pow(Double.parseDouble(y2) - Double.parseDouble(y1), 2) ;
		Double d3 = a1 +a2 ;
		Double result =	Math.sqrt(d3) ;
		return result.longValue();
	}

	private String getXMLStringDisplayData(
			HashMap<String, ArrayList<HashMap<String, String>>> indiviualBlobMaps) {
		ArrayList<BlobVO> blobs = new ArrayList<BlobVO>();
		ArrayList<LineVO> lines = new ArrayList<LineVO>();
		Iterator<String> itr = indiviualBlobMaps.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			if (indiviualBlobMaps.get(key).size() == 1) {
				HashMap<String, String> temp = indiviualBlobMaps.get(key)
						.get(0);
				BlobVO b = new BlobVO();
				b.setName(temp.get("blob_id"));
				b.setColor(MotionAppConstants.blobColorMap.get(temp
						.get("blob_id")));
				b.setRadius(MotionAppConstants.BLOB_RADIUS);
				b.setxPos(temp.get("global_x"));
				b.setyPos(temp.get("global_y"));
				blobs.add(b);
			} else if (indiviualBlobMaps.get(key).size() > 1) {
				Iterator<HashMap<String, String>> lineitr = indiviualBlobMaps
						.get(key).iterator();
				HashMap<String, String> prev = lineitr.next();
				HashMap<String, String> current = null;
				while (lineitr.hasNext()) {
					current = lineitr.next();
					LineVO line = new LineVO();
					line.setName(prev.get("blob_id"));
					line.setColor(MotionAppConstants.blobColorMap.get(prev
							.get("blob_id")));
					line.setPattern(MotionAppConstants.LINE_PATTERN);
					line.setWidth(MotionAppConstants.LINE_WIDTH);
					line.setxPosStart(prev.get("global_x"));
					line.setyPosStart(prev.get("global_y"));
					line.setxPosEnd(current.get("global_x"));
					line.setyPosEnd(current.get("global_y"));
					lines.add(line);
					prev = current;
				}
			}
		}
		DrawVO draw = new DrawVO();
		draw.setBlobVO(blobs);
		draw.setLineVO(lines);
		String result = null;
		StringWriter sw = new StringWriter();
		JAXBContext drawContext;
		try {
			drawContext = JAXBContext.newInstance(DrawVO.class);
			Marshaller drawMarshaller = drawContext.createMarshaller();
			drawMarshaller.marshal(draw, sw);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		result = sw.toString();
		return result;

	}

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		startSending();
	}

}

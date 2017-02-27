/**
 * 
 */
package com.ems.server.kafka.client.consumer;

import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.logging.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ems.server.device.ZigbeeDeviceImpl;
import com.ems.server.util.ServerUtil;

/**
 * The <code>MessageDispatcherRunner</code> class provides {@link Runnable} workers that are used to service the
 * PriorityBlockingQueue of incoming kafka messages. It publishes the messages on the websocket associated with the 
 * gateway associated with the KafkaMessage
 * @author Shyam
 *
 */
public class TextMessageDispatcherRunner implements Runnable {

	/***
	 * The PriorityBlockingQueue that holds all incoming messages from all KafkaConsumers
	 */
	private final PriorityBlockingQueue<ConsumerRecords<String, String>> queue;
	/***
	 * Threadsafe boolean to track whether this worker is getting closed
	 */
	private final AtomicBoolean closed = new AtomicBoolean(false);
	/***
	 * Logger
	 */
	//private static final Logger logger = LogManager.getFormatterLogger(MessageDispatcherRunner.class);
	private static Logger logger = Logger.getLogger("FixtureLogger");

	/****
	 * Instantiates a <code>MessageDispatcherRunner</code> object with the PriorityQueue containing the inbound kafka messages
	 * @param queue
	 */
	public TextMessageDispatcherRunner(PriorityBlockingQueue<ConsumerRecords<String, String>> queue) {
		this.queue = queue;
	}

	/***
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		while (!closed.get()) {
			try {
				Thread.sleep(10); //Since there is no IO on this thread we need to sleep or it will kill the CPU
				ConsumerRecords<String, String> consumerRecords = queue.take();
				Iterator<ConsumerRecord<String, String>> conRecIter = consumerRecords.iterator();
				while(conRecIter.hasNext()) {
					ConsumerRecord<String, String> consumerRecord = conRecIter.next();
					logger.debug("Text Message received for GW " + consumerRecord.topic() + " " 
							+ consumerRecord.value() + " " + consumerRecord.offset()	);
					String gwMac = extractGatewayMac(consumerRecord.topic());
					//@TODO enable/disable to send the gateway messages based on the text message
				}
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
	}

	private String extractGatewayMac(String topic) {
		
		String gwId = extractGatewayId(topic);
		String gwMac = gwId.replaceAll("..(?!$)",  "$0:");
		return gwMac;
		
	}

	/***
	 * Extract the gatewayId from the topic
	 * @param string representing the topic
	 * @return the extracted gatewayId
	 */
	private String extractGatewayId(String topic) {
		return topic.split("-")[0];
	}

	/***
	 * Shuts down this worker
	 */
	public void shutdown() {
		closed.set(true);
	}

}

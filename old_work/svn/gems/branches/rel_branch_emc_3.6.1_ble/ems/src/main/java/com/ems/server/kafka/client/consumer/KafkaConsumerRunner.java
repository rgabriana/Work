/**
 * 
 */
package com.ems.server.kafka.client.consumer;

import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.log4j.Logger;

/**
 * The <code>KafkaConsumerRunner</code> class provides a {@link Runnable} wrapper around the KafkaConsumer.
 * It allows to add and remove new topics to this KafkaConsumer.
 * It consumes inbound messages and pushes them a PriortiyBlockingQueue for consumption by other worker threads
 * @author Shyam
 *
 */
public class KafkaConsumerRunner implements Runnable {
	/***
	 * Threadsafe boolean to track whether this consumer is getting closed.
	 */
	private final AtomicBoolean closed = new AtomicBoolean(false);
	/***
	 * Threadsafe boolean to track whether topics have been added to or removed from the consumer
	 */
	private final AtomicBoolean newTopics = new AtomicBoolean(false);
	/****
	 * Holds the {@link KafkaConsumer}
	 */
	private final KafkaConsumer<String, byte[]> consumer;
	/***
	 * Holds the topics that are being serviced by this KafkaConsumer
	 */
	private final List<String> topics;
	/***
	 * Lock used to synchronize around the topics list
	 */
	private final String LOCK = "LOCK";
	/***
	 * The PriorityBlockingQueue that is used to hold incoming messages
	 */
	private final PriorityBlockingQueue<ConsumerRecords<String, byte[]>> messageDispatcher;
	/***
	 * Logger
	 */
	private static Logger logger = Logger.getLogger("KafkaLogger");


	/****
	 * Instantiates a <code>KafkaConsumerRunner</code> with the passed KafkaConsumer and PriorityBlockingQueue and initial List of topics
	 * @param the KafkaConsumer
	 * @param the PriorityBlockingQueue
	 * @param the initial list of toics
	 */
	public KafkaConsumerRunner(KafkaConsumer<String, byte[]> consumer, PriorityBlockingQueue<ConsumerRecords<String, byte[]>> messageDispatcher, List<String> topics) {
		this.consumer = consumer;
		this.topics = topics;
		this.messageDispatcher = messageDispatcher;
	}

	/****
	 * {@inheritDoc}
	 */
	public void run() {
		try {
			consumer.subscribe(topics);
			while (!closed.get()) {
				if(newTopics.get()) {
					consumer.subscribe(topics); //New topic added to this runner. Resubscribing
					newTopics.set(false);
				}
				// Handle new records
				ConsumerRecords<String, byte[]> records = consumer.poll(10000);
				messageDispatcher.put(records);
			}
		} catch (WakeupException e) {
			if (!closed.get()) {
				logger.error("", e); // Ignore exception if closing
			}
		} catch (Exception e) {
			logger.error("",e); //Catch all exception. Shouldn't happen but put to make sure that the thread doesn't die without logging incase of an unexpected exception
		} finally {
			consumer.close();
		}
	}

	/****
	 * Adds a new topic to the Consumer in a threadsafe manner
	 * @param the topic to be added
	 */
	public void addTopic(String topic) {
		synchronized (LOCK) {
			logger.debug("entering addTopic " + topic);
			topics.add(topic);
			newTopics.set(true);
			logger.debug("exiting addTopic " + topic);
		}
	}

	/****
	 * Removes a to the Consumer in a threadsafe manner
	 * @param the topic to be removed
	 */
	public void removeTopic(String topic) {
		synchronized (LOCK) {
			logger.debug("entering removeTopic " + topic);
			topics.remove(topic);
			newTopics.set(true);
			logger.debug("exiting removeTopic " + topic);
		}
	}

	/****
	 * Shutdown the KafkaConsumer held by this object
	 */
	public void shutdown() {
		logger.info("Shutting down kafkaconsumer. {}" + this.consumer);
		closed.set(true);
		consumer.wakeup();
	}
}

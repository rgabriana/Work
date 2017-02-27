/**
 * 
 */
package com.ems.server.kafka.client;

import org.apache.logging.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ems.server.kafka.client.consumer.KafkaConsumerManager;
import com.ems.server.kafka.client.producer.KafkaProducerManager;
import com.ems.server.kafka.config.KafkaConfig;

/**
 * The <code>KafkaCluster</code> class provides context associated with a single KafkaCluster.
 * It holds the Config and the Producers and Consumers associated with this KafkaCluster.
 * @author Shyam
 *
 */
public class KafkaCluster {

	/****
	 * The {@link KafkaConfig} associated with this cluster
	 */
	private final KafkaConfig config;
	/****
	 * The {@link KafkaProducerManager} associated with this cluster
	 */
	private final KafkaProducerManager producerManager;
	/****
	 * The {@link KafkaConsumerManager} associated with this cluster
	 */
	private final KafkaConsumerManager consumerManager;
	/***
	 * Logger
	 */
	//private static final Logger logger = LogManager.getFormatterLogger(KafkaCluster.class);
	private static Logger logger = Logger.getLogger("KafkaLogger");


	/****
	 * Instantiates a new <code>KafkaCluster</code> object using the KafkaConfig, KadkaProducerManager and KafkaConsumerManager
	 * that have been passed
	 * @param the {@link KafkaConfig} associated with this cluster
	 * @param the {@link KafkaProducerManager} associated with this cluster
	 * @param the {@link KafkaConsumerManager} associated with this cluster
	 */
	public KafkaCluster(KafkaConfig config,
			KafkaProducerManager producerManager, KafkaConsumerManager consumerManager) {
		logger.debug("entering KafkaCluster");
		this.config = config;
		this.producerManager = producerManager;
		this.consumerManager = consumerManager;
		logger.debug("exiting KafkaCluster");
	}

	/***
	 * Gets the KafkaConfig associated with this cluster
	 * @return  {@link KafkaConfig} associated with this cluster
	 */
	public KafkaConfig getConfig() {
		return config;
	}

	/***
	 * Gets the KafkaConfig associated with this cluster
	 * @return  {@link KafkaProducerManager} associated with this cluster
	 */
	public KafkaProducerManager getProducerManager() {
		return producerManager;
	}

	/***
	 * Gets the KafkaConfig associated with this cluster
	 * @return  {@link KafkaConsumerManager} associated with this cluster
	 */
	public KafkaConsumerManager getConsumerManager() {
		return consumerManager;
	}
	
	/****
	 * Shutdown this KafkaCluster. All the producer and consumer connections and associated threads 
	 * to the kafka cluster are closed via the KafkaConsumerManager and KafkaProducerManager
	 */
	public void shutdown(){
		this.consumerManager.shutdown();
		this.producerManager.shutdown();
	}
}


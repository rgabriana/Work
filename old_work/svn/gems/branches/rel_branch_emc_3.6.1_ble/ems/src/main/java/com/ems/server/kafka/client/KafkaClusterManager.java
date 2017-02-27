/**
 * 
 */
package com.ems.server.kafka.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ems.server.ServerMain;
import com.ems.server.kafka.client.consumer.KafkaConsumerManager;
import com.ems.server.kafka.client.producer.KafkaProducerManager;
import com.ems.server.kafka.config.KafkaConfig;
import com.ems.server.kafka.config.KafkaConfigException;
import com.ems.util.Constants;

/**
 * The <code>KafkaClusterManager</code> provides context over all the <code>KafkaCluster's </code>being serviced by 
 * this kafka proxy. It provides an interface that allows the initialization and shutdown of all the KafkaClusters
 * and the ability to fetch a KafkaCluster that is associated with a gateway
 */
public class KafkaClusterManager {

	/***
	 * List holding all the {@link KafkaCluster}'s being serviced by this Kafka Proxy
	 */
	private static final List<KafkaCluster> clusters = new ArrayList<KafkaCluster>(2);
	/***
	 * Map holding the association between gateway's and Kafka clusters. This needs to be prepopulated
	 * using either a config file or DB entries. Currently only a single Cluster is configured for all gateways
	 */
	private static final Map<String, KafkaCluster> gatewayToClusterMap = new ConcurrentHashMap<String, KafkaCluster>();
	/***
	 * Property file holding the single cluster's properties.//TODO This will go away as we will need a way to get KafkaConfig per cluster
	 * Mostly from the DB
	 */
	private static final String propertyFile = Constants.ENL_APP_HOME+"/Enlighted/kafka-proxy.properties";
	/***
	 * Logger
	 */
	//private static final Logger logger = LogManager.getFormatterLogger(KafkaClusterManager.class);
	private static Logger logger = Logger.getLogger("KafkaLogger");

	/****
	 * Initializes all the KafkaClusters. Currently it is only doing 1 but this will change once we read this info from the DB
	 */
	public static void init() {
		//TODO: Load all Clusters from a prop file or DB
		if(logger.isDebugEnabled()) {
			logger.debug("enering init");
		}
		try {
			KafkaConfig config = new KafkaConfig(propertyFile, ServerMain.getInstance().getKafkaServer(), 
					ServerMain.getInstance().getKafkaServerPort());
			if(logger.isDebugEnabled()) {
				logger.debug("kafka server " + config.getProducerProperties().get("bootstrap.servers"));
			}
			KafkaCluster cluster = new KafkaCluster(config, new KafkaProducerManager(config), new KafkaConsumerManager(config));
			gatewayToClusterMap.put("", cluster);
			clusters.add(cluster);
		} catch (KafkaConfigException e) {
			logger.error(e.getMessage());
		}
		if(logger.isDebugEnabled()) {
			logger.debug("exiting init");
		}
	}
	
	/***
	 * Shutdown all the KafkaCluster
	 */
	public static void shutdown() {
		
		Iterator<KafkaCluster> clusterIter = clusters.iterator();
		while(clusterIter.hasNext()) {
			clusterIter.next().shutdown();
		}
		
	}

	/***
	 * Gets the KafkaCluster associated with the passed gatewayid
	 * @param gatewayId
	 * @return The KafkaCluster associated with the passed gateway id
	 * @throws WebsocketConnectionValidationException if no matching Cluster is found and we have more than 1 cluster
	 */
	public static KafkaCluster getCluster(String gatewayId) throws KafkaConfigException {
		KafkaCluster cluster = gatewayToClusterMap.get(gatewayId);
		if(cluster == null) {
			if(clusters.size() == 1) {
				if(logger.isDebugEnabled()) {
					logger.debug("Only 1 Kafka Cluster Configured"); 
				}
				cluster = clusters.get(0);
			}
			else {
				throw new KafkaConfigException("No Kafka cluster mapped to gateway "+gatewayId);
			}
		}
		return cluster;
	}

}

/**
 * 
 */
package com.ems.server.kafka.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.model.SystemConfiguration;
import com.ems.server.ServerConstants;
import com.ems.service.SystemConfigurationManager;

/***
 * The <code>KafkaConfig</code> defines all the config needed to run this webapp. 
 * This includes KafkaProducer and KafkaConsumer config that is needed by the clients.
 * It also includes webapp specific config used to specify the size of certain connection pools.
 * @author Shyam
 */
public class KafkaConfig {

	/***
	 * Hold the properties object that gets loaded from the file
	 */
	private Properties originalProperties;
	/***
	 * List that will hold the name of the properties needed for the consumer
	 */
	private final List<String> consumerPropertyNames = new ArrayList<String>();
	/***
	 * List that will hold the name of the properties needed for the producer
	 */
	private final List<String> producerPropertyNames = new ArrayList<String>();
	
	private static Logger logger = Logger.getLogger("KafkaLogger");

	/***
	 * Instantiates the <code>KafkaConfig</code> object from the property file that is passed as parameter
	 * If no file is passed then default values are used.
	 * @param path to the property file
	 * @throws KafkaConfigException
	 */
	public KafkaConfig(String propsFile, String network) throws KafkaConfigException {
		this(getPropsFromFile(propsFile), network);
	}

	/***
	 * Instaniates the <code>KafkaConfig</code> object from the <code>Properties</code> object being passed.
	 * @param properties object
	 * @throws KafkaConfigException
	 */
	private KafkaConfig(Properties props, String network) throws KafkaConfigException {
		this.originalProperties = props;
		//bootstrap.servers need to be retrieved from database. so, overwriting the file property with db
		//kafka server
		SystemConfigurationManager sysMgr = (SystemConfigurationManager) SpringContext.getBean("systemConfigurationManager");
		SystemConfiguration kafkaConfig = null;
		if(network.equals(ServerConstants.KAFKA_GW_NETWORK)) {
			kafkaConfig = sysMgr.loadConfigByName("kafka.bootstrap.servers");
			if (kafkaConfig != null && kafkaConfig.getValue().length() > 0) {
				originalProperties.put("bootstrap.servers", kafkaConfig.getValue());
			}
		} else if(network.equals(ServerConstants.KAFKA_BLE_NETWORK)) {
			kafkaConfig = sysMgr.loadConfigByName("ble.forwarding.server");
			if (kafkaConfig != null && kafkaConfig.getValue().length() > 0) {
				originalProperties.put("bootstrap.servers", kafkaConfig.getValue());
			}
		}
    kafkaConfig = sysMgr.loadConfigByName("kafka.dispatcher.threads");
    if (kafkaConfig != null) {
    	originalProperties.put("dispatcher.pool", kafkaConfig.getValue());
    }
    
		if(logger.isDebugEnabled()) {
			logger.debug("kafka server -- " + originalProperties.getProperty("bootstrap.servers"));
		}
		populateProducerPropertyNames();
		populateConsumerPropertyNames();
	}

	/***
	 * Populates the list of property names that are required for the producer
	 */
	private void populateProducerPropertyNames() {
		producerPropertyNames.add("bootstrap.servers");
		producerPropertyNames.add("acks");  
		producerPropertyNames.add("retries");  
		producerPropertyNames.add("batch.size");
		producerPropertyNames.add("linger.ms");
		producerPropertyNames.add("buffer.memory");
	}

	/***
	 * Populates the list of property names that are required for the consumer
	 */
	private void populateConsumerPropertyNames() {
		consumerPropertyNames.add("bootstrap.servers");
		consumerPropertyNames.add("group.id");  
		consumerPropertyNames.add("consumer.pool");  
		consumerPropertyNames.add("dispatcher.pool");
		consumerPropertyNames.add("enable.auto.commit");
		consumerPropertyNames.add("auto.commit.interval.ms");
		consumerPropertyNames.add("session.timeout.ms");
		consumerPropertyNames.add("key.deserializer");
		consumerPropertyNames.add("value.deserializer");
	}

	/***
	 * Creates a <code>Properties</code> object from the passed property file. 
	 * Creates a <code>Properties</code> object with default value if no property file is passed
	 * @param the path to the property file
	 * @return<code>Properties</code> object
	 * @throws KafkaConfigException
	 */
	private static Properties getPropsFromFile(String propsFile) throws KafkaConfigException {
		Properties props = new Properties();
		if (propsFile == null) {
			generateDefaultProperties(props);
			return props;
		}
		try {
			FileInputStream propStream = new FileInputStream(propsFile);
			props.load(propStream);
		} catch (IOException e) {
			throw new KafkaConfigException("Couldn't load properties from " + propsFile, e);
		}
		return props;
	}

	/***
	 * Creates a <code>Properties</code> object with default values
	 * @param the properties object
	 */
	private static void generateDefaultProperties(Properties props) {
		props.put("bootstrap.servers", "localhost:9092");
		props.put("acks", "all");
		props.put("retries", "0");
		props.put("batch.size", "16384");
		props.put("linger.ms", "1");
		props.put("buffer.memory", "33554432");
		props.put("group.id", "test");
		props.put("consumer.pool", "1");
		props.put("dispatcher.pool", "1");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
	}

	/***
	 * Returns a map containing properties needed by the <code>KafkaProducerManager</code> class
	 * @return map containing producer related properties
	 */
	public Map<String,Object> getProducerProperties() {
		return getPropertiesMap(producerPropertyNames);
	}

	
	/***
	 * Returns a map containing property name and property values fetched from the <code>Properties</code> object
	 * based on the List of propertyNames passed as a param
	 * @param the list of propertynames
	 * @return map containing property name and property values
	 */
	private Map<String, Object> getPropertiesMap(List<String> propertyNames) {
		Map<String, Object> config = new HashMap<String, Object>();
		Iterator<String> nameIter = propertyNames.iterator();
		while(nameIter.hasNext()) {
			String key = nameIter.next();
			if(originalProperties.containsKey(key)) {
				config.put(key, originalProperties.get(key));
			}
		}
		System.out.println(config.get("bootstrap.servers"));
		return config;
	}


	/***
	 * Returns a map containing properties needed by the <code>KafkaConsumerManager</code> class
	 * @return map containing consumer related properties
	 */
	public Map<String, Object> getConsumerProperties() {
		return getPropertiesMap(consumerPropertyNames);
	}

}

/**
 * 
 */
package com.ems.server.kafka.client.producer;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.Logger;

import com.ems.server.kafka.config.KafkaConfig;

/**
 * The <code>KafkaProducerManager</code> provides a simple threadsafe interface that is used by the websocket layer
 * to produce kafka messages
 * @author Shyam
 */
public class KafkaProducerManager {

	/***
	 * Holds a binary KafkaProducer
	 * NOTE: We are creating a single producer as opposed to a pool. This is as per the recommendation 
	 * of the Kafka-Client library that indicates that it is more efficient to share a single producer across threads as it allows batching.
	 * NOTE: The KafkaProducer is inherently threadsafe
	 * (see the {@link org.apache.kafka.clients.producer.KafkaProducer} class)
	 */
	private Producer<String, byte[]> binaryProducer;
	/***
	 * Holds a string KafkaProducer
	 * NOTE: We are creating a single producer as opposed to a pool. This is as per the recommendation 
	 * of the Kafka-Client library that indicates that it is more efficient to share a single producer across threads as it allows batching.
	 * NOTE: The KafkaProducer is inherently threadsafe
	 * (see the {@link org.apache.kafka.clients.producer.KafkaProducer} class)
	 */
	private Producer<String, String> stringProducer;
	/***
	 * Logger
	 */
	//private static final Logger logger = LogManager.getFormatterLogger(KafkaProducerManager.class);
	private static Logger logger = Logger.getLogger("KafkaLogger");

	/***
	 * Instantiates a KafkaProducerManager object. A new binary and string producer are created
	 * using the passed KafkaConfig
	 * @param the config associated with this cluster
	 */
	public KafkaProducerManager(KafkaConfig config) {
		buildBinaryProducer(config);
		buildStringProducer(config);
	}

	/***
	 * Makes a binary producer from the passed KafkaConfig
	 * @param the config associated with this cluster
	 */
	private void buildBinaryProducer(KafkaConfig config) {
		Map<String, Object> producerProps = config.getProducerProperties();
		if(logger.isDebugEnabled()) {
			logger.debug("producers prop size - " + producerProps.size());
		}
		Serializer<String> keySerializer = new StringSerializer();
		Serializer<byte[]> valueSerializer = new ByteArraySerializer();
		keySerializer.configure(producerProps, true);
		valueSerializer.configure(producerProps, false);
		this.binaryProducer = new KafkaProducer<>(producerProps, keySerializer, valueSerializer);
		if(logger.isDebugEnabled()) {
			logger.debug("exiting buildBinaryProducer");
		}
	}

	/***
	 * Makes a string producer from the passed KafkaConfig
	 * @param the config associated with this cluster
	 */
	private void buildStringProducer(KafkaConfig config) {
		Map<String, Object> producerProps = config.getProducerProperties();
		if(logger.isDebugEnabled()) {
			logger.debug("kafka server -- " + producerProps.get("bootstrap.servers"));
		}
		Serializer<String> keySerializer = new StringSerializer();
		Serializer<String> valueSerializer = new StringSerializer();
		keySerializer.configure(producerProps, true);
		valueSerializer.configure(producerProps, false);
		this.stringProducer = new KafkaProducer<>(producerProps, keySerializer, valueSerializer);
		if(logger.isDebugEnabled()) {
			logger.debug("exiting buildStringProducer");
		}
	}

	/***
	 * Produces the passed binary message on the topics related to the passed gatewayId
	 * @param the id of the gateway producing this message
	 * @param the partition on which this message is to be produced
	 * @param the byte array representing the binary message
	 */
	public void produceBinary(String gatewayId, 
			Integer partition,
			byte[] bytes) {
		binaryProducer.send(new ProducerRecord<String, byte[]>(generateOutboundTopic(gatewayId),bytes),
							createCallback());
		if(logger.isDebugEnabled()) {
			logger.debug("exiting produceBinary");
		}
	}
	
	/***
	 * Produces the passed string message on the topics related to the passed gatewayId
	 * @param the id of the gateway producing this message
	 * @param the partition on which this message is to be produced
	 * @param the string representing the message
	 */
	public void produceString(String gatewayId, 
			Integer partition,
			String message) {
		stringProducer.send(new ProducerRecord<String, String>(generateOutboundTopic(gatewayId),message),
							createCallback());
		if(logger.isDebugEnabled()) {
			logger.debug("exiting produceString");
		}
	}
	
	
	/***
	 * Shutdown this KafkaProducerManager and associated producers
	 */
	public void shutdown() {
		this.binaryProducer.close(500, TimeUnit.MILLISECONDS);	
		this.stringProducer.close(500, TimeUnit.MILLISECONDS);
	}

	
	/***
	 * Creates an async <link>org.apache.kafka.clients.producer.Callback</link>.
	 * It gets called with a positive or negative response to the produce operation
	 * We don't want to send this back to the gateway as it can't do anything with this information
	 * so we only log it
	 * @return the callback object
	 */
	private Callback createCallback() {
		
		/*
		return (metadata, e) -> {
			if(e != null) {
				logger.catching(e);
			}
			logger.trace("The offset of the record we just sent is: " + metadata.offset());
		};*/
		return new Callback() {
			public void onCompletion(RecordMetadata metadata, Exception e) {
				if(e != null) {
					logger.error("callback exception " + e);
				} else {
					if(logger.isDebugEnabled()) {
						logger.debug("The offset of the record we just sent is: " + metadata.offset());
					}
				}
			}
		};
		
	}

	/****
	 * Generates the outbound topic from the gatewayId
	 * @param the string representing the gatewayid
	 * @return the string the representing the outvound topic
	 */
	private String generateOutboundTopic(String gatewayId) {
		return gatewayId + "-inbound";
	}

}

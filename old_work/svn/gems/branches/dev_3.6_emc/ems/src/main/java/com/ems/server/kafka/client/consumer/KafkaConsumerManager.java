/**
 * 
 */
package com.ems.server.kafka.client.consumer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

import com.ems.server.kafka.config.KafkaConfig;

/**
 * The <code>KafkaConsumerManager</code> provides a simple threadsafe interface that is used by the websocket layer to subscribe kafka messages. 
 * Internally it uses a configurable pool of {@link KafkaConsumer} objects each of which consumes topics 
 * for multiple gateways and then passes it to a BlockingPriorityQueue that is then again serviced by a configurable pool of workers
 * who then publish these message on the websocket session that is associated with the relevant gateway
 * @author Shyam
 */
public class KafkaConsumerManager {

	/***
	 * Pool of Kafka consumers that is used to service the subsribe requests
	 */
	private final List<KafkaConsumerRunner> consumers = new ArrayList<KafkaConsumerRunner>();
	
	/***
	 * Pool of Kafka consumers that is used to service the subsribe requests
	 */
	//private final List<KafkaTextConsumerRunner> textConsumers = new ArrayList<KafkaTextConsumerRunner>();
	
	/***
	 * Map that holds the associations between a gateway and a KafkaConsumer once a gateway has been subscribed
	 */
	private final Map<String, KafkaConsumerRunner> gatewayToConsumerMap = new ConcurrentHashMap<String, KafkaConsumerRunner>();
	
	/***
	 * Map that holds the associations between a gateway and a KafkaConsumer once a gateway has been subscribed
	 */
	//private final Map<String, KafkaTextConsumerRunner> gatewayToTextConsumerMap = 
		//	new ConcurrentHashMap<String, KafkaTextConsumerRunner>();
	
	/***
	 * Threadsafe integer that is used to get the next consumer in the map
	 */
	private final AtomicInteger nextConsumer;
	/***
	 * Holds the KafkaConfig object which contains the properties needed by <KafkaConsumerManager>
	 */
	private final KafkaConfig config;
	/***
	 * Threadpool for servicing the KafkaConsumers
	 */
	private final ExecutorService consumerRunnerExecutor; 
	/***
	 * Threadpool for servicing the KafkaTextConsumers
	 */
	//private final ExecutorService textConsumerRunnerExecutor;
	
	/***
	 * Threadpool for servicing the PriorityBlockingQueue
	 */
	private final ExecutorService messageDispatcher;
	
	/***
	 * Threadpool for servicing the PriorityBlockingQueue
	 */
	//private final ExecutorService textMessageDispatcher;
	
	/***
	 * The default size for the kafka consumer pool
	 */
	private int consumerPoolSize = 1;
	/***
	 * The default size for the PriorityBlockingQueue workers
	 */
	private int dispatcherPoolSize = 1;
	/***
	 * The PriorityBlockingQueue that holds all the incoming messages from all the KafkaConsumers in the pool
	 */
	private final PriorityBlockingQueue<ConsumerRecords<String, byte[]>> messages;
	//private final PriorityBlockingQueue<ConsumerRecords<String, String>> textMessages;
	
	/***
	 * Logger
	 */
	//private static final Logger logger = LogManager.getFormatterLogger(KafkaConsumerManager.class);
	private static Logger logger = Logger.getLogger("KafkaLogger");


	/***
	 * Instantiates the <code>KafkaConsumerManager</code> class using the passed {@link KafkaConfig} object.
	 * Initializes the KafkaConsumer pool and the PriorityBlockingQueue worker pool
	 * @param config
	 */
	public KafkaConsumerManager(KafkaConfig config) {
		logger.debug("entering KafkaConsumerManager" + config);
		this.config = config;
		this.messages = new PriorityBlockingQueue<ConsumerRecords<String, byte[]>>(20, new ConsumerRecordComparator());
		//this.textMessages = new PriorityBlockingQueue<ConsumerRecords<String, String>>(20, new ConsumerTextRecordComparator());
		consumerPoolSize = Integer.parseInt((String)config.getConsumerProperties().get("consumer.pool"));
		dispatcherPoolSize = Integer.parseInt((String)config.getConsumerProperties().get("dispatcher.pool"));
		consumerRunnerExecutor = Executors.newFixedThreadPool(consumerPoolSize);
		//textConsumerRunnerExecutor = Executors.newFixedThreadPool(consumerPoolSize);
		messageDispatcher = Executors.newFixedThreadPool(dispatcherPoolSize);
		//textMessageDispatcher = Executors.newFixedThreadPool(dispatcherPoolSize);
		nextConsumer = new AtomicInteger(0);
		initConsumerPool();
		initMessageDispatcher();
		logger.debug("exiting KafkaConsumerManager");
	}

	/***
	 * Initializes the KafkaConsumer pool
	 */
	private void initConsumerPool(){
		//Create Pool
		for(int i = 0; i < consumerPoolSize; i++) {
			KafkaConsumerRunner runner = new KafkaConsumerRunner(new KafkaConsumer<>(config.getConsumerProperties()), 
					this.messages, new ArrayList<String>());
			consumers.add(runner);
			consumerRunnerExecutor.submit(runner);
			//KafkaTextConsumerRunner textRunner = new KafkaTextConsumerRunner(new KafkaConsumer<>(config.getConsumerProperties()), 
			//		this.textMessages, new ArrayList<String>());
			//textConsumers.add(textRunner);
			//textConsumerRunnerExecutor.submit(textRunner);
		}
	}

	/****
	 * Initializes the PriorityBlockingQueue worker pool
	 */
	private void initMessageDispatcher() {
		for(int i = 0; i < dispatcherPoolSize; i ++) {
			MessageDispatcherRunner runner = new MessageDispatcherRunner(messages);
			messageDispatcher.submit(runner);
			
			//TextMessageDispatcherRunner textRunner = new TextMessageDispatcherRunner(textMessages);
			//textMessageDispatcher.submit(textRunner);
			
		}
	}

	/****
	 * Subscribes a KafkaConsumer from the pool to the topic related to the gateway.
	 * After subscription associates the KafkaConsumer with the gateway
	 * @param the id of the gateway
	 */
	public void subscribe(String gatewayId) {
		logger.debug("entering subscribe " + gatewayId);
		KafkaConsumerRunner consumerRunner  = consumers.get(nextConsumer.getAndIncrement() % consumers.size());
		consumerRunner.addTopic(gatewayId+"-outbound");
		gatewayToConsumerMap.put(gatewayId, consumerRunner);
		
		//KafkaTextConsumerRunner textConsumerRunner = textConsumers.get(nextConsumer.getAndIncrement() % textConsumers.size());
		//textConsumerRunner.addTopic(gatewayId + "-outbound");
		//logger.debug("added topic to the text consumer");
		//gatewayToTextConsumerMap.put(gatewayId, textConsumerRunner);
		
		logger.debug("exiting subscribe");
	}

	/****
	 * Unsubscribes the KafkaConsumer associated with the passed gatewayId
	 * from the topics related to the gateway
	 * @param the id of the gateway
	 */
	public void unsubscribe(String gatewayId) {
		logger.debug("entering unsubscribe " + gatewayId);
		KafkaConsumerRunner consumerRunner = gatewayToConsumerMap.get(gatewayId);
		consumerRunner.removeTopic(gatewayId);
		
		//KafkaTextConsumerRunner textConsumerRunner = gatewayToTextConsumerMap.get(gatewayId);
		//textConsumerRunner.removeTopic(gatewayId);
		logger.debug("exiting " + gatewayId);
	}
	
	/***
	 * Shutdown this KafkaConsumerManager. Closes all the threads associated with the KafkaConsumer pool and the 
	 * PriorityBlockingQueue worker thread
	 */
	public void shutdown() {
		
		Iterator<KafkaConsumerRunner> consIter = consumers.iterator();
		while(consIter.hasNext()) {
			consIter.next().shutdown();
		}
//		Iterator<KafkaTextConsumerRunner> textConsIter = textConsumers.iterator();
//		while(textConsIter.hasNext()) {
//			textConsIter.next().shutdown();
//		}
		this.consumerRunnerExecutor.shutdown(); //graceful shutdown
		//this.messageDispatcher.shutdown();//graceful shutdown	
	}

}

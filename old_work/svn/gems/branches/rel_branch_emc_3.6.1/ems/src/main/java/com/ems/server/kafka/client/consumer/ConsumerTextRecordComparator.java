package com.ems.server.kafka.client.consumer;

import java.util.Comparator;

import org.apache.kafka.clients.consumer.ConsumerRecords;

/**
 * The <code>ConsumerRecordComparator</code> provides an implementation of the {@link Comparator} interface that 
 * will allow for prioritization of incoming Kafka messages. Currently the compare method doesn't do anything
 * 
 * @author Shyam
 *
 */
public class ConsumerTextRecordComparator implements Comparator<ConsumerRecords<String, String>> {

	/***
	 * @{inheritDoc}
	 */
	@Override
	public int compare(ConsumerRecords<String, String> o1,
			ConsumerRecords<String, String> o2) {
		return 0;
	}

}

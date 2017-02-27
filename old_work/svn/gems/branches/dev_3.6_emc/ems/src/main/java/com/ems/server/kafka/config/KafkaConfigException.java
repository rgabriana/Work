/**
 * 
 */
package com.ems.server.kafka.config;

/**
 * @author Shyam
 *
 */
public class KafkaConfigException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8110290036109197138L;

	public KafkaConfigException() {
	  }

	  public KafkaConfigException(String message) {
	    super(message);
	  }

	  public KafkaConfigException(String message, Throwable cause) {
	    super(message, cause);
	  }

	  public KafkaConfigException(Throwable cause) {
	    super(cause);
	  }

}

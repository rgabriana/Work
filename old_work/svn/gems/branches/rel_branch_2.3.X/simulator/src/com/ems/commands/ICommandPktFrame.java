/**
 * 
 */
package com.ems.commands;

/**
 * @author Sameer Surjikar
 * 
 */
public interface ICommandPktFrame {

	public byte[] toByte(String nodeName);

	public long getLength();

}

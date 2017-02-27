package com.ems.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * This remote service is called using BlazeDS/LiveCycle DS from Flex
 *
 * @author Armindo Cachada
 *
 */
public class RemoteService {
 /**
  * I am not doing anything useful except to just show that I can be invoked remotely
  * from Adobe Flex using RemoteObject.
  *
  */
 public List<String> callMe() {
  System.out.println("I am being invoked!");
  List<String> result = new ArrayList<String>();
  result.add("Michael Jackson");
  result.add("Beatles");
  result.add("Tina Turner");
  return result;
 }
}

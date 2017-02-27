package com.ems.action;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContext implements BeanFactoryAware{

 private static BeanFactory beanFactory = null;

  /**
   * This method is called by spring at startup. No other class sould call this.
   */
  public void setBeanFactory(BeanFactory beanFactory) {
   SpringContext.beanFactory = beanFactory;
  }

  /**
   * Retuns a spring managed bean with the specified name.
   */
  public static Object getBean(String name) {
   return beanFactory.getBean(name);
  }
}
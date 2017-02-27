package com.motion.utils ;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContext implements BeanFactoryAware{

 private static BeanFactory beanFactory = null;

  /**
   * This method is called by spring at startup. No other class should call this.
   */
  public void setBeanFactory(BeanFactory beanFactory) {
   SpringContext.beanFactory = beanFactory;
  }

  /**
   * Returns a spring managed bean with the specified name.
   */
  public static Object getBean(String name) {
   return beanFactory.getBean(name);
  }
}
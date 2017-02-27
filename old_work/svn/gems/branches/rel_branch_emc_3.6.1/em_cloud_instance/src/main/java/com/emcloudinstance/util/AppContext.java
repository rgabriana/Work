package com.emcloudinstance.util;



import java.util.ResourceBundle;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;


public class AppContext implements BeanFactoryAware {

	private static BeanFactory beanFactory = null;

	/**
	 * This method is called by spring at startup. No other class sould call
	 * this.
	 */
	public void setBeanFactory(BeanFactory factory) {
		beanFactory = factory;
		// load properties files from here
		/*try {
		//	properties.load(getClass().getResourceAsStream("/ems.properties"));
			//properties.load(getClass().getResourceAsStream("/mail.properties"));
		} catch (Exception e) {
			throw new RuntimeException("Could not load properties."
					+ e.getMessage());
		}*/
	}

	/**
	 * Retuns a spring managed bean with the specified name.
	 */
	public static Object getBean(String name) {
		return beanFactory.getBean(name);
	}

	/**
	 * Return the value of a property from the configuration properties.
	 * 
	 * @param key
	 * @return
	 */
	public static String getProperty(String key) {
		ResourceBundle resources = ResourceBundle.getBundle("ApplicationResources");
		return resources.getString(key);
	}

//Not required now
	/*
	public static com.ems.model.User getCurrentUser() {
		if (ActionContext.getContext() != null && ActionContext.getContext().getSession() != null) {
			return (com.ems.model.User) ActionContext.getContext().getSession().get(Constants.SESSION_KEY_USER);
		} else {
			return null;
		}
	}
	*/
	
}

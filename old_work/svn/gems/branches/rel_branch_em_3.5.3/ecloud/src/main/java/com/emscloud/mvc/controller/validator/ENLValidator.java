package com.emscloud.mvc.controller.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

/**
 * The server side validator that can be used in ws as well
 * @author admin
 *
 */
public abstract class ENLValidator {

	@Autowired
    protected MessageSource messageSource;
	
	public boolean supports(Class<?> paramClass) {
		return true;
	}
	
	private boolean isValid(final Object obj){
		if (obj == null){
			throw new IllegalArgumentException("Object passed in validator is null");
		}
		if(this.supports(obj.getClass())){
			return performValidation(obj);
		}else{
			throw new ClassCastException("Class "+ obj.getClass()+" is not supported by this validator:"+this.getClass().getName());
		}
		
	}
	
	public boolean validate(final Object ob){
		return this.isValid(ob);
	}
	
	protected abstract boolean performValidation(final Object ob);
	
}

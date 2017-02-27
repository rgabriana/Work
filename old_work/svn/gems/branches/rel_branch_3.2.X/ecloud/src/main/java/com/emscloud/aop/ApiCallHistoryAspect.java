package com.emscloud.aop;

import javax.annotation.Resource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import com.emscloud.api.util.Request;

/**
 * Aspect is applied around the methods from com.emscloud.api.** package results in logging api call history.
 *  
 */
@Aspect
public class ApiCallHistoryAspect {

	@Resource
	Request apiRequest;

    @Before("execution(* com.emscloud.api.*.*(..))")
    public void beforeApiCall(JoinPoint joinPoint) {
    	if(apiRequest.getMessage() == null) {
    		apiRequest.setMessage(new StringBuilder("Before call " + joinPoint.getSignature().getName() + ", transaction Id = " + apiRequest.getTransactionId()  + ". "));
    	}
    	else {
    		apiRequest.getMessage().append("Before call " + joinPoint.getSignature().getName() + ". ");
    	}
    	;
    }
    
    @AfterReturning("execution(* com.emscloud.api.*.*(..))")
    public void afterSuccessfulReturn(JoinPoint joinPoint) {
		apiRequest.getMessage().append("After successful call " + joinPoint.getSignature().getName() + ". ");
    	System.out.println(apiRequest.getMessage().toString());
    }
    
    @AfterThrowing(pointcut = "execution(* com.emscloud.api.*.*(..))", throwing = "e")
    public void afterException(JoinPoint joinPoint, Exception e) {
    	apiRequest.getMessage().append("After exception in call " + joinPoint.getSignature().getName() + ", exception = " + e.getLocalizedMessage() + ". ");
    	System.out.println(apiRequest.getMessage().toString());
    }
}

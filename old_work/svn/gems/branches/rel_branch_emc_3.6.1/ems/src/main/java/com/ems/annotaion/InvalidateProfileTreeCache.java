package com.ems.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation when marked on a service managed bean method will trigger the clearing of profile Tree cache. The
 * annotation marker basically results in called the InvalidateProfileTreeCache Aspect.
 * 
 * @author lalit
 * 
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface InvalidateProfileTreeCache {

}

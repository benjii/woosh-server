package com.luminos.woosh.synchronization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Ben
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Deprecated
public @interface OnEntityCreate {

	/**
	 * The processor to invoke.
	 * 
	 * @return
	 */
	Class<? extends Processor<?, ?>> processor();

	/**
	 * Use this to describe the actions taken when a new instance of this entity type is created.
	 * 
	 * @return
	 */
	String description() default "";
	
	/**
	 * If true then the processor is invoked asynchronously.
	 * 
	 * @return
	 */
	boolean asynchronous() default false;
	
}

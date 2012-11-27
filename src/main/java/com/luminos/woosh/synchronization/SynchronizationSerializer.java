package com.luminos.woosh.synchronization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to tag a property of a @Synchronizable entity with a custom serializer.
 * 
 * @author Ben
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SynchronizationSerializer {

	// the class to instantiate and invoke to serialize / deserialize properties
	Class<? extends Serializer> serializer();
	
	// when a custom serializer is used a custom schema type can be reported to the client
	// this will give the user instructions on how to post properties of that particular type
	String reportedSchemaType() default "";
	
}

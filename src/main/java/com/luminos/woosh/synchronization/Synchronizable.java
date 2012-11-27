package com.luminos.woosh.synchronization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that an entity (the POJO form of it) is synchronizable. Classes with this annotation <i>must</i> implement
 * the SynchronizableEntity interface (a check is performed on this rule when the framework scans for annotated classes at start-up).
 * 
 * @author Ben
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Synchronizable {

	// the alias by which entities on this type are known to the client (defaults to the Camel-case class name of the entity)
	String alias() default "";
	
	// the merge policy for the object (default is read-only meaning that the client can read but not write)
	Policy policy() default Policy.READ_ONLY;
	
	// the scope of the entity (global or user)
	Scope scope() default Scope.GLOBAL; 
	
	// sometimes the order that synchronizable classes are processed is important - this field allows the control
	// of that order
	int order() default 9999;
	
}

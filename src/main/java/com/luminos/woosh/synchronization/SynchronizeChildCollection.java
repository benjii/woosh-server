package com.luminos.woosh.synchronization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sometimes the data models that we synchronize have parent-child relationships. When we synchronize a child, it is a
 * trivial task to associate the parent (by looking the single parent up in the data store by ID and calling the
 * appropriate setter).
 * 
 * However, we often need to then establish the 'reverse' link (that is, add the child to the parents collection of
 * children). This annotations allows synchronizaed entities to be tagged such that it is possible to locate the
 * relevant child collection(s).
 * 
 * @author Ben
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SynchronizeChildCollection {

	// the method that will be called to add the child entity to the parent
	String method() default "";

}

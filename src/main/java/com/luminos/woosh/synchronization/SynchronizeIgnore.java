package com.luminos.woosh.synchronization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Place this annotation on a field if you wish the synchronization mechamisn to ignore it
 * 
 * @author Ben
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SynchronizeIgnore {

}

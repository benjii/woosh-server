package com.luminos.woosh.synchronization.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.luminos.woosh.synchronization.ReceiptAnnotator;

/**
 * 
 * @author Ben
 */
public class ReceiptAnnotatorProcessor {

	/**
	 * Processors the @ReceiptAnnotator annotation for an entity by reading the list of paths
	 * and creating a map of key/value pairs to be embedded into the receipt for the object.
	 * 
	 * @param entity
	 * @return
	 */
	public Map<String, String> process(Object entity) {
		Map<String, String> result = new HashMap<String, String>();
		
		ReceiptAnnotator annotator = entity.getClass().getAnnotation(ReceiptAnnotator.class);
		if (annotator != null) {
			String[] paths = annotator.paths();
			
			for (String path : paths) {
				String[] elements = StringUtils.split(path, '.');
				
				Object obj = entity;
				Class<?> clazz = entity.getClass();
				
				for (String element : elements) {
					try {
						Method m = clazz.getMethod("get" + StringUtils.capitalize(element));
						Object r = m.invoke(obj, new Object[] { });
						
						obj = r;
						clazz = r.getClass();
					} catch (SecurityException e) {
						throw new RuntimeException(e);
					} catch (NoSuchMethodException e) {
						throw new RuntimeException(e);
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					} catch (InvocationTargetException e) {
						throw new RuntimeException(e);
					}
					
				}

				// place the key/value pair in the map (the key is the final element in the path, and the
				// value is the toString() result on the final object in the path
				
				// TODO add support for @SynchronizationSerializer
				// TODO support serializing more types - e.g.: lists and other complex types

				result.put(elements[elements.length -1], obj.toString());										

			}
		}
		
		return result;
	}
	
}

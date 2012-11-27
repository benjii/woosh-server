package com.luminos.woosh.synchronization.service;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import com.luminos.woosh.dao.RemoteBinaryObjectDao;
import com.luminos.woosh.dao.SynchronizableDao;
import com.luminos.woosh.domain.common.RemoteBinaryObject;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.synchronization.Policy;
import com.luminos.woosh.synchronization.ReadOnlySynchronizationEntity;
import com.luminos.woosh.synchronization.Scope;
import com.luminos.woosh.synchronization.Serializer;
import com.luminos.woosh.synchronization.Synchronizable;
import com.luminos.woosh.synchronization.SynchronizationSerializer;
import com.luminos.woosh.synchronization.SynchronizeChildCollection;
import com.luminos.woosh.synchronization.SynchronizeIgnore;
import com.luminos.woosh.synchronization.UserScopedEntity;
import com.luminos.woosh.synchronization.WritableSynchronizationEntity;

/**
 * 
 * @author Ben
 */
@Service
public class SynchronizationService {

	private static final Logger LOGGER = Logger.getLogger(SynchronizationService.class);

	private static final DateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'H:mm:ss.SSSZ");

	
	@Autowired
	private SynchronizableDao synchronizableDao = null;
	
	@Autowired
	private RemoteBinaryObjectDao remoteBinaryObjectDao = null;

	@Autowired
	private CloudServiceProxy cloudServiceProxy = null;

	
	/**
	 * 
	 * @param clazz
	 * @return
	 */
	public String aliasForBean(Class<Synchronizable> clazz) {
		Synchronizable s = clazz.getAnnotation(Synchronizable.class);

		if ( StringUtils.isNotBlank(s.alias()) ) {
			// if there is an alias defined, use it
			return s.alias();
		} else {
			// otherwise return the uncapitalised class name
			return StringUtils.uncapitalize(clazz.getSimpleName());			
		}
	}
	
	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Class<Synchronizable>> listSynchronizableClasses() {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Synchronizable.class));
		
		Set<BeanDefinition> beans = scanner.findCandidateComponents("");
		List<Class<Synchronizable>> classes = new ArrayList<Class<Synchronizable>>();

		// create a list of class types based upon the bean definitions that were found
		try {
			for (BeanDefinition def : beans) {
				classes.add( (Class<Synchronizable>) Class.forName(def.getBeanClassName()) );
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		// sort the synchronizable entities by their order
		Collections.sort(classes, new Comparator<Class<Synchronizable>>() {

			@Override
			public int compare(Class<Synchronizable> clazz, Class<Synchronizable> other) {
				Synchronizable s = clazz.getAnnotation(Synchronizable.class);
				Synchronizable t = other.getAnnotation(Synchronizable.class);

				return new Integer( s.order() ).compareTo( new Integer( t.order() ));
			}

		});
		
		return classes;
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String, SynchronizationEntityDefinition> determineSchema() {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Synchronizable.class));

		Set<BeanDefinition> beans = scanner.findCandidateComponents("");
		Map<String, SynchronizationEntityDefinition> schema = new HashMap<String, SynchronizationEntityDefinition>();
		
		for (BeanDefinition bd : beans) {
			
			try {
				@SuppressWarnings("unchecked")
				Class<Synchronizable> clazz = (Class<Synchronizable>) Class.forName(bd.getBeanClassName());
				Synchronizable s = clazz.getAnnotation(Synchronizable.class);

				// check that the synchronizable entity implements the correct interface for it's policy
				if (s.policy() == Policy.READ_ONLY) {
					if ( ! ReadOnlySynchronizationEntity.class.isAssignableFrom(clazz) ) {
						throw new RuntimeException("Synchronizable class " + bd.getBeanClassName() + " with policy READ_ONLY does not implement the ReadOnlySynchronizationEntity interface.");
					}
				} else {
					if ( ! WritableSynchronizationEntity.class.isAssignableFrom(clazz) ) {
						throw new RuntimeException("Synchronizable class " + bd.getBeanClassName() + " with policy READ_WRITE or WRITE_ONLY does not implement the WritableSynchronizationEntity interface.");
					}
				}

				// check that the synchronizable entity implements the correct interface for it's scope
				if (s.scope() == Scope.USER) {
					if ( ! UserScopedEntity.class.isAssignableFrom(clazz) ) {
						throw new RuntimeException("Synchronizable class " + bd.getBeanClassName() + " with scope USER does not implement the UserScopedEntity interface.");
					}					
				}

				Field[] fields = clazz.getDeclaredFields();
				SynchronizationEntityDefinition entityDefinition = new SynchronizationEntityDefinition();
				
				// for each field get it's name and verify that there is a getter and setter for it
				for (Field f : fields) {

					// if the field is non-static then we include it in the model
					if ( !Modifier.isStatic(f.getModifiers()) ) {

						if ( f.isAnnotationPresent(SynchronizeIgnore.class) )
							continue;
						
						// check that the field has a getter / setter and only include the field if it does
						try {
							clazz.getMethod("get" + StringUtils.capitalize(f.getName()));
							clazz.getMethod("set" + StringUtils.capitalize(f.getName()), f.getType());
						} catch (SecurityException e) {
							throw new RuntimeException(e);
						} catch (NoSuchMethodException e) {
							throw new RuntimeException("The getter and / or setter method for field " + f.getName() + " on synchronizable class " + bd.getBeanClassName() + " does not exist.");
						}

						// if all is OK then add the field definition to the entity definition
						if ( f.isAnnotationPresent(SynchronizationSerializer.class) ) {
							SynchronizationSerializer ss = f.getAnnotation(SynchronizationSerializer.class);
							entityDefinition.addFieldDefinition(f.getName(), ss.reportedSchemaType());
						}
						else if ( RemoteBinaryObject.class.isAssignableFrom(f.getType()) ) {
							entityDefinition.addFieldDefinition(f.getName(), "Binary (Attachment ID)");							
						}
						else if ( f.isAnnotationPresent(Synchronizable.class) ) {
						
							// if the field is a type that is another synchronizable entity then we return String
							// as the type in the schema - the client will pass client ID
							entityDefinition.addFieldDefinition(f.getName(), "String");
						
						} else {
							entityDefinition.addFieldDefinition(f.getName(), f.getType().getSimpleName());
						}
						
					}
				}
				
				// add the bean to the schema definition
				schema.put(this.aliasForBean(clazz), entityDefinition);
				
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		
		return schema;
	}

	public void updateServerEntity(Class<?> clazz, 
								   JsonNode entityJson,
								   MultiValueMap<String, MultipartFile> attachments,
								   WritableSynchronizationEntity newObj, 
								   User user) {

		Iterator<String> fields = (Iterator<String>) entityJson.getFieldNames();

		// if this is a user-scoped entity then assign the user as the currently authenticated users
		if ( UserScopedEntity.class.isAssignableFrom(clazz) ) {
			((UserScopedEntity) newObj).setOwner(user);
		}

		while (fields.hasNext()) {
			String key = fields.next();
			String value = entityJson.path(key).getTextValue();
			
			try {
				Field f = clazz.getDeclaredField(key);
				Method setter = clazz.getMethod("set" + StringUtils.capitalize(key), f.getType());
					
				if ( java.sql.Timestamp.class.isAssignableFrom(f.getType()) 
						|| java.util.Date.class.isAssignableFrom(f.getType()) 
						|| java.sql.Date.class.isAssignableFrom(f.getType()) ) {
					
					// this is a date or timestamp field - parse it
					setter.invoke(newObj, new Timestamp(DEFAULT_DATE_FORMATTER.parse(value).getTime()));											

				} else if ( f.getType().isAnnotationPresent(Synchronizable.class) ) {
					
					// this is a Synchronizable object field - find the parent and link it
					ReadOnlySynchronizationEntity attachedEntity = (ReadOnlySynchronizationEntity) synchronizableDao.findByClientId(value, f.getType());
					setter.invoke(newObj, attachedEntity);
					
					// now associate the child with the parents child collection if required
					SynchronizeChildCollection scc = f.getAnnotation(SynchronizeChildCollection.class);
					if (scc != null) {
						Method childCollectionMethod = attachedEntity.getClass().getMethod(scc.method(), newObj.getClass());
						childCollectionMethod.invoke(attachedEntity, newObj);
					}
					
				} else if ( f.isAnnotationPresent(SynchronizationSerializer.class) ) {
					
					// this property has a custom serializer - invoke it to get the deserialized (Object) value
					SynchronizationSerializer ss = f.getAnnotation(SynchronizationSerializer.class);

					try {
						Serializer serializer = ss.serializer().newInstance();											
						setter.invoke(newObj, serializer.deserialize(value));
					} catch (InstantiationException e) {
						throw new RuntimeException(e);
					}
					
				} else if ( Collection.class.isAssignableFrom(f.getType()) || Array.class.isAssignableFrom(f.getType())  ) {
					
					LOGGER.info("Collection or Array property type detected. Properties of these types are not de-serialized as part of the parent (but will be de-serialized separately if members are tagged with @Synchronizable.");

				} else if ( RemoteBinaryObject.class.isAssignableFrom(f.getType()) ) {
					
					// this is binary data - upload to the Cloud service
					try {
						
						byte[] data = attachments.get(value).get(0).getBytes();

						if ( data != null && data.length > 0) {
							// remove the old binary data object
							Method getter = clazz.getMethod("get" + StringUtils.capitalize(key));
							RemoteBinaryObject originalRemoteObject = (RemoteBinaryObject) getter.invoke(newObj);

							// remove from the Cloud service
							cloudServiceProxy.delete(originalRemoteObject);							
						}

						// create a new remote object pointer and upload to the Cloud service
						RemoteBinaryObject rbo = new RemoteBinaryObject(user, UUID.randomUUID().toString());
						remoteBinaryObjectDao.save(rbo);
						
						// upload to the Cloud service
						cloudServiceProxy.upload(rbo, data);

						// attach the remote binary object to the local entity
						setter.invoke(newObj, rbo);							

					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					

				} else if ( java.lang.Integer.class.isAssignableFrom(f.getType()) ) {
				
					// this is an integer property
					setter.invoke(newObj, Integer.parseInt(value));											
				
				} else if ( java.lang.Boolean.class.isAssignableFrom(f.getType()) ) {
				
					// this is a boolean property
					setter.invoke(newObj, Boolean.parseBoolean(value));											
				
				} else if ( java.lang.String.class.isAssignableFrom(f.getType()) ) {

					// this is a string property
					setter.invoke(newObj, value);											

				}
				
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}									
		}
	}
	
}

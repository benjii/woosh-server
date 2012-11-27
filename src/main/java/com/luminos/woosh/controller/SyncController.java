package com.luminos.woosh.controller;

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
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.NullNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.luminos.woosh.dao.SynchronizableDao;
import com.luminos.woosh.dao.UserDao;
import com.luminos.woosh.domain.common.RemoteBinaryObject;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.synchronization.OnEntityCreate;
import com.luminos.woosh.synchronization.Processor;
import com.luminos.woosh.synchronization.ReadOnlySynchronizationEntity;
import com.luminos.woosh.synchronization.Receipt;
import com.luminos.woosh.synchronization.ReceiptAnnotator;
import com.luminos.woosh.synchronization.Serializer;
import com.luminos.woosh.synchronization.Synchronizable;
import com.luminos.woosh.synchronization.SynchronizationSerializer;
import com.luminos.woosh.synchronization.SynchronizeIgnore;
import com.luminos.woosh.synchronization.WritableSynchronizationEntity;
import com.luminos.woosh.synchronization.service.CloudServiceProxy;
import com.luminos.woosh.synchronization.service.ReceiptAnnotatorProcessor;
import com.luminos.woosh.synchronization.service.SynchronizationEntityDefinition;
import com.luminos.woosh.synchronization.service.SynchronizationService;
import com.luminos.woosh.util.GeoSpatialUtils;

/**
 * Client devices call methods on this controller to synchronize server-side data with their device.
 * 
 * The synchronization service operates by exchanging data between the client and server based upon timestamp and version
 * properties in the incoming HTTP request. Synchronization is performed in discrete pages for connection efficiency reasons.
 * Specifically, these are the steps taken to correctly exchange data;
 * 
 *     1. The client begins by scanning the local (client) database for any dirty records that have a last updated time BEFORE 
 *        the server last updated time,
 *     2. The client posts any dirty records within the first page^, in JSON format, to the server,
 *     3. The server processes any posted records,
 *     4. The server scans the local (server) database for any records updated AFTER the client last updated time,
 *     5. The server sends back a response that contains;
 *     		  a) Receipts for all posted objects,
 *     		  b) Items that the client needs.
 *     		  c) The last update time (which is the time of the most recent record to update
 *     6. The client processes all receipts (flagging records as non-dirty) and all items that require updating,
 *     7. If the server indicated that more pages are available, the client must request those pages.
 *     		  a) At this point the client is allowed to post any records that have a last updated time BEFORE the server last
 *     			 updated time (as per step 1).
 *     8. The client continues to post records until there are no dirty records.  
 * 
 * ^ By default a page is 25 items in size. A client must only post records that are BEFORE the client last updated time. A client
 *   is not required to support post paging, but it is highly recommended.
 *   
 * Synchronization occurs for a number of different entity types. All entities should be sync'd in order (although this is not strictly
 * required). For example, sync all Form entities before all FormInstance entities.
 * 
 * @author Ben
 */
@Controller
public class SyncController extends AbstractLuminosController {

	private static final Logger LOGGER = Logger.getLogger(SyncController.class);

	// used in the entity synchronization methods - temporarily commented out
	private static final DateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'H:mm:ss.SSSz");

	public static final Integer DEFAULT_PAGE_SIZE = 20;


	@Autowired
	private SynchronizationService synchronizationService =  null;

	@Autowired
	private SynchronizableDao synchronizableDao = null;

	@Autowired
	private CloudServiceProxy cloudServiceProxy = null;
	
	@Autowired
	private UserDao userDao = null;
	
	

	/**
	 * Works out what the least recent updated time to report back to the client.
	 * 
	 * @param entities
	 * @param lastUpdatedTimeForClient
	 * @return
	 */
	private Timestamp computeLastUpdatedForClient(List<Synchronizable> entities, Timestamp lastUpdatedTimeForClient) {
		if (CollectionUtils.isEmpty(entities)) {
			return lastUpdatedTimeForClient;
		}
		
		Timestamp mostRecentEntityUpdate = ((ReadOnlySynchronizationEntity) entities.get(entities.size() -1 )).getLastUpdated();
		return ( lastUpdatedTimeForClient.before(mostRecentEntityUpdate) ? lastUpdatedTimeForClient : mostRecentEntityUpdate );
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/m/data", method=RequestMethod.POST)
//	@Secured({ "ROLE_USER" })
	public ModelAndView synchronize(@RequestParam("ts") String timestamp,
									@RequestParam("v") String version,
									@RequestParam("p") String page,
									@RequestParam(value="payload", required=false) String payload,
									HttpServletRequest request,
									HttpSession session) {	
		
		// note and store the current server time
		String serverTime = DEFAULT_DATE_FORMATTER.format(Calendar.getInstance().getTime());
		LOGGER.info("Synchronization request received at " + serverTime + ". Last client device update time: " + timestamp);
		
		// establish the page that the user is requesting (it is usually zero - the first one)
		Integer requestedPage = ( StringUtils.isBlank(page) ? 0 : Integer.parseInt(page) );

		// the number of remaining pages reported to the client device is the maximum number over all entity types
		Integer remainingPages = 0;
		
		// the last updated time reported back to the client is NOT the most recently updated entity
		// it is in fact the most recently updated entity across the page(s) being served
		Timestamp lastUpdatedTimeForClient = new Timestamp(new java.util.Date().getTime());

		// if there is a payload, parse it into a tree model - we'll use it later
		JsonNode payloadRoot = NullNode.instance;
		if ( StringUtils.isNotBlank(payload) ) {
			try {
				JsonParser parser = new JsonFactory().createJsonParser(payload);
				payloadRoot = new ObjectMapper().readTree(parser);
			} catch (JsonParseException e) {
				throw new RuntimeException(e);
			} catch (JsonMappingException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}			
		}

		// posted form instances can come with binary attachments
		MultiValueMap<String, MultipartFile> attachments = null;		
		if ( request instanceof DefaultMultipartHttpServletRequest ) {
			attachments = ((DefaultMultipartHttpServletRequest) request).getMultiFileMap();			
		}

		// create the object that will contain the model to return to the client
		Map<String, Set<Map<String, String>>> outOfSyncEntities = new HashMap<String, Set<Map<String, String>>>();
		Map<String, List<Receipt>> receipts = new HashMap<String, List<Receipt>>();

		// process the last updated time from the client device
		try {
			Timestamp clientLastUpdated = new Timestamp(DEFAULT_DATE_FORMATTER.parse(timestamp).getTime());

			// the first thing that we do is determine what can be synchronized
			List<Class<Synchronizable>> synchronizableClasses = synchronizationService.listSynchronizableClasses();
			LOGGER.info("Found " + synchronizableClasses.size() + " entity types in the inferred schema.");
			
			// for each class query the database for any objects of that type that are out-of-sync with the client device
			for (Class<?> clazz : synchronizableClasses) {
				LOGGER.info("Querying database for out-of-sync entities of type: " + clazz.getCanonicalName());

				List<Synchronizable> entities = synchronizableDao.findAllAfter(clazz, super.getUser(), requestedPage, clientLastUpdated);
				LOGGER.info("Found " + entities.size() + " out-of-sync entities of type: " + clazz.getCanonicalName());
			
				Integer remainingPagesForEntityType = synchronizableDao.countPagesAfter(clazz, super.getUser(), clientLastUpdated);
				LOGGER.info("There are " + remainingPagesForEntityType + " remaining pages for out-of-sync entity type: " + clazz.getCanonicalName());
				remainingPages = Math.max(remainingPages, remainingPagesForEntityType);

				lastUpdatedTimeForClient = computeLastUpdatedForClient(entities, lastUpdatedTimeForClient);
	
				// determine the alias of the entity that is currently being processed
				String alias = synchronizationService.aliasForBean( (Class<Synchronizable>) clazz);

				// only continue with this entity type if there are any entities of this type to synchronize
				if ( entities.size() > 0 ) {
					
					// for each entity that we find extract it's field properties so that they can be marshalled to the client
					Field[] fields = clazz.getDeclaredFields();
					Set<Map<String, String>> valuesMap = new HashSet<Map<String,String>>();
					
					for (Object entity : entities.toArray(new Object[] { })) {
						Map<String, String> values = new HashMap<String, String>();

						for (Field f : fields) {

							// if the field is non-static then we include it in the model
							if ( !Modifier.isStatic(f.getModifiers()) ) {

								if ( f.isAnnotationPresent(SynchronizeIgnore.class) )
									continue;
								
								// check that the field has a getter / setter and only include the field if it does
								Method getter = null;
								try {
									getter = clazz.getMethod("get" + StringUtils.capitalize(f.getName()));
									clazz.getMethod("set" + StringUtils.capitalize(f.getName()), f.getType());
								} catch (SecurityException e) {
									throw new RuntimeException(e);
								} catch (NoSuchMethodException e) {
									throw new RuntimeException("The getter and / or setter method for field " + f.getName() + " on synchronizable class " + clazz.getSimpleName() + " does not exist.");
								}

								// if all is OK then add then extract the field value
								try {
									Object valueFromInvocation = getter.invoke(entity, new Object[] { });
									
									// we do not serialize arrays as part of the parent
									if ( f.getType().isArray() ) {
										LOGGER.info("Array type detected. Arrays are not serialized as part of the parent (but will be serialized separately if members are tagged with @Synchronizable.");
										continue;
									}
									
									// if the field value is NULL then serialize it as an empty string
									if (valueFromInvocation == null) {
										
										values.put(f.getName(), "");							
										
									} else {

										if ( java.sql.Timestamp.class.isAssignableFrom(f.getType()) 
												|| java.util.Date.class.isAssignableFrom(f.getType()) 
												|| java.sql.Date.class.isAssignableFrom(f.getType()) ) {
										
											// this is a date / timestamp value - use the default date serializer
											values.put(f.getName(), DEFAULT_DATE_FORMATTER.format( (java.util.Date) valueFromInvocation ));
										
										} else if ( f.getType().isAnnotationPresent(Synchronizable.class) ) {
											
											// this is an entity that is tagged with @Synchronizable - therefore there is a parent-child
											// relationship here.
											
											Method clientIdGetter;
										
											try {
												clientIdGetter = valueFromInvocation.getClass().getMethod("getClientId");
											} catch (SecurityException e) {
												throw new RuntimeException(e);
											} catch (NoSuchMethodException e) {
												throw new RuntimeException(e);
											}
											
											// this is a Synchronizable entity (a parent object) - render it's client ID
											values.put(f.getName(), clientIdGetter.invoke(valueFromInvocation).toString());

										} else if ( RemoteBinaryObject.class.isAssignableFrom(f.getType()) ) {
											
											// if the property is of special type RemoteBinaryObject then this is a property
											// that stores binary data in remote Cloud storage - in this case we provide the
											// client with a timed URL for direct retrieval
											values.put(f.getName(), this.cloudServiceProxy.createSignedUrl((RemoteBinaryObject) valueFromInvocation));											

										} else if ( f.isAnnotationPresent(SynchronizationSerializer.class) ) {
											
											// this property has a custom serializer - invoke it to get the serialized (String) value
											SynchronizationSerializer ss = f.getAnnotation(SynchronizationSerializer.class);

											try {
												Serializer serializer = ss.serializer().newInstance();											
												values.put(f.getName(), serializer.serialize(valueFromInvocation));
											} catch (InstantiationException e) {
												throw new RuntimeException(e);
											}
											
										} else if ( Collection.class.isAssignableFrom(f.getType()) || Array.class.isAssignableFrom(f.getType())  ) {
											
											LOGGER.info("Collection or Array property type detected. Properties of these types are not serialized as part of the parent (but will be serialized separately if members are tagged with @Synchronizable.");
											values.put(f.getName(), Integer.toString( ((Collection<?>) valueFromInvocation).size()) );
											
										} else {
											
											// if none of the above matches then we have a standard property type - simply call toString()
											values.put(f.getName(), valueFromInvocation.toString());
											
										}
									}
									
								} catch (IllegalArgumentException e) {
									throw new RuntimeException(e);
								} catch (IllegalAccessException e) {
									throw new RuntimeException(e);
								} catch (InvocationTargetException e) {
									throw new RuntimeException(e);
								}
							}
						}
						
						// add the values for this entity to the values map
						valuesMap.add(values);
					}

					// place the full list of out-of-date entities into the model (to be rendered into the view)xs
					outOfSyncEntities.put(alias, valuesMap);
				}
				
				// now that we have read all out-of-date entities of the type, perform any require write
				// operations (that is, deal with the 'payload' parameter)
				JsonNode entityJsonArray = payloadRoot.path(alias);
				if ( !entityJsonArray.isMissingNode() && WritableSynchronizationEntity.class.isAssignableFrom(clazz) ) {
					List<Receipt> receiptsForEntity = new ArrayList<Receipt>();

					// there are posted entities to deal with
					for (JsonNode entityJson : ((ArrayNode) entityJsonArray) ) {
						try {
							
							// look at the client ID and version - this tells us how to behave going forward
							String clientId = entityJson.get("clientId").getTextValue();
							Integer clientVersion = Integer.parseInt(entityJson.get("clientVersion").getTextValue());
							
							// find the server-side object (if it exists) by ID and class type
							WritableSynchronizationEntity s = (WritableSynchronizationEntity) synchronizableDao.findByClientId(clientId, clazz);
							
							if (s == null) {
								
								// this is a new object that the server does not know about - create it
								WritableSynchronizationEntity newObj = (WritableSynchronizationEntity) clazz.newInstance();								

								// hydrate the new entity with data provided by the client
								synchronizationService.updateServerEntity(clazz, entityJson, attachments, newObj, super.getUser());

								// look for an OnEntityCreate annotation - if there is one then invoke the processor that
								// is associated with it
								OnEntityCreate createProcessor = clazz.getAnnotation(OnEntityCreate.class);
								if (createProcessor != null) {
									
									// TODO add support for invoking the processor asynchronously
									
									// when we invoke a processor we make sure that the user passed is the fully-loaded
									// user from the database (not the one held in the secure session)
									User fullyLoadedUser = userDao.findByUsername(super.getUser().getUsername());
									
									@SuppressWarnings("rawtypes")
									Processor p = createProcessor.processor().newInstance();
									p.process(fullyLoadedUser, newObj, this.synchronizableDao);
								}
								
								// this is a new object that has just been created - set the client version to 0 (as the
								// client itself would have set it to -1)
								newObj.setClientVersion(0);
								
								// flush to the database
								synchronizableDao.save(newObj);

								// create the receipt so that it will be added to the model and reported to the client
								Receipt receipt = new Receipt(newObj.getClientId(), newObj.getClientVersion(), newObj.getLastUpdated());
								ReceiptAnnotator annotator = clazz.getAnnotation(ReceiptAnnotator.class);
								if (annotator != null) {
									receipt.setAdditionalProperties(new ReceiptAnnotatorProcessor().process(newObj));
								}

								receiptsForEntity.add(receipt);
								
							} else if (s.getClientVersion() >= clientVersion) {

								// the server already has a more recent version of this object - create a receipt and tell
								// the client device to not send it again

								LOGGER.info("Server received an out-of-date entity from a client. Ignoring...");
								
							} else {
								
								// this is an updated object from the client - update the server and send a receipts
								synchronizationService.updateServerEntity(clazz, entityJson, attachments, s, super.getUser());

								// flush to the database
								synchronizableDao.save(s);

								// create the receipt so that it will be added to the model and reported to the client
								Receipt receipt = new Receipt(s.getClientId(), s.getClientVersion(), s.getLastUpdated());
								ReceiptAnnotator annotator = clazz.getAnnotation(ReceiptAnnotator.class);
								if (annotator != null) {
									receipt.setAdditionalProperties(new ReceiptAnnotatorProcessor().process(s));
								}
								
								receiptsForEntity.add(receipt);

							}
														
						} catch (InstantiationException e) {
							throw new RuntimeException(e);
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}

					receipts.put(alias, receiptsForEntity);
				}				
			}

		} catch (ParseException e) {
			return new ModelAndView("util/error");
		}
		
		ModelAndView mav = new ModelAndView("sync/sync");

		// fill out the model
		mav.addObject("pageNum", page);
		mav.addObject("remainingPages", remainingPages);
		
		// if there are no remaining pages (i.e.: this is a non-paged sync) then 'fast-forward' the client
		// to the current server time (essentially telling the client that they are fully up-to-date)
		// otherwise we have data left and the client must re-request for the next page
		if (remainingPages == 0) {
			mav.addObject("updateTime", new Timestamp(Calendar.getInstance().getTimeInMillis()));			
		} else {
			mav.addObject("updateTime", lastUpdatedTimeForClient);					
		}
		
		mav.addObject("entities", outOfSyncEntities);
		mav.addObject("receipts", receipts);

		return mav;
	}
	
	/**
	 * This method allows devices (or any other client type) to determine what is offered by the synchronization system that is
	 * embedded into this application.
	 * 
	 * It does this by performing a component scan on the classpath for classes annotated as synchronizable.
	 * 
	 * @param version
	 * @param session
	 * @return
	 */
	@RequestMapping(value="/m/schema", method=RequestMethod.GET)
//	@Secured({ "ROLE_USER" })
	public ModelAndView schema(@RequestParam("v") String version, HttpSession session) {			
		Map<String, SynchronizationEntityDefinition> schema = synchronizationService.determineSchema();
		
		ModelAndView mav = new ModelAndView("sync/schema");
		mav.addObject("version", version);
		mav.addObject("schema", schema);

		return mav;
	}
	
	/**
	 * This is a special method that identifies that user that is synchronizing with the system. Client devices need to know who the
	 * user is so that they can control access to forms and form states.
	 *
	 * @return
	 */
	@RequestMapping(value="/m/whoami", method=RequestMethod.GET)
//	@Secured({ "ROLE_USER" })
	public ModelAndView whoAmI(@RequestParam("v") String version, @RequestParam(value="lat", required=false) String latitude, @RequestParam(value="lon", required=false) String longitude, HttpSession session) {
		User user = super.getUser();
		
		// record the users last login time
		user.setLastLogin(new Timestamp(Calendar.getInstance().getTimeInMillis()));
		
//		// set the invitation key if the user does not already have one
//		if (user.getInvitationalKey() == null) {
//			user.setInvitationalKey(new RandomString(8).nextString());
//		}

//		// set the default points balance if the user does not have one 
//		if (user.getPointsBalance() == null) {
//			user.setPointsBalance(0);
//		}
		
		// store the last known user location if one was provided
		if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude)) {
			try {
				user.setLastKnownLocation(GeoSpatialUtils.createPoint(latitude, longitude));
			} catch (IllegalArgumentException ex) {
				// the client sent a bad whoami request - log the issue but ignore it
				LOGGER.warn("A client sent a bad 'whoami' request - not saving location information, but continuing...");
			}
		}
		
		try {
			userDao.save(user);			
		} catch (Exception ex) {
			LOGGER.warn("Could not persist user state in 'whoami' call. Non-critical, continuing...");
		}
		
		ModelAndView mav = new ModelAndView("sync/whoami");
		mav.addObject("user", user);
		mav.addObject("lastKnownLocation", (user.getLastKnownLocation() != null ? user.getLastKnownLocation().toText() : "" ));
		return mav;
	}
	
	
	/**
	 * Registers a request binder so that we can recieve binary data (photographs, etc) from the client devices.Ø
	 * 
	 * @param binder
	 */
	@InitBinder
    public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
	}
	
}

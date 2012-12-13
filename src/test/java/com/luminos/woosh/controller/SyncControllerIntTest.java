package com.luminos.woosh.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.luminos.woosh.base.AbstractLuminosIntegrationTest;
import com.luminos.woosh.dao.AcceptanceDao;
import com.luminos.woosh.dao.CardDao;
import com.luminos.woosh.dao.CardDataDao;
import com.luminos.woosh.dao.OfferDao;
import com.luminos.woosh.dao.UserDao;
import com.luminos.woosh.domain.Acceptance;
import com.luminos.woosh.domain.Card;
import com.luminos.woosh.domain.CardData;
import com.luminos.woosh.domain.Offer;
import com.luminos.woosh.domain.common.User;
import com.luminos.woosh.synchronization.Synchronizable;
import com.luminos.woosh.synchronization.service.SynchronizationEntityDefinition;
import com.luminos.woosh.testutils.MockServletContextWebContextLoader;
import com.luminos.woosh.util.GeoSpatialUtils;

/**
 * TODO: Cloud integration tests
 * 
 * @author Ben
 */
public class SyncControllerIntTest extends AbstractLuminosIntegrationTest {

	private static final Logger LOGGER = Logger.getLogger(SyncControllerIntTest.class);

	private static final DateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'H:mm:ss.SSSZ");

	
	@Autowired
	private SyncController syncController = null;

	@Autowired
	private CardDao cardDao = null;
	
	@Autowired
	private CardDataDao cardDataDao = null;
	
	@Autowired
	private OfferDao offerDao = null;
	
	@Autowired
	private AcceptanceDao acceptanceDao = null;

	@Autowired
	private UserDao userDao = null;
	

	// ****
	// These tests are for testing the 'who am i?' operation.
	// ****
	@Test
	public void whoAmIOperationWorks() throws Exception {

		// make a call to the sync controller
		ModelAndView mav = syncController.whoAmI("1.0", "1.0", "1.0", super.getSession());

		User user = (User) mav.getModel().get("user");

		assertEquals(user.getUsername(), END_USER.getUsername());

		String json = super.resolveAndProcessView(mav);
		super.assertIsValidJson(json);
		LOGGER.info(json);
		
	}

	// ****
	// These tests are for testing the schema discovery operations.
	// ****
	@Test
	public void canLocateClassesAnnotatedAsSynchronized() throws Exception {

		// make a call to the sync controller
		ModelAndView mav = syncController.schema("1.0", new MockHttpSession(MockServletContextWebContextLoader.SERVLET_CONTEXT));

		@SuppressWarnings("unchecked")
		Map<String, SynchronizationEntityDefinition> beans = (Map<String, SynchronizationEntityDefinition>) mav.getModel().get("schema");

		assertEquals("The number of classes annotated as synchronizable is not correct.", 5, beans.size());

		String json = super.resolveAndProcessView(mav);
		super.assertIsValidJson(json);
		LOGGER.info(json);
		
	}

	@Test
	public void canDetermineSchemaOfAnnotatedEntities() throws Exception {

		// make a call to the sync controller
		ModelAndView mav = syncController.schema("1.0", new MockHttpSession(MockServletContextWebContextLoader.SERVLET_CONTEXT));

		@SuppressWarnings("unchecked")
		Map<String, SynchronizationEntityDefinition> schema = (Map<String, SynchronizationEntityDefinition>) mav.getModel().get("schema");

		SynchronizationEntityDefinition def = schema.get("cards");
		assertNotNull("No schema definition 'cards' found.", def);
		assertEquals("The synchronizable object should have 10 fields.", 10, def.getFieldCount());

	}

	// ****
	// These tests are for testing the synchronization capabilities.
	// ****

	@Test
	public void canSynchronizeCardsAndData() throws Exception {

		// create a sample card in the database (we're going to test if we can synchronize it down)
		createSampleCard(END_USER);

		// this calendar represents the last time that the device synchronized (we make it 2 hours ago)
		Calendar c = Calendar.getInstance();
		c.add(Calendar.HOUR, -2);

		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
													  "1.0", 
													  "0", 
													  "", 
													  super.getRequest(), 
													  super.getSession());

		assertNotNull(mav);
		
		@SuppressWarnings("unchecked")
		Map<String, List<Synchronizable>> result = (Map<String, List<Synchronizable>>) mav.getModel().get("entities");

		String json = super.resolveAndProcessView(mav);
		LOGGER.info(json);
		
		assertNotNull(result);
		assertEquals(2, result.size());				// we expect one 'cards' node and one 'carddata' node (each with one element)
		assertTrue(result.containsKey("cards"));
		assertTrue(result.containsKey("carddata"));
		
		super.assertIsValidJson(json);
		super.assertJsonNodeChildCount(json, "entities.cards", 1);
		super.assertJsonNodeHasValue(json, "entities.carddata[0].name", "Hello, ");
		super.assertJsonNodeHasValue(json, "entities.carddata[0].data", "world!");		
	}
	
	@Test
	public void canPostCardsAndData() {
		StringBuffer payload = new StringBuffer();

		// create the sample card payload
		payload.append("{ ")
			   .append("  \"cards\": [")
			   .append("	{")
			   .append("        \"name\": \"Bar\",")
			   .append("        \"lastUpdated\": \"2012-09-20T12:27:32.694+0100\",")
			   .append("        \"clientVersion\": \"-1\",")
			   .append("        \"deleted\": \"false\",")
			   .append("        \"clientId\": \"b159b2dd-59c3-48c0-9c6d-e0c382f54439\",")
			   .append("        \"maximumAccepts\": \"1\"")
			   .append("	}")
			   .append("  ],")
			   .append("  \"carddata\": [")
			   .append("	{")
			   .append("        \"name\": \"Baz\",")
			   .append("        \"lastUpdated\": \"2012-09-20T12:27:32.694+0100\",")
			   .append("        \"clientVersion\": \"-1\",")
			   .append("        \"deleted\": \"false\",")
			   .append("        \"clientId\": \"1c5cf4fe-558e-4033-b11c-bde2be160ae4\",")
			   .append("        \"card\": \"b159b2dd-59c3-48c0-9c6d-e0c382f54439\",")
			   .append("        \"data\": \"Hello, world!\"")
			   .append("	}")
			   .append("  ]")
			   .append("}");


		// this calendar represents the last time that the device synchronized
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MILLISECOND, -5);		
		
		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
													  "1.0", 
													  "0", 
													  payload.toString(), 
													  super.getRequest(), 
													  super.getSession());

		assertNotNull(mav);
	
		String json = super.resolveAndProcessView(mav);
		LOGGER.info(json);
		
		super.assertIsValidJson(json);
		super.assertJsonNodeChildCount(json, "receipts.cards", 1);
		super.assertJsonNodeChildCount(json, "receipts.carddata", 1);
		super.assertJsonNodeHasValue(json, "receipts.cards[0].version", "0");
		super.assertJsonNodeHasValue(json, "receipts.carddata[0].version", "0");
	
		// ensure that the new entity was flushed to the database
		assertEquals(1, super.countRowsInTable("Card"));
		assertEquals(1, super.countRowsInTable("CardData"));
		
		Card postedCard = cardDao.findByClientId("b159b2dd-59c3-48c0-9c6d-e0c382f54439");
		assertEquals("Card name was not correct.", postedCard.getName(), "Bar");
		assertEquals("Card client version was not correct.", postedCard.getVersion(), (Integer)2);
		
		CardData postedData = cardDataDao.findByClientId("1c5cf4fe-558e-4033-b11c-bde2be160ae4");
		assertEquals(postedData.getName(), "Baz");
		assertEquals(postedData.getCard().getName(), "Bar");		
	}

	@Test
	public void canPostOffers() {

		// create a sample card (to post offers against)
		Card card = createSampleCard(OFFERING_USER);

		StringBuffer payload = new StringBuffer();

		// create the sample card payload
		payload.append("{ ")
			   .append("  \"offers\": [")
			   .append("	{")
			   .append("        \"lastUpdated\": \"2012-09-20T12:27:32.694+0100\",")
			   .append("        \"clientVersion\": \"-1\",")
			   .append("        \"deleted\": \"false\",")
			   .append("        \"clientId\": \"3ac0390f-ea51-4881-a23a-26b9f1bfafbe\",")
			   .append("        \"card\": \"" + card.getClientId() + "\",")
			   .append("        \"maximumAccepts\": \"1\",")
			   .append("        \"offerStart\": \"2012-09-22T12:30:00.000+0100\",")
			   .append("        \"offerEnd\": \"2012-09-22T14:30:00.000+0100\",")
			   .append("        \"offerRegion\": \"POINT (51.551419 -0.113859)\",")				// N7 8EW
			   .append("        \"autoAccept\": \"true\"")
			   .append("	}")
			   .append("  ]")
			   .append("}");

		// this calendar represents the last time that the device synchronized
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MILLISECOND, -5);		
		
		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
													  "1.0", 
													  "0", 
													  payload.toString(), 
													  super.getRequest(), 
													  super.getSession());

		assertNotNull(mav);
	
		String json = super.resolveAndProcessView(mav);
		LOGGER.info(json);
		
		super.assertIsValidJson(json);
		super.assertJsonNodeChildCount(json, "receipts.offers", 1);
		super.assertJsonNodeHasValue(json, "receipts.offers[0].version", "0");
	
		// ensure that the new entity was flushed to the database
		assertEquals(1, super.countRowsInTable("Offer"));
		
		Offer postedOffer = offerDao.findByClientId("3ac0390f-ea51-4881-a23a-26b9f1bfafbe");
		assertEquals(postedOffer.getCard().getClientId(), card.getClientId());
		assertEquals(postedOffer.getOfferRegion(), GeoSpatialUtils.createPoint("51.551419", "-0.113859"));	

	}

	@Test
	public void canPostCardsWithBinaryData() {
		
		// create some arbitrary binary data
		byte[] binary = new byte[2048];
		new Random().nextBytes(binary);
		
		// all binary data must have an ID (referred to as an 'attachment ID')
		String binaryId = UUID.randomUUID().toString();
	
		// set up the multipart HTTP request
		MultiValueMap<String, MultipartFile> files = new LinkedMultiValueMap<String, MultipartFile>();
		files.add(binaryId, new MockMultipartFile(binaryId, binary));
		
		DefaultMultipartHttpServletRequest multiPartRequest = super.getMultipartRequest(files);
		
		// create the card and card data payload
		StringBuffer payload = new StringBuffer();

		// create the sample card payload
		payload.append("{ ")
			   .append("  \"cards\": [")
			   .append("	{")
			   .append("        \"name\": \"Bar\",")
			   .append("        \"lastUpdated\": \"2012-09-20T12:27:32.694+0100\",")
			   .append("        \"clientVersion\": \"-1\",")
			   .append("        \"deleted\": \"false\",")
			   .append("        \"clientId\": \"b159b2dd-59c3-48c0-9c6d-e0c382f54439\",")
			   .append("        \"maximumAccepts\": \"1\"")
			   .append("	}")
			   .append("  ],")
			   .append("  \"carddata\": [")
			   .append("	{")
			   .append("        \"name\": \"Baz\",")
			   .append("        \"lastUpdated\": \"2012-09-20T12:27:32.694+0100\",")
			   .append("        \"clientVersion\": \"-1\",")
			   .append("        \"deleted\": \"false\",")
			   .append("        \"clientId\": \"1c5cf4fe-558e-4033-b11c-bde2be160ae4\",")
			   .append("        \"card\": \"b159b2dd-59c3-48c0-9c6d-e0c382f54439\",")
			   .append("        \"binaryData\": \"" + binaryId + "\"")
			   .append("	}")
			   .append("  ]")
			   .append("}");

		// this calendar represents the last time that the device synchronized
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MILLISECOND, -5);		
		
		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
													  "1.0", 
													  "0", 
													  payload.toString(), 
													  multiPartRequest, 
													  super.getSession());

		assertNotNull(mav);
	
		String json = super.resolveAndProcessView(mav);
		LOGGER.info(json);
		
		super.assertIsValidJson(json);
		super.assertJsonNodeChildCount(json, "receipts.cards", 1);
		super.assertJsonNodeChildCount(json, "receipts.carddata", 1);
		super.assertJsonNodeHasValue(json, "receipts.cards[0].version", "0");
		super.assertJsonNodeHasValue(json, "receipts.carddata[0].version", "0");
	
		// ensure that the new entity was flushed to the database
		assertEquals(1, super.countRowsInTable("Card"));
		assertEquals(1, super.countRowsInTable("CardData"));
		assertEquals(1, super.countRowsInTable("RemoteBinaryObject"));
				
	}
	
	@Test
	public void canScanForOffersAndGetCardsAndCandidates() throws Exception {
		
		// create a sample card (to post offers against)
		Card card = createSampleCard(OFFERING_USER);
		
		// create a sample offer
		Offer offer = new Offer(OFFERING_USER, card, GeoSpatialUtils.createPoint("51.551419", "-0.113859"));	// N7 8EW
		
		// we set the offer start and end to encompass the scan time (below), which is the 20th of September 2012
		offer.setOfferStart(new Timestamp(DEFAULT_DATE_FORMATTER.parse("2012-09-17T12:27:32.694+0100").getTime()));
		offer.setOfferEnd(new Timestamp(DEFAULT_DATE_FORMATTER.parse("2012-09-22T12:27:32.694+0100").getTime()));

		offerDao.save(offer);
		
		// now post a scan entity - this is what clients will post when looking for offers in the area
		StringBuffer payload = new StringBuffer();

		// create the sample scan payload
		// note that this scan is exactly on top of the offer so we should get an offer candidate regardless of offer range
		payload.append("{ ")
			   .append("  \"scans\": [")
			   .append("	{")
			   .append("        \"lastUpdated\": \"2012-09-20T12:27:32.694+0100\",")
			   .append("        \"clientVersion\": \"-1\",")
			   .append("        \"deleted\": \"false\",")
			   .append("        \"clientId\": \"3ac0390f-ea51-4881-a23a-26b9f1bfafbe\",")
			   .append("        \"scannedAt\": \"2012-09-20T13:45:27.000+0100\",")
			   .append("        \"location\": \"POINT (51.551419 -0.113859)\"")				// N7 8EW
			   .append("	}")
			   .append("  ]")
			   .append("}");
		
		// this calendar represents the last time that the device synchronized
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MILLISECOND, -5);
		
		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
													  "1.0", 
													  "0", 
													  payload.toString(), 
													  super.getRequest(), 
													  super.getSession());

		assertNotNull(mav);

		String json = super.resolveAndProcessView(mav);
		LOGGER.info(json);

		// ensure that we have 2 offers and 2 cards in the database
		assertEquals(1, super.countRowsInTable("Offer"));
		assertEquals(2, super.countRowsInTable("Card"));
		assertEquals(1, super.countRowsInTable("Acceptance"));

		// ensure that only one offer was synchronized to the end user
		super.assertIsValidJson(json);
		super.assertJsonNodeChildCount(json, "receipts.scans", 1);
		super.assertJsonNodeChildCount(json, "entities.cards", 1);
		super.assertJsonNodeChildCount(json, "entities.acceptances", 1);
		super.assertJsonNodeHasValue(json, "entities.acceptances[0].offer", offer.getClientId());
	}

	@Test
	public void canScanForOffersDoesNotProduceCandidatesWhenOutOfRange() throws Exception {

		// create a sample card (to post offers against)
		Card card = createSampleCard(OFFERING_USER);
		
		// create a sample offer
		Offer offer = new Offer(OFFERING_USER, card, GeoSpatialUtils.createPoint("-31.953004", "115.857469"));	// offer was made in Perth, WA

		// set the offer to start on the 17th of September 2012, and end on the 22nd of September 2012
		// note that in this test we simulate a scan on the 20th of September 2012 
		offer.setOfferStart(new Timestamp(DEFAULT_DATE_FORMATTER.parse("2012-09-17T12:27:32.694+0100").getTime()));
		offer.setOfferEnd(new Timestamp(DEFAULT_DATE_FORMATTER.parse("2012-09-22T12:27:32.694+0100").getTime()));
		
		offerDao.save(offer);
		
		// now post a scan entity - this is what clients will post when looking for offers in the area
		StringBuffer payload = new StringBuffer();

		// create the sample scan payload
		// note that this scan is exactly on top of the offer so we should get an offer candidate regardless of offer range
		payload.append("{ ")
			   .append("  \"scans\": [")
			   .append("	{")
			   .append("        \"lastUpdated\": \"2012-09-20T12:27:32.694+0100\",")
			   .append("        \"clientVersion\": \"-1\",")
			   .append("        \"deleted\": \"false\",")
			   .append("        \"clientId\": \"3ac0390f-ea51-4881-a23a-26b9f1bfafbe\",")
			   .append("        \"scannedAt\": \"2012-09-20T13:45:27.000+0100\",")
			   .append("        \"location\": \"POINT (51.551419 -0.113859)\"")				// scan at N7 8EW
			   .append("	}")
			   .append("  ]")
			   .append("}");
		
		// this calendar represents the last time that the device synchronized
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MILLISECOND, -5);
		
		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
													  "1.0", 
													  "0", 
													  payload.toString(), 
													  super.getRequest(), 
													  super.getSession());

		assertNotNull(mav);

		String json = super.resolveAndProcessView(mav);
		LOGGER.info(json);

		// ensure that we have 1 offers and 1 cards in the database
		assertEquals(1, super.countRowsInTable("Offer"));
		assertEquals(1, super.countRowsInTable("Card"));

		// ensure that no offers were made (this user is out of range)
		super.assertIsValidJson(json);
		super.assertJsonNodeChildCount(json, "receipts.scans", 1);

	}

	@Test
	public void canScanForOffersDoesNotProduceCandidatesWhenOfferIsExpired() throws Exception {

		// create a sample card (to post offers against)
		Card card = createSampleCard(OFFERING_USER);
		
		// create a sample offer
		Offer offer = new Offer(OFFERING_USER, card, GeoSpatialUtils.createPoint("51.551419", "-0.113859"));	// offer was made at N7 8EW

		// set the offer to start on the 17th of September 2012, and end on the 18th of September 2012
		// note that in this test we simulate a scan on the 20th of September 2012 (which is after the offer expires)
		offer.setOfferStart(new Timestamp(DEFAULT_DATE_FORMATTER.parse("2012-09-17T12:27:32.694+0100").getTime()));
		offer.setOfferEnd(new Timestamp(DEFAULT_DATE_FORMATTER.parse("2012-09-18T12:27:32.694+0100").getTime()));
		
		offerDao.save(offer);
		
		// now post a scan entity - this is what clients will post when looking for offers in the area
		StringBuffer payload = new StringBuffer();

		// create the sample scan payload
		// note that this scan is exactly on top of the offer so we should get an offer candidate regardless of offer range
		payload.append("{ ")
			   .append("  \"scans\": [")
			   .append("	{")
			   .append("        \"lastUpdated\": \"2012-09-20T12:27:32.694+0100\",")
			   .append("        \"clientVersion\": \"-1\",")
			   .append("        \"deleted\": \"false\",")
			   .append("        \"clientId\": \"3ac0390f-ea51-4881-a23a-26b9f1bfafbe\",")
			   .append("        \"scannedAt\": \"2012-09-20T13:45:27.000+0100\",")
			   .append("        \"location\": \"POINT (51.551419 -0.113859)\"")				// scan at N7 8EW
			   .append("	}")
			   .append("  ]")
			   .append("}");
		
		// this calendar represents the last time that the device synchronized
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MILLISECOND, -5);
		
		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
													  "1.0", 
													  "0", 
													  payload.toString(), 
													  super.getRequest(), 
													  super.getSession());

		assertNotNull(mav);

		String json = super.resolveAndProcessView(mav);
		LOGGER.info(json);

		// ensure that we have 1 offers and 1 cards in the database
		assertEquals(1, super.countRowsInTable("Offer"));
		assertEquals(1, super.countRowsInTable("Card"));

		// ensure that no offers were made (this user is out of range)
		super.assertIsValidJson(json);
		super.assertJsonNodeChildCount(json, "receipts.scans", 1);

	}

	/**
	 * TODO Should we allow acceptances outside of the offer start and end time? At the moment we do...
	 * 
	 * @throws Exception
	 */
	@Test
	public void canAcceptOffers() throws Exception {

		// create a sample card (to post offers against)
		Card card = createSampleCard(OFFERING_USER);
		
		// create a sample offer
		Offer offer = new Offer(OFFERING_USER, card, GeoSpatialUtils.createPoint("51.551419", "-0.113859"));	// N7 8EW
		offer.setAutoAccept(Boolean.FALSE);
		
		// we set the offer start and end to encompass the scan time (below), which is the 20th of September 2012
		offer.setOfferStart(new Timestamp(DEFAULT_DATE_FORMATTER.parse("2012-09-17T12:27:32.694+0100").getTime()));
		offer.setOfferEnd(new Timestamp(DEFAULT_DATE_FORMATTER.parse("2012-09-22T12:27:32.694+0100").getTime()));

		offerDao.save(offer);
		
		// create the card that the user would get from the scan
		Card cardForEndUser = card.clone(END_USER);
		cardDao.save(cardForEndUser);
		
		Acceptance acceptance = new Acceptance(END_USER, cardForEndUser, offer);
		acceptance.setClientVersion(0);
		acceptanceDao.save(acceptance);
				
		Thread.sleep(10);	// sleep for 10 milliseconds so that we can sync the acceptance entity
		
		// now post a scan entity - this is what clients will post when looking for offers in the area
		StringBuffer payload = new StringBuffer();

		// create the sample scan payload
		// note that this scan is exactly on top of the offer so we should get an offer candidate regardless of offer range
		payload.append("{ ")
			   .append("  \"acceptances\": [")
			   .append("	{")
			   .append("        \"lastUpdated\": \"2012-09-20T12:27:32.694+0100\",")
			   .append("        \"clientVersion\": \"1\",")									// make sure that this is > 0
			   .append("        \"deleted\": \"false\",")
			   .append("        \"clientId\": \"" + acceptance.getClientId() + "\",")
			   .append("        \"card\": \"" + cardForEndUser.getClientId() + "\",")
			   .append("        \"offer\": \"" + offer.getClientId() + "\",")
			   .append("        \"accepted\": \"true\",")
			   .append("        \"acceptedAt\": \"2012-09-20T12:27:32.694+0100\"")
			   .append("	}")
			   .append("  ]")
			   .append("}");
		
		// this calendar represents the last time that the device synchronized
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MILLISECOND, -5);
		
		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
													  "1.0", 
													  "0", 
													  payload.toString(), 
													  super.getRequest(), 
													  super.getSession());

		assertNotNull(mav);

		String json = super.resolveAndProcessView(mav);
		LOGGER.info(json);

		// ensure that we have 2 offers and 2 cards in the database
		assertEquals(1, super.countRowsInTable("Acceptance"));

		// ensure that the user got their card and acceptance, and that the card has not been accepted yet
		super.assertIsValidJson(json);
		super.assertJsonNodeChildCount(json, "receipts.acceptances", 1);
		
		Acceptance postedAcceptance = acceptanceDao.findByClientId(acceptance.getClientId());
		assertTrue(postedAcceptance.getAccepted());
		assertNotNull(postedAcceptance.getAcceptedAt());
	}

	@Test
	public void canUpdateCardAndData() {
		
	}

	@Test
	public void canUpdateAnOffer() {
		
	}

	@Test
	public void maximumOfferAcceptsAreEnforced() {
		
	}
	

	private Card createSampleCard(User user) {		
		// create a sample card
		Card card = new Card(user, "Foo");
		CardData data = new CardData(user, "Hello, ", "world!", card);

		// save the card
		cardDao.save(card);
		
		// add and save the card data and (re)save the card
		card.addData(data);
		cardDataDao.save(data);
		cardDao.save(card);

		return card;
	}

	
//	@Test
//	public void canSynchronizeReadOnlyEntity() throws Exception {
//		Achievement achievement = new Achievement("test-achievement", "This is for testing.",
//												  GeoSpatialUtils.createPoint(1.0D, 1.0D), 10);
//		achievementDao.save(achievement);
//		
//		// this calendar represents the last time that the device synchronized
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.HOUR, -2);
//		
//		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
//													  "1.0", 
//													  "0", 
//													  "", 
//													  super.getRequest(), 
//													  super.getSession());
//		
//		assertNotNull(mav);
//		
//		@SuppressWarnings("unchecked")
//		Map<String, List<Synchronizable>> result = (Map<String, List<Synchronizable>>) mav.getModel().get("entities");
//
//		assertNotNull(result);
//		assertEquals(1, result.size());
//		assertTrue(result.containsKey("achievements"));
//		
//		String json = super.resolveAndProcessView(mav);
//		LOGGER.info(json);
//		
//		super.assertIsValidJson(json);
//		super.assertJsonNodeChildCount(json, "achievements.entities", 1);
//		super.assertJsonNodeHasValue(json, "achievements.entities[0].name", "test-achievement");
//		super.assertJsonNodeHasValue(json, "achievements.entities[0].points", "10");		
//	}

//	@Test
//	public void canSynchronizeMultipleReadOnlyEntities() throws Exception {
//		Achievement climbedEverest = new Achievement("Summit Everest", "You climbed Everest!",
//												  	 GeoSpatialUtils.createPoint(1.0D, 1.0D), 100);
//		achievementDao.save(climbedEverest);
//
//		Achievement yosemiteSam = new Achievement("Yosemite Sam", "You visited Yosemite National Park!",
//			  	 								  GeoSpatialUtils.createPoint(1.0D, 1.0D), 10);
//		achievementDao.save(yosemiteSam);
//		
//		// this calendar represents the last time that the device synchronized
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.HOUR, -2);
//		
//		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
//													  "1.0", 
//													  "0", 
//													  "", 
//													  super.getRequest(), 
//													  super.getSession());
//		
//		assertNotNull(mav);
//		
//		@SuppressWarnings("unchecked")
//		Map<String, List<Synchronizable>> result = (Map<String, List<Synchronizable>>) mav.getModel().get("entities");
//
//		assertNotNull(result);
//		assertEquals(1, result.size());
//		assertTrue(result.containsKey("achievements"));
//		
//		String json = super.resolveAndProcessView(mav);
//		LOGGER.info(json);
//		
//		super.assertIsValidJson(json);
//		super.assertJsonNodeChildCount(json, "achievements.entities", 2);		
//	}

//	@Test
//	public void synchronizationCycleSyncsOutOfDateEntitiesOnly() throws Exception {
//		Achievement climbedEverest = new Achievement("Summit Everest", "You climbed Everest!",
//			  	 									 GeoSpatialUtils.createPoint(1.0D, 1.0D), 100);
//		achievementDao.save(climbedEverest);
//
//		Thread.sleep(10);	// sleep for 10 milliseconds before we create the second achievement
//		
//		Achievement yosemiteSam = new Achievement("Yosemite Sam", "You visited Yosemite National Park!",
//												  GeoSpatialUtils.createPoint(1.0D, 1.0D), 10);
//		achievementDao.save(yosemiteSam);
//
//		// set the last synchronization time to be 5 milliseconds ago - this will mean that the second
//		// achievement will be picked up in the synchronization cycle, but not the first
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.MILLISECOND, -5);
//		
//		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
//				  									  "1.0", 
//				  									  "0", 
//				  									  "", 
//				  									  super.getRequest(), 
//				  									  super.getSession());
//
//		assertNotNull(mav);
//
//		@SuppressWarnings("unchecked")
//		Map<String, List<Synchronizable>> result = (Map<String, List<Synchronizable>>) mav.getModel().get("entities");
//
//		assertNotNull(result);
//		assertEquals(1, result.size());
//		assertTrue(result.containsKey("achievements"));
//
//		String json = super.resolveAndProcessView(mav);
//		LOGGER.info(json);
//
//		super.assertIsValidJson(json);
//		super.assertJsonNodeChildCount(json, "achievements.entities", 1);
//		super.assertJsonNodeHasValue(json, "achievements.entities[0].name", "Yosemite Sam");
//		super.assertJsonNodeHasValue(json, "remainingPages", "0");
//		
//	}
	
//	@Test
//	public void synchronizationServiceSyncsOnlyUserOwnedEntities() throws Exception {
//		Achievement climbedEverest = new Achievement("Summit Everest", "You climbed Everest!",
//			  	 									 GeoSpatialUtils.createPoint(1.0D, 1.0D), 100);
//		achievementDao.save(climbedEverest);
//
//		// the default authenticated user has achieved the 'Summit Everest' achievement
//		EarnedAchievement everestForAuthUser = new EarnedAchievement(climbedEverest, GeoSpatialUtils.createPoint(2.0D, 2.0D), AUTHENTICATED_USER);
//		earnedAchievementDao.save(everestForAuthUser);
//		LOGGER.info("Expected earned achievement client ID: " + everestForAuthUser.getClientId());
//		
//		User anotherUser = new User("another-user", "password", "noone@nowhere.com");
//		userDao.save(anotherUser);
//
//		EarnedAchievement everestForOtherUser = new EarnedAchievement(climbedEverest, GeoSpatialUtils.createPoint(2.0D, 2.0D), anotherUser);
//		earnedAchievementDao.save(everestForOtherUser);
//		LOGGER.info("Earned achievement client ID for non-sync'ing: " + everestForOtherUser.getClientId());
//
//		// get a timestamp to simulate a client device that has not synchronized for the last two hours
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.HOUR, -2);
//		
//		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
//				  									  "1.0", 
//				  									  "0", 
//				  									  "", 
//				  									  super.getRequest(), 
//				  									  super.getSession());
//
//		assertNotNull(mav);
//
//		@SuppressWarnings("unchecked")
//		Map<String, List<Synchronizable>> result = (Map<String, List<Synchronizable>>) mav.getModel().get("entities");
//
//		assertNotNull(result);
//		assertEquals(2, result.size());
//		assertTrue(result.containsKey("achievements"));
//		assertTrue(result.containsKey("earnedAchievements"));
//
//		String json = super.resolveAndProcessView(mav);
//		LOGGER.info(json);
//		
//		super.assertJsonNodeChildCount(json, "achievements.entities", 1);
//		super.assertJsonNodeChildCount(json, "earnedAchievements.entities", 1);
//		super.assertJsonNodeHasValue(json, "earnedAchievements.entities[0].clientId", everestForAuthUser.getClientId());
//		super.assertJsonNodeHasValue(json, "remainingPages", "0");
//	
//	}
	
//	@Test
//	public void canSynchronizeRelatedEntities() throws Exception {
//		Achievement climbedEverest = new Achievement("Summit Everest", "You climbed Everest!",
//												  	 GeoSpatialUtils.createPoint(1.0D, 1.0D), 100);
//		achievementDao.save(climbedEverest);
//
//		EarnedAchievement everest = new EarnedAchievement(climbedEverest, GeoSpatialUtils.createPoint(2.0D, 2.0D), AUTHENTICATED_USER);
//		earnedAchievementDao.save(everest);
//		
//		// this calendar represents the last time that the device synchronized
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.HOUR, -2);
//		
//		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
//													  "1.0", 
//													  "0", 
//													  "", 
//													  super.getRequest(), 
//													  super.getSession());
//		
//		assertNotNull(mav);
//		
//		@SuppressWarnings("unchecked")
//		Map<String, List<Synchronizable>> result = (Map<String, List<Synchronizable>>) mav.getModel().get("entities");
//
//		assertNotNull(result);
//		assertEquals(2, result.size());
//		assertTrue(result.containsKey("achievements"));
//		assertTrue(result.containsKey("earnedAchievements"));
//		
//		String json = super.resolveAndProcessView(mav);
//		LOGGER.info(json);
//		
//		super.assertIsValidJson(json);
//		
//	}

//	@Test
//	public void canPagingWorksWhenSynchronizingEntities() throws Exception {
//		
//		// create one 'page' of achievements plus 3 'tail' achievements
//		for (int count = 0; count < SyncController.DEFAULT_PAGE_SIZE + 3; count++) {
//			Achievement achievement = new Achievement("test-achievement-" + count, "This is a test achievement.",
//													  GeoSpatialUtils.createPoint(1.0D, 1.0D), 100);
//			
//			achievementDao.save(achievement);
//		}
//				
//		// this calendar represents the last time that the device synchronized
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.HOUR, -2);
//		
//		// request the second page of achievements
//		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
//													  "1.0", 
//													  "1", 
//													  "", 
//													  super.getRequest(), 
//													  super.getSession());
//		
//		assertNotNull(mav);
//		
//		@SuppressWarnings("unchecked")
//		Map<String, List<Synchronizable>> result = (Map<String, List<Synchronizable>>) mav.getModel().get("entities");
//
//		assertNotNull(result);
//		assertEquals(1, result.size());
//		assertTrue(result.containsKey("achievements"));
//		
//		String json = super.resolveAndProcessView(mav);
//		LOGGER.info(json);
//		
//		super.assertIsValidJson(json);
//		super.assertJsonNodeChildCount(json, "achievements.entities", 3);
//		super.assertJsonNodeHasValue(json, "remainingPages", "1");
//		
//	}

//	@Test
//	public void canRetrieveMultipleSequentialPages() throws Exception {
//		
//		// create one 'page' of achievements plus 3 'tail' achievements
//		for (int count = 0; count < SyncController.DEFAULT_PAGE_SIZE + 3; count++) {
//			Achievement achievement = new Achievement("test-achievement-" + count, "This is a test achievement.",
//													  GeoSpatialUtils.createPoint(1.0D, 1.0D), 100);
//			
//			achievementDao.save(achievement);
//		}
//				
//		// this calendar represents the last time that the device synchronized
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.HOUR, -2);
//		
//		// request the first page of achievements
//		ModelAndView mav1 = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
//													   "1.0", 
//													   "0", 
//													   "", 
//													   super.getRequest(), 
//													   super.getSession());
//		
//		assertNotNull(mav1);
//		
//		@SuppressWarnings("unchecked")
//		Map<String, List<Synchronizable>> page1 = (Map<String, List<Synchronizable>>) mav1.getModel().get("entities");
//
//		assertNotNull(page1);
//		assertEquals(1, page1.size());
//		assertTrue(page1.containsKey("achievements"));
//		
//		String json1 = super.resolveAndProcessView(mav1);
//		LOGGER.info(json1);
//		
//		super.assertIsValidJson(json1);
//		super.assertJsonNodeChildCount(json1, "achievements.entities", 20);
//		super.assertJsonNodeHasValue(json1, "remainingPages", "1");
//		
//		// request the first page of achievements
//		String clientUpdatedTime = DEFAULT_DATE_FORMATTER.format( (Timestamp) mav1.getModel().get("updateTime") );
//		ModelAndView mav2 = syncController.synchronize(clientUpdatedTime, 
//													   "1.0", 
//													   "0", 
//													   "", 
//													   super.getRequest(), 
//													   super.getSession());
//
//		assertNotNull(mav2);
//		
//		@SuppressWarnings("unchecked")
//		Map<String, List<Synchronizable>> page2 = (Map<String, List<Synchronizable>>) mav1.getModel().get("entities");
//
//		assertNotNull(page2);
//		assertEquals(1, page2.size());
//		assertTrue(page2.containsKey("achievements"));
//
//		String json2 = super.resolveAndProcessView(mav2);
//		LOGGER.info(json2);
//		
//		super.assertIsValidJson(json2);
//		super.assertJsonNodeChildCount(json2, "achievements.entities", 3);
//		super.assertJsonNodeHasValue(json2, "remainingPages", "0");
//		
//	}

	// ****
	// These tests are for testing the client posting and synchronization processing capabilities.
	// ****
//	@Test
//	public void canPostNewEntitiesThatAreWriteable() throws Exception {
//		Achievement achievement = new Achievement("test-achievement", "This is a test achievement.",
//				  								  GeoSpatialUtils.createPoint(1.0D, 1.0D), 100);
//		achievement.setClientId("86afc579-cfd3-4369-9854-b75349822a7d");
//		achievementDao.save(achievement);
//
//		Thread.sleep(10);	// sleep for 10 milliseconds so that we don't pick this object up in the sync
//
//		StringBuffer payload = new StringBuffer();
//
//		// create the sample payload
//		payload.append("{ \"earnedAchievements\": [")
//			   .append("	{")
//			   .append("        \"achievement\": \"86afc579-cfd3-4369-9854-b75349822a7d\",")
//			   .append("        \"lastUpdated\": \"2012-09-20T12:27:32.694+0100\",")
//			   .append("        \"whenEarned\": \"2012-09-20T12:27:32.692+0100\",")
//			   .append("        \"whereEarned\": \"POINT (2 2)\",")
//			   .append("        \"clientVersion\": \"-1\",")
//			   .append("        \"deleted\": \"false\",")
//			   .append("        \"clientId\": \"b159b2dd-59c3-48c0-9c6d-e0c382f54439\"")
//			   .append("	}")
//			   .append("] }");
//			   
//		// this calendar represents the last time that the device synchronized
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.MILLISECOND, -5);		
//		
//		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
//													  "1.0", 
//													  "0", 
//													  payload.toString(), 
//													  super.getRequest(), 
//													  super.getSession());
//
//		assertNotNull(mav);
//	
//		String json = super.resolveAndProcessView(mav);
//		LOGGER.info(json);
//		
//		super.assertIsValidJson(json);
//		super.assertJsonNodeChildCount(json, "receipts.earnedAchievements", 1);
//		super.assertJsonNodeHasValue(json, "receipts.earnedAchievements[0].version", "0");
//	
//		// ensure that the new entity was flushed to the database
//		assertEquals(1, super.countRowsInTable("EarnedAchievement"));
//	}

//	@Test
//	public void canPostMultipleNewEntitiesThatAreWriteable() throws Exception {
//		Achievement achievement = new Achievement("test-achievement", "This is a test achievement.",
//				  								  GeoSpatialUtils.createPoint(1.0D, 1.0D), 100);
//		achievement.setClientId("86afc579-cfd3-4369-9854-b75349822a7d");
//		achievementDao.save(achievement);
//
//		Thread.sleep(10);	// sleep for 10 milliseconds so that we don't pick this object up in the sync
//
//		StringBuffer payload = new StringBuffer();
//
//		// create the sample payload
//		payload.append("{ \"earnedAchievements\": [")
//			   .append("	{")
//			   .append("        \"achievement\": \"86afc579-cfd3-4369-9854-b75349822a7d\",")
//			   .append("        \"lastUpdated\": \"2012-09-20T12:27:32.694+0100\",")
//			   .append("        \"whenEarned\": \"2012-09-20T12:27:32.692+0100\",")
//			   .append("        \"whereEarned\": \"POINT (2 2)\",")
//			   .append("        \"clientVersion\": \"-1\",")
//			   .append("        \"deleted\": \"false\",")
//			   .append("        \"clientId\": \"" + UUID.randomUUID().toString() + "\"")
//			   .append("	},")
//			   .append("	{")
//			   .append("        \"achievement\": \"86afc579-cfd3-4369-9854-b75349822a7d\",")
//			   .append("        \"lastUpdated\": \"2012-09-20T12:27:33.694+0100\",")
//			   .append("        \"whenEarned\": \"2012-09-20T12:27:33.692+0100\",")
//			   .append("        \"whereEarned\": \"POINT (3 3)\",")
//			   .append("        \"clientVersion\": \"-1\",")
//			   .append("        \"deleted\": \"false\",")
//			   .append("        \"clientId\": \"" + UUID.randomUUID().toString() + "\"")
//			   .append("	}")
//			   .append("] }");
//			   
//		// this calendar represents the last time that the device synchronized
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.MILLISECOND, -5);		
//		
//		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
//													  "1.0", 
//													  "0", 
//													  payload.toString(), 
//													  super.getRequest(), 
//													  super.getSession());
//
//		assertNotNull(mav);
//	
//		String json = super.resolveAndProcessView(mav);
//		LOGGER.info(json);
//		
//		super.assertIsValidJson(json);
//		super.assertJsonNodeChildCount(json, "receipts.earnedAchievements", 2);
//		super.assertJsonNodeHasValue(json, "receipts.earnedAchievements[0].version", "0");
//		super.assertJsonNodeHasValue(json, "receipts.earnedAchievements[1].version", "0");
//	
//		// ensure that the new entity was flushed to the database
//		assertEquals(2, super.countRowsInTable("EarnedAchievement"));
//	}

//	@Test
//	public void postedReadOnlyEntitiesAreNotUpdated() throws Exception {
//		StringBuffer payload = new StringBuffer();
//
//		// create the sample payload - this is incomplete but does not matter for this test as the
//		// synchronization service should not allow this entity type to be posted
//		payload.append("{ \"achievements\": [")
//			   .append("	{")
//			   .append("        \"lastUpdated\": \"2012-09-20T12:27:32.694+0100\",")
//			   .append("        \"clientVersion\": \"-1\",")
//			   .append("        \"deleted\": \"false\",")
//			   .append("        \"clientId\": \"" + UUID.randomUUID().toString() + "\"")
//			   .append("	}")
//			   .append("] }");
//		
//		// this calendar represents the last time that the device synchronized
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.MILLISECOND, -5);		
//		
//		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
//							  						  "1.0", 
//							  						  "0", 
//							  						  payload.toString(), 
//							  						  super.getRequest(), 
//							  						  super.getSession());
//		
//		assertNotNull(mav);
//		
//		String json = super.resolveAndProcessView(mav);
//		LOGGER.info(json);
//		
//		super.assertIsValidJson(json);
//		
//		// ensure that the new entity was flushed to the database
//		assertEquals(0, super.countRowsInTable("Achievement"));
//	}
	
//	@Test
//	public void writeableEntitiesWithLowerClientVersionAreNotUpdated() throws Exception {
//		Achievement achievement = new Achievement("test-achievement", "This is a test achievement.",
//				  								  GeoSpatialUtils.createPoint(1.0D, 1.0D), 100);
//		achievement.setClientId("86afc579-cfd3-4369-9854-b75349822a7d");
//		achievementDao.save(achievement);
//
//		// this earned achievement should not be over-written by the sync service in this test
//		EarnedAchievement earnedAchievement = new EarnedAchievement(achievement, GeoSpatialUtils.createPoint(2.0D, 2.0D), AUTHENTICATED_USER);
//		earnedAchievement.setClientVersion(2);
//		earnedAchievementDao.save(earnedAchievement);
//		
//		Thread.sleep(10);	// sleep for 10 milliseconds so that we don't pick this object up in the sync
//
//		StringBuffer payload = new StringBuffer();
//
//		// create the sample payload
//		payload.append("{ \"earnedAchievements\": [")
//			   .append("	{")
//			   .append("        \"achievement\": \"86afc579-cfd3-4369-9854-b75349822a7d\",")
//			   .append("        \"lastUpdated\": \"2012-09-20T12:27:32.694+0100\",")
//			   .append("        \"whenEarned\": \"2012-09-20T12:27:32.692+0100\",")
//			   .append("        \"whereEarned\": \"POINT (2 2)\",")
//			   .append("        \"clientVersion\": \"1\",")
//			   .append("        \"deleted\": \"true\",")
//			   .append("        \"clientId\": \"" + earnedAchievement.getClientId() + "\"")
//			   .append("	}")
//			   .append("] }");
//			   
//		// this calendar represents the last time that the device synchronized
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.MILLISECOND, -5);		
//		
//		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
//													  "1.0", 
//													  "0", 
//													  payload.toString(), 
//													  super.getRequest(), 
//													  super.getSession());
//
//		assertNotNull(mav);
//	
//		String json = super.resolveAndProcessView(mav);
//		LOGGER.info(json);
//		
//		super.assertIsValidJson(json);
//		super.assertJsonNodeChildCount(json, "receipts.earnedAchievements", 0);
//		
//		// even though the posted earned achievement set the deleted flag to 'true', it is out-of-date (client version is 1
//		// whereas the server version is 2) - the flag should therefore remain as 'false'
//		EarnedAchievement e = earnedAchievementDao.findByClientId(earnedAchievement.getClientId());
//		assertFalse(e.getDeleted());
//	}

//	@Test
//	public void writeableEntitiesWithEqualClientVersionAreNotUpdated() throws Exception {
//		Achievement achievement = new Achievement("test-achievement", "This is a test achievement.",
//				  								  GeoSpatialUtils.createPoint(1.0D, 1.0D), 100);
//		achievement.setClientId("86afc579-cfd3-4369-9854-b75349822a7d");
//		achievementDao.save(achievement);
//		
//		// this earned achievement should not be over-written by the sync service in this test
//		EarnedAchievement earnedAchievement = new EarnedAchievement(achievement, GeoSpatialUtils.createPoint(2.0D, 2.0D), AUTHENTICATED_USER);
//		earnedAchievement.setClientVersion(1);
//		earnedAchievementDao.save(earnedAchievement);
//		
//		Thread.sleep(10);	// sleep for 10 milliseconds so that we don't pick this object up in the sync
//		
//		StringBuffer payload = new StringBuffer();
//		
//		// create the sample payload
//		payload.append("{ \"earnedAchievements\": [")
//			   .append("	{")
//			   .append("        \"achievement\": \"86afc579-cfd3-4369-9854-b75349822a7d\",")
//			   .append("        \"lastUpdated\": \"2012-09-20T12:27:32.694+0100\",")
//			   .append("        \"whenEarned\": \"2012-09-20T12:27:32.692+0100\",")
//			   .append("        \"whereEarned\": \"POINT (2 2)\",")
//			   .append("        \"clientVersion\": \"1\",")
//			   .append("        \"deleted\": \"true\",")
//			   .append("        \"clientId\": \"" + earnedAchievement.getClientId() + "\"")
//			   .append("	}")
//			   .append("] }");
//		
//		// this calendar represents the last time that the device synchronized
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.MILLISECOND, -5);		
//		
//		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
//							  						  "1.0", 
//							  						  "0", 
//							  						  payload.toString(), 
//							  						  super.getRequest(), 
//							  						  super.getSession());
//		
//		assertNotNull(mav);
//		
//		String json = super.resolveAndProcessView(mav);
//		LOGGER.info(json);
//		
//		super.assertIsValidJson(json);
//		super.assertJsonNodeChildCount(json, "receipts.earnedAchievements", 0);
//		
//		// even though the posted earned achievement set the deleted flag to 'true', it is out-of-date (client version is 1
//		// and the server version is 1) - the flag should therefore remain as 'false'
//		EarnedAchievement e = earnedAchievementDao.findByClientId(earnedAchievement.getClientId());
//		assertFalse(e.getDeleted());
//	}
	
//	@Test
//	public void writeableEntitiesWithHigherClientVersionAreUpdatedd() throws Exception {
//		Achievement achievement = new Achievement("test-achievement", "This is a test achievement.",
//				  								  GeoSpatialUtils.createPoint(1.0D, 1.0D), 100);
//		achievement.setClientId("86afc579-cfd3-4369-9854-b75349822a7d");
//		achievementDao.save(achievement);
//		
//		// this earned achievement should not be over-written by the sync service in this test
//		EarnedAchievement earnedAchievement = new EarnedAchievement(achievement, GeoSpatialUtils.createPoint(2.0D, 2.0D), AUTHENTICATED_USER);
//		earnedAchievement.setClientVersion(1);
//		earnedAchievementDao.save(earnedAchievement);
//		
//		Thread.sleep(10);	// sleep for 10 milliseconds so that we don't pick this object up in the sync
//		
//		StringBuffer payload = new StringBuffer();
//		
//		// create the sample payload
//		payload.append("{ \"earnedAchievements\": [")
//			   .append("	{")
//			   .append("        \"achievement\": \"86afc579-cfd3-4369-9854-b75349822a7d\",")
//			   .append("        \"lastUpdated\": \"2012-09-20T12:27:32.694+0100\",")
//			   .append("        \"whenEarned\": \"2012-09-20T12:27:32.692+0100\",")
//			   .append("        \"whereEarned\": \"POINT (2 2)\",")
//			   .append("        \"clientVersion\": \"2\",")
//			   .append("        \"deleted\": \"true\",")
//			   .append("        \"clientId\": \"" + earnedAchievement.getClientId() + "\"")
//			   .append("	}")
//			   .append("] }");
//		
//		// this calendar represents the last time that the device synchronized
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.MILLISECOND, -5);		
//		
//		ModelAndView mav = syncController.synchronize(DEFAULT_DATE_FORMATTER.format(c.getTime()), 
//							  						  "1.0", 
//							  						  "0", 
//							  						  payload.toString(), 
//							  						  super.getRequest(), 
//							  						  super.getSession());
//		
//		assertNotNull(mav);
//		
//		String json = super.resolveAndProcessView(mav);
//		LOGGER.info(json);
//		
//		super.assertIsValidJson(json);
//		super.assertJsonNodeChildCount(json, "receipts.earnedAchievements", 1);
//		super.assertJsonNodeHasValue(json, "receipts.earnedAchievements[0].version", "2");
//
//		// in this case the client has posted an earned achievement that is of a higher version that the server
//		// ensure that the update to the deleted flag as taken effect
//		EarnedAchievement e = earnedAchievementDao.findByClientId(earnedAchievement.getClientId());
//		assertTrue(e.getDeleted());
//		assertEquals((Integer)2, e.getClientVersion());
//	}

}

package com.luminos.woosh.synchronization.service.s3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.utils.ServiceUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.luminos.woosh.domain.common.RemoteBinaryObject;
import com.luminos.woosh.synchronization.service.CloudServiceProxy;

/**
 * 
 * @author Ben
 */
@Service
public class S3ServiceProxyImpl implements CloudServiceProxy {

	private static final Logger LOGGER = Logger.getLogger(S3ServiceProxyImpl.class);
	
	
	private static final Integer URL_EXPIRY_MINS = 20;								// the default S3 signed URL expiry time (in minutes)
	
	private static final String DEFAULT_BUCKET_LOCATION = "ap-southeast-1";			// Singapore 
	
	
	private AWSCredentials AWS_CREDENTIALS = null;
	
	private S3Service S3_SERVICE_PROXY = null;

	
	@Value("#{s3Properties['s3Service.awsAccessKey']}")
	private String awsAccessKey = null;
	
	@Value("#{s3Properties['s3Service.awsSecretKey']}")
	private String awsSecretKey = null;
	
	@Value("#{s3Properties['s3Service.awsBucketName']}")
	private String bucketName = null;
	
	
	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		
		if (StringUtils.isBlank(awsAccessKey)) return;	

		AWS_CREDENTIALS = new AWSCredentials(this.awsAccessKey, this.awsSecretKey);

		try {
			S3_SERVICE_PROXY = new RestS3Service(AWS_CREDENTIALS);
			
			// this call forces an initial authentication and key pass with the S3 servers
			S3_SERVICE_PROXY.getOrCreateBucket(this.bucketName, DEFAULT_BUCKET_LOCATION);
			
			LOGGER.info("Successfully logged into SiFourteen S3 account.");
			
		} catch (S3ServiceException ex) {
			LOGGER.info("S3 error code: " + ex.getS3ErrorCode());
			LOGGER.info("S3 error message: " + ex.getS3ErrorMessage());
			throw new RuntimeException(ex);
		}		
	}

	
	public void upload(RemoteBinaryObject metadata, byte[] data) {
		
		if (StringUtils.isBlank(awsAccessKey)) return;	

		if (metadata.getRemoteId() == null) {
			throw new RuntimeException("Attempt to upload NULL object to S3 - aborting operation!");
		}
		
		if (data == null) {
			throw new RuntimeException("Attempt to upload NULL object to S3 - aborting operation!");
		}
				
		S3Object binaryObject = new S3Object(metadata.getS3Name());
		ByteArrayInputStream imageInputStream = new ByteArrayInputStream(data);
		byte[] md5Hash = null;

		// compute the hash for the object that we are going to upload
		try {
			md5Hash = ServiceUtils.computeMD5Hash(imageInputStream);
			imageInputStream.reset();
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		// construct the S3 image object
		binaryObject.setDataInputStream(imageInputStream);
		binaryObject.setContentLength(imageInputStream.available());
//		binaryObject.setContentType(metadata.getType().getMimeType());
		binaryObject.setMd5Hash(md5Hash);
		
		// upload the image to the S3 bucket for the company
		try {
			
			// upload the image data
			S3_SERVICE_PROXY.putObject(this.bucketName, binaryObject);
			
			LOGGER.info("Uploaded object to S3 - " + metadata.getRemoteId());
			
		} catch (S3ServiceException ex) {
			LOGGER.info("S3 error code: " + ex.getS3ErrorCode());
			LOGGER.info("S3 error message: " + ex.getS3ErrorMessage());
			throw new RuntimeException(ex);
		}
	}

	public byte[] get(RemoteBinaryObject metadata) {

		if (StringUtils.isBlank(awsAccessKey)) return null;

		if (metadata == null) {
			throw new RuntimeException("Attempt to get NULL object from S3 - aborting operation!");
		}

		if (metadata.getRemoteId() == null) {
			throw new RuntimeException("Attempt to get NULL object from S3 - aborting operation!");
		}
		
		try {
			S3Object binaryData = S3_SERVICE_PROXY.getObject(this.bucketName, metadata.getS3Name());

			LOGGER.info("Downloaded S3 object - " + metadata.getRemoteId());
			
			return ServiceUtils.readInputStreamToBytes(binaryData.getDataInputStream());

		} catch (S3ServiceException ex) {
			LOGGER.info("S3 error code: " + ex.getS3ErrorCode());
			LOGGER.info("S3 error message: " + ex.getS3ErrorMessage());
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (ServiceException ex) {
			throw new RuntimeException(ex);
		}

	}

	public void delete(RemoteBinaryObject metadata) {
		
		if (StringUtils.isBlank(awsAccessKey)) return;

		if (metadata == null) {
			throw new RuntimeException("Attempt to delete NULL object from S3 - aborting operation!");
		}

		if (metadata.getRemoteId() == null) {
			throw new RuntimeException("Attempt to delete NULL object from S3 - aborting operation!");
		}

		try {
			
			S3_SERVICE_PROXY.deleteObject(this.bucketName, metadata.getS3Name());
			LOGGER.info("Deleted S3 object - " + metadata.getRemoteId());
			
		} catch (ServiceException ex) {
			throw new RuntimeException(ex);
		}
		
	}

	public String createSignedUrl(RemoteBinaryObject metadata) {

		if (StringUtils.isBlank(awsAccessKey)) return null;	

		if (metadata == null) {
			throw new RuntimeException("Attempt to create timed URL for NULL object - aborting operation!");
		}

		if (metadata.getRemoteId() == null) {
			throw new RuntimeException("Attempt to create timed URL for NULL object - aborting operation!");
		}

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, URL_EXPIRY_MINS);
		
		try {
			LOGGER.info("Created signed URL - " + metadata.getRemoteId() + " - expiry in " + URL_EXPIRY_MINS + " minutes.");
			
			return S3_SERVICE_PROXY.createSignedGetUrl(this.bucketName, metadata.getS3Name(), calendar.getTime());
		} catch (S3ServiceException ex) {
			LOGGER.info("S3 error code: " + ex.getS3ErrorCode());
			LOGGER.info("S3 error message: " + ex.getS3ErrorMessage());
			throw new RuntimeException(ex);
		}
		
	}

	public String createSignedUrl(RemoteBinaryObject metadata, Integer minutes) {

		if (StringUtils.isBlank(awsAccessKey)) return null;	

		if (metadata.getRemoteId() == null) {
			throw new RuntimeException("Attempt to create timed URL for NULL object - aborting operation!");
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, minutes);
		
		try {
			LOGGER.info("Created signed URL - " + metadata.getRemoteId() + " - expiry in " + minutes + " minutes.");
			
			return S3_SERVICE_PROXY.createSignedGetUrl(this.bucketName, metadata.getS3Name(), calendar.getTime());
		} catch (S3ServiceException ex) {
			LOGGER.info("S3 error code: " + ex.getS3ErrorCode());
			LOGGER.info("S3 error message: " + ex.getS3ErrorMessage());
			throw new RuntimeException(ex);
		}

	}

}

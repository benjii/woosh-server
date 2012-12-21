package com.luminos.woosh.beans;

/**
 * Utility bean for serializing a receipt back to a client that has posted a new entity to Woosh.
 * 
 * @author Ben
 */
public class Receipt {

	private String id = null;
	
	private ReceiptStatus status = ReceiptStatus.OK;
	
	
	public Receipt(String id) {
		this.id = id;
	}

	public Receipt(String id, ReceiptStatus status) {
		this.id = id;
		this.status = status;
	}


	public String getId() {
		return id;
	}

	public ReceiptStatus getStatus() {
		return status;
	}


	private enum ReceiptStatus {
		OK,
		FAILED;
	}
}

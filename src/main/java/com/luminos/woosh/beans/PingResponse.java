package com.luminos.woosh.beans;

/**
 * 
 * @author Ben
 */
public class PingResponse {

	private String status = null;
	
	private String serverTime = null;
	
	private Integer remainingUserSlots = null;
	
	private String motd = null;

	
	public PingResponse() {
		
	}


	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getServerTime() {
		return serverTime;
	}

	public void setServerTime(String serverTime) {
		this.serverTime = serverTime;
	}

	public Integer getRemainingUserSlots() {
		return remainingUserSlots;
	}

	public void setRemainingUserSlots(Integer remainingUserSlots) {
		this.remainingUserSlots = remainingUserSlots;
	}

	public String getMotd() {
		return motd;
	}

	public void setMotd(String motd) {
		this.motd = motd;
	}
	
}

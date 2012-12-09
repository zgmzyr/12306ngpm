package org.ng12306.tpms.runtime;

import java.util.UUID;

public class PlanTicket extends Entity {
	
	private int _startStop;

	public int getStartStop() {
		return this._startStop;
	}

	public void setStartStop(int value) {
		this._startStop = value;
	}
	
	private int _endStop;

	public int getEndStop() {
		return this._endStop;
	}

	public void setEndStop(int value) {
		this._endStop = value;
	}

	private UUID _orignalId;

	public UUID getOrignalId() {
		return this._orignalId;
	}

	public void setOrginalId(UUID value) {
		this._orignalId = value;
	}

	

	

}

package org.ng12306.tpms.runtime;

import java.util.UUID;

public class SeatType extends Entity {

	
	public SeatType() {
		
	}
	
	public SeatType(UUID id, String name, long code) {
		this.setId(id);
		this._name = name;
		this._code = code;
	}
	
	private String _name;

	public String getName() {
		return this._name;
	}

	public void setName(String value) {
		this._name = value;
	}

	private long _code;

	public long getCode() {
		return this._code;
	}

	public void setCode(long value) {
		this._code = value;
	}

	
}

package org.ng12306.tpms.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class CarType extends Entity {

	public CarType() {
		
	}
	
	
	public CarType(UUID id, String name, long seatType) {
		this.setId(id);
		this.setName(name);
		this.setSeatType(seatType);
	}
	
	private String _name;

	public String getName() {
		return this._name;
	}

	public void setName(String value) {
		this._name = value;
	}

	private long _seatType;

	public long getSeatType() {
		return this._seatType;
	}

	public void setSeatType(long value) {
		this._seatType = value;
	}
	
	public Collection<Seat> getSeats() {
		return _seats;
	}

	private ArrayList<Seat> _seats = new ArrayList<Seat>();
	

	
	
}

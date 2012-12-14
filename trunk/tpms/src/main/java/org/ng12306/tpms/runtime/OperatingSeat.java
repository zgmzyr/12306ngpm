package org.ng12306.tpms.runtime;

public class OperatingSeat extends Entity {

	private long _seatType;

	public long getSeatType() {
		return this._seatType;
	}

	public void setSeatType(long value) {
		this._seatType = value;
	}

	private String _seatNumber;

	public String getSeatNumber() {
		return this._seatNumber;
	}

	public void setSeatNumber(String value) {
		this._seatNumber = value;
	}
	
	private int _sequence;

	public int getSequence() {
		return this._sequence;
	}

	public void setSequence(int value) {
		this._sequence = value;
	}
	
	private Car _car;

	public Car getCar() {
		return this._car;
	}

	public void setCar(Car value) {
		this._car = value;
	}

	

	

	
}

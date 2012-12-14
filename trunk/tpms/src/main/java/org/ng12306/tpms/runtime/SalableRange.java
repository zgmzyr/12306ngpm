package org.ng12306.tpms.runtime;

public class SalableRange {

	private int _departureStart;

	public int getDepartureStart() {
		return this._departureStart;
	}

	public void setDepartureStart(int value) {
		this._departureStart = value;
	}

	private int _departureEnd;

	public int getDepartureEnd() {
		return this._departureEnd;
	}

	public void setDepartureEnd(int value) {
		this._departureEnd = value;
	}

	

	private int _destinationStart;

	public int getDestinationStart() {
		return this._destinationStart;
	}

	public void setDestinationStart(int value) {
		this._destinationStart = value;
	}

	private int _destinationEnd;

	public int getDestinationEnd() {
		return this._destinationEnd;
	}

	public void setDestinationEnd(int value) {
		this._destinationEnd = value;
	}
	
	public void copyFrom(SalableRange other)
	{
		this._departureStart = other._departureStart;
		this._departureEnd = other._departureEnd;
		this._destinationStart = other._destinationStart;
		this._destinationEnd = other._destinationEnd;
	}

	
	
}

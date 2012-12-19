package org.ng12306.tpms.runtime;

import org.joda.time.*;

public class RouteStop extends Entity {

	private int _sequence;

	public int getSequence() {
		return this._sequence;
	}

	public void setSequence(int value) {
		this._sequence = value;
	}
	
    private Station _station;

	public Station getStation() {
		return this._station;
	}

	public void setStation(Station value) {
		this._station = value;
	}

	

	private Period _arrivalTime;

	public Period getArrivalTime() {
		return this._arrivalTime;
	}

	public void setArrivalTime(Period value) {
		this._arrivalTime = value;
	}

	private Period _departureTime;

	public Period getDepartureTime() {
		return this._departureTime;
	}

	public void setDepartureTime(Period value) {
		this._departureTime = value;
	}
	
	
	

	
		
}

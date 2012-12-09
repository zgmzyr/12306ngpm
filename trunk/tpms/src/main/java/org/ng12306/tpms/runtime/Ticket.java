package org.ng12306.tpms.runtime;

public class Ticket extends Entity {

	private String _trainNumber;

	public String getTrainNumber() {
		return this._trainNumber;
	}

	public void setTrainNumber(String value) {
		this._trainNumber = value;
	}

	private String _departureStation;

	public String getname() {
		return this._departureStation;
	}

	public void setname(String value) {
		this._departureStation = value;
	}

	

	private String _destinationStation;

	public String getDestination() {
		return this._destinationStation;
	}

	public void setDestination(String value) {
		this._destinationStation = value;
	}

	
	
}

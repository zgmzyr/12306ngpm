package org.ng12306.tpms.runtime;

import org.joda.time.LocalDate;

public class Ticket extends Entity {

	private String _trainNumber;

	public String getTrainNumber() {
		return this._trainNumber;
	}

	public void setTrainNumber(String value) {
		this._trainNumber = value;
	}

	private String _departureStation;

	public String getDepartureStation() {
		return this._departureStation;
	}

	public void setDepartureStation(String value) {
		this._departureStation = value;
	}

	private String _destinationStation;

	public String getDestinationStation() {
		return this._destinationStation;
	}

	public void setDestinationStation(String value) {
		this._destinationStation = value;
	}
	
	private String _car;

	public String getCar() {
		return this._car;
	}

	public void setCar(String value) {
		this._car = value;
	}
	

	private String _seatNumber;

	public String getSeatNumber() {
		return this._seatNumber;
	}
	
	
	public void setSeatNumber(String value) {
		this._seatNumber = value;
	}
	
	private String _seatType;

	public String getSeatType() {
		return this._seatType;
	}

	public void setSeatType(String value) {
		this._seatType = value;
	}

	private LocalDate _departureDate;

	public LocalDate getDepartureDate() {
		return this._departureDate;
	}

	public void setDepartureDate(LocalDate value) {
		this._departureDate = value;
	}

	

	

	
	
}

package org.ng12306.tpms.runtime;

import java.io.Serializable;

import org.joda.time.LocalDate;

public class TicketQueryArgs implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7163070002185653449L;
	
	private long _sequence;

	public long getSequence() {
		return this._sequence;
	}

	public void setSequence(long value) {
		this._sequence = value;
	}
	
	private String _trainNumber;

	public String getTrainNumber() {
		return this._trainNumber;
	}

	public void setTrainNumber(String value) {
		this._trainNumber = value;
	}

	private LocalDate _date;

	public LocalDate getDate() {
		return this._date;
	}

	public void setDate(LocalDate value) {
		this._date = value;
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

	
	private long _seatType;

	public long getSeatType() {
		return this._seatType;
	}

	public void setSeatType(long value) {
		this._seatType = value;
	}

	private int _count;

	public int getCount() {
		return this._count;
	}

	public void setCount(int value) {
		this._count = value;
	}
	
	

	private TicketQueryAction _action;

	public TicketQueryAction getAction() {
		return this._action;
	}

	public void setAction(TicketQueryAction value) {
		this._action = value;
	}

	public void copyTo(TicketQueryArgs other)
	{
		other._action = this._action;
		other._count = this._count;
		other._date = this._date;
		other._departureStation = this._departureStation;
		other._destinationStation = this._destinationStation;
		other._seatType = this._seatType;
		other._sequence = this._sequence;
		other._trainNumber = this._trainNumber;
		
	}
	
	
}

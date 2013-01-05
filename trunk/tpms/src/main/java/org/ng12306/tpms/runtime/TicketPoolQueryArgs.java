package org.ng12306.tpms.runtime;

public class TicketPoolQueryArgs {

	private int _departureStop;

	public int getDepartureStop() {
		return this._departureStop;
	}

	public void setDepartureStop(int value) {
		this._departureStop = value;
	}

	private int _destinationStop;

	public int getDestinationStop() {
		return this._destinationStop;
	}

	public void setDestinationStop(int value) {
		this._destinationStop = value;
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
	
	
	

	
}

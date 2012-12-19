package org.ng12306.tpms.runtime;

public class TicketPoolTicket {
   
	
	
	
	private OperatingSeat _seat;

	public OperatingSeat getSeat() {
		return this._seat;
	}

	public void setSeat(OperatingSeat value) {
		this._seat = value;
	}

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
	
    private ITicketPool _pool;

	public ITicketPool getPool() {
		return this._pool;
	}

	public void setPool(ITicketPool value) {
		this._pool = value;
	}

	

	
	
}

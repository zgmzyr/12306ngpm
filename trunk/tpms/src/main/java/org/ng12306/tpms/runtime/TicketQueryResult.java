package org.ng12306.tpms.runtime;

import java.io.Serializable;

public class TicketQueryResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4496121679957168355L;
    private Boolean _hasTicket = new Boolean(false);

	public Boolean getHasTicket() {
		return this._hasTicket;
	}

	public void setHasTicket(Boolean value) {
		this._hasTicket = value;
	}
	
	private long _sequence;

	public long getSequence() {
		return this._sequence;
	}

	public void setSequence(long value) {
		this._sequence = value;
	}

	private Ticket[] _tickets;

	public Ticket[] getTickets() {
		return this._tickets;
	}

	public void setTickets(Ticket[] value) {
		this._tickets = value;
	}

	

	
}

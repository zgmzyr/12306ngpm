package org.ng12306.tpms.runtime;

public interface ITicketPool {

	boolean hasTickets(TicketQueryArgs args) throws Exception;
	Ticket[] book(TicketQueryArgs args) throws Exception; 
	void initialize() throws Exception;
}



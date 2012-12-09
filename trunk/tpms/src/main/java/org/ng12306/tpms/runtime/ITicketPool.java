package org.ng12306.tpms.runtime;

public interface ITicketPool {

	boolean hasTickets(TicketQueryArgs args);
	Ticket[] book(TicketQueryArgs args); 
	void initialize() throws Exception;
}



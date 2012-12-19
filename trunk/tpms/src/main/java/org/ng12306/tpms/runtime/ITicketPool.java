package org.ng12306.tpms.runtime;

public interface ITicketPool {

	boolean hasTickets(TicketPoolQueryArgs args) throws Exception;
	
	TicketPoolTicket[] book(TicketPoolQueryArgs args) throws Exception; 
	
	TicketPoolQueryArgs toTicketPoolQueryArgs(TicketQueryArgs args) throws Exception;
	
	Ticket[] toTicket(TicketPoolTicket[] poolTickets) throws Exception;
	
	Boolean getIsSoldOut() throws Exception;

	void initialize() throws Exception;
}



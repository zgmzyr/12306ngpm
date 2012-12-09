package org.ng12306.tpms.runtime;

public interface ITicketPoolManager {
	boolean hasTicket(TicketQueryArgs args);
	Ticket[] book(TicketQueryArgs args); 
	
	void addPool(ITicketPool pool);
	
	void removePool(ITicketPool pool);

}

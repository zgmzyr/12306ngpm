package org.ng12306.tpms.runtime;

public interface ITicketPoolManager {
	boolean hasTicket(TicketQueryArgs args) throws Exception;
	Ticket[] book(TicketQueryArgs args) throws Exception;; 
	
	void addPool(ITicketPool pool) throws Exception;;
	
	void removePool(ITicketPool pool) throws Exception;;

}

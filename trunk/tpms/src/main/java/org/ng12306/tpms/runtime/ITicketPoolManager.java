package org.ng12306.tpms.runtime;

public interface ITicketPoolManager {
	
	
	ITicketPool getPool(TicketQueryArgs args);
	
	void addPool(ITicketPool pool) throws Exception;;
	
	void removePool(ITicketPool pool) throws Exception;;

}

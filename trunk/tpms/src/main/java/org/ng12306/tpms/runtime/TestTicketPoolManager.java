package org.ng12306.tpms.runtime;

import java.util.Hashtable;
import java.util.UUID;

import org.joda.time.LocalDate;

public class TestTicketPoolManager extends ServiceBase 
    implements ITicketPoolManager
{
	
	
	@Override
	public void initializeService() throws Exception 
	{
		
		IRailwayRepository repo = this.getSite().getRequiredService(IRailwayRepository.class);
		
		LocalDate tom = new LocalDate().plusDays(1);
		LocalDate dat = tom.plusDays(1);
		
		int flag = 1;
		
		for(TrainNumber tn : repo.getTrainNumbers())
		{
			LocalDate date = flag > 0 ? tom : dat;
			flag *=-1;
			
		    Train train = new Train();
		    train.setId(UUID.randomUUID());
		    train.setDepartureDate(date);
		    train.setTrainNumber(tn);
		    
		    TestTicketPool pool = new TestTicketPool(train);
		    pool.setSite(this.getSite());
		    pool.initialize();
		    
		    String key = tn.getName() + "|" + date.toString(); 

		    this._pools.put(key,  pool);
		    
		}
		
		
	
		
		super.initializeService();
	};
	
	private Hashtable<String, ITicketPool> _pools = new Hashtable<String, ITicketPool>();
	

	private ITicketPool getPool(TicketQueryArgs args)
	{
	    String key = args.getTrainNumber() + "|" + args.getDate().toString();
	    return this._pools.get(key);
	}
	
	@Override
	public boolean hasTicket(TicketQueryArgs args) throws Exception {
		ITicketPool pool = this.getPool(args);
		
		return pool != null ? pool.hasTickets(args) : false;
	}

	@Override
	public Ticket[] book(TicketQueryArgs args) throws Exception {
		ITicketPool pool = this.getPool(args);
		return pool != null ? pool.book(args) : noTickets;
	}
	
	private static Ticket[] noTickets = new Ticket[]{}; 

	@Override
	public void addPool(ITicketPool pool) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void removePool(ITicketPool pool) {
		throw new UnsupportedOperationException();
		
	}

}

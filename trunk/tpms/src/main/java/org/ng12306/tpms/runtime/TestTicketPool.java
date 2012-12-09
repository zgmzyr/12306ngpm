package org.ng12306.tpms.runtime;

public class TestTicketPool extends ObjectWithSite 
    implements ITicketPool
{

	public TestTicketPool(Train train) {
		this._train = train;
	}
	
	
	
	
	private Train _train;

	public Train getTrain() {
		return this._train;
	}

	@Override
	public void initialize() throws Exception
	{
		IRailwayRepository repo = this.getSite().getRequiredService(IRailwayRepository.class);
		
		
	}
	
	
	private TicketPoolQueryArgs toPoolQueryArgs(TicketQueryArgs args)
	{
		
	}
	

	@Override
	public boolean hasTickets(TicketQueryArgs args) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Ticket[] book(TicketQueryArgs args) {
		// TODO Auto-generated method stub
		return null;
	}
	
}

package org.ng12306.tpms.runtime;

import org.diting.collections.*;

public class TicketSalableRangeFilter extends ObjectWithSite
    implements IPlanTicketFilter 
{

	@Override
	public Iterable<PlanTicket> filter(Iterable<PlanTicket> source,
			final TicketPoolQueryArgs args) throws Exception {
		return Queries.query(source).where(new Predicate<PlanTicket>(){

			@Override
			public boolean evaluate(PlanTicket ticket) throws Exception {
				SalableRange range =  ticket.getSalableRange();
				
				return range.getDepartureStart() <= args.getDestinationStop() && args.getDepartureStop() <= range.getDepartureEnd()
						&& range.getDestinationStart() <= ticket.getEndStop() && ticket.getEndStop() <= range.getDestinationEnd();
				
			}});
	}
	
	
	

}

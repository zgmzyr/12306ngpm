package org.ng12306.tpms.runtime;

import org.diting.collections.*;

public class TicketSeatTypeFilter extends ObjectWithSite
   implements IPlanTicketFilter
{

	@Override
	public Iterable<PlanTicket> filter(Iterable<PlanTicket> source,
			final TicketPoolQueryArgs args) throws Exception {
		return Queries.query(source).where(new Predicate<PlanTicket>(){

			@Override
			public boolean evaluate(PlanTicket ticket) throws Exception {
				
				long t1 = ticket.getSeat().getSeatType();
				long t2 = args.getSeatType();
				
				return (t1 & t2) != 0;
				
			}});
	}

}

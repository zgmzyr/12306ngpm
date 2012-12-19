package org.ng12306.tpms.runtime;

import java.util.*;

public class TicketCountFilter extends ObjectWithSite
    implements IPlanTicketFilter
{

	@Override
	public Iterable<PlanTicket> filter(Iterable<PlanTicket> source,
			TicketPoolQueryArgs args) {
		ArrayList<PlanTicket> rs = new ArrayList<PlanTicket>(args.getCount());
		Iterator<PlanTicket> iter = source.iterator();
		for(int i = 0; i < args.getCount() && iter.hasNext(); i ++)
		{
		    rs.add(iter.next()); 	
		}
		
		return rs.size() == args.getCount() ? rs : _empty;
		
		
	}
	
	private static ArrayList<PlanTicket> _empty = new ArrayList<PlanTicket>(0);

    
}

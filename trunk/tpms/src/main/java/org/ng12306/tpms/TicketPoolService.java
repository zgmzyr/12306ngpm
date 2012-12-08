package org.ng12306.tpms;

import com.lmax.disruptor.*;

public class TicketPoolService {
    public final static EventFactory<TicketQueryEvent> QueryFactory =
	new EventFactory<TicketQueryEvent>() {
	public TicketQueryEvent newInstance() {
	    return new TicketQueryEvent();
	}
    };

    public final static EventFactory<TicketQueryResultEvent> 
	QueryResultFactory =
	new EventFactory<TicketQueryResultEvent>() {
	public TicketQueryResultEvent newInstance() {
	    return new TicketQueryResultEvent();
	}
    };
}

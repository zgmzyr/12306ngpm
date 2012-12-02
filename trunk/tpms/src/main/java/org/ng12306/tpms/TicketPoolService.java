package org.ng12306.tpms;

import com.lmax.disruptor.*;

public class TicketPoolService {
    public final static EventFactory<TicketEvent> INSTANCE =
	new EventFactory<TicketEvent>() {
	public TicketEvent newInstance() {
	    return new TicketEvent(TicketEventType.QueryByTrain);
	}
    };
}

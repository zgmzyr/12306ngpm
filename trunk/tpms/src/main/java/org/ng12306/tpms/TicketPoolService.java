package org.ng12306.tpms;

import com.lmax.disruptor.*;
import org.ng12306.tpms.runtime.TicketQueryArgs;

public class TicketPoolService {
    public final static EventFactory<TicketQueryArgs> QueryFactory =
	new EventFactory<TicketQueryArgs>() {
	public TicketQueryArgs newInstance() {
	    return new TicketQueryArgs();
	}
    };
}

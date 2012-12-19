package org.ng12306.tpms;

public class TicketQueryResultEvent extends TicketEvent
{
    // 查询到的车次信息
    public Train[] trains;
    
    public TicketQueryResultEvent() {
	super(TicketEventType.QueryResult);
    }
}

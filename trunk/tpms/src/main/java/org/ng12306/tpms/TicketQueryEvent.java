package org.ng12306.tpms;

import java.util.Date;

public class TicketQueryEvent extends TicketEvent
{
    // 车次号
    public String trainId;
    // 要查询的起始日期
    public Date startDate;
    // 要查询的终止日期
    public Date endDate;

    public TicketQueryEvent() {
	super(TicketEventType.QueryByTrain);
    }
}

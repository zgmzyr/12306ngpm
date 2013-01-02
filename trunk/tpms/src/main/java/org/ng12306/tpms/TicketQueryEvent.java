package org.ng12306.tpms;

// 将日期全部转到joda-time库里，java自带的日期类型太难用了！
import org.joda.time.DateTime;

// 根据车次查询余票的事件
public class TicketQueryEvent extends TicketEvent
{
    // 车次号
    public String trainId;
    // 要查询的起始日期
    public DateTime startDate;
    // 要查询的终止日期
    public DateTime endDate;

    public TicketQueryEvent() {
	super(TicketEventType.QueryByTrain);
    }
}

package org.ng12306.tpms.runtime;

public interface IPlanTicketFilter {
    Iterable<PlanTicket> filter(Iterable<PlanTicket> source, TicketPoolQueryArgs args) throws Exception;
}

package org.ng12306.tpms;


import junit.framework.Assert;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.ng12306.tpms.runtime.*;


public class TestTicketPoolManagerTest {

	
	@Test
	public void testBook() throws Exception {
		/*ServiceManager.getServices().initializeServices(new Object[] {new TestRailwayRepository(), new TestTicketPoolManager()});
		
		IRailwayRepository repo = ServiceManager.getServices().getRequiredService(IRailwayRepository.class);
		
		ITicketPoolManager tpm = ServiceManager.getServices().getRequiredService(ITicketPoolManager.class);
		
		TicketQueryArgs query = new TicketQueryArgs();
		query.setCount(1);
		query.setDate(new LocalDate());
		query.setSeatType(-1);
		query.setTrainNumberId("G101");
		query.setDepartureStation("北京南");
		query.setDestinationStation("南京南");
		
		
		
		Ticket[] tickets = tpm.book(query);
		Assert.assertEquals(1, tickets.length);
		
		Ticket ticket = tickets[0];
		
		Assert.assertEquals("G101", ticket.getTrainNumber());
		Assert.assertEquals("北京南", ticket.getDepartureStation());
		Assert.assertEquals("南京南", ticket.getDestinationStation());
		Assert.assertEquals(new LocalDate(), ticket.getDepartureDate());*/
	
		
	}
	
	

}

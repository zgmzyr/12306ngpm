package org.ng12306.tpms;

import java.util.*;

import junit.framework.Assert;

import org.diting.collections.*;

import org.joda.time.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ng12306.tpms.runtime.*;

public class TestTicketPoolTest {

	
	 @BeforeClass
	 public static void initServices() throws Exception
	 {
		 ServiceManager.getServices().initializeServices(
					new Object[] { new TestRailwayRepository()});
	 }
	
	/**
	 * Create G101 ticket pool for "today".
	 *
	 */
	private TestTicketPool createTicketPool() throws Exception {

		
		
		IRailwayRepository repo = ServiceManager.getServices()
				.getRequiredService(IRailwayRepository.class);

		TrainNumber tn = Queries.query(repo.getTrainNumbers()).first(
				new Predicate<TrainNumber>() {

					@Override
					public boolean evaluate(TrainNumber obj) throws Exception {
						return obj.getName().equals("G101");
					}
				});

		LocalDate today = LocalDate.now();

		org.ng12306.tpms.runtime.Train train = new org.ng12306.tpms.runtime.Train();
		train.setId(UUID.randomUUID());
		train.setDepartureDate(today);
		train.setTrainNumber(tn);

		TestTicketPool pool = new TestTicketPool(train);
		pool.setSite(ServiceManager.getServices());
		pool.initialize();

		return pool;

	}

	/**
	 * Test book 1 ticket from Beijingnan station to Nanjingnan station with any seat.
	 * @throws Exception
	 */
	@Test
	public void testBook() throws Exception {
		//Create query arguments. TicketQueryArgs is used for external client with human readable parameters.
		TicketQueryArgs query = new TicketQueryArgs();
		query.setDate(LocalDate.now());
		query.setDepartureStation("北京南");
		query.setDestinationStation("南京南");
		query.setTrainNumber("G101");
		query.setSeatType(-1);
		query.setCount(1);

		
		
		TestTicketPool pool = this.createTicketPool();
		
		//Because book is the only operation which may change data, so in the future, toTicketPoolQueryArgs, book and toTicket will
		//be executed by different disruptor consumers.  

		
		//Convert query to pool query. pool query is only used inside pool system and holds seat, train as ids or java references.
				
		TicketPoolQueryArgs poolQuery = pool.toTicketPoolQueryArgs(query);
		
		TicketPoolTicket[] poolTickets = pool.book(poolQuery);
		
		
		//Convert pool ticket to human readable ticket.
		Ticket[] tickets = pool.toTicket(poolTickets);
		
		Assert.assertEquals(1, tickets.length);
		
		Ticket ticket = tickets[0];
		
		Assert.assertEquals(LocalDate.now(), ticket.getDepartureDate());
		
		Assert.assertEquals("北京南", ticket.getDepartureStation());
		Assert.assertEquals("南京南", ticket.getDestinationStation());
		Assert.assertEquals("G101", ticket.getTrainNumber());
		

	}
	
	/**
	 * Repeat testSaleAll. Shiyimin reported it has random errors when testSaleAll running. But cannot be reproduced after run 1000 times. 
	 * @throws Exception
	 */
	@Test
	public void RepeatTestSaleAll() throws Exception
	{
		for(int i = 0; i < 1000; i++)
		{
			testSaleAll();
		}
	}
	
/*	@Test
	public void TestGrouping() throws Exception
	{
		TestTicketPool pool = this.createTicketPool();
		
		
	
		
		TicketPoolQueryArgs query = new TicketPoolQueryArgs();
		
		query.setCount(1);
		query.setSeatType(~0);
		
		int[][] ranges = new int[][]{
				new int[]{1, 3},
				new int[]{4, 5},
				new int[]{0, 1},
				new int[]{5, 6},
				new int[]{6,7},
				new int[]{0,1},
				new int[]{2,4},
				new int[]{1,2},
				
		}; 
		for(int[] range : ranges)
		{
			query.setDepartureStop(range[0]);
			query.setDestinationStop(range[1]);
			
			TicketPoolTicket[] tickets = pool.book(query);
			
			printTickets(Queries.query(tickets));
		}
	}*/
	
	/**
	 * This test randomly generates booking request until all tickets are sold out. 
	 * Finally, it checks every ticket of every seat has been sold and  no range overlapping between tickets on same seat.
	 * @throws Exception
	 */
	@Test
	public void testSaleAll() throws Exception
	{


		TestTicketPool pool = this.createTicketPool();
		
	
		int stopCount = pool.getRoute().getStops().size();
		
		TicketPoolQueryArgs query = new TicketPoolQueryArgs();
		
		query.setCount(1);
		query.setSeatType(~0);
	
		
		ArrayList<TicketPoolTicket> soldTickets = new ArrayList<TicketPoolTicket>(10000);
		
		Random random = new Random(new Date().getTime());
		
		while(!pool.getIsSoldOut())
		{
			int v1 = random.nextInt(stopCount);
			int v2 = random.nextInt(stopCount);
			
			if(v1 != v2)
			{
				int departure = Math.min(v1, v2);
				int destination = Math.max(v1, v2);
				query.setDepartureStop(departure);
				query.setDestinationStop(destination);
				
				TicketPoolTicket[] tickets = pool.book(query);
				for(TicketPoolTicket t : tickets)
				{
					soldTickets.add(t);
				}
			}
		}
		
		Iterable<IGrouping<OperatingSeat, TicketPoolTicket>> groups = Queries.query(soldTickets).groupBy(new Selector<TicketPoolTicket, OperatingSeat>(){

			@Override
			public OperatingSeat select(TicketPoolTicket item) {
				return item.getSeat();
			}});
		
		for(IGrouping<OperatingSeat, TicketPoolTicket> g : groups)
		{
			
			
			Assert.assertTrue(g.toQuery().count()> 0);
			
			int stop = 0;
			for(TicketPoolTicket t : g.toQuery().orderBy(new Selector<TicketPoolTicket, Integer>(){

				@Override
				public Integer select(TicketPoolTicket item) {
					return item.getDepartureStop();
				}}))
			{
				
				
				Assert.assertEquals(stop, t.getDepartureStop());
				stop = t.getDestinationStop();
			}
			
			Assert.assertEquals(stopCount - 1, stop);
			
			
		}
		
		
		
		
	}
	

}

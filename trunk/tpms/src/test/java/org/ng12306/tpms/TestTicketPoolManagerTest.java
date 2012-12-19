package org.ng12306.tpms;


import junit.framework.Assert;


import org.junit.Test;
import org.ng12306.tpms.runtime.*;


public class TestTicketPoolManagerTest {

	
	
	@Test
	public void testCreateServices() throws Exception {
		
		//Test TestTicketPoolManager can be initialized without throwing any exception.
		ServiceManager.getServices().initializeServices(new Object[] {new TestRailwayRepository(), new TestTicketPoolManager()});
		
		//IRailwayRepository repo = ServiceManager.getServices().getRequiredService(IRailwayRepository.class);
		
		ITicketPoolManager tpm = ServiceManager.getServices().getRequiredService(ITicketPoolManager.class);
		
		Assert.assertNotNull(tpm);
	
		
	}
	
	

}

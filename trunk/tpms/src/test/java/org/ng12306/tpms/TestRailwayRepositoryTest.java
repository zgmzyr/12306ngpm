package org.ng12306.tpms;

import org.ng12306.tpms.runtime.*;

import org.junit.Test;

public class TestRailwayRepositoryTest {

	
	
	
	@Test
	public void test() throws Exception {
		//Test service can be initialized without throwing any exception
		TestRailwayRepository repo = new  TestRailwayRepository();
		repo.initializeService();
		
		
	}

}

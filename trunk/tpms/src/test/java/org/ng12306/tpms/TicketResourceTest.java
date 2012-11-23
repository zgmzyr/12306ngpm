package org.ng12306.tpms;

import junit.framework.TestCase;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;

public class TicketResourceTest extends TestCase {
    private SelectorThread threadSelector;

    private Client c;

    public TicketResourceTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        threadSelector = Main.startServer();
        c = Client.create();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        threadSelector.stopEndpoint();
    }

	// 测试根据车次号查询车次
    public void testQueryTrainByTrainNo() {
        WebResource r = c.resource(Main.BASE_URI.toString() + "ticket/id/1");
	    ClientResponse response = r.get(ClientResponse.class);
		assertEquals(200, response.getStatus());
		assertNotNull(response.getEntity(String.class));
    }

	// TODO: 添加更多的测试用例！
}

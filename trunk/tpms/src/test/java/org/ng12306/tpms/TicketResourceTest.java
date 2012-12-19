package org.ng12306.tpms;

import org.junit.*;
import static org.junit.Assert.*;
import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;
import java.util.Date;

public class TicketResourceTest {
    private static SelectorThread threadSelector;

    private static Client c;

    @BeforeClass
    public static void setUp() throws Exception {
        threadSelector = Main.startServer();
	EventBus.start();
        c = Client.create();
    }

    @AfterClass
    public static void tearDown() throws Exception {
	EventBus.shutdown();
        threadSelector.stopEndpoint();
    }

	// 测试根据车次号查询车次
    @Test
    public void testQueryTrainByTrainNo() {
        WebResource r = c.resource(Main.BASE_URI.toString() + "ticket/id/1");
	ClientResponse response = r.get(ClientResponse.class);
	assertEquals(200, response.getStatus());
	assertNotNull(response.getEntity(String.class));
    }

    // TODO: 添加更多的测试用例！
    @Test
    public void 测试购票与后台Disruptor的整合() throws Exception {
	WebResource r = c.resource(Main.BASE_URI.toString() + "ticket/id/G101");
	ClientResponse response = r.accept("application/json").get(ClientResponse.class);
	assertEquals(200, response.getStatus());
	Train[] results = response.getEntity(Train[].class);
	Train result = results[0];

	assertEquals("G101", result.name);
	assertEquals("北京南", result.departure);
	assertEquals("上海虹桥", result.termination);
	
	// 一个车次的发车时间应该只有时间，没有日期。
	assertEquals("07:00",
		     result.departureTime);
	assertEquals("12:23",
		     result.arrivalTime);

	// TODO: 这个断言是有问题的,因为我没有车次的具体座位配置.
	// 等业务网关组的服务出来之后，再来更新这个测试用例
	assertEquals(2, result.availables.length);
    }
}

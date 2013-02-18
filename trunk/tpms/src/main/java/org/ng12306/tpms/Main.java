package org.ng12306.tpms;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;

import org.ng12306.tpms.runtime.ServiceManager;
import org.ng12306.tpms.runtime.TestRailwayRepository;
import org.ng12306.tpms.runtime.TestTicketPoolManager;

public class Main {
    public static final URI BASE_URI = UriBuilder.fromUri("http://localhost/").port(9998).build();

    protected static SelectorThread startServer() throws IOException {
        final Map<String, String> initParams = new HashMap<String, String>();

        initParams.put("com.sun.jersey.config.property.packages", 
                "org.ng12306.tpms");

        System.out.println("Starting grizzly...");
        SelectorThread threadSelector = GrizzlyWebContainerFactory.create(BASE_URI, initParams);     
        return threadSelector;
    }
    
    public static void main(String[] args) throws Exception {
    	
    	
    	ServiceManager.getServices().initializeServices(new Object[] {new TestRailwayRepository(), new TestTicketPoolManager()});
    	
	// 启动jersey restful服务
        SelectorThread threadSelector = startServer();
	// 启动disruptor服务
	EventBus.start();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...",
                BASE_URI));
        System.in.read();

	// 关闭disruptor消息队列
	EventBus.shutdown();
	// 关闭jersey restful服务
        threadSelector.stopEndpoint();
        
        ServiceManager.getServices().uninitializeServices();
    }    
}

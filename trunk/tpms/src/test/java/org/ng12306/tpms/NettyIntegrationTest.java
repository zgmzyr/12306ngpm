package org.ng12306.tpms;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import org.junit.*;
import static org.junit.Assert.*;
import static org.ng12306.tpms.support.TestConstants.*;

import org.ng12306.tpms.runtime.*;
import org.ng12306.tpms.support.ObjectBsonEncoder;
import org.ng12306.tpms.support.ObjectBsonDecoder;

public class NettyIntegrationTest {
     class TestQueryTrainServerHandler extends SimpleChannelUpstreamHandler {
	  @Override
	  public void messageReceived(ChannelHandlerContext ctx,
				      MessageEvent e) {
	       TicketQueryArgs event = (TicketQueryArgs)e.getMessage();
	       event.channel = e.getChannel();
	       EventBus.publishQueryEvent(event);
	  }
     }

     // 用于在测试用例里向票池服务发送车次查询的Netty处理函数
     class TestQueryTrainHandler extends SimpleChannelUpstreamHandler {
	  // 要向服务器发送的查询数据包 - 包含车次号
	  private final TicketQueryArgs _event;
	  private TicketQueryResult _response;
	  public TicketQueryResult getResponse() { return _response; }
	 
	  public TestQueryTrainHandler(String trainId) {
	       _event = new TicketQueryArgs();
	       _event.setAction(TicketQueryAction.Query);
	       _event.setTrainNumber(trainId);
	       _event.setDate(new LocalDate().plusDays(1));
	       _event.setDepartureStation("北京南");
	       _event.setDestinationStation("南京南");
	       _event.setSeatType(-1);
	       _event.setCount(1);
	  }
	  
	  @Override
	  public void channelConnected(ChannelHandlerContext ctx,
				       ChannelStateEvent e) {
	       e.getChannel().write(_event);
	  }
	  
	  @Override
	  public void messageReceived(ChannelHandlerContext ctx,
				      MessageEvent e) {
	       _response = (TicketQueryResult)e.getMessage();
	       e.getChannel().close();
	  }

	  @Override
	  public void exceptionCaught(ChannelHandlerContext ctx,
				      ExceptionEvent e) {
	       e.getCause().printStackTrace();
	       e.getChannel().close();
	  }
     }

     // 根据虫子的代码,所有的服务都需要预先注册,然后再使用时,通过getRequiredService
     // 获取,类似Ioc,因此服务器在启动时,需要注册这些服务
     private void registerService() throws Exception {
	  ServiceManager
	       .getServices()
	       .initializeServices(new Object[] {
			 new TestRailwayRepository(), 
			 new TestTicketPoolManager()});
     }

     @Test
     public void 由车次查询结果定义票池服务器API() throws Exception {
	  // 启动Netty服务，这个函数应该要放到setUp函数里
	  startRealServer();

	  try {
	       final TestQueryTrainHandler handler = 
		    new TestQueryTrainHandler("G101");
	       
	       // 客户端的工作就是向服务器发送一个车次查询BSON请求
	       connectToServer(handler);
	        
	       // 等待一秒钟
	       Thread.sleep(1000);
	       
	       // 并验证
	       TicketQueryResult result = handler.getResponse();
	       assertTrue(result.getHasTicket());
	  } finally { 
	       stopRealServer();
	  }
     }

     // 这个就是真正的票池服务器了，为了隐藏后面的具体实现，定义一个接口ITpServer
     private ITpServer _itpServer;
     private void startRealServer() throws Exception {
	  // TODO: TpServer应该由Ioc创建，
	  // 现在为了定义API就直接创建新实例了。
	  _itpServer = new TpServer(TP_SERVER_PORT);

	  // 票池服务器应该启动disruptor event bus。
	  _itpServer.start();
     }

     private void stopRealServer() throws Exception {
	  if ( _itpServer != null ) {
	       _itpServer.stop();
	  }
     }

     // 我特烦Java强制在函数里声明自己可能扔出的异常，我知道Java的初衷是好的，但是
     // ...
     // ...
     // ...
     // Java的设计师们就没有预见过会有很多人try ... catch (Exception e)吗?
     private void connectToServer(final ChannelHandler sendRequest) throws Exception {
	  // 这个代码是从Netty官网抄来的，暂时还不知道为什么要这么做！
	  ChannelFactory factory = new NioClientSocketChannelFactory(
	       Executors.newCachedThreadPool(),
	       Executors.newCachedThreadPool());
	  ClientBootstrap bootstrap = new ClientBootstrap(factory);
	  bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
		    public ChannelPipeline getPipeline() 
			 throws Exception {
			 return Channels.pipeline(
			      // 使用自定义的bson格式序列化
			      new ObjectBsonEncoder(),
			      new ObjectBsonDecoder(
				   ClassResolvers.cacheDisabled(
					getClass().getClassLoader())),
			      sendRequest);
		    }
	       });
	  // 下面的设置貌似是TCP长连接，不过我们的计划是将其更新成UDP
	  // 因此也直接抄Netty官网的示例程序好了！
	  bootstrap.setOption("tcpNoDelay", true);
	  bootstrap.setOption("keepAlive", true);
	  
	  // 连接到服务器
	  bootstrap.connect(new InetSocketAddress(TP_SERVER_ADDRESS,
						  TP_SERVER_PORT));	      
     }
}

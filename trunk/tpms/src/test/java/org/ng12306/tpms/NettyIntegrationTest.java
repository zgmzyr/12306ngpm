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

import org.junit.*;
import static org.junit.Assert.*;
import static org.ng12306.tpms.support.TestConstants.*;

import org.ng12306.tpms.runtime.*;
import org.ng12306.tpms.support.ObjectBsonEncoder;
import org.ng12306.tpms.support.ObjectBsonDecoder;
import org.ng12306.tpms.support.TestNettyServer;

public class NettyIntegrationTest {
     class TestQueryTrainServerHandler extends SimpleChannelUpstreamHandler {
	  @Override
	  public void messageReceived(ChannelHandlerContext ctx,
				      MessageEvent e) {
	       TicketQueryArgs event = (TicketQueryArgs)e.getMessage();
	       EventBus.publishQueryEvent(event);
	      
	  }
     }

     // 用于在测试用例里向票池服务发送车次查询的Netty处理函数
     class TestQueryTrainHandler extends SimpleChannelUpstreamHandler {
	  // 要向服务器发送的查询数据包 - 包含车次号
	  private final TicketQueryEvent _event;
	  private Train[] _response;
	  public Train[] getResponse() { return _response; }
	 
	  public TestQueryTrainHandler(String trainId) {
	       _event = new TicketQueryEvent();
	       _event.trainId = trainId;
	       _event.startDate = new DateTime();
	       _event.endDate = _event.startDate.plusDays(1);
	       
	       /* 
	       // 使用Ioc获取一个序列化的接口
	       ISerializerFactory sf = resolve(ISerializerFactory.class);
	       // 因为我是使用TDD的方式编程，下面的json的目的仅仅是为了设计一个API而已
	       // 我觉得流水型API设计可能会更好一些，例如对比下面两行代码与之后注释的代码
	       ISerializer s = sf.createFrom(event);
	       _message.writeBytes(s.json().bson().bytes());
	       
	       // ISerializer s = resolve(ISerializer.class);
	       // byte[] bytes = s.toBytes(s.ToBson(event));
	       // _message.writeBytes(bytes);
	       */
	  }
	  
	  @Override
	  public void channelConnected(ChannelHandlerContext ctx,
				       ChannelStateEvent e) {
	       e.getChannel().write(_event);
	  }
	  
	  @Override
	  public void messageReceived(ChannelHandlerContext ctx,
				      MessageEvent e) {
	       _response = (Train[])e.getMessage();
	       e.getChannel().close();
	  }

	  @Override
	  public void exceptionCaught(ChannelHandlerContext ctx,
				      ExceptionEvent e) {
	       e.getCause().printStackTrace();
	       e.getChannel().close();
	  }
     }
     
     @Test
     public void 试验根据车次查询结果() throws Exception {
	  // 启动Netty服务，这个函数应该要放到setUp函数里
	  startServer();

	  try {
	       // 这个代码是从Netty官网抄来的，暂时还不知道为什么要这么做！
	       ChannelFactory factory = new NioClientSocketChannelFactory(
		    Executors.newCachedThreadPool(),
		    Executors.newCachedThreadPool());
	       ClientBootstrap bootstrap = new ClientBootstrap(factory);
	       final TestQueryTrainHandler handler = 
		    new TestQueryTrainHandler("G101");
	       
	  // 客户端的工作就是向服务器发送一个车次查询BSON请求
	       bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			 public ChannelPipeline getPipeline() 
			      throws Exception {
			      // 查询G101
			      return Channels.pipeline(
				   // 使用自定义的bson格式序列化
				   new ObjectBsonEncoder(),
				   new ObjectBsonDecoder(
					ClassResolvers.cacheDisabled(
					     getClass().getClassLoader())),
				   handler);
			 }
		    });
	       // 下面的设置貌似是TCP长连接，不过我们的计划是将其更新成UDP
	       // 因此也直接抄Netty官网的示例程序好了！
	       bootstrap.setOption("tcpNoDelay", true);
	       bootstrap.setOption("keepAlive", true);
	       
	       // 连接到服务器
	       bootstrap.connect(new InetSocketAddress(TP_SERVER_ADDRESS,
						       TP_SERVER_PORT));
	       
	       // 等待一秒钟
	       Thread.sleep(1000);
	       
	       // 并验证
	       Train[] results = handler.getResponse();
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
	  } finally { 
	       stopServer();
	  }
     }
     
     private TestNettyServer _server;
     private void startServer() throws Exception {
	  EventBus.start();
	  _server = new TestNettyServer(TP_SERVER_PORT,
					new TestQueryTrainServerHandler());
	  _server.start();
     }

     private void stopServer() throws Exception {
	  if ( _server != null ) {
	       EventBus.shutdown();
	       _server.stop();
	  }
     }
}

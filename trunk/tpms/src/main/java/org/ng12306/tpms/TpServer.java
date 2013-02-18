package org.ng12306.tpms;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.UUID;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;

import org.diting.collections.Predicate;
import org.diting.collections.Queries;
import org.joda.time.LocalDate;

import org.ng12306.tpms.runtime.Train;
import org.ng12306.tpms.runtime.TicketQueryArgs;
import org.ng12306.tpms.runtime.TestRailwayRepository;
import org.ng12306.tpms.runtime.TestTicketPoolManager;
import org.ng12306.tpms.runtime.ServiceManager;
import org.ng12306.tpms.runtime.TestTicketPool;
import org.ng12306.tpms.runtime.TrainNumber;
import org.ng12306.tpms.runtime.IRailwayRepository;
import org.ng12306.tpms.runtime.ITicketPoolManager;

// ITpServer的默认实现，如果要做A/B测试的话，应该是
// 从TpServer继承实现两种方式
public class TpServer implements ITpServer {
     private int _port;
     private ChannelGroup _channels;
     private ChannelFactory _factory;
     private boolean _started;
     
     public TpServer(int port){
	  _port = port;
	  _channels = new DefaultChannelGroup("ticket-pool");
     }
     
     public void start() {
	  // 下面的代码都是可以直接在Netty的官网里看到的，不仔细注释
	  _factory = new NioServerSocketChannelFactory(
	       // TODO: 需要写性能测试用例已验证cached thread pool是否够用？
	       Executors.newCachedThreadPool(),
	       Executors.newCachedThreadPool());
	  ServerBootstrap bootstrap = new ServerBootstrap(_factory);
	  bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
		    public ChannelPipeline getPipeline() throws Exception {
			 // 这个就是发送消息包的Handler栈 - 虽然名字叫管道！
			 return Channels.pipeline(
			      new ObjectEncoder(),
			      new ObjectDecoder(
				   ClassResolvers.cacheDisabled(
					getClass().getClassLoader())),
			      new QueryTrainServerHandler());
		    }
	       });
	  _channels.add(bootstrap.bind(new InetSocketAddress(_port)));
	  try { 
	       // 如果eventbus因为日志\或者联系不上备份服务器而无法启动
	       // 那么就关闭Netty服务器
	       registerService();
	       EventBus.start();
	       _started = true;	       
	  } catch ( Exception e ) {	       
	       // TODO: 改成使用log4j之类的库来打印日志！
	       System.out.println("start server failed with: " + e);
	       e.printStackTrace();
	       stopNettyServer();
	  }
     }

     public void stop() {
	  if ( _started ) {
	       try { 
		    // EventBus如果没有正常关闭,打个日志好了
		    EventBus.shutdown();
	       } catch ( Exception e ) {
		    // TODO: 记录EventBus无法正常关闭的日志
	       }

	       stopNettyServer();
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

     private void stopNettyServer()  {
	  ChannelGroupFuture future = _channels.close();
	  future.awaitUninterruptibly();
	  _factory.releaseExternalResources();
	  _started = false;
     }

     class QueryTrainServerHandler extends SimpleChannelUpstreamHandler {
	  @Override
	  public void messageReceived(ChannelHandlerContext ctx,
				      MessageEvent e) {
	       // 票池服务器采用异步网络io的方式接受消息
	       // 因为我们的Handler是从SimpleChannelUpstreamHandler继承下来的
	       // Netty会帮我们将多个零散的数据包整合一个完整的原始的客户端请求数据包
	       // 另外，由于在其之前我们已经放置了序利化方面的Handler了，所以可以
	       // 直接通过e.getMessage()获取客户端发送的对象。
	       TicketQueryArgs event = (TicketQueryArgs)e.getMessage();
	       // 传递给disruptor车轮队列进行处理。
	       Channel channel = e.getChannel();
	       event.channel = channel;
	       EventBus.publishQueryEvent(event);       
	  }

	  // TODO: 需要定义和实现发生异常的日志方式
     }
}

package org.ng12306.tpms.support;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import static org.ng12306.tpms.support.TestConstants.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.*;
import org.jboss.netty.bootstrap.*;
import org.jboss.netty.channel.socket.nio.*;

import org.jboss.netty.handler.codec.serialization.ClassResolvers;

public class TestNettyServer {
     private int _port;
     private ChannelHandler _handler;
     private ChannelGroup _channels;
     private ChannelFactory _factory;
     private boolean _started;
     
     public TestNettyServer(int port, ChannelHandler handler) {
	  _port = port;
	  _handler = handler;
	  _channels = new DefaultChannelGroup("test-netty-server");
     }
     
     public void start() {
	  _factory = new NioServerSocketChannelFactory(
	       Executors.newCachedThreadPool(),
	       Executors.newCachedThreadPool());
	  ServerBootstrap bootstrap = new ServerBootstrap(_factory);
	  bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
		    public ChannelPipeline getPipeline() throws Exception {
			 return Channels.pipeline(
			      new ObjectBsonEncoder(),
			      new ObjectBsonDecoder(
				   ClassResolvers.cacheDisabled(
					getClass().getClassLoader())),
			      _handler);
		    }
	       });
	  _channels.add(bootstrap.bind(new InetSocketAddress(_port)));
	  _started = true;
     }

     public void stop() {
	  if ( _started ) {
	       ChannelGroupFuture future = _channels.close();
	       future.awaitUninterruptibly();
	       _factory.releaseExternalResources();
	       _started = false;
	  }
     }
}

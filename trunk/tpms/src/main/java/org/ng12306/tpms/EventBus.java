package org.ng12306.tpms;

import java.util.concurrent.*;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.*;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import org.joda.time.DateTime;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import org.ng12306.tpms.runtime.ServiceManager;
import org.ng12306.tpms.runtime.TicketQueryArgs;
import org.ng12306.tpms.runtime.TicketQueryResult;
import org.ng12306.tpms.runtime.TicketQueryAction;
import org.ng12306.tpms.runtime.TicketPoolQueryArgs;
import org.ng12306.tpms.runtime.ITicketPoolManager;
import org.ng12306.tpms.runtime.ITicketPool;

public class EventBus {
     private static ObjectOutputStream _journal;
     private static RingBuffer<TicketQueryArgs> _ringBuffer;
     private static Disruptor<TicketQueryArgs> _disruptor;
     private static ITicketPoolManager _poolManger;
     
     // 日志线程
     static final EventHandler<TicketQueryArgs> _journalist = 
	  new EventHandler<TicketQueryArgs>() {
	  public void onEvent(final TicketQueryArgs event, 
			      final long sequence,
			      final boolean endOfBatch) throws Exception {
	       // TODO: 需要确保程序崩溃的时候，所有的数据都在硬盘上
	       // 因为在硬盘上有一个缓冲区，需要确保即使程序崩溃也能把缓冲里的数据
	       // 写到硬盘上，不过貌似现代操作系统能够做到在程序崩溃时flush缓存，这点
	       // 需要测试验证。

	       // brucesea的反馈: 查询不改变状态，Journalist和Replicator感觉就用不着了，
	       // 对改变状态的操作Journalist和Replicator一下
	       // 因此需要根据TicketQueryArgs的类型来决定是否做日志和备份
	       
	       // _journal.writeObject(event);
	  }
     };
     
     // 将事件发送到备份服务器保存的备份线程
     static final EventHandler<TicketQueryArgs> _replicator = new EventHandler<TicketQueryArgs>() {
	  public void onEvent(final TicketQueryArgs event, final long sequence,
			      final boolean endOfBatch) throws Exception {
	       // TODO: 后期再实现备份线程的逻辑
	  }
     };

    static final EventHandler<TicketQueryArgs> _eventProcessor = 
	new EventHandler<TicketQueryArgs>() {
	public void onEvent(final TicketQueryArgs event,
			    final long sequence,
			    final boolean endOfBatch) throws Exception {
	     // 根据车次号查询车次详细信息	       
	     ITicketPool pool = EventBus._poolManger.getPool(event);
	     TicketQueryResult result = new TicketQueryResult();
	     
	     // TODO: 这段代码尚有争议，因为查询车票应该返回有票的车次列表。
	     if (pool != null) {
		  TicketPoolQueryArgs poolArgs = pool
		       .toTicketPoolQueryArgs(event);
		  if (event.getAction() == TicketQueryAction.Query) {
		      result.setHasTicket(pool.hasTickets(poolArgs));
		  }
	     }
	     
	     // 无论什么样的结果,都需要向客户端发送一个响应.
	     ChannelFuture future = event.channel.write(result);	    
	     future.addListener(ChannelFutureListener.CLOSE);

	     /*
	     // 不管找到与否，都会有一个响应
	    Train[] trains = null;
	    if ( train != null ) {
		trains = new Train[] { train };
	    } else { 
		trains = new Train[0];
	    }

	    // 将查询结果直接写到附在事件上的客户端端口上。
	    // 请求处理的结果不备份，因为没有必要，如果客户端收不到反馈，
	    // 它需要再发一次，否则的话，难道票池在恢复时还要遍历备份
	    // 再重发响应？
	    ChannelFuture future = event.channel.write(trains);	    
	    future.addListener(ChannelFutureListener.CLOSE);
	     */
	}
    };
    
    // 默认的请求消息和响应消息队列的大小是2的13次方
    private static int RING_SIZE = 2 << 13;
    private static final ExecutorService EXECUTOR = 
	Executors.newCachedThreadPool();

    // 向消息队列发布一个查询请求事件
    // TODO: 将publicXXXEvent改成异步的，应该返回void类型，异步返回查询结果。
    public static void publishQueryEvent(TicketQueryArgs args) {
	long sequence = _ringBuffer.next();
	TicketQueryArgs event = _ringBuffer.get(sequence);
	args.copyTo(event);
	// event.sequence = sequence;
	event.setSequence(sequence);

	// 将消息放到车轮队列里，以便处理
	_ringBuffer.publish(sequence);
    }

    public static void start() throws Exception {
	 _poolManger = ServiceManager.getServices().getRequiredService(
	      ITicketPoolManager.class);

	 // 在disruptor启动之前打开日志
	 openJournal();
	 startDisruptor();
    }
    
    public static void shutdown() throws Exception {
	// 先关闭掉disruptor，再关闭日志文件
	// 以免出现日志线程和disruptor关闭同时运行的情况
	_disruptor.shutdown();
	// _disruptorRes.shutdown();
	_journal.close();
    }

    private static void openJournal() throws Exception {
	// 应该是只增打开
	FileOutputStream fos = 
	    new FileOutputStream("eventbus.journal");
	_journal = new ObjectOutputStream(fos);
    }
    
    private static void startDisruptor() {       
	// 创建处理查询消息的disruptor
	_disruptor = 
	    new Disruptor<TicketQueryArgs>
	    (
	     TicketPoolService.QueryFactory,
	     EXECUTOR,
	     new SingleThreadedClaimStrategy(RING_SIZE),
	     new BlockingWaitStrategy()
	     );

	// @brucesea
	// 另外用Disruptor，推荐用DisruptorWizard，
	// 这样EventProcessor之间的关系会比较清晰
	_disruptor
	     // 注册日志和备份线程
	     .handleEventsWith(_journalist, _replicator)
	     // 事件处理线程只能在日志和备份线程之后处理它
	     .then(_eventProcessor);

	// 启动disruptor,等待publish事件
	_ringBuffer = _disruptor.start();
    }
}

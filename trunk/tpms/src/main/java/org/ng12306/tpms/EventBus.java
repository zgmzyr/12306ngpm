package org.ng12306.tpms;

import java.util.concurrent.*;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.*;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import org.joda.time.DateTime;

public class EventBus {
    private static ObjectOutputStream _journal;
    private static RingBuffer<TicketQueryEvent> _ringBuffer;
    private static RingBuffer<TicketQueryResultEvent> _outRingBuffer;
    private static Disruptor<TicketQueryEvent> _disruptor;
    private static Disruptor<TicketQueryResultEvent> _disruptorRes;
    
    // 日志线程
    static final EventHandler<TicketQueryEvent> _journalist = 
	new EventHandler<TicketQueryEvent>() {
	public void onEvent(final TicketQueryEvent event, 
			    final long sequence,
			    final boolean endOfBatch) throws Exception {
	    // TODO: 需要确保程序崩溃的时候，所有的数据都在硬盘上
	    // 因为在硬盘上有一个缓冲区，需要确保即使程序崩溃也能把缓冲里的数据
	    // 写到硬盘上，不过貌似现代操作系统能够做到在程序崩溃时flush缓存，这点
	    // 需要测试验证。
	    _journal.writeObject(event);
	}
    };

    // 将事件发送到备份服务器保存的备份线程
    static final EventHandler<TicketQueryEvent> _replicator = 
	new EventHandler<TicketQueryEvent>() {
	public void onEvent(final TicketQueryEvent event,
			    final long sequence,
			    final boolean endOfBatch) throws Exception {
	    // TODO: 后期再实现备份线程的逻辑
	}
    };

    static final EventHandler<TicketQueryEvent> _eventProcessor = 
	new EventHandler<TicketQueryEvent>() {
	public void onEvent(final TicketQueryEvent event,
			    final long sequence,
			    final boolean endOfBatch) throws Exception {
	    // 根据车次号查询车次详细信息
	    Train train = TicketRepository.queryTrain(event.trainId,
						      event.startDate,
						      event.endDate);

	    // 不管找到与否，都会有一个响应
	    long s = _outRingBuffer.next();
	    TicketQueryResultEvent e = _outRingBuffer.get(s);	    
	    // 将响应消息和请求消息关联起来，因为调用者也有可能是异步处理的
	    e.sequence = event.sequence;

	    // 找到了的话，就向响应消息队列里放入一个车次详细信息数组
	    if ( train != null ) {	       
		e.trains = new Train[1];
		e.trains[0] = train;
	    } else {
		// 不能设置为null，否则前台jersey等restful服务
		// 在序列化结果的时候可能会出错，因此宁愿返回一个空数组。
		e.trains = new Train[0];
	    }

	    _outRingBuffer.publish(s);
	}
    };
    
    // 默认的请求消息和响应消息队列的大小是2的13次方
    private static int RING_SIZE = 2 << 13;
    private static final ExecutorService EXECUTOR = 
	Executors.newCachedThreadPool();

    // 向消息队列发布一个查询请求事件
    // TODO: 将publicXXXEvent改成异步的，应该返回void类型，异步返回查询结果。
    public static Train[] publishQueryEvent(String trainId,
					    DateTime startDate,
					    DateTime endDate) {
	long sequence = _ringBuffer.next();
	TicketQueryEvent event = _ringBuffer.get(sequence);
	event.sequence = sequence;
	event.trainId = trainId;
	event.startDate = startDate;
	event.endDate = endDate;
	_ringBuffer.publish(sequence);

	// 代码应该到此为止，不过我还不知道如何修改jersey，使其
	// 返回异步向来源restful服务调用者返回结果，因此只好
	// 用下面同步的方式
	return waitForResponse(sequence).trains;
    }

    public static void start() throws Exception {
	// 在disruptor启动之前打开日志
	openJournal();
	startDisruptor();
    }
    
    public static void shutdown() throws Exception {
	// 先关闭掉disruptor，再关闭日志文件
	// 以免出现日志线程和disruptor关闭同时运行的情况
	_disruptor.shutdown();
	_disruptorRes.shutdown();
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
	    new Disruptor<TicketQueryEvent>
	    (
	     TicketPoolService.QueryFactory,
	     EXECUTOR,
	     new SingleThreadedClaimStrategy(RING_SIZE),
	     new BlockingWaitStrategy()
	     );
	// 注册日志和备份线程
	_disruptor.handleEventsWith(_journalist);
	_disruptor.handleEventsWith(_replicator);

	// 事件处理线程只能在日志和备份线程之后处理它
	_ringBuffer = _disruptor.getRingBuffer();
	SequenceBarrier barrier = _ringBuffer.newBarrier();
	_disruptor.handleEventsWith(_eventProcessor);

	// 启动disruptor,等待publish事件
	_disruptor.start();

	// 创建返回查询结果消息的disruptor
	_disruptorRes = 
	    new Disruptor<TicketQueryResultEvent>
	    (
	     TicketPoolService.QueryResultFactory,
	     EXECUTOR,
	     new SingleThreadedClaimStrategy(RING_SIZE),
	     new BlockingWaitStrategy()
	    );
	// 在返回结果消息的时候，就不做任何日志和备份了。
	_outRingBuffer = _disruptorRes.start();
    }

    // 等知道如何整合jersey异步调用后，删掉这个函数
    private static TicketQueryResultEvent waitForResponse(long sequence) {
	while ( true ) {
	    long s = _outRingBuffer.getCursor();
	    TicketQueryResultEvent event = _outRingBuffer.get(s);
	    if ( event.sequence == sequence ) {
		return event;
	    }
	}
    }

}

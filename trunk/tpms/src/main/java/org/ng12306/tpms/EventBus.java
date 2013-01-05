package org.ng12306.tpms;

import java.util.concurrent.*;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.*;
import java.io.*;
import org.ng12306.tpms.runtime.*;

public class EventBus {
	private static ObjectOutputStream _journal;
	private static RingBuffer<TicketQueryArgs> _ringBuffer;
	private static RingBuffer<TicketQueryResult> _outRingBuffer;
	private static Disruptor<TicketQueryArgs> _disruptor;
	private static Disruptor<TicketQueryResult> _disruptorRes;

	private static ITicketPoolManager _poolManger;

	// 日志线程
	static final EventHandler<TicketQueryArgs> _journalist = new EventHandler<TicketQueryArgs>() {
		public void onEvent(final TicketQueryArgs event, final long sequence,
				final boolean endOfBatch) throws Exception {
			// TODO: 需要确保程序崩溃的时候，所有的数据都在硬盘上
			// 因为在硬盘上有一个缓冲区，需要确保即使程序崩溃也能把缓冲里的数据
			// 写到硬盘上，不过貌似现代操作系统能够做到在程序崩溃时flush缓存，这点
			// 需要测试验证。
			_journal.writeObject(event);
		}
	};

	// 将事件发送到备份服务器保存的备份线程
	static final EventHandler<TicketQueryArgs> _replicator = new EventHandler<TicketQueryArgs>() {
		public void onEvent(final TicketQueryArgs event, final long sequence,
				final boolean endOfBatch) throws Exception {
			// TODO: 后期再实现备份线程的逻辑
		}
	};

	static final EventHandler<TicketQueryArgs> _eventProcessor = new EventHandler<TicketQueryArgs>() {
		public void onEvent(final TicketQueryArgs event, final long sequence,
				final boolean endOfBatch) throws Exception {
			// 根据车次号查询车次详细信息

			ITicketPool pool = EventBus._poolManger.getPool(event);

			// 不管找到与否，都会有一个响应
			long s = _outRingBuffer.next();
			TicketQueryResult result = _outRingBuffer.get(s);

			if (pool != null) {
				TicketPoolQueryArgs poolArgs = pool
						.toTicketPoolQueryArgs(event);
				if (event.getAction() == TicketQueryAction.Query) {
					result.setHasTicket(pool.hasTickets(poolArgs));
				} else {
					TicketPoolTicket[] poolTickets = pool.book(poolArgs);
					result.setHasTicket(poolTickets.length > 0);
					result.setTickets(pool.toTicket(poolTickets));
				}
			} else {
				result.setHasTicket(false);
			}

			// 将响应消息和请求消息关联起来，因为调用者也有可能是异步处理的
			result.setSequence(event.getSequence());
			_outRingBuffer.publish(s);
		}
	};

	// 默认的请求消息和响应消息队列的大小是2的13次方
	private static int RING_SIZE = 2 << 13;
	private static final ExecutorService EXECUTOR = Executors
			.newCachedThreadPool();

	// 向消息队列发布一个查询请求事件
	
	public static void publishQueryEvent(TicketQueryArgs args) {
		long sequence = _ringBuffer.next();
		TicketQueryArgs event = _ringBuffer.get(sequence);
		args.copyTo(event);
		event.setSequence(sequence);
		
		_ringBuffer.publish(sequence);
	}

	public static void start() throws Exception {
		// 在disruptor启动之前打开日志

		_poolManger = ServiceManager.getServices().getRequiredService(
				ITicketPoolManager.class);
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
		FileOutputStream fos = new FileOutputStream("eventbus.journal");
		_journal = new ObjectOutputStream(fos);
	}

	private static void startDisruptor() {
		// 创建处理查询消息的disruptor
		_disruptor = new Disruptor<TicketQueryArgs>(
				new EventFactory<TicketQueryArgs>(){

					@Override
					public TicketQueryArgs newInstance() {
						return new TicketQueryArgs();
					}}, EXECUTOR,
				new SingleThreadedClaimStrategy(RING_SIZE),
				new BlockingWaitStrategy());
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
		_disruptorRes = new Disruptor<TicketQueryResult>(
				new EventFactory<TicketQueryResult>(){

					@Override
					public TicketQueryResult newInstance() {
						return new TicketQueryResult();
					}}, EXECUTOR,
				new SingleThreadedClaimStrategy(RING_SIZE),
				new BlockingWaitStrategy());
		// 在返回结果消息的时候，就不做任何日志和备份了。
		_outRingBuffer = _disruptorRes.start();
	}

}

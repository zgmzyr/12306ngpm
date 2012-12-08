package org.ng12306.tpms;

import java.util.concurrent.*;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.*;
import static org.junit.Assert.*;
import org.junit.*;

public class DisruptorConceptProofTest
{
    private final static EventFactory<TestTicketEvent> TestFactory =
	new EventFactory<TestTicketEvent>() {
	public TestTicketEvent newInstance() {
	    return new TestTicketEvent();
	}
    };

    private long _journalistCount = 0L;
    // 用来验证日志线程看到的最后一个事件值
    private int _lastEventValue = 0;
    // 用来验证日志线程看到了所有生产线程产生的事件
    private int _journalistValueSum = 0;
    // 将事件保存在硬盘里的书记员线程
    final EventHandler<TestTicketEvent> _journalist = 
	new EventHandler<TestTicketEvent>() {
	public void onEvent(final TestTicketEvent event, 
			    final long sequence,
			    final boolean endOfBatch) throws Exception {
	    _journalistCount++;
	    _lastEventValue = event.getValue();
	    _journalistValueSum += _lastEventValue;
	}
    };
    
    private long _replicatorCount = 0L;
    // 用来验证备份线程看到了所有生产线程产生的事件
    private int _replicatorValueSum = 0;
    // 将事件发送到备份服务器保存的备份线程
    final EventHandler<TestTicketEvent> _replicator = 
	new EventHandler<TestTicketEvent>() {
	public void onEvent(final TestTicketEvent event,
			    final long sequence,
			    final boolean endOfBatch) throws Exception {
	    _replicatorCount++;
	    _replicatorValueSum += event.getValue();
	}
    };
    
    final EventHandler<TestTicketEvent> _eventProcessor = 
	new EventHandler<TestTicketEvent>() {
	public void onEvent(final TestTicketEvent event,
			    final long sequence,
			    final boolean endOfBatch) throws Exception {
	    System.out.println("[processor] " + new Long(sequence).toString());
	}
    };
    
    private int RING_SIZE = 128;
    private final ExecutorService EXECUTOR = 
	Executors.newCachedThreadPool();

    @Before
    public void setUp() throws Exception {
	_journalistCount = _replicatorCount = _lastEventValue = 0;
    }

    // 下面这个测试用例和"演示disruptor的基本用法"的作用一致
    @Test
    public void 演示disruptor的dsl用法() throws Exception {
	Disruptor<TestTicketEvent> disruptor = 
	    new Disruptor<TestTicketEvent>
	    (
	     TestFactory,
	     EXECUTOR,
	     new SingleThreadedClaimStrategy(RING_SIZE),
	     new BlockingWaitStrategy()
	     );
	// 注册日志和备份线程
	disruptor.handleEventsWith(_journalist);
	disruptor.handleEventsWith(_replicator);

	// 启动disruptor,等待publish事件
	RingBuffer<TestTicketEvent> ringBuffer = disruptor.start();

	// 添加一些事件
	for ( int i = 0; i < RING_SIZE; ++i ) {
	    long sequence = ringBuffer.next();
	    TestTicketEvent event = ringBuffer.get(sequence);
	    event.setValue(i);
	    ringBuffer.publish(sequence);
	}

	// 显式等待两个线程执行完毕,因为我现在还不知道如何更好的等待
	// 理论上来说,应该是在某个时候使用eventprocessor.halt函数的
	// 因为这个系统应该是不停循环处理的
	Thread.sleep(1000);
	assertEquals(RING_SIZE, _journalistCount);
	assertEquals(RING_SIZE, _replicatorCount);

	// 对于日志和备份线程,应该是串行执行每一个事件的
	assertEquals(RING_SIZE - 1, _lastEventValue);

	// 还有一个问题,就是确认所有事件是否真的已经处理了？
	int expected = (0 + RING_SIZE - 1) * RING_SIZE / 2;
	assertEquals(expected, _journalistValueSum);
	assertEquals(expected, _replicatorValueSum);

	disruptor.halt();
    }
	
    @Test
    public void 演示disruptor的基本用法() throws Exception {	
	RingBuffer<TestTicketEvent> ringBuffer = 
	    new RingBuffer<TestTicketEvent>
	    (
	     TestFactory,
	     new SingleThreadedClaimStrategy(RING_SIZE),
	     new BlockingWaitStrategy()
	    );

	SequenceBarrier barrier = ringBuffer.newBarrier();

	// 注册日志线程
	BatchEventProcessor<TestTicketEvent> journalist = 
	    new BatchEventProcessor<TestTicketEvent>(ringBuffer,
						 barrier, 
						 _journalist);
	ringBuffer.setGatingSequences(journalist.getSequence());
	EXECUTOR.submit(journalist);

	// 注册备份线程
	BatchEventProcessor<TestTicketEvent> replicator = 
	    new BatchEventProcessor<TestTicketEvent>(ringBuffer,
						 barrier,
						 _replicator);
	ringBuffer.setGatingSequences(replicator.getSequence());
	EXECUTOR.submit(replicator);

	for ( int i = 0; i < RING_SIZE; ++i ) {
	    long sequence = ringBuffer.next();
	    TestTicketEvent event = ringBuffer.get(sequence);
	    event.setValue(i);
	    ringBuffer.publish(sequence);
	}

	// 显式等待两个线程执行完毕,因为我现在还不知道如何更好的等待
	// 理论上来说,应该是在某个时候使用eventprocessor.halt函数的
	// 因为这个系统应该是不停循环处理的
	Thread.sleep(1000);
	assertEquals(RING_SIZE, _journalistCount);
	assertEquals(RING_SIZE, _replicatorCount);

	// 对于日志和备份线程,应该是串行执行每一个事件的
	assertEquals(RING_SIZE - 1, _lastEventValue);

	int expected = (0 + RING_SIZE - 1) * RING_SIZE / 2;
	assertEquals(expected, _journalistValueSum);
	assertEquals(expected, _replicatorValueSum);
    }
}

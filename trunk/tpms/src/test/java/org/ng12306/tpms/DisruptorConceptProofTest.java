package org.ng12306.tpms;

import java.util.concurrent.*;
import com.lmax.disruptor.*;
import static org.junit.Assert.*;
import org.junit.*;

public class DisruptorConceptProofTest
{
    private long _journalistCount = 0L;
    private int _lastEventValue = 0;
    // 将事件保存在硬盘里的书记员线程
    final EventHandler<TicketEvent> _journalist = 
	new EventHandler<TicketEvent>() {
	public void onEvent(final TicketEvent event, 
			    final long sequence,
			    final boolean endOfBatch) throws Exception {
	    _journalistCount++;
	    _lastEventValue = event.getValue();
	}
    };
    
    private long _replicatorCount = 0L;
    // 将事件发送到备份服务器保存的备份线程
    final EventHandler<TicketEvent> _replicator = 
	new EventHandler<TicketEvent>() {
	public void onEvent(final TicketEvent event,
			    final long sequence,
			    final boolean endOfBatch) throws Exception {
	    _replicatorCount++;
	}
    };
    
    final EventHandler<TicketEvent> _eventProcessor = 
	new EventHandler<TicketEvent>() {
	public void onEvent(final TicketEvent event,
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
    }
	
    @Test
    public void 演示disruptor的基本用法() throws Exception {	
	RingBuffer<TicketEvent> ringBuffer = 
	    new RingBuffer<TicketEvent>
	    (
	     TicketPoolService.INSTANCE,
	     new SingleThreadedClaimStrategy(RING_SIZE),
	     new BlockingWaitStrategy()
	    );

	SequenceBarrier barrier = ringBuffer.newBarrier();

	// 注册日志线程
	BatchEventProcessor<TicketEvent> journalist = 
	    new BatchEventProcessor<TicketEvent>(ringBuffer,
						 barrier, 
						 _journalist);
	ringBuffer.setGatingSequences(journalist.getSequence());
	EXECUTOR.submit(journalist);

	// 注册备份线程
	BatchEventProcessor<TicketEvent> replicator = 
	    new BatchEventProcessor<TicketEvent>(ringBuffer,
						 barrier,
						 _replicator);
	ringBuffer.setGatingSequences(replicator.getSequence());
	EXECUTOR.submit(replicator);

	for ( int i = 0; i < RING_SIZE; ++i ) {
	    long sequence = ringBuffer.next();
	    TicketEvent event = ringBuffer.get(sequence);
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
    }
}

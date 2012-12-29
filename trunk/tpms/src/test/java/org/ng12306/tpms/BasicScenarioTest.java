package org.ng12306.tpms;

import java.util.concurrent.*;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.*;
import static org.junit.Assert.*;
import org.junit.*;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import org.joda.time.DateTime;

public class BasicScenarioTest 
{
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
	    String id = event.trainId;
	    for ( int i = 0; i < _trains.length; ++i ) {
		Train train = _trains[i];		
		if ( train.name.compareTo(id) == 0 ) {
		    long s = _outRingBuffer.next();
		    TicketQueryResultEvent e = _outRingBuffer.get(s);
		    e.trains = new Train[1];
		    e.trains[0] = train;
		    e.sequence = event.sequence;
		    _outRingBuffer.publish(s);
		    break;
		}
	    }
	}
    };
    
    private static int RING_SIZE = 128;
    private static final ExecutorService EXECUTOR = 
	Executors.newCachedThreadPool();
    
    @BeforeClass
    public static void setUp() throws Exception {
	// 数据库那肯定需要在所有的玩意之前就准备好了
	prepareFakeTicketPool();
	// 在disruptor启动之前打开日志
	openJournal();
	startDisruptor();
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
	_disruptor.shutdown();
	_disruptorRes.shutdown();
	_journal.close();
    }

    @Test
    public void 实现一个车次查询的Disruptor流程() throws Exception {
	// 1.向event bus放入一个查询事件
	long sequence = _ringBuffer.next();
	TicketQueryEvent event = _ringBuffer.get(sequence);
	event.sequence = sequence;
	event.trainId = "G101";	    
	event.startDate = new DateTime(2012, 12, 8, 16, 0, 0);
	event.endDate = new DateTime(2012, 12, 9, 0, 0, 0);
	_ringBuffer.publish(sequence);

	// 2. 然后等待一段时间，以便其他线程可以处理它
	Thread.sleep(1);
	
	// 3. 验证结果。
	TicketQueryResultEvent response = waitForResponse(sequence);
	Train train = response.trains[0];
	assertEquals("G101", train.name);
	assertEquals("北京南", train.departure);
	assertEquals("上海虹桥", train.termination);
	assertEquals("07:00", train.departureTime); 
	assertEquals("12:23", train.arrivalTime);
	assertEquals(2, train.availables.length);
    }

    private TicketQueryResultEvent waitForResponse(long sequence) {
	while ( true ) {
	    long s = _outRingBuffer.getCursor();
	    TicketQueryResultEvent event = _outRingBuffer.get(s);
	    if ( event.sequence == sequence ) {
		return event;
	    }
	}
    }
    
    private static void openJournal() throws Exception {
	// 应该是只增打开
	FileOutputStream fos = 
	    new FileOutputStream("basicscenariotest.journal");
	_journal = new ObjectOutputStream(fos);
    }

    private static Train[] _trains;
    private static void prepareFakeTicketPool() {
	_trains = new Train[4];
	
	Train train = new Train();
	train.name = "G101";
	train.departure = "北京南";
	train.departureTime = "07:00";
	train.termination = "上海虹桥";
	train.arrivalTime = "12:23";
	
	String[][] availables = new String[2][2];
	availables[0][0] = "二等软座";
	availables[0][1] = "有票";
	availables[1][0] = "一等软座";
	availables[1][1] = "3";
	train.availables = availables;
	
	_trains[0] = train;
	
	train = new Train();
	train.name = "G105";
	train.departure = "北京南";
	train.departureTime = "07:30";
	train.termination = "上海虹桥";
	train.arrivalTime = "13:07";
	
	availables = new String[2][2];
	availables[0][0] = "二等软座";
	availables[0][1] = "无票";
	availables[1][0] = "一等软座";
	availables[1][1] = "5";
	train.availables = availables;
	
	_trains[1] = train;
	
	train = new Train();
	train.name = "D365";
	train.departure = "北京南";
	train.departureTime = "07:35";
	train.termination = "上海虹桥";
	train.arrivalTime = "15:42";
	
	availables = new String[4][2];
	availables[0][0] = "二等软座";
	availables[0][1] = "有票";
	availables[1][0] = "一等软座";
	availables[1][1] = "有票";
	availables[2][0] = "软卧上";
	availables[2][1] = "有票";
	availables[3][0] = "软卧下";
	availables[3][1] = "有票";
	train.availables = availables;
	
	_trains[2] = train;
	
	train = new Train();
	train.name = "T109";
	train.departure = "北京";
	train.departureTime = "19:33";
	train.termination = "上海";
	train.arrivalTime = "10:26";
	    
	availables = new String[8][2];
	availables[0][0] = "硬座";
	availables[0][1] = "有票";
	availables[1][0] = "硬卧上";
	availables[1][1] = "有票";
	availables[2][0] = "硬卧中";
	availables[2][1] = "有票";
	availables[3][0] = "硬卧下";
	availables[3][1] = "有票";
	availables[4][0] = "软卧上";
	availables[4][1] = "有票";
	availables[5][0] = "软卧下";
	availables[5][1] = "无票";
	availables[6][0] = "高级软卧上";
	availables[6][1] = "有票";
	availables[7][0] = "高级软卧下";
	availables[7][1] = "5";
	train.availables = availables;
	
	_trains[3] = train;       
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
}

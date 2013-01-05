package org.ng12306.tpms;

import java.io.Serializable;

// 默认打算使用ObjectOutputStream来将对象保存到文件里
// 虽然序列化需要一些计算，但相比io的速度来说，应该是很快了
// 因此暂时先用Serializable接口来看看
public class TicketEvent implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -8576176057379004677L;
	private TicketEventType _type;
    public TicketEventType getType() { return _type; }

    // 在disruptor队列里的序列号
    // 因为都是异步发送返回消息，因此需要有一个唯一的标识
    // 来匹配响应消息和原来的请求消息，长整型应该足够了
    // 不会出现溢出导致响应消息和溢出转了一圈的请求消息
    // 相匹配的情况，况且我们的机器还会每天重启。
    public long sequence = -1;

    public TicketEvent(TicketEventType type) {
	_type = type;
    }
}

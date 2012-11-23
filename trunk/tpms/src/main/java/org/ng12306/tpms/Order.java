package org.ng12306.tpms;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Order {
    // 车次号
    public String train;

    // 上车站点
    public String departure;

    // 下车站点
    public String termination;

	// 座位号
	public String seat;

	// 身份证号
	public String id;

    // 上车时间
    public String date;
}
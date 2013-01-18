package org.ng12306.tpms;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

@XmlRootElement
public class Train implements Serializable {
    // 车次号
    public String name;

    // 始发站
    public String departure;

    // 发车时间
    public String departureTime;

    // 终点站
    public String termination;

    // 到达时间
    public String arrivalTime;

    // 余票信息
    @XmlElement
    public String[][] availables;
}

package org.ng12306.tpms;

import org.joda.time.DateTime;

//
// 这代表车票数据库，应该支持增删改查等操作。
// 车票数据跟我们平时认为的mysql数据库不一样，它可以从mysql里的数据创建
// 也可以直接从内存数据库里创建。
//
// 今后或许会将其公共函数提取成一个接口IRepository，并使用Ioc的方式注入
// 但现在只是自下而上的封装方式，不着急把类设计的很完善。
// 
public class TicketRepository {
    static {
	prepareFakeTicketPool();	
    }
    
    /**
     * 根据车次号和乘车日期查询车次的详细信息
     * @param name 要查询的车次号
     * @param startDate 要查询的乘车起始日期
     * @param endDate 要查询的结束日期
     * @return 一个包含车次详细信息的Train对象，否则返回null
     * @see Train
     */
    public static Train queryTrain(String name, 
				   DateTime startDate, 
				   DateTime endDate) {
	for ( int i = 0; i < _trains.length; ++i ) {
	    Train train = _trains[i];		
	    if ( train.name.compareTo(name) == 0 ) {
		return train;
	    }
	}

	return null;
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
}

package org.ng12306.tpms;

import com.sun.jersey.spi.resource.Singleton;
import com.sun.jersey.api.json.JSONWithPadding;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;

@Singleton
@Path("/ticket")
public class TicketResource {
    // 由于需要支持跨域访问，默认的json格式不支持跨域访问，需要使用jsonp格式。
	// 所以在Produces属性里添加了"application/x-javascript; charset=UTF-8"，
	// 每个MIME类型后面加上charset是为了防止中文输出乱码的问题
	// 
	// 把问题详细描述一下，因为前台是一个single page web app，所有的数据都是通过
	// ajax调用restful api的方式调用。但这带来一个问题，原型系统里我没有装tomcat
	// 采用的jersey restful库是自己启动的web服务器响应ajax调用的，因此就无法做到
	// single page web app和restful service在同一个域里面。
	// 
	// 在jsonp格式里面，其实浏览器会将restful service返回的json对象当作一段javascript
	// 代码处理，导致javascript解释器因为语法错误停止工作，因此需要动态创建一个callback
	// 函数来绕过这个限制。
	// 
	// 上面的话可能不好理解，当我们以json格式向restful service发送请求时，
	// $.ajax({ url: 'restful-url', dataType: 'json' });
	// 人家返回的是如下格式：
	// { key: 'value' }
	//
	// 但如果以jsonp格式发送时，请求的url后面需要带一个参数，指明要动态生成的callback函数
	// $.ajax({ url: 'restful-url?jsonpcallback=jpcb', dataType: 'jsonp', jsonp: 'jpcb' });
	// 返回的是如下格式：
	// jpcb({key: 'value'});
	// 
    // 参考文献:http://weblogs.java.net/blog/felipegaucho/archive/2010/02/25/jersey-feat-jquery-jsonp
	//         http://api.jquery.com/jQuery.ajax/
	//         http://forum.jquery.com/topic/ajax-jsonpcallback
    @GET
    @Path("/id/{trainId}")
    @Produces({"application/x-javascript; charset=UTF-8", "application/json; charset=UTF-8"})
    public JSONWithPadding query(@QueryParam("jsonpcallback") @DefaultValue("jsonpcallback") String callback,
	                     @PathParam("trainId") String trainId) {
	Train[] trains = queryImpl(trainId);

	return new JSONWithPadding(
	    new GenericEntity<Train[]>(trains) {},
		callback);
    }

	@GET
    @Path("/make")
    @Produces({"application/x-javascript; charset=UTF-8", "application/json; charset=UTF-8"})
	public String make(@QueryParam("jsonpcallback") @DefaultValue("jsonpcallback") String callback, 
	                   @QueryParam("train") String train,
	                   @QueryParam("seat") String seat,
	                   @QueryParam("departure") String departure,
	                   @QueryParam("termination") String termination,
	                   @QueryParam("id") String id,
	                   @QueryParam("date") String date) {
		if ( train == null ) {
			throw new IllegalArgumentException("车次号不应该为空！");
		}
		if ( seat == null ) {
			throw new IllegalArgumentException("座位号不应该为空！");
		}
		if ( departure == null ) {
			throw new IllegalArgumentException("出发站点号不应该为空！");
		}
		if ( termination == null ) {
			throw new IllegalArgumentException("到达站点不应该为空！");
		}
		if ( id == null ) {
			throw new IllegalArgumentException("身份证号不应该为空！");
		}
		if ( date == null ) {
			throw new IllegalArgumentException("出发日期不应该为空！");
		}

		return "true";
	}

	public Train[] queryImpl(String trainId) {
	Train[] trains = new Train[4];
	
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
	
	trains[0] = train;
	
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
	
	trains[1] = train;
	
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
	
	trains[2] = train;
	
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
	
	trains[3] = train;

	return trains;
	}
}

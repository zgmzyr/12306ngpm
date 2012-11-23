package org.ng12306.tpms;

import com.sun.jersey.spi.resource.Singleton;
import javax.ws.rs.*;

@Singleton
@Path("/ticket")
public class TicketResource {
    @GET
    @Path("/id/{trainId}")
    @Produces("application/json; charset=UTF-8")
    public Train[] query(@PathParam("trainId") String trainId) {
	Train[] trains = new Train[1];
	
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

	return trains;
    }
}

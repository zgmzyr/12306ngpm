package org.ng12306.tpms;

import javax.ws.rs.*;

@Path("/helloworld")
public class HelloWorldResource {
    @GET
    @Produces("text/plain")
    public String getClichedMessage() {
	return "Hello World";
    }
}

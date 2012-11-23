
package org.ng12306.tpms;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import junit.framework.TestCase;


public class MainTest extends TestCase {

    private SelectorThread threadSelector;
    
    private WebResource r;

    public MainTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        threadSelector = Main.startServer();

        Client c = Client.create();
        r = c.resource(Main.BASE_URI);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        threadSelector.stopEndpoint();
    }

    /**
     * Test to see that the message "Got it!" is sent in the response.
	 * TODO: 需要找回myresource这个由maven自带的路径
	 * 它已经被我删掉了，但后面想想还是应该有这么一个玩意，用以在排错时
	 * 确保jersey是正常工作的。
    public void _testMyResource() {
        String responseMsg = r.path("myresource").get(String.class);
        assertEquals("Got it!", responseMsg);
    }
     */

    /**
     * Test if a WADL document is available at the relative path
     * "application.wadl".
     */
    public void testApplicationWadl() {
        String serviceWadl = r.path("application.wadl").
                accept(MediaTypes.WADL).get(String.class);
                
        assertTrue(serviceWadl.length() > 0);
    }
}

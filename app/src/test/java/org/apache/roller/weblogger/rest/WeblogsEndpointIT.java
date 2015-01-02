 /*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.rest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class WeblogsEndpointIT extends TestCase {

    protected static Log log = LogFactory.getFactory().getInstance(WeblogsEndpointIT.class);

    @ArquillianResource
    private URL webappUrl;


    @Deployment(testable = false)
    public static WebArchive createDeployment() {

        final WebArchive wrapped;
        try {
            wrapped = ShrinkWrap.createFromZipFile(WebArchive.class, new File("target/roller.war"));
        } catch (Throwable fit) {
            fit.printStackTrace();
            throw fit;
        }

        return wrapped;
    }



    @Test
    public void testSimpleGet() throws Exception {

        String baseUrl = webappUrl.toString();

        WebClient client = WebClient.create( baseUrl );
        client = client.path("/");
        String response = client.get(String.class);
        Assert.assertNotEquals(-1, response.indexOf("Front Page: Welcome to Roller!"));
    }


    @Test
    public void testAuthenticatedGet() throws Exception {

        String weblogId;

        User admin = null;
        User dave = null;
        Weblog davesblog = null;
        Weblog frontpageblog = null;

        TestUtils.setupWeblogger();

        PropertiesManager pmgr = WebloggerFactory.getWeblogger().getPropertiesManager();
        UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();

        try {

            admin = TestUtils.setupUser("admindude"); 
            umgr.grantRole("admin", admin);

            // create frontpageblog owned by admin

            frontpageblog = TestUtils.setupWeblog("frontpageblog", admin);
            weblogId = frontpageblog.getId();
            RuntimeConfigProperty frontpageProp = pmgr.getProperty("site.frontpage.weblog.handle");
            frontpageProp.setValue(frontpageblog.getHandle());
            pmgr.saveProperty(frontpageProp);

            // create davesblog owned by dave
            dave = TestUtils.setupUser("davedude");
            davesblog = TestUtils.setupWeblog("davesblog", dave);

            TestUtils.endSession(true);

        } catch (Exception ex) {
            log.error("Error setting up data for test", ex);
            throw new RuntimeException("Error setting Roller up for test", ex);
        }


        try {

            // test that user dave cannot get /weblogs
            assertGetDenied(dave, "/weblogs");

            // test that user admin can get /weblogs
            assertGetCollectionAllowed(admin, "/weblogs", 2);

            // test that user dave can get /weblogs/testblog
            assertGetEntityAllowed(dave, "/weblogs/" + davesblog.getHandle() );

            // test that user admin can get /weblogs/testblog
            assertGetEntityAllowed(admin, "/weblogs/" + davesblog.getHandle() );

            // test that anonymous user cannot get /weblogs
            assertGetDenied(null, "/weblogs");

            // test that anonymous user cannot get /weblogs/testblog
            assertGetDenied(null, "/weblogs" + davesblog.getHandle() );


        } finally {
            TestUtils.teardownUser( dave.getUserName() );
            TestUtils.teardownUser( admin.getUserName() );

            TestUtils.teardownWeblog( weblogId );

            TestUtils.shutdownWeblogger();
        }
    }


    public void assertGetEntityAllowed( User userOrNull, String path ) throws IOException {

        WebClient client = WebClient.create( webappUrl.toString() ).path( "/roller-services/rest" + path );

        if ( userOrNull != null ) {
            String  creds = userOrNull.getUserName() + ":" + userOrNull.getPassword();
            String authorizationHeader = "Basic " + Base64Utility.encode(creds.getBytes());
            client = client.header("Authorization", authorizationHeader);
        }

        log.debug("About to GET " + client.getCurrentURI()
                + " with Authentication: " + client.getHeaders().get("Authorization"));

        String responseString = client.get(String.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = mapper.readValue(
            responseString, new TypeReference<Map<String, Object>>() {
        });

        Assert.assertEquals(1, responseMap.size());
    }

    public void assertGetDenied( User userOrNull, String path ) throws IOException {

        WebClient client = WebClient.create( webappUrl.toString() ).path( "/roller-services/rest" + path );

        if ( userOrNull != null ) {
            String  creds = userOrNull.getUserName() + ":" + userOrNull.getPassword();
            String authorizationHeader = "Basic " + Base64Utility.encode(creds.getBytes());
            client = client.header("Authorization", authorizationHeader);
        }

        log.debug("About to GET " + client.getCurrentURI()
                + " with Authentication: " + client.getHeaders().get("Authorization"));

        try {
            client.get(String.class);
            fail("Get should have failed");

        } catch ( Throwable t ) {
            // expected
        }

    }

    public void assertGetCollectionAllowed( User userOrNull, String path, int expectedCount ) throws IOException {

        WebClient client = WebClient.create( webappUrl.toString() ).path( "/roller-services/rest" + path );

        if ( userOrNull != null ) {
            String  creds = userOrNull.getUserName() + ":" + userOrNull.getPassword();
            String authorizationHeader = "Basic " + Base64Utility.encode(creds.getBytes());
            client = client.header("Authorization", authorizationHeader);
        }

        log.debug("About to GET " + client.getCurrentURI()
                + " with Authentication: " + client.getHeaders().get("Authorization"));

        String responseString = client.get(String.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = mapper.readValue(
            responseString, new TypeReference<Map<String, Object>>() {
        });

        Assert.assertEquals(1, responseMap.size());

        String key = responseMap.keySet().iterator().next();
        List weblogList = (List)responseMap.get( key );
        Assert.assertEquals( expectedCount, weblogList.size() );
    }
}

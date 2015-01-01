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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.net.URL;
import java.util.Map;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.PropertiesManager;
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

        User dave = null;

        String creds;

        try {
            TestUtils.setupWeblogger();

            dave = TestUtils.setupUser("dave");

            Weblog weblog = TestUtils.setupWeblog("testblog", dave);
            weblogId = weblog.getId();
            PropertiesManager mgr = WebloggerFactory.getWeblogger().getPropertiesManager();
            RuntimeConfigProperty frontpageProp = mgr.getProperty("site.frontpage.weblog.handle");
            frontpageProp.setValue(weblog.getHandle());
            mgr.saveProperty(frontpageProp);

            TestUtils.endSession(true);

            creds = dave.getUserName() + ":" + dave.getPassword();

        } catch (Exception ex) {
            log.error("Error setting up data for test", ex);
            throw new RuntimeException("Error setting Roller up for test", ex);
        }


        try {
            String baseUrl = webappUrl.toString();

            WebClient client = WebClient.create( baseUrl );

            String authorizationHeader = "Basic " + Base64Utility.encode(creds.getBytes());

            client = client.path("/roller-services/rest/weblogs/")
                .header("Authorization", authorizationHeader);

            log.debug("GET " + client.getCurrentURI() + " Authentication: " + authorizationHeader );

            String responseString = client.get( String.class );

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue( 
                    responseString,  new TypeReference<Map<String, Object>>() { } );

            Assert.assertEquals( 1, responseMap.size() );

        } finally {
            TestUtils.teardownUser( dave.getUserName() );
            TestUtils.teardownWeblog( weblogId );

            TestUtils.shutdownWeblogger();
        }
    }
}

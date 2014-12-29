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

import java.io.File;
import java.net.URL;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.ws.commons.util.Base64;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class WeblogsEndpointIT extends TestCase {

    protected static Log log = LogFactory.getFactory().getInstance(WeblogsEndpointIT.class);

    private String WEBROOT_INDEX = "src/main/webapp";

    private int port = 8080;

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


    @Before
    public void setUp() throws Exception {

        try {
            TestUtils.setupWeblogger();

            User dave = TestUtils.setupUser("dave");

            Weblog weblog = TestUtils.setupWeblog("testblog", dave);

            PropertiesManager mgr = WebloggerFactory.getWeblogger().getPropertiesManager();
            RuntimeConfigProperty frontpageProp = mgr.getProperty("site.frontpage.weblog.handle");
            frontpageProp.setValue(weblog.getHandle());
            mgr.saveProperty(frontpageProp);

            TestUtils.endSession(true);

        } catch (Exception ex) {
            log.error("Error setting up Roller for test", ex);
            throw new RuntimeException("Error setting Roller up for test", ex);
        }

    }


    @Test
    public void testSimpleGet() throws Exception {

        String baseUrl = webappUrl.toString(); 

        WebClient client = WebClient.create( baseUrl );
        client = client.path("/");
        String response = client.get(String.class);
        Assert.assertNotEquals(-1, response.indexOf("Front Page: Welcome to Roller!"));

        client = WebClient.create( baseUrl );
        String creds = "dave:password";
        byte[] credbytes = creds.getBytes("UTF-8");
        client = client.path("/roller/roller-services/rest/weblogs")
                .header("Authorization", "Basic " + Base64.encode(credbytes).trim());
        response = client.get(String.class);
        Assert.assertEquals("OK", response);
    }
}

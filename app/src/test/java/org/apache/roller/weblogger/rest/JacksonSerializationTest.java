package org.apache.roller.weblogger.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.Test;

import java.util.List;


public class JacksonSerializationTest {

    protected static Log log = LogFactory.getFactory().getInstance(WeblogsEndpointIT.class);

    @Test
    public void testSerialization() throws Exception {

        User dave = null;

        Weblog weblog0, weblog1, weblog2, weblog3, weblog4;

        String creds;

        try {

            TestUtils.setupWeblogger();

            dave = TestUtils.setupUser("dave");

            weblog0 = TestUtils.setupWeblog("testblog", dave);
            PropertiesManager mgr = WebloggerFactory.getWeblogger().getPropertiesManager();
            RuntimeConfigProperty frontpageProp = mgr.getProperty("site.frontpage.weblog.handle");
            frontpageProp.setValue(weblog0.getHandle());
            mgr.saveProperty(frontpageProp);

            weblog1 = TestUtils.setupWeblog("testblog1", dave);
            weblog2 = TestUtils.setupWeblog("testblog2", dave);
            weblog3 = TestUtils.setupWeblog("testblog3", dave);
            weblog4 = TestUtils.setupWeblog("testblog4", dave);

            TestUtils.endSession(true);

            creds = dave.getUserName() + ":" + dave.getPassword();

        } catch (Exception ex) {
            log.error("Error setting up data for test", ex);
            throw new RuntimeException("Error setting Roller up for test", ex);
        }


        try {

            List<Weblog> weblogs = WebloggerFactory.getWeblogger().getWeblogManager()
                    .getWeblogs(Boolean.TRUE, Boolean.TRUE, null, null, 0, 100);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString( weblogs );
            log.info("json = " + json);

        } finally {

            TestUtils.teardownUser( dave.getUserName() );

            TestUtils.teardownWeblog(weblog0.getId());
            TestUtils.teardownWeblog(weblog1.getId());
            TestUtils.teardownWeblog(weblog2.getId());
            TestUtils.teardownWeblog(weblog3.getId());
            TestUtils.teardownWeblog(weblog4.getId());
        }
    }
}

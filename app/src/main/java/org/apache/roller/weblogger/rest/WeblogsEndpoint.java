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

 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.roller.weblogger.business.WeblogManager;
 import org.apache.roller.weblogger.business.Weblogger;
 import org.apache.roller.weblogger.business.WebloggerFactory;
 import org.apache.roller.weblogger.pojos.Weblog;
 import org.apache.roller.weblogger.rest.auth.RequireUser;
 import org.apache.roller.weblogger.rest.auth.RequireWeblogAdmin;

 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.Response;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
import javax.ws.rs.Produces;


 @Path("/weblogs")
public class WeblogsEndpoint {

    protected static final Log log = LogFactory.getFactory().getInstance(WeblogsEndpoint.class);

    private static final ObjectMapper mapper = new ObjectMapper();


    /** Get all weblogs in system. */
    @GET
    @Path("/")
    //@RequireGlobalAdmin
    @RequireUser
    @Produces("application/json")
    public Response getWeblogs() {

        int offset = 0;
        int length = 100;

        Weblogger roller = WebloggerFactory.getWeblogger();
        WeblogManager weblogMgr = roller.getWeblogManager();

        try {
            List<Weblog> weblogs = weblogMgr.getWeblogs(
                    Boolean.TRUE, // enabled
                    Boolean.TRUE, // active
                    null, // startDate
                    null, // endDate
                    offset,
                    length);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put( "weblogs", weblogs );
            String responseJson = mapper.writeValueAsString( responseMap );

            return Response.ok( responseJson ).build();

        } catch (Exception ex) {
            log.error("Error returning weblogs", ex);
            return Response.serverError().entity("Error returning weblogs").build();
        }
    }
  

    /** Get one weblog. */
    @PUT
    @Path("/{handle}")
    @RequireWeblogAdmin
    public Response putWeblog() {
        return Response.ok("OK").build();
    }

    
    /** Post weblog creates a new weblog. */
    @POST
    @Path("/{handle}")
    @RequireUser
    public Response postWeblog() {
        return Response.ok("OK").build();
    }
    
}

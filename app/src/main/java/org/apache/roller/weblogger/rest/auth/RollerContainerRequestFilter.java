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
package org.apache.roller.weblogger.rest.auth;


import org.apache.roller.weblogger.rest.exceptions.ServerErrorException;
import org.apache.roller.weblogger.rest.exceptions.AccessDeniedException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context; 
import javax.ws.rs.ext.Provider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;


/**
 * Ensure that user has roles and permissions necessary to access requested resource.
 * @author snoopdave 
 */
@Provider
public class RollerContainerRequestFilter implements ContainerRequestFilter {

    protected static Log log = 
            LogFactory.getFactory().getInstance(RollerContainerRequestFilter.class);

    @Context
    private ResourceInfo resourceInfo;


    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {

        log.debug("Entering filter() with header Authentication: " 
            + ctx.getHeaderString("Authorization"));

        try {
            Method method = resourceInfo.getResourceMethod();
            Subject subject = SecurityUtils.getSubject();
            log.debug("subject.isAuthenticated(): " + subject.isAuthenticated());

            Weblogger weblogger = WebloggerFactory.getWeblogger();
            Weblog weblog = null;
            String uri = ctx.getUriInfo().getPath();
            String[] parts = uri.split("/");
            if ( parts[0].equals("weblogs") && parts.length > 1 ) {
                String weblogHandle = parts[1];
                weblog = weblogger.getWeblogManager().getWeblogByHandle( weblogHandle );
            }

            if (method.isAnnotationPresent(RequireGlobalAdmin.class)) {
                User user = getAuthenticatedUser( subject );
                if (!WebloggerFactory.getWeblogger().getUserManager().hasRole("admin", user)) {
                    throw new AccessDeniedException("admin role is required for this resource");
                }
            }
            if (method.isAnnotationPresent(RequireWeblogAdmin.class)) {
                User user = getAuthenticatedUser( subject );
                checkWeblogPermission(weblog, user, Collections.singletonList("admin"));
            }
            if (method.isAnnotationPresent(RequireWeblogAuthor.class)) {
                User user = getAuthenticatedUser( subject );
                checkWeblogPermission(weblog, user, Collections.singletonList("author"));
            }
            if (method.isAnnotationPresent(RequireWeblogLimited.class)) {
                User user = getAuthenticatedUser( subject );
                checkWeblogPermission(weblog, user, Collections.singletonList("limited"));
            }
            if (method.isAnnotationPresent(RequireUser.class)) {
                getAuthenticatedUser( subject );
            }
            
            log.debug("Filter found method = " + method.getName() 
                    + " and subject authenticated " + subject.isAuthenticated());

        } catch (WebloggerException ex) {
            throw new ServerErrorException("Error checking roles and permission", ex);
        }
    }


    private User getAuthenticatedUser( Subject subject ) throws WebloggerException {

        if ( subject == null || subject.getPrincipal() == null ) {
            throw new AccessDeniedException("Authenticated user required");
        }
        String username = subject.getPrincipal().toString();
        User user = WebloggerFactory.getWeblogger().getUserManager().getUser(username);

        return user;
    }


    private void checkWeblogPermission(
            Weblog weblog, User user, List<String> actions)
            throws WebloggerException, AccessDeniedException {

        if ( weblog == null ) {
            throw new AccessDeniedException("Weblog perm required but no weblog specified");
        }

        if ( !WebloggerFactory.getWeblogger().getUserManager()
                .checkPermission(new WeblogPermission(weblog, actions), user)) {

            throw new AccessDeniedException(
                    "Admin permission required in weblog "+ weblog.getHandle());
        }

    }
}

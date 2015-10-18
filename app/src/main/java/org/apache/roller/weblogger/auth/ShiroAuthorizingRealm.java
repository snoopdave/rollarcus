/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
package org.apache.roller.weblogger.auth;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Sha1Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;


/**
 * Shiro realm that pull users and roles from Roller's manager interfaces.
 * @author snoopdave
 */
public class ShiroAuthorizingRealm extends AuthorizingRealm {
    private static Log log = LogFactory.getLog(ShiroAuthorizingRealm.class);


    public ShiroAuthorizingRealm(){
        setName("ShiroAuthorizingRealm");
        setCredentialsMatcher(new HashedCredentialsMatcher(Sha1Hash.ALGORITHM_NAME));

        log.info("ShiroAuthorizingRealm()");
    }

    @Override
    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authToken) 
            throws AuthenticationException {

        log.info("ShiroAuthorizingRealm.doGetAuthenticationInfo()");

        UsernamePasswordToken token = (UsernamePasswordToken) authToken;

        User user; 
        try {
            user = loadUserByUsername( token.getUsername() );
            
        } catch (WebloggerException ex) {
            log.error("Error looking up user", ex);
            throw new AuthenticationException("Error looking up user " + token.getUsername(), ex);
        }

        if (user != null) {
            log.debug("Returning user " + user.getUserName() + " password " + user.getPassword());
            return new SimpleAuthenticationInfo(
                    user.getUserName(), user.getPassword(), getName());
        } else {
            log.error("Username not found: " + token.getUsername());
            throw new AuthenticationException("Username not found: " + token.getUsername());
        }
    }


    public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        log.info("ShiroAuthorizingRealm.doGetAuthorizationInfo()");

        String userName = (String) (principals.fromRealm(getName()).iterator().next());
        User user;
        try {
            user = loadUserByUsername( userName );
        } catch (WebloggerException ex) {
            throw new RuntimeException("Error looking up user " + userName, ex);
        }

        Weblogger roller = WebloggerFactory.getWeblogger();
        UserManager umgr = roller.getUserManager();

        if (user != null) {
            List<String> roles;
            try {
                roles = umgr.getRoles(user);
            } catch (WebloggerException ex) {
                throw new RuntimeException("Error looking up roles for user " + userName, ex);
            }
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            for ( String role : roles ) {
                info.addRole( role );
            }
            log.debug("Returning " + roles.size() + " roles for user " + userName + " roles= " + roles);
            return info;

        } else {
            throw new RuntimeException("Username not found: " + userName);
        }
    }

    @Override
    public boolean supports( AuthenticationToken token ) {
        return true;
    } 


    private User loadUserByUsername(String userName) throws WebloggerException {

        Weblogger roller = WebloggerFactory.getWeblogger();
        UserManager umgr = roller.getUserManager();

        User userData;

        if (userName.startsWith("http://") || userName.startsWith("https://")) {
            // is OpenId user

            if (userName.endsWith("/")) {
                userName = userName.substring(0, userName.length() -1 );
            }

            userData = umgr.getUserByOpenIdUrl(userName);
            if (userData == null) {
                log.warn("No user found with OpenID URL: " + userName 
                    +" (OpenID aliased by auth provider?) Confirm URL exists in roller_user table");
            }
            return userData;

        } else {
            return umgr.getUserByUserName(userName);
        }            
    }

}

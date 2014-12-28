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

package org.apache.roller.weblogger.auth;

import com.google.inject.Provides;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.config.Ini;
import org.apache.shiro.guice.ShiroModule;


// TODO: do we need the ShiroGuice integration at all?
public class ShiroGuiceModule extends ShiroModule {
    private static Log log = LogFactory.getLog(ShiroGuiceModule.class);

    public ShiroGuiceModule() {
        log.info("ShiroGuiceModule()");
    }

    protected void configureShiro() {
        log.info("ShiroGuiceModule.configureShiro()");
    }

    @Provides
    Ini loadShiroIni() {
        return Ini.fromResourcePath("classpath:shiro.ini");
    }
}

/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.runtime.sql2o;

import griffon.core.GriffonApplication;
import griffon.core.env.Metadata;
import griffon.inject.DependsOn;
import griffon.plugins.monitor.MBeanManager;
import griffon.plugins.sql2o.Sql2oCallback;
import griffon.plugins.sql2o.Sql2oFactory;
import griffon.plugins.sql2o.Sql2oHandler;
import griffon.plugins.sql2o.Sql2oStorage;
import org.codehaus.griffon.runtime.core.addon.AbstractGriffonAddon;
import org.codehaus.griffon.runtime.jmx.Sql2oStorageMonitor;
import org.sql2o.Sql2o;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

import static griffon.util.ConfigUtils.getConfigValueAsBoolean;

/**
 * @author Andres Almiray
 */
@DependsOn("datasource")
@Named("sql2o")
public class Sql2oAddon extends AbstractGriffonAddon {
    @Inject
    private Sql2oHandler sql2oHandler;

    @Inject
    private Sql2oFactory sql2oFactory;

    @Inject
    private Sql2oStorage sql2oStorage;

    @Inject
    private MBeanManager mbeanManager;

    @Inject
    private Metadata metadata;

    @Override
    public void init(@Nonnull GriffonApplication application) {
        mbeanManager.registerMBean(new Sql2oStorageMonitor(metadata, sql2oStorage));
    }

    public void onStartupStart(@Nonnull GriffonApplication application) {
        for (String dataSourceName : sql2oFactory.getDatasourceNames()) {
            Map<String, Object> config = sql2oFactory.getConfigurationFor(dataSourceName);
            if (getConfigValueAsBoolean(config, "connect_on_startup", false)) {
                sql2oHandler.withSql2o(dataSourceName, new Sql2oCallback<Void>() {
                    @Override
                    public Void handle(@Nonnull String dataSourceName, @Nonnull Sql2o sql2o) {
                        return null;
                    }
                });
            }
        }
    }

    public void onShutdownStart(@Nonnull GriffonApplication application) {
        for (String dataSourceName : sql2oFactory.getDatasourceNames()) {
            sql2oHandler.closeSql2o(dataSourceName);
        }
    }
}

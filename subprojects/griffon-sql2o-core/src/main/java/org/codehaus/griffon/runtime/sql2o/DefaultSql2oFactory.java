/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2021 The author and/or original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.runtime.sql2o;

import griffon.annotations.core.Nonnull;
import griffon.core.Configuration;
import griffon.core.GriffonApplication;
import griffon.core.env.Metadata;
import griffon.core.injection.Injector;
import griffon.plugins.datasource.DataSourceFactory;
import griffon.plugins.datasource.DataSourceStorage;
import griffon.plugins.monitor.MBeanManager;
import griffon.plugins.sql2o.Sql2oBootstrap;
import griffon.plugins.sql2o.Sql2oFactory;
import griffon.plugins.sql2o.events.Sql2oConnectEndEvent;
import griffon.plugins.sql2o.events.Sql2oConnectStartEvent;
import griffon.plugins.sql2o.events.Sql2oDisconnectEndEvent;
import griffon.plugins.sql2o.events.Sql2oDisconnectStartEvent;
import org.codehaus.griffon.runtime.core.storage.AbstractObjectFactory;
import org.sql2o.Sql2o;
import org.sql2o.quirks.Db2Quirks;
import org.sql2o.quirks.NoQuirks;
import org.sql2o.quirks.OracleQuirks;
import org.sql2o.quirks.PostgresQuirks;
import org.sql2o.quirks.Quirks;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;

import static griffon.util.GriffonNameUtils.isBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class DefaultSql2oFactory extends AbstractObjectFactory<Sql2o> implements Sql2oFactory {
    @Inject
    private DataSourceFactory dataSourceFactory;

    @Inject
    private DataSourceStorage dataSourceStorage;

    @Inject
    private Injector injector;

    @Inject
    private MBeanManager mbeanManager;

    @Inject
    private Metadata metadata;

    @Inject
    public DefaultSql2oFactory(@Nonnull @Named("datasource") Configuration configuration, @Nonnull GriffonApplication application) {
        super(configuration, application);
    }

    @Nonnull
    @Override
    public Set<String> getDatasourceNames() {
        return dataSourceFactory.getDataSourceNames();
    }

    @Nonnull
    @Override
    public Map<String, Object> getConfigurationFor(@Nonnull String datasourceName) {
        return dataSourceFactory.getConfigurationFor(datasourceName);
    }

    @Nonnull
    @Override
    protected String getSingleKey() {
        return "dataSource";
    }

    @Nonnull
    @Override
    protected String getPluralKey() {
        return "dataSources";
    }

    @Nonnull
    @Override
    public Sql2o create(@Nonnull String name) {
        Map<String, Object> config = getConfigurationFor(name);
        event(Sql2oConnectStartEvent.of(name, config));
        Sql2o sql2o = createSql2o(name, config);

        for (Object o : injector.getInstances(Sql2oBootstrap.class)) {
            ((Sql2oBootstrap) o).init(name, sql2o);
        }

        event(Sql2oConnectEndEvent.of(name, config, sql2o));
        return sql2o;
    }

    @Override
    public void destroy(@Nonnull String name, @Nonnull Sql2o instance) {
        requireNonNull(instance, "Argument 'instance' must not be null");
        Map<String, Object> config = getConfigurationFor(name);
        event(Sql2oDisconnectStartEvent.of(name, config, instance));

        for (Object o : injector.getInstances(Sql2oBootstrap.class)) {
            ((Sql2oBootstrap) o).destroy(name, instance);
        }

        closeDataSource(name);

        event(Sql2oDisconnectEndEvent.of(name, config));
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    protected Sql2o createSql2o(@Nonnull String dataSourceName, @Nonnull Map<String, Object> config) {
        DataSource dataSource = getDataSource(dataSourceName);
        return new Sql2o(dataSource, resolveQuirks(config));
    }

    private Quirks resolveQuirks(@Nonnull Map<String, Object> config) {
        String quirks = String.valueOf(config.get("quirks"));
        if (isBlank(quirks) || "null".equals(quirks)) {
            return new NoQuirks();
        }

        switch (quirks.toLowerCase()) {
            case "none":
                return new NoQuirks();
            case "db2":
                return new Db2Quirks();
            case "oracle":
                return new OracleQuirks();
            case "postgres":
                return new PostgresQuirks();
        }

        try {
            Class<? extends Quirks> quirksClass = (Class<? extends Quirks>) Sql2oFactory.class.getClassLoader().loadClass(quirks);
            return quirksClass.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Quirks class: " + quirks);
        }
    }

    protected void closeDataSource(@Nonnull String dataSourceName) {
        DataSource dataSource = dataSourceStorage.get(dataSourceName);
        if (dataSource != null) {
            dataSourceFactory.destroy(dataSourceName, dataSource);
            dataSourceStorage.remove(dataSourceName);
        }
    }

    @Nonnull
    protected DataSource getDataSource(@Nonnull String dataSourceName) {
        DataSource dataSource = dataSourceStorage.get(dataSourceName);
        if (dataSource == null) {
            dataSource = dataSourceFactory.create(dataSourceName);
            dataSourceStorage.set(dataSourceName, dataSource);
        }
        return dataSource;
    }
}

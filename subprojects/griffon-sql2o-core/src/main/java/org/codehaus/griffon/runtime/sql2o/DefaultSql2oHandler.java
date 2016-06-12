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

import griffon.plugins.sql2o.Sql2oCallback;
import griffon.plugins.sql2o.Sql2oFactory;
import griffon.plugins.sql2o.Sql2oHandler;
import griffon.plugins.sql2o.Sql2oStorage;
import griffon.plugins.sql2o.exceptions.RuntimeSql2oException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class DefaultSql2oHandler implements Sql2oHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultSql2oHandler.class);
    private static final String ERROR_DATASOURCE_NAME_BLANK = "Argument 'datasourceName' must not be blank";
    private static final String ERROR_CALLBACK_NULL = "Argument 'callback' must not be null";

    private final Sql2oFactory sql2oFactory;
    private final Sql2oStorage sql2oStorage;

    @Inject
    public DefaultSql2oHandler(@Nonnull Sql2oFactory sql2oFactory, @Nonnull Sql2oStorage sql2oStorage) {
        this.sql2oFactory = requireNonNull(sql2oFactory, "Argument 'sql2oFactory' must not be null");
        this.sql2oStorage = requireNonNull(sql2oStorage, "Argument 'sql2oStorage' must not be null");
    }

    @Nullable
    @Override
    public <R> R withSql2o(@Nonnull Sql2oCallback<R> callback) throws RuntimeSql2oException {
        return withSql2o(DefaultSql2oFactory.KEY_DEFAULT, callback);
    }

    @Nullable
    @Override
    public <R> R withSql2o(@Nonnull String datasourceName, @Nonnull Sql2oCallback<R> callback) throws RuntimeSql2oException {
        requireNonBlank(datasourceName, ERROR_DATASOURCE_NAME_BLANK);
        requireNonNull(callback, ERROR_CALLBACK_NULL);
        Sql2o sql2o = getSql2o(datasourceName);
        try {
            LOG.debug("Executing statements on datasource '{}'", datasourceName);
            return callback.handle(datasourceName, sql2o);
        } catch (Exception e) {
            throw new RuntimeSql2oException(datasourceName, e);
        }
    }

    @Override
    public void closeSql2o() {
        closeSql2o(DefaultSql2oFactory.KEY_DEFAULT);
    }

    @Override
    public void closeSql2o(@Nonnull String datasourceName) {
        Sql2o sql2o = sql2oStorage.get(datasourceName);
        if (sql2o != null) {
            sql2oFactory.destroy(datasourceName, sql2o);
            sql2oStorage.remove(datasourceName);
        }
    }

    @Nonnull
    private Sql2o getSql2o(@Nonnull String datasourceName) {
        Sql2o sql2o = sql2oStorage.get(datasourceName);
        if (sql2o == null) {
            sql2o = sql2oFactory.create(datasourceName);
            sql2oStorage.set(datasourceName, sql2o);
        }
        return sql2o;
    }
}

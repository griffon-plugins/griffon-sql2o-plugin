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
package griffon.plugins.sql2o;

import griffon.plugins.sql2o.exceptions.RuntimeSql2oException;

import griffon.annotations.core.Nonnull;
import griffon.annotations.core.Nullable;

/**
 * @author Andres Almiray
 */
public interface Sql2oHandler {
    // tag::methods[]
    @Nullable
    <R> R withSql2o(@Nonnull Sql2oCallback<R> callback)
        throws RuntimeSql2oException;

    @Nullable
    <R> R withSql2o(@Nonnull String datasourceName, @Nonnull Sql2oCallback<R> callback)
        throws RuntimeSql2oException;

    void closeSql2o();

    void closeSql2o(@Nonnull String datasourceName);
    // end::methods[]
}
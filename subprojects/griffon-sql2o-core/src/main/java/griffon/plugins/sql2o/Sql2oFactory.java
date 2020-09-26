/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2020 The author and/or original authors.
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

import griffon.core.storage.ObjectFactory;
import org.sql2o.Sql2o;

import griffon.annotations.core.Nonnull;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 */
public interface Sql2oFactory extends ObjectFactory<Sql2o> {
    @Nonnull
    Set<String> getDatasourceNames();

    @Nonnull
    Map<String, Object> getConfigurationFor(@Nonnull String datasourceName);
}

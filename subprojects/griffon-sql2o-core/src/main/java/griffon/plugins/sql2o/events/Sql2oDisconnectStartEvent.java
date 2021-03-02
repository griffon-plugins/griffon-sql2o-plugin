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
package griffon.plugins.sql2o.events;

import griffon.annotations.core.Nonnull;
import griffon.core.event.Event;
import org.sql2o.Sql2o;

import java.util.Map;

import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 3.0.0
 */
public class Sql2oDisconnectStartEvent extends Event {
    private final String name;
    private final Map<String, Object> config;
    private final Sql2o sql2o;

    public Sql2oDisconnectStartEvent(@Nonnull String name, @Nonnull Map<String, Object> config, @Nonnull Sql2o sql2o) {
        this.name = requireNonBlank(name, "Argument 'name' must not be blank");
        this.config = requireNonNull(config, "Argument 'config' must not be null");
        this.sql2o = requireNonNull(sql2o, "Argument 'sql2o' must not be null");
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public Map<String, Object> getConfig() {
        return config;
    }

    @Nonnull
    public Sql2o getSql2o() {
        return sql2o;
    }

    @Nonnull
    public static Sql2oDisconnectStartEvent of(@Nonnull String name, @Nonnull Map<String, Object> config, @Nonnull Sql2o sql2o) {
        return new Sql2oDisconnectStartEvent(name, config, sql2o);
    }
}

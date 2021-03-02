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
package org.codehaus.griffon.runtime.sql2o.monitor;

import griffon.core.env.Metadata;
import griffon.plugins.sql2o.Sql2oStorage;
import org.codehaus.griffon.runtime.monitor.AbstractObjectStorageMonitor;
import org.sql2o.Sql2o;

import griffon.annotations.core.Nonnull;

/**
 * @author Andres Almiray
 */
public class Sql2oStorageMonitor extends AbstractObjectStorageMonitor<Sql2o> implements Sql2oStorageMonitorMXBean {
    public Sql2oStorageMonitor(@Nonnull Metadata metadata, @Nonnull Sql2oStorage delegate) {
        super(metadata, delegate);
    }

    @Override
    protected String getStorageName() {
        return "sql2o";
    }
}

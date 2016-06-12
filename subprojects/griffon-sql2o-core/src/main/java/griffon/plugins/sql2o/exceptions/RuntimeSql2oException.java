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
package griffon.plugins.sql2o.exceptions;

import griffon.exceptions.GriffonException;

import javax.annotation.Nonnull;

import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class RuntimeSql2oException extends GriffonException {
    private final String datasourceName;

    public RuntimeSql2oException(@Nonnull String datasourceName, @Nonnull Exception sqle) {
        super(format(datasourceName), requireNonNull(sqle, "sqle"));
        this.datasourceName = datasourceName;
    }

    @Nonnull
    private static String format(@Nonnull String datasourceName) {
        requireNonBlank(datasourceName, "datasourceName");
        return "An error occurred when executing a statement on sql2o '" + datasourceName + "'";
    }

    @Nonnull
    public String getSql2oName() {
        return datasourceName;
    }
}

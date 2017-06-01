/*
 * Copyright 2014-2017 the original author or authors.
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
package org.codehaus.griffon.compile.sql2o;

import org.codehaus.griffon.compile.core.BaseConstants;
import org.codehaus.griffon.compile.core.MethodDescriptor;

import static org.codehaus.griffon.compile.core.MethodDescriptor.annotatedMethod;
import static org.codehaus.griffon.compile.core.MethodDescriptor.annotatedType;
import static org.codehaus.griffon.compile.core.MethodDescriptor.annotations;
import static org.codehaus.griffon.compile.core.MethodDescriptor.args;
import static org.codehaus.griffon.compile.core.MethodDescriptor.method;
import static org.codehaus.griffon.compile.core.MethodDescriptor.throwing;
import static org.codehaus.griffon.compile.core.MethodDescriptor.type;
import static org.codehaus.griffon.compile.core.MethodDescriptor.typeParams;
import static org.codehaus.griffon.compile.core.MethodDescriptor.types;

/**
 * @author Andres Almiray
 */
public interface Sql2oAwareConstants extends BaseConstants {
    String SQL2O_HANDLER_TYPE = "griffon.plugins.sql2o.Sql2oHandler";
    String SQL2O_CALLBACK_TYPE = "griffon.plugins.sql2o.Sql2oCallback";
    String RUNTIME_SQL2O_EXCEPTION_TYPE = "griffon.plugins.sql2o.exceptions.RuntimeSql2oException";
    String SQL2O_HANDLER_PROPERTY = "sql2oHandler";
    String SQL2O_HANDLER_FIELD_NAME = "this$" + SQL2O_HANDLER_PROPERTY;

    String METHOD_WITH_SQL2O = "withSql2o";
    String METHOD_CLOSE_SQL2O = "closeSql2o";
    String DATASOURCE_NAME = "datasourceName";
    String CALLBACK = "callback";

    MethodDescriptor[] METHODS = new MethodDescriptor[]{
        method(
            type(VOID),
            METHOD_CLOSE_SQL2O
        ),
        method(
            type(VOID),
            METHOD_CLOSE_SQL2O,
            args(annotatedType(types(type(JAVAX_ANNOTATION_NONNULL)), JAVA_LANG_STRING))
        ),

        annotatedMethod(
            annotations(JAVAX_ANNOTATION_NONNULL),
            type(R),
            typeParams(R),
            METHOD_WITH_SQL2O,
            args(annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), SQL2O_CALLBACK_TYPE, R)),
            throwing(type(RUNTIME_SQL2O_EXCEPTION_TYPE))
        ),
        annotatedMethod(
            types(type(JAVAX_ANNOTATION_NONNULL)),
            type(R),
            typeParams(R),
            METHOD_WITH_SQL2O,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), SQL2O_CALLBACK_TYPE, R)),
            throwing(type(RUNTIME_SQL2O_EXCEPTION_TYPE))
        )
    };
}

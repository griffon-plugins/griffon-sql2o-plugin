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
package org.codehaus.griffon.compile.sql2o.ast.transform

import griffon.plugins.sql2o.Sql2oHandler
import spock.lang.Specification

import java.lang.reflect.Method

/**
 * @author Andres Almiray
 */
class Sql2oAwareASTTransformationSpec extends Specification {
    def 'Sql2oAwareASTTransformation is applied to a bean via @Sql2oAware'() {
        given:
        GroovyShell shell = new GroovyShell()

        when:
        def bean = shell.evaluate('''
        @griffon.transform.Sql2oAware
        class Bean { }
        new Bean()
        ''')

        then:
        bean instanceof Sql2oHandler
        Sql2oHandler.methods.every { Method target ->
            bean.class.declaredMethods.find { Method candidate ->
                candidate.name == target.name &&
                candidate.returnType == target.returnType &&
                candidate.parameterTypes == target.parameterTypes &&
                candidate.exceptionTypes == target.exceptionTypes
            }
        }
    }

    def 'Sql2oAwareASTTransformation is not applied to a Sql2oHandler subclass via @Sql2oAware'() {
        given:
        GroovyShell shell = new GroovyShell()

        when:
        def bean = shell.evaluate('''
        import griffon.plugins.sql2o.Sql2oCallback
        import griffon.plugins.sql2o.exceptions.RuntimeSql2oException
        import griffon.plugins.sql2o.Sql2oHandler

        import javax.annotation.Nonnull
        @griffon.transform.Sql2oAware
        class Sql2oHandlerBean implements Sql2oHandler {
            @Override
            public <R> R withSql2o(@Nonnull Sql2oCallback<R> callback) throws RuntimeSql2oException {
                return null
            }
            @Override
            public <R> R withSql2o(@Nonnull String datasourceName, @Nonnull Sql2oCallback<R> callback) throws RuntimeSql2oException {
                return null
            }
            @Override
            void closeSql2o(){}
            @Override
            void closeSql2o(@Nonnull String datasourceName){}
        }
        new Sql2oHandlerBean()
        ''')

        then:
        bean instanceof Sql2oHandler
        Sql2oHandler.methods.every { Method target ->
            bean.class.declaredMethods.find { Method candidate ->
                candidate.name == target.name &&
                    candidate.returnType == target.returnType &&
                    candidate.parameterTypes == target.parameterTypes &&
                    candidate.exceptionTypes == target.exceptionTypes
            }
        }
    }
}

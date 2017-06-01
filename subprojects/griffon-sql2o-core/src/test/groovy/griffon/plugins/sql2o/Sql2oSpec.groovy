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
package griffon.plugins.sql2o

import griffon.core.GriffonApplication
import griffon.core.RunnableWithArgs
import griffon.core.test.GriffonUnitRule
import griffon.inject.BindTo
import org.junit.Rule
import org.sql2o.Sql2o
import org.sql2o.StatementRunnable
import org.sql2o.StatementRunnableWithResult
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject

@Unroll
class Sql2oSpec extends Specification {
    static {
        System.setProperty('org.slf4j.simpleLogger.defaultLogLevel', 'trace')
    }

    @Rule
    public final GriffonUnitRule griffon = new GriffonUnitRule()

    @Inject
    private Sql2oHandler sql2oHandler

    @Inject
    private GriffonApplication application

    void 'Open and close default sql2o'() {
        given:
        List eventNames = [
            'Sql2oConnectStart', 'DataSourceConnectStart',
            'DataSourceConnectEnd', 'Sql2oConnectEnd',
            'Sql2oDisconnectStart', 'DataSourceDisconnectStart',
            'DataSourceDisconnectEnd', 'Sql2oDisconnectEnd'
        ]
        List events = []
        eventNames.each { name ->
            application.eventRouter.addEventListener(name, { Object... args ->
                events << [name: name, args: args]
            } as RunnableWithArgs)
        }

        when:
        sql2oHandler.withSql2o { String datasourceName, Sql2o sql2o ->
            true
        }
        sql2oHandler.closeSql2o()
        // second call should be a NOOP
        sql2oHandler.closeSql2o()

        then:
        events.size() == 8
        events.name == eventNames
    }

    void 'Connect to default Sql2o'() {
        expect:
        sql2oHandler.withSql2o { String datasourceName, Sql2o sql2o ->
            datasourceName == 'default' && sql2o
        }
    }

    void 'Bootstrap init is called'() {
        given:
        assert !bootstrap.initWitness

        when:
        sql2oHandler.withSql2o { String datasourceName, Sql2o sql2o -> }

        then:
        bootstrap.initWitness
        !bootstrap.destroyWitness
    }

    void 'Bootstrap destroy is called'() {
        given:
        assert !bootstrap.initWitness
        assert !bootstrap.destroyWitness

        when:
        sql2oHandler.withSql2o { String datasourceName, Sql2o sql2o -> }
        sql2oHandler.closeSql2o()

        then:
        bootstrap.initWitness
        bootstrap.destroyWitness
    }

    void 'Can connect to #name Sql2o'() {
        expect:
        sql2oHandler.withSql2o(name) { String datasourceName, Sql2o sql2o ->
            datasourceName == name && sql2o
        }

        where:
        name       | _
        'default'  | _
        'internal' | _
        'people'   | _
    }

    void 'Bogus Sql2o name (#name) results in error'() {
        when:
        sql2oHandler.withSql2o(name) { String datasourceName, Sql2o sql2o ->
            true
        }

        then:
        thrown(IllegalArgumentException)

        where:
        name    | _
        null    | _
        ''      | _
        'bogus' | _
    }

    void 'Execute statements on people table'() {
        when:
        List peopleIn = sql2oHandler.withSql2o('people') { String datasourceName, Sql2o sql2o ->
            String sql = 'INSERT INTO people(id, name, lastname) VALUES (:id, :name, :lastname)'
            [[id: 1, name: 'Danno', lastname: 'Ferrin'],
             [id: 2, name: 'Andres', lastname: 'Almiray'],
             [id: 3, name: 'James', lastname: 'Williams'],
             [id: 4, name: 'Guillaume', lastname: 'Laforge'],
             [id: 5, name: 'Jim', lastname: 'Shingler'],
             [id: 6, name: 'Alexander', lastname: 'Klein'],
             [id: 7, name: 'Rene', lastname: 'Groeschke']].collect([]) { data ->
                Person person = new Person(data)
                sql2o.withConnection({ connection, arg ->
                    connection.createQuery(sql)
                        .addParameter('id', person.id)
                        .addParameter('name', person.name)
                        .addParameter('lastname', person.lastname)
                        .executeUpdate()
                } as StatementRunnable)
                person
            }
        }

        List peopleOut = sql2oHandler.withSql2o('people') { String datasourceName, Sql2o sql2o ->
            sql2o.withConnection({ connection, arg ->
                String sql = 'SELECT id, name, lastname FROM people'
                connection.createQuery(sql).executeAndFetch(Person)
            } as StatementRunnableWithResult)
        }

        then:
        peopleIn == peopleOut
    }

    @BindTo(Sql2oBootstrap)
    private TestSql2oBootstrap bootstrap = new TestSql2oBootstrap()
}

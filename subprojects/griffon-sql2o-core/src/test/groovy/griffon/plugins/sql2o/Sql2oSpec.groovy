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
package griffon.plugins.sql2o

import griffon.annotations.inject.BindTo
import griffon.core.GriffonApplication
import griffon.plugins.datasource.events.DataSourceConnectEndEvent
import griffon.plugins.datasource.events.DataSourceConnectStartEvent
import griffon.plugins.datasource.events.DataSourceDisconnectEndEvent
import griffon.plugins.datasource.events.DataSourceDisconnectStartEvent
import griffon.plugins.sql2o.events.Sql2oConnectEndEvent
import griffon.plugins.sql2o.events.Sql2oConnectStartEvent
import griffon.plugins.sql2o.events.Sql2oDisconnectEndEvent
import griffon.plugins.sql2o.events.Sql2oDisconnectStartEvent
import griffon.test.core.GriffonUnitRule
import org.junit.Rule
import org.sql2o.Sql2o
import org.sql2o.StatementRunnable
import org.sql2o.StatementRunnableWithResult
import spock.lang.Specification
import spock.lang.Unroll

import javax.application.event.EventHandler
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
            'Sql2oConnectStartEvent', 'DataSourceConnectStartEvent',
            'DataSourceConnectEndEvent', 'Sql2oConnectEndEvent',
            'Sql2oDisconnectStartEvent', 'DataSourceDisconnectStartEvent',
            'DataSourceDisconnectEndEvent', 'Sql2oDisconnectEndEvent'
        ]
        TestEventHandler testEventHandler = new TestEventHandler()
        application.eventRouter.subscribe(testEventHandler)

        when:
        sql2oHandler.withSql2o { String datasourceName, Sql2o sql2o ->
            true
        }
        sql2oHandler.closeSql2o()
        // second call should be a NOOP
        sql2oHandler.closeSql2o()

        then:
        testEventHandler.events.size() == 8
        testEventHandler.events == eventNames
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

    private class TestEventHandler {
        List<String> events = []

        @EventHandler
        void handleDataSourceConnectStartEvent(DataSourceConnectStartEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleDataSourceConnectEndEvent(DataSourceConnectEndEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleDataSourceDisconnectStartEvent(DataSourceDisconnectStartEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleDataSourceDisconnectEndEvent(DataSourceDisconnectEndEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleSql2oConnectStartEvent(Sql2oConnectStartEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleSql2oConnectEndEvent(Sql2oConnectEndEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleSql2oDisconnectStartEvent(Sql2oDisconnectStartEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleSql2oDisconnectEndEvent(Sql2oDisconnectEndEvent event) {
            events << event.class.simpleName
        }
    }
}

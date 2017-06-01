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
package org.codehaus.griffon.compile.sql2o.ast.transform;

import griffon.plugins.sql2o.Sql2oHandler;
import griffon.transform.Sql2oAware;
import org.codehaus.griffon.compile.core.AnnotationHandler;
import org.codehaus.griffon.compile.core.AnnotationHandlerFor;
import org.codehaus.griffon.compile.core.ast.transform.AbstractASTTransformation;
import org.codehaus.griffon.compile.sql2o.Sql2oAwareConstants;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static org.codehaus.griffon.compile.core.ast.GriffonASTUtils.injectInterface;

/**
 * Handles generation of code for the {@code @Sql2oAware} annotation.
 *
 * @author Andres Almiray
 */
@AnnotationHandlerFor(Sql2oAware.class)
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class Sql2oAwareASTTransformation extends AbstractASTTransformation implements Sql2oAwareConstants, AnnotationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(Sql2oAwareASTTransformation.class);
    private static final ClassNode SQL2O_HANDLER_CNODE = makeClassSafe(Sql2oHandler.class);
    private static final ClassNode SQL2O_AWARE_CNODE = makeClassSafe(Sql2oAware.class);

    /**
     * Convenience method to see if an annotated node is {@code @Sql2oAware}.
     *
     * @param node the node to check
     * @return true if the node is an event publisher
     */
    public static boolean hasSql2oAwareAnnotation(AnnotatedNode node) {
        for (AnnotationNode annotation : node.getAnnotations()) {
            if (SQL2O_AWARE_CNODE.equals(annotation.getClassNode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the bulk of the processing, mostly delegating to other methods.
     *
     * @param nodes  the ast nodes
     * @param source the source unit for the nodes
     */
    public void visit(ASTNode[] nodes, SourceUnit source) {
        checkNodesForAnnotationAndType(nodes[0], nodes[1]);
        addSql2oHandlerIfNeeded(source, (AnnotationNode) nodes[0], (ClassNode) nodes[1]);
    }

    public static void addSql2oHandlerIfNeeded(SourceUnit source, AnnotationNode annotationNode, ClassNode classNode) {
        if (needsDelegate(classNode, source, METHODS, Sql2oAware.class.getSimpleName(), SQL2O_HANDLER_TYPE)) {
            LOG.debug("Injecting {} into {}", SQL2O_HANDLER_TYPE, classNode.getName());
            apply(classNode);
        }
    }

    /**
     * Adds the necessary field and methods to support sql2o handling.
     *
     * @param declaringClass the class to which we add the support field and methods
     */
    public static void apply(@Nonnull ClassNode declaringClass) {
        injectInterface(declaringClass, SQL2O_HANDLER_CNODE);
        Expression sql2oHandler = injectedField(declaringClass, SQL2O_HANDLER_CNODE, SQL2O_HANDLER_FIELD_NAME);
        addDelegateMethods(declaringClass, SQL2O_HANDLER_CNODE, sql2oHandler);
    }
}
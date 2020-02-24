/*
 * Copyright 2019 - 2020 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.expression.declarative.view;

import com.blazebit.domain.declarative.DeclarativeDomain;
import com.blazebit.domain.declarative.DomainFunctions;
import com.blazebit.domain.runtime.model.DomainModel;
import com.blazebit.domain.runtime.model.DomainType;
import com.blazebit.expression.ExpressionCompiler;
import com.blazebit.expression.ExpressionSerializer;
import com.blazebit.expression.ExpressionServiceFactory;
import com.blazebit.expression.Expressions;
import com.blazebit.expression.Predicate;
import com.blazebit.expression.declarative.persistence.FunctionExpression;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.SubqueryProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

public class EntityViewAttributeUsageTest extends AbstractCoreTest {

    private EntityViewManager evm;
    private DomainType domainType;
    private ExpressionServiceFactory expressionServiceFactory;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[] {
            UserEntity.class
        };
    }

    @Before
    public void init() {
        super.init();
        evm = EntityViews.createDefaultConfiguration()
            .addEntityView(UserView.class)
            .createEntityViewManager(cbf);
        DomainModel domainModel = DeclarativeDomain.getDefaultProvider()
                .createDefaultConfiguration()
                .addDomainType(UserView.class)
                .addDomainFunctions(Functions.class)
                .withService(EntityViewManager.class, evm)
                .createDomainModel();
        domainType = domainModel.getType(UserView.class);
        expressionServiceFactory = Expressions.forModel(domainModel);
    }

    @Test
    public void test() {
        ExpressionCompiler compiler = expressionServiceFactory.createCompiler();
        ExpressionCompiler.Context compilerContext = compiler.createContext(Collections.singletonMap("user", domainType));
        Predicate predicate = compiler.createPredicate("contains(user.sameAgeIds, 1) AND user.oldestNamedAge > 10", compilerContext);
        ExpressionSerializer<WhereBuilder> serializer = expressionServiceFactory.createSerializer(WhereBuilder.class);
        ExpressionSerializer.Context serializerContext = serializer.createContext(Collections.singletonMap("user", "userEntity"));
        CriteriaBuilder<UserEntity> cb = cbf.create(em, UserEntity.class);
        serializer.serializeTo(serializerContext, predicate, cb);
        Assert.assertEquals("SELECT userEntity FROM UserEntity userEntity " +
                                "JOIN UserEntity _expr_correlation_0 ON (_expr_correlation_0.age = userEntity.age) " +
                                "WHERE _expr_correlation_0.id = 1 " +
                                "AND (" +
                                "SELECT subSameNamed.age " +
                                "FROM UserEntity subSameNamed " +
                                "WHERE subSameNamed.name = userEntity.name " +
                                "ORDER BY subSameNamed.age DESC " +
                                "LIMIT 1" +
                                ") > 10", cb.getQueryString());
    }

    @DomainFunctions
    public static interface Functions {
        @FunctionExpression(value = "?1 = ?2", predicate = true)
        boolean contains(Collection<Integer> collection, Integer id);
    }

    @EntityView(UserEntity.class)
    @com.blazebit.domain.declarative.DomainType
    public static interface UserView {
        @IdMapping
        Integer getId();
        String getName();
        long getAge();
        @MappingSubquery(OldestSameNamedAgeSubqueryProvider.class)
        Long getOldestNamedAge();
        @MappingCorrelatedSimple(
            correlated = UserEntity.class,
            correlationBasis = "this",
            correlationExpression = "age = EMBEDDING_VIEW(age)",
            correlationResult = "id"
        )
        Collection<Integer> getSameAgeIds();
    }

    public static class OldestSameNamedAgeSubqueryProvider implements SubqueryProvider {
        @Override
        public <T> T createSubquery(SubqueryInitiator<T> subqueryInitiator) {
            return subqueryInitiator.from(UserEntity.class, "subSameNamed")
                .select("subSameNamed.age")
                .where("subSameNamed.name").eqExpression("EMBEDDING_VIEW(name)")
                .orderByDesc("subSameNamed.age")
                .setMaxResults(1)
                .end();
        }
    }
}

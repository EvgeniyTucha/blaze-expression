/*
 * Copyright 2019 - 2022 Blazebit.
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

package com.blazebit.expression.base;

import com.blazebit.domain.runtime.model.DomainOperator;
import com.blazebit.domain.runtime.model.DomainType;
import com.blazebit.domain.runtime.model.TemporalInterval;
import com.blazebit.expression.ComparisonOperator;
import com.blazebit.expression.DomainModelException;
import com.blazebit.expression.ExpressionInterpreter;
import com.blazebit.expression.spi.ComparisonOperatorInterpreter;
import com.blazebit.expression.spi.DomainOperatorInterpreter;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;

/**
 * @author Yevhen Tucha
 * @since 1.0.0
 */
public class DateOperatorInterpreter
        implements ComparisonOperatorInterpreter, DomainOperatorInterpreter, Serializable {

    public static final DateOperatorInterpreter INSTANCE = new DateOperatorInterpreter();

    private DateOperatorInterpreter() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public Boolean interpret(ExpressionInterpreter.Context context, DomainType leftType, DomainType rightType,
                             Object leftValue, Object rightValue, ComparisonOperator operator) {
        if (leftValue instanceof LocalDate) {
            Comparable<Temporal> l = (Comparable<Temporal>) leftValue;
            Temporal r = null;
            if (rightValue instanceof LocalDate) {
                r = (LocalDate) rightValue;
            } else if (rightValue instanceof Instant) {
                //noinspection rawtypes
                l = (Comparable) ((LocalDate) leftValue).atStartOfDay().toInstant(ZoneOffset.UTC);
                r = (Instant) rightValue;
            }
            if (r != null) {
                switch (operator) {
                    case EQUAL:
                        return l.compareTo(r) == 0;
                    case NOT_EQUAL:
                        return l.compareTo(r) != 0;
                    case GREATER_OR_EQUAL:
                        return l.compareTo(r) > -1;
                    case GREATER:
                        return l.compareTo(r) > 0;
                    case LOWER_OR_EQUAL:
                        return l.compareTo(r) < 1;
                    case LOWER:
                        return l.compareTo(r) < 0;
                    default:
                        break;
                }
            }
        } else {
            throw new DomainModelException("Illegal arguments [" + leftValue + ", " + rightValue + "]!");
        }

        throw new DomainModelException(
                "Can't handle the operator " + operator + " for the arguments [" + leftValue + ", " + rightValue
                        + "]!");
    }

    @Override
    public Object interpret(ExpressionInterpreter.Context context, DomainType targetType, DomainType leftType,
                            DomainType rightType, Object leftValue, Object rightValue, DomainOperator operator) {
        if (leftValue instanceof TemporalInterval && rightValue instanceof TemporalInterval) {
            TemporalInterval interval1 = (TemporalInterval) leftValue;
            TemporalInterval interval2 = (TemporalInterval) rightValue;

            switch (operator) {
                case PLUS:
                    return interval1.add(interval2);
                case MINUS:
                    return interval1.subtract(interval2);
                default:
                    break;
            }
        } else if (leftValue instanceof LocalDate && rightValue instanceof TemporalInterval) {
            LocalDate localDate = (LocalDate) leftValue;
            TemporalInterval interval = (TemporalInterval) rightValue;
            switch (operator) {
                case PLUS:
                    return interval.add(localDate);
                case MINUS:
                    return interval.subtract(localDate);
                default:
                    break;
            }
        } else if (leftValue instanceof TemporalInterval && rightValue instanceof LocalDate) {
            TemporalInterval interval = (TemporalInterval) leftValue;
            LocalDate localDate = (LocalDate) rightValue;

            if (operator == DomainOperator.PLUS) {
                return interval.add(localDate);
            }
        } else {
            throw new DomainModelException("Illegal arguments [" + leftValue + ", " + rightValue + "]!");
        }

        throw new DomainModelException(
                "Can't handle the operator " + operator + " for the arguments [" + leftValue + ", " + rightValue
                        + "]!");
    }

}

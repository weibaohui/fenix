package com.blinkfox.fenix.specification.listener.impl;

import com.blinkfox.fenix.specification.annotation.In;
import com.blinkfox.fenix.specification.listener.AbstractSpecificationHandler;
import com.blinkfox.fenix.specification.predicate.FenixBooleanStaticPredicate;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Predicate.BooleanOperator;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;

/**
 * 构建“范围匹配条件”({@code field IN ('xxx', 'yyy')})场景的 Specification 监听器.
 *
 * @author YangWenpeng on 2019-12-17
 * @author blinkfox on 2020-01-14
 * @since v2.2.0
 */
@Slf4j
public class InSpecificationHandler extends AbstractSpecificationHandler {

    @Override
    protected <Z, X> Predicate buildPredicate(
            CriteriaBuilder criteriaBuilder, From<Z, X> from, String name, Object value, Object annotation) {
        value = value.getClass().isArray() ? Arrays.asList((Object[]) value) : value;
        Path<Object> path = from.get(name);
        CriteriaBuilder.In<Object> in = criteriaBuilder.in(path);

        if (value instanceof Collection) {
            Collection<?> list = (Collection<?>) value;
            if (list.isEmpty()) {
                return new FenixBooleanStaticPredicate(
                        (CriteriaBuilderImpl) criteriaBuilder, true, BooleanOperator.AND);
            } else {
                list.forEach(in::value);
            }
        } else {
            in.value(value);
        }

        return criteriaBuilder.and(
                this.isAllowNull(annotation) ? criteriaBuilder.or(in, criteriaBuilder.isNull(path)) : in);
    }

    private boolean isAllowNull(Object annotation) {
        try {
            return (boolean) this.getAnnotation().getMethod("allowNull").invoke(annotation);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            log.error("【Fenix 错误提示】获取【@In】注解中的中【allowNull】的值失败，将默认返回 false 的值.", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<In> getAnnotation() {
        return In.class;
    }

}

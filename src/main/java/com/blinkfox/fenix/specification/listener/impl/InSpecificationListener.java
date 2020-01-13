package com.blinkfox.fenix.specification.listener.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Predicate.BooleanOperator;

import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.blinkfox.fenix.specification.annotation.In;
import com.blinkfox.fenix.specification.listener.AbstractListener;
import com.blinkfox.fenix.specification.predicate.FenixBooleanStaticPredicate;

/**
 * EquelsSpecificationListener
 * 
 * @description 构造相等条件监听器
 * @author YangWenpeng
 * @date 2019年3月27日 下午4:28:03
 * @version v1.0.0
 */
@Component
public class InSpecificationListener extends AbstractListener {

    Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected <Z, X> Predicate buildPredicate(CriteriaBuilder criteriaBuilder, From<Z, X> from, String name,
        Object value, Object annotation) {
        Path<Object> path = from.get(name);
        boolean allowNull = getAllowNull(annotation);
        CriteriaBuilder.In<Object> in = criteriaBuilder.in(path);
        if (value instanceof Collection) {
            Collection<?> statusList = (Collection<?>)value;
            if (((Collection<?>)value).isEmpty()) {
                return new FenixBooleanStaticPredicate((CriteriaBuilderImpl)criteriaBuilder, false,
                    BooleanOperator.AND);
            } else {
                statusList.stream().forEach(in::value);
            }
        } else {
            in.value(value);
        }
        return criteriaBuilder.and(allowNull ? criteriaBuilder.or(in, criteriaBuilder.isNull(path)) : in);
    }

    private boolean getAllowNull(Object annotation) {
        try {
            return (boolean)getAnnotation().getMethod("allowNull").invoke(annotation);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
            | SecurityException e1) {
            log.error("获取@In中allowNull时失败", e1);
        }
        return false;
    }

    /**
     * @see com.thunisoft.framework.jpaplus.specification.listener.impl.SpecificationListener#getAnnotation()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Class<In> getAnnotation() {
        return In.class;
    }

}
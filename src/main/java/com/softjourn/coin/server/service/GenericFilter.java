package com.softjourn.coin.server.service;

import lombok.Data;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;


@Data
public class GenericFilter<T> implements Specification<T> {

    private List<Condition> conditions;

    private Pageable pageable;

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        Predicate[] predicates = conditions.stream()
                .map(condition -> buildPredicate(root, criteriaBuilder, criteriaQuery, condition))
                .toArray(Predicate[]::new);
        return criteriaBuilder.and(predicates);
    }

    private Predicate buildPredicate(Root<T> root, CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery, Condition condition) {
        switch (condition.comparison) {
            case eq: return buildEqualPredicate(criteriaBuilder, root, condition);
            case gt: return buildGreaterThanPredicate(criteriaBuilder, root, condition);
            case lt: return buildLesThanPredicate(criteriaBuilder, root, condition);
            case in: return buildInPredicate(criteriaBuilder, root, condition);
            default: throw new IllegalArgumentException("Wrong condition " + condition + " specified.");
        }
    }

    private Predicate buildEqualPredicate(CriteriaBuilder criteriaBuilder, Root<T> root, Condition condition){
        return criteriaBuilder.equal(getFieldPath(root, condition), condition.value);
    }

    private Predicate buildGreaterThanPredicate(CriteriaBuilder criteriaBuilder, Root<T> root, Condition condition){
        return criteriaBuilder.greaterThanOrEqualTo(getFieldPath(root, condition), (Comparable)condition.value);
    }

    private Predicate buildLesThanPredicate(CriteriaBuilder criteriaBuilder, Root<T> root, Condition condition){
        return criteriaBuilder.lessThanOrEqualTo(getFieldPath(root, condition), (Comparable)condition.value);
    }

    private Predicate buildInPredicate(CriteriaBuilder criteriaBuilder, Root<T> root, Condition condition) {
        if (condition.value instanceof Collection) {
            if(((Collection) condition.value).isEmpty()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            return getFieldPath(root, condition).in((Collection<?>) condition.value);
        } else throw new IllegalArgumentException("Method buildInPredicate can be applied only for collections");
    }

    private Path getFieldPath(Root<T> root, Condition condition) {
        if (isCompositeField(condition.field)) {
            return getPathByCompositeField(root, condition);
        } else if (condition.value instanceof Collection){
            return getPathBySimpleFieldOrEntityFieldIdForInCause(root, condition);
        } else {
            return getPathBySimpleFieldOrEntityFieldId(root, condition);
        }
    }

    private Path getPathBySimpleFieldOrEntityFieldIdForInCause(Root<T> root, Condition condition) {
        if (condition.value instanceof  Collection && !((Collection)condition.value).isEmpty()) {
            Object value = ((Collection)condition.value).stream().findFirst().get();
            return getPathBySimpleFieldOrEntityFieldId(root, condition.field, value);
        } else {
            throw new IllegalArgumentException("Condition value should be not empty collection.");
        }
    }

    private Path getPathBySimpleFieldOrEntityFieldId(Root<T> root, Condition condition) {
        return getPathBySimpleFieldOrEntityFieldId(root, condition.field, condition.value);
    }

    private Path getPathByCompositeField(Root<T> root, Condition condition) {
        String[] fieldsPath = fieldsPath(condition.field);
        Join path = root.join(fieldsPath[0], JoinType.INNER);
        for (int i = 1; i < fieldsPath.length - 1; i++) {
            String field = fieldsPath[i];
            path = path.join(field, JoinType.INNER);
        }
        return path.get(fieldsPath[fieldsPath.length - 1]);
    }

    private Path getPathBySimpleFieldOrEntityFieldId(Root<T> root, String fieldName, Object value) {
        Class fieldType = root.getModel().getAttribute(fieldName).getJavaType();
        if (fieldType.isAnnotationPresent(Entity.class)){
            Class fieldIdType = getIdFieldType(fieldType);
            if (fieldIdType.isInstance(value)) {
                return root.join(fieldName, JoinType.INNER).get(getIdFieldName(fieldType));
            } else {
                throw new IllegalArgumentException("Can't create criteria based on field " + fieldName + " with value " + value + ".");
            }
        } else {
            return root.get(fieldName);
        }
    }

    private String getIdFieldName(Class entityClass) {
        return getIdFieldProperty(entityClass, Field::getName);

    }

    private Class getIdFieldType(Class entityClass) {
        return getIdFieldProperty(entityClass, Field::getType);
    }

    private <P> P getIdFieldProperty(Class entityClass, Function<Field, P> propertyMapper) {
        return Stream.of(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(propertyMapper)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Can't get ID field of entity " + entityClass));
    }

    private boolean isCompositeField(String fielsName) {
        return fielsName.contains(".");
    }

    private String[] fieldsPath(String compositeField) {
        return compositeField.split("\\.");
    }

    public enum Comparison {
        eq, in, gt, lt
    }

    @Data
    public static class Condition {
        private String field;

        private Object value;

        private Comparison comparison;
    }
}

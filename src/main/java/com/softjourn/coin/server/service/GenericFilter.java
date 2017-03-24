package com.softjourn.coin.server.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.softjourn.coin.server.util.SortJsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ClassUtils;

import javax.persistence.Entity;
import javax.persistence.criteria.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

import static com.softjourn.coin.server.util.ReflectionUtil.getIdFieldName;
import static com.softjourn.coin.server.util.ReflectionUtil.getIdFieldType;


@Data
@NoArgsConstructor
public class GenericFilter<T> implements Specification<T> {


    private static final Map<Class, Class> WRAPPERS = Collections.unmodifiableMap(new HashMap<Class, Class>(){{
        put(byte.class, Byte.class);
        put(char.class, Character.class);
        put(short.class, Short.class);
        put(int.class, Integer.class);
        put(long.class, Long.class);
        put(boolean.class, Boolean.class);
    }});

    private List<Condition> conditions = new ArrayList<>();

    private PageRequestImpl pageable;

    @JsonIgnore
    private Pageable innerPageable;

    @JsonIgnore
    private BoolOperation operation = BoolOperation.AND;

    public GenericFilter(List<Condition> conditions, PageRequestImpl pageable) {
        this.conditions = conditions;
        this.pageable = pageable;
        this.innerPageable = pageable.toPageable();
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        Predicate[] predicates = conditions.stream()
                .map(condition -> buildPredicate(root, criteriaBuilder, criteriaQuery, condition))
                .toArray(Predicate[]::new);
        return operation == BoolOperation.AND ? criteriaBuilder.and(predicates) : criteriaBuilder.or(predicates);
    }

    public static <T> GenericFilter<T> and(Condition... condition) {
        return and(null, condition);
    }

    public static <T> GenericFilter<T> and(PageRequest pageable, Condition... condition) {
        GenericFilter<T> filter = new GenericFilter<>();
        filter.innerPageable = pageable;
        filter.conditions = Arrays.asList(condition);
        return filter;
    }

    public static <T> GenericFilter<T> or(Condition... condition) {
        return or(null, condition);
    }

    public static <T> GenericFilter<T> or(PageRequest pageable, Condition... condition) {
        GenericFilter<T> filter = new GenericFilter<>();
        filter.innerPageable = pageable;
        filter.conditions = Arrays.asList(condition);
        filter.operation = BoolOperation.OR;
        return filter;
    }

    private Predicate buildPredicate(Root<T> root, CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery, Condition condition) {
        switch (condition.comparison) {
            case eq:
                return buildEqualPredicate(criteriaBuilder, root, condition);
            case gt:
                return buildGreaterThanPredicate(criteriaBuilder, root, condition);
            case lt:
                return buildLesThanPredicate(criteriaBuilder, root, condition);
            case in:
                return buildInPredicate(criteriaBuilder, root, condition);
            default:
                throw new IllegalArgumentException("Wrong condition " + condition + " specified.");
        }
    }

    private Predicate buildEqualPredicate(CriteriaBuilder criteriaBuilder, Root<T> root, Condition condition) {
        Path fieldPath = getFieldPath(root, condition);
        if (condition.value == null) {
            return criteriaBuilder.isNull(fieldPath);
        } else {
            Object value = getCastedValue(fieldPath, condition.value);
            return criteriaBuilder.equal(fieldPath, value);
        }
    }

    @SuppressWarnings("unchecked")
    private Predicate buildGreaterThanPredicate(CriteriaBuilder criteriaBuilder, Root<T> root, Condition condition) {
        Path fieldPath = getFieldPath(root, condition);
        Object value = getCastedValue(fieldPath, condition.value);
        return criteriaBuilder.greaterThanOrEqualTo(fieldPath, (Comparable) value);
    }

    @SuppressWarnings("unchecked")
    private Predicate buildLesThanPredicate(CriteriaBuilder criteriaBuilder, Root<T> root, Condition condition) {
        Path fieldPath = getFieldPath(root, condition);
        Object value = getCastedValue(fieldPath, condition.value);
        return criteriaBuilder.lessThanOrEqualTo(fieldPath, (Comparable) value);
    }

    private Object getCastedValue(Path fieldPath, Object value) {
        Class fieldClass = fieldPath.getJavaType();
        return tryToCastValue(fieldClass, value);
    }

    @SuppressWarnings("unchecked")
    private Predicate buildInPredicate(CriteriaBuilder criteriaBuilder, Root<T> root, Condition condition) {
        if (condition.value instanceof Collection) {
            if (((Collection) condition.value).isEmpty()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
            return getFieldPath(root, condition).in((Collection<?>) condition.value);
        } else throw new IllegalArgumentException("Method buildInPredicate can be applied only for collections");
    }

    private Object tryToCastValue(Class valueClass, Object value) {
        if (valueClass.isInstance(value) || isWrapperFor(valueClass, value)) {
            return value;
        } else if (hasAppropriateConstructor(valueClass, value)) {
            return getInstanceByConstructor(valueClass, value);
        } else if (hasValueOfFactoryMethod(valueClass, value)) {
            return getInstanceByValueOf(valueClass, value);
        } else if (value instanceof String && hasParseFactoryMethod(valueClass)) {
            return getInstanceByParse(valueClass, value);
        }
        throw new IllegalArgumentException("Can't create value of class " + valueClass.getName() + " from value " + value.toString());
    }

    private boolean isWrapperFor(Class valueClass, Object value) {
        return valueClass.isPrimitive() && getWrapperClass(valueClass).isInstance(value);
    }

    private Class getWrapperClass(Class valueClass) {
        return WRAPPERS.get(valueClass);
    }

    @SuppressWarnings("unchecked")
    private Object getInstanceByConstructor(Class valueClass, Object value) {
        return Stream.of(valueClass.getConstructors())
                .filter(constructor -> constructor.getParameterCount() == 1)
                .filter(constructor -> ClassUtils.isAssignableValue(constructor.getParameterTypes()[0], value))
                .findAny()
                .map(constructor -> getInstanceByConstructor(value, constructor))
                .orElseThrow(() -> new IllegalArgumentException("Can't create value of class " + valueClass.getName() + " from value " + value));
    }

    private Object getInstanceByConstructor(Object value, Constructor constructor) {
        try {
            return constructor.newInstance(value);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Can't create value of class " + constructor.getDeclaringClass().getName() + " from value " + value);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean hasAppropriateConstructor(Class valueClass, Object value) {
        return Stream.of(valueClass.getConstructors())
                .filter(constructor -> constructor.getParameterCount() == 1)
                .anyMatch(constructor -> ClassUtils.isAssignableValue(constructor.getParameterTypes()[0], value));
    }

    private Object getInstanceByValueOf(Class valueClass, Object value) {
        return getInstanceByFactoryMethod(valueClass, value, "valueOf");
    }

    private Object getInstanceByParse(Class valueClass, Object value) {
        return getInstanceByFactoryMethod(valueClass, value, "parse");
    }

    @SuppressWarnings("unchecked")
    private Object getInstanceByFactoryMethod(Class valueClass, Object value, String methodName) {
            return Stream.of(valueClass.getMethods())
                    .filter(method -> method.getName().equals(methodName))
                    .map(method -> invokeFactoryMethod(method, value))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Class " + valueClass + " doesn't contain method " + methodName));
    }

    private Object invokeFactoryMethod(Method method, Object value) {
        try {
            return method.invoke(null, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Can't create value of class " + method.getDeclaringClass().getName() + " from value " + value);
        }
    }

    private boolean hasValueOfFactoryMethod(Class valueClass, Object value) {
        return hasFactoryMethod(valueClass, "valueOf", value.getClass());
    }

    private boolean hasParseFactoryMethod(Class valueClass) {
        return hasFactoryMethod(valueClass, "parse", String.class);
    }

    private boolean hasFactoryMethod(Class clazz, String methodName, Class argumentClass) {
        return Stream.of(clazz.getMethods())
                .filter(method -> method.getName().equals(methodName))
                .filter(method -> method.getParameterCount() == 1)
                .filter(this::isStatic)
                .anyMatch(method -> method.getParameterTypes()[0].isAssignableFrom(argumentClass));
    }

    private boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    private Path getFieldPath(Root<T> root, Condition condition) {
        if (isCompositeField(condition.getField())) {
            return getPathByCompositeField(root, condition.getField());
        } else if (condition.value instanceof Collection) {
            return getPathBySimpleFieldOrEntityFieldIdForInCause(root, condition);
        } else {
            return getPathBySimpleFieldOrEntityFieldId(root, condition);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private Path getPathBySimpleFieldOrEntityFieldIdForInCause(Root<T> root, Condition condition) {
        if (condition.value instanceof Collection && !((Collection) condition.value).isEmpty()) {
            Object value = ((Collection) condition.value).stream().findFirst().get();
            return getPathBySimpleFieldOrEntityFieldId(root, condition.field, value);
        } else {
            throw new IllegalArgumentException("Condition value should be not empty collection.");
        }
    }

    private Path getPathBySimpleFieldOrEntityFieldId(Root<T> root, Condition condition) {
        return getPathBySimpleFieldOrEntityFieldId(root, condition.field, condition.value);
    }

    private Path getPathByCompositeField(Root<T> root, String fieldName) {
        String[] fieldsPath = fieldsPath(fieldName);
        if (fieldsPath.length == 1) return root.get(fieldName);
        Join path = root.join(fieldsPath[0], JoinType.INNER);
        for (int i = 1; i < fieldsPath.length - 1; i++) {
            String field = fieldsPath[i];
            path = path.join(field, JoinType.LEFT);
        }
        return path.get(fieldsPath[fieldsPath.length - 1]);
    }

    private Path getPathBySimpleFieldOrEntityFieldId(Root<T> root, String fieldName, Object value) {
        Class fieldType = root.getModel().getAttribute(fieldName).getJavaType();
        if (fieldType.isAnnotationPresent(Entity.class)) {
            Class fieldIdType = getIdFieldType(fieldType);
            if (fieldIdType.isInstance(value) || value == null) {
                return root.join(fieldName, JoinType.LEFT).get(getIdFieldName(fieldType));
            } else {
                throw new IllegalArgumentException("Can't create criteria based on field " + fieldName + " with value " + value + ".");
            }
        } else {
            return root.get(fieldName);
        }
    }

    private boolean isCompositeField(String fielsName) {
        return fielsName.contains(".");
    }

    private String[] fieldsPath(String compositeField) {
        return compositeField.split("\\.");
    }

    public enum BoolOperation {
        OR, AND
    }

    public enum Comparison {
        eq, in, gt, lt
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Condition {
        private String field;

        private Object value;

        private Comparison comparison;

        public static Condition eq(String field, Object value) {
            return new Condition(field, value, Comparison.eq);
        }

        public static Condition in(String field, Object value) {
            return new Condition(field, value, Comparison.in);
        }

        public static Condition gt(String field, Object value) {
            return new Condition(field, value, Comparison.gt);
        }

        public static Condition lt(String field, Object value) {
            return new Condition(field, value, Comparison.lt);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PageRequestImpl {
        private int size;
        private int page;
        @JsonDeserialize(using = SortJsonDeserializer.class)
        private Sort sort;

        public Pageable toPageable() {
            if (sort == null) {
                return new PageRequest(page, size);
            } else {
                return new PageRequest(page, size, sort);
            }
        }
    }
}

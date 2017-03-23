package com.softjourn.coin.server.util;

import javax.persistence.Id;
import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.stream.Stream;

public class ReflectionUtil {

    public static String getIdFieldName(Class entityClass) {
        return getIdFieldProperty(entityClass, Field::getName);

    }

    public static Class getIdFieldType(Class entityClass) {
        return getIdFieldProperty(entityClass, Field::getType);
    }

    public static  <P> P getIdFieldProperty(Class entityClass, Function<Field, P> propertyMapper) {
        return Stream.of(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(propertyMapper)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Can't get ID field of entity " + entityClass));
    }


}

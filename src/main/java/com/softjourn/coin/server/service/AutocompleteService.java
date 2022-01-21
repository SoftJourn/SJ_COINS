package com.softjourn.coin.server.service;

import static com.softjourn.common.utils.ReflectionUtil.getIdFieldName;
import static com.softjourn.common.utils.ReflectionUtil.getIdFieldType;

import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import org.springframework.beans.BeanUtils;

public class AutocompleteService<T> {

  private final EntityManager entityManager;
  private final CriteriaBuilder criteriaBuilder;
  private final Root<T> root;

  public AutocompleteService(Class<T> entityClass, EntityManager entityManager) {
    this.entityManager = entityManager;

    criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<T> query = criteriaBuilder.createQuery(entityClass);
    root = query.from(entityClass);
  }

  @SuppressWarnings("unchecked")
  public List getAutocomplete(String stringPath) {
    Path path = getPathByCompositeField(root, stringPath);
    Class pathClass = path.getJavaType();
    CriteriaQuery rootQuery = criteriaBuilder.createQuery(pathClass);
    Root<T> rot = rootQuery.from(root.getModel());
    CriteriaQuery query = rootQuery
        .select(getPathByCompositeField(rot, stringPath).alias("result")).distinct(true);
    TypedQuery requestQuery = entityManager.createQuery(query);
    return requestQuery.getResultList();
  }

  public <C> Map<String, Object> getAllPaths(Class<? extends C> rootClass) {
    if (BeanUtils.isSimpleProperty(rootClass) || Temporal.class.isAssignableFrom(rootClass)) {
      throw new IllegalArgumentException("Class " + rootClass + " is not entity class.");
    }
    return getAllPaths(rootClass, "");
  }

  private Map<String, Object> getAllPaths(Class clazz, String fieldName) {
    Map<String, Object> result = new TreeMap<>();
    Stream.concat(Stream.of(clazz.getDeclaredFields()), Stream.of(clazz.getFields()))
        .distinct()
        .filter(innerField -> !innerField.isAnnotationPresent(FilterIgnore.class))
        .forEach(innerField ->
            result.put(innerField.getName(), getFieldDescription(innerField.getType())));
    return result;
  }

  private Object getFieldDescription(Class clazz) {
    if (BeanUtils.isSimpleProperty(clazz) || Temporal.class.isAssignableFrom(clazz)) {
      return getTypeString(clazz);
    } else {
      return getAllPaths(clazz);
    }
  }

  private String getTypeString(Class<?> type) {
    if (Temporal.class.isAssignableFrom(type)) {
      return "date";
    } else if (isNumber(type)) {
      return "number";
    } else if (canBeString(type)) {
      return "text";
    } else if (boolean.class.equals(type)) {
      return "bool";
    } else {
      return getTypeString(getIdFieldType(type));
    }
  }

  private String merge(String prefix, String string) {
    return prefix + (prefix.isEmpty() ? "" : ".") + string;
  }

  private boolean canBeString(Class<?> type) {
    return CharSequence.class.isAssignableFrom(type) || type.isEnum();
  }

  private boolean isNumber(Class<?> type) {
    return Number.class.isAssignableFrom(type) || (type.isPrimitive() && !boolean.class.equals(type));
  }

  private Path getPathByCompositeField(Root root, String fieldName) {
    String[] fieldsPath = fieldName.split("\\.");
    if (fieldsPath.length == 1) return getSimpleTypePath(root, fieldName);
    Join path = root.join(fieldsPath[0], JoinType.INNER);
    for (int i = 1; i < fieldsPath.length - 1; i++) {
      String field = fieldsPath[i];
      path = path.join(field, JoinType.LEFT);
    }
    return path.get(fieldsPath[fieldsPath.length - 1]);
  }

  private Path getSimpleTypePath(Root path, String fieldName) {
    Class javaType = getFieldClass(path, fieldName);
    if (BeanUtils.isSimpleProperty(javaType)) {
      return path.get(fieldName);
    } else {
      return path.join(fieldName).get(getIdFieldName(javaType));
    }
  }

  private Class getFieldClass(Path path, String fieldName) {
    Class clazz = path.getJavaType();
    try {
      return clazz.getDeclaredField(fieldName).getType();
    } catch (NoSuchFieldException e) {
      throw new IllegalArgumentException(
          "Class " + clazz + " have not field with name " + fieldName);
    }
  }
}

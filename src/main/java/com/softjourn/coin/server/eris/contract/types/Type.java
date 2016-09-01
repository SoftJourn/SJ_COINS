package com.softjourn.coin.server.eris.contract.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonDeserialize(using = TypeDeserializer.class)
public abstract class Type<T> {

    /**
     * Format given value in accordance with this type
     * to be passed to ERIS
     * @param value value to be formatted
     * @return formatted value
     */
    public abstract String formatInput(T value);

    /**
     * Format given value in accordance with this type
     * that is passed from ERIS
     * @param value value to be formatted
     * @return formatted value
     */
    public abstract T formatOutput(String value);

    /**
     * Check if value can be instance of this type
     * @param value value to be checked
     * @return true if this value can be represented by this type
     */
    public abstract boolean canRepresent(Object value);

    /**
     * Returns name of type with length (i.e. uint256, bytes32)
     *
     * @return full name of this type
     */
    public abstract String toString();

    public abstract Class<T> valueClass();

    @SuppressWarnings("unchecked")
    static Type suggestType(String typeName) {
        for (Class clazz : allDefinedTypes()) {
            if( Type.class.isAssignableFrom(clazz) && checkType(typeName, clazz)) try {
                int length = getLength(typeName);
                if (length > 0) return (Type) clazz.getConstructor(int.class).newInstance(length);
                else return (Type) clazz.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Class " + clazz.getName() + " don't have required constructor.");
            }
        }

        throw new RuntimeException("Type for " + typeName + " is not registered yet.");
    }

    private static int getLength(String typeName) {
        Pattern pattern = Pattern.compile("[0-9]+$");
        Matcher matcher = pattern.matcher(typeName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return 0;
    }

    private static boolean checkType(String typeName, Class<? extends Type> clazz) {
        try {
            return  (Boolean) clazz.getMethod("isType", String.class).invoke(null, typeName);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Class " + clazz.getName() + "is not type or not provide isType() method or this method is not static.");
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Type>[] allDefinedTypes() {
        return new Class[]{
                Uint.class,
                Address.class
        };
    }


}

package com.softjourn.coin.server.aop.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation mark methods for which {@see com.softjourn.coin.server.entity.Transaction} should be saved.
 *
 * For proper working this method have to declare {@see com.softjourn.coin.server.entity.Transaction} as return type
 * but can return null such as return value will be overridden in aspect advice
 *
 * Method arguments should be named as:
 *      - "accountName" - for donor account;
 *      - "destinationName" - for acceptor account;
 *      - "amount" - for coins amount
 *      - "comment" - for additional information about transaction
 *
 * if there no such argument or it's name not mach specified above null will be set to appropriate transaction fields
 */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface SaveTransaction {

    String accountName() default "";

    String destinationName() default "";

    int amount() default 0;

    String comment()default "";

}

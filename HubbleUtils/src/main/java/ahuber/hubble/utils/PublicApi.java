package ahuber.hubble.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;

/**
 * Used to indicate that a method is a public API
 */
@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE}) // Targets are same as @SuppressWarnings
@Retention(RetentionPolicy.SOURCE) // Retention is same as @SuppressWarnings
public @interface PublicApi {
}

package ahuber.hubble.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;

/**
 * Used to document the reason(s) why the {@link SuppressWarnings} annotation was used.
 */
@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE}) // Targets are same as @SuppressWarnings
@Retention(RetentionPolicy.SOURCE) // Retention is same as @SuppressWarnings
public @interface WarningSuppressionReason {

    /**
     * A string containing the reason why the {@link SuppressWarnings} annotation was used.
     * @return The reason why the {@link SuppressWarnings} annotation was used.
     */
    String value();
}

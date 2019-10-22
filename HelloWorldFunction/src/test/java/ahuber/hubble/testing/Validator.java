package ahuber.hubble.testing;

/**
 * An interface defining whether a value is valid.
 * @param <T> The type of value to check.
 */
public interface Validator<T> {
    /**
     * Determines whether the provided object is valid or not.
     * @param obj The object to check.
     * @return true if the object is valid.
     */
    boolean isValid(T obj);
}

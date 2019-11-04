package ahuber.hubble.adt;

import java.util.Collection;

/**
 * Defines a method that is invoked upon the size of a collection changing.
 * @param <C> The collection type.
 */
public interface SizeObserver<C> {

    /**
     * The method that is invoked whenever the size of the collection changes.
     * @param collection The collection that had its size change.
     */
    void sizeChanged(C collection);
}

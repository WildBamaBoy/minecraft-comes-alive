package mca.util.compat;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface OptionalCompat {

    /**
     * @since 1.16
     */
    static <T> Optional<T> or(Optional<T> optional, Supplier<Optional<T>> otherwise) {
        return optional.isPresent() ? optional : otherwise.get();
    }

    /**
     * @since 1.16
     */
    static <T> void ifPresentOrElse(Optional<T> optional, Consumer<T> present, Runnable otherwise) {
        if (optional.isPresent()) {
            present.accept(optional.get());
        } else {
            otherwise.run();
        }
    }
}

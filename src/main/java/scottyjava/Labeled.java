package scottyjava;

import java.util.Optional;

public abstract class Labeled<T> {
    public Optional<T> label;

    public boolean hasLabel(T l) {
        return label.isPresent() && label.get() == l;
    }
}

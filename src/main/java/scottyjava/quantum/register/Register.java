package scottyjava.quantum.register;

import java.util.List;

// TODO: add [T <: Labeled[String]]
public abstract class Register<T> {
    public List<T> values;

    public int size() {
        return values.size();
    }

    // TODO: add areLabelsUnique
}

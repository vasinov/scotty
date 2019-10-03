package scottyjava.quantum;

import scottyjava.quantum.register.QubitRegister;
import scottyjava.quantum.state.Superposition;

public abstract class QuantumContext {
    public abstract Superposition tensorProduct(QubitRegister register, Superposition sp1, Superposition sp2);

    public abstract float[][] densityMatrix(float[] vector);

//    public abstract boolean isUnitary(TargetGate gate);
//
//    public abstract List<StateData> probabilities(Superposition sp);
//
//    public abstract void applyGate(float[] state, Gate gate);
//
//    public abstract State run(Circuit circuit);
//
//    public abstract Collapsed measure(QubitRegister register, float[] state);
//
//    public Collapsed runAndMeasure(Circuit circuit) {
//        var result = run(circuit);
//
//        if (result instanceof Superposition) {
//            return measure(circuit.register, (Superposition)result.state);
//        } else {
//            return (Collapsed)result;
//        }
//    }
//
//    public abstract ExperimentResult runExperiment(Circuit circuit, int trialsCount);
}

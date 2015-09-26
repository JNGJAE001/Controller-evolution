package za.redbridge.controller.NEATM;

import org.encog.engine.network.activation.ActivationFunction;

/**
 * Created by jamie on 2014/10/08.
 */
public class ActivationSteepenedShiftedSigmoid implements ActivationFunction {

    private static final long serialVersionUID = -7731056394594891357L;

    private static final double SLOPE = 4.924273;
    private static final double SHIFT = 2.4621365;

    /**
     * Construct a steepened and right-shifted sigmoid activation function.
     */
    public ActivationSteepenedShiftedSigmoid() {
    }

    @Override
    public void activationFunction(double[] x, int start, int size) {
        for (int i = start, n = start + size; i < n; i++) {
            x[i] = 1.0 / (1.0 + Math.exp(-SLOPE * (x[i] - SHIFT))); // From NEAT source
        }
    }

    @Override
    public double derivativeFunction(double b, double a) {
        double s = Math.exp(-(SLOPE * (a - SHIFT)));
        double numerator = SLOPE * s;
        double denominator = Math.pow(s + 1, 2);
        return numerator / denominator;
    }

    @Override
    public boolean hasDerivative() {
        return true;
    }

    @Override
    public double[] getParams() {
        return new double[0];
    }

    @Override
    public void setParam(int index, double value) {
        // NO-OP
    }

    @Override
    public String[] getParamNames() {
        return new String[0];
    }

    @Override
    public ActivationFunction clone() {
        return new ActivationSteepenedShiftedSigmoid();
    }

    @Override
    public String getFactoryCode() {
        return null;
    }
}

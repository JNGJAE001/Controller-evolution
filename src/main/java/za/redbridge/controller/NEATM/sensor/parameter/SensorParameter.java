package za.redbridge.controller.NEATM.sensor.parameter;

import java.io.Serializable;
import java.util.Random;

import za.redbridge.controller.NEATM.sensor.parameter.spec.Range;

/**
 * Created by jamie on 2014/11/26.
 */
public class SensorParameter implements Cloneable, Serializable {

    private static final long serialVersionUID = 7096040734234576161L;

    private final SensorParameterSpec spec;

    private float value;

    public SensorParameter(SensorParameterSpec spec) {
        this(spec, 0);
    }

    public SensorParameter(SensorParameterSpec spec, float value) {
        this.spec = spec;
        setValue(value);
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = spec.applyLimit(this.value, value);
    }

    public SensorParameterSpec getSpec() {
        return spec;
    }

    /**
     * Get the value of this parameter as a value between 0.0 and 1.0.
     * @return
     */
    public float getNormalizedValue() {
        final Range range = spec.getRange();
        return (value - range.min) / (range.max - range.min);
    }

    /**
     * Utility method to set the parameter value to a new random value within the valid range.
     * @param random
     */
    public void randomize(Random random) {
        value = spec.getRange().randomValueWithinRange(random);
    }

    @Override
    public SensorParameter clone() {
        return new SensorParameter(spec, value);
    }
}

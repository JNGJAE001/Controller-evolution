package za.redbridge.controller.NEATM.sensor.parameter;

import java.io.Serializable;
import java.util.Random;

import za.redbridge.controller.NEATM.sensor.parameter.spec.Initializer;
import za.redbridge.controller.NEATM.sensor.parameter.spec.Limiter;
import za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType;
import za.redbridge.controller.NEATM.sensor.parameter.spec.Range;

/**
 * Describes the valid range of a configuration parameter for a sensor.
 *
 * Created by jamie on 2014/11/26.
 */
public class SensorParameterSpec implements Serializable {

    private static final long serialVersionUID = 1700183124160179196L;

    private final ParameterType parameterType;
    private final Initializer initializer;
    private final Range range;
    private final Limiter limiter;

    private SensorParameterSpec(ParameterType parameterType, Initializer initializer, Range range, Limiter limiter) {
        this.parameterType = parameterType;
        this.initializer = initializer;
        this.range = range;
        this.limiter = limiter;
    }

    float applyLimit(float oldValue, float newValue) {
        float limitedValue = newValue;
        if (!range.isValueWithinRange(newValue)) {
            limitedValue = limiter.limitValue(oldValue, newValue, range);
            if (!range.isValueWithinRange(limitedValue)) {
                throw new RuntimeException("Limiter failed to limit parameter value, unlimited: " + newValue + ", limited: " + limitedValue);
            }
        }

        return limitedValue;
    }

    ParameterType getParameterType() {
        return parameterType;
    }

    public Initializer getInitializer() {
        return initializer;
    }

    public Range getRange() {
        return range;
    }

    public Limiter getLimiter() {
        return limiter;
    }

    public static class BearingSpec extends SensorParameterSpec {
        public BearingSpec(Initializer initializer, Range range, Limiter limiter) {
            super(ParameterType.BEARING, initializer, range, limiter);
        }
    }

    public static class OrientationSpec extends SensorParameterSpec {
        public OrientationSpec(Initializer initializer, Range range, Limiter limiter) {
            super(ParameterType.ORIENTATION, initializer, range, limiter);
        }
    }

    public static class RangeSpec extends SensorParameterSpec {
        public RangeSpec(Initializer initializer, Range range, Limiter limiter) {
            super(ParameterType.RANGE, initializer, range, limiter);
        }
    }

    public static class FieldOfViewSpec extends SensorParameterSpec {
        public FieldOfViewSpec(Initializer initializer, Range range, Limiter limiter) {
            super(ParameterType.FIELD_OF_VIEW, initializer, range, limiter);
        }
    }

    SensorParameter createParameter(Random random, SensorParameter existingParameter) {
        if (initializer.shouldCopyExistingValue() && existingParameter != null) {
            return new SensorParameter(this, existingParameter.getValue());
        }

        final float value = applyLimit(0, initializer.initializeValue(range, random));
        return new SensorParameter(this, value);
    }

}

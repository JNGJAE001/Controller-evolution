package za.redbridge.controller.NEATM.sensor.parameter.spec;

import org.jbox2d.common.MathUtils;

/**
 * Created by jamie on 2014/11/28.
 */
public final class Ranges {

    private Ranges() {
    }

    public static Range plusMinusPi() {
        return new Range(-MathUtils.PI, MathUtils.PI, true, true);
    }

    public static Range plusMinusHalfPi() {
        return new Range(-MathUtils.HALF_PI, MathUtils.HALF_PI, true, true);
    }

    public static Range zeroToPi() {
        return new Range(0, MathUtils.PI, false, true);
    }

    public static Range zeroTo2Pi() {
        return new Range(0, 2*MathUtils.PI, false, true);
    }

    public static Range positiveNumbers() {
        return new Range(0, Float.MAX_VALUE);
    }

    public static Range positiveNumbersLessThan(float value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Value must be greater than 0");
        }

        return new Range(0, value);
    }

    public static Range range(float min, float max) {
        if (min > max) {
            throw new IllegalArgumentException("Min must be less than or equal to max");
        }

        return new Range(min, max);
    }

    public static Range exclusiveRange(float min, float max) {
        if (min > max) {
            throw new IllegalArgumentException("Min must be less than or equal to max");
        }

        return new Range(min, max, false, false);
    }
}

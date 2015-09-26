package za.redbridge.controller.NEATM.sensor.parameter.spec;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by jamie on 2014/11/28.
 */
public final class Range implements Serializable {

    private static final long serialVersionUID = -1401371455734818086L;

    public final float min;
    public final float max;

    public final boolean inclusiveMin;
    public final boolean inclusiveMax;

    public Range(float min, float max, boolean inclusiveMin, boolean inclusiveMax) {
        if (min >= max) {
            throw new IllegalArgumentException("Min must be less than max");
        }

        this.min = min;
        this.max = max;
        this.inclusiveMin = inclusiveMin;
        this.inclusiveMax = inclusiveMax;
    }

    public Range(float min, float max) {
        this(min, max, true, false);
    }

    public boolean isValueWithinRange(float value) {
        if (inclusiveMin) {
            if (value < min) {
                return false;
            }
        } else {
            if (value <= min) {
                return false;
            }
        }

        if (inclusiveMax) {
            if (value > max) {
                return false;
            }
        } else {
            if (value >= max) {
                return false;
            }
        }

        return true;
    }

    public float randomValueWithinRange(Random random) {
        float range = max - min;
        float offset = min;
        float value;
        if (inclusiveMin) {
            if (inclusiveMax) {
                // [min, max]: extend range
                range = Math.nextAfter(range, Double.POSITIVE_INFINITY);

                value = random.nextFloat() * range + offset;
                if (value > max) {
                    value = max;
                }
            } else {
                // [min, max): the norm
                value = random.nextFloat() * range + offset;
            }
        } else {
            if (inclusiveMax) {
                // (min, max]: shift random value right
                value = random.nextFloat() * range + offset;
                value = Math.nextAfter(value, Double.POSITIVE_INFINITY);
                if (value > max) {
                    value = max;
                }
            } else {
                // (min, max): shrink range and shift random value right
                range = Math.nextAfter(range, Double.NEGATIVE_INFINITY);

                value = random.nextFloat() * range + offset;
                value = Math.nextAfter(value, Double.POSITIVE_INFINITY);
                if (value >= max) {
                    value = Math.nextAfter(max, Double.NEGATIVE_INFINITY);
                }
            }
        }
        return value;
    }

}

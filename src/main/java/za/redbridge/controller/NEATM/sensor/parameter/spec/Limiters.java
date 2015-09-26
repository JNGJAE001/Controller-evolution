package za.redbridge.controller.NEATM.sensor.parameter.spec;

/**
 * Created by jamie on 2014/11/28.
 */
public final class Limiters {

    private Limiters() {
    }

    public static Limiter wrap() {
        return new Limiter() {
            @Override
            public float limitValue(float oldValue, float newValue, Range range) {
                final float diff = range.max - range.min;
                newValue %= diff;
                if (range.inclusiveMin && newValue < range.min
                        || !range.inclusiveMin && newValue <= range.min) {
                    newValue += diff;
                } else if (range.inclusiveMax && newValue > range.max
                        || !range.inclusiveMax && newValue >= range.max) {
                    newValue -= diff;
                }
                return newValue;
            }
        };
    }

    public static Limiter clamp() {
        return new Limiter() {
            @Override
            public float limitValue(float oldValue, float newValue, Range range) {
                if (range.inclusiveMin) {
                    if (newValue < range.min) {
                        return range.min;
                    }
                } else {
                    if (newValue <= range.min) {
                        return Math.nextAfter(range.min, Double.POSITIVE_INFINITY);
                    }
                }

                if (range.inclusiveMax) {
                    if (newValue > range.max) {
                        return range.max;
                    }
                } else {
                    if (newValue >= range.max) {
                        return Math.nextAfter(range.max, Double.NEGATIVE_INFINITY);
                    }
                }

                return newValue;
            }
        };
    }

}

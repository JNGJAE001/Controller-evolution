package za.redbridge.controller.NEATM.sensor;

import org.jbox2d.common.MathUtils;

import za.redbridge.controller.NEATM.sensor.parameter.SensorParameterSpec.BearingSpec;
import za.redbridge.controller.NEATM.sensor.parameter.SensorParameterSpec.FieldOfViewSpec;
import za.redbridge.controller.NEATM.sensor.parameter.SensorParameterSpec.OrientationSpec;
import za.redbridge.controller.NEATM.sensor.parameter.SensorParameterSpec.RangeSpec;
import za.redbridge.controller.NEATM.sensor.parameter.SensorParameterSpecSet;
import za.redbridge.controller.NEATM.sensor.parameter.spec.Range;


import static za.redbridge.controller.NEATM.sensor.parameter.spec.Initializers.copyExistingOrRandom;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.Initializers.random;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.Limiters.clamp;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.Limiters.wrap;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.Ranges.exclusiveRange;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.Ranges.plusMinusHalfPi;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.Ranges.plusMinusPi;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.Ranges.zeroToPi;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.Ranges.zeroTo2Pi;

/**
 * Contains the definitions of the valid parameters for each sensor type.
 *
 * Created by jamie on 2014/09/19.
 */
public enum SensorType {

    /**
     * new Spec(initial value, valid range, limiter)
     */

    BOTTOM_PROXIMITY(null),

    PROXIMITY(new SensorParameterSpecSet(
            new BearingSpec(random(), plusMinusPi(), wrap()),
            new OrientationSpec(random(), plusMinusHalfPi(), clamp()),
            // TODO: query environment size for range value
//            new RangeSpec(copyExistingOrRandom(), exclusiveRange(0.01f, 40), clamp()),
            new RangeSpec(copyExistingOrRandom(), exclusiveRange(0.0f, 0.2f), clamp()),
            new FieldOfViewSpec(copyExistingOrRandom(), new Range(0.1f, MathUtils.PI, true, true),
                    clamp()))),

    ULTRASONIC(new SensorParameterSpecSet(
            new BearingSpec(random(), plusMinusPi(), wrap()),
            new OrientationSpec(random(), plusMinusHalfPi(), clamp()),
            // TODO: query environment size for range value
//            new RangeSpec(copyExistingOrRandom(), exclusiveRange(0.01f, 40), clamp()),
            new RangeSpec(copyExistingOrRandom(), exclusiveRange(0.2f, 4.0f), clamp()),
            new FieldOfViewSpec(copyExistingOrRandom(), new Range(0.1f, MathUtils.PI, true, true),
                    clamp())))/*,

    PICKUP(new SensorParameterSpecSet(
        new BearingSpec(random(), plusMinusPi(), wrap()),
        new OrientationSpec(random(), plusMinusHalfPi(), clamp()),
        // TODO: query environment size for range value
//            new RangeSpec(copyExistingOrRandom(), exclusiveRange(0.01f, 40), clamp()),
        new RangeSpec(copyExistingOrRandom(), exclusiveRange(0.0f, 0.2f), clamp()),
        new FieldOfViewSpec(copyExistingOrRandom(), new Range(0.1f, MathUtils.PI, true, true),
            clamp()))),

    COLLISION(new SensorParameterSpecSet(
        new BearingSpec(random(), plusMinusPi(), wrap()),
        new OrientationSpec(random(), plusMinusHalfPi(), clamp()),
        // TODO: query environment size for range value
//            new RangeSpec(copyExistingOrRandom(), exclusiveRange(0.01f, 40), clamp()),
        new RangeSpec(copyExistingOrRandom(), exclusiveRange(0.0f, 0.2f), clamp()),
        new FieldOfViewSpec(copyExistingOrRandom(), new Range(0.1f, MathUtils.PI, true, true),
    clamp())))*/;

    private final SensorParameterSpecSet defaultSpecSet;

    SensorType(SensorParameterSpecSet defaultSpecSet) {
        this.defaultSpecSet = defaultSpecSet;
    }

    public boolean isConfigurable() {
        return defaultSpecSet != null;
    }

    public SensorParameterSpecSet getDefaultSpecSet() {
        return defaultSpecSet;
    }

}

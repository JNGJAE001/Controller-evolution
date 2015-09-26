package za.redbridge.controller.NEATM.sensor.parameter.spec;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by jamie on 2014/11/28.
 */
public interface Initializer extends Serializable {
    /**
     * Initializes the value of the parameter when a genome is being created.
     * @param range The valid range of the parameter value.
     * @param random A random number generator to use.
     * @return The initial value of the sensor.
     */
    float initializeValue(Range range, Random random);

    /**
     * Determines whether new sensors should be added with the value of this parameter taken from
     * existing sensors in the genome.
     * @return true if the value of this parameter should be copied from an existing sensor of the
     *      same type.
     */
    boolean shouldCopyExistingValue();
}

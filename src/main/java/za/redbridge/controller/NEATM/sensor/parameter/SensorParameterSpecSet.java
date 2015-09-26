package za.redbridge.controller.NEATM.sensor.parameter;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import za.redbridge.controller.NEATM.sensor.parameter.SensorParameterSpec.BearingSpec;
import za.redbridge.controller.NEATM.sensor.parameter.SensorParameterSpec.FieldOfViewSpec;
import za.redbridge.controller.NEATM.sensor.parameter.SensorParameterSpec.OrientationSpec;
import za.redbridge.controller.NEATM.sensor.parameter.SensorParameterSpec.RangeSpec;
import za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType;


import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.BEARING;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.FIELD_OF_VIEW;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.ORIENTATION;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.RANGE;

/**
 * Created by jamie on 2014/11/26.
 */
public class SensorParameterSpecSet {

    private final Map<ParameterType, SensorParameterSpec> parameterSpecs = new EnumMap<>(ParameterType.class);

    public SensorParameterSpecSet(BearingSpec bearing, OrientationSpec orientation,
            RangeSpec range, FieldOfViewSpec fieldOfView) {
        parameterSpecs.put(BEARING, bearing);
        parameterSpecs.put(ORIENTATION, orientation);
        parameterSpecs.put(RANGE, range);
        parameterSpecs.put(FIELD_OF_VIEW, fieldOfView);
    }

    private SensorParameterSpecSet(Map<ParameterType, SensorParameterSpec> parameterSpecs) {
        this.parameterSpecs.putAll(parameterSpecs);
    }

    public SensorParameterSpec getParameterSpec(ParameterType parameterType) {
        return parameterSpecs.get(parameterType);
    }

    public SensorParameterSet createParameterSet(Random random,
            SensorParameterSet existingParameters) {
        Map<ParameterType, SensorParameter> parameters = new EnumMap<>(ParameterType.class);
        for (SensorParameterSpec spec : this.parameterSpecs.values()) {
            // Check for an existing parameter
            SensorParameter existingParameter = null;
            if (existingParameters != null) {
                existingParameter = existingParameters.getParameter(spec.getParameterType());
            }

            // Create the new parameter
            SensorParameter parameter = spec.createParameter(random, existingParameter);
            parameters.put(spec.getParameterType(), parameter);
        }
        return new SensorParameterSet(parameters);
    }

    public Editor edit() {
        return new Editor(parameterSpecs);
    }

    public static class Editor {

        private final Map<ParameterType, SensorParameterSpec> parameterSpecs = new EnumMap<>(ParameterType.class);

        private Editor(Map<ParameterType, SensorParameterSpec> parameterSpecs) {
            this.parameterSpecs.putAll(parameterSpecs);
        }

        public Editor setParameterSpec(SensorParameterSpec spec) {
            parameterSpecs.put(spec.getParameterType(), spec);
            return this;
        }

        public SensorParameterSpecSet save() {
            return new SensorParameterSpecSet(parameterSpecs);
        }
    }

}

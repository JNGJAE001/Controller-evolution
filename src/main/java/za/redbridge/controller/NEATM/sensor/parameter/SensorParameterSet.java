package za.redbridge.controller.NEATM.sensor.parameter;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

import za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType;


import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.BEARING;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.FIELD_OF_VIEW;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.ORIENTATION;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.RANGE;

/**
 * Created by jamie on 2014/11/26.
 */
public class SensorParameterSet implements Cloneable, Serializable {

    private static final long serialVersionUID = 1700183124160179195L;

    private final Map<ParameterType, SensorParameter> parameters =
            new EnumMap<>(ParameterType.class);

    public SensorParameterSet(SensorParameter bearing, SensorParameter orientation,
            SensorParameter range, SensorParameter fieldOfView) {
        parameters.put(BEARING, bearing);
        parameters.put(ORIENTATION, orientation);
        parameters.put(RANGE, range);
        parameters.put(FIELD_OF_VIEW, fieldOfView);
    }

    SensorParameterSet(Map<ParameterType, SensorParameter> parameters) {
        this.parameters.putAll(parameters);
    }

    public SensorParameter getParameter(ParameterType parameterType) {
        return parameters.get(parameterType);
    }

    @Override
    public SensorParameterSet clone() {
        Map<ParameterType, SensorParameter> parameters = new EnumMap<>(ParameterType.class);
        for (Map.Entry<ParameterType, SensorParameter> entry : this.parameters.entrySet()) {
            parameters.put(entry.getKey(), entry.getValue().clone());
        }
        return new SensorParameterSet(parameters);
    }

    public Editor edit() {
        return new Editor(parameters);
    }

    public static class Editor {

        private final Map<ParameterType, SensorParameter> parameters = new EnumMap<>(ParameterType.class);

        private Editor(Map<ParameterType, SensorParameter> parameters) {
            this.parameters.putAll(parameters);
        }

        public Editor setParameter(SensorParameter parameter) {
            parameters.put(parameter.getSpec().getParameterType(), parameter);
            return this;
        }

        public SensorParameterSet save() {
            return new SensorParameterSet(parameters);
        }
    }

}

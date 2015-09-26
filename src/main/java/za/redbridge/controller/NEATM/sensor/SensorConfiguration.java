package za.redbridge.controller.NEATM.sensor;

import java.io.Serializable;

import za.redbridge.controller.NEATM.sensor.parameter.SensorParameterSet;


import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.BEARING;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.FIELD_OF_VIEW;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.ORIENTATION;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.RANGE;

/**
 * The mutable sensor configuration for a single sensor.
 *
 * Created by jamie on 2014/11/26.
 */
public class SensorConfiguration implements Cloneable, Serializable {

    private static final long serialVersionUID = -7123480667581109967L;

    private final SensorType sensorType;
    private final SensorParameterSet parameters;

    public SensorConfiguration(SensorType sensorType, SensorParameterSet parameters) {
        this.sensorType = sensorType;
        this.parameters = parameters;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public SensorParameterSet getSensorParameterSet() {
        return parameters;
    }

    public SensorModel toSensorModel() {
        if (!sensorType.isConfigurable()) {
            return new SensorModel(sensorType);
        }

        return new SensorModel(sensorType,
                parameters.getParameter(BEARING).getValue(),
                parameters.getParameter(ORIENTATION).getValue(),
                parameters.getParameter(RANGE).getValue(),
                parameters.getParameter(FIELD_OF_VIEW).getValue());
    }

    @Override
    public SensorConfiguration clone() {
        return new SensorConfiguration(sensorType, parameters != null ? parameters.clone() : null);
    }

}

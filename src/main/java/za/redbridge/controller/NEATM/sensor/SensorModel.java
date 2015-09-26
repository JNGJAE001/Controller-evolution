package za.redbridge.controller.NEATM.sensor;

import java.io.Serializable;

import za.redbridge.simulator.khepera.BottomProximitySensor;
import za.redbridge.simulator.khepera.ProximitySensor;
import za.redbridge.simulator.khepera.UltrasonicSensor;
import za.redbridge.simulator.sensor.AgentSensor;

/**
 * An immutable sensor configuration.
 *
 * Created by jamie on 2014/11/26.
 */
public class SensorModel implements Serializable {
    private static final long serialVersionUID = 3163879729851486644L;

    private final SensorType type;
    private final float bearing;
    private final float orientation;
    private final float range;
    private final float fieldOfView;

    public SensorModel(SensorType type, float bearing, float orientation, float range,
            float fieldOfView) {
        if (!type.isConfigurable()) {
            throw new IllegalArgumentException("Sensor type must be configurable");
        }

        this.type = type;
        this.bearing = bearing;
        this.orientation = orientation;
        this.range = range;
        this.fieldOfView = fieldOfView;
    }

    public SensorModel(SensorType type) {
        if (type.isConfigurable()) {
            throw new IllegalArgumentException("Sensor type must not be configurable");
        }

        this.type = type;

        bearing = orientation = range = fieldOfView = 0;
    }

    public SensorType getType() {
        return type;
    }

    public float getBearing() {
        return bearing;
    }

    public float getOrientation() {
        return orientation;
    }

    public float getRange() {
        return range;
    }

    public float getFieldOfView() {
        return fieldOfView;
    }

    public SensorModel setType(SensorType type) {
        return new SensorModel(type, bearing, orientation, range, fieldOfView);
    }

    public SensorModel setBearing(float bearing) {
        return new SensorModel(type, bearing, orientation, range, fieldOfView);
    }
    public SensorModel setOrientation(float orientation) {

        return new SensorModel(type, bearing, orientation, range, fieldOfView);
    }

    public SensorModel setRange(float range) {
        return new SensorModel(type, bearing, orientation, range, fieldOfView);
    }

    public SensorModel setFieldOfView(float fieldOfView) {
        return new SensorModel(type, bearing, orientation, range, fieldOfView);
    }

    public AgentSensor createSensor() {
        switch (type) {
            case BOTTOM_PROXIMITY:
                return new BottomProximitySensor();
            case PROXIMITY:
                return new ProximitySensor(bearing, orientation, range, fieldOfView);
            case ULTRASONIC:
                return new UltrasonicSensor(bearing, orientation, range, fieldOfView);
        }
        return null;
    }

}

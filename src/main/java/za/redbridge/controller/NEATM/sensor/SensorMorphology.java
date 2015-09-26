package za.redbridge.controller.NEATM.sensor;

import java.io.Serializable;
import java.util.Arrays;

import za.redbridge.simulator.khepera.BottomProximitySensor;
import za.redbridge.simulator.khepera.ProximitySensor;
import za.redbridge.simulator.khepera.UltrasonicSensor;
import za.redbridge.simulator.sensor.AgentSensor;

/**
 * Container class for the bearing and orientation values for a sensor morphology configuration.
 * Created by jamie on 2014/09/09.
 */
public class SensorMorphology implements Serializable {

    private static final long serialVersionUID = 5166321415840834464L;

    private final SensorModel[] sensorModels;

    public SensorMorphology(SensorModel[] sensorModels) {
        if (sensorModels.length == 0) {
            throw new IllegalArgumentException("There must be at least one sensor");
        }

        this.sensorModels =
                Arrays.copyOf(sensorModels, sensorModels.length);
    }

    public int getNumSensors() {
        return sensorModels.length;
    }

    public AgentSensor getSensor(int index) {
        checkValidIndex(index);

        SensorModel config = sensorModels[index];
        switch (config.getType()) {
            case BOTTOM_PROXIMITY:
                return new BottomProximitySensor();
            case PROXIMITY:
                return new ProximitySensor(config.getBearing(), config.getOrientation(),
                        config.getRange(), config.getFieldOfView());
            case ULTRASONIC:
                return new UltrasonicSensor(config.getBearing(), config.getOrientation(),
                        config.getRange(), config.getFieldOfView());
        }

        return null;
    }

    private void checkValidIndex(int index) {
        if (index < 0 || index >= getNumSensors()) {
            throw new IllegalArgumentException("Invalid sensor index: " + index);
        }
    }
}

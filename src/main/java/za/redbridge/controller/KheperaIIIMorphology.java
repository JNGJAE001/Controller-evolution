package za.redbridge.controller;

import za.redbridge.controller.NEATM.sensor.SensorModel;
import za.redbridge.controller.NEATM.sensor.SensorMorphology;
import za.redbridge.simulator.khepera.KheperaIIIPhenotype;
import za.redbridge.simulator.khepera.ProximitySensor;
import za.redbridge.simulator.khepera.UltrasonicSensor;


import static za.redbridge.controller.NEATM.sensor.SensorType.BOTTOM_PROXIMITY;
import static za.redbridge.controller.NEATM.sensor.SensorType.PROXIMITY;
import static za.redbridge.controller.NEATM.sensor.SensorType.ULTRASONIC;

/**
 * A horrible adapter class for different representations of morphologies. Creates a
 * {@link SensorMorphology} for a {@link KheperaIIIPhenotype.Configuration}.
 *
 * Created by jamie on 2014/10/06.
 */
public class KheperaIIIMorphology extends SensorMorphology {

    private static final long serialVersionUID = 8121207679231125300L;

    private static final KheperaIIIPhenotype.Configuration DEFAULT_CONFIGURATION =
            new KheperaIIIPhenotype.Configuration();
    static {
        DEFAULT_CONFIGURATION.enableProximitySensors40Degrees = true;
        DEFAULT_CONFIGURATION.enableProximitySensorBottom = true;
        DEFAULT_CONFIGURATION.enableUltrasonicSensor0Degrees = true;
        DEFAULT_CONFIGURATION.enableUltrasonicSensors90Degrees = true;
    }

    public KheperaIIIMorphology() {
        this(DEFAULT_CONFIGURATION);
    }

    public KheperaIIIMorphology(KheperaIIIPhenotype.Configuration config) {
        super(createSensorModels(config));
    }

    private static SensorModel[] createSensorModels(KheperaIIIPhenotype.Configuration config) {
        int sensorIndex = 0;
        final int sensorCount = config.getNumberOfSensors();
        SensorModel[] sensorModels = new SensorModel[sensorCount];

        if (config.enableProximitySensorBottom) {
            sensorModels[sensorIndex++] = new SensorModel(BOTTOM_PROXIMITY);
        }

        if (config.enableProximitySensors10Degrees) {
            sensorModels[sensorIndex++] = new SensorModel(PROXIMITY, (float) Math.toRadians(10), 0,
                    ProximitySensor.RANGE, ProximitySensor.FIELD_OF_VIEW);

            sensorModels[sensorIndex++] = new SensorModel(PROXIMITY, (float) Math.toRadians(-10), 0,
                    ProximitySensor.RANGE, ProximitySensor.FIELD_OF_VIEW);
        }

        if (config.enableProximitySensors40Degrees) {
            sensorModels[sensorIndex++] = new SensorModel(PROXIMITY, (float) Math.toRadians(40), 0,
                    ProximitySensor.RANGE, ProximitySensor.FIELD_OF_VIEW);

            sensorModels[sensorIndex++] = new SensorModel(PROXIMITY, (float) Math.toRadians(-40), 0,
                    ProximitySensor.RANGE, ProximitySensor.FIELD_OF_VIEW);
        }

        if (config.enableProximitySensors75Degrees) {
            sensorModels[sensorIndex++] = new SensorModel(PROXIMITY, (float) Math.toRadians(75), 0,
                    ProximitySensor.RANGE, ProximitySensor.FIELD_OF_VIEW);

            sensorModels[sensorIndex++] = new SensorModel(PROXIMITY, (float) Math.toRadians(-75), 0,
                    ProximitySensor.RANGE, ProximitySensor.FIELD_OF_VIEW);
        }

        if (config.enableProximitySensors140Degrees) {
            sensorModels[sensorIndex++] = new SensorModel(PROXIMITY, (float) Math.toRadians(140), 0,
                    ProximitySensor.RANGE, ProximitySensor.FIELD_OF_VIEW);

            sensorModels[sensorIndex++] = new SensorModel(PROXIMITY, (float) Math.toRadians(-140),
                    0, ProximitySensor.RANGE, ProximitySensor.FIELD_OF_VIEW);
        }

        if (config.enableProximitySensor180Degrees) {
            sensorModels[sensorIndex++] = new SensorModel(PROXIMITY, (float) Math.toRadians(180), 0,
                    ProximitySensor.RANGE, ProximitySensor.FIELD_OF_VIEW);
        }

        if (config.enableUltrasonicSensor0Degrees) {
            sensorModels[sensorIndex++] = new SensorModel(ULTRASONIC, 0, 0,
                    UltrasonicSensor.RANGE, UltrasonicSensor.FIELD_OF_VIEW);
        }

        if (config.enableUltrasonicSensors40Degrees) {
            sensorModels[sensorIndex++] = new SensorModel(ULTRASONIC, (float) Math.toRadians(40), 0,
                    UltrasonicSensor.RANGE, UltrasonicSensor.FIELD_OF_VIEW);

            sensorModels[sensorIndex++] = new SensorModel(ULTRASONIC, (float) Math.toRadians(-40),
                    0, UltrasonicSensor.RANGE, UltrasonicSensor.FIELD_OF_VIEW);
        }

        if (config.enableUltrasonicSensors90Degrees) {
            sensorModels[sensorIndex++] = new SensorModel(ULTRASONIC, (float) Math.toRadians(90), 0,
                    UltrasonicSensor.RANGE, UltrasonicSensor.FIELD_OF_VIEW);

            sensorModels[sensorIndex] = new SensorModel(ULTRASONIC, (float) Math.toRadians(-90), 0,
                    UltrasonicSensor.RANGE, UltrasonicSensor.FIELD_OF_VIEW);
        }

        return sensorModels;
    }
}

package za.redbridge.controller.NEATM.sensor.parameter.spec;

/**
 * The five different parameters for adjusting a sensor.
 *
 * Created by jamie on 2014/11/26.
 */
public enum ParameterType {
    /**
     * The bearing of the sensor relative to the forward vector of the robot.
     */
    BEARING(0.4),

    /**
     * The direction "offset" of the sensor.
     */
    ORIENTATION(0.1),

    /**
     * The range of the sensor (the distance it can "see").
     */
    RANGE(0.25),

    /**
     * The field of view of the sensor (how wide it can "see").
     */
    FIELD_OF_VIEW(0.25);

    private final double speciationWeighting;

    ParameterType(double speciationWeighting) {
        this.speciationWeighting = speciationWeighting;
    }

    public double getSpeciationWeighting() {
        return speciationWeighting;
    }
}

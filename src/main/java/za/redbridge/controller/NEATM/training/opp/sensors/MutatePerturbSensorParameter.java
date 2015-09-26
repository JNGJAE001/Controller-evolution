package za.redbridge.controller.NEATM.training.opp.sensors;

import org.apache.commons.math3.distribution.CauchyDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;

import java.util.List;
import java.util.Random;

import za.redbridge.controller.NEATM.sensor.SensorConfiguration;
import za.redbridge.controller.NEATM.sensor.parameter.SensorParameter;
import za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType;

/**
 * Created by jamie on 2014/09/08.
 */
public class MutatePerturbSensorParameter implements MutateSensor {

    private EvolutionaryAlgorithm trainer;

    private final double sigma;
    private final ParameterType parameterType;

    private final RealDistribution distribution = new CauchyDistribution();

    /**
     * Construct the perturbing mutator.
     *
     */
    public MutatePerturbSensorParameter(double sigma, ParameterType parameterType) {
        this.sigma = sigma;
        this.parameterType = parameterType;
    }

    @Override
    public EvolutionaryAlgorithm getTrainer() {
        return trainer;
    }

    @Override
    public void init(EvolutionaryAlgorithm theTrainer) {
        this.trainer = theTrainer;
    }

    @Override
    public void mutateSensor(Random rnd, SensorConfiguration sensorConfiguration) {
        // Mutate parameter
        double delta = distribution.sample() * sigma;
        SensorParameter sensorParameter =
                sensorConfiguration.getSensorParameterSet().getParameter(parameterType);

        float newValue = (float) (sensorParameter.getValue() + delta);
        sensorParameter.setValue(newValue);
    }

    @Override
    public void mutateSensorGroup(Random rnd, List<SensorConfiguration> configurations) {
        // Mutate parameter
        double delta = distribution.sample() * sigma;

        for (SensorConfiguration configuration : configurations) {
            SensorParameter sensorParameter =
                    configuration.getSensorParameterSet().getParameter(parameterType);
            float newValue = (float) (sensorParameter.getValue() + delta);
            sensorParameter.setValue(newValue);
        }
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("[")
                .append(this.getClass().getSimpleName())
                .append(":sigma=")
                .append(this.sigma)
                .append(":parameterType=")
                .append(this.parameterType)
                .append("]");
        return result.toString();
    }
}

package za.redbridge.controller.NEATM.training.opp.sensors;

import org.encog.ml.ea.train.EvolutionaryAlgorithm;

import java.util.List;
import java.util.Random;

import za.redbridge.controller.NEATM.sensor.SensorConfiguration;
import za.redbridge.controller.NEATM.sensor.parameter.spec.Range;
import za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType;

/**
 * Mutation that resets a sensor's position to a new random position.
 *
 * Created by jamie on 2014/10/14.
 */
public class MutateResetSensorParameter implements MutateSensor {

    private EvolutionaryAlgorithm trainer;

    private final ParameterType parameterType;

    public MutateResetSensorParameter(ParameterType parameterType) {
        this.parameterType = parameterType;
    }

    @Override
    public EvolutionaryAlgorithm getTrainer() {
        return trainer;
    }

    @Override
    public void init(EvolutionaryAlgorithm theTrainer) {
        trainer = theTrainer;
    }

    @Override
    public void mutateSensor(Random rnd, SensorConfiguration sensorConfiguration) {
        sensorConfiguration.getSensorParameterSet().getParameter(parameterType).randomize(rnd);
    }

    @Override
    public void mutateSensorGroup(Random rnd, List<SensorConfiguration> configurations) {
        if (configurations.isEmpty()) {
            return;
        }

        // Get the first sensor configuration to get the range
        Range range = configurations.get(0).getSensorParameterSet().getParameter(parameterType)
                .getSpec().getRange();
        float newValue = range.randomValueWithinRange(rnd);

        for (SensorConfiguration configuration : configurations) {
            configuration.getSensorParameterSet().getParameter(parameterType).setValue(newValue);
        }
    }

}

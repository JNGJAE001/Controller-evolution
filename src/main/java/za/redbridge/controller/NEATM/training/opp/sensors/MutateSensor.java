package za.redbridge.controller.NEATM.training.opp.sensors;

import org.encog.ml.ea.train.EvolutionaryAlgorithm;

import java.util.List;
import java.util.Random;

import za.redbridge.controller.NEATM.sensor.SensorConfiguration;

/**
 * Created by jamie on 2014/09/08.
 */
public interface MutateSensor {
    /**
     * @return The training class that this mutator is being used with.
     */
    EvolutionaryAlgorithm getTrainer();

    /**
     * Setup the sensor mutator.
     *
     * @param theTrainer
     *            The training class that this mutator is used with.
     */
    void init(EvolutionaryAlgorithm theTrainer);

    /**
     * Perform the parameter mutation on the specified sensor.
     *
     * @param rnd
     *            A random number generator.
     * @param sensorConfiguration
     *            The sensor configuration to mutate
     */
    void mutateSensor(Random rnd, SensorConfiguration sensorConfiguration);

    /**
     * Perform the same mutation on all the sensor configurations.
     *
     * @param rnd
     *            A random number generator.
     * @param configurations
     *            The list of sensor configuration to mutate
     */
    void mutateSensorGroup(Random rnd, List<SensorConfiguration> configurations);
}

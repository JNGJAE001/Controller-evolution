package za.redbridge.controller.NEATM;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;

import java.util.List;

import za.redbridge.controller.NEATM.sensor.SensorMorphology;

/**
 * Created by jamie on 2014/09/08.
 */
public class NEATMNetwork extends NEATNetwork {

    private static final long serialVersionUID = 6663988435664701217L;

    private final SensorMorphology sensorMorphology;

    /**
     * Construct a NEAT network. The links that are passed in also define the
     * neurons.
     *
     * @param inputNeuronCount       The input neuron count.
     * @param outputNeuronCount      The output neuron count.
     * @param connectionArray        The links.
     * @param theActivationFunctions The activation functions of the neurons.
     * @param sensorMorphology       The sensor morphology for this genome.
     */
    public NEATMNetwork(int inputNeuronCount, int outputNeuronCount,
            List<NEATLink> connectionArray, ActivationFunction[] theActivationFunctions,
            SensorMorphology sensorMorphology) {
        super(inputNeuronCount, outputNeuronCount, connectionArray, theActivationFunctions);
        this.sensorMorphology = sensorMorphology;
    }

    public SensorMorphology getSensorMorphology() {
        return sensorMorphology;
    }
}

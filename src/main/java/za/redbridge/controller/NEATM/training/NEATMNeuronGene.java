package za.redbridge.controller.NEATM.training;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.training.NEATNeuronGene;

import za.redbridge.controller.NEATM.sensor.SensorConfiguration;

/**
 * Created by jamie on 2014/09/08.
 */
public class NEATMNeuronGene extends NEATNeuronGene {

    private static final long serialVersionUID = 1583781843029771944L;

    private SensorConfiguration sensorConfiguration;

    public NEATMNeuronGene(NEATNeuronType type, ActivationFunction theActivationFunction, long id,
            long innovationID) {
        setNeuronType(type);
        setInnovationId(innovationID);
        setId(id);
        setActivationFunction(theActivationFunction);
    }

    /**
     * Construct this gene by comping another.
     * @param other The other gene to copy.
     */
    public NEATMNeuronGene(NEATMNeuronGene other) {
        copy(other);
    }

    /**
     * Copy another gene to this one.
     *
     * @param other
     *            The other gene.
     */
    public void copy(NEATMNeuronGene other) {
        setId(other.getId());
        setNeuronType(other.getNeuronType());
        setActivationFunction(other.getActivationFunction());
        setInnovationId(other.getInnovationId());

        if (getNeuronType() == NEATNeuronType.Input) {
            setSensorConfiguration(other.getSensorConfiguration().clone());
        }
    }

    public SensorConfiguration getSensorConfiguration() {
        return sensorConfiguration;
    }

    public void setSensorConfiguration(SensorConfiguration sensorConfiguration) {
        this.sensorConfiguration = sensorConfiguration;
    }

    private void checkInputNeuron() {
        if (getNeuronType() != NEATNeuronType.Input) {
            throw new UnsupportedOperationException("Not an input neuron");
        }
    }
}

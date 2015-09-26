package za.redbridge.controller.SANE;

import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.genome.GenomeFactory;

import java.io.Serializable;

/**
 * Created by jae on 2015/09/14.
 * Neuron genome factory
 */

public class NeuronGenomeFactory implements GenomeFactory, Serializable
{
    //length of the connection chromosome
    int size;

    //constructor
    public NeuronGenomeFactory(int size)
    {
        this.size = size;
    }

    //create neuron genome with empty chromosome
    @Override
    public NeuronGenome factor()
    {
        return new NeuronGenome(size);
    }

    //create a copy of neuron genome
    @Override
    public NeuronGenome factor(Genome other)
    {
        return new NeuronGenome((NeuronGenome) other);
    }
}

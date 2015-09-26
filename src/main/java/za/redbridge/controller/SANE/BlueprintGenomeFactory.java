package za.redbridge.controller.SANE;

import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.genome.GenomeFactory;

import java.io.Serializable;

/**
 * Created by jae on 2015/09/14.
 * Blueprint genome factory
 */

public class BlueprintGenomeFactory implements GenomeFactory, Serializable
{
    //length of blueprint
    int size;

    //constructor
    public BlueprintGenomeFactory(int size)
    {
        this.size = size;
    }

    //creates new blueprint genome with empty blueprint
    @Override
    public BlueprintGenome factor()
    {
        return new BlueprintGenome(size);
    }

    //creates a copy of blueprint genome
    @Override
    public BlueprintGenome factor(Genome other)
    {
        return new BlueprintGenome((BlueprintGenome) other);
    }

    public BlueprintGenome factor(NeuronGenome[] blueprint)
    {
        return new BlueprintGenome(blueprint);
    }
}

package za.redbridge.controller.SANE;

import org.encog.ml.ea.genome.BasicGenome;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.genetic.genome.ArrayGenome;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by jae on 2015/09/11.
 * Genome for blueprints (SANE).
 */
public class BlueprintGenome extends BasicGenome implements ArrayGenome, Serializable
{
    //blueprint of neuron combination
    private NeuronGenome[] blueprint;

    //constructor - constructs empty blueprint
    public BlueprintGenome(int size)
    {
        blueprint = new NeuronGenome[size];
    }

    //constructor - receives blueprint
    public BlueprintGenome(NeuronGenome[] blueprint)
    {
        this.blueprint = blueprint;
    }

    //copy constructor
    public BlueprintGenome(BlueprintGenome b)
    {
        this.blueprint = new NeuronGenome[b.blueprint.length];
        for (int i = 0; i < blueprint.length; i++)
        {
            this.blueprint[i] = b.blueprint[i];
        }
    }


    @Override
    //copies the neuron reference
    public void copy(ArrayGenome source, int sourceIndex, int targetIndex)
    {
        //point to the same neuron
        this.blueprint[targetIndex] = (((BlueprintGenome) source).blueprint[sourceIndex]);
    }

    @Override
    public void swap(int swap1, int swap2)
    {
        NeuronGenome temp = blueprint[swap1];
        blueprint[swap1] = blueprint[swap2];
        blueprint[swap2] = temp;
    }

    @Override
    public void copy(Genome source)
    {
        BlueprintGenome sourceNeuron = (BlueprintGenome)source;
        for (int i = 0; i < blueprint.length; i++)
        {
            blueprint[i] = sourceNeuron.blueprint[i];
        }
        setScore(source.getScore());
        setAdjustedScore(source.getAdjustedScore());
    }

    @Override
    public int size()
    {
        return blueprint.length;
    }

    //returns data
    public NeuronGenome[] getBlueprint()
    {
        return blueprint;
    }
}

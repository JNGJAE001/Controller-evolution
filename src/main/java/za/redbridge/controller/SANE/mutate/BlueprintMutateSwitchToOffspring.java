package za.redbridge.controller.SANE.mutate;

import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.EvolutionaryOperator;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import za.redbridge.controller.SANE.BlueprintGenome;
import za.redbridge.controller.SANE.NeuronGenome;
import za.redbridge.controller.SANE.SANE;

import java.util.List;
import java.util.Random;

/**
 * Created by jae on 2015/09/13.
 */
public class BlueprintMutateSwitchToOffspring implements EvolutionaryOperator
{
    /**
     * The owner.
     */
    private EvolutionaryAlgorithm owner;

    //chance of mutation occurring
    private double mutation_rate;

    public BlueprintMutateSwitchToOffspring(double d)
    {
        mutation_rate = d;
    }

    @Override
    public void init(EvolutionaryAlgorithm evolutionaryAlgorithm)
    {
        owner = evolutionaryAlgorithm;
    }

    @Override
    public int offspringProduced()
    {
        return 1;
    }

    @Override
    public int parentsNeeded()
    {
        return 1;
    }

    @Override
    public void performOperation(Random random, Genome[] parents, int parentIndex,
                                 Genome[] BlueprintGenomes, int blueprintIndex)
    {
        //blueprint genomes array
        BlueprintGenome B =  (BlueprintGenome)BlueprintGenomes[parentIndex];

        //perform mutation on each neuron reference
        for (int i = 0; i < SANE.HIDDEN_SIZE; i++)
        {
            if(random.nextFloat() <= mutation_rate)
            {
                //neuron reference to mutate
                NeuronGenome neuron = B.getBlueprint()[i];

                //children of the neuron
                List<NeuronGenome> children = neuron.getChildren();

                //switch to a random offspring with 50% chance
                if (random.nextFloat() >= 0.5 && !children.isEmpty())
                    B.getBlueprint()[i] = children.get(random.nextInt(children.size()));
            }
        }
    }
}

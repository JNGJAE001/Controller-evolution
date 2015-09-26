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
 * Blueprint mutation which causes pointers to switch to random neuron
 */
public class BlueprintMutateSwitchToRandom implements EvolutionaryOperator
{

    /**
     * The owner.
     */
    private EvolutionaryAlgorithm owner;

    //chance of mutation occuring
    private double mutation_rate;

    public BlueprintMutateSwitchToRandom(double d)
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
    public void performOperation(Random random, Genome[] NeuronGenomes, int parentIndex,
                                 Genome[] BlueprintGenomes, int blueprintIndex)
    {
        //blueprint genomes array
        BlueprintGenome[] B =  (BlueprintGenome[])BlueprintGenomes;

        //neuron genomes array
        NeuronGenome[] N = (NeuronGenome[]) NeuronGenomes;

        //random neuron genome to be switched to
        NeuronGenome neuron;

        //perform mutation on each neuron reference
        for (int i = 0; i < SANE.HIDDEN_SIZE; i++)
        {
            if(random.nextFloat() <= mutation_rate)
            {
                //random selection
                neuron = N[random.nextInt(NeuronGenomes.length)];

                //re-roll if mutated to same neuron pointer
                while (neuron == B[blueprintIndex].getBlueprint()[i])
                {
                    neuron = N[random.nextInt(NeuronGenomes.length)];
                }

                //new reference
                B[blueprintIndex].getBlueprint()[i] = neuron;
            }
        }
    }

    public void performOperation(Random random, List<Genome> NeuronGenomes, int parentIndex,
                                 Genome[] BlueprintGenomes, int blueprintIndex)
    {
        //blueprint genomes array
        BlueprintGenome B = (BlueprintGenome) BlueprintGenomes[blueprintIndex];

        //random neuron genome to be switched to
        NeuronGenome neuron;

        //perform mutation on each neuron reference
        for (int i = 0; i < SANE.HIDDEN_SIZE; i++)
        {
            if (random.nextFloat() <= mutation_rate)
            {
                //random selection
                neuron = (NeuronGenome) NeuronGenomes.get(random.nextInt(NeuronGenomes.size()));

                //re-roll if mutated to same neuron pointer
                while (neuron == B.getBlueprint()[i])
                {
                    neuron = (NeuronGenome) NeuronGenomes.get(random.nextInt(NeuronGenomes.size()));
                }

                //new reference
                B.getBlueprint()[i] = neuron;
            }
        }
    }
}

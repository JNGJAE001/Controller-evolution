package za.redbridge.controller.SANE.crossover;

import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.EvolutionaryOperator;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import za.redbridge.controller.SANE.BlueprintGenome;
import za.redbridge.controller.SANE.SANE;

import java.util.Random;

/**
 * Created by jae on 2015/09/13.
 * crossover operation for blueprints
 */
public class BlueprintCrossover implements EvolutionaryOperator
{
    /**
     * The owner.
     */
    private EvolutionaryAlgorithm owner;

    @Override
    public void init(EvolutionaryAlgorithm evolutionaryAlgorithm)
    {
        owner = evolutionaryAlgorithm;
    }

    @Override
    public int offspringProduced()
    {
        return 2;
    }

    @Override
    public int parentsNeeded()
    {
        return 2;
    }


    @Override
    public void performOperation(Random random, Genome[] parents, int parentIndex,
                                 Genome[] offspring, int offspringIndex)
    {
        //point of crossover
        int point = random.nextInt(SANE.HIDDEN_SIZE);

        //two parents for mating
        BlueprintGenome mother = (BlueprintGenome)parents[parentIndex];
        BlueprintGenome father = (BlueprintGenome)parents[parentIndex+1];

        //child produced by offspring
        BlueprintGenome offspring1 = (BlueprintGenome)this.owner.getPopulation().getGenomeFactory().factor();
        BlueprintGenome offspring2 = (BlueprintGenome)this.owner.getPopulation().getGenomeFactory().factor();
        offspring[offspringIndex] = offspring1;
        offspring[offspringIndex+1] = offspring2;

        //produce offspring which inherits portion of each parent's gene
        //before crossover point
        for (int i = 0; i < point; i++)
        {
            offspring1.copy(mother, i, i);
            offspring2.copy(father, i ,i);
        }
        //after crossover point
        for (int i = point; i < SANE.HIDDEN_SIZE; i++)
        {
            offspring1.copy(father, i, i);
            offspring2.copy(mother, i ,i);
        }
    }
}

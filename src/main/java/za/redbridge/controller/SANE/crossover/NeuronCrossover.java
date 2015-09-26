package za.redbridge.controller.SANE.crossover;

import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.EvolutionaryOperator;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.ml.genetic.GeneticError;
import za.redbridge.controller.SANE.BasicSANE;
import za.redbridge.controller.SANE.Connection;
import za.redbridge.controller.SANE.NeuronGenome;
import za.redbridge.controller.SANE.SANE;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by jae on 2015/09/11.
 * simple crossover operation for hidden neurons
 */

public class NeuronCrossover implements EvolutionaryOperator
{

    /**
     * The owner.
     */
    private BasicSANE owner;

    @Override
    public void init(EvolutionaryAlgorithm evolutionaryAlgorithm)
    {
        owner = (BasicSANE) evolutionaryAlgorithm;
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


    //returns connection with no repeat
    private static Connection getNotTaken(final NeuronGenome source,
                                   final Set<Integer> taken) {

        for (final Connection trial : source.getChromosome()) {
            if (!taken.contains(trial.getLabel())) {
                taken.add(trial.getLabel());
                return new Connection(trial.getLabel(), trial.getWeight());
            }
        }

        throw new GeneticError("Ran out of integers to select.");
    }

    //1-point crossover
    @Override
    public void performOperation(Random random, Genome[] parents, int parentIndex,
                                 Genome[] offspring, int offspringIndex)
    {
        //point of crossover
        int point = random.nextInt(SANE.CHROMOSOME_LENGTH);

        //two parents for mating
        NeuronGenome mother = (NeuronGenome) parents[parentIndex];
        NeuronGenome father = (NeuronGenome) parents[parentIndex + 1];

        //child produced by offspring
        NeuronGenome offspring1 = (NeuronGenome) this.owner.getNeuronPopulation().getGenomeFactory().factor();
        offspring[offspringIndex] = offspring1;

        //one of the offspring is copy of parent
        NeuronGenome parentCopy;

        //keep one of the parents as offspring
        if (random.nextFloat() >= 0.5)
        {
            parentCopy = new NeuronGenome(mother);
            offspring[offspringIndex + 1] = parentCopy;
            mother.add_children(parentCopy);
        } else
        {
            parentCopy = new NeuronGenome(father);
            offspring[offspringIndex + 1] = parentCopy;
            father.add_children(parentCopy);
        }
        final Set<Integer> taken = new HashSet<Integer>();

        //only need one offspring
        if (random.nextFloat() >= 0.5)
        {
            //produce offspring which inherits portion of each parent's gene
            for (int i = 0; i < point; i++)
            {
                offspring1.copy(mother, i, i);
                taken.add(mother.getChromosome()[i].getLabel());
            }
            for (int i = point; i < SANE.CHROMOSOME_LENGTH; i++)
            {
                offspring1.getChromosome()[i] = getNotTaken(father, taken);

                //offspring1.copy(father, i, i);
            }
        } else
        {
            //produce offspring which inherits portion of each parent's gene
            for (int i = 0; i < point; i++)
            {
                offspring1.copy(father, i, i);
                taken.add(father.getChromosome()[i].getLabel());
            }
            for (int i = point; i < SANE.CHROMOSOME_LENGTH; i++)
            {
                offspring1.getChromosome()[i] = getNotTaken(mother, taken);
                //offspring1.copy(mother,i,i);
            }
        }

        //keep track of children for blueprint mutation
        mother.add_children(offspring1);
        father.add_children(offspring1);
    }
}

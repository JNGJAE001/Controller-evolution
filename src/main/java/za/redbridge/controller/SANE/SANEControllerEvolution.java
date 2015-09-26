package za.redbridge.controller.SANE;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.ml.MethodFactory;
import org.encog.ml.TrainingImplementationType;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.ml.ea.population.BasicPopulation;
import org.encog.ml.ea.population.Population;
import org.encog.ml.ea.sort.GenomeComparator;
import org.encog.ml.ea.sort.MaximizeScoreComp;
import org.encog.ml.ea.sort.MinimizeScoreComp;
import org.encog.ml.ea.species.Species;
import org.encog.ml.genetic.crossover.Splice;
import org.encog.ml.genetic.mutate.MutatePerturb;
import org.encog.ml.train.BasicTraining;
import org.encog.neural.networks.training.propagation.TrainingContinuation;
import org.encog.util.concurrency.MultiThreadable;
import org.encog.util.logging.EncogLogging;

import java.util.List;
import java.util.Random;

/**
 * Implements a genetic algorithm that allows an MLMethod that is encodable
 * (MLEncodable) to be trained. It works well with both BasicNetwork and
 * FreeformNetwork class, as well as any MLEncodable class.
 * <p>
 * There are essentially two ways you can make use of this class.
 * <p>
 * Either way, you will need a score object. The score object tells the genetic
 * algorithm how well suited a neural network is.
 * <p>
 * If you would like to use genetic algorithms with a training set you should
 * make use TrainingSetScore class. This score object uses a training set to
 * score your neural network.
 * <p>
 * If you would like to be more abstract, and not use a training set, you can
 * create your own implementation of the CalculateScore method. This class can
 * then score the networks any way that you like.
 */

public class SANEControllerEvolution extends BasicTraining implements
        MultiThreadable
{
    /**
     * Very simple class that implements a genetic algorithm.
     *
     * @author jheaton
     */
    public class SANEControllerEvolutionHelper extends TrainSANE
    {
        /**
         * The serial id.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Construct the helper.
         *
         * @param blueprintPopulation    The population.
         * @param theScoreFunction The score function.
         */
        public SANEControllerEvolutionHelper(final Population blueprintPopulation, final Population neuronPopulation,
                                              final CalculateScore theScoreFunction)
        {
            super(blueprintPopulation, neuronPopulation, theScoreFunction);
        }

    }

    /**
     * Simple helper class that implements the required methods to implement a
     * genetic algorithm.
     */
    private SANEControllerEvolutionHelper genetic;

    /**
     * Construct a method genetic algorithm.
     *
     * @param phenotypeFactory The phenotype factory.
     * @param calculateScore   The score calculation object.
     * @param populationSize   The population size.
     */
    public SANEControllerEvolution(final MethodFactory phenotypeFactory,
                                   final CalculateScore calculateScore, final int populationSize)
    {
        super(TrainingImplementationType.Iterative);
        Random random = new Random();

        // Create the population
        final Population blueprint_population = new BasicPopulation(populationSize, null);
        final Species blueprintSpecies = blueprint_population.createSpecies();

        final Population neuron_population = new BasicPopulation(populationSize, null);
        final Species neuronSpecies = neuron_population.createSpecies();
        neuronSpecies.setOffspringCount(populationSize);
        //factory to produce genomes
        NeuronGenomeFactory neuronFac = new NeuronGenomeFactory(SANE.CHROMOSOME_LENGTH);
        BlueprintGenomeFactory blueprintFac = new BlueprintGenomeFactory(SANE.HIDDEN_SIZE);

        //initialize Neuron population
        for (int i = 0; i < neuron_population.getPopulationSize(); i++)
        {
            //create neuron genome
            final NeuronGenome genome = neuronFac.factor();

            //intialize chromosome randomly
            genome.randomInit();

            //add the genome
            neuronSpecies.add(genome);
        }


        //initialize blueprint population
        for (int i = 0; i < blueprint_population.getPopulationSize(); i++)
        {
            //final MLEncodable chromosomeNetwork = (MLEncodable) phenotypeFactory.factor();

            //final MLMethodGenome genome = new MLMethodGenome(chromosomeNetwork);

            NeuronGenome[] blueprint = new NeuronGenome[SANE.HIDDEN_SIZE];

            List<Genome> neurons = neuron_population.getSpecies().get(0).getMembers();

            //randomly initialize blueprint
            for (int b = 0; b < blueprint.length; b++)
            {
                blueprint[b] = (NeuronGenome)neurons.get(random.nextInt(neurons.size()));
            }


            //create blueprint genome
            final BlueprintGenome genome = blueprintFac.factor(blueprint);

            //add the genome
            blueprintSpecies.add(genome);
        }

        blueprintSpecies.setLeader(blueprintSpecies.getMembers().get(0));
        neuronSpecies.setLeader(neuronSpecies.getMembers().get(0));

        //set population factory
        blueprint_population.setGenomeFactory(blueprintFac);
        neuron_population.setGenomeFactory(neuronFac);

        // create the trainer
        this.genetic = new SANEControllerEvolutionHelper(blueprint_population, neuron_population,calculateScore);

        this.genetic.setMaxChildren(2);
        this.genetic.setMaxParents(2);
        //SANE codec
        this.genetic.setCODEC(new SANECODEC());

        //50% of population is preserved
        this.genetic.setEliteRate(0.5);

        //rank based selection
        this.genetic.setSelection(new TruncationSelection(this.genetic, 0.25));

        GenomeComparator comp = null;
        if (calculateScore.shouldMinimize())
        {
            comp = new MinimizeScoreComp();
        } else
        {
            comp = new MaximizeScoreComp();
        }
        this.genetic.setBestComparator(comp);
        this.genetic.setSelectionComparator(comp);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canContinue()
    {
        return false;
    }

    /**
     * @return The genetic algorithm implementation.
     */
    public SANEControllerEvolutionHelper getGenetic()
    {
        return this.genetic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MLMethod getMethod()
    {
        final Genome best = this.genetic.getBestGenome();
        return this.genetic.getCODEC().decode(best);
    }

    @Override
    public int getThreadCount()
    {
        return this.genetic.getThreadCount();
    }

    /**
     * Perform one training iteration.
     */
    @Override
    public void iteration()
    {

        EncogLogging.log(EncogLogging.LEVEL_INFO,
                "Performing Genetic iteration.");
        preIteration();
        setError(getGenetic().getError());
        getGenetic().iteration();
        setError(getGenetic().getError());
        postIteration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TrainingContinuation pause()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resume(final TrainingContinuation state)
    {

    }

    /**
     * Set the genetic helper class.
     *
     * @param genetic The genetic helper class.
     */
    public void setGenetic(final SANEControllerEvolutionHelper genetic)
    {
        this.genetic = genetic;
    }

    @Override
    public void setThreadCount(final int numThreads)
    {
        this.genetic.setThreadCount(numThreads);

    }
}

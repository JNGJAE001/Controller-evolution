package za.redbridge.controller.SANE;

import org.encog.Encog;
import org.encog.EncogError;
import org.encog.EncogShutdownTask;
import org.encog.mathutil.randomize.factory.RandomFactory;
import org.encog.ml.CalculateScore;
import org.encog.ml.MLContext;
import org.encog.ml.MLMethod;
import org.encog.ml.ea.codec.GeneticCODEC;
import org.encog.ml.ea.codec.GenomeAsPhenomeCODEC;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.EvolutionaryOperator;
import org.encog.ml.ea.opp.OperationList;
import org.encog.ml.ea.opp.selection.SelectionOperator;
import org.encog.ml.ea.opp.selection.TournamentSelection;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.ml.ea.population.Population;
import org.encog.ml.ea.rules.BasicRuleHolder;
import org.encog.ml.ea.rules.RuleHolder;
import org.encog.ml.ea.score.AdjustScore;
import org.encog.ml.ea.score.parallel.ParallelScore;
import org.encog.ml.ea.sort.*;
import org.encog.ml.ea.species.SingleSpeciation;
import org.encog.ml.ea.species.Speciation;
import org.encog.ml.ea.species.Species;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.ml.genetic.GeneticError;
import org.encog.util.concurrency.MultiThreadable;
import org.encog.util.logging.EncogLogging;
import za.redbridge.controller.SANE.crossover.BlueprintCrossover;
import za.redbridge.controller.SANE.crossover.NeuronCrossover;
import za.redbridge.controller.SANE.mutate.BlueprintMutateSwitchToOffspring;
import za.redbridge.controller.SANE.mutate.BlueprintMutateSwitchToRandom;
import za.redbridge.controller.SANE.mutate.NeuronMutate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Provides a basic implementation of a multi-threaded Evolutionary Algorithm.
 * The EA works from a score function.
 */
public class BasicSANE implements EvolutionaryAlgorithm, MultiThreadable,
        EncogShutdownTask, Serializable
{

    /**
     * The serial id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Calculate the score adjustment, based on adjusters.
     *
     * @param genome    The genome to adjust.
     * @param adjusters The score adjusters.
     */
    public static void calculateScoreAdjustment(final Genome genome,
                                                final List<AdjustScore> adjusters)
    {
        final double score = genome.getScore();
        double delta = 0;

        for (final AdjustScore a : adjusters)
        {
            delta += a.calculateAdjustment(genome);
        }

        genome.setAdjustedScore(score + delta);
    }

    /**
     * Should exceptions be ignored.
     */
    private boolean ignoreExceptions;

    /**
     * The genome comparator.
     */
    private GenomeComparator bestComparator;

    /**
     * The genome comparator.
     */
    private GenomeComparator selectionComparator;

    /**
     * The population.
     */
    private Population blueprint_population, neuron_population;

    /**
     * The score calculation function.
     */
    private final CalculateScore scoreFunction;

    /**
     * The selection operator.
     */
    private SelectionOperator selection;

    /**
     * The score adjusters.
     */
    private final List<AdjustScore> adjusters = new ArrayList<AdjustScore>();

    /**
     * The operators. to use.
     */
    private final OperationList operators = new OperationList();

    /**
     * The CODEC to use to convert between genome and phenome.
     */
    private GeneticCODEC codec = new GenomeAsPhenomeCODEC();

    /**
     * Random number factory.
     */
    private RandomFactory randomNumberFactory = Encog.getInstance()
            .getRandomFactory().factorFactory();

    /**
     * The validation mode.
     */
    private boolean validationMode;

    /**
     * The iteration number.
     */
    private int iteration;

    /**
     * The desired thread count.
     */
    private int threadCount;

    /**
     * The actual thread count.
     */
    private int actualThreadCount = -1;

    /**
     * The speciation method.
     */
    private SANESpeciation speciation = new SANESpeciation();

    /**
     * This property stores any error that might be reported by a thread.
     */
    private Throwable reportedError;

    /**
     * The best genome from the last iteration.
     */
    private Genome oldBestGenome;

    /**
     * The population for the next iteration.
     */
    private final List<Genome> newBlueprints = new ArrayList<Genome>();
    /**
     * The population for the next iteration.
     */
    private final List<Genome> newNeurons = new ArrayList<Genome>();

    /**
     * The mutation to be used on the top genome. We want to only modify its
     * weights.
     */
    private EvolutionaryOperator champMutation;

    //neuron genetic operators
    private EvolutionaryOperator neuron_crossover, neuron_mutate;

    //blueprint genetic operators
    private EvolutionaryOperator blueprint_crossover, blueprint_mutate_offspring;
    private BlueprintMutateSwitchToRandom blueprint_mutate_random;


    private int max_parents, max_children;

    /**
     * The percentage of a species that is "elite" and is passed on directly.
     */
    private double eliteRate = 0.3;

    /**
     * The number of times to try certian operations, so an endless loop does
     * not occur.
     */
    private int maxTries = 5;

    /**
     * The best ever genome.
     */
    private Genome bestGenome;

    /**
     * The thread pool executor.
     */
    private ExecutorService taskExecutor;

    /**
     * Holds the threads used each iteration.
     */
    private final List<Callable<Object>> threadList = new ArrayList<Callable<Object>>();

    /**
     * Holds rewrite and constraint rules.
     */
    private RuleHolder rules;

    private int maxOperationErrors = 500;

    /**
     * Construct an EA.
     *
     * @param blueprintPopulation    The population.
     * @param theScoreFunction The score function.
     */
    public BasicSANE(final Population blueprintPopulation, final Population neuronPopulation,
                     final CalculateScore theScoreFunction)
    {

        this.blueprint_population = blueprintPopulation;
        this.neuron_population = neuronPopulation;

        this.scoreFunction = theScoreFunction;
        //this.selection = new TournamentSelection(this, 4);
        this.selection = new TruncationSelection(this, 0.25);
        this.rules = new BasicRuleHolder();

        //neuron crossover
        this.neuron_crossover = new NeuronCrossover();
        this.neuron_crossover.init(this);

        //neuron mutation
        this.neuron_mutate = new NeuronMutate(SANE.neuron_mutation_rate);
        this.neuron_mutate.init(this);

        //blueprint crossover
        this.blueprint_crossover = new BlueprintCrossover();
        this.blueprint_crossover.init(this);

        //blueprint mutations
        this.blueprint_mutate_random = new BlueprintMutateSwitchToRandom(SANE.blueprint_mutation_rate_random);
        this.blueprint_mutate_random.init(this);
        this.blueprint_mutate_offspring = new BlueprintMutateSwitchToOffspring(SANE.blueprint_mutation_rate_offspring);
        this.blueprint_mutate_offspring.init(this);

        // set the score compare method
        if (theScoreFunction.shouldMinimize())
        {
            this.selectionComparator = new MinimizeAdjustedScoreComp();
            this.bestComparator = new MinimizeScoreComp();
        } else
        {
            this.selectionComparator = new MaximizeAdjustedScoreComp();
            this.bestComparator = new MaximizeScoreComp();
        }

        // set the iteration
        for (final Species species : blueprint_population.getSpecies())
        {
            for (final Genome genome : species.getMembers())
            {
                setIteration(Math.max(getIteration(),genome.getBirthGeneration()));
            }
        }

        // set the iteration
        for (final Species species : neuron_population.getSpecies())
        {
            for (final Genome genome : species.getMembers())
            {
                setIteration(Math.max(getIteration(),genome.getBirthGeneration()));
            }
        }

        // Set a best genome, just so it is not null.
        // We won't know the true best genome until the first iteration.
        if (this.blueprint_population.getSpecies().size() > 0 && this.blueprint_population.getSpecies().get(0).getMembers().size() > 0)
        {
            this.bestGenome = this.blueprint_population.getSpecies().get(0).getMembers().get(0);
        }
    }

    /**
     * Add a child to the next iteration.
     *
     * @param genome The child.
     * @return True, if the child was added successfully.
     */
    public boolean addChild(final Genome genome)
    {
        synchronized (newBlueprints)
        {
            System.out.println("blueprint list size :" + this.newBlueprints.size());
            System.out.println("blueprint population count :" + getPopulation().getPopulationSize());

            if (this.newBlueprints.size() < getPopulation().getPopulationSize())
            {
                // don't readd the old best genome, it was already added
                if (genome != this.oldBestGenome)
                {
                    if (isValidationMode())
                    {
                        if (this.newBlueprints.contains(genome))
                        {
                            throw new EncogError(
                                    "Genome already added to population: "
                                            + genome.toString());
                        }
                    }

                    this.newBlueprints.add(genome);
                }

                if (!Double.isInfinite(genome.getScore()) && !Double.isNaN(genome.getScore())&& getBestComparator().isBetterThan(genome,this.bestGenome))
                {
                    this.bestGenome = genome;
                    getPopulation().setBestGenome(this.bestGenome);
                }
                return true;
            } else
            {
                return false;
            }
        }
    }

    //adds neuron children
    public boolean addChildNeuron(final Genome genome)
    {
        synchronized (newNeurons)
        {
            System.out.println("neuron list size :" + this.newNeurons.size());
            System.out.println("neuron population count :" + getNeuronPopulation().getPopulationSize());

            if (this.newNeurons.size() < getNeuronPopulation().getPopulationSize())
            {
                this.newNeurons.add(genome);
                System.out.println("adding neuron true");
                return true;
            } else
            {
                System.out.println("adding neuron false");
                return false;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addOperation(final double probability,
                             final EvolutionaryOperator opp)
    {
        getOperators().add(probability, opp);
        opp.init(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addScoreAdjuster(final AdjustScore scoreAdjust)
    {
        this.adjusters.add(scoreAdjust);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void calculateScore(final Genome g)
    {

        BlueprintGenome blueprint = (BlueprintGenome) g;

        // try rewrite
        this.rules.rewrite(blueprint);

        // decode
        final MLMethod phenotype = getCODEC().decode(blueprint);
        double score;

        // deal with invalid decode
        if (phenotype == null)
        {
            if (getBestComparator().shouldMinimize())
            {
                score = Double.POSITIVE_INFINITY;
            } else
            {
                score = Double.NEGATIVE_INFINITY;
            }
        } else
        {
            if (phenotype instanceof MLContext)
            {
                ((MLContext) phenotype).clearContext();
            }
            System.out.println("calculation starting");
            score = getScoreFunction().calculateScore(phenotype);
        }

        // now set the scores
        blueprint.setScore(score);
        blueprint.setAdjustedScore(score);


    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishTraining()
    {

        // wait for threadpool to shutdown
        if (this.taskExecutor != null)
        {
            this.taskExecutor.shutdown();
            try
            {
                this.taskExecutor.awaitTermination(Long.MAX_VALUE,
                        TimeUnit.MINUTES);
            }
            catch (final InterruptedException e)
            {
                throw new GeneticError(e);
            }
            finally
            {
                this.taskExecutor = null;
                Encog.getInstance().removeShutdownTask(this);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenomeComparator getBestComparator()
    {
        return this.bestComparator;
    }

    /**
     * @return the bestGenome
     */
    @Override
    public Genome getBestGenome()
    {
        return this.bestGenome;
    }

    /**
     * @return the champMutation
     */
    public EvolutionaryOperator getChampMutation()
    {
        return this.champMutation;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public GeneticCODEC getCODEC()
    {
        return this.codec;
    }

    /**
     * @return the eliteRate
     */
    public double getEliteRate()
    {
        return this.eliteRate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getError()
    {
        // do we have a best genome, and does it have an error?
        if (this.bestGenome != null)
        {
            double err = this.bestGenome.getScore();
            if (!Double.isNaN(err))
            {
                return err;
            }
        }

        // otherwise, assume the worst!
        if (getScoreFunction().shouldMinimize())
        {
            return Double.POSITIVE_INFINITY;
        } else
        {
            return Double.NEGATIVE_INFINITY;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIteration()
    {
        return this.iteration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxIndividualSize()
    {
        return this.blueprint_population.getMaxIndividualSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxTries()
    {
        return this.maxTries;
    }

    /**
     * @return the oldBestGenome
     */
    public Genome getOldBestGenome()
    {
        return this.oldBestGenome;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperationList getOperators()
    {
        return this.operators;
    }

    /**
     * @return The population.
     */
    @Override
    public Population getPopulation()
    {
        return this.blueprint_population;
    }

    public Population getNeuronPopulation()
    {
        return this.neuron_population;
    }

    /**
     * @return the randomNumberFactory
     */
    public RandomFactory getRandomNumberFactory()
    {
        return this.randomNumberFactory;
    }

    /**
     * @return the rules
     */
    @Override
    public RuleHolder getRules()
    {
        return this.rules;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AdjustScore> getScoreAdjusters()
    {
        return this.adjusters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CalculateScore getScoreFunction()
    {
        return this.scoreFunction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SelectionOperator getSelection()
    {
        return this.selection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenomeComparator getSelectionComparator()
    {
        return this.selectionComparator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getShouldIgnoreExceptions()
    {
        return this.ignoreExceptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Speciation getSpeciation()
    {
        return this.speciation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getThreadCount()
    {
        return this.threadCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidationMode()
    {
        return this.validationMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void iteration()
    {
        if (this.actualThreadCount == -1)
        {
            preIteration();
        }

        if (getPopulation().getSpecies().size() == 0)
        {
            throw new EncogError("Population is empty, there are no species.");
        }

        this.threadList.clear();

        this.iteration++;

        System.out.println("Starting neuron evolution");
        //clear neurons
        this.newNeurons.clear();

        System.out.println("Clearing fitness");
        //clear fitness of neuron
        for (final Genome g : getNeuronPopulation().getSpecies().get(0).getMembers())
        {
            NeuronGenome neuron = (NeuronGenome) g;
            neuron.clearParticipation();
            neuron.setScore(0);
            neuron.setAdjustedScore(0);
        }
        System.out.println("Assigning fitness");
        //assign fitness to neurons
        for (final Genome g : getPopulation().getSpecies().get(0).getMembers())
        {
            BlueprintGenome blueprint = (BlueprintGenome) g;

            //add the score of network to neurons
            for (int i = 0; i < blueprint.getBlueprint().length; i++)
            {
                blueprint.getBlueprint()[i].addScore(blueprint.getScore());
                blueprint.getBlueprint()[i].incrementParticipation();
            }
        }

        System.out.println("Finalizing fitness");
        //finalize fitness of neuron by averaging its fitness per network
        for (final Genome g : getNeuronPopulation().getSpecies().get(0).getMembers())
        {
            NeuronGenome neuron = (NeuronGenome) g;
            neuron.finalizeScore();
        }

        //neuron iteration
        for (final Species species : getNeuronPopulation().getSpecies())
        {
            int numToSpawn = species.getOffspringCount();
            // Add elite genomes directly
            if (species.getMembers().size() > 5)
            {
                final int idealEliteCount = (int) (species.getMembers().size() * getEliteRate());
                final int eliteCount = Math.min(numToSpawn, idealEliteCount);
                for (int i = 0; i < eliteCount; i++)
                {
                    final Genome eliteGenome = species.getMembers().get(i);
                    numToSpawn--;
                    if (!addChildNeuron(eliteGenome))
                    {
                        break;
                    }

                }
            }

            if (numToSpawn % 2 == 0)
            {
                numToSpawn = numToSpawn/2;
            }
            else numToSpawn = (numToSpawn/2) +1;
            // now add one task for each offspring that each species is allowed
            while (numToSpawn-- >= 0)
            {

                final NeuronWorker worker = new NeuronWorker(this, species);
                this.threadList.add(worker);
            }

        }

        // run all threads and wait for them to finish
        try
        {
            this.taskExecutor.invokeAll(this.threadList);
        }
        catch (final InterruptedException e)
        {
            EncogLogging.log(e);
        }

        // handle any errors that might have happened in the threads
        if (this.reportedError != null && !getShouldIgnoreExceptions())
        {
            throw new GeneticError(this.reportedError);
        }

        this.speciation.performSpeciation(this.newNeurons, neuron_population);

        // purge invalid genomes
        this.neuron_population.purgeInvalidGenomes();

        System.out.println("Starting blueprint evolution");

        // Clear new population to just best genome.
        this.newBlueprints.clear();
        this.newBlueprints.add(this.bestGenome);
        this.oldBestGenome = this.bestGenome;

        // execute species in parallel
        this.threadList.clear();

        //blueprint iteration
        for (final Species species : getPopulation().getSpecies())
        {
            int numToSpawn = species.getOffspringCount();

            // Add elite genomes directly
            if (species.getMembers().size() > 5)
            {
                final int idealEliteCount = (int) (species.getMembers().size() * getEliteRate());
                final int eliteCount = Math.min(numToSpawn, idealEliteCount);
                for (int i = 0; i < eliteCount; i++)
                {
                    final Genome eliteGenome = species.getMembers().get(i);
                    if (getOldBestGenome() != eliteGenome)
                    {
                        numToSpawn--;
                        if (!addChild(eliteGenome))
                        {
                            break;
                        }
                    }
                }
            }

            if (numToSpawn % 2 == 0)
            {
                numToSpawn = numToSpawn/2;
            }
            else numToSpawn = (numToSpawn/2) +1;
            // now add one task for each offspring that each species is allowed
            while (numToSpawn-- >= 0)
            {
                final BlueprintWorker worker = new BlueprintWorker(this, species);
                this.threadList.add(worker);
            }
        }

        // run all threads and wait for them to finish
        try
        {
            this.taskExecutor.invokeAll(this.threadList);
        }
        catch (final InterruptedException e)
        {
            EncogLogging.log(e);
        }

        // handle any errors that might have happened in the threads
        if (this.reportedError != null && !getShouldIgnoreExceptions())
        {
            throw new GeneticError(this.reportedError);
        }

        // validate, if requested
        if (isValidationMode())
        {
            if (this.oldBestGenome != null
                    && !this.newBlueprints.contains(this.oldBestGenome))
            {
                throw new EncogError(
                        "The top genome died, this should never happen!!");
            }

            if (this.bestGenome != null
                    && this.oldBestGenome != null
                    && getBestComparator().isBetterThan(this.oldBestGenome,
                    this.bestGenome))
            {
                throw new EncogError(
                        "The best genome's score got worse, this should never happen!! Went from "
                                + this.oldBestGenome.getScore() + " to "
                                + this.bestGenome.getScore());
            }
        }

        this.speciation.performSpeciation(this.newBlueprints, blueprint_population);

        // purge invalid genomes
        this.blueprint_population.purgeInvalidGenomes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performShutdownTask()
    {
        finishTraining();
    }

    /**
     * Called before the first iteration. Determine the number of threads to
     * use.
     */
    private void preIteration()
    {

        this.speciation.init(this);

        // find out how many threads to use
        if (this.threadCount == 0)
        {
            this.actualThreadCount = Runtime.getRuntime().availableProcessors();
        } else
        {
            this.actualThreadCount = this.threadCount;
        }

        // score the initial population
        final ParallelScore pscore = new ParallelScore(getPopulation(),
                getCODEC(), new ArrayList<AdjustScore>(), getScoreFunction(),
                this.actualThreadCount);
        pscore.setThreadCount(this.actualThreadCount);
        pscore.process();
        this.actualThreadCount = pscore.getThreadCount();

        // start up the thread pool
        if (this.actualThreadCount == 1)
        {
            this.taskExecutor = Executors.newSingleThreadScheduledExecutor();
        } else
        {
            this.taskExecutor = Executors
                    .newFixedThreadPool(this.actualThreadCount);
        }

        // register for shutdown
        Encog.getInstance().addShutdownTask(this);

        // just pick the first genome with a valid score as best, it will be
        // updated later.
        // also most populations are sorted this way after training finishes
        // (for reload)
        // if there is an empty population, the constructor would have blow
        final List<Genome> list = getPopulation().flatten();

        int idx = 0;
        do
        {
            this.bestGenome = list.get(idx++);
        } while (idx < list.size()
                && (Double.isInfinite(this.bestGenome.getScore()) || Double
                .isNaN(this.bestGenome.getScore())));

        getPopulation().setBestGenome(this.bestGenome);

        // speciate
        final List<Genome> genomes = getPopulation().flatten();
        this.speciation.performSpeciation(genomes, blueprint_population);

        // purge invalid genomes
        this.blueprint_population.purgeInvalidGenomes();
    }

    /**
     * Called by a thread to report an error.
     *
     * @param t The error reported.
     */
    public void reportError(final Throwable t)
    {
        synchronized (this)
        {
            if (this.reportedError == null)
            {
                this.reportedError = t;
            }
        }
    }

    /**
     * Set the comparator.
     *
     * @param theComparator The comparator.
     */
    @Override
    public void setBestComparator(final GenomeComparator theComparator)
    {
        this.bestComparator = theComparator;
    }

    /**
     * @param champMutation the champMutation to set
     */
    public void setChampMutation(final EvolutionaryOperator champMutation)
    {
        this.champMutation = champMutation;
    }

    /**
     * Set the CODEC to use.
     *
     * @param theCodec The CODEC to use.
     */
    public void setCODEC(final GeneticCODEC theCodec)
    {
        this.codec = theCodec;
    }

    /**
     * @param eliteRate the eliteRate to set
     */
    public void setEliteRate(final double eliteRate)
    {
        this.eliteRate = eliteRate;
    }

    /**
     * Set the current iteration number.
     *
     * @param iteration The iteration number.
     */
    public void setIteration(final int iteration)
    {
        this.iteration = iteration;
    }

    /**
     * @param maxTries the maxTries to set
     */
    public void setMaxTries(final int maxTries)
    {
        this.maxTries = maxTries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPopulation(final Population thePopulation)
    {
        this.blueprint_population = thePopulation;
    }

    /**
     * @param randomNumberFactory the randomNumberFactory to set
     */
    public void setRandomNumberFactory(final RandomFactory randomNumberFactory)
    {
        this.randomNumberFactory = randomNumberFactory;
    }

    /**
     * @param rules the rules to set
     */
    @Override
    public void setRules(final RuleHolder rules)
    {
        this.rules = rules;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelection(final SelectionOperator selection)
    {
        this.selection = selection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectionComparator(final GenomeComparator theComparator)
    {
        this.selectionComparator = theComparator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setShouldIgnoreExceptions(final boolean b)
    {
        this.ignoreExceptions = b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpeciation(final Speciation speciation)
    {
        this.speciation = (SANESpeciation)speciation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setThreadCount(final int numThreads)
    {
        this.threadCount = numThreads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValidationMode(final boolean validationMode)
    {
        this.validationMode = validationMode;
    }

    /**
     * @return the maxOperationErrors
     */
    public int getMaxOperationErrors()
    {
        return maxOperationErrors;
    }

    /**
     * @param maxOperationErrors the maxOperationErrors to set
     */
    public void setMaxOperationErrors(int maxOperationErrors)
    {
        this.maxOperationErrors = maxOperationErrors;
    }

    //returns blueprint genetic operators
    public EvolutionaryOperator getNeuronCrossover()
    {
        return this.neuron_crossover;
    }
    public EvolutionaryOperator getNeuronMutate()
    {
        return this.neuron_mutate;
    }
    public EvolutionaryOperator getBlueprintCrossover()
    {
        return this.blueprint_crossover;
    }
    public BlueprintMutateSwitchToRandom getBlueprintMutateRandom()
    {
        return this.blueprint_mutate_random;
    }
    public EvolutionaryOperator getBlueprintMutateOffspring()
    {
        return this.blueprint_mutate_offspring;
    }

    public void setMaxParents(int max)
    {
        max_parents = max;
    }
    public void setMaxChildren(int max)
    {
        max_children = max;
    }
    public int getMaxParents(){return max_parents;}
    public int getMaxChildren(){return max_children;}
}

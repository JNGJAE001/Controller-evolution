package za.redbridge.controller.SANE;


import org.encog.EncogError;
import org.encog.ml.ea.exception.EARuntimeError;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.EvolutionaryOperator;
import org.encog.ml.ea.species.Species;
import org.encog.ml.ea.train.basic.BasicEA;

import java.util.Random;
import java.util.concurrent.Callable;

/**
 * A worker thread for an Evolutionary Algorithm.
 */
public class NeuronWorker implements Callable<Object>
{

    /**
     * The species being processed.
     */
    private final Species species;

    /**
     * The parent genomes.
     */
    private final Genome[] parents;

    /**
     * The children genomes.
     */
    private final Genome[] children;

    /**
     * Random number generator.
     */
    private final Random rnd;

    /**
     * The parent object.
     */
    private final BasicSANE train;

    /**
     * Construct the EA worker.
     *
     * @param theTrain   The trainer.
     * @param theSpecies The species.
     */
    public NeuronWorker(final BasicSANE theTrain, final Species theSpecies)
    {
        this.train = theTrain;
        this.species = theSpecies;
        this.rnd = this.train.getRandomNumberFactory().factor();

        this.parents = new Genome[this.train.getMaxParents()];
        this.children = new Genome[this.train.getMaxChildren()];
    }

    /**
     * Choose a parent.
     *
     * @return The chosen parent.
     */
    private Genome chooseParent()
    {
        final int idx = this.train.getSelection().performSelection(this.rnd,
                this.species);
        return this.species.getMembers().get(idx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object call()
    {
        boolean success = false;
        int tries = this.train.getMaxOperationErrors();
        do
        {
            try
            {
                // choose an evolutionary operation (i.e. crossover or a type of
                // mutation) to use
                System.out.println("Neuron mating started");
                final EvolutionaryOperator neuron_crossover = this.train.getNeuronCrossover();
                final EvolutionaryOperator neuron_mutate = this.train.getNeuronMutate();

                this.children[0] = null;
                this.children[1] = null;
                // prepare for either sexual or asexual reproduction either way,
                // we
                // need at least
                // one parent, which is the first parent.
                //
                // Chose the first parent, there must be at least one genome in
                // this
                // species
                this.parents[0] = chooseParent();

                // if the number of individuals in this species is only
                // one then we can only clone and perhaps mutate, otherwise use
                // the crossover probability to determine if we are to use
                // sexual reproduction.


                int numAttempts = 5;

                this.parents[1] = chooseParent();
                while (this.parents[0] == this.parents[1]
                        && numAttempts-- > 0)
                {
                    //System.out.println("attempts "+numAttempts);
                    this.parents[1] = chooseParent();
                }

                // success, perform crossover
                neuron_crossover.performOperation(this.rnd, this.parents, 0, this.children, 0);

                //perform mutation on the offspring
                for (int i = 0; i < children.length; i++)
                {
                    neuron_mutate.performOperation(this.rnd, this.parents, 0, this.children, i);
                }


                System.out.println("Neuron mating finished");

                // process the new child
                for (Genome child : this.children)
                {
                    if (child != null)
                    {
                        child.setPopulation(this.parents[0].getPopulation());
                        if (this.train.getRules().isValid(child))
                        {
                            child.setBirthGeneration(this.train.getIteration());

                            if (!this.train.addChildNeuron(child))
                            {
                                return null;
                            }
                            success = true;
                        }
                    }
                }
            }
            catch (EARuntimeError e)
            {
                tries--;
                if (tries < 0)
                {
                    throw new EncogError(
                            "Could not perform a successful genetic operaton after "
                                    + this.train.getMaxOperationErrors()
                                    + " tries.");
                }
            }
            catch (final Throwable t)
            {
                if (!this.train.getShouldIgnoreExceptions())
                {
                    this.train.reportError(t);
                }
            }
        } while (!success);
        return null;
    }
}

package za.redbridge.controller.NEATM;

import org.encog.ml.ea.species.BasicSpecies;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATInnovationList;

import java.util.Random;

import za.redbridge.controller.NEATM.sensor.SensorType;
import za.redbridge.controller.NEAT.NEATPopulation;

/**
 * Created by jamie on 2014/09/08.
 */
public class NEATMPopulation extends NEATPopulation {

    private static final long serialVersionUID = -6647644833955733411L;

    /**
     * An empty constructor for serialization.
     */
    public NEATMPopulation() {

    }

    /**
     * Construct a starting NEAT population. This does not generate the initial
     * random population of genomes.
     *
     * @param outputCount
     *            The output neuron count.
     * @param populationSize
     *            The population size.
     */
    public NEATMPopulation(int outputCount, int populationSize) {
        super(SensorType.values().length, outputCount, populationSize);
    }

    @Override
    public void reset() {
        setCODEC(new NEATMCODEC());
        setGenomeFactory(new FactorNEATMGenome());

        // create the new genomes
        getSpecies().clear();

        // reset counters
        getGeneIDGenerate().setCurrentID(1);
        getInnovationIDGenerate().setCurrentID(1);

        final Random rnd = getRandomNumberFactory().factor();

        // create one default species
        final BasicSpecies defaultSpecies = new BasicSpecies();
        defaultSpecies.setPopulation(this);

        // create the initial population
        for (int i = 0; i < getPopulationSize(); i++) {
            final NEATGenome genome = getGenomeFactory().factor(rnd, this,
                    getInputCount(), getOutputCount(), getInitialConnectionDensity());
            defaultSpecies.add(genome);
        }
        defaultSpecies.setLeader(defaultSpecies.getMembers().get(0));
        getSpecies().add(defaultSpecies);

        // create initial innovations
        setInnovations(new NEATInnovationList(this));
    }

}

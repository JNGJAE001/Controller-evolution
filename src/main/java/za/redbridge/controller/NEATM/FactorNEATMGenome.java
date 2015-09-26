package za.redbridge.controller.NEATM;

import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.NEATGenomeFactory;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

import za.redbridge.controller.NEATM.training.NEATMGenome;

/**
 * Created by jamie on 2014/09/08.
 */
public class FactorNEATMGenome implements NEATGenomeFactory, Serializable {

    private static final long serialVersionUID = 602605192989600971L;

    @Override
    public NEATGenome factor(List<NEATNeuronGene> neurons, List<NEATLinkGene> links, int inputCount,
            int outputCount) {
        return new NEATMGenome(neurons, links, inputCount, outputCount);
    }

    @Override
    public NEATGenome factor(Random rnd, NEATPopulation pop, int inputCount, int outputCount,
            double connectionDensity) {
        return new NEATMGenome(rnd, pop, outputCount, connectionDensity);
    }

    @Override
    public Genome factor() {
        return new NEATMGenome();
    }

    @Override
    public Genome factor(Genome other) {
        return new NEATMGenome((NEATMGenome) other);
    }
}

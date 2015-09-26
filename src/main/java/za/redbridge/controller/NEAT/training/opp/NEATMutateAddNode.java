package za.redbridge.controller.NEAT.training.opp;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATInnovation;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.encog.neural.neat.training.opp.NEATMutation;

import java.util.Random;

/**
 * NEATMutateAddNode implementation that uses the shifted steepened shifted sigmoid activation
 * function for the new nodes.
 *
 * Created by jamie on 2014/10/14.
 */
public class NEATMutateAddNode extends NEATMutation {

    @Override
    public void performOperation(Random rnd, Genome[] parents, int parentIndex, Genome[] offspring,
            int offspringIndex) {
        final NEATGenome target = obtainGenome(parents, parentIndex, offspring, offspringIndex);
        int countTrysToFindOldLink = getOwner().getMaxTries();

        final NEATPopulation pop = ((NEATPopulation) target.getPopulation());

        // the link to split
        NEATLinkGene splitLink = null;

        final int sizeBias = ((NEATGenome) parents[0]).getInputCount()
                + ((NEATGenome) parents[0]).getOutputCount() + 10;

        // if there are not at least
        int upperLimit;
        if (target.getLinksChromosome().size() < sizeBias) {
            upperLimit = target.getNumGenes() - 1 - (int) Math.sqrt(target.getNumGenes());
        } else {
            upperLimit = target.getNumGenes() - 1;
        }

        while (countTrysToFindOldLink-- > 0) {
            // choose a link, use the square root to prefer the older links
            final int i = (int) (rnd.nextDouble() * upperLimit);
            final NEATLinkGene link = target.getLinksChromosome().get(i);

            // get the from neuron
            final long fromNeuron = link.getFromNeuronID();

            if (link.isEnabled()) {
                int genePosition = getElementPos(target, fromNeuron);
                NEATNeuronGene gene = target.getNeuronsChromosome().get(genePosition);
                if (gene.getNeuronType() != NEATNeuronType.Bias) {
                    splitLink = link;
                    break;
                }
            }
        }

        if (splitLink == null) {
            return;
        }

        splitLink.setEnabled(false);

        final long from = splitLink.getFromNeuronID();
        final long to = splitLink.getToNeuronID();

        final NEATInnovation innovation = ((NEATPopulation) getOwner().getPopulation())
                .getInnovations().findInnovationSplit(from, to);

        // add the splitting neuron
        final ActivationFunction af = pop.getActivationFunctions().pickFirst();

        target.getNeuronsChromosome().add(new NEATNeuronGene(NEATNeuronType.Hidden, af,
                innovation.getNeuronID(), innovation.getInnovationID()));

        // add the other two sides of the link
        createLink(target, from, innovation.getNeuronID(),
                splitLink.getWeight());
        createLink(target, innovation.getNeuronID(), to, pop.getWeightRange());

        target.sortGenes();
    }

}

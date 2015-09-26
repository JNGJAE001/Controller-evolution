package za.redbridge.controller.NEATM.training.opp;

import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.encog.neural.neat.training.opp.NEATMutateRemoveLink;

import za.redbridge.controller.NEATM.training.NEATMGenome;
import za.redbridge.controller.NEATM.training.NEATMNeuronGene;

/**
 * Remove link mutation that can also remove input nodes.
 *
 * Created by jamie on 2014/10/20.
 */
public class NEATMMutateRemoveLink extends NEATMutateRemoveLink {

    public NEATMMutateRemoveLink() {
    }

    @Override
    public boolean isNeuronNeeded(NEATGenome target, long neuronID) {
        for (NEATNeuronGene neuron : target.getNeuronsChromosome()) {
            if (neuron.getId() == neuronID) {
                if (neuron.getNeuronType() == NEATNeuronType.Bias
                        || neuron.getNeuronType() == NEATNeuronType.Output) {
                    return true;
                }
                break;
            }
        }

        // Now check to see if the neuron is used in any links
        for (final NEATLinkGene link : target.getLinksChromosome()) {
            if (link.getFromNeuronID() == neuronID) {
                return true;
            }
            if (link.getToNeuronID() == neuronID) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void removeNeuron(NEATGenome target, long neuronID) {
        for (NEATNeuronGene neuron : target.getNeuronsChromosome()) {
            if (neuron.getId() == neuronID) {
                if (neuron.getNeuronType() == NEATNeuronType.Input) {
                    ((NEATMGenome) target).removeInputNeuron((NEATMNeuronGene) neuron);
                } else {
                    target.getNeuronsChromosome().remove(neuron);
                }
                break;
            }
        }
    }
}

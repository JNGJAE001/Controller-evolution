package za.redbridge.controller.NEATM.training.opp;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.training.NEATInnovation;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.encog.neural.neat.training.opp.NEATMutation;
import org.encog.neural.neat.training.opp.links.MutateLinkWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import za.redbridge.controller.NEATM.NEATMPopulation;
import za.redbridge.controller.NEATM.sensor.SensorConfiguration;
import za.redbridge.controller.NEATM.sensor.SensorType;
import za.redbridge.controller.NEATM.sensor.parameter.SensorParameterSet;
import za.redbridge.controller.NEATM.training.NEATMGenome;
import za.redbridge.controller.NEATM.training.NEATMNeuronGene;

/**
 * Mutation to add a single new input node to the network. This mutation adds a new sensor in a
 * random position and with a random type. The sensor is connected to other (non-input) nodes in
 * the network randomly but with a certain connection density. The weights of the connections are
 * set to 0 and then mutated using the provided weight mutation operator.
 *
 * Created by jamie on 2014/09/16.
 */
public class NEATMMutateAddSensor extends NEATMutation {

    /**
     * Do not add sensors to genomes that have this number of sensors or more.
     */
    public static final int MAX_SENSORS = 10;

    private final SensorType sensorType;
    private final double connectionDensity;
    private final MutateLinkWeight weightMutator;

    /**
     * @param sensorType the sensor type to add
     * @param connectionDensity the connection density to use when adding links to the new sensor
     * @param weightMutator the mutator that will initialize the links of the new sensor
     */
    public NEATMMutateAddSensor(SensorType sensorType, double connectionDensity,
                                MutateLinkWeight weightMutator) {
        this.sensorType = sensorType;
        this.connectionDensity = connectionDensity;
        this.weightMutator = weightMutator;
    }

    @Override
    public void performOperation(Random rnd, Genome[] parents, int parentIndex, Genome[] offspring,
            int offspringIndex) {
        final NEATMGenome target =
                (NEATMGenome) obtainGenome(parents, parentIndex, offspring, offspringIndex);

        if (target.getInputCount() >= MAX_SENSORS) {
            // Don't add sensors to genomes with a lot of sensors
            return;
        }

        final NEATMPopulation pop = ((NEATMPopulation) target.getPopulation());

        final NEATInnovation innovation = pop.getInnovations().findInnovation(pop.assignGeneID());

        // NOTE: This will not work for HyperNEAT
        final ActivationFunction af = ((NEATMPopulation) getOwner().getPopulation())
                .getActivationFunctions().pickFirst();

        // Create the input node with random sensor configuration
        NEATMNeuronGene inputNeuron = new NEATMNeuronGene(NEATNeuronType.Input, af,
                innovation.getNeuronID(), innovation.getInnovationID());

        SensorParameterSet parameterSet = null;
        if (sensorType.isConfigurable()) {
            // Find an existing parameter set from another sensor of the same type
            List<NEATMNeuronGene> inputNeurons = target.getInputNeuronsChromosome();
            SensorParameterSet existingParameters = null;
            for (NEATMNeuronGene neuron : inputNeurons) {
                SensorConfiguration configuration = neuron.getSensorConfiguration();
                if (configuration.getSensorType() == sensorType) {
                    existingParameters = configuration.getSensorParameterSet();
                    break;
                }
            }

            parameterSet =
                    sensorType.getDefaultSpecSet().createParameterSet(rnd, existingParameters);
        }

        inputNeuron.setSensorConfiguration(new SensorConfiguration(sensorType, parameterSet));
        target.addInputNeuron(inputNeuron);

        // Get the list of all neurons that aren't input nodes or the bias node
        List<NEATNeuronGene> neurons = new ArrayList<>(target.getNeuronsChromosome());
        neurons.removeAll(target.getInputNeuronsChromosome());
        neurons.remove(target.getBiasGene());

        // Iterate through list of those neurons, pick neurons to link to randomly
        boolean haveLinkToInput = false;
        long fromID = inputNeuron.getId();
        List<NEATLinkGene> linkGenes = target.getLinksChromosome();
        for (NEATNeuronGene neuron : neurons) {
            // Connect with same connection density as the network was initialized with
            if (rnd.nextDouble() < connectionDensity) {
                long toID = neuron.getId();
                createLink(target, fromID, toID, 0.0);

                // Get last added link and mutate weights
                NEATLinkGene link = linkGenes.get(linkGenes.size() - 1);
                weightMutator.mutateWeight(rnd, link, pop.getWeightRange());

                haveLinkToInput = true;
            }
        }

        // Ensure there is at least one link
        if (!haveLinkToInput) {
            // Pick a random neuron and link it up
            int index = (int) (rnd.nextDouble() * neurons.size());
            long toID = neurons.get(index).getId();
            createLink(target, fromID, toID, 0.0);
            NEATLinkGene link = linkGenes.get(linkGenes.size() - 1);
            weightMutator.mutateWeight(rnd, link, pop.getWeightRange());
        }

    }

}

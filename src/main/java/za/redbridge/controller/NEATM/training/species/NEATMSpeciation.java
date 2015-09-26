package za.redbridge.controller.NEATM.training.species;

import org.encog.ml.ea.genome.Genome;

import java.util.List;

import za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType;
import za.redbridge.controller.NEATM.training.NEATMGenome;
import za.redbridge.controller.NEATM.training.NEATMNeuronGene;
import za.redbridge.controller.NEAT.training.species.NEATSpeciation;

/**
 * Adds an additional term to the NEAT speciation that accounts for sensor parameters.
 *
 * Created by jamie on 2014/10/21.
 */
public class NEATMSpeciation extends NEATSpeciation {

    private static final long serialVersionUID = -505824828539787086L;

    @Override
    public double getCompatibilityScore(Genome gen1, Genome gen2) {
        double score = super.getCompatibilityScore(gen1, gen2);

        int numMatched = 0;

        ParameterType[] parameterTypes = ParameterType.values();
        final int numParameters = parameterTypes.length;
        double[] differences = new double[numParameters];

        NEATMGenome genome1 = (NEATMGenome) gen1;
        NEATMGenome genome2 = (NEATMGenome) gen2;

        List<NEATMNeuronGene> genome1Inputs = genome1.getInputNeuronsChromosome();
        List<NEATMNeuronGene> genome2Inputs = genome2.getInputNeuronsChromosome();

        int genome1InputCount = genome1Inputs.size();
        int genome2InputCount = genome2Inputs.size();

        int g1 = 0;
        int g2 = 0;

        while (g1 < genome1InputCount && g2 < genome2InputCount) {
            NEATMNeuronGene genome1Input = genome1Inputs.get(g1);
            NEATMNeuronGene genome2Input = genome2Inputs.get(g2);

            // get neuron id for each gene at this point
            long id1 = genome1Input.getId();
            long id2 = genome2Input.getId();

            if (id1 == id2) {
                // Get the difference of every parameter
                if (genome1Input.getSensorConfiguration().getSensorType().isConfigurable()) {
                    for (int i = 0; i < numParameters; i++) {
                        ParameterType parameterType = parameterTypes[i];
                        float genome1Parameter = genome1Input.getSensorConfiguration()
                                .getSensorParameterSet().getParameter(parameterType)
                                .getNormalizedValue();

                        float genome2Parameter = genome2Input.getSensorConfiguration()
                                .getSensorParameterSet().getParameter(parameterType)
                                .getNormalizedValue();

                        double difference = Math.abs(genome2Parameter - genome1Parameter);
                        differences[i] += difference;
                    }
                    numMatched++;
                }

                g1++;
                g2++;
            } else if (id1 < id2) {
                g1++;
            } else { // if (id1 > id2)
                g2++;
            }
        }

        // Calculate the total score for the parameters
        double sensorScore = 0;
        for (int i = 0; i < numParameters; i++) {
            sensorScore += parameterTypes[i].getSpeciationWeighting()
                    * (differences[i] / numMatched);
        }

        return score + sensorScore;
    }

}

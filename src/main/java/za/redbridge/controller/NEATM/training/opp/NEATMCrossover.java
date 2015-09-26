package za.redbridge.controller.NEATM.training.opp;

import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATGenomeFactory;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.encog.neural.neat.training.opp.NEATCrossover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import za.redbridge.controller.NEATM.training.NEATMGenome;

/**
 * Created by jamie on 2014/09/22.
 */
public class NEATMCrossover extends NEATCrossover {

    private EvolutionaryAlgorithm owner;

    @Override
    public void init(EvolutionaryAlgorithm owner) {
        super.init(owner);
        this.owner = owner;
    }

    /**
     * Choose a parent to favor.
     * Copied from {@link NEATCrossover} because that class was not designed for extensibility.
     *
     * @param mom
     *            The mother.
     * @param dad
     *            The father.
     * @return The parent to favor.
     */
    private NEATMGenome favorParent(Random rnd, NEATMGenome mom, NEATMGenome dad) {

        // first determine who is more fit, the mother or the father?
        // see if mom and dad are the same fitness
        if (mom.getScore() == dad.getScore()) {
            // are mom and dad the same fitness
            if (mom.getNumGenes() == dad.getNumGenes()) {
                // if mom and dad are the same fitness and have the same number
                // of genes,
                // then randomly pick mom or dad as the most fit.
                if (rnd.nextDouble() < 0.5) {
                    return mom;
                } else {
                    return dad;
                }
            }
            // mom and dad are the same fitness, but different number of genes
            // favor the parent with fewer genes
            else {
                if (mom.getNumGenes() < dad.getNumGenes()) {
                    return mom;
                } else {
                    return dad;
                }
            }
        } else {
            // mom and dad have different scores, so choose the better score.
            // important to note, better score COULD BE the larger or smaller
            // score.
            if (this.owner.getSelectionComparator().compare(mom, dad) < 0) {
                return mom;
            }

            else {
                return dad;
            }
        }

    }

    @Override
    public void performOperation(Random rnd, Genome[] parents, int parentIndex, Genome[] offspring,
            int offspringIndex) {

        final NEATMGenome mom = (NEATMGenome) parents[parentIndex];
        final NEATMGenome dad = (NEATMGenome) parents[parentIndex + 1];

        final NEATMGenome best = favorParent(rnd, mom, dad);
        final NEATMGenome notBest = (best != mom) ? mom : dad;

        final List<NEATLinkGene> selectedLinks = new ArrayList<>();
        final List<NEATNeuronGene> selectedNeurons = new ArrayList<>();

        // add in the input, output and bias, they should always be here
        selectedNeurons.add(best.getBiasGene());
        selectedNeurons.addAll(best.getInputNeuronsChromosome());
        selectedNeurons.addAll(best.getOutputNeuronsChromosome());

        int momIndex = 0; // current gene index from mom
        int dadIndex = 0; // current gene index from dad
        NEATLinkGene selectedGene = null;
        while ((momIndex < mom.getNumGenes()) || (dadIndex < dad.getNumGenes())) {
            NEATLinkGene momGene = null; // the mom gene object
            NEATLinkGene dadGene = null; // the dad gene object
            long momInnovation = -1;
            long dadInnovation = -1;

            // grab the actual objects from mom and dad for the specified
            // indexes
            // if there are none, then null
            if (momIndex < mom.getNumGenes()) {
                momGene = mom.getLinksChromosome().get(momIndex);
                momInnovation = momGene.getInnovationId();
            }

            if (dadIndex < dad.getNumGenes()) {
                dadGene = dad.getLinksChromosome().get(dadIndex);
                dadInnovation = dadGene.getInnovationId();
            }

            // now select a gene for mom or dad. This gene is for the baby
            if ((momGene == null) && (dadGene != null)) {
                if (best == dad) {
                    selectedGene = dadGene;
                }
                dadIndex++;
            } else if ((dadGene == null) && (momGene != null)) {
                if (best == mom) {
                    selectedGene = momGene;
                }
                momIndex++;
            } else if (momInnovation < dadInnovation) {
                if (best == mom) {
                    selectedGene = momGene;
                }
                momIndex++;
            } else if (dadInnovation < momInnovation) {
                if (best == dad) {
                    selectedGene = dadGene;
                }
                dadIndex++;
            } else if (dadInnovation == momInnovation) {
                if (rnd.nextDouble() < 0.5) {
                    selectedGene = momGene;
                }

                else {
                    selectedGene = dadGene;
                }
                momIndex++;
                dadIndex++;
            }

            if (selectedGene != null) {
                if (selectedLinks.size() == 0) {
                    selectedLinks.add(selectedGene);
                } else {
                    if (selectedLinks.get(selectedLinks.size() - 1)
                            .getInnovationId() != selectedGene
                            .getInnovationId()) {
                        selectedLinks.add(selectedGene);
                    }
                }

                // Check if we already have the nodes referred to in
                // SelectedGene.
                // If not, they need to be added.
                addNeuronID(selectedGene.getFromNeuronID(), selectedNeurons,
                        best, notBest);
                addNeuronID(selectedGene.getToNeuronID(), selectedNeurons,
                        best, notBest);
            }

        }

        // now create the required nodes. First sort them into order
        Collections.sort(selectedNeurons);

        // finally, create the genome
        final NEATGenomeFactory factory = (NEATGenomeFactory) this.owner
                .getPopulation().getGenomeFactory();
        final NEATGenome babyGenome = factory.factor(selectedNeurons,
                selectedLinks, mom.getInputCount(), mom.getOutputCount());
        babyGenome.setBirthGeneration(this.owner.getIteration());
        babyGenome.setPopulation(this.owner.getPopulation());
        babyGenome.sortGenes();

        offspring[offspringIndex] = babyGenome;
    }

}

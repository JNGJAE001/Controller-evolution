package za.redbridge.controller.NEAT.training.species;

import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.species.ThresholdSpeciation;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;

import java.util.List;

/**
 * This is similar to {@link OriginalNEATSpeciation} except it normalizes the weights.
 * Created by jamie on 2014/10/21.
 */
public class NEATSpeciation extends ThresholdSpeciation {

    private static final long serialVersionUID = 6638788214251753854L;

    /**
     * The adjustment factor for disjoint genes.
     */
    private double constDisjoint = 1;

    /**
     * The adjustment factor for excess genes.
     */
    private double constExcess = 1;

    /**
     * The adjustment factor for matched genes.
     */
    private double constMatched = 0.4;

    public NEATSpeciation() {
        // Default the threshold to 3.0 as seen in NEAT source code
        setCompatibilityThreshold(3.0);
    }

    @Override
    public double getCompatibilityScore(Genome genome1, Genome genome2) {
        int numDisjoint = 0;
        int numExcess = 0;
        int numMatched = 0;
        double weightDifference = 0;

        NEATGenome neatGenome1 = (NEATGenome) genome1;
        NEATGenome neatGenome2 = (NEATGenome) genome2;

        double weightRange = ((NEATPopulation) genome1.getPopulation()).getWeightRange();

        List<NEATLinkGene> genome1Links = neatGenome1.getLinksChromosome();
        List<NEATLinkGene> genome2Links = neatGenome2.getLinksChromosome();
        int genome1Size = genome1Links.size();
        int genome2Size = genome2Links.size();

        int g1 = 0;
        int g2 = 0;

        while (g1 < genome1Size && g2 < genome2Size) {
            NEATLinkGene link1 = genome1Links.get(g1);
            NEATLinkGene link2 = genome2Links.get(g2);
            long id1 = link1.getInnovationId();
            long id2 = link2.getInnovationId();

            if (id1 < id2) {
                numDisjoint++;
                g1++;
            } else if (id1 > id2) {
                numDisjoint++;
                g2++;
            } else { // id1 == id2
                double weight1 = link1.getWeight() / weightRange;
                double weight2 = link2.getWeight() / weightRange;
                weightDifference += Math.abs(weight2 - weight1);

                g1++;
                g2++;
                numMatched++;
            }
        }

        if (g1 < genome1Size) {
            numExcess = genome1Size - g1;
        } else if (g2 < genome2Size) {
            numExcess = genome2Size - g2;
        }

        return constExcess * numExcess
                + constDisjoint * numDisjoint
                + constMatched * (weightDifference / numMatched);
    }

    public double getConstDisjoint() {
        return constDisjoint;
    }

    public void setConstDisjoint(double constDisjoint) {
        this.constDisjoint = constDisjoint;
    }

    public double getConstExcess() {
        return constExcess;
    }

    public void setConstExcess(double constExcess) {
        this.constExcess = constExcess;
    }

    public double getConstMatched() {
        return constMatched;
    }

    public void setConstMatched(double constMatched) {
        this.constMatched = constMatched;
    }
}

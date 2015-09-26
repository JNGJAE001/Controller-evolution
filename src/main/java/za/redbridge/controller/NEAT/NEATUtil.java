package za.redbridge.controller.NEAT;

import org.encog.ml.CalculateScore;
import org.encog.ml.ea.opp.CompoundOperator;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.neat.NEATCODEC;
import org.encog.neural.neat.training.opp.NEATCrossover;
import org.encog.neural.neat.training.opp.NEATMutateAddLink;
import org.encog.neural.neat.training.opp.NEATMutateAddNode;
import org.encog.neural.neat.training.opp.NEATMutateRemoveLink;
import org.encog.neural.neat.training.opp.NEATMutateWeights;
import org.encog.neural.neat.training.opp.links.MutatePerturbLinkWeight;
import org.encog.neural.neat.training.opp.links.MutateResetLinkWeight;
import org.encog.neural.neat.training.opp.links.SelectFixed;
import org.encog.neural.neat.training.opp.links.SelectProportion;

import za.redbridge.controller.NEAT.training.species.NEATSpeciation;

/**
 * NEATUtil for regular NEAT network (no HyperNEAT) with link weight range of -1.0 to 1.0.
 *
 * Created by jamie on 2014/10/14.
 */
public final class NEATUtil {

    private NEATUtil() {
    }

    public static TrainEA constructNEATTrainer(CalculateScore calculateScore, int inputCount,
            int outputCount, int populationSize) {
        NEATPopulation pop = new NEATPopulation(inputCount, outputCount, populationSize);
        pop.reset();
        return constructNEATTrainer(pop, calculateScore);
    }

    /**
     * Construct a NEAT (or HyperNEAT trainer.
     * @param population The population.
     * @param calculateScore The score function.
     * @return The NEAT EA trainer.
     */
    public static TrainEA constructNEATTrainer(NEATPopulation population,
            CalculateScore calculateScore) {
        TrainEA result = new TrainEA(population, calculateScore);
        result.setSpeciation(new NEATSpeciation());

        result.setSelection(new TruncationSelection(result, 0.3));
        CompoundOperator weightMutation = new CompoundOperator();
        weightMutation.getComponents().add(0.1125, new NEATMutateWeights(new SelectFixed(1),
                        new MutatePerturbLinkWeight(0.004)));
        weightMutation.getComponents().add(0.1125, new NEATMutateWeights(new SelectFixed(2),
                        new MutatePerturbLinkWeight(0.004)));
        weightMutation.getComponents().add(0.1125, new NEATMutateWeights(new SelectFixed(3),
                        new MutatePerturbLinkWeight(0.004)));
        weightMutation.getComponents().add(0.1125, new NEATMutateWeights(new SelectProportion(0.02),
                        new MutatePerturbLinkWeight(0.004)));
        weightMutation.getComponents().add(0.1125, new NEATMutateWeights(new SelectFixed(1),
                        new MutatePerturbLinkWeight(0.2)));
        weightMutation.getComponents().add(0.1125, new NEATMutateWeights(new SelectFixed(2),
                        new MutatePerturbLinkWeight(0.2)));
        weightMutation.getComponents().add(0.1125, new NEATMutateWeights(new SelectFixed(3),
                        new MutatePerturbLinkWeight(0.2)));
        weightMutation.getComponents().add(0.1125, new NEATMutateWeights(new SelectProportion(0.02),
                        new MutatePerturbLinkWeight(0.2)));
        weightMutation.getComponents().add(0.03, new NEATMutateWeights(new SelectFixed(1),
                        new MutateResetLinkWeight()));
        weightMutation.getComponents().add(0.03, new NEATMutateWeights(new SelectFixed(2),
                        new MutateResetLinkWeight()));
        weightMutation.getComponents().add(0.03, new NEATMutateWeights(new SelectFixed(3),
                        new MutateResetLinkWeight()));
        weightMutation.getComponents().add(0.01, new NEATMutateWeights(new SelectProportion(0.02),
                        new MutateResetLinkWeight()));
        weightMutation.getComponents().finalizeStructure();

        result.setChampMutation(weightMutation);
        result.addOperation(0.5, new NEATCrossover());
        result.addOperation(0.493, weightMutation);
        result.addOperation(0.001, new NEATMutateAddNode());
        result.addOperation(0.005, new NEATMutateAddLink());
        result.addOperation(0.001, new NEATMutateRemoveLink());
        result.getOperators().finalizeStructure();

        result.setCODEC(new NEATCODEC());

        return result;
    }
}

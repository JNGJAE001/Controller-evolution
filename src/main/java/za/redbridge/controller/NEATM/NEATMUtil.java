package za.redbridge.controller.NEATM;

import org.encog.ml.CalculateScore;
import org.encog.ml.ea.opp.CompoundOperator;
import org.encog.ml.ea.opp.OperationList;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.opp.NEATMutateAddLink;
import org.encog.neural.neat.training.opp.NEATMutateWeights;
import org.encog.neural.neat.training.opp.links.MutatePerturbLinkWeight;
import org.encog.neural.neat.training.opp.links.MutateResetLinkWeight;
import org.encog.neural.neat.training.opp.links.SelectFixed;
import org.encog.neural.neat.training.opp.links.SelectProportion;

import za.redbridge.controller.NEATM.sensor.SensorType;
import za.redbridge.controller.NEATM.training.opp.NEATMCrossover;
import za.redbridge.controller.NEATM.training.opp.NEATMMutateAddNode;
import za.redbridge.controller.NEATM.training.opp.NEATMMutateAddSensor;
import za.redbridge.controller.NEATM.training.opp.NEATMMutateIndividualSensor;
import za.redbridge.controller.NEATM.training.opp.NEATMMutateRemoveLink;
import za.redbridge.controller.NEATM.training.opp.NEATMMutateSensorGroup;
import za.redbridge.controller.NEATM.training.opp.sensors.MutatePerturbSensorParameter;
import za.redbridge.controller.NEATM.training.opp.sensors.SelectSensorsFixed;
import za.redbridge.controller.NEATM.training.opp.sensors.SelectSensorsType;
import za.redbridge.controller.NEATM.training.species.NEATMSpeciation;


import static za.redbridge.controller.NEATM.sensor.SensorType.PROXIMITY;
import static za.redbridge.controller.NEATM.sensor.SensorType.ULTRASONIC;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.BEARING;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.FIELD_OF_VIEW;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.ORIENTATION;
import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.RANGE;

/**
 * Created by jamie on 2014/09/08.
 */
public final class NEATMUtil {

    public static TrainEA constructNEATTrainer(CalculateScore calculateScore, int outputCount,
            int populationSize) {
        final NEATMPopulation pop = new NEATMPopulation(outputCount, populationSize);
        pop.reset();
        return constructNEATTrainer(pop, calculateScore);
    }

    /**
     * Construct a NEAT trainer.
     * @param population The population.
     * @param calculateScore The score function.
     * @return The NEAT EA trainer.
     */
    public static TrainEA constructNEATTrainer(NEATPopulation population,
            CalculateScore calculateScore) {
        final TrainEA result = new TrainEA(population, calculateScore);

        // Speciation
        result.setSpeciation(new NEATMSpeciation());

        // Selection
        result.setSelection(new TruncationSelection(result, 0.3));

        // Create compound operator for weight mutation
        CompoundOperator weightMutation = new CompoundOperator();
        weightMutation.getComponents().add(0.1125,
                new NEATMutateWeights(new SelectFixed(1), new MutatePerturbLinkWeight(0.004)));
        weightMutation.getComponents().add(0.1125,
                new NEATMutateWeights(new SelectFixed(2), new MutatePerturbLinkWeight(0.004)));
        weightMutation.getComponents().add(0.1125,
                new NEATMutateWeights(new SelectFixed(3), new MutatePerturbLinkWeight(0.004)));
        weightMutation.getComponents().add(0.1125,
                new NEATMutateWeights(new SelectProportion(0.004),
                        new MutatePerturbLinkWeight(0.004)));
        weightMutation.getComponents().add(0.1125,
                new NEATMutateWeights(new SelectFixed(1), new MutatePerturbLinkWeight(0.2)));
        weightMutation.getComponents().add(0.1125,
                new NEATMutateWeights(new SelectFixed(2), new MutatePerturbLinkWeight(0.2)));
        weightMutation.getComponents().add(0.1125,
                new NEATMutateWeights(new SelectFixed(3), new MutatePerturbLinkWeight(0.2)));
        weightMutation.getComponents().add(0.1125,
                new NEATMutateWeights(new SelectProportion(0.02),
                        new MutatePerturbLinkWeight(0.2)));
        weightMutation.getComponents().add(0.03,
                new NEATMutateWeights(new SelectFixed(1), new MutateResetLinkWeight()));
        weightMutation.getComponents().add(0.03,
                new NEATMutateWeights(new SelectFixed(2), new MutateResetLinkWeight()));
        weightMutation.getComponents().add(0.03,
                new NEATMutateWeights(new SelectFixed(3), new MutateResetLinkWeight()));
        weightMutation.getComponents().add(0.01,
                new NEATMutateWeights(new SelectProportion(0.02), new MutateResetLinkWeight()));
        weightMutation.getComponents().finalizeStructure();

        // Champ operator limits mutation operator on best genome (seems unused for now)
        result.setChampMutation(weightMutation);

        // Add all the operators, probability should sum to 1
        result.addOperation(0.5, new NEATMCrossover());
        result.addOperation(0.5, weightMutation);
        result.addOperation(0.05, new NEATMMutateAddNode());
        result.addOperation(0.05, new NEATMutateAddLink());
        result.addOperation(0.005, new NEATMMutateRemoveLink());

        // Add the sensor position mutators
        CompoundOperator positionMutation = new CompoundOperator();
        OperationList positionMutationComponents = positionMutation.getComponents();
        positionMutationComponents.add(0.25, new NEATMMutateIndividualSensor(
                new SelectSensorsFixed(1), new MutatePerturbSensorParameter(5.0f, BEARING)));
        positionMutationComponents.add(0.25, new NEATMMutateIndividualSensor(
                new SelectSensorsFixed(2), new MutatePerturbSensorParameter(5.0f, BEARING)));
        positionMutationComponents.add(0.25, new NEATMMutateIndividualSensor(
                new SelectSensorsFixed(1), new MutatePerturbSensorParameter(5.0f, ORIENTATION)));
        positionMutationComponents.add(0.25, new NEATMMutateIndividualSensor(
                new SelectSensorsFixed(2), new MutatePerturbSensorParameter(5.0f, ORIENTATION)));
        positionMutationComponents.finalizeStructure();

        result.addOperation(0.1, positionMutation);

        // Add the sensor field mutators
        CompoundOperator fieldMutation = new CompoundOperator();
        OperationList fieldMutationComponents = fieldMutation.getComponents();
        fieldMutationComponents.add(0.5, new NEATMMutateSensorGroup(
                new SelectSensorsType(ULTRASONIC), new MutatePerturbSensorParameter(5.0f, RANGE)));
        fieldMutationComponents.add(0.5, new NEATMMutateSensorGroup(
                new SelectSensorsType(PROXIMITY), new MutatePerturbSensorParameter(5.0f, RANGE)));
        fieldMutationComponents.add(0.5, new NEATMMutateSensorGroup(
                new SelectSensorsType(ULTRASONIC),
                new MutatePerturbSensorParameter(5.0f, FIELD_OF_VIEW)));
        fieldMutationComponents.add(0.5, new NEATMMutateSensorGroup(
                new SelectSensorsType(PROXIMITY),
                new MutatePerturbSensorParameter(5.0f, FIELD_OF_VIEW)));
        fieldMutationComponents.finalizeStructure();

        result.addOperation(0.05, fieldMutation);

        // Add sensor mutation
//        double connectionDensity = 0.1; chuck : edit connection density
        double connectionDensity = 1;
        CompoundOperator addSensorMutation = new CompoundOperator();
        OperationList addSensorComponents = addSensorMutation.getComponents();
        addSensorComponents.add(0.5, new NEATMMutateAddSensor(SensorType.PROXIMITY,
                connectionDensity, /*new FixedLinkWeight() chuck : edit weight */new MutateResetLinkWeight()));
        addSensorComponents.add(0.5, new NEATMMutateAddSensor(SensorType.ULTRASONIC,
                connectionDensity, /*new FixedLinkWeight() chuck : edit weight */new MutatePerturbLinkWeight(0.2)));
        addSensorComponents.finalizeStructure();

        result.addOperation(0.05, addSensorMutation);


        result.getOperators().finalizeStructure();

        result.setCODEC(new NEATMCODEC());

        return result;
    }
}

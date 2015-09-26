package za.redbridge.controller.NEATM.training.opp;

import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.training.opp.NEATMutation;

import java.util.List;
import java.util.Random;

import za.redbridge.controller.NEATM.training.NEATMGenome;
import za.redbridge.controller.NEATM.training.NEATMNeuronGene;
import za.redbridge.controller.NEATM.training.opp.sensors.MutateSensor;
import za.redbridge.controller.NEATM.training.opp.sensors.SelectSensors;

/**
 * Created by jamie on 2014/09/08.
 */
public class NEATMMutateIndividualSensor extends NEATMutation {

    private final SelectSensors sensorSelection;

    private final MutateSensor mutation;

    /**
     * Construct a position mutation operator
     * @param sensorSelection The method used to choose the sensors for mutation.
     * @param mutation The method used to actually mutate the sensor.
     */
    public NEATMMutateIndividualSensor(SelectSensors sensorSelection, MutateSensor mutation) {
        this.sensorSelection = sensorSelection;
        this.mutation = mutation;
    }

    public SelectSensors getSensorSelection() {
        return sensorSelection;
    }

    public MutateSensor getMutation() {
        return mutation;
    }

    @Override
    public void performOperation(Random rnd, Genome[] parents, int parentIndex, Genome[] offspring,
            int offspringIndex) {
        NEATMGenome target =
                (NEATMGenome) obtainGenome(parents, parentIndex, offspring, offspringIndex);

        List<NEATMNeuronGene> sensors = sensorSelection.selectSensors(rnd, target);
        for (NEATMNeuronGene sensor : sensors) {
            mutation.mutateSensor(rnd, sensor.getSensorConfiguration());
        }
    }
}

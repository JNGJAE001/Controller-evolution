package za.redbridge.controller.NEATM.training.opp.sensors;

import org.encog.ml.ea.train.EvolutionaryAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import za.redbridge.controller.NEATM.training.NEATMGenome;
import za.redbridge.controller.NEATM.training.NEATMNeuronGene;

/**
 * Created by jamie on 2014/09/08.
 */
public class SelectSensorsFixed implements SelectSensors {

    private EvolutionaryAlgorithm evolutionaryAlgorithm;

    private final int sensorCount;

    public SelectSensorsFixed(int sensorCount) {
        this.sensorCount = sensorCount;
    }

    @Override
    public EvolutionaryAlgorithm getTrainer() {
        return evolutionaryAlgorithm;
    }

    @Override
    public void init(EvolutionaryAlgorithm theTrainer) {
        this.evolutionaryAlgorithm = theTrainer;
    }

    @Override
    public List<NEATMNeuronGene> selectSensors(Random rnd, NEATMGenome genome) {
        final List<NEATMNeuronGene> result = new ArrayList<>();
        final int count = Math.min(sensorCount, genome.getConfigurableSensorCount());

        while (result.size() < count) {
            final int idx = rnd.nextInt(genome.getInputNeuronsChromosome().size());
            final NEATMNeuronGene sensor = genome.getInputNeuronsChromosome().get(idx);
            if (sensor.getSensorConfiguration().getSensorType().isConfigurable()
                    && !result.contains(sensor)) {
                result.add(sensor);
            }
        }
        return result;
    }
}

package za.redbridge.controller.NEATM.training.opp.sensors;

import org.encog.ml.ea.train.EvolutionaryAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import za.redbridge.controller.NEATM.sensor.SensorType;
import za.redbridge.controller.NEATM.training.NEATMGenome;
import za.redbridge.controller.NEATM.training.NEATMNeuronGene;

/**
 * Created by jamie on 2014/11/28.
 */
public class SelectSensorsType implements SelectSensors {

    private EvolutionaryAlgorithm trainer;

    private final SensorType sensorType;

    public SelectSensorsType(SensorType sensorType) {
        if (!sensorType.isConfigurable()) {
            throw new IllegalArgumentException("Sensor type not configurable");
        }

        this.sensorType = sensorType;
    }

    @Override
    public EvolutionaryAlgorithm getTrainer() {
        return trainer;
    }

    @Override
    public void init(EvolutionaryAlgorithm theTrainer) {
        trainer = theTrainer;
    }

    @Override
    public List<NEATMNeuronGene> selectSensors(Random rnd, NEATMGenome genome) {
        List<NEATMNeuronGene> inputNeurons = genome.getInputNeuronsChromosome();

        List<NEATMNeuronGene> selectedNeurons = new ArrayList<>();
        for (NEATMNeuronGene neuron : inputNeurons) {
            if (neuron.getSensorConfiguration().getSensorType() == sensorType) {
                selectedNeurons.add(neuron);
            }
        }

        // If no sensors available of specified type, try fall back to different type
        if (selectedNeurons.isEmpty()) {
            SensorType[] sensorTypes = SensorType.values();
            for (SensorType type : sensorTypes) {
                if (type == sensorType || !type.isConfigurable()) {
                    continue;
                }

                for (NEATMNeuronGene neuron : inputNeurons) {
                    if (neuron.getSensorConfiguration().getSensorType() == type) {
                        selectedNeurons.add(neuron);
                    }
                }

                if (!selectedNeurons.isEmpty()) {
                    break;
                }
            }
        }

        return selectedNeurons;
    }
}

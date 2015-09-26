package za.redbridge.controller;

import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.neat.NEATNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sim.util.Double2D;
import za.redbridge.controller.NEATM.sensor.SensorMorphology;
import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.sensor.AgentSensor;

/**
 * Created by jamie on 2014/10/06.
 */
public class NEATPhenotype implements Phenotype {

    private final NEATNetwork network;
    private final SensorMorphology morphology;

    private final MLData input;
    private final List<AgentSensor> sensors;

    public NEATPhenotype(NEATNetwork network, SensorMorphology morphology) {
        this.network = network;
        this.morphology = morphology;

        // Initialise sensors
        final int numSensors = morphology.getNumSensors();
        sensors = new ArrayList<>(numSensors);
        for (int i = 0; i < numSensors; i++) {
            sensors.add(morphology.getSensor(i));
        }

        input = new BasicMLData(numSensors);
    }

    @Override
    public List<AgentSensor> getSensors() {
        return sensors;
    }

    @Override
    public Double2D step(List<List<Double>> sensorReadings) {
        final MLData input = this.input;
        for (int i = 0, n = input.size(); i < n; i++) {
            input.setData(i, sensorReadings.get(i).get(0));
        }

        MLData output = network.compute(input);
        return new Double2D(output.getData(0) * 2.0 - 1.0, output.getData(1) * 2.0 - 1.0);
    }

    @Override
    public Phenotype clone() {
        return new NEATPhenotype(network, morphology);
    }

    @Override
    public void configure(Map<String, Object> stringObjectMap) {
        throw new UnsupportedOperationException();
    }
}

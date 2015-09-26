package za.redbridge.controller.SANE;

import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import sim.util.Double2D;
import za.redbridge.controller.NEATM.sensor.SensorMorphology;
import za.redbridge.simulator.phenotype.Phenotype;
import za.redbridge.simulator.sensor.AgentSensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jae on 2015/09/14.
 * Phenotype for SANE
 */

public class SANEPhenotype implements Phenotype
{

    private final BasicNetwork network;
    private final SensorMorphology morphology;

    private final MLData input;
    private final List<AgentSensor> sensors;

    public SANEPhenotype(BasicNetwork network, SensorMorphology morphology)
    {
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
    public List<AgentSensor> getSensors()
    {
        return sensors;
    }

    @Override
    public Double2D step(List<List<Double>> sensorReadings) {
        final MLData input = this.input;
        for (int i = 0, n = input.size(); i < n; i++) {
            input.setData(i, sensorReadings.get(i).get(0));
        }

        MLData output = network.compute(input);

        System.out.println("left wheel : " + output.getData(0) + "   right wheel : " + output.getData(1));
        return new Double2D(output.getData(0) * 2.0 - 1.0, output.getData(1) * 2.0 - 1.0);
    }

    @Override
    public Phenotype clone()
    {
        return new SANEPhenotype(network, morphology);
    }

    @Override
    public void configure(Map<String, Object> map)
    {
        throw new UnsupportedOperationException();
    }
}

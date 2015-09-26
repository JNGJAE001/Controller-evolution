package za.redbridge.controller.SANE;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.MLMethod;
import org.encog.ml.ea.codec.GeneticCODEC;
import org.encog.ml.ea.genome.Genome;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by jae on 2015/09/13.
 * Decoding and encoding of genotypes to phenotype and vice versa
 */

public class SANECODEC implements GeneticCODEC, Serializable
{
    /**
     * The serial id.
     */
    private static final long serialVersionUID = 35124L;

    @Override
    public MLMethod decode(Genome genome)
    {
        BlueprintGenome blueprintGenome = (BlueprintGenome) genome;

        //basic neural network
        BasicNetwork network = new BasicNetwork();

        //add input layer
        network.addLayer(new BasicLayer(null, false, SANE.INPUT_SIZE));

        //add hidden layer
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, SANE.HIDDEN_SIZE));

        //add output layer
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, SANE.OUTPUT_SIZE));

        //finalize network structure
        network.getStructure().finalizeStructure();

        //blueprint used to construct the hidden layer
        NeuronGenome[] blueprint = blueprintGenome.getBlueprint();

        //System.out.println("Network construction , length"+ blueprint.length);

        for (int i = 0; i < SANE.INPUT_SIZE; i++)
        {
            for (int h = 0; h < SANE.HIDDEN_SIZE; h++)
            {
                network.enableConnection(0,i,h,false);
            }
        }

        for (int h = 0; h < SANE.HIDDEN_SIZE; h++)
        {
            for (int o = 0; o < SANE.OUTPUT_SIZE; o++)
            {
                network.enableConnection(0,h,o,false);
            }
        }
        //construct weighted connection based on the blueprint
        for (int i = 0; i < blueprint.length; i++)
        {
            for (Connection connection : blueprint[i].getChromosome())
            {
                int label = connection.getLabel();
                double weight = connection.getWeight();

                //System.out.println("Label : " + label +"    Weight : "+weight);
                //connect to to input neuron
                if (label < SANE.INPUT_SIZE)
                {
                    network.enableConnection(0, label, i, true);
                    network.setWeight(0, label, i, weight);
                }
                //connect to to output neuron
                else
                {
                    network.enableConnection(1, i, label - SANE.INPUT_SIZE, true);
                    network.setWeight(1, i, label - SANE.INPUT_SIZE, weight);
                }
            }
        }
        //System.out.println();
        return network;
    }

    @Override
    public Genome encode(MLMethod mlMethod)
    {
        return null;
    }
}

package za.redbridge.controller;

import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.redbridge.controller.NEATM.sensor.SensorConfiguration;
import za.redbridge.controller.NEATM.sensor.parameter.SensorParameterSet;
import za.redbridge.controller.NEATM.training.NEATMNeuronGene;
import za.redbridge.controller.NEATM.sensor.SensorType;


import static za.redbridge.controller.NEATM.sensor.parameter.spec.ParameterType.*;

/**
 * Basic methods to save a genome as a Graphviz ".dot" file so that it can be visualized.
 * This code is pretty clumsy but works reasonably.
 *
 * Created by jamie on 2014/10/06.
 */
public class GraphvizEngine {

    private static final Logger log = LoggerFactory.getLogger(GraphvizEngine.class);

    public static void saveGenome(NEATGenome genome, Path path) {
        try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset())) {
            writer.write("digraph G {");
            writer.newLine();

            writeNeuronGenes(writer, genome.getNeuronsChromosome());
            writeLinkGenes(writer, genome.getLinksChromosome());

            writer.write("}");
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            log.error("Failed to save graphviz representation of network", e);
        }
    }

    private static void writeNeuronGenes(BufferedWriter writer, List<NEATNeuronGene> neurons)
            throws IOException {
        List<NEATNeuronGene> inputs = new ArrayList<>();
        List<NEATNeuronGene> outputs = new ArrayList<>();

        for (NEATNeuronGene neuron : neurons) {
            writer.write("  ");
            writer.write(String.valueOf(neuron.getId()));
            if (neuron.getNeuronType() == NEATNeuronType.Input
                    && neuron instanceof NEATMNeuronGene) {
                NEATMNeuronGene neatmNeuronGene = (NEATMNeuronGene) neuron;
                SensorConfiguration sensorConfiguration = neatmNeuronGene.getSensorConfiguration();
                SensorType sensorType = sensorConfiguration.getSensorType();
                writer.write(" [ label=\"" + neuron.getNeuronType()
                        + " (" + neuron.getId() + ")"
                        + "\\n" + formatSensorTypeString(sensorType));

                if (sensorType.isConfigurable()) {
                    SensorParameterSet parameterSet = sensorConfiguration.getSensorParameterSet();
                    writer.write("\\n" + String.format("B:%.2f, O:%.2f, R:%.2f, F:%.2f",
                            parameterSet.getParameter(BEARING).getValue(),
                            parameterSet.getParameter(ORIENTATION).getValue(),
                            parameterSet.getParameter(RANGE).getValue(),
                            parameterSet.getParameter(FIELD_OF_VIEW).getValue()));
                }
                writer.write("\" ];");
            } else {
                writer.write(" [ label=\"" + neuron.getNeuronType()
                        + " (" + neuron.getId() + ")\" ];");
            }
            writer.newLine();

            NEATNeuronType type = neuron.getNeuronType();
            if (type == NEATNeuronType.Input || type == NEATNeuronType.Bias) {
                inputs.add(neuron);
            } else if (type == NEATNeuronType.Output) {
                outputs.add(neuron);
            }
        }

        // Set the same rank for all inputs/outputs
        writer.write("  ");
        writer.write("{ rank=same ");
        for (NEATNeuronGene neuron : inputs) {
            writer.write(String.valueOf(neuron.getId()));
            writer.write(" ");
        }
        writer.write("}");
        writer.newLine();

        writer.write("  ");
        writer.write("{ rank=same ");
        for (NEATNeuronGene neuron : outputs) {
            writer.write(String.valueOf(neuron.getId()));
            writer.write(" ");
        }
        writer.write("}");
        writer.newLine();
    }

    private static void writeLinkGenes(BufferedWriter writer, List<NEATLinkGene> links)
            throws IOException {
        for (NEATLinkGene link : links) {
            writer.write("  ");
            writer.write(link.getFromNeuronID() + " -> " + link.getToNeuronID());
            if (link.isEnabled()) {
                writer.write(" [ label=\"" + String.format("%.3f", link.getWeight()) + "\" ];");
            } else {
                writer.write(" [ style=\"dashed\" ];");
            }
            writer.newLine();
        }
    }

    public static void saveNetwork(NEATNetwork network, Path path) {
        try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset())) {
            writer.write("digraph G {");
            writer.newLine();

            writeNodes(writer, network);
            writeLinks(writer, network.getLinks());

            writer.write("}");
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            log.error("Failed to save graphviz representation of network", e);
        }
    }

    private static void writeNodes(BufferedWriter writer, NEATNetwork network)
        throws IOException {
        // Reconstruct node information from links (only works for constant inputs/outputs)
        Map<Integer, String> nodes = new HashMap<>();
        List<Integer> inputs = new ArrayList<>();
        List<Integer> outputs = new ArrayList<>();
        NEATLink[] links = network.getLinks();
        int inputCount = network.getInputCount();
        int outputCount = network.getOutputCount();
        for (NEATLink link : links) {
            int fromNeuron = link.getFromNeuron();
            if (!nodes.containsKey(fromNeuron)) {
                NEATNeuronType type = typeForNode(fromNeuron, inputCount, outputCount);
                nodes.put(fromNeuron, type.toString());

                if (type == NEATNeuronType.Input || type == NEATNeuronType.Bias) {
                    inputs.add(fromNeuron);
                } else if (type == NEATNeuronType.Output) {
                    outputs.add(fromNeuron);
                }
            }

            int toNeuron = link.getToNeuron();
            if (!nodes.containsKey(toNeuron)) {
                NEATNeuronType type = typeForNode(toNeuron, inputCount, outputCount);
                nodes.put(toNeuron, type.toString());

                if (type == NEATNeuronType.Output) {
                    outputs.add(toNeuron);
                }
            }
        }

        // Write to file
        for (Map.Entry<Integer, String> entry : nodes.entrySet()) {
            writer.write("  ");
            writer.write(entry.getKey().toString());
            writer.write(" [ label=\"" + entry.getValue() + " (" + entry.getKey() + ")\" ]");
            writer.newLine();
        }

        // Set the same rank for all inputs/outputs
        writer.write("  ");
        writer.write("{ rank=same ");
        for (Integer id : inputs) {
            writer.write(String.valueOf(id));
            writer.write(" ");
        }
        writer.write("}");
        writer.newLine();

        writer.write("  ");
        writer.write("{ rank=same ");
        for (Integer id : outputs) {
            writer.write(String.valueOf(id));
            writer.write(" ");
        }
        writer.write("}");
        writer.newLine();
    }

    private static NEATNeuronType typeForNode(int node, int inputCount, int outputCount) {
        if (node < inputCount) {
            return NEATNeuronType.Input;
        }

        if (node == inputCount) {
            return NEATNeuronType.Bias;
        }

        if (node < inputCount + outputCount + 1) {
            return NEATNeuronType.Output;
        }

        return NEATNeuronType.Hidden;
    }

    private static void writeLinks(BufferedWriter writer, NEATLink[] links)
            throws IOException {
        for (NEATLink link : links) {
            writer.write("  ");
            writer.write(link.getFromNeuron() + " -> " + link.getToNeuron());
            writer.write(" [ label=\"" + String.format("%.3f", link.getWeight()) + "\" ];");
            writer.newLine();
        }
    }

    private static String formatSensorTypeString(SensorType sensorType) {
        String str = sensorType.toString();
        str = str.replace('_', ' ');
        str = str.toLowerCase();
        str = Character.toUpperCase(str.charAt(0)) + str.substring(1);
        return str;
    }
}

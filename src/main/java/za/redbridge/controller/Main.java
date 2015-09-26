package za.redbridge.controller;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import org.encog.Encog;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.MLMethod;
import org.encog.ml.MethodFactory;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import za.redbridge.controller.NEATM.sensor.SensorMorphology;
import za.redbridge.controller.SANE.BlueprintGenome;
import za.redbridge.controller.SANE.SANEControllerEvolution;
import za.redbridge.simulator.config.SimConfig;


import static za.redbridge.controller.Utils.isBlank;
import static za.redbridge.controller.Utils.readObjectFromFile;

/**
 * Entry point for the controller platform.
 *
 * Created by jamie on 2014/09/09.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final double CONVERGENCE_SCORE = 110;

    public static void main(String[] args) throws IOException {
        Args options = new Args();
        new JCommander(options, args);

        log.info(options.toString());

        SimConfig simConfig;
        if (!isBlank(options.configFile)) {
            simConfig = new SimConfig(options.configFile);
        } else {
            simConfig = new SimConfig();
        }

        // Load the morphology
        SensorMorphology morphology = null;
        morphology = new KheperaIIIMorphology();
        ScoreCalculator calculateScore =
                new ScoreCalculator(simConfig, options.simulationRuns, morphology);

        if (!isBlank(options.genomePath)) {
            //BlueprintGenome gen = (BlueprintGenome)readObjectFromFile(options.genomePath);
            BasicNetwork network = (BasicNetwork)readObjectFromFile(options.genomePath);
            calculateScore.demo(network);
            return;
        }


        SANEControllerEvolution sane = new SANEControllerEvolution(new MethodFactory(){ @Override public MLMethod
                                                                                        factor(){System.out.println("Stub");return null; }},
                calculateScore,options.populationSize);

        log.debug("Population of size " + options.populationSize + " initialized");

        final StatsRecorder statsRecorder = new StatsRecorder(sane.getGenetic(), calculateScore);
        statsRecorder.recordIterationStats();

        for (int i = 0; i < options.numIterations; i++) {
            sane.iteration();
            statsRecorder.recordIterationStats();
        }

        log.debug("Training complete");
        Encog.getInstance().shutdown();
    }

    private static class Args {
        @Parameter(names = "-c", description = "Simulation config file to load")
        private String configFile = "config/mediumSimConfig.yml";

        @Parameter(names = "-i", description = "Number of simulation iterations to train for")
        private int numIterations = 500;

        @Parameter(names = "-p", description = "Initial population size")
        private int populationSize = 100;

        @Parameter(names = "--sim-runs", description = "Number of simulation runs per iteration")
        private int simulationRuns = 5;

        @Parameter(names = "--conn-density", description = "Adjust the initial connection density"
                + " for the population")
        private double connectionDensity = 0.5;
        @Parameter(names = "--demo", description = "Show a GUI demo of a given genome")
        private String genomePath = null;

        @Parameter(names = "--control", description = "Run with the control case")
        private boolean control = true;

        @Parameter(names = "--morphology", description = "For use with the control case, provide"
                + " the path to a serialized MMNEATNetwork to have its morphology used for the"
                + " control case")
        private String morphologyPath = null;

        @Parameter(names = "--population", description = "To resume a previous controller, provide"
                + " the path to a serialized population")
        private String populationPath = null;

        @Override
        public String toString() {
            return "Options: \n"
                    + "\tConfig file path: " + configFile + "\n"
                    + "\tNumber of simulation steps: " + numIterations + "\n"
                    + "\tPopulation size: " + populationSize + "\n"
                    + "\tNumber of simulation tests per iteration: " + simulationRuns + "\n"
                    + "\tInitial connection density: " + connectionDensity + "\n"
                    + "\tDemo network config path: " + genomePath + "\n"
                    + "\tRunning with the control case: " + control + "\n"
                    + "\tMorphology path: " + morphologyPath + "\n"
                    + "\tPopulation path: " + populationPath;
        }
    }
}

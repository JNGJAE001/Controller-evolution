package za.redbridge.controller.SANE;

import java.util.Random;

/**
 * Created by Jae on 2015-08-23.
 * SANE algorithms implementation
 */
public class SANE
{
    static final int POPULATION_SIZE = 20;

    //dimension of the neural network
    public static final int INPUT_SIZE = 6;
    public static final int OUTPUT_SIZE = 2;
    public static final int HIDDEN_SIZE = 5;
    public static final int IO_COUNT = INPUT_SIZE + OUTPUT_SIZE;
    public static final int network_size = IO_COUNT + HIDDEN_SIZE;
    public static final int CHROMOSOME_LENGTH = IO_COUNT/2;

    static Random random = new Random();
    static float neuron_mutation_rate = 0.1f;
    static float blueprint_mutation_rate_random = 0.1f;
    static float blueprint_mutation_rate_offspring = 0.1f;

    public SANE()
    {
        random = new Random();
    }

}

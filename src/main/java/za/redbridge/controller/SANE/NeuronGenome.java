package za.redbridge.controller.SANE;

import org.encog.ml.ea.genome.BasicGenome;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.genetic.genome.ArrayGenome;

import java.io.Serializable;
import java.util.*;

/**
 * Created by jae on 2015/09/10.
 * Genome for hidden layer neuron (SANE).
 */
public class NeuronGenome extends BasicGenome implements ArrayGenome, Serializable
{
    //array of connections
    private Connection[] chromosome;

    //number of times this neuron has participated in the network
    private int participation;

    //list of children
    private List<NeuronGenome> children = new ArrayList<NeuronGenome>();

    //constructor - empty chromosome
    public NeuronGenome(int size)
    {
        chromosome = new Connection[size];
    }

    //constructor - receives chromosome
    public NeuronGenome(Connection[] chromosome)
    {
        this.chromosome = chromosome;
    }

    //copy constructor
    public NeuronGenome(NeuronGenome n)
    {
        this.chromosome = new Connection[n.chromosome.length];
        for (int i = 0; i < chromosome.length; i++)
        {
            this.chromosome[i] = new Connection(n.chromosome[i]);
        }
    }

    //initialize chromosome randomly
    public void randomInit()
    {
        Random random = new Random();

        //obtain unique random number by shuffling the list of unique numbers
        List<Integer> ints = new ArrayList<Integer>();
        for (int i = 0; i < SANE.IO_COUNT; i++) ints.add(i);
        Collections.shuffle(ints, random);

        //init chromosome
        for (int i = 0; i < chromosome.length; i++)
        {
            chromosome[i] = new Connection(ints.get(i), random.nextDouble()/*(random.nextFloat()*2) -1*/);
        }
    }

    @Override
    //copies connection object
    public void copy(ArrayGenome source, int sourceIndex, int targetIndex)
    {
        //creates copy of the source connection and copies it over
        this.chromosome[targetIndex] = new Connection(((NeuronGenome) source).chromosome[sourceIndex]);
    }

    @Override
    public void swap(int swap1, int swap2)
    {
        Connection temp = new Connection(chromosome[swap1]);
        chromosome[swap1].set(chromosome[swap2]);
        chromosome[swap2].set(temp);
    }

    @Override
    public void copy(Genome source)
    {
        NeuronGenome sourceNeuron = (NeuronGenome)source;
        for (int i = 0; i < chromosome.length; i++)
        {
            chromosome[i].set(sourceNeuron.chromosome[i]);
        }
        setScore(source.getScore());
        setAdjustedScore(source.getAdjustedScore());
    }

    @Override
    public int size()
    {
        return chromosome.length;
    }

    //returns data
    public Connection[] getChromosome()
    {
        return chromosome;
    }

    public void clear_children() {children.clear();}

    public void add_children(NeuronGenome n){children.add(n);}

    public List<NeuronGenome> getChildren(){return children;}

    public void addScore(double score)
    {
        double new_score =  getScore() + score;
        setScore(new_score);
    }

    public void addAdjustedScore(double score)
    {
        double new_score =  getScore() + score;
        setScore(new_score);
    }

    public void incrementParticipation()
    {
        participation++;
    }

    public void clearParticipation()
    {
        participation = 0;
    }

    public void finalizeScore()
    {
        setScore(getScore() / participation);
        setAdjustedScore(getScore());
    }
}

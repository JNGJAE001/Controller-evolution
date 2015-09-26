package za.redbridge.controller.SANE;

import org.encog.EncogError;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.population.Population;
import org.encog.ml.ea.sort.SortGenomesForSpecies;
import org.encog.ml.ea.species.SingleSpeciation;
import org.encog.ml.ea.species.Speciation;
import org.encog.ml.ea.species.Species;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;

import java.util.Collections;
import java.util.List;

/**
 * Created by jae on 2015/09/23.
 */
public class SANESpeciation implements Speciation
{
    private EvolutionaryAlgorithm owner;
    private SortGenomesForSpecies sortGenomes;

    public SANESpeciation() {
    }

    @Override
    public void init(EvolutionaryAlgorithm theOwner) {
        this.owner = theOwner;
        this.sortGenomes = new SortGenomesForSpecies(this.owner);
    }

    @Override
    public void performSpeciation(List<Genome> genomeList) {
        this.updateShare();
        Species species = (Species)this.owner.getPopulation().getSpecies().get(0);
        species.getMembers().clear();
        species.getMembers().addAll(genomeList);
        Collections.sort(species.getMembers(), this.sortGenomes);
        species.setLeader((Genome)species.getMembers().get(0));
    }

    public void performSpeciation(List<Genome> genomeList, Population population) {
        this.updateShare(population);
        Species species = (Species)population.getSpecies().get(0);
        species.getMembers().clear();
        species.getMembers().addAll(genomeList);
        Collections.sort(species.getMembers(), this.sortGenomes);
        species.setLeader((Genome)species.getMembers().get(0));
    }

    private void updateShare(Population population) {
        int speciesCount = population.getSpecies().size();
        if(speciesCount != 1) {
            throw new EncogError("SingleSpeciation can only be used with a species count of 1, species count is " + speciesCount);
        } else {
            Species species = (Species)population.getSpecies().get(0);
            species.setOffspringCount(population.getPopulationSize());
        }
    }

    private void updateShare() {
        int speciesCount = this.owner.getPopulation().getSpecies().size();
        if(speciesCount != 1) {
            throw new EncogError("SingleSpeciation can only be used with a species count of 1, species count is " + speciesCount);
        } else {
            Species species = (Species)this.owner.getPopulation().getSpecies().get(0);
            species.setOffspringCount(this.owner.getPopulation().getPopulationSize());
        }
    }
}

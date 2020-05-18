import java.util.*;

public abstract class Evaluator {

    private FitnessGenomeComparator fitComp = new FitnessGenomeComparator();

    private Counter nodeInnovation;
    private Counter connectionInnovation;

    private int populationSize;
    public int passedPopulation = 0;

    public Map<Genome, Species> speciesMap;
    private Map<Genome, Float> scoreMap;
    public List<Genome> genomes;
    public List<Genome> nextGenGenomes;
    public List<Species> species;
    public float highestScore;
    public Genome fittestGenome;
    public int fittestGenomeIndex;
    public int counter = 0;

    private Random random = new Random();

    public Evaluator(int populationSize, Genome startingGenome, Counter nodeInnovation, Counter connectionInnovation) {
        this.populationSize = populationSize;
        this.nodeInnovation = nodeInnovation;
        this.connectionInnovation = connectionInnovation;
        genomes = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            genomes.add(new Genome(startingGenome));
        }
        nextGenGenomes = new ArrayList<>(populationSize);
        speciesMap = new HashMap<>();
        scoreMap = new HashMap<>();
        species = new ArrayList<>();
    }

    public void evaluate() {
        for (Species s : species) {
            s.reset(random);
        }
        scoreMap.clear();
        speciesMap.clear();
        nextGenGenomes.clear();
        highestScore = Integer.MIN_VALUE;
        fittestGenome = null;
        fittestGenomeIndex = 0;

        //Placeing genomes into species
        for(Genome g : genomes) {
            boolean foundSpecies = false;
            for (Species s : species) {
                if (Genome.compatibilityDistance(g, s.mascot, Constants.C1, Constants.C2, Constants.C3) < Constants.DT ) {
                    s.members.add(g);
                    speciesMap.put(g, s);
                    foundSpecies = true;
                    break;
                }
            }
            if (!foundSpecies) {
                Species newSpecies = new Species(g);
                newSpecies.id = counter;
                counter++;
                species.add(newSpecies);
                speciesMap.put(g, newSpecies);
            }
        }

        species.removeIf(s -> s.members.isEmpty());

        //evaluating genomes
        for (int i = 0; i < genomes.size(); i++) {
            Species s = speciesMap.get(genomes.get(i));

            float score = evaluateGenome(genomes.get(i), i);
            if (score > Constants.MAX_FITNESS) genomes.get(i).foundSolution = true;
            float adjustedScore = score / s.members.size();

            s.addAdjustedFitness(adjustedScore);
            s.fitnessPop.add(new FitnessGenome(genomes.get(i), adjustedScore));
            scoreMap.put(genomes.get(i), adjustedScore);
            if (score > highestScore) {
                highestScore = score;
                fittestGenome = genomes.get(i);
                fittestGenomeIndex = i;
            }
        }

        Species backupSpecie = new Species(species.get(0));
        species.removeIf(s -> s.checkStagnation() && species.size()>1);
        if (species.isEmpty()) species.add(backupSpecie);
        if (species.size() == 1) {
            species.get(0).stagnationGenCount = 0;
        }

        //solved genomes gets a free pass
        int passedPop = 0;
        for (Genome g: genomes) {
            if (g.foundSolution) {
                nextGenGenomes.add(g);
                passedPop++;
            }
        }
        passedPopulation = passedPop;
        //best genomes gets a free pass
        for (Species s : species) {
            Collections.sort(s.fitnessPop, fitComp);
            Collections.reverse(s.fitnessPop);
            FitnessGenome fittestInSpecies = s.fitnessPop.get(0);
            if (!fittestInSpecies.genome.foundSolution) {
                nextGenGenomes.add(fittestInSpecies.genome);
            }
        }

        //breeding the rest
        while(nextGenGenomes.size() < populationSize) {
            Species s = getRandomSpeciesBiasedAF(random);

            Genome p1 = getRandomGenomeBiasedAF(s, random);
            Genome p2 = getRandomGenomeBiasedAF(s, random);

            Genome child;
            if (scoreMap.get(p1) >= scoreMap.get(p2)) {
                child = Genome.crossover(p1,p2, random);
            } else {
                child = Genome.crossover(p2,p1, random);
            }
            if (random.nextFloat() < Constants.MUTATION_RATE) {
                child.mutation(random);
            }
            if (random.nextFloat() < Constants.ADD_CONNECTION_RATE) {
                child.addConnectionMutation(random, connectionInnovation, 10);
            }
            if (random.nextFloat() < Constants.ADD_NODE_RATE) {
                child.addNodeMutation(random, connectionInnovation,nodeInnovation);
            }
            nextGenGenomes.add(child);
        }

        genomes = nextGenGenomes;
        nextGenGenomes = new ArrayList<>();
    }

    private Species getRandomSpeciesBiasedAF (Random random) {
        double completeWeight = 0.0;
        for(Species s : species) {
            completeWeight += s.getTotalAdjustedFitness();
        }
        double r = Math.random() * completeWeight;
        double countWeight = 0.0;
        for (Species s : species) {
            countWeight += s.getTotalAdjustedFitness();
            if (countWeight >= r) {
                return s;
            }
        }
        throw new RuntimeException("Couldn't find a species");
    }

    private Genome getRandomGenomeBiasedAF (Species speciesFrom,Random random) {
        double completeWeight = 0.0;
        for(FitnessGenome s : speciesFrom.fitnessPop) {
            completeWeight += (s.fitness * s.fitness);
        }
        double r = Math.random() * completeWeight;
        double countWeight = 0.0;
        for (FitnessGenome s : speciesFrom.fitnessPop) {
            countWeight += (s.fitness * s.fitness);
            if (countWeight >= r) {
                return s.genome;
            }
        }
        throw new RuntimeException("Couldn't find a genome");
    }

    abstract int evaluateGenome(Genome genome, int index);

    public class FitnessGenome {
        public float fitness;
        public Genome genome;
        public FitnessGenome(Genome genome, float fitness) {
            this.genome = genome;
            this.fitness = fitness;
        }
    }

    public class FitnessGenomeComparator implements Comparator<FitnessGenome> {
        @Override
        public int compare(FitnessGenome o1, FitnessGenome o2) {
            if (o1.fitness > o2.fitness) {
                return 1;
            } else if (o2.fitness > o1.fitness) {
                return -1;
            }
            return 0;
        }
    }

    public class Species {

        public Genome mascot;
        public List<Genome> members;
        public List<FitnessGenome> fitnessPop;
        public float totalAdjustedFitness = 0f;
        public float topFitness = 0;
        public int stagnationGenCount = 0;
        public int id;

        public Species(Genome mascot) {
            this.mascot = mascot;
            this.members = new LinkedList<>();
            this.members.add(mascot);
            this.fitnessPop = new ArrayList<>();
        }

        public Species(Species species) {
            this.mascot = new Genome(species.mascot);
            this.members = new LinkedList<>(species.members);
            this.fitnessPop = new ArrayList<>(species.fitnessPop);
        }

        public float getTotalAdjustedFitness() {
            return totalAdjustedFitness*(1.0f-stagnationGenCount*Constants.STAGNATION_DECAY);
        }

        public void addAdjustedFitness(float adjustedFitness) {
            this.totalAdjustedFitness += adjustedFitness;
        }

        public void reset(Random r) {
            int newMascotIndex = r.nextInt(members.size());
            this.mascot = members.get(newMascotIndex);
            members.clear();
            fitnessPop.clear();
            totalAdjustedFitness = 0;
        }

        public boolean checkStagnation() {
            boolean masterRace = true;
            for (Genome g:members) {
                if (!g.foundSolution) masterRace = false;
            }
            if (masterRace) {
                stagnationGenCount = 0;
                return false;
            }
            if (totalAdjustedFitness > topFitness) {
                topFitness = totalAdjustedFitness;
                stagnationGenCount = 0;
            } else {
                stagnationGenCount++;
                if (stagnationGenCount > Constants.MAX_STAGNATION) return true;
            }
            return false;
        }
    }
}

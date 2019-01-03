import java.util.*;

public abstract class Evaluator {

    private FitnessGenomeComparator fitComp = new FitnessGenomeComparator();

    private Counter nodeInnovation;
    private Counter connectionInnovation;

    private int populationSize;

    private Map<Genome, Species> speciesMap;
    private Map<Genome, Float> scoreMap;
    private List<Genome> genomes;
    private List<Genome> nextGenGenomes;
    public List<Species> species;
    public float highestScore;
    public Genome fittestGenome;

    private Random random = new Random();

    public Evaluator(int populationSize, Genome startingGenome, Counter nodeInnovation, Counter connectionInnovation) {
        this.populationSize = populationSize;
        this.nodeInnovation = nodeInnovation;
        this.connectionInnovation = connectionInnovation;
        genomes = new ArrayList<Genome>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            genomes.add(new Genome(startingGenome));
        }
        nextGenGenomes = new ArrayList<Genome>(populationSize);
        speciesMap = new HashMap<Genome, Species>();
        scoreMap = new HashMap<Genome, Float>();
        species = new ArrayList<Species>();
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

        //Placeing genomes into species
        for(Genome g : genomes) {
            boolean foundSpecies = false;
            for (Species s : species) {
                if (Genome.compatibilityDistance(g, s.mascot, Constants.C1, Constants.C2, Constants.C3) < Constants.DT) {
                    s.members.add(g);
                    speciesMap.put(g, s);
                    foundSpecies = true;
                    break;
                }
            }
            if (!foundSpecies) {
                Species newSpecies = new Species(g);
                species.add(newSpecies);
                speciesMap.put(g, newSpecies);
            }
        }

        species.removeIf(s -> s.members.isEmpty());

        //evaluating genomes
        for (Genome g: genomes) {
            Species s = speciesMap.get(g);

            float score = evaluateGenome(g);
            float adjustedScore = score / s.members.size();

            s.addAdjustedFitness(adjustedScore);
            s.fitnessPop.add(new FitnessGenome(g, adjustedScore));
            scoreMap.put(g, adjustedScore);
            if (score > highestScore) {
                highestScore = score;
                fittestGenome = g;
            }
        }

        //best genomes gets a free pass
        for (Species s : species) {
            Collections.sort(s.fitnessPop, fitComp);
            Collections.reverse(s.fitnessPop);
            FitnessGenome fittestInSpecies = s.fitnessPop.get(0);
            nextGenGenomes.add(fittestInSpecies.genome);
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
            completeWeight += s.totalAdjustedFitness;
        }
        double r = Math.random() * completeWeight;
        double countWeight = 0.0;
        for (Species s : species) {
            countWeight += s.totalAdjustedFitness;
            if (countWeight >= r) {
                return s;
            }
        }
        throw new RuntimeException("Couldn't find a species");
    }

    private Genome getRandomGenomeBiasedAF (Species speciesFrom,Random random) {
        double completeWeight = 0.0;
        for(FitnessGenome s : speciesFrom.fitnessPop) {
            completeWeight += s.fitness;
        }
        double r = Math.random() * completeWeight;
        double countWeight = 0.0;
        for (FitnessGenome s : speciesFrom.fitnessPop) {
            countWeight += s.fitness;
            if (countWeight >= r) {
                return s.genome;
            }
        }
        throw new RuntimeException("Couldn't find a genome");
    }

    abstract int evaluateGenome(Genome genome);

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

        public Species(Genome mascot) {
            this.mascot = mascot;
            this.members = new LinkedList<>();
            this.members.add(mascot);
            this.fitnessPop = new ArrayList<>();
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
    }
}

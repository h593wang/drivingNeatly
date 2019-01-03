import java.util.*;

public class Genome {

    public Map<Integer, ConnectionGene> connections;
    public Map<Integer, NodeGene> nodes;

    private static ArrayList<Integer> helperList1 = new ArrayList<>();
    private static ArrayList<Integer> helperList2 = new ArrayList<>();

    public Genome() {
        nodes = new HashMap<>();
        connections = new HashMap<>();
    }

    public Genome(Genome toBeCopied) {
        nodes = new HashMap<>();
        connections = new HashMap<>();

        for (Integer index : toBeCopied.nodes.keySet()) {
            nodes.put(index, toBeCopied.nodes.get(index).copy());
        }

        for (Integer index : toBeCopied.connections.keySet()) {
            connections.put(index, toBeCopied.connections.get(index).copy());
        }
    }

    public void mutation(Random r) {
        for (ConnectionGene connection:connections.values()) {
            if (r.nextFloat() < Constants.PROBABILITY_PRETURBING) {
                connection.weight *= r.nextFloat()*4.0f - 2.0f;
            } else {
                connection.weight = r.nextFloat() * 4.0f - 2.0f;
            }
        }
    }

    public void addConnectionMutation(Random r, Counter counter, int maxAttempts) {
        int tries = 0;
        boolean success = false;
        while (tries < maxAttempts && !success) {
            tries++;

            Integer [] nodeInnovationNumbers = new Integer[nodes.keySet().size()];
            nodes.keySet().toArray(nodeInnovationNumbers);
            Integer keyNode1 = nodeInnovationNumbers[r.nextInt(nodeInnovationNumbers.length)];
            Integer keyNode2 = nodeInnovationNumbers[r.nextInt(nodeInnovationNumbers.length)];

            NodeGene node1 = nodes.get(keyNode1);
            NodeGene node2 = nodes.get(keyNode2);

            boolean reversed = false;
            if (node1.type == NodeGene.TYPE.HIDDEN && node2.type == NodeGene.TYPE.INPUT) {
                reversed = true;
            } else if (node1.type == NodeGene.TYPE.OUTPUT && node2.type == NodeGene.TYPE.INPUT) {
                reversed = true;
            } else if (node1.type == NodeGene.TYPE.OUTPUT && node2.type == NodeGene.TYPE.HIDDEN) {
                reversed = true;
            }

            boolean connectionImpossible = false;
            if (node1.type == NodeGene.TYPE.INPUT && node2.type == NodeGene.TYPE.INPUT) {
                connectionImpossible = true;
            } else if (node1.type == NodeGene.TYPE.OUTPUT && node2.type == NodeGene.TYPE.OUTPUT) {
                connectionImpossible = true;
            } else if (node1.id == node2.id) {
                connectionImpossible = true;
            }

            boolean connectionExists = false;
            for (ConnectionGene connection : connections.values()) {
                if (connection.inNode == node1.id && connection.outNode == node2.id) {
                    connectionExists = true;
                    break;
                } else if (connection.inNode == node2.id && connection.outNode == node1.id) {
                    connectionExists = true;
                    break;
                }
            }
            if (connectionExists||connectionImpossible) {
                continue;
            }

            ConnectionGene con = new ConnectionGene(reversed ? node2.id : node1.id, reversed ? node1.id : node2.id,
                    r.nextFloat() * 2f - 1f, true, counter.getInnovation());
            connections.put(con.innovation, con);
            success = true;
        }
        if (!success) {
            System.out.println("Tried, but could not add more connections");
        }
    }

    public void addNodeMutation(Random r, Counter conCounter, Counter nodeCounter) {
        ConnectionGene con = (ConnectionGene) connections.values().toArray()[r.nextInt(connections.size())];

        NodeGene inNode = nodes.get(con.inNode);
        NodeGene outNode = nodes.get(con.outNode);

        con.enabled = false;

        NodeGene newNode = new NodeGene(NodeGene.TYPE.HIDDEN, nodeCounter.getInnovation());
        ConnectionGene inToNew = new ConnectionGene(inNode.id, newNode.id, 1.0f, true, conCounter.getInnovation());
        ConnectionGene newToOld = new ConnectionGene(newNode.id, outNode.id, con.weight, true,  conCounter.getInnovation());

        nodes.put(newNode.id, newNode);
        connections.put(inToNew.innovation, inToNew);
        connections.put(newToOld.innovation, newToOld);
    }

    // assumes parent1 is more fit
    public static Genome crossover(Genome parent1, Genome parent2, Random r) {
        Genome child = new Genome();
        for (NodeGene parentNode:parent1.nodes.values()) {
            child.nodes.put(parentNode.id, parentNode.copy());
        }

        for (ConnectionGene parent1Connection : parent1.connections.values()) {
            if (parent2.connections.containsKey(parent1Connection.innovation)) { //matching gene
                child.connections.put(parent1Connection.innovation, r.nextBoolean() ?  parent1Connection.copy(): parent2.connections.get(parent1Connection.innovation).copy());
            } else { //disjoint or excess
                child.connections.put(parent1Connection.innovation, parent1Connection.copy());
            }
        }
        return child;
    }

    public static float compatibilityDistance(Genome genome1, Genome genome2, float c1, float c2, float c3) {

        int N = 1;
        int Genome1Size = genome1.connections.size() + genome1.nodes.size();
        int Genome2Size = genome2.connections.size() + genome2.nodes.size();
        if (Genome1Size > 20 || Genome2Size > 20) {
            //N = Math.max(Genome1Size,Genome2Size);
        }

        int disjointGenes = 0;
        int matchingGenes = 0;
        float weightDiff = 0.0f;
        int excessGenes = 0;

        helperList1.clear();
        helperList1.ensureCapacity(genome1.nodes.size());
        helperList1.addAll(genome1.nodes.keySet());
        Collections.sort(helperList1);
        helperList2.clear();
        helperList2.ensureCapacity(genome2.nodes.size());
        helperList2.addAll(genome2.nodes.keySet());
        Collections.sort(helperList2);

        int highestInnovation1 = helperList1.get(helperList1.size()-1);
        int highestInnovation2 = helperList2.get(helperList2.size()-1);
        int indices = Math.max(highestInnovation1, highestInnovation2);

        for (int i = 0; i <= indices; i++) {
            NodeGene node1 = genome1.nodes.get(i);
            NodeGene node2 = genome2.nodes.get(i);
            if (node1 == null && node2 != null) {
                if (highestInnovation1 >= i) {
                    disjointGenes++;
                } else {
                    excessGenes++;
                }
            } else if (node2 == null && node1 != null) {
                if (highestInnovation2 >= i) {
                    disjointGenes++;
                } else {
                    excessGenes++;
                }
            }
        }

        helperList1.clear();
        helperList1.ensureCapacity(genome1.connections.size());
        helperList1.addAll(genome1.connections.keySet());
        Collections.sort(helperList1);
        helperList2.clear();
        helperList2.ensureCapacity(genome2.connections.size());
        helperList2.addAll(genome2.connections.keySet());
        Collections.sort(helperList2);

        highestInnovation1 = helperList1.get(helperList1.size()-1);
        highestInnovation2 = helperList2.get(helperList2.size()-1);
        indices = Math.max(highestInnovation1, highestInnovation2);
        for (int i = 0; i <= indices; i++) {
            ConnectionGene connection1 = genome1.connections.get(i);
            ConnectionGene connection2 = genome2.connections.get(i);

            if (connection1 != null && connection2 != null) {
                matchingGenes++;
                weightDiff += Math.abs(connection1.weight - connection2.weight);
            } else if (connection1 == null && connection2 != null) {
                if (highestInnovation1 >= i) {
                    disjointGenes++;
                } else {
                    excessGenes++;
                }
            } else if (connection2 == null && connection1 != null) {
                if (highestInnovation2 >= i) {
                    disjointGenes++;
                } else {
                    excessGenes++;
                }
            }
        }
        return excessGenes * c1 / N + disjointGenes * c2 / N + c3 * weightDiff/matchingGenes;
    }


}


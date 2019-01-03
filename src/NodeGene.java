public class NodeGene {
    public NodeGene(TYPE type, int id) {
        this.type = type;
        this.id = id;
    }

    public NodeGene copy() {
        return new NodeGene(type,id);
    }

    public enum TYPE {
        INPUT, HIDDEN, OUTPUT
    }

    public TYPE type;
    public int id;
}

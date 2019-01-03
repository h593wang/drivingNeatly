public class ConnectionGene {
    public int inNode;
    public int outNode;
    public float weight;
    public boolean enabled;
    public int innovation;

    public ConnectionGene(int inNode, int outNode, float weight, boolean enabled, int innovation) {
        this.inNode = inNode;
        this.outNode = outNode;
        this.weight = weight;
        this.enabled = enabled;
        this.innovation = innovation;
    }

    public ConnectionGene copy() {
        return new ConnectionGene(inNode,outNode,weight,enabled,innovation);
    }
}

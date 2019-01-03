public class ConnectionGene {
    public int inNode;
    public int outNode;
    public float weight;
    public boolean enabled = true;
    public int innovation;

    public ConnectionGene(int inNode, int outNode, float weight, boolean enabled, int innovation) {
        this.inNode = inNode;
        this.outNode = outNode;
        this.weight = weight;
        this.enabled = enabled;
        this.innovation = innovation;
    }

    public ConnectionGene(ConnectionGene con) {
        this.inNode = con.inNode;
        this.outNode = con.outNode;
        this.weight = con.weight;
        this.enabled = con.enabled;
        this.innovation = con.innovation;
    }

    public void disable() {
        enabled = false;
    }

    public boolean enabled () {
        return enabled;
    }

    public ConnectionGene copy() {
        return new ConnectionGene(inNode,outNode,weight,enabled,innovation);
    }
}

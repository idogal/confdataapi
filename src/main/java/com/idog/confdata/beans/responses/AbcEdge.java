package com.idog.confdata.beans.responses;

public class AbcEdge {
    private final String id;
    private final int couplingFrequency;
    private final String source;
    private final String target;
    private final EdgeDirection edgeDirection;

    public AbcEdge(String id, int couplingFrequency, String source, String target, EdgeDirection edgeDirection) {
        this.id = id;
        this.couplingFrequency = couplingFrequency;
        this.source = source;
        this.target = target;
        this.edgeDirection = edgeDirection;
    }

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public int getCouplingFrequency() {
        return couplingFrequency;
    }

    public EdgeDirection getEdgeDirection() {
        return edgeDirection;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s", source, target, couplingFrequency, edgeDirection, id);
    }
}

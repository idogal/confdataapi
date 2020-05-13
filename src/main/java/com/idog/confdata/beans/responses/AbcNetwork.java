package com.idog.confdata.beans.responses;

import java.util.Set;

public class AbcNetwork {
    Set<AbcEdge> edges;
    Set<AbcNode> nodes;

    public AbcNetwork(Set<AbcEdge> edges, Set<AbcNode> nodes) {
        this.edges = edges;
        this.nodes = nodes;
    }

    public Set<AbcEdge> getEdges() {
        return edges;
    }

    public Set<AbcNode> getNodes() {
        return nodes;
    }
}

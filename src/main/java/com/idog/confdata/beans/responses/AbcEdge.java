package com.idog.confdata.beans.responses;

public class AbcEdge {
    private String id;
    private String source;
    private String target;

    public AbcEdge(String id, String source, String target) {
        this.id = id;
        this.source = source;
        this.target = target;
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
}

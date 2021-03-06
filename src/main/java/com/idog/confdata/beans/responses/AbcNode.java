package com.idog.confdata.beans.responses;

import java.util.Objects;

public class AbcNode {

    private final String id;
    private final String label;
    private final int x;
    private final int y;
    private final int size;

    public AbcNode(String id, String label, int x, int y, int size) {
        this.id = id;
        this.label = label;
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbcNode abcNode = (AbcNode) o;
        return Objects.equals(id, abcNode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}

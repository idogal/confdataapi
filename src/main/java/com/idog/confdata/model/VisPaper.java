package com.idog.confdata.model;

import java.util.Objects;

public class VisPaper {
    
    private Long id;

    public VisPaper(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof VisPaper)) {
            return false;
        }
        VisPaper author = (VisPaper) obj;
        return Objects.equals(author.id, this.id);
    }

    @Override
    public int hashCode() {
        int result = 84;
        result = 31 * result * id.hashCode();
        return result;
    }    
    
    @Override
    public String toString() {
        return "Id: " + String.valueOf(id);
    }
}

package org.anyline.metadata;

import java.util.ArrayList;
import java.util.List;

public class IndexsDiffer {
    private List<Index> adds = new ArrayList<>();
    private List<Index> drops = new ArrayList<>();
    private List<Index> updates = new ArrayList<>();

    public boolean isEmpty(){
        return adds.isEmpty() && drops.isEmpty() && updates.isEmpty();
    }

    public List<Index> getAdds() {
        return adds;
    }

    public void setAdds(List<Index> adds) {
        this.adds = adds;
    }

    public List<Index> getDrops() {
        return drops;
    }

    public void setDrops(List<Index> drops) {
        this.drops = drops;
    }

    public List<Index> getUpdates() {
        return updates;
    }

    public void setUpdates(List<Index> updates) {
        this.updates = updates;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A class for representing nodes of a graph (designed for MDPs, where there are distinct successor and predecessor
 * nodes for each node.
 * @author dkasenberg
 */
public class GraphVertex<T> {
    protected T contents;
    public Set<GraphVertex<T>> successors;
    public Set<GraphVertex<T>> predecessors;
    public GraphVertex(T contents) {
        this.contents = contents;
        this.successors = new HashSet<>();
    }

    public T getContents() {
        return this.contents;
    }

    public void addSuccessor(GraphVertex succ) {
        this.successors.add(succ);
    }

    public void addSuccessors(Collection<GraphVertex<T>> succ) {
        this.successors.addAll(succ);
    }
}
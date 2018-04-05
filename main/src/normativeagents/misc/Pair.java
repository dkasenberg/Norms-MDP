/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.misc;

import java.util.Objects;

/**
 *
 * @author dkasenberg
 * @param <L>
 * @param <R>
 */
public class Pair<L,R> {
    private final L left;
    private final R right;
    
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }
    
    public L getLeft() { return left; }
    public R getRight() { return right; }
       
    @Override
    public int hashCode() {
        return this.left.hashCode() + this.right.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair<?, ?> other = (Pair<?, ?>) obj;
        if (!Objects.equals(this.left, other.left)) {
            return false;
        }
        return Objects.equals(this.right, other.right);
    }
    
    @Override
    public String toString() {
        return "PAIR(\n" + left.toString() + "\n,\n" + right.toString() + "\n)";
    }
}

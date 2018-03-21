/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.misc;

/**
 *
 * @author dkasenberg
 */
public class Triple<L,M,R> {
    public L left;
    public M middle;
    public R right;

    public Triple(L l, M m, R r) {
        left = l;
        middle = m;
        right = r;
    }

    public boolean equals(Triple<L,M,R> other) {
        return this.left.equals(other.left)
                && this.middle.equals(other.middle)
                && this.right.equals(other.right);
    }

    public L getLeft() {
        return left;
    }

    public M getMiddle() {
        return middle;
    }

    public R getRight() {
        return right;
    }
}

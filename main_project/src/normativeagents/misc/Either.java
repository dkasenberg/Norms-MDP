/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.misc;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author dkasenberg (from stackoverflow page)
 */
public class Either<L, R> {

    public static <L, R> Either<L, R> left(L value) {
        return new Either<>(Optional.of(value), Optional.empty());
    }

    public static <L, R> Either<L, R> right(R value) {
        return new Either<>(Optional.empty(), Optional.of(value));
    }
    public final Optional<L> left;
    public final Optional<R> right;

    public Either(Optional<L> l, Optional<R> r) {
        left = l;
        right = r;
    }

    public <T> T map(
            Function<? super L, ? extends T> lFunc,
            Function<? super R, ? extends T> rFunc) {
        Optional<T> leftMapped = left.map(lFunc);
        Optional<T> rightMapped = right.map(rFunc);
        if(!leftMapped.isPresent()) {
            return rightMapped.get();
        }
        return leftMapped.get();
    }

    public <T> Either<T, R> mapLeft(Function<? super L, ? extends T> lFunc) {
        return new Either<>(left.map(lFunc), right);
    }

    public <T> Either<L, T> mapRight(Function<? super R, ? extends T> rFunc) {
        return new Either<>(left, right.map(rFunc));
    }

    public void apply(Consumer<? super L> lFunc, Consumer<? super R> rFunc) {
        left.ifPresent(lFunc);
        right.ifPresent(rFunc);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Either) {
            return ((Either) obj).left.equals(this.left) && ((Either)obj).right.equals(this.right);
        } else {
            if(left.isPresent()) return left.equals(obj);
            else return right.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        if(left.isPresent()) return left.hashCode();
        else return right.hashCode();
    }
}

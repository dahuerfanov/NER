package utils;

public class Pair<U extends Comparable<U>, V extends Comparable<V>> implements Comparable<Pair<U, V>> {

    public U first;
    public V second;

    public Pair(U first, V second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair pair = (Pair) o;
        if (!first.equals(pair.first)) {
            return false;
        }
        if (!second.equals(pair.second)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + second.hashCode();
        return result;
    }

    @Override
    public int compareTo(Pair<U, V> o) {
        if (first.compareTo(o.first) == 0) {
            return second.compareTo(o.second);
        } else {
            return first.compareTo(o.first);
        }
    }
}

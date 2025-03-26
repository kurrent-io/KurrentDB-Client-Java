package io.kurrent.dbclient;

class Tuple<A, B> {
    private final A _1;
    private final B _2;

    public Tuple(A _1, B _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public A get_1() {
        return this._1;
    }

    public B get_2() {
        return this._2;
    }
}

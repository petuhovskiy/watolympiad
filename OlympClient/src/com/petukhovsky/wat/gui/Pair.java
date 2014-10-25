package com.petukhovsky.wat.gui;

/**
 * Created by Arthur on 10/25/2014.
 */
public class Pair<K,V> {

    K key;
    V value;

    Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    public K getKey() {
        return key;
    }
}

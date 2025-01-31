package me.luckyluuk.luckybindings.model;

import lombok.Data;

@Data
public class Tuple<K, V> {
  private final K key;
  private final V value;

  public K fst() { return key; }
  public K getFirst() { return key; }

  public V snd() { return value; }
  public V getSecond() { return value; }
}

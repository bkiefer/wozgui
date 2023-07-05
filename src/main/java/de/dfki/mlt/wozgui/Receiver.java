package de.dfki.mlt.wozgui;

public interface Receiver<T> {
  public void receive(T s, T t);
}

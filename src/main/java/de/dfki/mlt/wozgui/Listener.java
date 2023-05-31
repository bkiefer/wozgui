package de.dfki.mlt.wozgui;

public interface Listener<T> {
  public void receive(T t);

  public void receive(T s, T t);
}

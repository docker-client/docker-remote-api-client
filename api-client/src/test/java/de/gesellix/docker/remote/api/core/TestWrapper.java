package de.gesellix.docker.remote.api.core;

public class TestWrapper<T> {
  public TestWrapper() {
  }

  private T property;

  public T getProperty() {
    return property;
  }

  public void setProperty(T property) {
    this.property = property;
  }
}

package de.gesellix.docker.remote.api.core;

import java.util.HashMap;
import java.util.Map;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class TestWrapperAdapter<T> {
  @ToJson
  public Map<String, T> toJson(TestWrapper<T> from) {
    Map<String, T> result = new HashMap<>();
    result.put("property", from.getProperty());
    return result;
  }

  @FromJson
  public TestWrapper<T> fromJson(Map<String, T> delegate) {
    TestWrapper<T> result = new TestWrapper<>();
    result.setProperty(delegate.get("property"));
    return result;
  }
}

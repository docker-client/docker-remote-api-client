package de.gesellix.docker.remote.api.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import de.gesellix.docker.remote.api.ChangeType;
import de.gesellix.docker.remote.api.ContainerState;

class NullIfEmptyEnumAdapterFactoryTest {

  @Test
  @Disabled("not sure if this should work as expected by the test")
  public void shouldReadNullEnumValues() throws IOException {
    TestWrapper<ContainerState.Status> result = deserialize(
        ContainerState.Status.class,
        null);
    assertNull(result.getProperty());
  }

  @Test
  public void shouldReadEmptyStringEnumValuesAsNull() throws IOException {
    TestWrapper<ContainerState.Status> result = deserialize(
        ContainerState.Status.class,
        "\"\"");
    assertNull(result.getProperty());
  }

  @Test
  public void shouldFailWhenReadingInvalidEnumValues() {
    JsonDataException thrown = assertThrows(JsonDataException.class,
        () -> deserialize(ContainerState.Status.class, "\"foobar\""));
    assertEquals("Expected one of [created, running, paused, restarting, removing, exited, dead] but was foobar at path $", thrown.getMessage());
  }

  @Test
  public void shouldReadStringEnumValues() throws IOException {
    TestWrapper<ContainerState.Status> result = deserialize(
        ContainerState.Status.class,
        "\"" + ContainerState.Status.Dead.getValue() + "\"");
    assertEquals(ContainerState.Status.Dead, result.getProperty());
  }

  @Test
  public void shouldReadIntegerEnumValuesFromString() throws IOException {
    TestWrapper<ChangeType> result = deserialize(
        ChangeType.class,
        "\"" + ChangeType.T1.getValue() + "\"");
    assertEquals(ChangeType.T1, result.getProperty());
  }

  @Test
  public void shouldReadIntegerEnumValuesFromNumber() throws IOException {
    TestWrapper<ChangeType> result = deserialize(
        ChangeType.class,
        ChangeType.T1.getValue());
    assertEquals(ChangeType.T1, result.getProperty());
  }

  @Test
  public void shouldReadDoubleEnumValuesFromString() throws IOException {
    TestWrapper<TestEnum> result = deserialize(
        TestEnum.class,
        "\"" + TestEnum.V1_2.getValue() + "\"");
    assertEquals(TestEnum.V1_2, result.getProperty());
  }

  @Test
  @Disabled("not sure if this should work as expected by the test")
  public void shouldReadDoubleEnumValuesFromNumber() throws IOException {
    TestWrapper<TestEnum> result = deserialize(
        TestEnum.class,
        TestEnum.V1_2.getValue());
    assertEquals(TestEnum.V1_2, result.getProperty());
  }

  private <T> TestWrapper<T> deserialize(Class<T> enumType, Object serializedValue) throws IOException {
    JsonAdapter<TestWrapper<T>> moshi = new Moshi.Builder()
        .add(new NullIfEmptyEnumAdapterFactory())
        .add(new TestWrapperAdapter<T>())
        .build()
        .adapter(Types.newParameterizedType(TestWrapper.class, enumType));

    String serialized = "{ \"property\" : " + serializedValue + " }";
    System.out.println("Deserializing '" + serialized + "'");
    return moshi.fromJson(serialized);
  }

  enum TestEnum {
    @Json(name = "1.1")
    V1_1("1.1"),
    @Json(name = "1.2")
    V1_2("1.2");

    private final String value;

    TestEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}

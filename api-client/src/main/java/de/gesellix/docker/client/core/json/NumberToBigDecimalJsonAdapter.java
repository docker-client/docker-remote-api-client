package de.gesellix.docker.client.core.json;

import java.io.IOException;
import java.math.BigDecimal;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;

public class NumberToBigDecimalJsonAdapter extends JsonAdapter<Object> {

  private final JsonAdapter<Object> delegate;

  public NumberToBigDecimalJsonAdapter(JsonAdapter<Object> delegate) {
    this.delegate = delegate;
  }

  @Override
  public Object fromJson(JsonReader reader) throws IOException {
    if (reader.peek().equals(JsonReader.Token.NUMBER)) {
      // allows Integer or Long values instead of strictly using Double as value type.
      return new BigDecimal(reader.nextString());
    }
    else {
      return delegate.fromJson(reader);
    }
  }

  @Override
  public void toJson(JsonWriter writer, Object value) throws IOException {
    if (value instanceof Number) {
      writer.jsonValue(value);
    }
    else {
      delegate.toJson(writer, value);
    }
  }
}

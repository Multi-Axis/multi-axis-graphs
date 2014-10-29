package com.github.multi_axis;

import java.math.BigDecimal;
import java.lang.Long;
import java.math.RoundingMode;
import java.io.InputStream;
import java.io.OutputStream;

import fj.F;
import fj.Unit;
import fj.data.Option;
import fj.data.Stream;
import fj.data.Validation;
import fj.data.IO;
import fj.data.IOFunctions;
import fj.data.Java;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;

import javax.json.stream.JsonParsingException;
import javax.json.JsonException;
import java.lang.ClassCastException;

import com.github.multi_axis.TimedValue;
import com.github.multi_axis.Errors;

import static fj.data.Validation.success;
import static fj.data.Validation.fail;
import static fj.data.Java.JUList_List;
import static fj.Unit.unit;
//import static fj.data.IOFunctions.lazy;

import static javax.json.Json.createReader;
import static javax.json.Json.createWriter;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createArrayBuilder;

import static com.github.multi_axis.Errors.*;
import static com.github.multi_axis.TimedValue.timedVal;

public abstract class JsonUtils {


  public static final IO<Validation<Errors,JsonObject>>
    readJsonObject(final InputStream in) {

      return new IO<Validation<Errors,JsonObject>>() {
        public Validation<Errors,JsonObject>
          run() {

            final JsonReader
              reader = createReader(in);

            try {
              final JsonObject json = reader.readObject();
              reader.close();
              return success(json); }

            catch (JsonParsingException e) {
              reader.close();
              return fail(jsonParsingError(e)); }

            catch (JsonException e) {
              reader.close();
              return fail(miscJsonError(e)); } } }; }


  public static final IO<Unit>
    writeJsonObject(final OutputStream out, final JsonObject json) {

      return new IO<Unit>() {
        public Unit
          run() {

            final JsonWriter
              writer = createWriter(out);

            writer.write(json);
            writer.close();

            return unit(); } }; }


  private JsonUtils() {}

}

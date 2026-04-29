package com.auction.data;

import com.auction.exceptions.DataException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;

public final class DataManager {

  private static final Gson GSON =
      new GsonBuilder().setPrettyPrinting().create();

  private DataManager() {}

  public static <T> void save(String path, List<T> data)
      throws DataException {

    try (Writer writer = new FileWriter(path)) {
      GSON.toJson(data, writer);
    } catch (IOException e) {
      throw new DataException("Error writing file", e);
    }
  }

  public static <T> List<T> load(String path, Type type)
      throws DataException {

    try (Reader reader = new FileReader(path)) {
      return GSON.fromJson(reader, type);
    } catch (IOException e) {
      throw new DataException("Error reading file", e);
    }
  }
}
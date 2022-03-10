package ru.turbo.goose.storages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface FileManager {
    int create() throws IOException;
    boolean exists(int id);
    File get(int id) throws FileNotFoundException;
    boolean delete(int id);
}

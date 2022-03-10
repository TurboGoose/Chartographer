package ru.turbo.goose.storages;

import org.springframework.stereotype.Service;
import ru.turbo.goose.utils.IdGenerator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

@Service
public class FileManagerImpl implements FileManager {
    static final String DEFAULT_DATA_DIR = "charta-temp";
    private File root;

    public FileManagerImpl() {
        String rootDir = readRootDirFromConfigFile();
        if (rootDir != null && !rootDir.isBlank()) {
            root = new File(rootDir);
        } else {
            try {
                root = Files.createTempDirectory("charta-temp").toFile();
            } catch (IOException exc) {
                root = new File(DEFAULT_DATA_DIR);
            }
        }
        root.mkdirs();
    }

    private String readRootDirFromConfigFile() {
        String configPath = Path.of("src", "main", "resources", "app.properties").toString();
        try (InputStream is = new FileInputStream(configPath)) {
            Properties props = new Properties();
            props.load(is);
            return props.getProperty("dataDirectory");
        } catch (IOException exc) {
            return null;
        }
    }

    FileManagerImpl(String rootDir) {
        this.root = new File(rootDir);
        root.mkdir();
    }

    @Override
    public int create() throws IOException {
        int id = IdGenerator.next();
        while (exists(id)) {
            id = IdGenerator.next();
        }
        File file = new File(generateFileName(id));
        boolean created = file.createNewFile();
        if (!created) {
            throw new IOException("Cannot create file " + file.getPath());
        }
        return id;
    }

    @Override
    public boolean exists(int id) {
        return Objects.requireNonNull(root.listFiles((dir, name) -> name.equals(id + ".bmp"))).length > 0;
    }

    @Override
    public File get(int id) throws FileNotFoundException {
        if (!exists(id)) {
            throw new FileNotFoundException();
        }
        return new File(generateFileName(id));
    }

    @Override
    public boolean delete(int id) {
        return new File(generateFileName(id)).delete();
    }

    private String generateFileName(int id) {
        return root.getPath() + File.separator + id + ".bmp";
    }
}

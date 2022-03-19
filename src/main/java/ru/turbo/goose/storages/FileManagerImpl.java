package ru.turbo.goose.storages;

import org.springframework.stereotype.Service;
import ru.turbo.goose.utils.IdGenerator;
import ru.turbo.goose.utils.PathHolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

@Service
public class FileManagerImpl implements FileManager {
    static final String DEFAULT_DATA_DIR = "charta-temp";
    private File root;

    public FileManagerImpl() {
        String path = PathHolder.getPath();
        if (path == null) {
            path = DEFAULT_DATA_DIR;
        }
        root = new File(path);
        root.mkdirs();
    }

    FileManagerImpl(String rootDir) {
        this.root = new File(rootDir);
        root.mkdirs();
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

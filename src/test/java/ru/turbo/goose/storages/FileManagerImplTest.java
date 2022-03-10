package ru.turbo.goose.storages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import ru.turbo.goose.utils.IdGenerator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

class FileManagerImplTest {
    @TempDir
    File tempDir;
    FileManagerImpl manager;

    @BeforeEach
    void setUp() {
        IdGenerator.reset();
        manager = new FileManagerImpl(tempDir.getPath());
    }

    @Nested
    class ConstructorTests {
        @Test
        public void whenConfigFileContainsRequiredPathThenCreateDirThere() throws IOException {
            String path = readRootDirFromConfigFile();
            File rootDir = new File(path);
            new FileManagerImpl();
            assertThat(rootDir.exists(), is(true));
            assertThat(rootDir.isDirectory(), is(true));
            boolean deleted = rootDir.delete();
            assertThat(deleted, is(true));
        }

        @Test
        public void whenConfigFileNotAvailableThenCreateTempDirUsingOS(@TempDir Path temp) throws IOException {
            String pathInConfigFile = readRootDirFromConfigFile();
            Path configSource = Path.of("src", "main", "resources", "app.properties");
            Path configMoved = Files.move(configSource, Path.of(temp.toString(), "app.properties"));
            assertThat(configSource.toFile().exists(), is(false));

            new FileManagerImpl();
            assertThat(new File(pathInConfigFile).exists(), is(false));
            assertThat(new File(FileManagerImpl.DEFAULT_DATA_DIR).exists(), is(false));

            Files.move(configMoved, configSource);
            assertThat(configSource.toFile().exists(), is(true));
        }

        String readRootDirFromConfigFile() throws IOException {
            String configPath = Path.of("src", "main", "resources", "app.properties").toString();
            try (InputStream is = new FileInputStream(configPath)) {
                Properties props = new Properties();
                props.load(is);
                return props.getProperty("dataDirectory");
            }
        }

        @Test
        public void whenConfigFileNotAvailableAndOSTempDirCouldNotBeCreatedThenCreateTempDirInWorkingDirectory(@TempDir Path temp)
                throws IOException {
            String pathInConfigFile = readRootDirFromConfigFile();
            Path configSource = Path.of("src", "main", "resources", "app.properties");
            Path configMoved = Files.move(configSource, Path.of(temp.toString(), "app.properties"));
            assertThat(configSource.toFile().exists(), is(false));

            try (MockedStatic<Files> files = mockStatic(Files.class)) {
                files.when(() -> Files.createTempDirectory(anyString())).thenThrow(IOException.class);
                new FileManagerImpl();
            }
            assertThat(new File(pathInConfigFile).exists(), is(false));
            File defaultDir = new File(FileManagerImpl.DEFAULT_DATA_DIR);
            assertThat(defaultDir.exists(), is(true));
            assertThat(defaultDir.isDirectory(), is(true));
            boolean deleted = defaultDir.delete();
            assertThat(deleted, is(true));

            Files.move(configMoved, configSource);
            assertThat(configSource.toFile().exists(), is(true));
        }
    }

    @Nested
    class CreateMethodTests {
        @Test
        public void whenCreatingOneFile() throws IOException {
            manager.create();
            assertThat(tempDir.listFiles(), is(not(emptyArray())));
        }

        @Test
        public void whenCreatingTwoFiles() throws IOException {
            int id1 = manager.create();
            int id2 = manager.create();
            assertThat(id1, is(not(id2)));
            assertThat(tempDir.listFiles(), is(arrayWithSize(2)));
        }

        @Test
        public void whenCreatingTwoFilesWithIdGeneratorBeingReset() throws IOException {
            int id1 = manager.create();
            IdGenerator.reset();
            int id2 = manager.create();
            assertThat(id1, is(not(id2)));
            assertThat(tempDir.listFiles(), is(arrayWithSize(2)));
        }
    }

    @Nested
    class ExistsMethodTests {
        @Test
        public void whenCallingExistsWithoutCreationThenReturnFalse() {
            boolean created = manager.exists(1);
            assertThat(created, is(false));
        }

        @Test
        public void whenCallingExistsAfterCreationThenReturnTrue() throws IOException {
            int id = manager.create();
            boolean created = manager.exists(id);
            assertThat(created, is(true));
        }
    }

    @Nested
    class GetMethodTests {
        @Test
        public void whenGettingWithoutCreatingThenThrowException() {
            assertThrows(FileNotFoundException.class, () -> manager.get(1));
        }

        @Test
        public void whenGettingAfterCreatingThenReturnFile() throws IOException {
            int id = manager.create();
            File created = manager.get(id);
            assertThat(created.exists(), is(true));
            assertThat(created.isFile(), is(true));
            assertThat(created.getName(), endsWith(".bmp"));
        }
    }

    @Nested
    class DeleteMethodTests {
        @Test
        public void whenDeletingWithoutCreationThenReturnFalse() {
            boolean deleted = manager.delete(1);
            assertThat(deleted, is(false));
        }

        @Test
        public void whenDeletingWithoutCreationThenDeleteFileAndReturnTrue() throws IOException {
            int id = manager.create();
            File file = manager.get(id);
            assertThat(file.exists(), is(true));
            boolean deleted = manager.delete(id);
            assertThat(deleted, is(true));
            assertThat(file.exists(), is(false));
        }
    }
}
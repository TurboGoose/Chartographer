package ru.turbo.goose.storages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.turbo.goose.utils.IdGenerator;
import ru.turbo.goose.utils.PathHolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileManagerImplTest {
    @TempDir
    File tempDir;
    FileManagerImpl manager;

    @BeforeEach
    void setUp() {
        IdGenerator.reset();
        PathHolder.setPath(null);
        manager = new FileManagerImpl(tempDir.getPath());
    }

    @Nested
    class ConstructorTests {
        @Test
        public void whenDirectoryProvidedThenCreateIt(@TempDir File temp) {
            File dir = Path.of(temp.getPath(), "temp").toFile();
            PathHolder.setPath(dir.getPath());
            assertThat(dir.exists(), is(false));
            new FileManagerImpl();
            assertThat(dir.exists(), is(true));
            assertThat(dir.isDirectory(), is(true));
        }

        @Test
        public void whenDirectoryNotProvidedThenCreateDefault() {
            File defaultDir = new File(FileManagerImpl.DEFAULT_DATA_DIR);
            assertThat(defaultDir.exists(), is(false));
            new FileManagerImpl();
            assertThat(defaultDir.exists(), is(true));
            assertThat(defaultDir.isDirectory(), is(true));
            boolean deleted = defaultDir.delete();
            assertThat(deleted, is(true));
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
package ru.turbo.goose.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.turbo.goose.exceptions.ChartaNotExistsException;
import ru.turbo.goose.exceptions.ImagesDoNotIntersectException;
import ru.turbo.goose.exceptions.ServiceException;
import ru.turbo.goose.exceptions.ValidationException;
import ru.turbo.goose.storages.FileManager;
import ru.turbo.goose.utils.ImageFormatConverter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChartaServiceImplTest {
    @TempDir
    File tempDir;
    @Mock
    FileManager fileManager;
    @InjectMocks
    ChartaServiceImpl service;

    final int id = 1;
    final File pic = Path.of("src", "test", "resources", "test.bmp").toFile();

    @Nested
    class CreateMethodTests {
        File target;

        @BeforeEach
        void setUp() throws IOException {
            target = Path.of(tempDir.getPath(), id + ".bmp").toFile();
            target.createNewFile();
        }

        @Test
        public void whenCreatingLittleCharta() throws ServiceException, IOException {
            int w = 100;
            int h = 100;
            when(fileManager.create()).thenReturn(id);
            when(fileManager.get(id)).thenReturn(target);
            int generatedId = service.createCharta(w, h);
            assertThat(generatedId, is(id));
            BufferedImage img = ImageIO.read(target);
            assertThat(img.getWidth(), is(w));
            assertThat(img.getHeight(), is(h));
        }

        @Test
        public void whenWrongValueOfWidthOrHeightPassedThenThrowException() {
            assertThrows(ValidationException.class, () -> service.createCharta(-3, 4));
            assertThrows(ValidationException.class, () -> service.createCharta(3, -4));
            assertThrows(ValidationException.class, () -> service.createCharta(-3, -4));
        }
    }

    @Nested
    class GetSegmentMethodTests {

        @Test
        public void whenGettingSegmentThatFullyInsideTheChartaThenReturnFullSegment()
                throws IOException, ServiceException {
            when(fileManager.exists(id)).thenReturn(true);
            when(fileManager.get(id)).thenReturn(pic);
            int x = 1;
            int y = 1;
            int w = 5;
            int h = 5;
            byte[] bytes = service.getSegment(id, x, y, w, h);
            BufferedImage segment = ImageFormatConverter.byteArrayToBufferedBmpImage(bytes);
            verify(fileManager, atLeast(1)).get(id);
            assertThat(segment.getWidth(), is(w));
            assertThat(segment.getHeight(), is(h));
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    assertThat(new Color(segment.getRGB(i, j)), is(Color.GREEN));
                }
            }
        }

        @Test
        public void whenGettingSegmentThatPartlyIntersectsWithChartaThenReturnThatPartOfSegment()
                throws IOException, ServiceException {
            when(fileManager.exists(id)).thenReturn(true);
            when(fileManager.get(id)).thenReturn(pic);
            int x = -1;
            int y = -1;
            int w = 5;
            int h = 5;
            byte[] bytes = service.getSegment(id, x, y, w, h);
            BufferedImage segment = ImageFormatConverter.byteArrayToBufferedBmpImage(bytes);verify(fileManager, atLeast(1)).get(id);
            assertThat(segment.getWidth(), is(w));
            assertThat(segment.getHeight(), is(h));
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    if (i == 0 || j == 0) {
                        assertThat(new Color(segment.getRGB(i, j)), is(Color.BLACK));
                    } else {
                        assertThat(new Color(segment.getRGB(i, j)), is(Color.GREEN));
                    }
                }
            }
        }

        @Test
        public void whenGettingSegmentThatFullyContainsChartaThenReturnFullCharta()
                throws IOException, ServiceException {
            when(fileManager.exists(id)).thenReturn(true);
            when(fileManager.get(id)).thenReturn(pic);
            int x = -10;
            int y = -10;
            int w = 200;
            int h = 200;
            byte[] bytes = service.getSegment(id, x, y, w, h);
            BufferedImage segment = ImageFormatConverter.byteArrayToBufferedBmpImage(bytes); verify(fileManager, atLeast(1)).get(id);
            assertThat(segment.getWidth(), is(w));
            assertThat(segment.getHeight(), is(h));
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    if (i < 10 || j < 10 || i >= 110 || j >= 110) {
                        assertThat(new Color(segment.getRGB(i, j)), is(Color.BLACK));
                    } else {
                        assertThat(new Color(segment.getRGB(i, j)), is(Color.GREEN));
                    }
                }
            }
        }

        @Test
        public void whenGettingSegmentThatDoesNotIntersectWithChartaAndLocatesInPositiveAreaThenThrowException()
                throws FileNotFoundException {
            when(fileManager.exists(id)).thenReturn(true);
            when(fileManager.get(id)).thenReturn(pic);
            int x = 1000;
            int y = 1000;
            int w = 10;
            int h = 10;
            assertThrows(ImagesDoNotIntersectException.class, () -> service.getSegment(id, x, y, w, h));
        }

        @Test
        public void whenGettingSegmentThatDoesNotIntersectWithChartaAndLocatesInNegativeAreaThenThrowException()
                throws FileNotFoundException {
            when(fileManager.exists(id)).thenReturn(true);
            when(fileManager.get(id)).thenReturn(pic);
            int x = -1000;
            int y = -1000;
            int w = 10;
            int h = 10;
            assertThrows(ImagesDoNotIntersectException.class, () -> service.getSegment(id, x, y, w, h));
        }

        @Test
        public void whenWrongIdPassedThenThrowException() {
            when(fileManager.exists(id)).thenReturn(false);
            assertThrows(ChartaNotExistsException.class, () -> service.getSegment(id, 1, 2, 3, 4));
        }

        @Test
        public void whenWrongValueOfWidthOrHeightPassedThenThrowException() {
            assertThrows(ValidationException.class, () -> service.getSegment(id, 1, 2, -3, 4));
            assertThrows(ValidationException.class, () -> service.getSegment(id, 1, 2, 3, -4));
            assertThrows(ValidationException.class, () -> service.getSegment(id, 1, 2, -3, -4));
        }
    }

    @Nested
    class UpdateSegmentMethodTests {
        File picCopy;

        @BeforeEach
        void setUp() throws IOException {
            picCopy = Files.copy(pic.toPath(), tempDir.toPath(), StandardCopyOption.REPLACE_EXISTING).toFile();
        }

        BufferedImage createRedRect(int w, int h) {
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D gr = image.createGraphics();
            gr.setColor(Color.RED);
            gr.fillRect(0, 0, w, h);
            return image;
        }

        @Test
        public void whenUpdatingSegmentThatFullyInsideChartaThenUpdateImage()
                throws IOException, ServiceException {
            when(fileManager.exists(id)).thenReturn(true);
            when(fileManager.get(id)).thenReturn(picCopy);
            int x = 1;
            int y = 1;
            int w = 5;
            int h = 5;
            byte[] data = ImageFormatConverter.bufferedBmpImageToByteArray(createRedRect(w, h));
            service.updateSegment(id, x, y, w, h, data);
            verify(fileManager, atLeast(1)).get(id);
            int xRed = 1;
            int yRed = 1;
            int wRed = 5;
            int hRed = 5;
            BufferedImage result = ImageIO.read(picCopy);
            for (int i = xRed; i < wRed; i++) {
                for (int j = yRed; j < hRed; j++) {
                    assertThat(new Color(result.getRGB(i, j)), is(Color.RED));
                }
            }
        }

        @Test
        public void whenUpdatingSegmentThatPartlyInsideChartaThenUpdateImage()
                throws IOException, ServiceException {
            when(fileManager.exists(id)).thenReturn(true);
            when(fileManager.get(id)).thenReturn(picCopy);int x = -1;
            int y = -1;
            int w = 5;
            int h = 5;
            byte[] data = ImageFormatConverter.bufferedBmpImageToByteArray(createRedRect(w, h));
            service.updateSegment(id, x, y, w, h, data);
            verify(fileManager, atLeast(1)).get(id);
            int xRed = 0;
            int yRed = 0;
            int wRed = 4;
            int hRed = 4;
            BufferedImage result = ImageIO.read(picCopy);
            for (int i = xRed; i < wRed; i++) {
                for (int j = yRed; j < hRed; j++) {
                    assertThat(new Color(result.getRGB(i, j)), is(Color.RED));
                }
            }
        }

        @Test
        public void whenUpdatingSegmentThatContainsChartaThenUpdateImage()
                throws IOException, ServiceException {
            when(fileManager.exists(id)).thenReturn(true);
            when(fileManager.get(id)).thenReturn(picCopy);
            int x = -10;
            int y = -10;
            int w = 200;
            int h = 200;
            byte[] data = ImageFormatConverter.bufferedBmpImageToByteArray(createRedRect(w, h));
            service.updateSegment(id, x, y, w, h, data);
            verify(fileManager, atLeast(1)).get(id);
            int xRed = 0;
            int yRed = 0;
            int wRed = 100;
            int hRed = 100;
            BufferedImage result = ImageIO.read(picCopy);
            for (int i = xRed; i < wRed; i++) {
                for (int j = yRed; j < hRed; j++) {
                    assertThat(new Color(result.getRGB(i, j)), is(Color.RED));
                }
            }
        }

        @Test
        public void whenUpdatingSegmentThatFullyOutsideCartaThenThrowException()
                throws IOException {
            when(fileManager.exists(id)).thenReturn(true);
            when(fileManager.get(id)).thenReturn(picCopy);
            int x = -10;
            int y = -10;
            int w = 5;
            int h = 5;
            byte[] data = ImageFormatConverter.bufferedBmpImageToByteArray(createRedRect(w, h));
            assertThrows(ImagesDoNotIntersectException.class, () -> service.updateSegment(id, x, y, w, h, data));
            verify(fileManager, atLeast(1)).get(id);
            BufferedImage result = ImageIO.read(picCopy);
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {
                    assertThat(new Color(result.getRGB(i, j)), is(Color.GREEN));
                }
            }
        }

        @Test
        public void whenWrongIdPassedThenThrowException() {
            when(fileManager.exists(id)).thenReturn(false);
            assertThrows(ServiceException.class, () -> service.getSegment(id, 1, 2, 3, 4));
        }

        @Test
        public void whenWrongValueOfWidthOrHeightPassedThenThrowException() {
            assertThrows(ValidationException.class, () -> service.updateSegment(id, 1, 2, -3, 4, new byte[0]));
            assertThrows(ValidationException.class, () -> service.updateSegment(id, 1, 2, 3, -4, new byte[0]));
            assertThrows(ValidationException.class, () -> service.updateSegment(id, 1, 2, -3, -4, new byte[0]));
        }
    }

    @Nested
    class DeleteMethodTests {
        @Test
        public void whenDeletingExistingChartaThenDelegate() throws ServiceException {
            when(fileManager.exists(id)).thenReturn(true);
            service.deleteCharta(id);
            verify(fileManager).delete(id);
        }

        @Test
        public void whenDeletingNonExistingChartaThenThrowException() {
            when(fileManager.exists(id)).thenReturn(false);
            assertThrows(ChartaNotExistsException.class, () -> service.deleteCharta(id));
        }
    }
}
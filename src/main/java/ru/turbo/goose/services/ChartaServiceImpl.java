package ru.turbo.goose.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.turbo.goose.exceptions.ChartaNotExistsException;
import ru.turbo.goose.exceptions.ImagesDoNotIntersectException;
import ru.turbo.goose.exceptions.ServiceException;
import ru.turbo.goose.exceptions.ValidationException;
import ru.turbo.goose.storages.FileManager;
import ru.turbo.goose.utils.BoundaryChecker;
import ru.turbo.goose.utils.ImageFormatConverter;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

@Service
public class ChartaServiceImpl implements ChartaService {
    private final FileManager fileManager;

    @Autowired
    public ChartaServiceImpl(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public int createCharta(int width, int height) throws ServiceException {
        validateWidthAndHeight(width, height);
        try {
            int id = fileManager.create();
            File imageFile = fileManager.get(id);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            fillImageWithBlack(image);
            ImageIO.write(image, "bmp", imageFile);
            return id;
        } catch (IOException exc) {
            throw new ServiceException(exc);
        }
    }

    private void fillImageWithBlack(BufferedImage image) {
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
    }

    @Override
    public byte[] getSegment(int id, int x, int y, int w, int h) throws ServiceException {
        validateWidthAndHeight(w, h);
        validateIdExistence(id);
        try (ImageInputStream input = ImageIO.createImageInputStream(fileManager.get(id))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new ServiceException("No appropriate reader found");
            }
            ImageReader reader = readers.next();
            reader.setInput(input);
            int imageWidth = reader.getWidth(0);
            int imageHeight = reader.getHeight(0);
            Rectangle croppedSegment = BoundaryChecker.intersectInImageCoords(imageWidth, imageHeight, x, y, w, h);
            if (croppedSegment.isEmpty()) {
                throw new ImagesDoNotIntersectException("Regions are not intersecting");
            }
            ImageReadParam param = reader.getDefaultReadParam();
            param.setSourceRegion(croppedSegment);
            BufferedImage segment = reader.read(0, param);
            return ImageFormatConverter.bufferedBmpImageToByteArray(segment);
        } catch (IOException exc) {
            throw new ServiceException(exc);
        }
    }

    @Override
    public void updateSegment(int id, int x, int y, int w, int h, byte[] data) throws ServiceException {
        validateWidthAndHeight(w, h);
        validateIdExistence(id);
        try {
            Dimension dims = readImageDimensions(id);
            Rectangle interSegArea = BoundaryChecker.intersectInSegmentCoords(dims.width, dims.height, x, y, w, h);
            if (interSegArea.isEmpty()) {
                throw new ImagesDoNotIntersectException();
            }
            File target = fileManager.get(id);
            BufferedImage segment = ImageFormatConverter.byteArrayToBufferedBmpImage(data);
            BufferedImage croppedSeg = segment.getSubimage(interSegArea.x, interSegArea.y, interSegArea.width, interSegArea.height);
            BufferedImage image = ImageIO.read(target);
            Graphics2D gr = image.createGraphics();
            Rectangle interImgArea = BoundaryChecker.intersectInImageCoords(dims.width, dims.height, x, y, w, h);
            gr.drawImage(croppedSeg, null, interImgArea.x, interImgArea.y);
            ImageIO.write(image, "bmp", target);
        } catch (IOException exc) {
            throw new ServiceException(exc);
        }
    }

    private Dimension readImageDimensions(int id) throws IOException {
        Dimension dims;
        try (ImageInputStream stream = ImageIO.createImageInputStream(fileManager.get(id))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw new IOException("Cannot read image region");
            }
            ImageReader reader = readers.next();
            reader.setInput(stream);
            dims = new Dimension(reader.getWidth(0), reader.getHeight(0));
        }
        return dims;
    }

    @Override
    public void deleteCharta(int id) throws ServiceException{
        validateIdExistence(id);
        fileManager.delete(id);
    }

    private void validateWidthAndHeight(int w, int h) throws ValidationException {
        if (w <= 0) {
            throw new ValidationException("Wrong input argument: w = " + w + " <= 0");
        }
        if (h <= 0) {
            throw new ValidationException("Wrong input argument: h = " + h + " <= 0");
        }
    }

    private void validateIdExistence(int id) throws ChartaNotExistsException {
        if (!fileManager.exists(id)) {
            throw new ChartaNotExistsException("Charta with id=" + id + " does not exist");
        }
    }
}

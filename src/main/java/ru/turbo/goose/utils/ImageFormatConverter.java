package ru.turbo.goose.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageFormatConverter {
    public static byte[] bufferedBmpImageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "bmp", baos);
        return baos.toByteArray();
    }

    public static BufferedImage byteArrayToBufferedBmpImage(byte[] bytes) throws IOException {
        InputStream is = new ByteArrayInputStream(bytes);
        return ImageIO.read(is);
    }
}

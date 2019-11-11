package ahuber.hubble.utils;

import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Objects;

/**
 * A class containing utility functions for normalizing {@code int} data and writing it as an image.
 */
public final class SatelliteImageWriter {
    /**
     * Normalizes the provided {@code int} array into a {@code byte} array, and writes the resulting bytes into a
     * square greyscale image. This is an O(n) operation.
     * @param data The {@code byte} array.
     * @return A {@link BufferedImage} representing the square greyscale image generated using the data in the input
     * {@code int} array.
     * @throws NullPointerException If the {@code int} array was {@code null}
     * @see Utils#normalize(int[])
     */
    @NotNull
    @PublicApi
    public static BufferedImage writeGreyscaleImage(int[] data) {
        Objects.requireNonNull(data, String.format("array \"%s\" cannot be null.", "data"));
        data = Utils.normalize(data);
        int length = (int) Math.sqrt(data.length);
        BufferedImage image = new BufferedImage(length, length, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = image.getRaster();
        int index = 0;

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                raster.setPixel(j, i, new int[] { data[index++] });
            }
        }

        return image;
    }
}

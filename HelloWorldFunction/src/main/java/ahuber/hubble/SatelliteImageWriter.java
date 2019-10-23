package ahuber.hubble;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Objects;

/**
 * A class containing utility functions for normalizing {@code int} data and writing it as an image.
 */
@SuppressWarnings("WeakerAccess")
public final class SatelliteImageWriter {

    //region Write Greyscale Image

    /**
     * Normalizes the provided {@code int} array into a {@code byte} array, and writes the resulting bytes into a
     * square greyscale image. This is an O(n) operation.
     * @param data The {@code byte} array.
     * @return A {@link BufferedImage} representing the square greyscale image generated using the data in the input
     * {@code int} array.
     * @throws NullPointerException If the {@code int} array was {@code null}
     * @see #normalize(int[])
     * @see #writeGreyscaleImage(byte[])
     */
    @SuppressWarnings("WeakerAccess")
    @NotNull
    public static BufferedImage writeGreyscaleImage(int[] data) {
        return writeGreyscaleImage(normalize(data));
    }

    /**
     * Writes the data contained in the {@code byte} array to a square greyscale image. This is an O(n) operation.
     * @param data The {@code byte} array.
     * @return A {@link BufferedImage} representing the square greyscale image generated using the data in the input
     * {@code byte} array.
     * @throws NullPointerException If the {@code byte} array was {@code null}
     * @see #writeGreyscaleImage(int[])
     */
    @SuppressWarnings("WeakerAccess")
    @NotNull
    public static BufferedImage writeGreyscaleImage(byte[] data) {
        Objects.requireNonNull(data, String.format("The byte array \"%s\" cannot be null.", "data"));
        int length = (int) Math.sqrt(data.length);
        BufferedImage image = new BufferedImage(length, length, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = image.getRaster();
        int index = 0;

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                raster.setPixel(i, j, new int[] { data[index++] });
            }
        }

        return image;
    }
    //endregion

    //region Normalization

    /**
     * Normalizes the provided {@code int} array into a {@code byte} array. This is an O(n) operation.
     * @param data The {@code int} array
     * @return A {@code byte} array where each item is the normalized equivalent of the {@code int} value in the same
     * position as the input {@code int} array.
     * @throws NullPointerException If the {@code int} array is {@code null}.
     * @see #normalize(int)
     */
    @SuppressWarnings("WeakerAccess")
    @NotNull
    public static byte[] normalize(int[] data) {
        Objects.requireNonNull(data, String.format("The int array \"%s\" cannot be null.", "data"));
        byte[] bytes = new byte[data.length];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = normalize(data[i]);
        }

        return bytes;
    }

    /**
     * Normalizes the provided {@code int} value as a {@code byte} value.
     * @param value The {@code int} value.
     * @return The {@code int} value normalized as a {@code byte} value.
     * @see #normalize(int[])
     */
    @SuppressWarnings("WeakerAccess")
    @Contract(pure = true)
    public static byte normalize(int value)
    {
        double minInteger = Integer.MIN_VALUE;
        double maxInteger = Integer.MAX_VALUE;
        return (byte) ((value - minInteger) / (maxInteger - minInteger) * (Byte.MAX_VALUE - Byte.MIN_VALUE) + Byte.MIN_VALUE);
    }
    //endregion
}

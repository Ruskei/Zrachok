package fft;

public class FFT {
    public static void fft(Complex[] data) {
        final int length = data.length;
        if (length == 1) return;
        if (!Math.isPowerOfTwo(length)) throw new IllegalArgumentException();

        for (int chunk = length; chunk >= 2; chunk /= 2) {
            final int half = chunk / 2;
            final double angle = 2.0 * java.lang.Math.PI / chunk;
            final Complex factorStep = new Complex(java.lang.Math.cos(angle), java.lang.Math.sin(angle));

            for (int i = 0; i < length; i += chunk) {
                Complex factor = new Complex(1.0, 0.0);
                for (int j = 0; j < half; j++) {
                    final Complex even = data[i + j];
                    final Complex odd = data[i + j + half];

                    data[i + j] = even.add(odd);
                    data[i + j + half] = even.sub(odd).mul(factor);

                    factor = factor.mul(factorStep);
                }
            }
        }

        for (int i = 1, j = 0; i < length; i++) {
            int bit = length >>> 1;
            while ((j & bit) != 0) {
                j ^= bit;
                bit >>>= 1;
            }
            j ^= bit;

            if (i < j) {
                Complex tmp = data[i];
                data[i] = data[j];
                data[j] = tmp;
            }
        }
    }

    /**
     * Does in-place transformation of data at an offset, with a specified stride
     * between the values it operates on.
     * <p>
     * DFT of
     * c_k = sum_j y_j e^(2 pi i j k / n)
     * <p>
     * Does not perform normalization
     *
     * @param data
     * @param offset
     * @param stride
     * @param length
     */
    private static void rawFFT(
            final Complex[] data,
            final int offset,
            final int stride,
            final int length
    ) {
        if (length == 1) return;
        if (!Math.isPowerOfTwo(length)) throw new IllegalArgumentException();

        for (int chunk = length; chunk >= 2; chunk /= 2) {
            final int half = chunk / 2;
            final double angle = -2.0 * java.lang.Math.PI / chunk;
            final Complex factorStep = new Complex(java.lang.Math.cos(angle), java.lang.Math.sin(angle));

            for (int i = 0; i < length; i += chunk) {
                Complex factor = new Complex(1.0, 0.0);
                for (int j = 0; j < half; j++) {
                    final Complex even = data[offset + stride * (i + j)];
                    final Complex odd = data[offset + stride * (i + j + half)];

                    data[offset + stride * (i + j)] = even.add(odd);
                    data[offset + stride * (i + j + half)] = even.sub(odd).mul(factor);

                    factor = factor.mul(factorStep);
                }
            }
        }

        for (int i = 1, j = 0; i < length; i++) {
            int bit = length >>> 1;
            while ((j & bit) != 0) {
                j ^= bit;
                bit >>>= 1;
            }
            j ^= bit;

            if (i < j) {
                Complex tmp = data[offset + stride * i];
                data[offset + stride * i] = data[offset + stride * j];
                data[offset + stride * j] = tmp;
            }
        }
    }

    /**
     * Expects row-major data
     *
     * @param data
     * @param rows
     * @param columns
     */
    public static void fft2D(Complex[] data, int rows, int columns) {
        if (!Math.isPowerOfTwo(rows)) throw new IllegalArgumentException();
        if (!Math.isPowerOfTwo(columns)) throw new IllegalArgumentException();
        if (data.length != rows * columns) throw new IllegalArgumentException();

        for (int row = 0; row < rows; row++)
            rawFFT(data, row * columns, 1, columns);
        for (int column = 0; column < columns; column++)
            rawFFT(data, column, columns, rows);
    }

    /**
     * Expects row-major data
     *
     * @param data
     * @param rows
     * @param columns
     */
    public static void ifft2D(Complex[] data, int rows, int columns) {
        final int numPoints = rows * columns;
        for (int i = 0; i < numPoints; i++)
            data[i] = data[i].conjugate();

        fft2D(data, rows, columns);

        final double normalization = 1.0 / numPoints;
        for (int i = 0; i < numPoints; i++)
            data[i] = data[i].mul(normalization).conjugate();
    }

    public static void shiftCenter(Complex[] data, int rows, int columns) {
        if (!Math.isPowerOfTwo(rows)) throw new IllegalArgumentException();
        if (!Math.isPowerOfTwo(columns)) throw new IllegalArgumentException();
        if (data.length != rows * columns) throw new IllegalArgumentException();

        final int rowsHalf = rows / 2;
        final int columnsHalf = columns / 2;

        for (int row = 0; row < rowsHalf; row++) {
            final int topBase = row * columns;
            final int bottomBase = (row + rowsHalf) * columns;

            for (int column = 0; column < columnsHalf; column++) {
                final int a = topBase + column;
                final int b = bottomBase + column + columnsHalf;
                Complex tmp = data[a];
                data[a] = data[b];
                data[b] = tmp;

                final int a2 = topBase + column + columnsHalf;
                final int b2 = bottomBase + column;
                tmp = data[a2];
                data[a2] = data[b2];
                data[b2] = tmp;
            }
        }
    }
}

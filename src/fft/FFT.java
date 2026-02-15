package fft;

public class FFT {
    public static void fft(Complex[] data) {
        final var length = data.length;
        if (length == 1) return;
        if (!Math.isPowerOfTwo(length)) throw new IllegalArgumentException();

        for (var chunk = length; chunk >= 2; chunk /= 2) {
            final var half = chunk / 2;
            final var angle = 2.0 * java.lang.Math.PI / chunk;
            final Complex factorStep = new Complex(java.lang.Math.cos(angle), java.lang.Math.sin(angle));

            for (var i = 0; i < length; i += chunk) {
                var factor = new Complex(1.0, 0.0);
                for (var j = 0; j < half; j++) {
                    final var even = data[i + j];
                    final var odd = data[i + j + half];

                    data[i + j] = even.add(odd);
                    data[i + j + half] = even.sub(odd).mul(factor);

                    factor = factor.mul(factorStep);
                }
            }
        }

        for (int i = 1, j = 0; i < length; i++) {
            var bit = length >>> 1;
            while ((j & bit) != 0) {
                j ^= bit;
                bit >>>= 1;
            }
            j ^= bit;

            if (i < j) {
                var tmp = data[i];
                data[i] = data[j];
                data[j] = tmp;
            }
        }
    }

    /**
     * Does in-place transformation of data at an offset, with a specified stride
     * between the values it operates on.
     *
     * @param data
     * @param offset
     * @param stride
     * @param length
     */
    private static void rawFFT(final Complex[] data, final int offset, final int stride, final int length) {
        if (length == 1) return;
        if (!Math.isPowerOfTwo(length)) throw new IllegalArgumentException();

        for (var chunk = length; chunk >= 2; chunk /= 2) {
            final var half = chunk / 2;
            final var angle = 2.0 * java.lang.Math.PI / chunk;
            final Complex factorStep = new Complex(java.lang.Math.cos(angle), java.lang.Math.sin(angle));

            for (var i = 0; i < length; i += chunk) {
                var factor = new Complex(1.0, 0.0);
                for (var j = 0; j < half; j++) {
                    final var even = data[offset + stride * (i + j)];
                    final var odd = data[offset + stride * (i + j + half)];

                    data[offset + stride * (i + j)] = even.add(odd);
                    data[offset + stride * (i + j + half)] = even.sub(odd).mul(factor);

                    factor = factor.mul(factorStep);
                }
            }
        }

        for (int i = 1, j = 0; i < length; i++) {
            var bit = length >>> 1;
            while ((j & bit) != 0) {
                j ^= bit;
                bit >>>= 1;
            }
            j ^= bit;

            if (i < j) {
                var tmp = data[offset + stride * i];
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

        for (var row = 0; row < rows; row++)
            rawFFT(data, row * columns, 1, columns);
        for (var column = 0; column < columns; column++)
            rawFFT(data, column, columns, rows);
    }

    public static void shiftCenter(Complex[] data, int rows, int columns) {
        if (!Math.isPowerOfTwo(rows)) throw new IllegalArgumentException();
        if (!Math.isPowerOfTwo(columns)) throw new IllegalArgumentException();
        if (data.length != rows * columns) throw new IllegalArgumentException();

        final var rowsHalf = rows / 2;
        final int columnsHalf = columns / 2;

        for (var row = 0; row < rowsHalf; row++) {
            final var topBase = row * columns;
            final var bottomBase = (row + rowsHalf) * columns;

            for (var column = 0; column < columnsHalf; column++) {
                final var a = topBase + column;
                final var b = bottomBase + column + columnsHalf;
                var tmp = data[a];
                data[a] = data[b];
                data[b] = tmp;

                final var a2 = topBase + column + columnsHalf;
                final var b2 = bottomBase + column;
                tmp = data[a2];
                data[a2] = data[b2];
                data[b2] = tmp;
            }
        }
    }
}
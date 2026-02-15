import fft.Complex;
import fft.FFT;
import terminal.Color;
import terminal.TerminalDisplay;

public static void main(String[] args) {
    System.out.println("hiya!");

    final int extentX = 30;
    final int extentY = 30;

    var display = new TerminalDisplay(extentX, extentY);
    var colorData = display.colorData;
    colorData[10 * extentX + 10] = new Color(255, 0, 0);

    testFFT();

    testFFT2D();
}

private static void testFFT() {
    final var data = new Complex[]{
            new Complex(1),
            new Complex(-1),
            new Complex(1),
            new Complex(-1),
            new Complex(5),
            new Complex(4),
            new Complex(3),
            new Complex(2),
    };

    final var normalization = Math.sqrt(data.length);

    FFT.fft(data);

    for (var c : data)
        System.out.println(c.divByReal(normalization));
}

private static void testFFT2D() {
    final var rows = 64;
    final var columns = 64;

    final var circleRadius = 12;
    final var centerRow = rows / 2;
    final var centerColumn = columns / 2;

    final var data = new Complex[rows * columns];
    for (var row = 0; row < rows; row++)
        for (var column = 0; column < columns; column++)
            if ((row - centerRow) * (row - centerRow) +
                    (column - centerColumn) * (column - centerColumn)
                    < circleRadius * circleRadius)
                data[row * columns + column] = new Complex(1.0);
            else
                data[row * columns + column] = new Complex(0.0);

    final var start = System.nanoTime();
    FFT.fft2D(data, rows, columns);
    FFT.shiftCenter(data, rows, columns);

    // frequency messing
//    var passRadius = 8;
//    for (var row = 0; row < rows; row++)
//        for (var column = 0; column < columns; column++)
//            if ((row - centerRow) * (row - centerRow) +
//                    (column - centerColumn) * (column - centerColumn)
//                    > passRadius * passRadius)
//                data[row * columns + column] = new Complex(0.0, 0.0);
    
    FFT.fft2D(data, rows, columns);
    final var fftDuration = System.nanoTime() - start;
    System.out.println("Calculations took " + ((double) fftDuration / 1_000_000.0) + " ms");

    final var display = new TerminalDisplay(rows, columns);
    final var colorData = display.colorData;

    double ref = 0.0;
    for (Complex complex : data) ref = Math.max(ref, complex.modulus());
    if (ref == 0.0) ref = 1.0;

    // show with different scaling that makes smaller differences more visible
//    double floorDb = -80.0;
//    for (int r = 0; r < rows; r++)
//        for (int c = 0; c < columns; c++) {
//            double m = data[r * columns + c].modulus();
//            double db = 20.0 * Math.log10((m + 1e-12) / ref);
//            double t = (db - floorDb) / (0.0 - floorDb);
//            t = Math.clamp(t, 0.0, 1.0);
//            int g = (int) Math.round(t * 255.0);
//            colorData[r * columns + c] = new Color(g, g, g);
//        }

    // linear
    for (int r = 0; r < rows; r++)
        for (int c = 0; c < columns; c++) {
            double m = data[r * columns + c].modulus();
            int g = (int) Math.round(m / ref * 255.0);
            colorData[r * columns + c] = new Color(g, g, g);
        }

    display.update();
}
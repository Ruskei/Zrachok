import fft.AngularSpectrumMethod;
import fft.Complex;
import fft.FFT;
import terminal.Color;
import terminal.TerminalDisplay;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public static void main(String[] args) {
    System.out.println("hiya!");

    final int extentX = 30;
    final int extentY = 30;

    var display = new TerminalDisplay(extentX, extentY);
    var colorData = display.colorData;
    colorData[10 * extentX + 10] = new Color(255, 0, 0);

//    testFFT();
//
//    testFFT2D();
    try {

        angularSpectrumTest();
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
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

private static void angularSpectrumTest() throws IOException {
    final var extentX = 1024;
    final var extentY = 1024;

    final var wavelength = 543e-9;
    final var physicalExtentX = 5.6e-3;
    final var physicalExtentY = 5.6e-3;
    final var distance = 0.8;

    final var amplitudeMask = new double[extentX * extentY];

    final var amplitudeMaskFile = new File("hexagon_1024.png");
    final var amplitudeMaskImage = ImageIO.read(amplitudeMaskFile);
    final var amplitudeMaskImageWidth = amplitudeMaskImage.getWidth();
    final var amplitudeMaskImageHeight = amplitudeMaskImage.getHeight();
    if (amplitudeMaskImageWidth != extentX || amplitudeMaskImageHeight != extentY)
        throw new IllegalStateException();

    for (var y = 0; y < extentY; y++)
        for (var x = 0; x < extentX; x++) {
            final var whiteThreshold = 250;

            final var argb = amplitudeMaskImage.getRGB(x, y);
            final var alpha = (argb >>> 24) & 0xFF;
            final var red = (argb >>> 16) & 0xFF;
            final var green = (argb >>> 8) & 0xFF;
            final var blue = argb & 0xFF;

            final var isWhite = alpha > 0 &&
                    red >= whiteThreshold &&
                    green >= whiteThreshold &&
                    blue >= whiteThreshold;
            if (isWhite) amplitudeMask[y * extentX + x] = 1.0;
        }

    final var incidentIntensity = new double[extentX * extentY];
    Arrays.fill(incidentIntensity, 1.0);

    final var observation = AngularSpectrumMethod.process(
            wavelength,
            physicalExtentX, physicalExtentY,
            distance,
            extentX, extentY,
            amplitudeMask,
            incidentIntensity
    );

    outputModulusVisualization("test.png", observation, extentX, extentY);
}

private static void outputModulusVisualization(
        String path,
        Complex[] data,
        int width,
        int height
) {
    double reference = 0.0;
    for (Complex complex : data) reference = Math.max(reference, complex.modulus());
    if (reference == 0.0) reference = 1.0;
   
    System.out.println("peak " + reference);

    final var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    for (var y = 0; y < height; y++)
        for (var x = 0; x < width; x++) {
            final var modulus = data[y * width + x].modulus();
            final var color = (int) (modulus / reference * 255.0);
            image.setRGB(x, y, (0xFF << 24) | (color << 16) | (color << 8) | color);
        }

    try {
        ImageIO.write(image, "png", new File(path));
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
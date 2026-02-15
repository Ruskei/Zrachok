package color;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CIE {
    private static double[] xyz2rgb = {
            3.2406, -1.5372, -0.4986,
            -0.9689, 1.8758, 0.0415,
            0.0557, -0.204, 1.057
    };
    private final List<WavelengthXYZColor> colorMatching;

    private CIE(List<WavelengthXYZColor> colorMatching) {
        this.colorMatching = colorMatching;
    }

    public static CIE parseColorData(File file) throws FileNotFoundException {
        final var scanner = new Scanner(file);
        ;

        final var colorMatching = new ArrayList<WavelengthXYZColor>();

        while (scanner.hasNextLine()) {
            final var line = scanner.nextLine();
            final var split = line.split(" ");
            if (split.length != 4) throw new IllegalStateException();
            final var wavelength = Double.parseDouble(split[0]);
            final var x = Double.parseDouble(split[1]);
            final var y = Double.parseDouble(split[2]);
            final var z = Double.parseDouble(split[3]);

            colorMatching.add(new WavelengthXYZColor(wavelength, x, y, z));
        }

        if (colorMatching.isEmpty()) throw new IllegalStateException();

        return new CIE(colorMatching);
    }

    private XYZColor tristimulus(double wavelength) {
        if (colorMatching.isEmpty()) throw new IllegalStateException();

        final var first = colorMatching.getFirst();
        final var last = colorMatching.getLast();

        if (wavelength < first.wavelength || wavelength > last.wavelength)
            return new XYZColor(0.0, 0.0, 0.0);

        var index = 0;
        while (index < colorMatching.size()
                && colorMatching.get(index).wavelength < wavelength) index++;

        if (index == 0) return new XYZColor(
                colorMatching.get(index).x,
                colorMatching.get(index).y,
                colorMatching.get(index).z
        );

        final var previous = colorMatching.get(index - 1);
        final var next = colorMatching.get(index);
        final var range = next.wavelength - previous.wavelength;
        final var factorPrevious = range - wavelength;
        final var factorNext = 1.0 - factorPrevious;

        return new XYZColor(
                factorPrevious * previous.x + factorNext * next.x,
                factorPrevious * previous.y + factorNext * next.y,
                factorPrevious * previous.z + factorNext * next.z
        );
    }

    public Color calculateColor(
            List<Double> wavelengths,
            List<Double> intensities,
            double reflectance
    ) {
        if (wavelengths.size() != intensities.size()) throw new IllegalArgumentException();

        var x = 0.0;
        var y = 0.0;
        var z = 0.0;

        for (var i = 0; i < wavelengths.size(); i++) {
            final var wavelength = wavelengths.get(i);
            final var intensity = intensities.get(i);
            final var factor = reflectance / Math.PI * intensity;
            final var stimulus = tristimulus(wavelength);

            x += factor * stimulus.x;
            y += factor * stimulus.y;
            z += factor * stimulus.z;
        }

        var linearR = x * xyz2rgb[0] + y * xyz2rgb[1] + z * xyz2rgb[2];
        var linearG = x * xyz2rgb[3] + y * xyz2rgb[4] + z * xyz2rgb[5];
        var linearB = x * xyz2rgb[6] + y * xyz2rgb[7] + z * xyz2rgb[8];

        final var magic1 = 0.00304;
        final var magic2 = 12.92;
        final var magic3 = 1.055;
        final var magic4 = 0.42;
        final var magic5 = 0.055;

        if (linearR <= magic1) linearR *= magic2;
        else linearR = magic3 * Math.pow(linearR, magic4) - magic5;
        if (linearG <= magic1) linearG *= magic2;
        else linearG = magic3 * Math.pow(linearG, magic4) - magic5;
        if (linearB <= magic1) linearB *= magic2;
        else linearB = magic3 * Math.pow(linearB, magic4) - magic5;

        linearR = Math.clamp(linearR, 0.0, 1.0);
        linearG = Math.clamp(linearG, 0.0, 1.0);
        linearB = Math.clamp(linearB, 0.0, 1.0);

        return new Color(
                (int) (linearR * 255.0),
                (int) (linearG * 255.0),
                (int) (linearB * 255.0)
        );
    }

    private value record WavelengthXYZColor(double wavelength, double x, double y, double z) {
    }

    private value record XYZColor(double x, double y, double z) {
    }
}

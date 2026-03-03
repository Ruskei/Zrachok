package color;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
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
        final Scanner scanner = new Scanner(file);
        ;

        final ArrayList<WavelengthXYZColor> colorMatching = new ArrayList<>();

        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            final String[] split = line.split(" ");
            if (split.length != 4)
                throw new IllegalStateException("Split was: " + Arrays.toString(split));
            final double wavelength = Double.parseDouble(split[0]) * 1.0e-9;
            final double x = Double.parseDouble(split[1]);
            final double y = Double.parseDouble(split[2]);
            final double z = Double.parseDouble(split[3]);

            colorMatching.add(new WavelengthXYZColor(wavelength, x, y, z));
        }

        if (colorMatching.isEmpty()) throw new IllegalStateException();

        return new CIE(colorMatching);
    }

    private XYZColor tristimulus(double wavelength) {
        if (colorMatching.isEmpty()) throw new IllegalStateException();

        final WavelengthXYZColor first = colorMatching.getFirst();
        final WavelengthXYZColor last = colorMatching.getLast();

        if (wavelength < first.wavelength || wavelength > last.wavelength)
            return new XYZColor(0.0, 0.0, 0.0);

        int index = 0;
        while (index < colorMatching.size()
                && colorMatching.get(index).wavelength < wavelength) index++;

        if (index == 0) return new XYZColor(
                colorMatching.get(index).x,
                colorMatching.get(index).y,
                colorMatching.get(index).z
        );

        final WavelengthXYZColor previous = colorMatching.get(index - 1);
        final WavelengthXYZColor next = colorMatching.get(index);
        final double range = next.wavelength - previous.wavelength;
        final double factorPrevious = range - wavelength;
        final double factorNext = 1.0 - factorPrevious;

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

        double x = 0.0;
        double y = 0.0;
        double z = 0.0;

        for (int i = 0; i < wavelengths.size(); i++) {
            final double wavelength = wavelengths.get(i);
            final double intensity = intensities.get(i);
            final double factor = reflectance / Math.PI * intensity;
            final XYZColor stimulus = tristimulus(wavelength);

            x += factor * stimulus.x;
            y += factor * stimulus.y;
            z += factor * stimulus.z;
        }

        double linearR = x * xyz2rgb[0] + y * xyz2rgb[1] + z * xyz2rgb[2];
        double linearG = x * xyz2rgb[3] + y * xyz2rgb[4] + z * xyz2rgb[5];
        double linearB = x * xyz2rgb[6] + y * xyz2rgb[7] + z * xyz2rgb[8];

        final double magic1 = 0.00304;
        final double magic2 = 12.92;
        final double magic3 = 1.055;
        final double magic4 = 0.42;
        final double magic5 = 0.055;

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

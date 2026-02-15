package color;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class PolychromaticLight {
    public final List<WavelengthWithIntensity> lightData;

    private PolychromaticLight(List<WavelengthWithIntensity> lightData) {
        this.lightData = lightData;
    }

    public static PolychromaticLight parsePolychromaticData(File file) throws FileNotFoundException {
        final var scanner = new Scanner(file);

        final var lightData = new ArrayList<WavelengthWithIntensity>();

        while (scanner.hasNextLine()) {
            final var line = scanner.nextLine();
            final var split = line.split(" ");
            if (split.length != 2)
                throw new IllegalStateException("Split was: " + Arrays.toString(split));
            final var wavelength = Double.parseDouble(split[0]) * 1.0e-9;
            final var intensity = Double.parseDouble(split[1]);

            lightData.add(new WavelengthWithIntensity(wavelength, intensity));
        }

        return new PolychromaticLight(lightData);
    }

    public value record WavelengthWithIntensity(double wavelength, double intensity) {
    }
}

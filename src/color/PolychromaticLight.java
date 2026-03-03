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
        final Scanner scanner = new Scanner(file);

        final ArrayList<WavelengthWithIntensity> lightData = new ArrayList<>();

        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            final String[] split = line.split(" ");
            if (split.length != 2)
                throw new IllegalStateException("Split was: " + Arrays.toString(split));
            final double wavelength = Double.parseDouble(split[0]) * 1.0e-9;
            final double intensity = Double.parseDouble(split[1]);

            lightData.add(new WavelengthWithIntensity(wavelength, intensity));
        }

        return new PolychromaticLight(lightData);
    }

    public value record WavelengthWithIntensity(double wavelength, double intensity) {
    }
}

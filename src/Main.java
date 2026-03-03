import color.CIE;
import color.PolychromaticLight;
import fft.AngularSpectrumMethod;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        long start = System.nanoTime();
        propagateLight(
                256, 256,
                5.6e-3, 5.6e-3,
                0.8,
                "triangle_256.png",
                "triangle_256_propagated.png"
        );
        long finish = System.nanoTime();
        System.out.println("Fully took " + ((double) (finish - start) / 1_000_000.0) + " ms");
    }

    private static void propagateLight(
            int extentX, int extentY,
            double physicalExtentX, double physicalExtentY,
            double distance,
            String amplitudeMaskPath,
            String saveFilePath
    ) throws IOException {
        System.out.println("polychromatic test started!");
        final double[] amplitudeMask = new double[extentX * extentY];

        final File amplitudeMaskFile = new File(amplitudeMaskPath);
        final BufferedImage amplitudeMaskImage = ImageIO.read(amplitudeMaskFile);
        final int amplitudeMaskImageWidth = amplitudeMaskImage.getWidth();
        final int amplitudeMaskImageHeight = amplitudeMaskImage.getHeight();
        if (amplitudeMaskImageWidth != extentX || amplitudeMaskImageHeight != extentY)
            throw new IllegalStateException();

        for (int y = 0; y < extentY; y++)
            for (int x = 0; x < extentX; x++) {
                final int whiteThreshold = 250;

                final int argb = amplitudeMaskImage.getRGB(x, y);
                final int alpha = (argb >>> 24) & 0xFF;
                final int red = (argb >>> 16) & 0xFF;
                final int green = (argb >>> 8) & 0xFF;
                final int blue = argb & 0xFF;

                final boolean isWhite = alpha > 0 &&
                        red >= whiteThreshold &&
                        green >= whiteThreshold &&
                        blue >= whiteThreshold;
                if (isWhite) amplitudeMask[y * extentX + x] = 1.0;
            }

        long start = System.nanoTime();
        final ArrayList<MonochromaticData> lightData = new ArrayList<>();

        final PolychromaticLight whiteLight = PolychromaticLight.parsePolychromaticData(new File("illuminant_d65.txt"));
        System.out.println("  " + whiteLight.lightData.size() + " wavelengths");
        int count = 0;
        for (PolychromaticLight.WavelengthWithIntensity wavelengthWithIntensity : whiteLight.lightData) {
            final double wavelength = wavelengthWithIntensity.wavelength();
            final double intensity = wavelengthWithIntensity.intensity();

            final double[] incidentIntensity = new double[extentX * extentY];
            Arrays.fill(incidentIntensity, intensity);

            final fft.Complex[] observation = AngularSpectrumMethod.process(
                    wavelength,
                    physicalExtentX, physicalExtentY,
                    distance,
                    extentX, extentY,
                    amplitudeMask,
                    incidentIntensity
            );

            final double[] observationIntensity = new double[observation.length];
            for (int i = 0; i < observation.length; i++)
                observationIntensity[i] = observation[i].modulus();

            lightData.add(new MonochromaticData(wavelength, observationIntensity));

            if (count % 10 == 0)
                System.out.println("  finished " + count + "/" + whiteLight.lightData.size());
            count++;
        }

        long finish = System.nanoTime();
        long duration = finish - start;
        System.out.println("Simulation took " + ((double) duration / 1_000_000.0) + "ms");

        writePhysicalVisualization(saveFilePath, lightData, extentX, extentY);
    }

    private static void writePhysicalVisualization(
            String path,
            List<MonochromaticData> colorData,
            int extentX, int extentY
    ) throws IOException {
        final double reflectance = 3.0;
        final int numPoints = extentX * extentY;
        for (MonochromaticData data : colorData)
            if (data.intensities.length != numPoints)
                throw new IllegalArgumentException();

        final ArrayList<Double> wavelengths = new ArrayList<>();
        for (MonochromaticData data : colorData)
            wavelengths.add(data.wavelength);

        final CIE cie = CIE.parseColorData(new File("cie-cmf.txt"));

        final BufferedImage image = new BufferedImage(extentX, extentY, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < extentY; y++)
            for (int x = 0; x < extentX; x++) {
                final ArrayList<Double> intensities = new ArrayList<>();
                for (MonochromaticData data : colorData)
                    intensities.add(data.intensities[y * extentX + x]);
                final java.awt.Color color =
                        cie.calculateColor(wavelengths, intensities, reflectance);
                image.setRGB(x, y, color.getRGB());
            }

        ImageIO.write(image, "png", new File(path));
    }

    value record MonochromaticData(double wavelength, double[] intensities) {
    }
}

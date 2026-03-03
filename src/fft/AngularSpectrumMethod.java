package fft;

import java.lang.Math;

public class AngularSpectrumMethod {
    /**
     * Propagates field
     *
     * @param wavelength        wavelength of monochromatic light
     * @param extentX           half the x-size of the simulation
     * @param extentY           half the y-size of the simulation
     * @param distance          observation distance from incident
     * @param resolutionX       the number of points used to represent the X direction
     * @param resolutionY       the number of points used to represent the Y direction
     * @param amplitudeMask     incident amplitude mask
     * @param incidentIntensity incident field intensity
     * @return observed field
     */
    public static Complex[] process(
            double wavelength,
            double extentX,
            double extentY,
            double distance,
            int resolutionX,
            int resolutionY,
            double[] amplitudeMask,
            double[] incidentIntensity
    ) {
        final int numPoints = resolutionX * resolutionY;

        if (amplitudeMask.length != numPoints)
            throw new IllegalArgumentException("Amplitude mask size does not match expected resolution");
        if (incidentIntensity.length != numPoints)
            throw new IllegalArgumentException("Incident field intensity size does not match expected resolution");

        final Complex[] incidentField = new Complex[numPoints];
        for (int i = 0; i < numPoints; i++)
            incidentField[i] = new Complex(amplitudeMask[i] * incidentIntensity[i], 0);

        /*
          A(k_x, k_y ; 0) = integral^2 U(x', y', 0) e^(-i(k_x x' + k_y y')) dx' dy'
          where (x', y') is on the z = 0 plane, k_x, k_y are input wavenumbers
          A is angular spectrum, U is disturbance in the z = 0 plane
          
          in the finite discrete world, we have (x', y') is in [-L_x, L_x] x [-L_y, L_y]
          (extentX, Y). x-axis, y-axis is divided into N_x, N_y points (resolutionX, Y)
          
          we will name our indices s_x, s_y; then the physical positions associated
          with these will be x'_s_x, y'_s_y.
          x'_s_x = -L_x + s_x (2 L_x) / N_x, x'_s_y = -L_y + s_y (2 L_y) / N_y
          technically the full s_x, s_y subscript is redundant but it helps with clarity
          
          now we find DFT of our U(x', y', 0)
          frequency : k_x_n_x = n_x (2 pi) / (2 L_x) = (pi n_x) / L_x, same for y
          so the exponent becomes
          (pi n_x) / L_x (-L_x + s_x (2 L_x) / N_x)
          = (pi n_x) (-1 + s_x 2 / N_x)
          = -pi n_x + (2 pi n_x s_x) / N_x
          we ignore the shift in the FFT exponent, making it (2 pi i s_x n_x) / N_x
          since the exponent in our DFT is e^(2 pi i j k / n)
          where j, k, n = s_x, n_x, N_x
          where s_x, n_x are indices, and N_x is resolution, so this perfectly matches
          our DFT form
         */

//        final Complex[] incidentFieldSpectrum = new Complex[numPoints];
//        System.arraycopy(incidentField, 0, incidentFieldSpectrum, 0, numPoints);
//
        FFT.fft2D(incidentField, resolutionY, resolutionX);
        /*
        shift because x'_s_x = -L + s_x (2 L_x) / N_x
         */
        FFT.shiftCenter(incidentField, resolutionY, resolutionX);

        /*
        now to obtain U(x, y, -L), where L is distance to the observation
        screen and x, y are on the observation screen, we have:
        U(x, y, -L) integral integral A(k_x, k_y ; 0) e^(k_z L i) e^(i(k_x x + k_y y)) dk_x dk_y
        discretizing gives us:
        U(x_s_x, y_s_y, -L) = 1/(N_x N_y) sum_(n_x = -N_x/2)^(N_x/2 - 1) sum c(n_x, n_y) e^(i k_z L) e^(i(2 pi n_x) / N_x s_x) e^(i(2 pi n_y) / N_y s_y)
        we're explicitly shifting the boundaries here for clarity.
        k_z^2 = k^2 - k_x^2 - k_y^2
        k_z = sqrt(((2pi)/wavelength)^2 - (pi n_x / L_x)^2 - (pi n_y / L_y)^2)
        we can accomplish this by multiplying by the centered k_z, shifting, then performing
        IFFT like normal
         */

        for (int y = 0; y < resolutionY; y++)
            for (int x = 0; x < resolutionX; x++) {
                final int shiftedX = x - resolutionX / 2;
                final int shiftedY = y - resolutionY / 2;
                final double pi = java.lang.Math.PI;
                final double frequencyZ = Math.sqrt(
                        (4.0 * pi * pi) / wavelength / wavelength -
                                pi * pi * shiftedX * shiftedX / extentX / extentX -
                                pi * pi * shiftedY * shiftedY / extentY / extentY
                );
                final double angle = frequencyZ * distance;
                final Complex factor = new Complex(Math.cos(angle), Math.sin(angle));
                incidentField[y * resolutionX + x] =
                        incidentField[y * resolutionX + x]
                                .mul(factor);
            }

        FFT.shiftCenter(incidentField, resolutionY, resolutionX);
        FFT.ifft2D(incidentField, resolutionY, resolutionX);

        return incidentField;
    }
}

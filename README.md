# Zrachok

This project is a blind implementation, with no code references, of the Angular Spectrum Method described in [this article](https://rafael-fuente.github.io/simulating-diffraction-patterns-with-the-angular-spectrum-method-and-python.html). It simulates how light passes through a patterned screen.

<img width="512" height="512" alt="hexagon_defects_propagated" src="https://github.com/user-attachments/assets/9d531fb4-c592-4b7e-bb04-80254a0cb866" />

<img width="512" height="512" alt="letter_a_propagated" src="https://github.com/user-attachments/assets/cefbeb71-2981-4c13-970f-f86bb7e16c04" />

(Output images)

### Angular Spectrum Method

Diffraction is the phenomenon of light changing as it passes through or near an obstacle. One way to simulate this is with two planes: an obstacle plane, which allows light through in some areas and blocks it in others, and an observation plane, where the resulting diffraction pattern is observed.

This can be modeled by treating light as a plane wave: an infinitely long wave defined by direction, frequency, phase, and magnitude. The simulation then computes how the obstacle affects that wave.

First, we find the spectrum, or frequencies, of light that can pass through the obstacle. The obstacle is represented as a 2D black-and-white image, where each white pixel represents an opening. The core idea is that the spectrum passing through the obstacle must be the Fourier transform of the obstacle image, since those spectral components combine to form the obstacle pattern.

Next, we propagate this spectrum through space by solving the Helmholtz equation, which produces a complex exponential propagation factor. We then take the inverse Fourier transform of the propagated wave and compute the amount of light at each pixel on the observation plane. Repeating this process for many light frequencies approximates white light and produces the demo images shown above.

Further documentation is included in `AngularSpectrumMethod.java`, including details about centering.

The Fourier method is especially visible when using a negative circular obstacle:

<img width="1024" height="1024" alt="spiral_amp_l2_propagated" src="https://github.com/user-attachments/assets/dd52753d-5384-40b7-9dd1-cd45adeba18c" />

This image shows concentric discs, which is the expected result of the Fourier transform of a circle.

### Implementation Details

A Fast Fourier Transform algorithm is implemented in `FFT.java` and used by `AngularSpectrumMethod.java` for the simulation. The file `illuminant_d65.txt` stores some of the frequencies and magnitudes of light in white light. The diffraction images for those frequencies are superimposed to create the final result.

This Java program does not use multithreading, but it is easily parallelizable because each frequency can be computed independently.

### More Images

<img width="1024" height="1024" alt="pentagon_propagated" src="https://github.com/user-attachments/assets/900c7ebc-9500-49f0-af45-3cd2ed7278cf" />

<img width="1024" height="1024" alt="spokes_propagated" src="https://github.com/user-attachments/assets/ccb1aea8-c708-4e1f-9f2a-099c65647439" />

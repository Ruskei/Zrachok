package fft;

import java.lang.Math;

public value record Complex(double real, double imaginary) {
    public Complex(double real) {
        this(real, 0.0);
    }

    public Complex mul(Complex other) {
        // (a + bi) (c + di) = (a c - b d) + (a d + b c)i
        return new Complex(
                real * other.real - imaginary * other.imaginary,
                real * other.imaginary + imaginary * other.real
        );
    }

    public Complex mul(double real) {
        return new Complex(this.real * real, this.imaginary * real);
    }

    public Complex divByReal(double real) {
        return new Complex(this.real / real, this.imaginary / real);
    }

    public Complex add(Complex other) {
        return new Complex(this.real + other.real, this.imaginary + other.imaginary);
    }

    public Complex sub(Complex other) {
        return new Complex(this.real - other.real, this.imaginary - other.imaginary);
    }

    public double modulus() {
        return Math.sqrt(real * real + imaginary * imaginary);
    }
    
    public Complex conjugate() {
        return new Complex(real, -imaginary);
    }
}

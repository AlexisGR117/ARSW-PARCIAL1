package edu.eci.arsw.math;

import java.util.Arrays;

public class CalculatorThread extends Thread {

    private static int DigitsPerSum = 8;
    private static double Epsilon = 1e-17;
    private byte[] digits;
    private int start;
    private int count;
    private final Object lock;
    private boolean state;
    private long startTime;
    private int numberThread;
    private int numberDigits;

    public CalculatorThread(int start, int count, Object lock, int numberThread) {
        this.digits = new byte[count];
        this.start = start;
        this.count = count;
        this.lock = lock;
        this.state = false;
        this.numberThread = numberThread;
        this.numberDigits = 0;
    }

    @Override
    public void run() {
        double sum = 0;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            try {
                stopThread();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            if (i % DigitsPerSum == 0) {
                sum = 4 * sum(1, start)
                        - 2 * sum(4, start)
                        - sum(5, start)
                        - sum(6, start);

                start += DigitsPerSum;
            }
            sum = 16 * (sum - Math.floor(sum));
            digits[i] = (byte) sum;
            numberDigits ++;
        }
        state = true;
    }

    private void stopThread() throws InterruptedException {
        synchronized (lock) {
            while (System.currentTimeMillis() - startTime >= 5000) {
                System.out.println("Cantidad de digitos calculados por el hilo "+ numberThread + ": " + numberDigits);
                lock.wait();
            }
        }
    }

    public void resetTimer() {
        startTime = System.currentTimeMillis();
    }

    public byte[] getDigits() {
        return digits;
    }

    public boolean getStateThread() {
        return state;
    }

    /// <summary>
    /// Returns the sum of 16^(n - k)/(8 * k + m) from 0 to k.
    /// </summary>
    /// <param name="m"></param>
    /// <param name="n"></param>
    /// <returns></returns>
    private static double sum(int m, int n) {
        double sum = 0;
        int d = m;
        int power = n;

        while (true) {
            double term;

            if (power > 0) {
                term = (double) hexExponentModulo(power, d) / d;
            } else {
                term = Math.pow(16, power) / d;
                if (term < Epsilon) {
                    break;
                }
            }

            sum += term;
            power--;
            d += 8;
        }

        return sum;
    }

    /// <summary>
    /// Return 16^p mod m.
    /// </summary>
    /// <param name="p"></param>
    /// <param name="m"></param>
    /// <returns></returns>
    private static int hexExponentModulo(int p, int m) {
        int power = 1;
        while (power * 2 <= p) {
            power *= 2;
        }

        int result = 1;

        while (power > 0) {
            if (p >= power) {
                result *= 16;
                result %= m;
                p -= power;
            }

            power /= 2;

            if (power > 0) {
                result *= result;
                result %= m;
            }
        }

        return result;
    }

    public int getStart() {
        return start;
    }

    public int getCount() {
        return count;
    }

    public int getNumberThread() {
        return numberThread;
    }

    public int getNumberDigits() {
        return numberDigits;
    }
}

package edu.eci.arsw.math;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

///  <summary>
///  An implementation of the Bailey-Borwein-Plouffe formula for calculating hexadecimal
///  digits of pi.
///  https://en.wikipedia.org/wiki/Bailey%E2%80%93Borwein%E2%80%93Plouffe_formula
///  *** Translated from C# code: https://github.com/mmoroney/DigitsOfPi ***
///  </summary>
public class PiDigits {

    private static final Object lock = new Object();

    
    /**
     * Returns a range of hexadecimal digits of pi.
     * @param start The starting location of the range.
     * @param count The number of digits to return
     * @return An array containing the hexadecimal digits.
     */
    public static byte[] getDigits(int start, int count, int N) {
        if (start < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        if (count < 0) {
            throw new RuntimeException("Invalid Interval");
        }
        byte[] digits = new byte[count];

        ArrayList<CalculatorThread> calculatorThreads = new ArrayList<>();
        int countThread = count/N;
        for (int i = 0; i < N; i++) {
            if (i < N - 1) calculatorThreads.add(new CalculatorThread(start + i*countThread, countThread, lock, i));
            else calculatorThreads.add(new CalculatorThread(start + i*countThread, count - i*countThread, lock, i));
        }
        for (CalculatorThread calculatorThread: calculatorThreads) calculatorThread.start();
        while (!calculatorThreads.get(0).getStateThread()) {
            try {

                Thread.sleep(5000);
                (new Scanner(System.in)).nextLine();
                synchronized (lock) {
                    lock.notifyAll();
                }
                for (CalculatorThread calculatorThread: calculatorThreads) calculatorThread.resetTimer();

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        for (CalculatorThread calculatorThread: calculatorThreads) {
            try {
                calculatorThread.join();
                int initialIndex = calculatorThread.getNumberThread() * countThread;
                byte[] addDigits = calculatorThread.getDigits();
                for (int i = 0; i < addDigits.length; i++) digits[initialIndex + i] = addDigits[i];
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return digits;
    }

}

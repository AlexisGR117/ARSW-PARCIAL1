package edu.eci.arsw.math;

import java.util.Scanner;

///  <summary>
///  An implementation of the Bailey-Borwein-Plouffe formula for calculating hexadecimal
///  digits of pi.
///  https://en.wikipedia.org/wiki/Bailey%E2%80%93Borwein%E2%80%93Plouffe_formula
///  *** Translated from C# code: https://github.com/mmoroney/DigitsOfPi ***
///  </summary>
public class PiDigits {

    // Objeto que servirá como monitor para poder detener la ejecución de los hilos y reanudarla posteriormente
    private static final Object lock = new Object();

    /**
     * Returns a range of hexadecimal digits of pi.
     *
     * @param start The starting location of the range.
     * @param count The number of digits to return
     * @return An array containing the hexadecimal digits.
     */
    public static byte[] getDigits(int start, int count, int N) {
        if (start < 0) throw new RuntimeException("Invalid Interval");
        if (count < 0) throw new RuntimeException("Invalid Interval");
        // Se crean todos los hilos y se guardan en un array.
        CalculatorThread[] calculatorThreads = createCalculatorThreads(N, start, count);
        // Se inician todos los hilos
        for (CalculatorThread calculatorThread : calculatorThreads) calculatorThread.start();
        // Se pausan los hilos cada 5 segundos hasta que terminen su ejecución.
        stopCalculatorThreads(calculatorThreads);
        // Se consolida los dígitos obtenidos por los distintos hilos en un solo array.
        return consolidateDigits(calculatorThreads, count);
    }

    /*
    Crea los hilos que se usarán para calcular los dígitos, se da el número de hilos que se crearán, el índice inicial
    desde el que se comenzarán a calcular los dígitos de pi y la cantidad de dígitos que se calcularán.
     */
    private static CalculatorThread[] createCalculatorThreads(int numberOfThreads, int start, int count) {
        // Se crea un Array en el que se guardaran los hilos que se crearán.
        CalculatorThread[] calculatorThreads = new CalculatorThread[numberOfThreads];
        // countThread es la cantidad de dígitos que tendrá que calcular cada hilo.
        int countThread = count / numberOfThreads;
        // Ciclo for para crear los hilos
        for (int i = 0; i < numberOfThreads; i++) {
            // Si es el último hilo que se va a crear el número de dígitos que tendrá que calcular será
            // count - i * countThread, esto con el fin de que el residuo de que en caso de que la división no sea exacta
            // se incluya el residuo en el último hilo.
            if (i < numberOfThreads - 1) {
                calculatorThreads[i] = new CalculatorThread(start + i * countThread, countThread, lock);
            } else {
                calculatorThreads[i] = new CalculatorThread(start + i * countThread, count - i * countThread, lock);
            }
        }
        return calculatorThreads;
    }

    /*
    Detiene los hilos cada 5 segundos y los vuelve a reanudar una vez que se haya oprimido la tecla enter.
     */
    private static void stopCalculatorThreads(CalculatorThread[] calculatorThreads) {
        int numberOfThreads = calculatorThreads.length;
        // Este ciclo se ejecutará mientras que el último hilo que esté en ejecución.
        while (calculatorThreads[numberOfThreads - 1].isRunning()) {
            try {
                // El hilo "coordinador" dormirá por 5 segundos, para no generar una espera activa.
                Thread.sleep(5000);
                // Una vez se despierte en el caso de que aún no haya finalizado el último hilo, se esperará a que
                // se presione la tecla enter para que los hilos continúen su proceso.
                if (calculatorThreads[numberOfThreads - 1].isRunning()) {
                    // Se cambia el estado de los hilos a 1 (waiting) para que se detengan.
                    for (CalculatorThread calculatorThread : calculatorThreads) calculatorThread.changeState(1);
                    (new Scanner(System.in)).nextLine();
                    // Cuando se presiona la tecla enter se cambia el estado de los hilos a 0 (running) y
                    // se despiertan a todos los hilos.
                    for (CalculatorThread calculatorThread : calculatorThreads) calculatorThread.changeState(0);
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    /*
    Con todos los hilos terminados se obtienen los dígitos calculados paro cada uno y se consolidan en un solo array
    de bytes, esto se hace con su posición dentro del array y la cantidad de dígitos que debía calcular cada hilo.
     */
    private static byte[] consolidateDigits(CalculatorThread[] calculatorThreads, int count) {
        int numberOfThreads = calculatorThreads.length;
        byte[] digits = new byte[count];
        for (int i = 0; i < numberOfThreads; i++) {
            try {
                // Se verifica que el hilo haya terminado su ejecución
                calculatorThreads[i].join();
                // Cálculo del índice inicial correspondiente al hilo.
                int initialIndex = i * (count / numberOfThreads);
                // Se obtienen todos los dígitos que cálculo el hilo.
                byte[] addDigits = calculatorThreads[i].getDigits();
                // Se agregan los dígitos al array que contendrá la consolidación de todos los dígitos calculados por
                // todos los hilos.
                System.arraycopy(addDigits, 0, digits, initialIndex, addDigits.length);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
        return digits;
    }
}


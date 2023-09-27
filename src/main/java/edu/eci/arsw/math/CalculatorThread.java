package edu.eci.arsw.math;

public class CalculatorThread extends Thread {

    private static final int DigitsPerSum = 8;
    private static final double Epsilon = 1e-17;
    private final Object lock;
    private final byte[] digits;
    private final int count;
    private int start;
    private int state;
    private int numberDigits;

    /**
     * Crea un objeto de clase CalculatorThread.
     *
     * @param start Índice inicial desde el cual se obtendrán los dígitos que le corresponden al hilo.
     * @param count Cantidad de dígitos que va a calcular el hilo.
     * @param lock  Objeto que servirá como monitor para poder detener la ejecución del hilo y reanudarla posteriormente.
     */
    public CalculatorThread(int start, int count, Object lock) {
        this.start = start;
        this.count = count;
        this.lock = lock;
        // Esta variable índica el estado del hilo, 0 representa running, 1 waiting y 2 terminated.
        this.state = 0;
        // Cantidad de dígitos que se han calculado
        this.numberDigits = 0;
        // Array donde están guardados los dígitos calculados.
        this.digits = new byte[count];
    }


    @Override
    public void run() {
        // Cálculo de los dígitos que le corresponden al hilo, este se hace de la misma manera que estaba en PiDigits
        // (se pasaron los métodos que realizaban el cálculo a esta clase).
        double sum = 0;
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
            // Cuando se finaliza el cálculo correspondiente para cada dígito se guarda en la matriz digits,
            // en su respectiva posición.
            digits[i] = (byte) sum;
            // Se aumenta el número de dígitos calculados por el hilo.
            numberDigits++;
        }
        // Al finalizar el cálculo de todos los números que le correspondían al hilo, se cambia el valor de state
        // a 2, lo cual representa que ya terminó su ejecución.
        state = 2;
    }

    /*
    Método que se ejecuta en cada iteración del ciclo for, este pregunta si la variable state del hilo es 1 (waiting),
    si esto pasa imprime la cantidad de números calculados hasta el momento por el hilo y
    lo duerme con lock.wait()
     */
    private void stopThread() throws InterruptedException {
        synchronized (lock) {
            while (state == 1) {
                System.out.println("Cantidad de dígitos calculados por " + Thread.currentThread().getName() + ": " + numberDigits);
                lock.wait();
            }
        }
    }

    public byte[] getDigits() {
        return digits;
    }

    public void changeState(int state) {
        this.state = state;
    }

    public boolean isRunning() {
        return state == 0;
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
}

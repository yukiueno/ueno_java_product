import java.math.BigDecimal;
import java.math.RoundingMode;

public class HiddenMarkovModel {
    static double output_T = 0.2;
    static double output_A = 0.2;
    static double output_C = 0.3;
    static double output_G = 0.3;
    static double initial_probability_a0K = 0.5;
    static double last_probability_aLL = 0.5;

    /*
    static double[][] a =
    {   // "A"  "T"  "G"  "C"
        { 0.2, 0.3, 0.1, 0.4 }, // "A"
        { 0.3, 0.1, 0.5, 0.1 }, // "T"
        { 0.2, 0.2, 0.4, 0.3 }, // "G"
        { 0.2, 0.1, 0.2, 0.5 }, // "C"
    };
    */

    static double[][] a =
        {   // "H"  "L"
            { 0.4, 0.6 }, // "H"
            { 0.5, 0.5 }, // "L"
        };

    static double[][] output =
        {   // "A"  "T"  "G"  "C"
            { 0.2, 0.2, 0.3, 0.3 }, // "H"
            { 0.3, 0.3, 0.2, 0.2 }, // "L"
        };

    public static void main (String[] args) {
        String[] sequence = SetSequence.getSequenceByOption(args);
        for (int i = 0; i < sequence.length; i++) {
            System.out.print(sequence[i]);
        }
        System.out.println();

        double[][] f = initializeForwardVariable(sequence);
        f = forward(sequence, f);
        printMatrix(f);
        System.out.println();

        double[][] b = initializeBackwardVariable(sequence);
        b = backward(sequence, b);
        printMatrix(b);
        System.out.println();
    }

    public static double[][] initializeForwardVariable( String[] sequence  ) {
        double[][] f = new double[a.length][sequence.length];
        for (int k = 0; k < a.length; k++) {
           if (sequence[1].equals("A")) {
                f[k][0] = Math.log10(output_A) + Math.log10(initial_probability_a0K);
            } else if (sequence[1].equals("T")) {
                f[k][0] = Math.log10(output_T) + Math.log10(initial_probability_a0K);
            } else if (sequence[1].equals("G")) {
                f[k][0] = Math.log10(output_G) + Math.log10(initial_probability_a0K);
            } else if (sequence[1].equals("C")) {
                f[k][0] = Math.log10(output_C) + Math.log10(initial_probability_a0K);
            } else {
                f[k][0] = Math.log10(output_A) + Math.log10(initial_probability_a0K);
            }
        }
        return f;
    }

     public static double[][] initializeBackwardVariable( String[] sequence  ) {
        double[][] b = new double[a.length][sequence.length];
        for (int k = 0; k < a.length; k++) {
           if (sequence[sequence.length - 1].equals("A")) {
                b[k][sequence.length - 1] = Math.log10(output_A) + Math.log10(initial_probability_a0K);
            } else if (sequence[sequence.length - 1].equals("T")) {
                b[k][sequence.length - 1] = Math.log10(output_T) + Math.log10(initial_probability_a0K);
            } else if (sequence[sequence.length - 1].equals("G")) {
                b[k][sequence.length - 1] = Math.log10(output_G) + Math.log10(initial_probability_a0K);
            } else if (sequence[sequence.length - 1].equals("C")) {
                b[k][sequence.length - 1] = Math.log10(output_C) + Math.log10(initial_probability_a0K);
            } else {
                b[k][sequence.length - 1] = Math.log10(output_A) + Math.log10(initial_probability_a0K);
            }
        }
        return b;
    }

    public static double[][] forward(String[] sequence, double[][] f) {
        for (int i = 1; i < sequence.length; i++) {
            for (int k = 0; k < a.length; k++) {
                if (sequence[i].equals("A")) {
                    f[k][i] = Math.log10(output[k][0]) + makeForwardProbability(f, i, k);
                } else if (sequence[i].equals("T")) {
                    f[k][i] = Math.log10(output[k][1]) + makeForwardProbability(f, i, k);
                } else if (sequence[i].equals("G")) {
                    f[k][i] = Math.log10(output[k][2]) + makeForwardProbability(f, i, k);
                } else if (sequence[i].equals("C")) {
                    f[k][i] = Math.log10(output[k][3]) + makeForwardProbability(f, i, k);
                } else {
                    f[k][i] = Math.log10(output[k][0]) + makeForwardProbability(f, i, k);
                }
            }
        }
        return f;
    }

    public static double[][] backward(String[] sequence, double[][] b) {
        for (int i = sequence.length -  2; i > 0; i--) {
            for (int k = 0; k < a.length; k++) {
                if (sequence[i].equals("A")) {
                    b[k][i] = Math.log10(output[k][0]) + makeBackwardProbability(b, i, k);
                } else if (sequence[i].equals("T")) {
                    b[k][i] = Math.log10(output[k][1]) + makeBackwardProbability(b, i, k);
                } else if (sequence[i].equals("G")) {
                    b[k][i] = Math.log10(output[k][2]) + makeBackwardProbability(b, i, k);
                } else if (sequence[i].equals("C")) {
                    b[k][i] = Math.log10(output[k][3]) + makeBackwardProbability(b, i, k);
                } else {
                    b[k][i] = Math.log10(output[k][0]) + makeBackwardProbability(b, i, k);
                }
            }
        }
        return b;
    }

    public static double makeForwardProbability(double[][] f, int i, int k) {
        double sum = 0;
        for (int l = 0; l < a.length; l++) {
            f[k][i] = f[l][i-1] + Math.log10(a[l][k]);
            sum += f[k][i];
        }
        return sum;
    }

    public static double makeBackwardProbability(double[][] b, int i, int k) {
        double sum = 0;
        for (int l = 0; l < a.length; l++) {
            b[k][i] = b[l][i+1] + Math.log10(a[l][k]);
            sum += b[k][i];
        }
        return sum;
    }

    public static void printMatrix(double[][] Matrix) {
        System.out.print("\n");
        for(int i = 0;i < Matrix.length; i++){
            for(int j = 0;j < Matrix[0].length; j++){
               BigDecimal L01 = new BigDecimal(Matrix[i][j]);
               System.out.print(L01.setScale(1, RoundingMode.CEILING) + "\t");
            }
            System.out.print("\n");
        }
    }
}

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MarkovModel {
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

    static int[] yazirusi = {};

    public static void main (String[] args) {
        String[] sequence = SetSequence.getSequenceByOption(args);
        for (int i = 0; i < sequence.length; i++) {
            System.out.print(sequence[i]);
        }
        System.out.println();
        double[][] v = initializeViterbi(sequence);
        v = viterbi(sequence, v);

        for (int i = 0; i < sequence.length; i++) {
            System.out.print(sequence[i] + "\t");
        }

        printMatrix(v);
        System.out.println();
        for (int i = 1; i < sequence.length; i++) {
            System.out.print(yazirusi[i] + "\t");
        }
        System.out.println();
    }

    public static double[][] initializeViterbi(String[] sequence) {
        double[][] v = new double[a.length][sequence.length];
        for (int k = 0; k < a.length; k++) {
            if (sequence[1].equals("A")) {
                v[k][0] = Math.log10(output_A) + Math.log10(initial_probability_a0K);
            } else if (sequence[1].equals("T")) {
                v[k][0] = Math.log10(output_T) + Math.log10(initial_probability_a0K);
            } else if (sequence[1].equals("G")) {
                v[k][0] = Math.log10(output_G) + Math.log10(initial_probability_a0K);
            } else if (sequence[1].equals("C")) {
                v[k][0] = Math.log10(output_C) + Math.log10(initial_probability_a0K);
            } else {
                v[k][0] = Math.log10(output_A) + Math.log10(initial_probability_a0K);
            }
        }
        return v;
    }

    public static double[][] viterbi(String[] sequence, double[][] v) {
        yazirusi = new int[sequence.length];
        for (int i = 1; i < sequence.length; i++) {
            for (int k = 0; k < a.length; k++) {
                if (sequence[i].equals("A")) {
                    v[k][i] = Math.log10(output[k][0]) + viterbiAlgorism(v, i, k);
                } else if (sequence[i].equals("T")) {
                    v[k][i] = Math.log10(output[k][1]) + viterbiAlgorism(v, i, k);
                } else if (sequence[i].equals("G")) {
                    v[k][i] = Math.log10(output[k][2]) + viterbiAlgorism(v, i, k);
                } else if (sequence[i].equals("C")) {
                    v[k][i] = Math.log10(output[k][3]) + viterbiAlgorism(v, i, k);
                } else {
                    v[k][i] = Math.log10(output[k][0]) + viterbiAlgorism(v, i, k);
                }
            }
        }
        return v;
    }

    public static double viterbiAlgorism(double[][] v, int i, int k) {
        double maxV = -1000000000;
        for (int l = 0; l < a.length; l++) {
            if (maxV < v[k][i-1] + Math.log10(a[l][k])) {
                v[k][i] = v[l][i-1] + Math.log10(a[l][k]);
                maxV = v[k][i];
                yazirusi[i] = l;
            }
        }
        return maxV;
    }


    public static void printMatrix(double[][] Matrix) {
        System.out.print("\n");
        for(int i = 0;i < Matrix.length; i++){
            for(int j = 0;j < Matrix[0].length; j++){
               BigDecimal L01 = new BigDecimal(Matrix[i][j]);
               System.out.print(L01.setScale(3, RoundingMode.CEILING) + "\t");
            }
            System.out.print("\n");
        }
    }
}

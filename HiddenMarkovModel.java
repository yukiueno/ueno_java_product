import java.math.BigDecimal;
import java.math.RoundingMode;

public class HiddenMarkovModel {

    static int stetesNum = 2;
    static int number_of_emission_types = 4;
    static double[] scaling_coefficient;
    static double[] primaryStateProbability = new double[number_of_emission_types];

    static double[][] transition_probabilities =
        {   // "H"  "L"
            { 0.1, 0.9 }, // "H"
            { 0.6, 0.4 }, // "L"
        };

    static double[][] emission_probabilities =
        {   // "A"  "T"  "G"  "C"
            { 0.1, 0.2, 0.4, 0.3 }, // "H"
            { 0.5, 0.1, 0.2, 0.2 }, // "L"
        };

    public static void main (String[] args) {
        String[] sequence = SetSequence.getSequenceByOption(args);
        for (int i = 0; i < sequence.length; i++) {
            System.out.print(sequence[i]);
        }

        System.out.println();
        baum_welch_algorithm( sequence );
        System.out.println();
    }

    public static double[][] baum_welch_algorithm( String[] sequence ) {
        int t = 0;
        do {
            double[][] f = culculateForwardVariables(sequence);
            double[][] b = culculateBackwardVariables(sequence);

            double[][] Akl = new double[stetesNum][stetesNum];
            for(int k = 0; k < stetesNum; k++) {
                double sigmaAkl = 0;
                for(int l = 0; l < stetesNum; l++) {
                    double intermediate_transition_probability = 0;
                    for(int i = 1; i < sequence.length - 1; i++ ) {
                        if (sequence[i].equals("A")) {
                            intermediate_transition_probability += f[k][i] * transition_probabilities[k][l] * emission_probabilities[l][0] * b[l][i + 1] / scaling_coefficient[i + 1];
                        } else if (sequence[i].equals("T")) {
                            intermediate_transition_probability += f[k][i] * transition_probabilities[k][l] * emission_probabilities[l][1] * b[l][i + 1] / scaling_coefficient[i + 1];
                        } else if (sequence[i].equals("G")) {
                            intermediate_transition_probability += f[k][i] * transition_probabilities[k][l] * emission_probabilities[l][2] * b[l][i + 1] / scaling_coefficient[i + 1];
                        } else if (sequence[i].equals("C")) {
                            intermediate_transition_probability += f[k][i] * transition_probabilities[k][l] * emission_probabilities[l][3] * b[l][i + 1] / scaling_coefficient[i + 1];
                        }
                    }
                    Akl[k][l] = intermediate_transition_probability;
                    sigmaAkl += intermediate_transition_probability;
                }
                for(int l = 0; l < stetesNum; l++) {
                    transition_probabilities[k][l] = Akl[k][l] / sigmaAkl;
                }
            }

            for(int k = 0; k < stetesNum; k++) {
               double sigmaEkx = 0;
               double[] Ekx = new double[number_of_emission_types];
               for (int x = 0; x < number_of_emission_types; x++) Ekx[x] = 0;
               for(int i = 1; i < sequence.length; i++ ) {
                    if (sequence[i].equals("A")) {
                        Ekx[0] += f[k][i] * b[k][i];
                    } else if (sequence[i].equals("T")) {
                        Ekx[1] += f[k][i] * b[k][i];
                    } else if (sequence[i].equals("G")) {
                        Ekx[2] += f[k][i] * b[k][i];
                    } else if (sequence[i].equals("C")) {
                        Ekx[3]+= f[k][i] * b[k][i];
                    } else {
                        Ekx[0] += f[k][i] * b[k][i];
                    }
                    sigmaEkx += f[k][i] * b[k][i];
                }
                for (int x = 0; x < number_of_emission_types; x++) {
                    if(Ekx[x] != 0){
                        emission_probabilities[k][x] = Ekx[x] / sigmaEkx;
                    } else {
                        emission_probabilities[k][x] = Ekx[x];
                    }
                }
            }
            t++;
        } while ( Math.log10(scaling_coefficient[t]) == Math.log10(scaling_coefficient[t - 1]) );

        System.out.println("\nTransition");
        printMatrix(transition_probabilities);
        System.out.println("\nEmission");
        printMatrix(emission_probabilities);

        return transition_probabilities;
    }

    public static double[][] initializeForwardVariables( String[] sequence  ) {
        double[][] f = new double[stetesNum][sequence.length];
        for (int k = 0; k < stetesNum; k++) {
            f[k][0] = 1;
            for (int i = 1; i < sequence.length; i++) {
                f[k][i] = 0;
            }
        }
        return f;
    }

     public static double[][] initializeBackwardVariables( String[] sequence  ) {
        double[][] b = new double[stetesNum][sequence.length];
        for (int k = 0; k < stetesNum; k++) {
            b[k][sequence.length - 1] = 1;
            for (int i = sequence.length - 2; i > 0; i--) {
                b[k][i] = 0;
            }
        }
        return b;
    }

    public static double[][] culculateForwardVariables( String[] sequence ) {
        double[][] f = initializeForwardVariables( sequence );
        scaling_coefficient = new double[sequence.length];
        for (int i = 1; i < sequence.length; i++) {
            scaling_coefficient[i] = 0;
            for (int k = 0; k < stetesNum; k++) {
                for (int l = 0; l < stetesNum; l++) {
                    if (sequence[i].equals("A")) {
                        f[k][i] += emission_probabilities[k][0] * f[l][i-1] * transition_probabilities[l][k];
                    } else if (sequence[i].equals("T")) {
                        f[k][i] += emission_probabilities[k][1] * f[l][i-1] * transition_probabilities[l][k];
                    } else if (sequence[i].equals("G")) {
                        f[k][i] += emission_probabilities[k][2] * f[l][i-1] * transition_probabilities[l][k];
                    } else if (sequence[i].equals("C")) {
                        f[k][i] += emission_probabilities[k][3] * f[l][i-1] * transition_probabilities[l][k];
                    } else {
                        f[k][i] += emission_probabilities[k][0] * f[l][i-1] * transition_probabilities[l][k];
                    }
                    scaling_coefficient[i] += f[l][i];
                }
                f[k][i] = f[k][i] / scaling_coefficient[i];
            }
        }
        return f;
    }

    public static double[][] culculateBackwardVariables(String[] sequence ) {
        double[][] b = initializeBackwardVariables( sequence );
        for (int i = sequence.length - 2 ; i > 0; i--) {
            double sum = 0;
            for (int k = 0; k < stetesNum; k++) {
                for (int l = 0; l < stetesNum; l++) {
                    if (sequence[i].equals("A")) {
                        b[k][i] += emission_probabilities[k][0] * b[l][i+1] * transition_probabilities[l][k];
                    } else if (sequence[i].equals("T")) {
                        b[k][i] += emission_probabilities[k][1] * b[l][i+1] * transition_probabilities[l][k];
                    } else if (sequence[i].equals("G")) {
                        b[k][i] += emission_probabilities[k][2] * b[l][i+1] * transition_probabilities[l][k];
                    } else if (sequence[i].equals("C")) {
                        b[k][i] += emission_probabilities[k][3] * b[l][i+1] * transition_probabilities[l][k];
                    } else {
                        b[k][i] += emission_probabilities[k][0] * b[l][i+1] * transition_probabilities[l][k];
                    }
                    sum += b[k][i];
                }
                b[k][i] = b[k][i] / sum;
            }
        }
        return b;
    }

    public static void printMatrix(double[][] Matrix) {
        System.out.print("\n");
        for(int i = 0;i < Matrix.length; i++){
            for(int j = 0;j < Matrix[0].length; j++){
               //BigDecimal L01 = new BigDecimal(Matrix[i][j]);
               System.out.print(/*L01.setScale(5, RoundingMode.CEILING)*/Matrix[i][j] + "\t");
            }
            System.out.print("\n");
        }
    }
}

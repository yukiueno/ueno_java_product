import java.math.BigDecimal;
import java.math.RoundingMode;

public class HMMforFLDice {

    static int stetesNum = 2;
    static int number_of_emission_types = 6;
    static double[] scaling_coefficient;
    static double[][] bb;

    static double[][] transition_probabilities =
        {   // "F"  "L"
            { 0.5, 0.5 }, // "F"
            { 0.5, 0.5 }, // "L"
        };

    static double[][] emission_probabilities =
        {   // 1,   2,   3,   4,   5,   6
            { 0.1, 0.2, 0.2, 0.2, 0.1, 0.2 }, // "F"
            { 0.3, 0.1, 0.2, 0.2, 0.1, 0.1 }, // "L"
        };


    public static void main (String[] args) {
        Dice dice = new Dice();
        int[] diceSpots300 = dice.roll(300);
        System.out.println();
        baum_welch_algorithm( diceSpots300 );
        System.out.println();

        int[] diceSpots30000 = dice.roll(30000);
        System.out.println();
        baum_welch_algorithm( diceSpots30000 );
        System.out.println();

    }

    public static void baum_welch_algorithm( int[] emissions ) {
        int t = 0;
        double[] p = new double[10000000];
        double[] backp = new double[10000000];
        double differ = 1;
        do {
            /* E-step */
            double[][] f = culculateForwardVariables(emissions);
            double[][] b = culculateBackwardVariables(emissions);
            //printMatrix(f);
            //printMatrix(b);

            double[][] intermediate_transition_probability = new double[stetesNum][stetesNum];
            for(int k = 0; k < stetesNum; k++) {
                for(int l = 0; l < stetesNum; l++) {
                    intermediate_transition_probability[k][l] = 0;
                }
            }

            /* M-step */
            double[][] Akl = new double[stetesNum][stetesNum];
            for(int k = 0; k < stetesNum; k++) {
                double sigmaAkl = 0;
                for(int l = 0; l < stetesNum; l++) {
                    Akl[k][l] = 0;
                    intermediate_transition_probability[k][l] = 0;
                    for(int i = 0; i < emissions.length - 1; i++ ) {
                        intermediate_transition_probability[k][l]
                            += devide( f[k][i] * transition_probabilities[k][l] * emission_probabilities[l][emissions[i+1]] * b[l][i+1], scaling_coefficient[i+1] );
                    }
                    Akl[k][l] = intermediate_transition_probability[k][l];
                }
                for(int l = 0; l < stetesNum; l++) {
                    sigmaAkl += Akl[k][l];
                }
                for(int l = 0; l < stetesNum; l++) {
                    transition_probabilities[k][l] = devide( Akl[k][l], sigmaAkl );
                }
            }

            for(int k = 0; k < stetesNum; k++) {
                double sigmaEkx = 0;
                double[][] Ekx = new double[stetesNum][number_of_emission_types];
                for (int x = 0; x < number_of_emission_types; x++) Ekx[k][x] = 0;
                for(int i = 0; i < emissions.length; i++ ) {
                    Ekx[k][emissions[i]] += f[k][i] * b[k][i];
                }
                for (int x = 0; x < number_of_emission_types; x++) {
                    sigmaEkx += Ekx[k][x];
                }
                for (int x = 0; x < number_of_emission_types; x++) {
                    emission_probabilities[k][x] = devide( Ekx[k][x], sigmaEkx );
                }
            }

            /* 収束判定 */
            for(int i = 0; i < emissions.length; i++ ) {
                p[t] += scaling_coefficient[i];
            }
            if (t != 0 ) differ = Math.log(p[t]) - Math.log(p[t - 1]);

            backp[t] = 0;
            for (int l = 0; l < stetesNum; l++) {
                backp[t] += bb[l][1];
            }

            t++;
        } while ( differ != 0  );

        System.out.println("estimation complete!\n\nSummary\n");
        System.out.println("learning steps : " + t);
        System.out.println("\nbackward : forward\n" + backp[t-1] + " : " + p[t-1]);
        System.out.println("\nTransition");
        printMatrix(transition_probabilities);
        System.out.println("\nEmission");
        printMatrix(emission_probabilities);
        System.out.println("\n------------------------------------------------");
    }

    public static double[][] initializeForwardVariables( int[] emissions  ) {
        double[][] f = new double[stetesNum][emissions.length];
        for (int k = 0; k < stetesNum; k++) {
            f[k][0] = 1;
            for (int i = 1; i < emissions.length; i++) {
                f[k][i] = 0;
            }
        }
        return f;
    }

     public static double[][] initializeBackwardVariables( int[] emissions  ) {
        double[][] b = new double[stetesNum][emissions.length];
        for (int k = 0; k < stetesNum; k++) {
            b[k][emissions.length - 1] = 1;
            for (int i = emissions.length - 2; i > 0; i--) {
                b[k][i] = 0;
            }
        }
        return b;
    }

    public static double[][] culculateForwardVariables( int[] emissions ) {
        double[][] f = initializeForwardVariables( emissions );
        scaling_coefficient = new double[emissions.length];
        for (int i = 1; i < emissions.length; i++) {
            scaling_coefficient[i] = 0;
            double sigma_el = 0;
            double forward_variable_for_scaling = 0;
            double[] forward_variable = new double[stetesNum];
            for (int l = 0; l < stetesNum; l++) {
                sigma_el += emission_probabilities[l][emissions[i]];
                for (int k = 0; k < stetesNum; k++) {
                    forward_variable_for_scaling += f[k][i-1] * transition_probabilities[k][l];
                    forward_variable[l] = 0;
                }
            }
            scaling_coefficient[i] = sigma_el * forward_variable_for_scaling;
            for (int l = 0; l < stetesNum; l++) {
                for (int k = 0; k < stetesNum; k++) {
                    f[l][i] = emission_probabilities[l][emissions[i]] * f[k][i-1] * transition_probabilities[k][l];
                    forward_variable[l] += f[k][i-1] * transition_probabilities[k][l];
                }
                f[l][i] = devide( emission_probabilities[l][emissions[i]] * forward_variable[l], scaling_coefficient[i] );
            }
        }
        return f;
    }

    public static double[][] culculateBackwardVariables(int[] emissions ) {
        double[][] b = initializeBackwardVariables( emissions );
        bb = initializeBackwardVariables( emissions );
        double[] p = new double[emissions.length];
        for (int i = emissions.length - 2 ; i > 0; i--) {
            double backward_variable = 0;
            p[i] += scaling_coefficient[i];
            for (int k = 0; k < stetesNum; k++) {
                backward_variable = 0;
                b[k][i] = 0;
                bb[k][i] = 0;
                for (int l = 0; l < stetesNum; l++) {
                    backward_variable += emission_probabilities[l][emissions[i+1]] * b[l][i+1] * transition_probabilities[k][l];
                }
                bb[k][i] = backward_variable;
                //if (i == 1 ) System.out.println(backward_variable +" : "+ p[i]);
                b[k][i] = devide( backward_variable, scaling_coefficient[i+1] );
            }
        }
        return b;
    }

    public static double devide(double bunsi, double bunbo) {
        if (bunsi != 0 && bunbo != 0) {
            return bunsi / bunbo;
        } else {
            return 0;
        }
    }

    public static void printMatrix(double[][] Matrix) {
        System.out.print("\n");
        for(int i = 0;i < Matrix.length; i++){
            for(int j = 0;j < Matrix[0].length; j++){
               BigDecimal L01 = new BigDecimal(Matrix[i][j]);
               System.out.print(L01.setScale(3, RoundingMode.CEILING)/*Matrix[i][j]*/ + "\t");
            }
            System.out.print("\n");
        }
    }
}

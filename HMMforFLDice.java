import java.math.BigDecimal;
import java.math.RoundingMode;

public class HMMforFLDice {

    static int stetesNum = 2;
    static int number_of_emission_types = 6;
    static double[] scaling_coefficient;
    static double[] primaryStateProbability = new double[number_of_emission_types];

    static double[][] transition_probabilities =
        {   // "H"  "L"
            { 0.5, 0.5 }, // "H"
            { 0.5, 0.5 }, // "L"
        };

    static double[][] emission_probabilities =
        {   // "A"  "T"  "G"  "C"
            { 0.1, 0.2, 0.2, 0.2, 0.1, 0.2 }, // "H"
            { 0.3, 0.1, 0.2, 0.2, 0.1, 0.1 }, // "L"
        };


    public static void main (String[] args) {
        Dice dice = new Dice();
        int[] diceSpots = dice.roll(30000);

        System.out.println();
        baum_welch_algorithm( diceSpots );
        System.out.println();
    }

    public static double[][] baum_welch_algorithm( int[] emissions ) {
        int t = 0;
        double[] p = new double[10000000];
        double[] backp = new double[10000000];
        double differ = 1;
        do {
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
            for(int i = 0; i < emissions.length; i++ ) {
                p[t] += scaling_coefficient[i];
            }
            if (t != 0 ) differ = Math.log(p[t]) - Math.log(p[t - 1]);
            for(int k = 0; k < stetesNum; k++) {
                for(int l = 0; l < stetesNum; l++) {
                    backp[t] += emission_probabilities[l][emissions[1]] * b[l][1] * transition_probabilities[k][l];
                }
            }
            //System.out.println("back : " + backp[t] + ", likelyhood : " + Math.log(p[t]));
            t++;
        } while ( differ != 0  );

        System.out.println("estimation complete!\n\nSummary\n");
        System.out.println("learning steps : " + t);
        //System.out.println("back : " + backp[t] + ", likelyhood : " + Math.log(p[t]));
        System.out.println("\nTransition");
        printMatrix(transition_probabilities);
        System.out.println("\nEmission");
        printMatrix(emission_probabilities);

        return transition_probabilities;
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
            double esum = 0;
            double fsum = 0;
            double[] ffsum = new double[stetesNum];
            for (int l = 0; l < stetesNum; l++) {
                esum += emission_probabilities[l][emissions[i]];
                for (int k = 0; k < stetesNum; k++) {
                    fsum += f[k][i-1] * transition_probabilities[k][l];
                    ffsum[l] = 0;
                }
            }
            scaling_coefficient[i] = esum * fsum;
            for (int l = 0; l < stetesNum; l++) {
                for (int k = 0; k < stetesNum; k++) {
                    f[l][i] = emission_probabilities[l][emissions[i]] * f[k][i-1] * transition_probabilities[k][l];
                    ffsum[l] += f[k][i-1] * transition_probabilities[k][l];
                }
                f[l][i] = devide( emission_probabilities[l][emissions[i]] * ffsum[l], scaling_coefficient[i] );
            }
        }
        return f;
    }

    public static double[][] culculateBackwardVariables(int[] emissions ) {
        double[][] b = initializeBackwardVariables( emissions );
        for (int i = emissions.length - 2 ; i > 0; i--) {
            double sum = 0;
            for (int k = 0; k < stetesNum; k++) {
                sum = 0;
                b[k][i] = 0;
                for (int l = 0; l < stetesNum; l++) {
                    sum += emission_probabilities[l][emissions[i+1]] * b[l][i+1] * transition_probabilities[k][l];
                }
                b[k][i] = devide( sum, scaling_coefficient[i+1] );
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

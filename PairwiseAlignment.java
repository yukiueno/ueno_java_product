public class PairwiseAlignment {
    static boolean LOCAL = false;
    static boolean GLOBAL = false;
    static boolean AFFINE = false;
    static int MatchScore = 2;
    static int GapPenalty = -2;
    static int MismatchPenalty = -1;
    static int GrouthPenalty = -1;

    public static void main (String[] args) {
        if (args.length > 0) { LOCAL = args[0].equals("-local"); }
        if (args.length > 0) { GLOBAL = args[0].equals("-global"); }
        if (args.length > 0) { AFFINE = args[0].equals("-affine");  }

        String[] sequence1 = SetSequence.getSequenceByOption(args);
        String[] sequence2 = SetSequence.getSequenceByOption2(args);

        int[][] scoreMatrix = makeScoreMatrix(sequence1, sequence2);
        String[] traceback = new String[]{};

        if ( LOCAL ) {
            traceback = LocalAlignment.traceback(scoreMatrix, sequence1, sequence2);
        } else if ( GLOBAL ) {
            traceback = GlobalAlignment.traceback(scoreMatrix, sequence1, sequence2);
        } else if ( AFFINE ) {
            traceback = AffineGapMatrix.traceback(scoreMatrix, sequence1, sequence2);
        } else {
            traceback = LocalAlignment.traceback(scoreMatrix, sequence1, sequence2);
        }

        printTrace(traceback);
    }

    public static int max_of_three(int x, int y, int z) {
        int t = x > y ? x : y;
        return (t > z ? t : z);
    }

    public static int max_of_four(int x, int y, int z, int w) {
        int t = x > y ? x : y;
        int s = z > w ? z : w;
        return (t > s ? t : s);
    }

    public static int[][] initializeScoreMatrix(int scoreMatrix[][]) {
        scoreMatrix[0][0] = 0;
        if ( LOCAL ){
            LocalAlignment.initialize(scoreMatrix);
        } else if ( GLOBAL ) {
            GlobalAlignment.initialize(scoreMatrix);
        } else if ( AFFINE ) {
            AffineGapMatrix.initialize(scoreMatrix);
        } else {
            System.out.println("No option. So do alignment by local.");
            LocalAlignment.initialize(scoreMatrix);
        }
        return scoreMatrix;
    }

    public static int[][] makeScoreMatrix(String[] sequence1, String[] sequence2) {
        int[][] scoreMatrix = new int[sequence1.length][sequence2.length];
        initializeScoreMatrix(scoreMatrix);

        if ( LOCAL  ) {
            LocalAlignment.makeScore(scoreMatrix, sequence1, sequence2);
        } else if ( GLOBAL ) {
            GlobalAlignment.makeScore(scoreMatrix, sequence1, sequence2);
        } else if ( AFFINE ) {
            AffineGapMatrix.makeScore(scoreMatrix, sequence1, sequence2);
        } else {
            LocalAlignment.makeScore(scoreMatrix, sequence1, sequence2);
        }
        printScoreMatrix(scoreMatrix, sequence1, sequence2);
        return scoreMatrix;
    }

    public static void printScoreMatrix(int scoreMatrix[][], String[] sequence1, String[] sequence2) {
        System.out.print("\t");
        for(int j = 0;j < scoreMatrix[0].length; j++){
            System.out.print(sequence2[j] + "\t");
        }
        System.out.print("\n");
        for(int i = 0;i < scoreMatrix.length; i++){
            System.out.print(sequence1[i] + "\t");
            for(int j = 0;j < scoreMatrix[0].length; j++){
               System.out.print(scoreMatrix[i][j] + "\t");
            }
            System.out.print("\n");
        }
    }

    public static void printTrace(String[] traceback) {
        String[] trace = traceback;
        String x = trace[0];
        String y = trace[1];
        StringBuffer sb = new StringBuffer();
        sb.append("  ");
        for (int i = 2; x.charAt(i) != ':'; i++)
          sb.append(x.charAt(i) == y.charAt(i) ? "|" : " ");
        sb.append("  ");
        String z = sb.toString();
        int width = 120;
        int i = -1;
        while (++i * width < x.length()) {
          System.out.println(x.substring(i * width,
              Math.min(x.length(), (i + 1) * width)));
          System.out.println(z.substring(i * width,
              Math.min(z.length(), (i + 1) * width)));
          System.out.println(y.substring(i * width,
              Math.min(y.length(), (i + 1) * width)));
        }
    }
}

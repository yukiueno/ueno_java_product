public class PairwiseAlignment {
    static boolean LOCAL = false;
    static boolean GLOBAL = false;
    static boolean AFFINE = false;
    static int AlignType = -1;
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
        String[] traceback = traceback(scoreMatrix, sequence1, sequence2);
        if ( AFFINE ) { traceback = tracebackForAffine(scoreMatrix, sequence1, sequence2); }

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
            initializeByLocal(scoreMatrix);
        } else if ( GLOBAL ) {
            initializeByGlobal(scoreMatrix);
        } else if ( AFFINE ) {
            initializeByAffine(scoreMatrix);
        } else {
            System.out.println("No option. So do alignment by local.");
            initializeByLocal(scoreMatrix);
        }
        return scoreMatrix;
    }

    public static int[][] initializeByLocal(int scoreMatrix[][]) {
        for(int i = 1;i < scoreMatrix.length; i++){ scoreMatrix[i][0] = scoreMatrix[i-1][0] + GapPenalty;}
        for(int j = 1;j < scoreMatrix[0].length; j++){ scoreMatrix[0][j] = scoreMatrix[0][j-1] + GapPenalty;}
        return scoreMatrix;
    }

    public static int[][] initializeByGlobal(int scoreMatrix[][]) {
        for(int i = 1;i < scoreMatrix.length; i++){ scoreMatrix[i][0] = 0; }
        for(int j = 1;j < scoreMatrix[0].length; j++){ scoreMatrix[0][j] = 0; }
        return scoreMatrix;
    }

    public static int[][] initializeByAffine(int scoreMatrix[][]) {
        for(int i = 1;i < scoreMatrix.length; i++){ scoreMatrix[i][0] = scoreMatrix[i-1][0] + GapPenalty + (i - 1) * GrouthPenalty;}
        for(int j = 1;j < scoreMatrix[0].length; j++){ scoreMatrix[0][j] = scoreMatrix[0][j-1] + GapPenalty + (j - 1) * GrouthPenalty;}
        return scoreMatrix;
    }

    public static int[][] makeScoreMatrix(String[] sequence1, String[] sequence2) {
        int[][] scoreMatrix = new int[sequence1.length][sequence2.length];
        initializeScoreMatrix(scoreMatrix);

        if ( LOCAL  ) {
            makeScoreByLocal(scoreMatrix, sequence1, sequence2);
        } else if ( GLOBAL ) {
            makeScoreByGlobal(scoreMatrix, sequence1, sequence2);
        } else if ( AFFINE ) {
            makeScoreByAffine(scoreMatrix, sequence1, sequence2);
        } else {
            makeScoreByLocal(scoreMatrix, sequence1, sequence2);
        }
        return scoreMatrix;
    }

    public static int[][] makeScoreByLocal(int[][] scoreMatrix, String[] sequence1, String[] sequence2) {
        for(int i = 1;i < scoreMatrix.length; i++){
            for(int j = 1;j < scoreMatrix[0].length; j++){
                scoreMatrix[i][j] = max_of_three(
                    scoreMatrix[i][j-1] + GapPenalty,
                    scoreMatrix[i-1][j] + GapPenalty,
                    scoreMatrix[i-1][j-1] + (sequence1[i].equals(sequence2[j]) ? MatchScore : MismatchPenalty)
                );
            }
        }
        return scoreMatrix;
    }

    public static int[][] makeScoreByGlobal(int[][] scoreMatrix, String[] sequence1, String[] sequence2) {
        for(int i = 1;i < scoreMatrix.length; i++){
            for(int j = 1;j < scoreMatrix[0].length; j++){
                scoreMatrix[i][j] = max_of_four(
                    0,
                    scoreMatrix[i][j-1] + GapPenalty,
                    scoreMatrix[i-1][j] + GapPenalty,
                    scoreMatrix[i-1][j-1] + (sequence1[i].equals(sequence2[j]) ? MatchScore : MismatchPenalty)
                );
            }
        }
        return scoreMatrix;
    }

    public static int[][] makeScoreByAffine(int[][] scoreMatrix, String[] sequence1, String[] sequence2) {
        for(int i = 1;i < scoreMatrix.length; i++){
            for(int j = 1;j < scoreMatrix[0].length; j++) {
                if ( i > 1 && j > 1){
                    scoreMatrix[i][j] = max_of_three(
                        makeAffineGapScoreX(scoreMatrix, i-1, j-1) + MatchScore,
                        makeAffineGapScoreY(scoreMatrix, i-1, j-1) + MatchScore,
                        scoreMatrix[i-1][j-1] + MatchScore
                    );
                } else {
                    scoreMatrix[i][j] = scoreMatrix[i-1][j-1] + (sequence1[i].equals(sequence2[j]) ? MatchScore : MismatchPenalty);
                }
            }
        }
        printScoreMatrix(scoreMatrix, sequence1, sequence2);
        return scoreMatrix;
    }

    public static int makeAffineGapScoreX(int[][] scoreMatrix, int x, int y) {
        int gap = 1;
        do {
            if (scoreMatrix[x - gap][y] + GapPenalty == scoreMatrix[x][y]) {
                return scoreMatrix[x - gap][y] + GapPenalty + (gap - 1) * GrouthPenalty;
            } else if (scoreMatrix[x - gap][y] + GapPenalty + (gap - 1) * GrouthPenalty == scoreMatrix[x][y]) {
                gap++;
            } else {
                return scoreMatrix[x - gap][y] + GapPenalty + (gap - 1) * GrouthPenalty;
            }
        } while ( x > gap );
        return scoreMatrix[x - gap][y] + GapPenalty + (gap - 1) * GrouthPenalty;
    }

    public static int makeAffineGapScoreY(int[][] scoreMatrix, int x, int y) {
        int gap = 1;
        do {
            if (scoreMatrix[x][y - gap] + GapPenalty == scoreMatrix[x][y]) {
                return scoreMatrix[x][y - gap] + GapPenalty + (gap - 1) * GrouthPenalty;
            } else if (scoreMatrix[x][y] + GapPenalty + (gap - 1) * GrouthPenalty == scoreMatrix[x][y]) {
                gap++;
                y--;
            } else {
                return scoreMatrix[x][y - gap] + GapPenalty + (gap - 1) * GrouthPenalty;
            }
        } while ( y > gap );
        return scoreMatrix[x][y - gap] + GapPenalty + (gap - 1) * GrouthPenalty;
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

    public static String[] traceback(int[][] scoreMatrix, String[] sequence1, String[] sequence2) {
        int x = scoreMatrix.length - 1, y = scoreMatrix[0].length - 1;

        StringBuffer line1 = new StringBuffer();
        StringBuffer line2 = new StringBuffer();
        line1.append(':');
        line1.append(x);
        line2.append(':');
        line2.append(y);
        do {
            if (scoreMatrix[x - 1][y] - 2 == scoreMatrix[x][y]) {
                line1.insert(0, sequence1[x]);
                line2.insert(0, '-');
            } else if (scoreMatrix[x][y - 1] - 2 == scoreMatrix[x][y]) {
                line1.insert(0, '-');
                line2.insert(0, sequence2[y]);
            } else {
                line1.insert(0, sequence1[x]);
                line2.insert(0, sequence2[y]);
            }
            x--;
            y--;
        } while (x > 1 && y > 1);
        line1.insert(0, ':');
        line1.insert(0, '0');
        line2.insert(0, ':');
        line2.insert(0, '0');
        return new String[] { line1.toString(), line2.toString() };
    }

    public static String[] tracebackForAffine(int[][] scoreMatrix, String[] sequence1, String[] sequence2) {
        int x = scoreMatrix.length - 1, y = scoreMatrix[0].length - 1;
        int max = -1000000000;

        for (int i = 0; i < scoreMatrix.length; i++) {
            max = Math.max( scoreMatrix[i][scoreMatrix[0].length - 1], max );
        }

        for (int j = 0; j < scoreMatrix[0].length; j++) {
            max = Math.max( scoreMatrix[scoreMatrix.length - 1][j], max );
        }

        for (int i = 0; i < scoreMatrix.length; i++) {
            if ( scoreMatrix[i][scoreMatrix[0].length - 1] == max ) {
                x = i;
            }
        }

        for (int j = 0; j < scoreMatrix[0].length; j++) {
            if ( scoreMatrix[scoreMatrix.length - 1][j] == max ) {
                y = j;
            }
        }

        System.out.println("\nMax score : " + max + "\n");

        StringBuffer line1 = new StringBuffer();
        StringBuffer line2 = new StringBuffer();
        line1.append(':');
        line1.append(x);
        line2.append(':');
        line2.append(y);
        do {
            if ( makeAffineGapScoreX(scoreMatrix, x - 1, y - 1) + MatchScore == scoreMatrix[x][y]) {
                line1.insert(0, sequence1[x]);
                line2.insert(0, '-');
            } else if ( makeAffineGapScoreY(scoreMatrix, x - 1, y - 1) + MatchScore == scoreMatrix[x][y]) {
                line1.insert(0, '-');
                line2.insert(0, sequence2[y]);
            } else {
                line1.insert(0, sequence1[x]);
                line2.insert(0, sequence2[y]);
            }
            x--;
            y--;
        } while (x > 1 && y > 1);
        line1.insert(0, ':');
        line1.insert(0, '0');
        line2.insert(0, ':');
        line2.insert(0, '0');
        return new String[] { line1.toString(), line2.toString() };
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

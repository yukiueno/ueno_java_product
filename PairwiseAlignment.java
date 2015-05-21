public class PairwiseAlignment {
    static boolean LOCAL = true;
    static int AlignType = -1;
    static int MatchScore = 2;
    static int GapPenalty = -2;
    static int MismatchPenalty = -1;

    public static void main (String[] args) {
        if (args.length > 0) { LOCAL = !args[0].equals("-global"); }

        String[] sequence1;
        String[] sequence2;

        sequence1 = SetSequence.getSequenceByOption(args);
        sequence2 = SetSequence.getSequenceByOption2(args);

        int[][] scoreMatrix = makeScoreMatrix(sequence1, sequence2);
        String[] traceback = traceback(scoreMatrix, sequence1, sequence2);

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
            for(int i = 1;i < scoreMatrix.length; i++){ scoreMatrix[i][0] = scoreMatrix[i-1][0] + GapPenalty;}
            for(int j = 1;j < scoreMatrix[0].length; j++){ scoreMatrix[0][j] = scoreMatrix[0][j-1] + GapPenalty;}
        } else {
            for(int i = 1;i < scoreMatrix.length; i++){ scoreMatrix[i][0] = 0; }
            for(int j = 1;j < scoreMatrix[0].length; j++){ scoreMatrix[0][j] = 0; }
        }
        return scoreMatrix;
    }

    public static int[][] makeScoreMatrix(String[] sequence1, String[] sequence2) {
        int[][] scoreMatrix = new int[sequence1.length][sequence2.length];

        initializeScoreMatrix(scoreMatrix);

        if ( LOCAL  ) {
            for(int i = 1;i < scoreMatrix.length; i++){
                for(int j = 1;j < scoreMatrix[0].length; j++){
                    scoreMatrix[i][j] = max_of_three(
                        scoreMatrix[i][j-1] + GapPenalty,
                        scoreMatrix[i-1][j] + GapPenalty,
                        scoreMatrix[i-1][j-1] + (sequence1[i].equals(sequence2[j]) ? MatchScore : MismatchPenalty)
                    );
                }
            }
        } else {
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
        }
        printScoreMatrix(scoreMatrix, sequence1, sequence2);
        return scoreMatrix;
    }

    public static void printScoreMatrix(int scoreMatrix[][], String[] sequence1, String[] sequence2) {
        System.out.print("\t");
        for(int j = 0;j < scoreMatrix[0].length; j++){
            System.out.print(sequence2[j] + "\t");
        }
        System.out.println("\n");
        for(int i = 0;i < scoreMatrix.length; i++){
            System.out.print(sequence1[i] + "\t");
            for(int j = 0;j < scoreMatrix[0].length; j++){
               System.out.print(scoreMatrix[i][j] + "\t");
            }
            System.out.println("\n");
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
                line1.insert(0, sequence1[x - 1]);
                line2.insert(0, '-');
            } else if (scoreMatrix[x][y - 1] - 2 == scoreMatrix[x][y]) {
                line1.insert(0, '-');
                line2.insert(0, sequence2[y - 1]);
            } else {
                line1.insert(0, sequence1[x - 1]);
                line2.insert(0, sequence2[y - 1]);
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
        int width = 50;
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

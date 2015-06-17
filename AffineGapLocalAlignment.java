public class AffineGapLocalAlignment {
    static int MatchScore = 2;
    static int GapPenalty = -2;
    static int MismatchPenalty = -1;
    static int GrouthPenalty = -1;

    public static int max_of_three(int x, int y, int z) {
        int t = x > y ? x : y;
        return (t > z ? t : z);
    }

    public static int max_of_four(int x, int y, int z, int w) {
        int t = x > y ? x : y;
        int s = z > w ? z : w;
        return (t > s ? t : s);
    }

    public static String[] alignment( String[] sequence1, String[] sequence2) {
        int[][] scoreMatrix = makeScoreMatrix(sequence1, sequence2);
        String[] traceback = traceback(scoreMatrix, sequence1, sequence2);
        return traceback;
    }

    public static int[][] initialize(int scoreMatrix[][]) {
        scoreMatrix[0][0] = 0;
        initializeByAffine(scoreMatrix);
        return scoreMatrix;
    }

    public static int[][] initializeByAffine(int scoreMatrix[][]) {
        for(int i = 1;i < scoreMatrix.length; i++){ scoreMatrix[i][0] = 0;}
        for(int j = 1;j < scoreMatrix[0].length; j++){ scoreMatrix[0][j] = 0;}
        return scoreMatrix;
    }

    public static int[][] makeScoreMatrix(String[] sequence1, String[] sequence2) {
        int[][] scoreMatrix = new int[sequence1.length][sequence2.length];
        initialize(scoreMatrix);
        makeScore(scoreMatrix, sequence1, sequence2);
        return scoreMatrix;
    }

    public static int[][] makeScore(int[][] scoreMatrix, String[] sequence1, String[] sequence2) {
        for(int i = 1;i < scoreMatrix.length; i++){
            for(int j = 1;j < scoreMatrix[0].length; j++) {
                if ( i > 1 && j > 1){
                    scoreMatrix[i][j] = max_of_four(
                        0,
                        makeAffineGapScoreX(scoreMatrix, i-1, j-1) + (sequence1[i].equals(sequence2[j]) ? MatchScore : MismatchPenalty),
                        makeAffineGapScoreY(scoreMatrix, i-1, j-1) + (sequence1[i].equals(sequence2[j]) ? MatchScore : MismatchPenalty),
                        scoreMatrix[i-1][j-1] + (sequence1[i].equals(sequence2[j]) ? MatchScore : MismatchPenalty)
                    );
                } else {
                    scoreMatrix[i][j] = scoreMatrix[i-1][j-1] + (sequence1[i].equals(sequence2[j]) ? MatchScore : MismatchPenalty);
                }
            }
        }
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

    public static String[] traceback(int[][] scoreMatrix, String[] sequence1, String[] sequence2) {
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
        } while ( scoreMatrix[x][y] > 0 );
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

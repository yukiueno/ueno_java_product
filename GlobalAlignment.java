public class GlobalAlignment {
    static int MatchScore = 2;
    static int GapPenalty = -2;
    static int MismatchPenalty = -1;
    static int GrouthPenalty = -1;

    public static int max_of_three(int x, int y, int z) {
        int t = x > y ? x : y;
        return (t > z ? t : z);
    }

    public static int[][] initialize(int scoreMatrix[][]) {
        for(int i = 1;i < scoreMatrix.length; i++){ scoreMatrix[i][0] = scoreMatrix[i-1][0] + GapPenalty;}
        for(int j = 1;j < scoreMatrix[0].length; j++){ scoreMatrix[0][j] = scoreMatrix[0][j-1] + GapPenalty;}
        return scoreMatrix;
    }

    public static int[][] makeScore(int[][] scoreMatrix, String[] sequence1, String[] sequence2) {
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
        } while (x > 0 && y > 0);
        line1.insert(0, sequence1[1]);
        line2.insert(0, sequence2[1]);

        line1.insert(0, ':');
        line1.insert(0, '0');
        line2.insert(0, ':');
        line2.insert(0, '0');
        return new String[] { line1.toString(), line2.toString() };
    }
}

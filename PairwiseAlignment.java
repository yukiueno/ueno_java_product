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
        String[] traceback = new String[]{};

        if ( LOCAL ) {
            if ( AFFINE ) {
                traceback = AffineGapLocalAlignment.alignment(sequence1, sequence2);
            } else {
                traceback = LocalAlignment.alignment(sequence1, sequence2);
            }
        } else if ( GLOBAL ) {
            if ( AFFINE ) {
                traceback = AffineGapGlobalAlignment.alignment(sequence1, sequence2);
            }
            else {
                traceback = GlobalAlignment.alignment(sequence1, sequence2);
            }
        } else if ( AFFINE ) {
            traceback = AffineGapGlobalAlignment.alignment(sequence1, sequence2);
        } else {
            System.out.println("No option. So do alignment by local.");
            traceback = LocalAlignment.alignment(sequence1, sequence2);
        }

        printTrace(traceback);
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

import java.util.Random;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PairwiseAlignment {
    public static void main (String[] args) {
        String[] sequence1;
        String[] sequence2;

        if (args.length == 0 || args.length > 2) {
            System.out.print("Randam sequence test.\n");
            int array_size1 = new Random().nextInt(20);
            int array_size2 = new Random().nextInt(20);
            sequence1 = makeSequence(array_size1);
            sequence2 = makeSequence(array_size2);
        }else if (args.length == 1){
            int array_size2 = new Random().nextInt(20);
            sequence1 = readFasta(args[0]);
            sequence2 = makeSequence(array_size2);
        }else{
            sequence1 = readFasta(args[0]);
            sequence2 = readFasta(args[0]);
        }

        int[][] scoreMatrix = makeScoreMatrix(sequence1, sequence2);
        String[] traceback = traceback(scoreMatrix, sequence1, sequence2);
        printTrace(traceback);
    }

    public static String[] readFasta(String file) {
        int i = 0;
        String[] read = new String[100];
        try{
            FileReader filereader = new FileReader(file);

            int mozi;
            while((mozi = filereader.read()) != -1){
                if(String.valueOf((char)mozi) != null) {
                    System.out.print((char)mozi);
                    read[i] = String.valueOf((char)mozi);
                    i++;
                }
           }

           filereader.close();
        }catch(FileNotFoundException e){
           System.out.println(e);
        }catch(IOException e){
           System.out.println(e);
        }
        String[] sequence = new String[i];
        for(int j = 1;j < i; j++){
            sequence[j] = read[j - 1];
        }
        sequence[0] = "0";
        return sequence;
    }

    public static String[] makeSequence(int array_size) {
        String[] arr = new String[array_size];
        String[] sequence_type = new String[]{"A","T","G","C"};

        for(int i = 1;i < array_size; i++){
            int r = new Random().nextInt(sequence_type.length);
            arr[i] = sequence_type[r];
        }
        arr[0] = "0";
        return arr;
    }

    public static int[][] initializeScoreMatrix(int scoreMatrix[][]) {
        scoreMatrix[0][0] = 0;
        for(int i = 1;i < scoreMatrix.length; i++){
            scoreMatrix[i][0] = scoreMatrix[i-1][0] - 2;
        }
        for(int j = 1;j < scoreMatrix[0].length; j++){
            scoreMatrix[0][j] = scoreMatrix[0][j-1] - 2;
        }
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

    public static int[][] makeScoreMatrix(String[] sequence1, String[] sequence2) {
        int score = 0;
        int[][] scoreMatrix = new int[sequence1.length][sequence2.length];

        initializeScoreMatrix(scoreMatrix);

        for(int i = 1;i < scoreMatrix.length; i++){
            for(int j = 1;j < scoreMatrix[0].length; j++){
                int point_score = 0;
                int gap_score = 0;
                if(sequence1[i] == sequence2[j]){
                    point_score = scoreMatrix[i-1][j-1] + 2;
                }else{
                    point_score = scoreMatrix[i-1][j-1] - 1;
                }

                if(scoreMatrix[i-1][j] < scoreMatrix[i][j-1]){
                    gap_score = scoreMatrix[i][j-1] - 2;
                }else{
                    gap_score = scoreMatrix[i-1][j] - 2;
                }

                if(point_score < gap_score){
                    scoreMatrix[i][j] = gap_score;
                }else{
                    scoreMatrix[i][j] = point_score;
                }
            }
        }
        printScoreMatrix(scoreMatrix, sequence1, sequence2);
        return scoreMatrix;
    }

    public static String[] traceback(int[][] scoreMatrix, String[] sequence1, String[] sequence2) {
        int x = scoreMatrix.length - 1, y = scoreMatrix[0].length - 1;
          // Search for the maximum score brute-force (very inefficient).
          // The max score can be recorded when computing scoreMatrix.
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
        sb.append("    ");
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

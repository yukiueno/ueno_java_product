import java.util.Random;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SetSequence {
    static boolean LOCAL = true;
    static int SIZE = 20;
    public static String[] getSequenceByOption(String[] options) {
        String[] sequence = makeRandomSequence(SIZE);
        switch ( options.length ) {
        case 0:
            sequence = makeRandomSequence(SIZE);
            break;
        case 1:
            sequence = getSequenceByOptionType(options[0]);
            break;
        case 2:
            sequence = getSequenceByOptionType(options[1]);
            break;
        case 3:
            sequence = getSequenceByOptionType(options[1]);
            break;
        }
        return sequence;
    }

    public static String[] getSequenceByOption2(String[] options) {
        String[] sequence = makeRandomSequence(SIZE);
        switch ( options.length ) {
        case 0:
            sequence = makeRandomSequence(SIZE);
            break;
        case 1:
            sequence = makeRandomSequence(SIZE);
            break;
        case 2:
            if ( options[0].equals("-global") ){
                sequence = makeRandomSequence(SIZE);
            } else if ( options[0].equals("-affine") ) {
                sequence = makeRandomSequence(SIZE);
            } else {
                sequence = getSequenceByOptionType(options[0]);
            }
            break;
        case 3:
            sequence = getSequenceByOptionType(options[2]);
            break;
        }
        return sequence;
    }

    public static String[] getSequenceByOptionType(String option) {
        String[] sequence = makeRandomSequence(SIZE);
        switch ( option ) {
        case "-local":
            break;
        case "-global":
            break;
        case "-affine":
            break;
        case "":
            sequence = makeRandomSequence(SIZE);
            break;
        default:
            sequence = readFasta(option);
        }
        return sequence;
    }

    public static String[] readFasta(String file) {
        int text_size = 0;
        int label_size = 0;
        boolean readlabel = false;
        String[] read = new String[100000];

        try{
            FileReader filereader = new FileReader(file);

            int mozi;
            while((mozi = filereader.read()) != -1){
                if ( String.valueOf((char)mozi) != null ){
                    read[text_size] = String.valueOf((char)mozi);
                    if(read[text_size].equals(">")) {
                        readlabel = true;
                    } else if ( read[text_size].equals("\n") ) {
                        readlabel = false;
                    }
                    if ( readlabel ) { label_size++; };
                    text_size++;
                }
            }
            filereader.close();
        }catch(FileNotFoundException e){
           System.out.println(e);
        }catch(IOException e){
           System.out.println(e);
        }

        int sequence_size = text_size - label_size - 1;
        String[] sequence = new String[sequence_size];

        for(int j = 1;j < sequence_size; j++){
            sequence[j] = read[j + label_size];
            System.out.print(sequence[j]);
        }
        sequence[0] = "0";

        System.out.print("\n");
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

    public static String[] makeRandomSequence(int array_size) {
        int random_array_size = 0;
        do {
            random_array_size = new Random().nextInt(array_size);
        } while ( random_array_size < 3 );

        return makeSequence(array_size);
    }
}

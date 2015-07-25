import java.util.BitSet;
public class WaveletMatrix {
    public String unique( final String sequence ) {
        StringBuilder unique = new StringBuilder();
        int count = 0;
        unique.append(sequence.charAt(0));
        for (int i = 0; i < sequence.length(); i++) {
            count = 0;
            for (int j = 0; j < unique.length(); j++) {
                if ( sequence.charAt(i) == unique.charAt(j)) count++;
            }
            if ( count == 0 ) unique.append(sequence.charAt(i));
        }

        return unique.toString();
    }

    public int[] setBucket( final String sequence ) {
        String unique = this.unique( sequence );
        int[] indexBucket = new int[unique.length()];
        for (int i = 0; i < unique.length(); i++) {
            indexBucket[i] = 0;
            for (int j = 0; j < sequence.length(); j++) {
                if( unique.charAt(i) == sequence.charAt(j) ) indexBucket[i]++;
            }
        }
        return indexBucket;
    }

    public int takeBitSize( final String unique ) {
        int size = unique.length();
        int bitSize = Integer.toBinaryString(size).length();
        return bitSize;
    }

    public boolean convertChartoBit( final int keta, final int index, final String sequence, final String unique ) {
        boolean bit = false;
        Character character = sequence.charAt(index);
        String test = "";
        Character trueth = new Character('1');
        for (int k = 0; k < unique.length(); k++) {
            if ( character == unique.charAt(k) ) {
                test = Integer.toBinaryString(k);
                //System.out.println( character + ":" + test );
            }
        }

        if ( test.length() > keta && test != "") {
            if ( test.charAt(keta) == trueth && character != unique.charAt(0) ) {
                bit = true;
                //System.out.println( character + ":" + test );
            }
        } /*else if (test.length() == keta && test.charAt(keta - 1) == trueth ) {
            bit = true;
        }*/
        if ( keta == 0 && test != "") {
            
        }
        return bit;
    }

    public SuccinctBitSet[] buildBitSet( final String sequence ) {
        String unique = this.unique(sequence);
        SuccinctBitSet[] bitSet = new SuccinctBitSet[Integer.toBinaryString(unique.length()).length()];

        for (int i = 0; i < bitSet.length; i++) {
            bitSet[i] = new SuccinctBitSet();
            for (int j = 0; j < sequence.length(); j++) {
                if ( convertChartoBit( i, j, sequence, unique ) ) {
                    bitSet[i].set(j);
                }
            }
        }
        return bitSet;
    }
}

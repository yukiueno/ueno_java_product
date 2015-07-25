public class WaveletMatrixTest {
    public static void main(String[] args) {
        //int [] codePointArray = new int [] { 1, 7, 3, 5, 6, 2, 4, 0, 4, 1, 4, 7 };
        //String str = new String( codePointArray, 0, codePointArray.length );
        String str = "abracadabraadddrrcffd$";

        // Wavelet Matrix
        WaveletMatrix wm = new WaveletMatrix();
        int[] bucket = wm.setBucket(str);
        String unique = wm.unique(str);
        SuccinctBitSet[] bit = wm.buildBitSet(str);
        System.out.println(bit);

        for ( int j = 2; j > -1; j-- ) {
            for ( int i = 0; i < str.length(); i++ ) {
                //System.out.print(bucket[i] + "\t");
                if (bit[j].get(i)) {
                    System.out.print(1);
                } else {
                    System.out.print(0);
                }
            }
            System.out.println();
        }
        System.out.println(str);
        System.out.println();
    }

}

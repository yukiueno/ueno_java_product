public class WaveletTree {
    public static void main(String[] args) {
        String test = "kondeneinensizaihou$";
        for (int i = 0; i < test.length; i++ ) {
            String c = test.charAt(i);
            String t = "$";
            while (isleaf(c) == false) {
                b = c;
                push_back(Bt, b);
                t = child(t, b);
                d++;
            }
        }
        return WT;
}

public class BWTTest {
    public static void main(String[] args) {

        //String text = "mmiissiissiippii\0";
        //String text = "abracadabra\0";
        String text = "Alice was beginning to get very tired of sitting by her sister on the bank, and of having nothing to do: once or twice she had peeped into the book her sister was reading, but it had no pictures or conversations in it, 'and what is the use of a book,' thought Alice 'without pictures or conversation?'\0";

        // Suffix Array
        SuffixArray sa = new SuffixArray( text );
        sa.build();

        int codePointCount = text.codePointCount(0, text.length());

        System.out.println();

        // BWT
        System.out.println( "BWT:" );
        StringBuffer bwt = new StringBuffer();
        for ( int i = 0; i < sa.getSize(); i++ ) {
            if ( 0 == sa.at(i) ) {
                bwt.appendCodePoint(text.codePointAt(codePointCount - 1));
            } else {
                bwt.appendCodePoint(text.codePointAt(sa.at(i) - 1));
            }
        }

        System.out.println( bwt.toString() );

    }

}

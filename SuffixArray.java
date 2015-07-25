import java.util.BitSet;
import java.util.Arrays;
public class SuffixArray {
    private String targetText;
    private int[] mSuffixArray;

    public SuffixArray( final String originalText ) {
        this.targetText = originalText;
    }

    public int at( final int pIndex ) {
        return this.mSuffixArray[ pIndex ];
    }

    public int getSize() {
        return this.mSuffixArray.length;
    }

    public int [] getSuffixArray() {
        return this.mSuffixArray;
    }

    private int getMaxCodePoint( final String originalText, final int textLength ) {
        // 最後の位置は最小のコードポイントが格納されているので、無視する。
        int maxCodePoint = originalText.codePointAt(textLength - 2);

        for ( int i = textLength - 3; i >= 0; i-- ) {
            // 最大のコードポイントを決定する。
            int codePoint = this.targetText.codePointAt(i);
            if ( maxCodePoint < codePoint ) {
                maxCodePoint = codePoint;
            }
        }

        return maxCodePoint;

    }

    private void setLSType( final String originalText, final BitSet typeSLbyBit, final int textLength ) {
        typeSLbyBit.set(textLength - 1, true);
        typeSLbyBit.set(textLength - 2, false);

        for ( int i = textLength - 3; i >= 0; i-- ) {
            if ( originalText.codePointAt(i) < originalText.codePointAt(i + 1) || (originalText.codePointAt(i) == originalText.codePointAt(i + 1) && true == typeSLbyBit.get(i + 1) ) ) {
                typeSLbyBit.set(i, true);
            } else {
                typeSLbyBit.set(i, false);
            }
        }
    }

    private void setBucket( final String originalText, final int [] locusBucket, final int textLength, final boolean typeSorL ) {

        // バケツを初期化する。
        Arrays.fill(locusBucket, 0);

        // 各文字のバケツを初期化する。
        for ( int i = 0; i < textLength; i++ ) {
            locusBucket[ originalText.codePointAt( i ) ]++;
        }

        // 2種類のバケツを取得する。
        int sum = 0;
        for ( int i = 0; i < locusBucket.length; i++ ) {
            sum += locusBucket[i];

            if ( typeSorL ) {
                // 指定された文字より小さい文字の頻度と指定された文字の頻度の合計を取得する。
                // Type-Sで利用する。
                locusBucket[i] = sum;
            } else {
                // 指定された文字より小さい文字の頻度を取得する。
                // Type-Lで利用する。
                locusBucket[i] = sum - locusBucket[i];
            }
        }

    }

    private boolean isLMS(
            final BitSet typeSLbyBit,
            final int pIndex
            ) {
        if ( 0 < pIndex &&
                typeSLbyBit.get(pIndex) &&
                !typeSLbyBit.get(pIndex - 1) ) {
            return true;
        }
        return false;
    }

    private void induceSuffixArrayTypeL(
            final BitSet typeSLbyBit,
            final int [] locusSuffixArray,
            final String originalText,
            final int [] locusBucket,
            final int textLength,
            final boolean typeSorL
            ) {

        // バケツを取得する。
        setBucket(originalText, locusBucket, textLength, typeSorL);

        // 先頭からType-LのSuffix Arrayの位置を決定する。
        for ( int i = 0; i < textLength; i++ ) {

            // ソートされているType-S*の直前のType-Lを取得する。
            // すでに決定したType-Lの直前のType-Lも取得して、位置を決定する。
            int j = locusSuffixArray[i] - 1;

            if ( 0 <= j && !typeSLbyBit.get(j) ) {
                // まだ決定していないType-Lの文字の中で最小のSuffixなので、バケツの先頭に追加する。
                locusSuffixArray[ locusBucket[ originalText.codePointAt(j) ]++ ] = j;
            }
        }

    }

    private void induceSuffixArrayTypeS(
            final BitSet typeSLbyBit,
            final int [] locusSuffixArray,
            final String originalText,
            final int [] locusBucket,
            final int textLength,
            final boolean typeSorL
            ) {

        // バケツを取得する。
        setBucket(originalText, locusBucket, textLength, typeSorL);

        // 最後からType-SのSuffix Arrayの位置を決定する。
        for ( int i = textLength - 1; i >= 0; i-- ) {

            // ソートされているType-Lの位置からType-Sの位置を決定する。
            // すでに決定したType-Sの直前のType-Sも取得して、位置を決定する。
            int j = locusSuffixArray[i] - 1;

            if ( 0 <= j && typeSLbyBit.get(j) ) {
                // まだ決定していないType-S(Type-S*も含む)の文字の中で最大のSuffixなので、バケツの最後に追加する。
                locusSuffixArray[ --locusBucket[ originalText.codePointAt(j) ] ] = j;
            }
        }

    }

    private void sort(
            final String originalText,
            final int[] locusSuffixArray,
            final int textLength,
            final int pMaxCodePoint
            ) {

        // Typeを格納する領域を取得する。
        BitSet type = new BitSet( textLength );

        // Typeを決定する。
        setLSType( originalText, type, textLength );

        // バケツを取得する。
        int [] bucket = new int [ pMaxCodePoint + 1 ];

        // バケツを初期化する。
        setBucket(originalText, bucket, textLength, true);

        // Suffix Arrayを初期化する。
        Arrays.fill(locusSuffixArray, -1);

        // Type-S*(LMS)のソートは、1文字目だけで行う。
        // Type-S*のSuffixとしてのソートはこの段階では行わないので任意で構わない。
        // つまり、この段階では、Type-S*の順序が決定できないが、1文字目の位置に格納しておくと、それ以外のType-SとType-Lの位置を決定できる。
        for ( int i = 1; i < textLength; i++ ) {
            if ( isLMS(type, i) ) {
                // Type-S*は、Type-Sなので、後ろから格納する。
                locusSuffixArray[ --bucket[ originalText.codePointAt(i) ] ] = i;
            }
        }

        // Type-S*を使って、Type-Lのソートを行う。
        induceSuffixArrayTypeL( type, locusSuffixArray, originalText, bucket, textLength, false );

        // Type-Lを使って、Type-S(Type-S*)のソートを行う。
        induceSuffixArrayTypeS( type, locusSuffixArray, originalText, bucket, textLength, true );

        // Type-S*の部分文字列単位でしか扱っていないので、この時点ではSuffixが決定できない場合がある。
        // Type-S*の部分文字列の順序を決定するために再帰的に適用する。

        // Type-S*の数を数える。
        int n1 = 0;
        for ( int i = 0; i < textLength; i++ ) {
            if ( isLMS( type, locusSuffixArray[i] ) ) {
                locusSuffixArray[n1] = locusSuffixArray[i];
                n1++;
            }
        }

        // Type-S*でない部分は、-1で初期化する。
        Arrays.fill(locusSuffixArray, n1, textLength, -1);

        int name = 0;
        int prev = -1;
        // ユニークなType-S*部分文字列を決定する。
        // Type-Sのソートは終わっているので、同じ部分文字列は並んでいる。
        for ( int i = 0; i < n1; i++ ) {

            // ソート済みのType-S*部分文字列の位置を取得する。
            int pos = locusSuffixArray[i];

            // Type-S*部分文字列が同一か？
            boolean diff = false;
            for ( int d = 0; d < textLength; d++ ) {

                if ( -1 == prev ||
                        originalText.codePointAt(pos + d) != originalText.codePointAt(prev + d) ||
                        type.get(pos + d) != type.get(prev + d) ) {
                    diff = true;
                    break;
                } else if ( 0 < d &&
                        ( isLMS(type, pos + d) || isLMS(type, prev + d) ) ) {
                    // Type-S*の場合は、ユニークでないType-S*部分文字列だった。
                    break;
                }
            }

            // Type-S*部分文字列がユニークか？
            if ( diff ) {
                name++;
                prev = pos;
            }

            if ( 0 == (pos % 2) ) {
                pos = pos / 2;
            } else {
                pos = (pos - 1) / 2;
            }

            // Type-S*部分文字列の出現位置を保ったまま、ユニークな値を割り当てる。
            locusSuffixArray[ n1 + pos ] = name - 1;
        }

        // 後方の位置に一旦、集める。
        int lastIndex = textLength - 1;
        for ( int i = textLength - 1; i >= n1; i-- ) {
            if ( 0 <= locusSuffixArray[i] ) {
                locusSuffixArray[ lastIndex-- ] = locusSuffixArray[i];
            }
        }

        // 元のテキストの出現順序を維持しながら、Type-S*部分文字列にユニークな値を割り当てた新しいテキストを生成する。
        StringBuffer newTextBuffer = new StringBuffer();
        for ( int i = lastIndex + 1; i < textLength; i++ ) {
            newTextBuffer.appendCodePoint( locusSuffixArray[i] );
        }

        // 再帰的にソートする。
        // Type-S*部分文字列がすべてユニークか？
        if ( name < n1 ) {
            // 現時点でType-S*部分文字列の順序が決定できていないので、再帰的にソートを実行して決定する。
            sort( newTextBuffer.toString(), locusSuffixArray, n1, name - 1 );
        } else {
            // ユニークな場合は、これ以上ソートする必要がなく、すべての順序が決定しているので再帰実行を終了する。
            for ( int i = 0; i < n1; i++ ) {
                locusSuffixArray[ newTextBuffer.toString().codePointAt(i) ] = i;
            }
        }

        // バケツを初期化する。
        setBucket(originalText, bucket, textLength, true);

        int [] LMSIndex = new int [n1];
        int index = 0;
        for ( int i = 1; i < textLength; i++ ) {
            if ( isLMS(type, i) ) {
                LMSIndex[ index ] = i;
                index++;
            }
        }

        for ( int i = 0; i < n1; i++ ) {
            locusSuffixArray[i] = LMSIndex[ locusSuffixArray[i] ];
        }

        Arrays.fill(locusSuffixArray, n1, textLength, -1);

        // ソートされたType-S*をSuffix Arrayに配置する。
        for ( int i = n1 - 1; i >= 0; i-- ) {
            int j = locusSuffixArray[i];
            locusSuffixArray[i] = -1;
            locusSuffixArray[ --bucket[ originalText.codePointAt(j) ] ] = j;
        }

        // Type-S*を使って、Type-Lのソートを行う。
        induceSuffixArrayTypeL( type, locusSuffixArray, originalText, bucket, textLength, false );

        // Type-Lを使って、Type-S(Type-S*)のソートを行う。
        induceSuffixArrayTypeS( type, locusSuffixArray, originalText, bucket, textLength, true );

    }

    /**
     * 
     * <p>
     * コードポイント単位でソートを実行して、Suffix Arrayを構築する。
     * </p>
     * 
     */
    public void build() {

        int textCodePointCount = this.targetText.codePointCount(0, this.targetText.length());

        // 最大文字のコードポイントを取得する。
        int maxCodePoint = getMaxCodePoint( this.targetText, textCodePointCount );

        this.mSuffixArray = new int [ textCodePointCount ];

        // Suffix Arrayをソートする。
        sort( this.targetText, this.mSuffixArray, textCodePointCount, maxCodePoint );

    }

}


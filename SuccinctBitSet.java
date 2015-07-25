import java.util.BitSet;
public class SuccinctBitSet extends BitSet {
    public int getNaiveSelect( final int targetCount, final int pBit ) {
        int select = 0;
        int count1 = 0;
        int count0 = 0;
        do {
            if ( this.get(select) ) {
                count1++;
            } else {
                count0++;
            }
            select++;
        } while ( targetCount + 1 > count1 );
        return select;
    }

    public int getNaiveSelect( final int startIndex, final int pTargetLas, final int pBit ) {
        int select = startIndex;
        int count1 = 0;
        int count0 = 0;
        do {
            if ( this.get(select) ) {
                count1++;
            } else {
                count0++;
            }
            select++;
        } while ( pTargetLas + 1 > count1 );
        return select;
    }

    public int getNaiveRank( final int targetIndex, final int pBit ) {
        int rank = 0;
        for ( int i = 0; i < targetIndex + 1; i++ ) {
            if ( this.get(i) ) {
                ++rank;
            }
        }
        if ( pBit == 0 ) {
            rank = targetIndex - rank;
        }
        return rank;
    }

    public int getNaiveRank( final int targetIndex, final int pTargetLas, final int pBit ) {
        int rank = 0;
        for ( int i = targetIndex + 1; i < pTargetLas + 1; i++ ) {
            if ( this.get(i) ) {
                ++rank;
            }
        }
        if ( pBit == 0 ) {
            rank = ( pTargetLas - targetIndex ) - rank;
        }
        return rank;
    }

    public int[] buildLS() {
        int LS = (int) Math.pow(Math.log(this.length()), 2);
        int LSlength = this.length() / LS;
        int[] p = new int[LSlength + 1];
        p[0] = this.getNaiveRank(0, 1);

        for (int i = 1; i < LSlength + 1; i++) {
            int num = i * LS;
            int las = (i - 1) * LS;
            p[i] = this.getNaiveRank(las, num, 1) + p[i-1];
            //System.out.println(i * LS + "番目までの1の出現回数は：" + p[i]);
        }
        //System.out.println(LSlength * LS + "番目までの1の出現回数は：" + p[LSlength]);
        return p;
    }

    public int[] buildSB (int[] p, int LS) {
        int SB = (int) Math.log(this.length()) / 2;
        int SBlength = this.length() / SB;
        int[] s = new int[SBlength + 1];
        s[0] = 0;

        for (int j = 1; j < SBlength + 1; j++) {
            int num = j * SB;
            int las = (j - 1) * SB;
            if ( 0 == num % LS ) {
                s[j] = 0;
            } else {
                s[j] = this.getNaiveRank(las, num, 1) + s[j-1];
            }
            //System.out.println(j * SB + "番目までの1の出現回数は：" + s[j]);
        }
        //System.out.println(SBlength * SB + "番目までの1の出現回数は：" + s[SBlength]);

        int[][] table =  output1ByteSelect( SB );
        return s;
    }

    public int[][] output1ByteSelect( int SB) {
        // 1バイトで表現できる文字数分ループする。
        int[][] table = new int[(int) Math.pow(2, SB)][SB];
        for ( int number = 0; number < Math.pow(2, SB); number++ ) {
            int currentPos = 0;
            int counter = SB;
            // 1の数ごとの1の位置を出力する。
            for ( int num = 0; num < SB; num++ ) {
                boolean isExisted = false;
                for ( int pos = currentPos; pos < SB; pos++ ) {
                    int bit = 1 & ( number >> pos );
                    currentPos = pos + 1;
                    if ( 1 == bit ) {
                        table[number][num] = pos + 1;
                        isExisted = true;
                        break;
                    }
                }
                // 1の数に対応する位置が存在しない場合は、0を出力しておく。
                if ( !isExisted ) {
                    table[number][num] = 0;
                }
                counter--;
            }
        }
        return table;
    }

    public int[] buildSS (int[] s) {
        int[] ss = new int[s.length];
        ss[0] = 0;
        for (int i = 1; i < s.length; i++) {
            ss[i] = s[i] - s[i - 1];
        }
        return ss;
    }

    public int getRank( final int targetIndex, final int pBit, final int[] p, final int[] s ) {
        int rank = 0;
        int LS = (int) Math.pow(Math.log(this.length()), 2);
        int SB = (int) Math.log(this.length()) / 2;
        int LSquery = (int) Math.round(targetIndex / LS);
        int SBquery = (int) Math.round(targetIndex / SB);
        int SSquery = targetIndex % SB;

        rank += p[LSquery];
        rank += s[SBquery];
        int ppp = SBquery * SB;
        rank += this.getNaiveRank(ppp, ppp + SSquery, 1);

        if ( 0 == pBit ) {
            rank = targetIndex - rank;
        }

        return rank;
    }

    public int getSelect( final int pCount, final int pBit, final int[] p, final int[] s ) {
        int LS = (int) Math.pow(Math.log(this.length()), 2);
        int SB = (int) Math.log(this.length()) / 2;
        int select = 0;
        int block = 0;
        int targetCount = pCount + 1;

        // LB配列を2分探索する。
        int min = 0;
        int max = p.length;
        int lbIndex = 0;
        long value = 0;

        if ( 0 != max ) {
            while ( min < max ) {
                int mid = (min + max) / 2;
                if ( 1 == pBit ) {
                    // 1のrank値を求める場合は、1のrank値を取り出す。
                    value = p[ mid ];
                } else {
                    // 0のrank値を求める場合は、0のrank値を取り出す。
                    value = ((long) mid * LS) - p[mid];
                }
                if ( value >= targetCount ) {
                    max = mid;
                } else {
                    min = mid + 1;
                }
            }
            lbIndex = min - 1;
        }

        // SB配列を線形探索する。
        // 探索開始位置を取得する。
        int sbIndexStart = (lbIndex * (int) LS) / (int) SB;
        // 探索終了位置を取得する。
        int sbIndexEnd = sbIndexStart + (int) (LS / SB);
        if ( s.length <= sbIndexEnd ) {
            sbIndexEnd = s.length - 1;
        }

        // 特定したLB内のselectの値を計算する。
        int restTargetCount = 0;
        if ( 1 == pBit ) {
            // 1の数の残りを取り出す。
            restTargetCount = (int) (targetCount - p[lbIndex]);
        } else {
            // 0の数の残りを取り出す。
            restTargetCount = (int) (targetCount - ( ( lbIndex * LS ) - p[lbIndex] ));
        }

        // SB配列の線形探索を開始する。
        int sbIndex = sbIndexStart;
        while ( sbIndex < sbIndexEnd - 1 ) {
            int sbValue = 0;
            int sbValueNext = 0;
            if ( 1 == pBit ) {
                sbValue = s[sbIndex];
                sbValueNext = s[sbIndex + 1];
            } else {
                int sbTotal = (sbIndex - sbIndexStart) * (int) SB;
                int sbTotalNext = ((sbIndex - sbIndexStart) + 1) * (int) SB;
                sbValue = sbTotal - s[sbIndex];
                sbValueNext = sbTotalNext - s[sbIndex + 1];
            }
            if ( restTargetCount > sbValue && restTargetCount <= sbValueNext ) {
                // 該当のselect値が存在するブロックを発見した。
                break;
            }
            sbIndex++;
        }

        // 特定したSB内のselectの値を計算する。
        int wordRestTargetCount = 0;
        if ( 1 == pBit ) {
            wordRestTargetCount = restTargetCount - s[sbIndex];
        } else {
            wordRestTargetCount = restTargetCount - ((sbIndex * (int) SB) - s[sbIndex]);
        }

        // ここまでの位置を合計する。
        select += lbIndex * LS;
        select += (sbIndex - sbIndexStart) * SB;

        int[][] table =  output1ByteSelect( SB );

        // SB内のselectの値は表引きで求める。
        /*if ( 1 == pBit ) {
            select += getWordSelect(s[sbIndex], wordRestTargetCount, table, SB);
        } else {
            select += getWordSelect(~(s[sbIndex]), wordRestTargetCount, table, SB);
        }
        */

        /*
        mid = (0 + s.length + 1) / 2;
        do {
            if ( targetCount < s[mid] ) {
                mid = mid / 2;
            } else {
                block = mid;
            }
        } while ( block != mid );
        select += block * SB;
        */
        int nokori = targetCount - p[block];
        //select = getNaiveSelect(select, nokori, 1);

        return select;
    }

    /*private int getWordSelect( final int pValue, final int pRestBitNum, int[][] table, int SB ) {

        int select = 0;

        int restBitNum = pRestBitNum;

        // 32bitを8bitで区切った数を取得する。
        int octetNum = (int) (SB / 8);
        int num = 0;
        // 8bit単位にループする。
        while ( num < octetNum ) {
            int shift = num * 8;
            // 8bit単位に1のrank値を取得する。
            int rank = table[ (pValue >> shift) & 0xff ];

            int rest = restBitNum - rank;
            if ( 0 >= rest ) {
                // 求めたいselect値が存在する8bit範囲が特定されたので、ループを終了する。
                break;
            }

            // 求めたいselect値よりも8bitのrankの方が小さい場合は、その8bitには、求めたいselect値がないので、継続してループする。
            restBitNum = rest;

            num++;
        }

        // 探索した8bit分の位置には、select値がなかったので、その位置分selectに加算する。
        select += num * 8;

        // 最後に、求めたいselect値が存在する8bitを表引きする。
        select += table[ (pValue >> (num * 8)) & 0xff ][restBitNum - 1] - 1;

        return select;

    }*/

}

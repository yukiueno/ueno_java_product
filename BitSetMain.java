import java.util.BitSet;
public class BitSetMain {
    public static void main(String[] args) {

        SuccinctBitSet bs = new SuccinctBitSet();
        bs.set(100000000);

        for ( int i = 0; i < bs.length(); i++ ) {
            if ( 0 == ( i % 5 ) ) {
                bs.set(i);
            }
        }

        int query = 100000;
        int pBit = 1;
        int[] p =  bs.buildLS();
        int[] s =  bs.buildSB(p, (int) Math.pow(Math.log(bs.length()), 2));

        int select1 = bs.getNaiveSelect(query, pBit);
        int select2 = bs.getSelect(query, pBit, p, s);

        if (select1 == select2) {
            System.out.println("succeed:" + select1);
        } else {
            System.out.println("faild:" + select2);
            System.out.println("正解は:" + select1);
        }

        for (int que = 0; que < query; que++) {
            //System.out.println(que + "番目までの" + pBit + "の出現回数は：" + bs.getNaiveRank( que, pBit));
            //System.out.println();
            //System.out.println(que + "番目までの" + pBit + "の出現回数は：" + bs.getRank( que, pBit, p, s));
            //bs.getRank( que, pBit, p, s);
            //bs.getNaiveRank( que, pBit);
            //bs.getNaiveSelect(query, pBit);
            //bs.getSelect(query, pBit, p, s);
        }
    }
}

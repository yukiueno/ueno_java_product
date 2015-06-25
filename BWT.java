import java.util.*;

class BWT_array{

    static ArrayList<BWT> make_suffix(String word){
        int p =0;
        for (i = 1; i < ) {
            c = word[n-1];

        }
    }

    static ArrayList<BWT> make_suffix(String word){
        int len = word.length();
        ArrayList<BWT> suffix = new ArrayList<BWT>();
        for(int i=0; i<len;i++){
            suffix.add(new BWT(word.substring(i),i+1));
        }
        return suffix;
    }
    public static void main(String[] args){
        String[] read = SetSequence.getSequenceByOption(args);

        ArrayList<BWT> suffix = new ArrayList<BWT>();
        suffix = make_suffix(String.join("", read));
        // sort
        Collections.sort(suffix, new BWTComparator());
        for (BWT suf: suffix){
            System.out.println(suf.getStr() + suf.getNum());
        }
    }
}

class BWT{
    private int num;
    private String str;

    BWT(String str, int num){
        this.num = num;
        this.str = str;
    }
    public String getStr(){
        return str;
    }
    public int getNum(){
        return num;
    }
}

class BWTComparator implements Comparator<BWT>{
    public int compare(BWT arr1, BWT arr2){
        return arr1.getStr().compareTo(arr2.getStr());
    }
}

import java.util.*;

class Suffix_array{
    static ArrayList<Suffix> make_suffix(String word){
        int len = word.length();
        ArrayList<Suffix> suffix = new ArrayList<Suffix>();
        for(int i=0; i<len;i++){
            suffix.add(new Suffix(word.substring(i),i+1));
        }
        return suffix;
    }
    public static void main(String[] args){
        String word = "adabrakatabra";
        ArrayList<Suffix> suffix = new ArrayList<Suffix>();
        suffix = make_suffix(word);
        // sort
        Collections.sort(suffix, new SuffixComparator());
        for (Suffix suf: suffix){
            System.out.println(suf.getStr() + suf.getNum());
        }
    }
}

class Suffix{
    private int num;
    private String str;

    Suffix(String str, int num){
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

class SuffixComparator implements Comparator<Suffix>{
    public int compare(Suffix arr1, Suffix arr2){
        return arr1.getStr().compareTo(arr2.getStr());
    }
}

import java.util.Random;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Dice {
    private int SIZE = 1000000;
    private int[] dice_spots_type = new int[]{ 0, 1, 2, 3, 4, 5 };

    public int[] roll( final int array_size) {
        int[] arr = new int[array_size];
        int[] spots = new int[dice_spots_type.length];
        boolean state = new Random().nextBoolean();
        for( int i = 0; i < array_size; i++ ) {
            state = this.changeDice(state);
            if ( state ) {
                arr[i] = this.makeSpotsOnDice();
            } else {
                arr[i] = this.makeSpotsOnCrooledDice();
            }
            //System.out.print(arr[i] + " ");
        }

        for( int i = 0; i < array_size; i++  ) {
            for ( int spot = 0; spot < dice_spots_type.length; spot++ ) {
                if ( arr[i] == spot ) {
                    spots[spot]++;
                }
            }
        }
        for ( int spot = 0; spot < dice_spots_type.length; spot++ ) {
            System.out.println(spot + "の数 : " + spots[spot]);
        }
        return arr;
    }

    public boolean changeDice( boolean state ) {
        boolean nextDice;
        int p = new Random().nextInt(100);
        if ( state ) {
            if ( p > 94 ) {
                //System.out.println("\nChange Loaded");
                nextDice = !state;
            } else {
                nextDice = state;
            }
        } else {
            if ( p < 10 ) {
                //System.out.println("\nChange Fair");
                nextDice = !state;
            } else {
                nextDice = state;
            }
        }
        return nextDice;
    }

    public int makeSpotsOnDice() {
        int r = new Random().nextInt(dice_spots_type.length);
        return dice_spots_type[r];
    }

    public int makeSpotsOnCrooledDice() {
        int[] sameProbabilitySpots1 = new int[]{ 0, 1, 2, 3, 4 };
        int[] sameProbabilitySpots2 = new int[]{ 5 };
        int r = 0;

        if ( new Random().nextBoolean() ) {
            r = 5;
        } else {
            r = new Random().nextInt(sameProbabilitySpots1.length);
            r = dice_spots_type[r];
        }
        return r;
    }
}

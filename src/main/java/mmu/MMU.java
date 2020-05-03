package main.java.mmu;

import main.java.mmu.LRU.ReplacementAlgorithm;

import java.io.File;
import java.io.IOException;

public class MMU {

    public MMU(ReplacementAlgorithm algorithm) {
        if(!new File("resources/BACKING_STORE").exists()) {
            try{
                new MakeBACKING_STORE();
            }catch (IOException e){
                System.out.println(e);
            }
        }




    }

    public static void main(String[] args) {
//        new MMU();

    }
}

package main.java.mmu;

public class Test {
    public static void main(String[] args) {
        int addr = 62493;

        //takes off the higher 16 bits leaving only lower 16 bits
        addr = addr % 65536;
        System.out.println(addr);

        //takes off the high 8 bits in the 16 bits leaving lower 8 bits to offset
        int offset = addr % 256;
        System.out.println(offset);

        //gives value of higher 8 bits in the 16 buts leaving higher 8 bits to page number
        int p_num = addr / 256;
        System.out.println(p_num);

    }
}

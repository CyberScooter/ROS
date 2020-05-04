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

    //test========================================

        int vaddr = 48855;
        int page_no = vaddr / 256;
        int f_num = 0;
        int v_offset = (vaddr  & 0x000000ff);

        //15 bit representation
        int phys_add = (f_num << 8) + v_offset;



        int f_num_derived = (phys_add & 0x0000ff00) >> 8;
        int lel = (f_num << 8);

        System.out.println(" physical address " + phys_add);
        System.out.println("page number " + page_no);
        System.out.println("offset: " + 48855);


        System.out.println("515: " + (515 / 256) + " 16916: " + (16916/256) + " 62493: " + (62493/256) + " 30198: " + (30198/256));
        System.out.println(18145 /256);




    }
}

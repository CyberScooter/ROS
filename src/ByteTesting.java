
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ByteTesting {
    public static void main(String[] args) throws Exception {

        //MAKES UP PAGE TABLE, ONLY BELOW PART

        //LAS (LOGICAL ADDRESS SPACE) - 1GB
        //1GB = 2^0 * 2 ^ 30 = 2 ^ 30

        //PAGE SIZE 8KB
        //now if page size is 8KB
        //8KB = 2^3 * 2 ^ 10 = 2^13

        //TAKING BITS 13 (PAGE) AND 30 (LAS)
        //13 BITS IS THE PAGE OFFSET, REMAINDER BETWEEN LAS AND PAGE IS PAGE NUMBER: 17

        //PAGE HAS A PAGE NUMBER OF 17 BITS LOG AND AN OFFSET OF 13 BITS

        //-----------------------------------------------------------

        //PAS (PHYSICAL ADDRESS SPACE) - 64MB
        // 64MB = 2^6 * 2^20 = 2^26

        //USING PAGE SIZE
        //FRAME OFFSET IS SAME AS PAGE SIZE WHICH    IS 2^13
        //SO FRAME OFFSET IS 26 - 13 = 13
        //FRAME OFFSET IS 13 AND FRAME NUMBER IS 13


        File file = new File("file.txt");

        byte[] fileContent = Files.readAllBytes(file.toPath());

        Scanner sc = new Scanner(new File("file.txt"));

        ArrayList<Integer> data = new ArrayList<>();

        while(sc.hasNextInt()){
            int addr = sc.nextInt() % 256;
            System.out.println(addr);
        }

        System.out.println("data " + data.size());


        byte[] logicalAddressSpace = new byte[400];

        logicalAddressSpace[0] = (byte) 234;

        byte[] physicalAddressSpace = new byte[200];


        System.out.println("==============================================================================================================================================");

        System.out.println(fileContent.length);
        System.out.println(Arrays.toString(fileContent));
    }
}

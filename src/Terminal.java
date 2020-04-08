// Java code to illustrate remove() when position of 
// element is passed as parameter 
import java.util.*;

public class Terminal {
    public static void main(String args[])
    {

        // Creating an empty Vector 
        Vector<String> vec_tor = new Vector<String>();

        // Use add() method to add elements in the Vector 
        vec_tor.add("Geeks");
        vec_tor.add("for");
        vec_tor.add("Geeks");
        vec_tor.add("10");
        vec_tor.add("20");

        // Output the Vector 
        System.out.println("Vector: " + vec_tor);

        // Remove the element using remove() 
        String rem_ele = vec_tor.remove(3);

        // Print the removed element 
        System.out.println("Removed element: " + rem_ele);

        // Print the final Vector 
        System.out.println("Final Vector: " + vec_tor);
    }
} 
package org.genevaers.engine;

import java.nio.ByteBuffer;

public class comparebb {
    public static void main(String[] args) {
        ByteBuffer buffer1 = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        ByteBuffer buffer2 = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        ByteBuffer buffer3 = ByteBuffer.wrap(new byte[]{1, 2, 3, 6, 7});
        ByteBuffer buffer4 = ByteBuffer.allocate(5);
        buffer4.put(new byte[]{1, 2, 3, 4, 5});
        buffer4.flip(); // Set limit to current position, then position to zero

        // Using equals()
        System.out.println("buffer1 equals buffer2: " + buffer1.equals(buffer2)); // true
        System.out.println("buffer1 equals buffer3: " + buffer1.equals(buffer3)); // false
        System.out.println("buffer1 equals buffer4: " + buffer1.equals(buffer4)); // true

        // Using compareTo()
        System.out.println("buffer1 compareTo buffer2: " + buffer1.compareTo(buffer2)); // 0
        System.out.println("buffer1 compareTo buffer3: " + buffer1.compareTo(buffer3)); // negative value (buffer1 is less)
        System.out.println("buffer3 compareTo buffer1: " + buffer3.compareTo(buffer1)); // positive value (buffer3 is greater)
    }
}
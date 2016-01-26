package soldierRush;

/**
 * Created by samuel on 1/11/16.
 */
public class Utils {
    public static int pack2(int a, int b) {
        // -32768 <= a,b <= 32767
        return (a << 16) | (b & 0xFFFF);
    }
    public static int[] unpack2(int a) {
        return new int[]{a >> 16, (short)(a & 0xFFFF)};
    }
}
package utils;

/**
 *
 * @author Diego
 */
public class Utils {

    public static String reverseString(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = s.length() - 1; i >= 0; i--) {
            sb.append(s.charAt(i));
        }
        return sb.toString();
    }

    public static String getShape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i))) {
                sb.append('X');
            } else if (Character.isLowerCase(s.charAt(i))) {
                sb.append('x');
            } else if (Character.isDigit(s.charAt(i))) {
                sb.append('d');
            } else {
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }
}

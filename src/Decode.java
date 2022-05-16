import java.io.*;
import java.util.Scanner;

public class Decode {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a source name: ");
        File sourcefile = new File(scanner.nextLine());

        System.out.print("Enter a target file: ");
        File targetfile = new File(scanner.nextLine());

        String[] codes = new String[0];
        int length = -1;
        StringBuilder sbEncode = new StringBuilder();

        try (ObjectInputStream objInput = new ObjectInputStream(new BufferedInputStream(new FileInputStream(sourcefile)))) {
            Object x = objInput.readObject();
            codes = (String[]) x;
            length = objInput.readInt();
            while (true) {
                sbEncode.append(getBits(objInput.readByte()));
            }
        } catch (EOFException ignored) {}

        sbEncode.delete(length, sbEncode.length());

        StringBuilder sbText = new StringBuilder();
        StringBuilder sbCharCode = new StringBuilder();
        for (int i = 0; i < sbEncode.length(); i++) {
            sbCharCode.append(sbEncode.charAt(i));
            for (int j = 0; j < codes.length; j++) {
                if (sbCharCode.toString().equals(codes[j])) {
                    sbText.append((char) j);
                    sbCharCode = new StringBuilder();
                }
            }
        }

        try (PrintWriter pwOutput = new PrintWriter(targetfile)) {
            pwOutput.write(sbText.toString());
        }
    }

    public static String getBits(int value) {
        value = value % 256;
        StringBuilder binaryInteger = new StringBuilder();
        int i = 0;
        int tmp = value >> i;
        for (int j = 0; j < 8; j++) {
            binaryInteger.insert(0, (tmp & 1));
            i++;
            tmp = value >> i;
        }
        return binaryInteger.toString();
    }
}

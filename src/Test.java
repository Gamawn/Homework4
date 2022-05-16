import java.io.*;
import java.util.Scanner;

public class Test {
    public static int SIZE = 2 * 128; // Number of characters in ASCII

    public static void main(String[] args) throws Exception {
        Scanner input = new Scanner(System.in);
        System.out.print("Enter a filename: ");
        String filename = input.nextLine();

        // Read the file and get the counts
        int[] counts = getCounts(filename);

        // Create a Huffman tree
        Tree tree = getHuffmanTree(counts);

        // Assign Huffman codes for characters
        String[] codes = new String[SIZE];
        assignCode(tree.root, codes);

        // Store file size and counts in a file named filename.counts
        storeCounts(filename, new File(filename).length(), counts);

        // Encode the file
        encode(filename, codes);

        System.out.println("The Huffman code is stored in " + filename + ".counts");
        System.out.println("The encoded file is stored in " + filename + ".new");
    }

    public static void storeCounts(String filename, long numberOfCharacters, int[] counts) throws IOException {
        ObjectOutputStream output = new ObjectOutputStream(
                new FileOutputStream(filename + ".counts"));
        output.writeLong(numberOfCharacters);
        output.writeObject(counts);
        output.close();
    }

    public static void encode(String filename, String[] codes) throws IOException {
        BufferedInputStream fileInput = new BufferedInputStream(
                new FileInputStream(filename));
        BitOutputStream output = new BitOutputStream(new File(filename + ".new"));

        int r;
        while ((r = fileInput.read()) != -1 ) {
            output.writeBit(codes[r]);
        }
    }

    // This method is called once after a Huffman tree is built
    public static void assignCode(Tree.Node root, String[] codes) {
        if (root == null) return;

        if (root.left != null) {
            root.left.code = root.code + "0";
            assignCode(root.left, codes);

            root.right.code = root.code + "1";
            assignCode(root.right, codes);
        }
        else {
            codes[root.element] = root.code;
        }
    }

    // Constructs a Huffman tree from the frequency table
    public static Tree getHuffmanTree(int[] counts) {
        Heap<Tree> heap = new Heap<>();

        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0)
                heap.add(new Tree(counts[i], (char)i));
        }

        while (heap.getSize() > 1) {
            Tree t1 = heap.remove();
            Tree t2 = heap.remove();
            heap.add(new Tree(t1, t2));
        }

        return heap.remove();
    }

    public static int[] getCounts(String filename) throws Exception {
        BufferedInputStream fileInput = new BufferedInputStream(
                new FileInputStream(filename));

        int[] counts = new int[SIZE];
        int r;
        while ((r = fileInput.read()) != -1 ) {
            counts[r]++;
        }

        fileInput.close();
        return counts;
    }

    // A linked structure for tree
    public static class Tree implements Comparable<Tree> {
        Node root;

        // Create a new node as the parent of t1 and t2
        public Tree(Tree t1, Tree t2) {
            root = new Node();
            root.left = t1.root;
            root.right = t2.root;
            root.weight = t1.root.weight + t2.root.weight;
        }

        // Create a tree with a single node
        public Tree(int weight, char element) {
            root = new Node(weight, element);
        }

        public int compareTo(Tree o) {
            // Purposely reverse the order so the smallest one will be removed first from the heap
            return Integer.compare(o.root.weight, root.weight);
        }

        public static class Node {
            char element; // For a leaf node, element stores the character in the text
            int weight; // weight is stored in the node
            Node left;
            Node right;
            String code = "";

            public Node() {
            }

            public Node(int weight, char element) {
                this.weight = weight;
                this.element = element;
            }
        }
    }

    // Copied from the text
    public static class Heap<E extends Comparable<E>> {
        private final java.util.ArrayList<E> list = new java.util.ArrayList<>();

        /** Create a default heap */
        public Heap() {
        }

        /** Add a new object into the heap */
        public void add(E newObject) {
            list.add(newObject); // Append to the heap
            int currentIndex = list.size() - 1; // The index of the last node

            while (currentIndex > 0) {
                int parentIndex = (currentIndex - 1) / 2;
                // Swap if the current object is greater than its parent
                if (list.get(currentIndex).compareTo(
                        list.get(parentIndex)) > 0) {
                    E temp = list.get(currentIndex);
                    list.set(currentIndex, list.get(parentIndex));
                    list.set(parentIndex, temp);
                }
                else
                    break; // the tree is a heap now

                currentIndex = parentIndex;
            }
        }

        /** Remove the root from the heap */
        public E remove() {
            if (list.size() == 0) return null;

            E removedObject = list.get(0);
            list.set(0, list.get(list.size() - 1));
            list.remove(list.size() - 1);

            int currentIndex = 0;
            while (currentIndex < list.size()) {
                int leftChildIndex = 2 * currentIndex + 1;
                int rightChildIndex = 2 * currentIndex + 2;

                // Find the maximum between two children
                if (leftChildIndex >= list.size()) break; // The tree is a heap
                int maxIndex = leftChildIndex;
                if (rightChildIndex < list.size()) {
                    if (list.get(maxIndex).compareTo(
                            list.get(rightChildIndex)) < 0) {
                        maxIndex = rightChildIndex;
                    }
                }

                // Swap if the current node is less than the maximum
                if (list.get(currentIndex).compareTo(
                        list.get(maxIndex)) < 0) {
                    E temp = list.get(maxIndex);
                    list.set(maxIndex, list.get(currentIndex));
                    list.set(currentIndex, temp);
                    currentIndex = maxIndex;
                }
                else
                    break; // The tree is a heap
            }

            return removedObject;
        }

        /** Get the number of nodes in the tree */
        public int getSize() {
            return list.size();
        }
    }

    public static class BitOutputStream {
        private final FileOutputStream output;
        private int value;
        private int count = 0;

        public BitOutputStream(File file) throws IOException {
            output = new FileOutputStream(file);
        }

        public void writeBit(char bit) throws IOException {
            count++;
            value = value << 1;

            // The bits are all zeros except the last one
            int mask = 1;
            if (bit == '1')
                value = value | mask;

            if (count == 8) {
                output.write(value);
                count = 0;
            }
        }

        public void writeBit(String bitString) throws IOException {
            for (int i = 0; i < bitString.length(); i++)
                writeBit(bitString.charAt(i));
        }

    }
}
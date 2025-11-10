import java.util.*;

public class TreeBasedSorts {

    static class BSTNode {
        int data;
        BSTNode left, right;
        BSTNode () {

        }
        BSTNode (int data) {
            this.data = data;
            this.left = this.right = null;
        }
    }

    public static BSTNode insertInBST(BSTNode root, int data) {
        if(root == null) return new BSTNode(data);
        if(data <= root.data) root.left = insertInBST(root.left, data);
        else root.right = insertInBST(root.right, data);
        return root;
    }

    public static void inorderTraversal(BSTNode root, List<Integer> inorderList) {
        if(root == null) return;
        inorderTraversal(root.left, inorderList);
        inorderList.add(root.data);
        inorderTraversal(root.right, inorderList);
    }

    public static void BSTSort(List<Integer> arr, List <Integer> sortedList) {
        BSTNode root = null;

        for (int i = 0; i < arr.size(); i++) {
            root = insertInBST(root, arr.get(i));
        }

        inorderTraversal(root, sortedList);
    }


    static class HeapNode {
        int data;
        HeapNode left, right;
        HeapNode(int data) {
            this.data = data;
            this.left = this.right = null;
        }
    }

    
    public static void heapSort(int[] arr) {
        int n = arr.length;

        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }

        for (int i = n - 1; i > 0; i--) {    
            int temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;

            heapify(arr, i, 0);
        }
    }

    
    public static void heapify(int[] arr, int n, int i) {
        int largest = i;       
        int left = 2 * i + 1; 
        int right = 2 * i + 2; 

        if (left < n && arr[left] > arr[largest])
            largest = left;

        if (right < n && arr[right] > arr[largest])
            largest = right;

        if (largest != i) {
            int swap = arr[i];
            arr[i] = arr[largest];
            arr[largest] = swap;

            heapify(arr, n, largest);
        }
    }


    private static int[] generateRandomArray(int size) {
        Random rand = new Random();
        Set<Integer> set = new LinkedHashSet<>();
        while (set.size() < size) {
            set.add(rand.nextInt(size * 10)); // Ensure wide range for uniqueness
        }
        return set.stream().mapToInt(Integer::intValue).toArray();
    }

    public static void main(String[] args) {
        int sizes[] = {100, 1000, 10000, 100000, 200000, 1000000};
        
        for (int n : sizes) {
            int[] array = generateRandomArray(n);
            List<Integer> arr = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                arr.add(array[i]);
            }

            List<Integer> sortedArr = new ArrayList<>();

            long start1 = System.nanoTime();
            BSTSort(arr, sortedArr);
            long end1 = System.nanoTime();

            /*for (int i = 0; i < sortedArr.size(); i++) {
                System.out.print(sortedArr.get(i) + " ");
            }*/

            System.out.println("Time taken by BSTSort for " + n + " inputs: " + (end1 - start1) / 1000000 + " ms");
        }


        System.out.println();

        for (int n : sizes) {
            int[] array = generateRandomArray(n);
            long start2 = System.nanoTime();
            heapSort(array);
            long end2 = System.nanoTime();
            /*for (int i = 0; i < sortedArr.length; i++) {
                System.out.print(sortedArr[i] + " ");
            }*/
            System.out.println("Time taken by HeapSort for " + n + " inputs: " + (end2-start2)/1000000 + " ms");
        }
        

        
    }
}
import java.util.*;

public class KthSmallestLasrgestDC {
    private static int partition(int[] arr, int left, int right) {
        int pivot = arr[right];
        int i = left;
        for (int j = left; j < right; j++) {
            if (arr[j] < pivot) {
                int t = arr[j];
                arr[j] = arr[i];
                arr[i] = t;
                i++;
            }
        }
        int temp = arr[i];
        arr[i] = arr[right];
        arr[right] = temp;
        return i;
    } 

    private static int quickSelect(int[] arr, int left, int right, int k) {
        if (left == right) {
            return arr[left];
        }

        int pi = partition(arr, left, right);
        int rank = pi - left + 1;

        if (k == rank) {
            return arr[pi];
        }
        else if (k < rank) {
            return quickSelect(arr, left, pi-1, k);
        }
        else {
            return quickSelect(arr, pi+1, right, k-rank);
        }
    }

    public static int kthSmallest(int[] arr, int k) {
        return quickSelect(arr, 0, arr.length-1, k);
    }

    public static int kthLargest(int[] arr, int k) {
        return quickSelect(arr, 0, arr.length-1, arr.length-k+1);
    } 


    private static int[] generateRandomArray(int size) {
        Random rand = new Random();
        Set<Integer> set = new LinkedHashSet<>();
        while (set.size() < size) {
            set.add(rand.nextInt(size * 10));
        }
        return set.stream().mapToInt(Integer::intValue).toArray();
    }



    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int[] sizes = {1000000, 200000, 100000, 10000, 1000};
        System.out.println("Enter the value of K: ");
        int k = sc.nextInt(); 

        for (int n : sizes) {
            long start = System.nanoTime();
            int[] arr = generateRandomArray(n);
            
            System.out.println(k + "th smallest element in the aray of size " + n + ": " + kthSmallest(arr, k));
            System.out.println(k + "th largesest element in the aray of size " + n + ": " + kthLargest(arr, k));

            long end = System.nanoTime();

            System.out.println("Time taken to find: " + (end-start)/1000000 + " ms.");
            System.out.println();
        }

        sc.close();
    }
}


import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class SortingAlgorithms {

    // Generate unique random integers
    private static int[] generateRandomArray(int size) {
        Random rand = new Random();
        Set<Integer> set = new LinkedHashSet<>();
        while (set.size() < size) {
            set.add(rand.nextInt(size * 10)); // Ensure wide range for uniqueness
        }
        return set.stream().mapToInt(Integer::intValue).toArray();
    }

    // Bubble Sort
    private static void bubbleSort(int[] arr) {
        int n = arr.length;
        boolean swapped;
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
    }

    // Selection Sort
    private static void selectionSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIdx]) minIdx = j;
            }
            int temp = arr[minIdx];
            arr[minIdx] = arr[i];
            arr[i] = temp;
        }
    }

    // Insertion Sort
    private static void insertionSort(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            int key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    // Quick Sort
    private static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                int temp = arr[i]; arr[i] = arr[j]; arr[j] = temp;
            }
        }
        int temp = arr[i + 1]; arr[i + 1] = arr[high]; arr[high] = temp;
        return i + 1;
    }

    // Merge Sort
    private static void mergeSort(int[] arr, int l, int r) {
        if (l < r) {
            int m = (l + r) / 2;
            mergeSort(arr, l, m);
            mergeSort(arr, m + 1, r);
            merge(arr, l, m, r);
        }
    }

    private static void merge(int[] arr, int l, int m, int r) {
        int n1 = m - l + 1, n2 = r - m;
        int[] L = new int[n1], R = new int[n2];
        System.arraycopy(arr, l, L, 0, n1);
        System.arraycopy(arr, m + 1, R, 0, n2);

        int i = 0, j = 0, k = l;
        while (i < n1 && j < n2) arr[k++] = (L[i] <= R[j]) ? L[i++] : R[j++];
        while (i < n1) arr[k++] = L[i++];
        while (j < n2) arr[k++] = R[j++];
    }

    private static long measureTime(Runnable sortFunction) {
        long start = System.currentTimeMillis();
        sortFunction.run();
        return System.currentTimeMillis() - start;
    }

    public static void main(String[] args) {
        int[] sizes = {100, 1000, 10000, 100000, 200000, 1000000};

        try (PrintWriter writer = new PrintWriter(new FileWriter("execution_times.csv"))) {
            // CSV header
            writer.println("Size,BubbleSort(ms),SelectionSort(ms),InsertionSort(ms),QuickSort(ms),MergeSort(ms)");
            System.out.println("Size,BubbleSort(ms),SelectionSort(ms),InsertionSort(ms),QuickSort(ms),MergeSort(ms)");

            for (int size : sizes) {
                int[] original = generateRandomArray(size);

                long bubbleTime = (size <= 10000) ? measureTime(() -> bubbleSort(Arrays.copyOf(original, size))) : -1;
                long selectionTime = (size <= 10000) ? measureTime(() -> selectionSort(Arrays.copyOf(original, size))) : -1;
                long insertionTime = (size <= 10000) ? measureTime(() -> insertionSort(Arrays.copyOf(original, size))) : -1;
                long quickTime = measureTime(() -> quickSort(Arrays.copyOf(original, size), 0, size - 1));
                long mergeTime = measureTime(() -> mergeSort(Arrays.copyOf(original, size), 0, size - 1));

                // print to console
                System.out.printf("%d,%d,%d,%d,%d,%d%n",
                        size, bubbleTime, selectionTime, insertionTime, quickTime, mergeTime);

                // write to file
                writer.printf("%d,%d,%d,%d,%d,%d%n",
                        size, bubbleTime, selectionTime, insertionTime, quickTime, mergeTime);
            }

            System.out.println("\nExecution times also saved to execution_times.csv");
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

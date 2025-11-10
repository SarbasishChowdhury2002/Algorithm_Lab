import java.util.Arrays;

public class KtElementPseudoMedian {

    
    private static int getMedian(int[] arr, int l, int r) {
        Arrays.sort(arr, l, r + 1); 
        return arr[l + (r - l) / 2];
    }

    // Partition function
    private static int partition(int[] arr, int l, int r, int pivot) {
        int i = l;

        // Move pivot to end
        for (int j = l; j <= r; j++) {
            if (arr[j] == pivot) {
                int temp = arr[j];
                arr[j] = arr[r];
                arr[r] = temp;
                break;
            }
        }

        // Standard partitioning
        for (int j = l; j < r; j++) {
            if (arr[j] < pivot) {
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                i++;
            }
        }

        // Place pivot at correct position
        int temp = arr[i];
        arr[i] = arr[r];
        arr[r] = temp;

        return i;
    }

    // Median of Medians algorithm (k is 1-based)
    public static int kthSmallest(int[] arr, int l, int r, int k) {
        if (l == r) return arr[l];

        int n = r - l + 1;

        // Step 1: Find medians of groups of 5
        int[] medians = new int[(n + 4) / 5];
        int m = 0;

        for (int i = l; i <= r; i += 5) {
            int right = Math.min(i + 4, r);
            medians[m++] = getMedian(arr, i, right);
        }

        // Step 2: Find pivot (median of medians)
        int pivot;
        if (m == 1) {
            pivot = medians[0];
        } else {
            pivot = kthSmallest(medians, 0, m - 1, (m + 1) / 2);
        }

        // Step 3: Partition and recurse
        int pos = partition(arr, l, r, pivot);
        int rank = pos - l + 1;

        if (k == rank) {
            return arr[pos];
        } else if (k < rank) {
            return kthSmallest(arr, l, pos - 1, k);
        } else {
            return kthSmallest(arr, pos + 1, r, k - rank);
        }
    }

    public static void main(String[] args) {
        int[] arr = {10, 20, 45, 30, 99, 55, 33, 44, 55, 22, 22};
        int n = arr.length;
        int k = 4;

        System.out.println(k + "th smallest element is " +
                kthSmallest(arr, 0, n - 1, k));
    }
}

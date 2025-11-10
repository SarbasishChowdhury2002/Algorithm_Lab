

public class HeapSort {
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

        
    public static void main(String[] args) {
        int[] arr = {10, 20, 5, 9, 2, 44, 67, 45, 89, 0};
     
        heapSort(arr);
        for (int i : arr) {
            System.out.print(i + " ");
        }
    }
}

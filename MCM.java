
import java.util.*;

public class MCM {

    // using D&C (Recursive)
    public static int matrixMultiRec(int[] p, int i, int j) {
        if(i == j) return 0;

        int minM = Integer.MAX_VALUE;
        for(int k=i; k<j; k++) {
            int cost = matrixMultiRec(p, i, k) + matrixMultiRec(p, k+1, j) + p[i-1]*p[k]*p[j];
            minM = Math.min(minM, cost);
        }

        return minM;
    }


    // using DP (Tabulation)
    public static int matrixMultiDP(int[] p) {
        int n = p.length;
        int[][] dp = new int[n][n];

        for (int i = 0; i < n; i++) {
            for(int j= 0; j < n; j++) {
                if(i == j) dp[i][j] = 0;
                else {
                    dp[i][j] = Integer.MAX_VALUE;
                }
            }
        }

        for(int d=2; d<n; d++) {
            for(int i=1; i<n-d+1; i++) {
                int j = i+d-1;
                for(int k=i; k<j; k++) {
                    int cost = dp[i][k]+dp[k+1][j]+p[i-1]*p[k]*p[j]; {
                        if(cost < dp[i][j]) {
                            dp[i][j] = cost;
                        }
                    }
                }
            }
        }

        return  dp[1][n-1];
    }


    private static int[] generateRandomArray(int size) {
        Random rand = new Random();
        List<Integer> ls = new ArrayList<>();
        while (ls.size() < size) {
            ls.add(rand.nextInt(100)+1); // avoiding 0 because it leads the ans = 0
        }
        return ls.stream().mapToInt(Integer::intValue).toArray();
    }


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the no. of Matrices to be multiplied: ");
        int n = sc.nextInt();
        
        int[] p = generateRandomArray(n+1);
        System.out.println("Generating dimensions...");
        for (int i = 0; i < p.length; i++) {
            System.err.print(p[i] + " ");
        }

        System.out.println();

        /*System.out.println("Enter the dimensions: ");
        for (int i = 0; i < p.length; i++) {
            p[i] = sc.nextInt();
        }*/


        long start1 = System.nanoTime();
        int cost1 = matrixMultiRec(p, 1, n);
        long end1 = System.nanoTime();
        long time1 = (end1-start1)/1000000;
        System.out.println("Minimum no. of multiplications(D&C Recursive): " + cost1);
        System.out.println("Execution time(ms): " + time1);

        System.out.println();

        long start2 = System.nanoTime();
        int cost2 = matrixMultiDP(p);
        long end2 = System.nanoTime();
        long time2 = (end2-start2)/1000000;
        System.out.println("Minimum no. of multiplications(DP tabulation): " + cost2);
        System.out.println("Execution time(ms): " + time2);

        sc.close();
    }
}

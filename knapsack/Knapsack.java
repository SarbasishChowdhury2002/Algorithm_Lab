
// Given n items of length s1, s2, s3, ..., sn and profits associated with the items p1, p2, p3, ..., pn.
// We have to find the The Maximum Profit we can earn with the subset of the items with total length exactly S.

import java.util.*;

public class Knapsack {

	private static int[] generateRandom(int size) {
        Random rand = new Random();
        Set<Integer> set = new LinkedHashSet<>();
        while (set.size() < size) {
            set.add(rand.nextInt(size * 10));
        }
        return set.stream().mapToInt(Integer::intValue).toArray();
    }


	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the number of items: ");
		int n = sc.nextInt();
		System.out.println("\nEnter the maximum size limit: ");
		int S = sc.nextInt();

		int[] sizes = generateRandom(n);
		int[] profits = generateRandom(n);

		int[][] dp = new int[n+1][S+1];

		for(int i=0; i<=n; i++) {
			for(int j=0; j<=S; j++) {
				if(i==0 || j==0) dp[i][j] = 0;
				else {
					int pick = 0;
					if(sizes[i-1] <= j) pick = profits[i-1] + dp[i-1][j-sizes[i-1]];
					int notPick = 0 + dp[i-1][j];
					dp[i][j] = Math.max(pick, notPick);
				}
			}
		}


		System.out.println("\nThe sizes of the " + n + " items: ");
		for(int i=0; i<n; i++) {
			System.out.print(sizes[i] + " ");
		}
		System.out.println();

		System.out.println("\nThe profits of the " + n + " items: ");
		for(int i=0; i<n; i++) {
            System.out.print(profits[i] + " ");
        }
        System.out.println();

		System.out.println("\nThe maximum profit with the subset items with total size exactly " + S + " is: " + dp[n][S]);

        sc.close();
	}
}

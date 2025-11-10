import java.io.*;
import java.util.*;

public class PCS2ILP {
    static class Edge { int u, v; Edge(int u, int v){ this.u=u; this.v=v; } }
    public static void main(String[] args) throws Exception {
        // default parameters
        int n = 500;         // number of tasks (nodes)
        int m = 10;          // number of processors
        int D = 100;         // deadline (number of slots)
        double edgeProb = 0.004; // probability of precedence edge i->j for i<j

        if (args.length >= 1) n = Integer.parseInt(args[0]);
        if (args.length >= 2) m = Integer.parseInt(args[1]);
        if (args.length >= 3) D = Integer.parseInt(args[2]);
        if (args.length >= 4) edgeProb = Double.parseDouble(args[3]);

        if (D < 1) throw new IllegalArgumentException("Deadline D must be >=1");
        if (m < 1) throw new IllegalArgumentException("Processors m must be >=1");

        System.out.printf("Generating DAG: n=%d, m=%d, D=%d, edgeProb=%.6f\n", n, m, D, edgeProb);

        // Seeded RNG for reproducibility
        Random rng = new Random(1234567);

        // Generate random DAG by only allowing edges i -> j with i < j
        List<Edge> edges = new ArrayList<>();
        for (int i = 1; i <= n; ++i) {
            for (int j = i+1; j <= n; ++j) {
                if (rng.nextDouble() < edgeProb) {
                    edges.add(new Edge(i, j));
                }
            }
        }

        System.out.printf("Generated %d precedence edges.\n", edges.size());

        // Create LP file writer
        String outFile = "pcs.lp";
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outFile)))) {
            
            // Objective: dummy variable for GLPK compatibility
            pw.println("Minimize");
            pw.println(" obj: 0 x_1_1");
            pw.println("Subject To");


            // 1) Each job assigned exactly once: for each j sum_t x_j_t = 1
            for (int j = 1; j <= n; ++j) {
                StringBuilder sb = new StringBuilder();
                sb.append(" assign_job_").append(j).append(":");
                for (int t = 1; t <= D; ++t) {
                    sb.append(" +").append(varName(j,t));
                }
                sb.append(" = 1");
                pw.println(sb.toString());
            }

            // 2) Per-slot capacity: for each t sum_j x_j_t <= m
            for (int t = 1; t <= D; ++t) {
                StringBuilder sb = new StringBuilder();
                sb.append(" slot_cap_").append(t).append(":");
                for (int j = 1; j <= n; ++j) {
                    sb.append(" +").append(varName(j,t));
                }
                sb.append(" <= ").append(m);
                pw.println(sb.toString());
            }

            // 3) Precedence constraints: for each (i->j), sum t*(x_i_t) - sum t*(x_j_t) <= -1
            int ec = 0;
            for (Edge e : edges) {
                ec++;
                StringBuilder sb = new StringBuilder();
                sb.append(" prec_").append(ec).append("_").append(e.u).append("_").append(e.v).append(":");
                for (int t = 1; t <= D; ++t) {
                    sb.append(" +").append(t).append(" ").append(varName(e.u,t));
                }
                for (int t = 1; t <= D; ++t) {
                    sb.append(" -").append(t).append(" ").append(varName(e.v,t));
                }
                sb.append(" <= -1");
                pw.println(sb.toString());
            }

            pw.println("Binary");
            // list all binary vars
            // Order: j from 1..n, t 1..D
            StringBuilder binLine = new StringBuilder();
            int counter = 0;
            for (int j = 1; j <= n; ++j) {
                for (int t = 1; t <= D; ++t) {
                    // keep lines reasonably short
                    pw.print(" " + varName(j,t));
                    counter++;
                    if (counter % 16 == 0) pw.println();
                }
                pw.println();
            }
            pw.println("End");
        }

        System.out.println("Wrote ILP to file: " + outFile);
        printStats(n, m, D, edges.size());
    }

    static String varName(int j, int t) {
        // variable names in LP must avoid spaces; use x_j_t
        return "x_" + j + "_" + t;
    }

    static void printStats(int n, int m, int D, int edges) {
        long vars = (long)n * D;
        long constraints = n + D + edges;
        long nnz = vars /* each variable appears in exactly one job assign row? */ 
                   + (long)D * n  /* per-slot capacity: each slot references all n variables */
                   + (long)edges * D * 2; /* each precedence constraint references t coefficients for i and j -> 2*D per edge */
        // note: the above double-counts some; it's a loose upper estimate of non-zero coefficients

        System.out.println("=== Reduction stats (estimates) ===");
        System.out.println("Number of tasks (n): " + n);
        System.out.println("Processors (m): " + m);
        System.out.println("Deadline (D): " + D);
        System.out.println("Binary variables (n * D): " + vars);
        System.out.println("Linear constraints (approx): " + constraints + "  (n assignment + D slot + |E| precedence)");
        System.out.println("Estimated nonzero coefficients (upper bound): " + nnz);
        System.out.println("Note: LP file size will roughly scale with number of variables * average name/coef length.");
    }
}

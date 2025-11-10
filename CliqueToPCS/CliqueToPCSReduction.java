
import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * CliqueToPCSReduction
 *
 * Builds the PCS instance (unit jobs with precedence constraints, m processors, T=4)
 * that is equivalent to CLIQUE(G,k) according to the Lenstra / Hoogeveen construction.
 *
 * Usage (examples):
 *  - Generate random graph with n vertices and edge probability p:
 *      java CliqueToPCSReduction random 500 0.01 10 output.txt
 *  - Or load edge list from file (each line "u v", 0-based vertex ids):
 *      java CliqueToPCSReduction file vertices k infile.txt output.txt
 *
 * Output format (simple text):
 *  First lines:
 *    JOBS <numJobs>
 *    PROCESSORS <M>
 *    DEADLINE 4
 *  Then lines:
 *    PRECEDENCE u v
 *  (meaning job u must finish before job v starts)
 *
 * Note: job ids are integers [0 .. numJobs-1]. The code documents job-id layout.
 */
public class CliqueToPCSReduction {

    // simple primitive-backed dynamic int array
    static class IntList {
        private int[] a;
        private int size;
        IntList(int cap) { a = new int[Math.max(4, cap)]; size = 0; }
        void add(int x) {
            if (size == a.length) a = Arrays.copyOf(a, a.length * 2);
            a[size++] = x;
        }
        int get(int i) { return a[i]; }
        int size() { return size; }
        int[] toArray() { return Arrays.copyOf(a, size); }
    }

    // stores directed precedence edges (u -> v) as parallel arrays
    static class EdgePairs {
        IntList from = new IntList(1024);
        IntList to = new IntList(1024);
        void add(int u, int v) { from.add(u); to.add(v); }
        int size() { return from.size(); }
        int[] fromArray() { return from.toArray(); }
        int[] toArray() { return to.toArray(); }
    }

    // Simple undirected graph (0..n-1)
    static class Graph {
        final int n;
        final ArrayList<int[]> edges; // store as pairs [u,v] with u<v
        Graph(int n) { this.n = n; edges = new ArrayList<>(); }
        void addEdge(int u, int v) {
            if (u == v) return;
            if (u > v) { int t=u; u=v; v=t; }
            edges.add(new int[]{u,v});
        }
        int edgeCount() { return edges.size(); }
    }

    /**
     * Build the PCS instance according to the construction.
     *
     * Returns an object with:
     *   - numJobs
     *   - processors M
     *   - precedence edges
     *   - and a mapping note printed in main describing job id ranges
     */
    static class PCSInstance {
        int numJobs;
        int processors;
        EdgePairs precedences;
    }

    static PCSInstance reduceCliqueToPCS(Graph G, int k) {
        int n = G.n;
        int m_e = G.edgeCount();
        long l = (long)k * (k-1) / 2L;            // number of edges in k-clique
        if (l > Integer.MAX_VALUE) throw new IllegalArgumentException("k too large");
        int lInt = (int) l;
        int iota = Math.max(n + 1 - k, m_e + 1 - lInt);
        int M = 2 * (iota + lInt); // number of processors

        // counts of dummy jobs, as in the construction
        int Wcnt = M - k;
        int Xcnt = M - n;
        int Ycnt = M - n + k - 1;
        int Zcnt = M - m_e + 1;

        // sanity: total jobs should be 4M
        // jobs: 2 per vertex (J and K): 2*n
        // edge jobs: m_e
        // dummy jobs: Wcnt + Xcnt + Ycnt + Zcnt
        // total = 2n + m_e + (W+X+Y+Z)
        // the construction ensures total == 4M
        // We will not enforce here but will assert at the end.

        // Assign contiguous id blocks
        int id = 0;
        int J_start = id;                  id += n;          // J_v : J_start .. J_start + n-1
        int K_start = id;                  id += n;          // K_v : K_start .. K_start + n-1
        int L_start = id;                  id += m_e;        // L_e : L_start .. L_start + m_e - 1
        int W_start = id;                  id += Wcnt;       // W
        int X_start = id;                  id += Xcnt;       // X
        int Y_start = id;                  id += Ycnt;       // Y
        int Z_start = id;                  id += Zcnt;       // Z

        int totalJobs = id;
        if (totalJobs != 4 * M) {
            // This should not happen for reasonable inputs; print a warning but continue.
            System.err.println("Warning: totalJobs != 4*M: totalJobs=" + totalJobs + " 4*M=" + (4*M));
        }

        EdgePairs precedences = new EdgePairs();

        // Precedences: J_v -> K_v
        for (int v = 0; v < n; v++) {
            int Jv = J_start + v;
            int Kv = K_start + v;
            precedences.add(Jv, Kv);
        }

        // For each edge e=(u,v): L_e, and J_u -> L_e, J_v -> L_e
        for (int ei = 0; ei < G.edges.size(); ei++) {
            int[] uv = G.edges.get(ei);
            int u = uv[0], v = uv[1];
            int Le = L_start + ei;
            precedences.add(J_start + u, Le);
            precedences.add(J_start + v, Le);
        }

        // Dummy precedences:
        // All W -> all Y and all Z
        for (int wi = 0; wi < Wcnt; wi++) {
            int Wjob = W_start + wi;
            for (int yi = 0; yi < Ycnt; yi++) {
                precedences.add(Wjob, Y_start + yi);
            }
            for (int zi = 0; zi < Zcnt; zi++) {
                precedences.add(Wjob, Z_start + zi);
            }
        }
        // All X -> all Z
        for (int xi = 0; xi < Xcnt; xi++) {
            int Xjob = X_start + xi;
            for (int zi = 0; zi < Zcnt; zi++) {
                precedences.add(Xjob, Z_start + zi);
            }
        }

        PCSInstance out = new PCSInstance();
        out.numJobs = totalJobs;
        out.processors = M;
        out.precedences = precedences;
        return out;
    }

    // Small helper to generate an Erdős–Rényi random undirected graph
    static Graph generateRandomGraph(int n, double p) {
        Graph G = new Graph(n);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int u = 0; u < n; u++) {
            for (int v = u + 1; v < n; v++) {
                if (rnd.nextDouble() < p) G.addEdge(u, v);
            }
        }
        return G;
    }

    static void writePCSInstanceToFile(PCSInstance inst, String filename) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(filename))) {
            w.write("JOBS " + inst.numJobs + "\n");
            w.write("PROCESSORS " + inst.processors + "\n");
            w.write("DEADLINE 4\n");
            int[] from = inst.precedences.fromArray();
            int[] to = inst.precedences.toArray();
            for (int i = 0; i < from.length; i++) {
                w.write("PRECEDENCE " + from[i] + " " + to[i] + "\n");
            }
        }
    }

    // Small summary printer telling job-id ranges
    static void printJobLayout(Graph G, int k) {
        int n = G.n;
        int m_e = G.edgeCount();
        int l = (k*(k-1))/2;
        int iota = Math.max(n + 1 - k, m_e + 1 - l);
        int M = 2 * (iota + l);
        int Wcnt = M - k;
        int Xcnt = M - n;
        int Ycnt = M - n + k - 1;
        int Zcnt = M - m_e + 1;
        int id = 0;
        int J_start = id; id += n;
        int K_start = id; id += n;
        int L_start = id; id += m_e;
        int W_start = id; id += Wcnt;
        int X_start = id; id += Xcnt;
        int Y_start = id; id += Ycnt;
        int Z_start = id; id += Zcnt;
        int totalJobs = id;
        System.out.println("GRAPH n=" + n + " m_e=" + m_e + " k=" + k + " l=" + l);
        System.out.println("Computed iota=" + iota + " processors M=" + M);
        System.out.println("Counts: W=" + Wcnt + " X=" + Xcnt + " Y=" + Ycnt + " Z=" + Zcnt);
        System.out.println("Job id layout:");
        System.out.println(" J: [" + J_start + "," + (J_start + n - 1) + "]");
        System.out.println(" K: [" + K_start + "," + (K_start + n - 1) + "]");
        System.out.println(" L: [" + L_start + "," + (L_start + m_e - 1) + "]");
        System.out.println(" W: [" + W_start + "," + (W_start + Math.max(0,Wcnt) - 1) + "]");
        System.out.println(" X: [" + X_start + "," + (X_start + Math.max(0,Xcnt) - 1) + "]");
        System.out.println(" Y: [" + Y_start + "," + (Y_start + Math.max(0,Ycnt) - 1) + "]");
        System.out.println(" Z: [" + Z_start + "," + (Z_start + Math.max(0,Zcnt) - 1) + "]");
        System.out.println("Total jobs (should be 4*M): " + totalJobs + "  (4*M=" + (4*M) + ")");
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage examples:");
            System.err.println("  java CliqueToPCSReduction random <n>=500 <p>=0.01 <k> <outfile>");
            System.err.println("  java CliqueToPCSReduction file <n> <k> <infile> <outfile>");
            return;
        }

        Graph G;
        int k;
        String outfile;

        if (args[0].equalsIgnoreCase("random")) {
            if (args.length < 5) {
                System.err.println("random usage: random n p k outfile");
                return;
            }
            int n = Integer.parseInt(args[1]);
            double p = Double.parseDouble(args[2]);
            k = Integer.parseInt(args[3]);
            outfile = args[4];
            if (n < 500) {
                System.out.println("Warning: you requested n < 500; forcing n = 500 as you asked input graph must have at least 500 vertices.");
                n = 500;
            }
            System.out.println("Generating random G(n=" + n + ", p=" + p + ") ...");
            G = generateRandomGraph(n, p);
            System.out.println("Generated graph with n=" + n + " edges=" + G.edgeCount());
        } else if (args[0].equalsIgnoreCase("file")) {
            if (args.length < 5) {
                System.err.println("file usage: file n k infile outfile");
                return;
            }
            int n = Integer.parseInt(args[1]);
            k = Integer.parseInt(args[2]);
            String infile = args[3];
            outfile = args[4];
            if (n < 500) {
                System.out.println("Warning: forcing n=500 because input graph must have at least 500 vertices.");
                n = 500;
            }
            G = new Graph(n);
            try (BufferedReader br = new BufferedReader(new FileReader(infile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    String[] parts = line.split("\\s+");
                    if (parts.length < 2) continue;
                    int u = Integer.parseInt(parts[0]);
                    int v = Integer.parseInt(parts[1]);
                    G.addEdge(u, v);
                }
            }
            System.out.println("Read graph n=" + G.n + " edges=" + G.edgeCount());
        } else {
            System.err.println("Unknown mode: " + args[0]);
            return;
        }

        // Build reduction
        long t0 = System.currentTimeMillis();
        PCSInstance inst = reduceCliqueToPCS(G, k);
        long t1 = System.currentTimeMillis();
        System.out.println("Reduction built in " + (t1 - t0) + " ms.");
        System.out.println("Output: jobs=" + inst.numJobs + " processors=" + inst.processors +
                " precedenceEdges=" + inst.precedences.size());
        printJobLayout(G, k);

        // Write to file (may be large due to many precedence edges)
        System.out.println("Writing output to " + outfile + " ... (may be large)");
        writePCSInstanceToFile(inst, outfile);
        System.out.println("Done.");
    }
}

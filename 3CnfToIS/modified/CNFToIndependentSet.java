import java.io.*;
import java.util.*;

/**
 * CNFToIndependentSet
 *
 * Usage:
 *   javac CNFToIndependentSet.java
 *   java CNFToIndependentSet input.cnf
 *
 * Reads a DIMACS CNF file where each clause has exactly 3 literals (3-CNF).
 * Constructs an undirected graph for the standard reduction:
 *  - For each clause C_j with literals (l1, l2, l3) create 3 vertices:
 *      v_{j,1}, v_{j,2}, v_{j,3}
 *  - Add triangle edges between the 3 vertices of each clause.
 *  - For any occurrences of complementary literals (x and -x) add edges
 *    between the corresponding vertices.
 *
 * Produces: cnf_graph.dot (Graphviz)
 */
public class CNFToIndependentSet {

    // Internal representation of a clause: exactly 3 ints (literals).
    static class Clause {
        final int[] lits = new int[3];
        Clause(int a, int b, int c) { lits[0] = a; lits[1] = b; lits[2] = c; }
    }

    // Graph stored as adjacency sets (undirected).
    static class Graph {
        final int n;
        final List<Set<Integer>> adj;
        Graph(int n) {
            this.n = n;
            this.adj = new ArrayList<>(n);
            for (int i = 0; i < n; i++) adj.add(new HashSet<>());
        }
        void addEdge(int u, int v) {
            if (u == v) return;
            if (adj.get(u).add(v)) adj.get(v).add(u);
        }
        long edgeCount() {
            long sum = 0;
            for (Set<Integer> s : adj) sum += s.size();
            return sum / 2;
        }
        int vertexCount() { return n; }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java CNFToIndependentSet input.cnf");
            System.exit(1);
        }

        String inputPath = args[0];
        List<Clause> clauses = new ArrayList<>();
        int declaredVars = -1, declaredClauses = -1;

        // 1) Parse DIMACS CNF
        try (BufferedReader br = new BufferedReader(new FileReader(inputPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.startsWith("c") || line.startsWith("C")) continue; // comment
                if (line.startsWith("p ")) {
                    // p cnf numVars numClauses
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 4) {
                        try {
                            declaredVars = Integer.parseInt(parts[2]);
                            declaredClauses = Integer.parseInt(parts[3]);
                        } catch (NumberFormatException nfe) {
                            // ignore, keep -1
                        }
                    }
                    continue;
                }
                // read clause line — may split across lines in DIMACS, but we assume simple one-line clauses
                String[] toks = line.split("\\s+");
                List<Integer> litsLine = new ArrayList<>();
                for (String t : toks) {
                    if (t.equals("0")) break;
                    if (t.length() == 0) continue;
                    try {
                        litsLine.add(Integer.parseInt(t));
                    } catch (NumberFormatException nfe) {
                        // ignore stray tokens
                    }
                }
                if (litsLine.size() == 0) continue;
                if (litsLine.size() != 3) {
                    // If a clause line doesn't have exactly 3 literals, try to be robust:
                    // - If more than 3, take first 3
                    // - If less than 3, skip and warn
                    if (litsLine.size() > 3) {
                        int a = litsLine.get(0), b = litsLine.get(1), c = litsLine.get(2);
                        clauses.add(new Clause(a, b, c));
                    } else {
                        System.err.println("Warning: skipping clause with != 3 literals: " + litsLine);
                    }
                } else {
                    clauses.add(new Clause(litsLine.get(0), litsLine.get(1), litsLine.get(2)));
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read input file: " + e.getMessage());
            System.exit(2);
        }

        if (declaredClauses != -1 && declaredClauses != clauses.size()) {
            System.out.println("Note: header declared " + declaredClauses + " clauses but parsed " + clauses.size());
        }

        int m = clauses.size();
        if (m == 0) {
            System.err.println("No clauses parsed. Exiting.");
            System.exit(3);
        }

        final long t0 = System.nanoTime();

        // 2) Build graph
        // vertex id: vid = clauseIndex * 3 + litPos (0..2)
        int n = 3 * m;
        Graph g = new Graph(n);

        // map literal value (like 5 or -5) to list of vertex ids where it occurs
        Map<Integer, List<Integer>> occ = new HashMap<>(m * 3);

        for (int ci = 0; ci < m; ci++) {
            Clause c = clauses.get(ci);
            for (int li = 0; li < 3; li++) {
                int lit = c.lits[li];
                int vid = ci * 3 + li;
                occ.computeIfAbsent(lit, k -> new ArrayList<>()).add(vid);
            }
        }

        // add triangle edges inside each clause
        for (int ci = 0; ci < m; ci++) {
            int v0 = ci * 3;
            int v1 = ci * 3 + 1;
            int v2 = ci * 3 + 2;
            g.addEdge(v0, v1);
            g.addEdge(v1, v2);
            g.addEdge(v0, v2);
        }

        // add edges between complementary literals: for literal l and -l connect all occurrences
        // to avoid double-work iterate through occ.keySet and for l where l > 0 connect with -l (or vice versa)
        Set<Integer> processed = new HashSet<>();
        for (Integer lit : new ArrayList<>(occ.keySet())) {
            if (processed.contains(lit)) continue;
            int neg = -lit;
            List<Integer> listPos = occ.getOrDefault(lit, Collections.emptyList());
            List<Integer> listNeg = occ.getOrDefault(neg, Collections.emptyList());
            if (!listPos.isEmpty() && !listNeg.isEmpty()) {
                for (int u : listPos) {
                    for (int v : listNeg) {
                        g.addEdge(u, v);
                    }
                }
            }
            processed.add(lit);
            processed.add(neg);
        }

        final long t1 = System.nanoTime();

        // 3) Write DOT with readable labels
        String dotPath = "cnf_graph.dot";
        try (PrintWriter pw = new PrintWriter(new FileWriter(dotPath))) {
            pw.println("graph cnf_is_graph {");
            pw.println("  overlap=false;");
            pw.println("  splines=true;");
            // node labels: C<clauseIndex+1>_L<litPos+1>: <literal>
            for (int ci = 0; ci < m; ci++) {
                for (int li = 0; li < 3; li++) {
                    int vid = ci * 3 + li;
                    int literal = clauses.get(ci).lits[li];
                    String label = String.format("C%d_L%d: %s", ci+1, li+1, literal);
                    // escape quotes if any (shouldn't be)
                    label = label.replace("\"", "\\\"");
                    pw.printf("  %d [label=\"%s\"];\n", vid, label);
                }
            }
            // edges
            for (int u = 0; u < g.n; u++) {
                for (int v : g.adj.get(u)) {
                    if (u < v) pw.printf("  %d -- %d;\n", u, v);
                }
            }
            pw.println("}");
        } catch (IOException e) {
            System.err.println("Failed to write DOT file: " + e.getMessage());
            System.exit(4);
        }

        final long t2 = System.nanoTime();

        long buildMs = (t1 - t0) / 1_000_000;
        long writeMs = (t2 - t1) / 1_000_000;
        long totalMs = (t2 - t0) / 1_000_000;

        System.out.println("Input CNF: " + inputPath);
        if (declaredVars != -1) System.out.println("Declared variables: " + declaredVars);
        System.out.println("Parsed clauses: " + m);
        System.out.println("Graph vertices (n): " + g.vertexCount());
        System.out.println("Graph edges (m): " + g.edgeCount());
        System.out.println(String.format("Time: build=%d ms, writeDot=%d ms, total=%d ms", buildMs, writeMs, totalMs));

        // Provide complexity notes
        System.out.println("\nNotes:");
        System.out.println(" - Graph has 3 vertices per clause (n = 3 * #clauses).");
        System.out.println(" - Triangles: 3 edges per clause (3 * #clauses).");
        System.out.println(" - Complement edges: for variable x with p occurrences and !x with q occurrences");
        System.out.println("   you add p*q edges; worst-case this sums to O(n^2) edges.");
        System.out.println("\nDOT written to: " + dotPath);
        System.out.println("Render with: dot -Tpng " + dotPath + " -o cnf_graph.png");
    }
}

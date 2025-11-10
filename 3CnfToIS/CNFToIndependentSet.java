

import java.io.*;
import java.util.*;

/**
 * Reduction from 3-CNF (list of clauses) to an undirected graph for Independent Set.
 * Each clause is an array of 3 integers (literals): positive for x_i, negative for ~x_i.
 *
 * Output: Graphviz DOT to stdout or to a file.
 */
public class CNFToIndependentSet {

    // Clause holds three literal ints.
    static class Clause {
        int[] lits = new int[3];
        Clause(int a, int b, int c) { lits[0]=a; lits[1]=b; lits[2]=c; }
    }

    public static void main(String[] args) throws Exception {
        // Example usage: construct a formula programmatically
        // Replace with parser if you want to read from file.
        List<Clause> clauses = new ArrayList<>();

        // Small example (3 clauses)
        clauses.add(new Clause(1, -2, 3));
        clauses.add(new Clause(-1, 2, 4));
        clauses.add(new Clause(-3, -4, 5));

        // Or to test with m = 200, uncomment and create 200 clauses programmatically:
        // for (int j=0; j<200; j++) clauses.add(new Clause(j%50+1, -( (j+1)%50+1 ), (j+2)%50+1 ));

        Graph g = buildGraphFrom3CNF(clauses);
        String dot = g.toDot("cnf_is_graph");
        // write to file
        try (PrintWriter out = new PrintWriter(new FileWriter("cnf_graph.dot"))) {
            out.println(dot);
        }
        System.out.println("Wrote cnf_graph.dot (|V|=" + g.numVertices() + ", |E|=" + g.numEdges() + ")");
        System.out.println("Render with: dot -Tpng cnf_graph.dot -o cnf_graph.png");
    }

    // Graph represented as adjacency sets (undirected).
    static class Graph {
        int n;
        List<Set<Integer>> adj;
        Graph(int n) {
            this.n = n;
            adj = new ArrayList<>(n);
            for (int i=0;i<n;i++) adj.add(new HashSet<>());
        }
        void addEdge(int u, int v) {
            if (u==v) return;
            if (adj.get(u).add(v)) adj.get(v).add(u);
        }
        int numVertices() { return n; }
        long numEdges() {
            long sum=0;
            for (Set<Integer> s: adj) sum += s.size();
            return sum/2;
        }
        String toDot(String name) {
            StringBuilder sb = new StringBuilder();
            sb.append("graph ").append(name).append(" {\n");
            // Optionally label nodes with clause/literal info
            for (int i=0;i<n;i++) {
                sb.append("  ").append(i).append(" [label=\"").append(i).append("\"];\n");
            }
            Set<String> written = new HashSet<>();
            for (int u=0;u<n;u++) {
                for (int v: adj.get(u)) {
                    if (u < v) {
                        sb.append("  ").append(u).append(" -- ").append(v).append(";\n");
                    }
                }
            }
            sb.append("}\n");
            return sb.toString();
        }
    }

    // Build the graph: returns Graph where vertex id = clauseIndex*3 + literalIndex(0..2)
    static Graph buildGraphFrom3CNF(List<Clause> clauses) {
        int m = clauses.size();
        int n = 3*m;
        Graph g = new Graph(n);

        // Map literal value -> list of vertex ids where that literal occurs
        // literal is integer like 1, -1, 2, -2, ...
        Map<Integer, List<Integer>> occ = new HashMap<>();

        // create vertices and record occurrences
        for (int ci=0; ci<m; ci++) {
            Clause c = clauses.get(ci);
            for (int li=0; li<3; li++) {
                int lit = c.lits[li];
                int vid = ci*3 + li;
                occ.computeIfAbsent(lit, k->new ArrayList<>()).add(vid);
            }
        }

        // 1) add triangle edges inside each clause
        for (int ci=0; ci<m; ci++) {
            int v0 = ci*3 + 0;
            int v1 = ci*3 + 1;
            int v2 = ci*3 + 2;
            g.addEdge(v0, v1);
            g.addEdge(v1, v2);
            g.addEdge(v0, v2);
        }

        // 2) add complement edges: for each variable, connect occurrences of x with occurrences of -x
        Set<Integer> seen = new HashSet<>();
        for (Integer lit : new ArrayList<>(occ.keySet())) {
            if (seen.contains(lit)) continue;
            int neg = -lit;
            List<Integer> posList = occ.getOrDefault(lit, Collections.emptyList());
            List<Integer> negList = occ.getOrDefault(neg, Collections.emptyList());
            // connect every u in posList with every v in negList
            for (int u : posList) {
                for (int v : negList) {
                    g.addEdge(u, v);
                }
            }
            seen.add(lit);
            seen.add(neg);
        }
        return g;
    }
}

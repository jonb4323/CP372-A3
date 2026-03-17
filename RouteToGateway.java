// imports
import java.util.*;

/**
 * RouteToGateway.java
 *
 * CP372 – Computer Networks (Winter 2026)
 * Assignment 3: Policy-Based Link State Routing
 *
 * This program computes forwarding tables for all non-gateway routers in a
 * directed weighted graph (an Autonomous System). A Security Agent (SA) router
 * enforces the policy that every path to a gateway must pass through the SA.
 *
 * Policy rules:
 * - Every datagram leaving the AS must pass through the SA before reaching a gateway.
 * - A gateway cannot be an intermediate node on the way TO the SA.
 * - Once a packet leaves the SA, the first gateway it reaches is its exit point.
 *
 * Algorithm overview:
 * 1. Read the graph (adjacency matrix), gateway list, and SA index.
 * 2. Build the TRANSPOSE graph (flip all edge directions).
 * 3. Run Dijkstra on the TRANSPOSE from SA, forbidding gateways as intermediates.
 *    This gives shortest distances from every non-gateway node TO the SA.
 * 4. For each gateway G, run Dijkstra on the ORIGINAL graph from SA (no gateway
 *    restriction on this leg). This gives cost SA→G.
 * 5. Combine: cost(src→gateway) = dist_to_SA[src] + dist_SA_to_gateway[gateway]
 *
 * Compilation: javac RouteToGateway.java
 * Usage:       java RouteToGateway < input.txt
 */
public class RouteToGateway {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** Sentinel for "no edge" in the input adjacency matrix. */
    private static final int NO_EDGE = -1; // -1 means no direct link between two routers

    /** A large value representing infinity (unreachable). Divided by 2 to prevent overflow when adding. */
    private static final long INF = Long.MAX_VALUE / 2; // use half of max to avoid overflow during addition

    // -------------------------------------------------------------------------
    // Main entry point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in); // create a scanner to read from standard input

        // ---- Read the number of routers ----
        int n = Integer.parseInt(sc.nextLine().trim()); // parse the first line as the number of routers

        // ---- Read the adjacency matrix ----
        // adj[i][j] = weight of directed edge from router (i+1) to router (j+1),
        //             or NO_EDGE (-1) if no edge exists
        long[][] adj = new long[n][n]; // allocate the n x n adjacency matrix
        for (int i = 0; i < n; i++) { // iterate over each row of the matrix
            StringTokenizer st = new StringTokenizer(sc.nextLine()); // tokenize the current line by whitespace
            for (int j = 0; j < n; j++) { // iterate over each column in the row
                adj[i][j] = Long.parseLong(st.nextToken()); // parse each weight value from the token
            }
        }

        // ---- Read gateway router indices (1-based in input) ----
        // Convert to 0-based internally throughout.
        String[] gatewayTokens = sc.nextLine().trim().split("\\s+"); // split the gateway line by whitespace
        int k = gatewayTokens.length; // k = number of gateway routers

        boolean[] isGateway = new boolean[n]; // isGateway[i] = true if router i+1 is a gateway
        int[] gateways = new int[k];           // 0-based gateway indices
        for (int i = 0; i < k; i++) { // loop through each gateway token
            gateways[i] = Integer.parseInt(gatewayTokens[i]) - 1; // convert from 1-based to 0-based
            isGateway[gateways[i]] = true; // mark this router as a gateway in the boolean array
        }

        // ---- Read SA index (1-based) ----
        // Take the first token on the line for robustness (SA is always a single integer).
        int sa = Integer.parseInt(sc.nextLine().trim().split("\\s+")[0]) - 1; // convert SA index to 0-based

        // ---- Build the TRANSPOSE graph ----
        // transAdj[i][j] = adj[j][i] (reverse all edge directions).
        //
        // Running Dijkstra on the transpose from destination D gives shortest paths
        // from every node v toward D in the original graph.
        long[][] transAdj = new long[n][n]; // allocate the transpose adjacency matrix
        for (int i = 0; i < n; i++) { // iterate over each row
            for (int j = 0; j < n; j++) { // iterate over each column
                transAdj[i][j] = adj[j][i]; // flip the direction: transpose entry
            }
        }

        // =====================================================================
        // Step A: Dijkstra on TRANSPOSE from SA (with gateway restriction)
        //
        //   distToSA[v]  = shortest distance from v to SA in the ORIGINAL graph
        //   predToSA[v]  = next-hop from v toward SA in the original graph
        //                  (= predecessor of v in the transpose shortest-path tree)
        //
        // FORBIDDEN intermediate nodes: gateways may NOT appear as intermediates
        // on the path from src to SA (policy rule: a gateway cannot be on the way
        // to the SA). We enforce this by not relaxing edges THROUGH gateway nodes
        // during Dijkstra (a gateway can still be the source, just not a relay).
        // =====================================================================
        long[] distToSA = new long[n]; // array to store shortest distance from each node to SA
        int[] predToSA = new int[n];   // array to store the next-hop (predecessor) from each node toward SA

        // run Dijkstra from SA on the transpose graph, forbidding gateways as intermediates
        dijkstra(transAdj, sa, n, isGateway, distToSA, predToSA);

        // =====================================================================
        // Step B: Dijkstra on ORIGINAL graph from SA (no gateway restriction)
        //
        // distFromSA[v] = shortest distance from SA to v in the original graph
        // predFromSA[v] = predecessor of v on shortest path from SA
        //
        // No gateway restriction here because once a packet leaves SA, the first
        // gateway it reaches is its exit point. Gateways CAN appear on SA→gateway paths.
        // =====================================================================
        long[] distFromSA = new long[n]; // distances from SA to every node in original graph
        int[] predFromSA = new int[n];   // predecessors from SA to every node in original graph

        // run standard Dijkstra from SA on original graph, NO gateway restrictions
        dijkstra(adj, sa, n, null, distFromSA, predFromSA);

        // =====================================================================
        // Step C: Build and print forwarding tables for all non-gateway routers.
        //
        // For source router src (not a gateway):
        //   total cost = distToSA[src] + distFromSA[gateway]
        //   next hop from src = predToSA[src]  (first step toward SA)
        //
        // Special case – if src == SA:
        //   distToSA[sa] = 0 (SA is already at the SA).
        //   Next hop = first hop from SA toward gateway (from predFromSA tree).
        //
        // If either leg is unreachable (INF), output -1 / -1.
        // =====================================================================

        
        }
    }
}

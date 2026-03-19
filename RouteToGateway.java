
// imports
import java.util.*;

public class RouteToGateway {
    private static final int NO_EDGE = -1; // no link
    private static final long INF = Long.MAX_VALUE / 2; // overflow stop

    public static void main(String[] args) {
        Scanner myScanner = new Scanner(System.in); // for reading router info
        int n = Integer.parseInt(myScanner.nextLine().trim());

        long[][] adj = new long[n][n];
        for (int i = 0; i < n; i++) {
            StringTokenizer strTokenizer = new StringTokenizer(myScanner.nextLine());
            for (int j = 0; j < n; j++) {
                adj[i][j] = Long.parseLong(strTokenizer.nextToken());
            }
        }
        String[] gatewayTokens = myScanner.nextLine().trim().split("\\s+");
        int numOfGateways = gatewayTokens.length;
        boolean[] isGateway = new boolean[n];
        int[] gateways = new int[numOfGateways];

        for (int i = 0; i < numOfGateways; i++) {
            gateways[i] = Integer.parseInt(gatewayTokens[i]) - 1;
            isGateway[gateways[i]] = true;
        }

        int secAgent = Integer.parseInt(myScanner.nextLine().trim().split("\\s+")[0]) - 1;
        myScanner.close(); // save resources

        long[][] transAdj = new long[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                transAdj[i][j] = adj[j][i];
            }
        }

        long[] distToSA = new long[n];
        int[] predToSA = new int[n];
        dijkstra(transAdj, secAgent, n, isGateway, distToSA, predToSA);

        long[] distFromSA = new long[n];
        int[] predFromSA = new int[n];
        dijkstra(adj, secAgent, n, null, distFromSA, predFromSA);

        List<Integer> nonGateways = new ArrayList<>(); // tracking index of the nongateway routers
        for (int v = 0; v < n; v++) {
            if (!isGateway[v])
                nonGateways.add(v);
        }
        Collections.sort(nonGateways);

        for (int src : nonGateways) {
            System.out.println("Forwarding table for " + (src + 1));
            System.out.println("\tTo\tCost\tNext Hop");

            for (int gi = 0; gi < numOfGateways; gi++) {
                int gw = gateways[gi];

                if (src == secAgent) {
                    long cost = distFromSA[gw];

                    if (cost >= INF) {
                        System.out.println("\t" + (gw + 1) + "\t-1\t-1");
                    } else {
                        int nextHop = getFirstHop(predFromSA, secAgent, gw);
                        if (nextHop == -1) {
                            System.out.println("\t" + (gw + 1) + "\t-1\t-1");
                        } else {
                            System.out.println("\t" + (gw + 1) + "\t" + cost + "\t" + (nextHop + 1));
                        }
                    }
                } else {
                    long costToSA = distToSA[src];
                    long costSAtoGW = distFromSA[gw];

                    if (costToSA >= INF || costSAtoGW >= INF || predToSA[src] == -1) {
                        System.out.println("\t" + (gw + 1) + "\t-1\t-1");
                    } else {
                        long totalCost = costToSA + costSAtoGW;
                        System.out.println("\t" + (gw + 1) + "\t" + totalCost + "\t" + (predToSA[src] + 1));
                    }
                }
            }
            System.out.println();
        }
    }

    private static int getFirstHop(int[] pred, int source, int dest) { // find first hop
        int cur = dest;
        while (pred[cur] != source && pred[cur] != -1) {
            cur = pred[cur];
        }
        // return -1 if chain breaks before reaching source
        if (pred[cur] == -1 && cur != source)
            return -1;
        return cur;
    }

    // dijkstras algo
    private static void dijkstra(long[][] adjMatrix, int source, int n, boolean[] isGateway, long[] dist, int[] pred) {
        Arrays.fill(dist, INF);
        Arrays.fill(pred, -1);
        dist[source] = 0;

        PriorityQueue<long[]> priorityQ = new PriorityQueue<>(Comparator.comparingLong(a -> a[0]));
        priorityQ.offer(new long[] { 0L, source });

        boolean[] visited = new boolean[n];

        while (!priorityQ.isEmpty()) {
            long[] top = priorityQ.poll();
            long dCur = top[0];
            int u = (int) top[1];

            if (visited[u])
                continue;
            visited[u] = true;
            if (isGateway != null && isGateway[u] && u != source)
                continue;

            for (int v = 0; v < n; v++) {
                if (v == u)
                    continue;

                long w = adjMatrix[u][v];

                if (w == NO_EDGE)
                    continue;

                long newDist = dCur + w;

                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    pred[v] = u;
                    priorityQ.offer(new long[] { newDist, v });
                }
            }
        }
    }
}
package simple;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
// import javax.security.auth.kerberos.KeyTab;
// import javax.swing.text.Segment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.random.RandomGenerator;

class Pair<K, V> {
    public K a;
    public V b;

    public Pair(K a, V b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", a, b);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair<?, ?> other)) return false;
        return Objects.equals(a, other.a) && Objects.equals(b, other.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }
}
class Geometry {
    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }
    public static double squareDistance(double x1, double y1, double x2, double y2){
        return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
    }
    public static double angle(double x1, double y1, double x2, double y2, double x3, double y3){
        double cosA = dot(x2 - x1, y2 - y1, x3 - x1, y3 - y1) / distance(x1, y1, x2, y2) / distance(x1, y1, x3, y3);
        return Math.acos(cosA);
    }
    public static double cross(double x1, double y1, double x2, double y2) {
        return x1 * y2 - y1 * x2;
    }
    public static double dot(double x1, double y1, double x2, double y2) {
        return x1 * x2 + y1 * y2;
    }
}

class Point2D {
    public static Point2D infPoint = new Point2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    public static double eps = 1e-9;
    private double x, y;
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("Point2D(%f, %f)", x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Point2D other)) return false;
        return Math.abs(x - other.x) < eps && Math.abs(y - other.y) < eps;
//        return Double.compare(x, other.x) == 0 && Double.compare(y, other.y) == 0;
    }

    public double distance(Point2D o) {
//        return Math.hypot(x - o.x, y - o.y);
        return Geometry.distance(x, y, o.getX(), o.getY());
    }
    public double distance(Line2D line) {
        return Math.abs(line.getA() * x + line.getB() * y + line.getC()) / Math.hypot(line.getA(), line.getB());
    }

    public double distance(Segment2D seg) {
        Point2D x = seg.getX(), y = seg.getY();
        if (x.getX() == y.getX() && x.getY() == y.getY()) {
            return distance(x);
        }
        if (Geometry.dot(x.getX() - y.getX(), x.getY() - y.getY(), y.getX() - this.x, y.getY() - this.y) > 0) {
            return distance(y);
        }
        if (Geometry.dot(x.getX() - y.getX(), x.getY() - y.getY(), x.getX() - this.x, x.getY() - this.y) < 0) {
            return distance(x);
        }
//        return distance(new Line2D(x.getY() - y.getY(), y.getX() - x.getX(), x.cross(y)));
        return Math.abs((x.getY() - y.getY()) * this.x + (y.getX() - x.getX()) * this.y + x.cross(y)) /
                Math.hypot(x.getY() - y.getY(), y.getX() - x.getX());
    }

    public double cross(Point2D o) {
        return Geometry.cross(x, y, o.getX(), o.getY());
    }
    public double cross(Point2D o1, Point2D o2) {
        return Geometry.cross(o1.getX() - x, o1.getY() - y, o2.getX() - x, o2.getY() - y);
    }
    public double dot(Point2D o) {
        return Geometry.dot(x, y, o.getX(), o.getY());
    }

    public double minAngle(List<Point2D> points) {
        int n = points.size();
        List<Integer> id = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            id.add(i);
        }
        List<Double> angle = new ArrayList<>();
        for (Point2D p : points) {
            angle.add(Math.atan2(p.getY() - y, p.getX() - x));
        }
        id.sort(Comparator.comparingDouble(angle::get));

        double min = Double.MAX_VALUE;
        double total = Math.PI * 2;
        for (int i = 0; i + 1 < points.size(); i++) {
            total -= angle.get(id.get(i + 1)) - angle.get(id.get(i));
            min = Math.min(min, angle.get(id.get(i + 1)) - angle.get(id.get(i)));
        }
        min = Math.min(min, total);
        return min;
    }

    public int countCoincide(List<Point2D> points) {
        int n = points.size();
        List<Integer> id = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            id.add(i);
        }
        List<Double> angle = new ArrayList<>();
        for (Point2D p : points) {
            angle.add(Math.atan2(p.getY() - y, p.getX() - x));
        }
        id.sort(Comparator.comparingDouble(angle::get));

        int cnt = 0;
        int res = 0;
        for (int i = 0; i + 1 < points.size(); i++) {
//            if (Math.abs(angle.get(id.get(i + 1)) - angle.get(id.get(i))) < 1e-15) {
            if (Math.abs(angle.get(id.get(i + 1)) - angle.get(id.get(i))) < eps) {
                cnt++;
            }
            else {
                res += cnt * (cnt + 1) / 2;
            }
        }
        res += cnt * (cnt + 1) / 2;
        return res;
    }

    public Point2D ray(double angle, int minX, int minY, int maxX, int maxY) {
        double vx = Math.cos(angle);
        double vy = Math.sin(angle);

        // u = x + t * vx
        // v = y + t * vy
        double u, v, t;

        // cut minX
        if (Math.abs(vx) >= eps) {
            t = (minX - x) / vx;
            if (t > -eps) {
                u = minX;
                v = y + t * vy;
                if (v >= minY && v <= maxY) {
                    return new Point2D(u, v);
                }
            }
        }

        // cut maxX
        // System.err.println("vx = " + vx + " vy = " + vy + " minX = " + minX + " minY = " + minY + " maxX = " + maxX + " maxY = " + maxY);
        if (Math.abs(vx) >= eps) {
            t = (maxX - x) / vx;
            if (t > -eps) {
                u = maxX;
                v = y + t * vy;
                if (v >= minY && v <= maxY) {
                    return new Point2D(u, v);
                }
            }
        }

        // cut minY
        if (Math.abs(vy) >= eps) {
            t = (minY - y) / vy;
            if (t > -eps) {
                u = x + t * vx;
                v = minY;
                if (u >= minX && u <= maxX) {
                    return new Point2D(u, v);
                }
            }
        }

        // cut maxY
        if (Math.abs(vy) >= eps) {
            t = (maxY - y) / vy;
            if (t > -eps) {
                u = x + t * vx;
                v = maxY;
                if (u >= minX && u <= maxX) {
                    return new Point2D(u, v);
                }
            }
        }

        return null;
    }

    public int countCoincide(List<Point2D> points, Point2D point) {
        int n = points.size();
        double v = Math.atan2(point.getY() - y, point.getX() - x);
        int cnt = 0;
        for (Point2D p : points) {
            if (Math.abs(Math.atan2(p.getY() - y, p.getX() - x) - v) < eps) {
                cnt++;
            }
        }
        return cnt;
    }

    public double getX() {
        return x;
    }
    public double x() {
        return x;
    }

    public double getY() {
        return y;
    }
    public double y() {
        return y;
    }
}

class Line2D {
    private double a, b, c;
    public Line2D(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public String toString() {
        return String.format("Line2D(%f, %f, %f)", a, b, c);
    }

    public Point2D intersect(Line2D line) {
        double d = a * line.b - line.a * b;
        if (d == 0) {
            if (b * line.c == line.b * c || a * line.c == line.a * c) {
                return Point2D.infPoint;
            }
            return null;
        }
        double x = (b * line.c - line.b * c) / d;
        double y = (c * line.a - line.c * a) / d;
        return new Point2D(x, y);
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getC() {
        return c;
    }
}

class Segment2D {
    private Point2D x, y;
    public Segment2D(Point2D x, Point2D y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("Segment2D(%s, %s)", x, y);
    }

    public Point2D intersect(Segment2D seg) {
//        if (x.getX() == seg.getX().getX() && x.getY() == seg.getX().getY() && y.getX() == seg.getY().getX() && y.getY() == seg.getY().getY() ||
//            x.getX() == seg.getY().getX() && x.getY() == seg.getY().getY() && y.getX() == seg.getX().getX() && y.getY() == seg.getX().getY()) {
//        if (x.equals(seg.x) && y.equals(seg.y) || x.equals(seg.y) && y.equals(seg.x)) {
//            return null;
//        }
        Line2D l1 = new Line2D(x.getY() - y.getY(), y.getX() - x.getX(), x.cross(y));
        Line2D l2 = new Line2D(seg.x.getY() - seg.y.getY(), seg.y.getX() - seg.x.getX(), seg.x.cross(seg.y));
//        if (x.getX() == 17 && x.getY() == 10 && y.getX() == 11 && y.getY() == 15 && seg.getX().getX() == 13 && seg.getX().getY() == 16 && seg.getY().getX() == 10 && seg.getY().getY() == 6) {
//            System.err.printf("(%f %f %f) (%f %f %f)\n", l1.getA(), l1.getB(), l1.getC(), l2.getA(), l2.getB(), l2.getC());
//        }

        Point2D p = l1.intersect(l2);
        if (p == null) return null;
        double d1 = x.distance(y), d2 = seg.x.distance(seg.y);
        if (p == Point2D.infPoint) {
            double xx = x.distance(seg.x), yy = y.distance(seg.y), xy = x.distance(seg.y), yx = y.distance(seg.x);
//            if (x.distance(seg.x) + x.distance(seg.y) < seg.x.distance(seg.y) + 1e-15) return x;
            if (Math.abs(xx + xy - d2) < Point2D.eps) return x;
//            if (Double.compare(xx + xy, d2) == 0) return x;
//            if (y.distance(seg.x) + y.distance(seg.y) < seg.x.distance(seg.y) + 1e-15) return y;
            if (Math.abs(yx + yy - d2) < Point2D.eps) return y;
//            if (Double.compare(yx + yy, d2) == 0) return y;
//            if (seg.x.distance(x) + seg.x.distance(y) < x.distance(y) + 1e-15) return seg.x;
            if (Math.abs(xx + yx - d1) < Point2D.eps) return seg.x;
//            if (Double.compare(xx + yx, d1) == 0) return seg.x;
//            if (seg.y.distance(x) + seg.y.distance(y) < x.distance(y) + 1e-15) return seg.y;
            if (Math.abs(yy + xy - d1) < Point2D.eps) return seg.y;
//            if (Double.compare(yy + xy, d1) == 0) return seg.y;
            return null;
        }

//        if (x.equals(seg.x) || x.equals(seg.y) || y.equals(seg.x) || y.equals(seg.y)) {
//            return null;
//        }

//        System.err.println(l1 + " " + l2 + " " + p + " " + d1 + " " + d2 + "\n" + (p.distance(x) + p.distance(y)) + " " + (p.distance(seg.x) + p.distance(seg.y)));
        if (p.distance(x) + p.distance(y) > d1 + Point2D.eps) return null;
//        if (Double.compare(p.distance(x) + p.distance(y), d1) > 0) return null;
        if (p.distance(seg.x) + p.distance(seg.y) > d2 + Point2D.eps) return null;
//        if (Double.compare(p.distance(seg.x) + p.distance(seg.y), d2) > 0) return null;
        return p;
    }

    public Point2D getX() {
        return x;
    }

    public Point2D getY() {
        return y;
    }
}

class Kattio extends PrintWriter {
    private final BufferedReader r;
    private StringTokenizer st;

    // standard input
    public Kattio() { this(System.in, System.out); }
    public Kattio(OutputStream o) {
        super(o);
        r = null;
    }
    public Kattio(InputStream i, OutputStream o) {
        super(o);
        r = new BufferedReader(new InputStreamReader(i));
    }
    public Kattio(String inputFile) throws IOException {
        super(System.out);
//        System.err.println("Reading from file: " + inputFile);
        try {
            r = new BufferedReader(new FileReader(inputFile));
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }
    public Kattio(String inputFile, String outputFile) throws IOException {
        super(outputFile != null ? (new BufferedOutputStream(new FileOutputStream(outputFile))) : System.out);
        try {
            r = inputFile != null ? new BufferedReader(new FileReader(inputFile)) : null;
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }
    public String next() {
        try {
            while (st == null || !st.hasMoreTokens())
                st = new StringTokenizer(r.readLine());
            return st.nextToken();
        } catch (Exception ignored) {}
        return null;
    }

    public int nextInt() { return Integer.parseInt(next()); }
    public double nextDouble() { return Double.parseDouble(next()); }
    public long nextLong() { return Long.parseLong(next()); }
}



interface Function{
    double evaluation();
    double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY);
    void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY);
    void initPropagation();
}
class VarIntLS{
    int id;
    Set<Integer> domain;
    int _value;
    public VarIntLS(int id, Set<Integer> domain){
        this.id = id; this.domain = domain;
    }
    public int value(){
        return _value;
    }

}

class NodePosition{
    int id;
    int x_pos;// x-coordinate
    int y_pos;// y-coordinate
    public NodePosition(int id, int x_pos, int y_pos){
        this.id = id;
        this.x_pos = x_pos;
        this.y_pos = y_pos;
    }
    public int id(){ return id; }
    public int x(){ return x_pos; }
    public int y(){ return y_pos; }
    public void assign(int id, int newX, int newY){
        this.id = id;
        x_pos = newX; y_pos = newY;
    }
    public void assign(int newX, int newY){
        x_pos = newX; y_pos = newY;
    }
}

class VarNodePosition{
    int id;
    int x_pos;// x-coordinate
    int y_pos;// y-coordinate
    Set<Integer> DX;// domain for x-coordinate
    Set<Integer> DY;// domain for y-coordinate
    public VarNodePosition(int id, Set<Integer> DX, Set<Integer> DY){
        this.id = id; this.DX = DX; this.DY = DY;
        this.x_pos = -1;
        this.y_pos = -1;
    }
    public int x(){ return x_pos; }
    public int y(){ return y_pos; }
    public void assign(int newX, int newY){
        x_pos = newX; y_pos = newY;
    }
    public NodePosition getNodePosition(){
        return new NodePosition(id, x_pos, y_pos);
    }
}
class Node{
    int id;
    public Node(int id){ this.id = id; }
}
class Edge{
    int id;
    Node fromNode;
    Node toNode;

    public Edge(int id, Node fromNode, Node toNode) {
        this.id = id;
        this.fromNode = fromNode;
        this.toNode = toNode;
    }
    public Edge(Node fromNode, Node toNode) {
        this.id = -1;
        this.fromNode = fromNode;
        this.toNode = toNode;
    }

    public Node getRemaining(Node node){
        if(node == fromNode) return toNode;
        if(node == toNode) return fromNode;
        return null;
    }
}

class Graph {
    private final List<Node> nodes;
    private final Map<Integer, Node> nodeMap;
    private final Map<Node, List<Edge>> A; // A[v] is the list of adjacent edges of v

    public Graph(List<Node> nodes){
        this.nodes = nodes;
        nodeMap = new HashMap<>();
        for(Node n: nodes) nodeMap.put(n.id, n);
        A = new HashMap<>();
        for(Node n: nodes) A.put(n, new ArrayList<>());
    }
    public Graph(List<Node> nodes, Map<Node, List<Edge>> a) {
        this.nodes = nodes;
        nodeMap = new HashMap<>();
        for(Node n: nodes) nodeMap.put(n.id, n);
        A = a;
    }

    private int edgeId = 0;
    public Edge addEdge(Node u, Node v){
        Edge e = new Edge(edgeId++, u, v);
        A.get(u).add(e);
        return e;
    }
    public List<Node> getNodes(){ return nodes; }
    public List<Edge> getEdges(Node u){
        return A.get(u);
    }
    public Map<Node, List<Edge>> getA() {
        return A;
    }
    public List<Edge> getEdges() {
        List<Edge> edges = new ArrayList<>();
        for (Map.Entry<Node, List<Edge>> entry : A.entrySet()) {
            List<Edge> edgeList = entry.getValue();
            edges.addAll(edgeList);
        }
        return edges;
    }
    public Node getNode(int id) {
        return nodeMap.get(id);
    }
    public Node getNodeI(int id) {
        return nodes.get(id);
    }

    private Set<Edge> visited;
    private List<Edge> result;
    private void dfs(Node current, Node prev) {
        for (Edge edge : A.get(current)) {
            Node next = edge.getRemaining(current);
            if (next == null || next == prev || visited.contains(edge)) continue;
            visited.add(edge);
            result.add(edge);
            dfs(next, current);
        }
    }

    public List<Edge> dfs(Node start) {
        result = new ArrayList<>();
        visited = new HashSet<>();
        dfs(start, start);
        // for (Edge edge : result) {
        //     System.err.println("Edge: " + edge.fromNode.id + " -> " + edge.toNode.id);
        // }

        return result;
    }

    private void dfs(Node current, Node prev, List<DoubleLinkedList<Edge>> adj) {
        LinkedNode<Edge> edge = adj.get(current.id).getFirst();
        Edge prevEdge = null;
        while (edge != null) {
            Node next = edge.value.toNode;
            // System.err.println("." + prev.id + " " + next.id);
            if (next == prev) {
                prevEdge = edge.value;
                edge = edge.next;
                continue;
            }
            if (next == null || visited.contains(edge.value)) {
                edge = edge.next;
                continue;
            }

            visited.add(edge.value);
            result.add(edge.value);
            dfs(next, current, adj);
            edge = edge.next;
        }
        if (prevEdge != null) {
            // visited.add(prevEdge);
            result.add(prevEdge);
        }
    }


    public List<Edge> dfs(Node start, List<DoubleLinkedList<Edge>> adj) {
        // List<Edge> result = new ArrayList<>();
        // Set<Node> visited = new HashSet<>();
        // Stack<Node> stack = new Stack<>();
        // stack.push(start);
        // visited.add(start);
        
        // while (!stack.isEmpty()) {
        //     Node current = stack.pop();
        //     LinkedNode<Edge> edge = adj.get(current.id).getFirst();
        //     while (edge != null) {
        //         Node next = edge.value.getRemaining(current);
        //         if (next != null && !visited.contains(next)) {
        //             visited.add(next);
        //             stack.push(next);
        //             result.add(edge.value);
        //         }
        //         edge = edge.next;
        //     }
        // }
        // for (DoubleLinkedList<Edge> edges : adj) {
        //     for (LinkedNode<Edge> edge = edges.getFirst(); edge != null; edge = edge.next) {
        //         System.err.println("Edge: " + edge.value.fromNode.id + " -> " + edge.value.toNode.id);
        //     }
        // }

        result = new ArrayList<>();
        visited = new HashSet<>();
        dfs(start, start, adj);
        // System.err.println(result.size());
        // for (Edge edge : result) {
        //     System.err.println("Edge: " + edge.fromNode.id + " -> " + edge.toNode.id);
        // }

        return result;
    }

    public List<Integer> bfs(Node start, List<DoubleLinkedList<Edge>> adj) {
        List<Integer> distances = new ArrayList<>();
        int n = nodes.size();
        for (int i = 0; i < n; i++) {
            distances.add(Integer.MAX_VALUE);
        }

        Queue<Node> queue = new LinkedList<>();
        queue.add(start);
        distances.set(start.id, 0);
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            LinkedNode<Edge> edge = adj.get(current.id).getFirst();
            while (edge != null) {
                Node next = edge.value.getRemaining(current);
                if (next != null && distances.get(next.id) == Integer.MAX_VALUE) {
                    distances.set(next.id, distances.get(current.id) + 1);
                    queue.add(next);
                }
                edge = edge.next;
            }
        }

        return distances;
    }


}

class CBLSGPModel{
    List<VarNodePosition> varNodePositions;
    List<Function> F;

    // additional data structures defined here to efficiently perform the move
    
    public CBLSGPModel() {
        varNodePositions = new ArrayList<>();
        F = new ArrayList<>();
    }

    public VarNodePosition getVarNode(int id){
        return varNodePositions.get(id);
    }

    public void addVarNode(VarNodePosition varNodePosition){
        varNodePositions.add(varNodePosition);
    }
    public void addFunction(Function f){
        F.add(f);
    }
    public void move(VarNodePosition varNode, int x, int y){
        // perform propagation to update functions in F
        varNode.assign(x,y);
//        for (Function f : F) {
//            f.propagateOneNodeMove(varNode, x, y);
//        }
    }
    public List<NodePosition> getNodePositions(){
        List<NodePosition> nodePositions = new ArrayList<>();
        for(VarNodePosition v: varNodePositions){
            nodePositions.add(v.getNodePosition());
        }
        return nodePositions;
    }
    public void setNodePositionsValue(List<NodePosition> nodePositions){
        for(int i = 0; i < varNodePositions.size(); i++){
            // varNodePositions.get(i).assign(nodePositions.get(i).x(), nodePositions.get(i).y());
            VarNodePosition pos = varNodePositions.get(i);
            nodePositions.get(i).assign(pos.id, pos.x(), pos.y());
        }
    }

    public void close(){
        for(Function f: F) f.initPropagation();
    }
}

class DoubleCompare implements Comparator<Double> {
    @Override
    public int compare(Double a, Double b) {
        if (Math.abs(a - b) < Point2D.eps) return 0;
        return Double.compare(a, b);
    }
}

class TreeMultiset<T extends Comparable<T>> implements Iterable<T> {
    private int size = 0;
    private final TreeMap<T, Integer> map;

    public TreeMultiset() {
        map = new TreeMap<>();
    }
    public TreeMultiset(Comparator<T> comparator) {
        map = new TreeMap<>(comparator);
    }

    public void clear() {
        size = 0;
        map.clear();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Iterator<Map.Entry<T, Integer>> entryIterator = map.entrySet().iterator();
            private Map.Entry<T, Integer> currentEntry = null;
            private int currentCount = 0;

            @Override
            public boolean hasNext() {
                return currentCount > 0 || entryIterator.hasNext();
            }

            @Override
            public T next() {
                if (currentCount == 0) {
                    currentEntry = entryIterator.next();
                    currentCount = currentEntry.getValue();
                }
                currentCount--;
                return currentEntry.getKey();
            }
        };
    }

    void add(T value) {
        size++;
        map.put(value, map.getOrDefault(value, 0) + 1);
    }

    void add(T value, int cnt) {
        size += cnt;
        map.put(value, map.getOrDefault(value, 0) + cnt);
    }

    boolean remove(T value) {
        Integer count = map.get(value);
        if (count != null) {
            size--;
            if (count > 1) {
                map.put(value, count - 1);
            } else {
                map.remove(value);
            }
            return true;
        }
//        System.err.println(" " + value);
        return false;
    }

    boolean remove(T value, int cnt) {
        Integer count = map.get(value);
        if (count != null) {
            if (count > cnt) {
                size -= cnt;
                map.put(value, count - cnt);
                return true;
            } else {
                size -= count;
                map.remove(value);
                return count >= cnt;
            }
        }
        return false;
    }

    T first() {
        return map.isEmpty() ? null : map.firstKey();
    }

    public boolean isContains(T value) {
        return map.containsKey(value);
    }

    public int countContains(T value) {
        return map.getOrDefault(value, 0);
    }

    public int size() {
        return size;
    }

    boolean isEmpty() {
        return map.isEmpty();
    }

//    TreeMap<T, Integer> map() {
    // Object map() {
    //     return map;
    // }
    Set<Map.Entry<T, Integer>> entrySet() {
        return map.entrySet();
    }
}
class SumDistanceEdge implements Function{
    double sumDistance = 0;
    Graph g;
    List<List<Edge>> adj;
    Map<Node, VarNodePosition> positions;
    Map<Integer, Double> distances;

    @Override
    public String toString() {
        return "sumDistance=" + sumDistance + " ";
    }

    private int encode(Edge e) {
        int u = Math.min(e.fromNode.id, e.toNode.id);
        int v = Math.max(e.fromNode.id, e.toNode.id);
        return u * g.getNodes().size() + v;
    }

    public SumDistanceEdge(Graph g, Map<Node, VarNodePosition> positions){
        this.g = g;
        this.positions = positions;
        distances = new HashMap<>();

        adj = new ArrayList<>();
        Map<Integer, Boolean> marked = new HashMap<>();
        for (Node node : g.getNodes()) {
            adj.add(new ArrayList<>());
            for (Edge e : g.getEdges(node)) {
                int eId = encode(e);
                if (marked.containsKey(eId)) continue;
                marked.put(eId, true);
                adj.get(node.id).add(e);
            }
            marked.clear();
        }

        for (Node node: g.getNodes()){
            for(Edge e: adj.get(node.id)){
                int eId = encode(e);
                if (distances.containsKey(eId)) continue;
                Node u = e.fromNode, v = e.toNode;
                VarNodePosition posU = positions.get(u), posV = positions.get(v);
                double d = Geometry.distance(posU.x(), posU.y(), posV.x(), posV.y());
                sumDistance += d;
            }
        }
    }

    @Override
    public double evaluation() {
        return sumDistance;
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (newX == -1 && newY == -1) return evaluation();
        int oldX = varNodePosition.x(), oldY = varNodePosition.y();
        if (oldX == newX && oldY == newY) {
            return evaluation();
        }
        Node node = g.getNode(varNodePosition.id);
        double current = sumDistance;
        for (Edge e : adj.get(node.id)) {
            Node u = e.getRemaining(node);
            VarNodePosition posU = positions.get(u);
            double oldD = distances.getOrDefault(encode(e), -1.);
            if (oldD > -1.) {
                current -= oldD;
            }
            double d = Geometry.distance(posU.x(), posU.y(), newX, newY);
            current += d;
        }

        return current;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (newX == -1 && newY == -1) return;
        int oldX = varNodePosition.x(), oldY = varNodePosition.y();

        if (oldX == newX && oldY == newY) return;
        Node node = g.getNode(varNodePosition.id);

        for (Edge e : adj.get(node.id)) {
            Node u = e.getRemaining(node);
            VarNodePosition posU = positions.get(u);
            double d = Geometry.distance(posU.x(), posU.y(), newX, newY);

            int eId = encode(e);
            double oldDistance = distances.getOrDefault(eId, -1.);
            if (oldDistance > -1) {
                if (Math.abs(oldDistance - d) < Point2D.eps) {
                    continue;
                }
                sumDistance -= oldDistance;
            }
            distances.put(eId, d);
            sumDistance += d;
        }
    }

    @Override
    public void initPropagation() {

    }
}

class MinDistanceEdge implements Function{
//    double minDistance;
    Graph g;
    List<List<Edge>> adj;
    Map<Node, VarNodePosition> positions;
    Map<Integer, Double> distances;
//    Queue<Pair<Double, Integer>> pq;
//    TreeSet<Pair<Double, Integer>> pq;
    TreeMultiset<Double> pq;

    @Override
    public String toString() {
        return "minDistance=" + evaluation() + " ";
    }

    private int encode(Edge e) {
        int u = Math.min(e.fromNode.id, e.toNode.id);
        int v = Math.max(e.fromNode.id, e.toNode.id);
        return u * g.getNodes().size() + v;
    }
    public MinDistanceEdge(Graph g, Map<Node, VarNodePosition> positions){
//        minDistance = Double.POSITIVE_INFINITY;
        this.g = g;
        this.positions = positions;
        distances = new HashMap<>();
//        pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a.a));
//        pq = new TreeSet<>(Comparator.comparingDouble((Pair<Double, Integer> p) -> p.a).thenComparing(p -> p.b));
        pq = new TreeMultiset<>(new DoubleCompare());
        adj = new ArrayList<>();
        Map<Integer, Boolean> marked = new HashMap<>();
        for (Node node : g.getNodes()) {
            adj.add(new ArrayList<>());
            for (Edge e : g.getEdges(node)) {
                int eId = encode(e);
                if (marked.containsKey(eId)) continue;
                marked.put(eId, true);
                adj.get(node.id).add(e);
            }
            marked.clear();
        }

        for (Node node: g.getNodes()){
            for(Edge e: adj.get(node.id)){
                int eId = encode(e);
                if (distances.containsKey(eId)) continue;
                Node u = e.fromNode, v = e.toNode;
                VarNodePosition posU = positions.get(u), posV = positions.get(v);
                double d = Geometry.distance(posU.x(), posU.y(), posV.x(), posV.y());
                distances.put(eId, d);
//                minDistance = Math.min(minDistance, d);

//                distances.put(encode(e), Double.POSITIVE_INFINITY);
//                pq.add(new Pair<>(d, encode(e)));
                pq.add(d);
            }
        }
//        System.err.println(pq.size() + " " + distances.size() + " " + minDistance);
    }

    @Override
    public double evaluation() {
//        while (!pq.isEmpty()) {
//            Pair<Double, Integer> p = pq.peek();
////            if (Double.compare(distances.getOrDefault(p.b, Double.POSITIVE_INFINITY), p.a) != 0) {
//            if (Math.abs(distances.getOrDefault(p.b, Double.POSITIVE_INFINITY) - p.a) > Point2D.eps) {
//                pq.poll();
//                continue;
//            }
//            return p.a;
//        }
//        return 0;
        if (pq.isEmpty()) return Double.POSITIVE_INFINITY;
        return pq.first();
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (newX == -1 && newY == -1) return evaluation();
        int oldX = varNodePosition.x(), oldY = varNodePosition.y();
        if (oldX == newX && oldY == newY) {
//            System.err.printf("spec (%d %d %f)", oldX, oldY, evaluation());
            return evaluation();
        }
        double min = Double.POSITIVE_INFINITY;
        Node node = g.getNode(varNodePosition.id);
        TreeMultiset<Double> visited = new TreeMultiset<>(new DoubleCompare());
//        Map<Integer, Boolean> visited = new HashMap<>();
//        for (Edge e : g.getEdges(node)) {
        for (Edge e : adj.get(node.id)) {
            Node u = e.getRemaining(node);
//            if (node.id >= u.id) continue;
            VarNodePosition posU = positions.get(u);
            double oldD = distances.getOrDefault(encode(e), -1.);
            if (oldD > -1.) {
                visited.add(oldD);
            }
            double d = Geometry.distance(posU.x(), posU.y(), newX, newY);
//            System.err.print("(" + u.id + " " + posU.x() + " " + posU.y() + " " + d + ") ");
//            visited.put(encode(e), true);
            min = Math.min(min, d);
        }

        for (Double d : pq) {
            // if (d >= min) {
            //     return min;
            // }
            if (d > min - Point2D.eps) {
                return min;
            }
            if (!visited.remove(d)) {
                return d;
            }
        }
//        System.err.print("0" + min + " " + newX + " " + newY);

//        List<Pair<Double, Integer>> tmp = new ArrayList<>();
//        while (!pq.isEmpty()) {
//            Pair<Double, Integer> p = pq.poll();
//
////            if (Double.compare(distances.getOrDefault(p.b, Double.POSITIVE_INFINITY), p.a) != 0) {
//            if (Math.abs(distances.getOrDefault(p.b, Double.POSITIVE_INFINITY) - p.a) > Point2D.eps) {
//                continue;
//            }
//            tmp.add(p);
//            if (p.a >= min) {
//                pq.addAll(tmp);
//                return min;
//            }
//            if (visited.containsKey(p.b)) {
//                continue;
//            }
//            pq.addAll(tmp);
//            return p.a;
//        }
//        pq.addAll(tmp);
////        return Double.POSITIVE_INFINITY;
//        return min;
//        for (Pair<Double, Integer> p : pq) {
//            if (visited.containsKey(p.b)) {
//                continue;
//            }
//            if (p.a >= min) {
//                return min;
//            }
//            return p.a;
//        }
        return min;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (newX == -1 && newY == -1) return;
        int oldX = varNodePosition.x(), oldY = varNodePosition.y();
//        System.err.printf("propagate (%d %d %d %d) ", oldX, oldY, newX, newY);
        if (oldX == newX && oldY == newY) return;
        Node node = g.getNode(varNodePosition.id);
//        for (Edge e : g.getEdges(node)) {
        for (Edge e : adj.get(node.id)) {
            Node u = e.getRemaining(node);
            VarNodePosition posU = positions.get(u);
            double d = Geometry.distance(posU.x(), posU.y(), newX, newY);
//            System.err.printf("(%d %d %f) ", e.fromNode.id, e.toNode.id, d);
            int eId = encode(e);
            double oldDistance = distances.getOrDefault(eId, -1.);
            if (oldDistance > -1) {
                if (Math.abs(oldDistance - d) < Point2D.eps) {
                    continue;
                }
                pq.remove(oldDistance);
//                pq.remove(new Pair<>(oldDistance, eId));
            }
            distances.put(eId, d);
            pq.add(d);
//            pq.add(new Pair<>(d, eId));
        }
    }
    @Override
    public void initPropagation() {

    }
}

class SumAngle implements Function {
    Graph g;
    List<List<Edge>> adj;
    Map<Node, VarNodePosition> positions;
    Map<Node, TreeSet<NodeAngle>> nodeNeighbors = new HashMap<>();
    Map<Integer, Map<Integer, NodeAngle>> nodeAngleMap = new HashMap<>();
    List<TreeMultiset<Double>> angles = new ArrayList<>();
    double sumAngleValue = 0.0;

    @Override
    public String toString() {
        return "sumAngle=" + sumAngleValue + " ";
    }

    private static class NodeAngle implements Comparable<NodeAngle> {
        Node node;
        double angle;
        
        NodeAngle(Node node, double angle) {
            this.node = node;
            this.angle = angle;
        }
        
        @Override
        public int compareTo(NodeAngle o) {
            if (Math.abs(angle - o.angle) >= Point2D.eps) {
                return Double.compare(angle, o.angle);
            }
            return Integer.compare(node.id, o.node.id);
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof NodeAngle na)) return false;
            // return node.id == na.node.id && Double.compare(angle, na.angle) == 0;
            return node.id == na.node.id && Math.abs(angle - na.angle) < Point2D.eps;
        }
        
    }
    
    public SumAngle(Graph g, Map<Node, VarNodePosition> positions) {
        this.g = g;
        this.positions = positions;
        adj = new ArrayList<>();

        Map<Integer, Boolean> marked = new HashMap<>();
        for (Node node : g.getNodes()) {
            nodeAngleMap.put(node.id, new HashMap<>());
            nodeNeighbors.put(node, new TreeSet<>());
            angles.add(new TreeMultiset<>(new DoubleCompare()));
            adj.add(new ArrayList<>());
            for (Edge e : g.getEdges(node)) {
                int u = Math.min(e.fromNode.id, e.toNode.id);
                int v = Math.max(e.fromNode.id, e.toNode.id);
                int eId = u * g.getNodes().size() + v;
                if (marked.containsKey(eId)) continue;
                marked.put(eId, true);
                adj.get(node.id).add(e);
            }
            marked.clear();
        }
        
        for (Node node : g.getNodes()) {
            VarNodePosition nodePos = positions.get(node);
            updateNodeAngles(node, nodePos.x(), nodePos.y());
        }
    }
    
    private void updateNodeAngles(Node node, int newX, int newY) {
        if (newX == -1 && newY == -1) return;

        TreeSet<NodeAngle> neighbors = nodeNeighbors.get(node);
        TreeMultiset<Double> angleN = angles.get(node.id);
        if (neighbors.size() > 1) {
            sumAngleValue -= angleN.first();
            angleN.clear();
        }
        
        neighbors.clear();
        for (Edge e : adj.get(node.id)) {
            Node neighbor = e.getRemaining(node);
            VarNodePosition neighborPos = positions.get(neighbor);
            
            if (neighborPos.x() != -1 && neighborPos.y() != -1) {
                double angle = Math.atan2(neighborPos.y() - newY, neighborPos.x() - newX);
                NodeAngle nodeAngle = new NodeAngle(neighbor, angle);
                nodeAngleMap.get(node.id).put(neighbor.id, nodeAngle);
                neighbors.add(nodeAngle);
            }
        }
        
        if (neighbors.size() > 1) {
            List<NodeAngle> neighborList = new ArrayList<>(neighbors);
            int m = neighborList.size();
            for (int i = 0; i < m; i++) {
                NodeAngle current = neighborList.get(i);
                NodeAngle next = neighborList.get((i + 1) % neighborList.size());
                double angle = (next.angle - current.angle + 2 * Math.PI) % (2 * Math.PI);
                angleN.add(angle);
            }

            sumAngleValue += angleN.first();
        }
    }
    
    private void updateNeighborAngle(Node node, Node movedNode, double newAngle, boolean debug) {
        TreeSet<NodeAngle> neighbors = nodeNeighbors.get(node);
        
        NodeAngle oldNodeAngle = null;
        Map<Integer, NodeAngle> nodeAngles = nodeAngleMap.computeIfAbsent(node.id, k -> new HashMap<>());
        try {
            oldNodeAngle = nodeAngles.get(movedNode.id);
        } catch (Exception e) {
        }

        TreeMultiset<Double> angleN = angles.get(node.id);
        if (oldNodeAngle != null) {
            sumAngleValue -= angleN.first();
            if (neighbors.size() > 1) {
                NodeAngle prev = neighbors.lower(oldNodeAngle);
                if (prev == null) prev = neighbors.last();

                NodeAngle next = neighbors.higher(oldNodeAngle);
                if (next == null) next = neighbors.first();

                double prevOldAngle = (oldNodeAngle.angle - prev.angle + 2 * Math.PI) % (2 * Math.PI);
                angleN.remove(prevOldAngle);
                double oldNextAngle = (next.angle - oldNodeAngle.angle + 2 * Math.PI) % (2 * Math.PI);
                angleN.remove(oldNextAngle);
                if (neighbors.size() > 2) {
                    double prevNextAngle = (next.angle - prev.angle + 2 * Math.PI) % (2 * Math.PI);
                    angleN.add(prevNextAngle);
                }
            }

            neighbors.remove(oldNodeAngle);
        }

        NodeAngle newNodeAngle = new NodeAngle(movedNode, newAngle);
        nodeAngles.put(movedNode.id, newNodeAngle);
        neighbors.add(newNodeAngle);

        if (neighbors.size() > 1) {
            NodeAngle prev = neighbors.lower(newNodeAngle);
            if (prev == null) prev = neighbors.last();
            
            NodeAngle next = neighbors.higher(newNodeAngle);
            if (next == null) next = neighbors.first();
            
            double prevNewAngle = (newNodeAngle.angle - prev.angle + 2 * Math.PI) % (2 * Math.PI);
            angleN.add(prevNewAngle);
            double newNextAngle = (next.angle - newNodeAngle.angle + 2 * Math.PI) % (2 * Math.PI);
            angleN.add(newNextAngle);
            if (neighbors.size() > 2) {
                double prevNextAngle = (next.angle - prev.angle + 2 * Math.PI) % (2 * Math.PI);
                angleN.remove(prevNextAngle);
            }

            sumAngleValue += angleN.first();
        }
    }

    @Override
    public double evaluation() {
        return sumAngleValue;
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition v, int newX, int newY) {
        Node node = g.getNode(v.id);
        int oldX = v.x(), oldY = v.y();
        
        if (oldX == newX && oldY == newY || newX == -1 && newY == -1) {
            return evaluation();
        }

        updateNodeAngles(node, newX, newY);
        for (Edge e : adj.get(node.id)) {
            Node neighbor = e.getRemaining(node);
            VarNodePosition neighborPos = positions.get(neighbor);
            if (neighborPos.x() == -1 && neighborPos.y() == -1) continue;
            double newAngle = Math.atan2(newY - neighborPos.y(), newX - neighborPos.x());
            updateNeighborAngle(neighbor, node, newAngle, false);
        }
        double newSumAngle = sumAngleValue;

        updateNodeAngles(node, oldX, oldY);
        for (Edge e : adj.get(node.id)) {
            Node neighbor = e.getRemaining(node);
            VarNodePosition neighborPos = positions.get(neighbor);
            if (neighborPos.x() == -1 && neighborPos.y() == -1) continue;
            double oldAngle = Math.atan2(oldY - neighborPos.y(), oldX - neighborPos.x());
            updateNeighborAngle(neighbor, node, oldAngle, false);
        }

        return newSumAngle;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition v, int newX, int newY) {
        Node node = g.getNode(v.id);
        int oldX = v.x(), oldY = v.y();

        if (oldX == newX && oldY == newY || newX == -1 && newY == -1) {
            return;
        }

        updateNodeAngles(node, newX, newY);
        for (Edge e : adj.get(node.id)) {
            Node neighbor = e.getRemaining(node);
            VarNodePosition neighborPos = positions.get(neighbor);
            if (neighborPos.x() == -1 && neighborPos.y() == -1) continue;
            double newAngle = Math.atan2(newY - neighborPos.y(), newX - neighborPos.x());
            updateNeighborAngle(neighbor, node, newAngle, true);
        }
    }
    @Override
    public void initPropagation() {

    }
}

class MinAngle implements Function {
    Graph g;
    List<List<Edge>> adj;
    Map<Node, VarNodePosition> positions;
    Map<Node, TreeSet<NodeAngle>> nodeNeighbors = new HashMap<>();
    // TreeMultiset<Double> allAngles = new TreeMultiset<>();
    TreeMultiset<Double> allAngles = new TreeMultiset<>(new DoubleCompare());
    Map<Integer, Map<Integer, NodeAngle>> nodeAngleMap = new HashMap<>();
    double minAngleValue = Double.POSITIVE_INFINITY;

    @Override
    public String toString() {
        return "minAngle=" + minAngleValue + " ";
    }

    private static class NodeAngle implements Comparable<NodeAngle> {
        Node node;
        double angle;
        
        NodeAngle(Node node, double angle) {
            this.node = node;
            this.angle = angle;
        }
        
        @Override
        public int compareTo(NodeAngle o) {
            // int angleComp = Double.compare(angle, o.angle);
            // return angleComp != 0 ? angleComp : Integer.compare(node.id, o.node.id);
            if (Math.abs(angle - o.angle) >= Point2D.eps) {
                return Double.compare(angle, o.angle);
            }
            return Integer.compare(node.id, o.node.id);
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof NodeAngle na)) return false;
            // return node.id == na.node.id && Double.compare(angle, na.angle) == 0;
            return node.id == na.node.id && Math.abs(angle - na.angle) < Point2D.eps;
        }
        
//        @Override
//        public int hashCode() {
//            return Objects.hash(node.id, angle);
//        }
    }
    
    public MinAngle(Graph g, Map<Node, VarNodePosition> positions) {
        this.g = g;
        this.positions = positions;
        adj = new ArrayList<>();

        Map<Integer, Boolean> marked = new HashMap<>();
        for (Node node : g.getNodes()) {
            nodeAngleMap.put(node.id, new HashMap<>());
            nodeNeighbors.put(node, new TreeSet<>());
            adj.add(new ArrayList<>());
            for (Edge e : g.getEdges(node)) {
                int u = Math.min(e.fromNode.id, e.toNode.id);
                int v = Math.max(e.fromNode.id, e.toNode.id);
                int eId = u * g.getNodes().size() + v;
                if (marked.containsKey(eId)) continue;
                marked.put(eId, true);
                adj.get(node.id).add(e);
            }
            marked.clear();
        }
        
        for (Node node : g.getNodes()) {
            VarNodePosition nodePos = positions.get(node);
            updateNodeAngles(node, nodePos.x(), nodePos.y());
        }
        
        if (!allAngles.isEmpty()) {
            minAngleValue = allAngles.first();
        }
    }
    
    private void updateNodeAngles(Node node, int newX, int newY) {
//        VarNodePosition nodePos = positions.get(node);
//        if (nodePos.x() == -1 && nodePos.y() == -1) return;
        if (newX == -1 && newY == -1) return;
//        if (nodePos.x() == newX && nodePos.y() == newY) return;
        
        TreeSet<NodeAngle> neighbors = nodeNeighbors.get(node);
        
        if (neighbors.size() > 1) {
            List<NodeAngle> neighborList = new ArrayList<>(neighbors);
            for (int i = 0; i < neighborList.size(); i++) {
                NodeAngle current = neighborList.get(i);
                NodeAngle next = neighborList.get((i + 1) % neighborList.size());
                double angle = (next.angle - current.angle + 2 * Math.PI) % (2 * Math.PI);
//                allAngles.remove(angle);
                if (!allAngles.remove(angle)) {
//                    System.err.print("start: " + node.id + " " + current.angle + " " + current.node.id + " - ");
//                    for (double v : allAngles) {
//                        System.err.print(v + " ");
//                    }
//                    System.err.println();
//                    for (NodeAngle n : neighborList) {
//                        System.err.printf("(%f, %d)", n.angle, n.node.id);
//                    }
//                    System.err.println(" " + angle);
                }
            }
        }
        
        neighbors.clear();
        
//        List<Node> connectedNodes = new ArrayList<>();
//        for (Edge e : g.getEdges(node)) {
        for (Edge e : adj.get(node.id)) {
            Node neighbor = e.getRemaining(node);
            VarNodePosition neighborPos = positions.get(neighbor);
            
            if (neighborPos.x() != -1 && neighborPos.y() != -1) {
//                connectedNodes.add(neighbor);
                double angle = Math.atan2(neighborPos.y() - newY, neighborPos.x() - newX);
                NodeAngle nodeAngle = new NodeAngle(neighbor, angle);
                nodeAngleMap.get(node.id).put(neighbor.id, nodeAngle);
                neighbors.add(nodeAngle);
            }
        }
        
        if (neighbors.size() > 1) {
            List<NodeAngle> neighborList = new ArrayList<>(neighbors);
            for (int i = 0; i < neighborList.size(); i++) {
                NodeAngle current = neighborList.get(i);
                NodeAngle next = neighborList.get((i + 1) % neighborList.size());
                double angle = (next.angle - current.angle + 2 * Math.PI) % (2 * Math.PI);
                allAngles.add(angle);
            }
        }
        
        if (!allAngles.isEmpty()) {
            minAngleValue = allAngles.first();
        } else {
            minAngleValue = Math.PI * 2; 
        }
    }
    
    private void updateNeighborAngle(Node node, Node movedNode, double newAngle, boolean debug) {
        TreeSet<NodeAngle> neighbors = nodeNeighbors.get(node);
//        if (neighbors.size() < 2) return;
        
        NodeAngle oldNodeAngle = null;
        Map<Integer, NodeAngle> nodeAngles = nodeAngleMap.computeIfAbsent(node.id, k -> new HashMap<>());
        try {
//            nodeAngles = nodeAngleMap.get(node.id);
            oldNodeAngle = nodeAngles.get(movedNode.id);
        } catch (Exception e) {
            // NodeAngle not found, do nothing
        }
//        for (NodeAngle na : neighbors) {
//            if (na.node.id == movedNode.id) {
//                oldNodeAngle = na;
//                break;
//            }
//        }

        if (oldNodeAngle != null) {
//            if (debug) {
//                System.err.printf("updateNeighborAngle: node %d, movedNode %d, newAngle %f, oldNodeAngle %f\n",
//                        node.id, movedNode.id, newAngle, oldNodeAngle.angle);
//                for (NodeAngle nodeAngle : neighbors) {
//                    System.err.printf("(%f, %d) ", nodeAngle.angle, nodeAngle.node.id);
//                }
//                System.err.println();
//            }

            if (neighbors.size() > 1) {
                NodeAngle prev = neighbors.lower(oldNodeAngle);
                if (prev == null) prev = neighbors.last();

                NodeAngle next = neighbors.higher(oldNodeAngle);
                if (next == null) next = neighbors.first();

                double prevOldAngle = (oldNodeAngle.angle - prev.angle + 2 * Math.PI) % (2 * Math.PI);
                double oldNextAngle = (next.angle - oldNodeAngle.angle + 2 * Math.PI) % (2 * Math.PI);
//                allAngles.remove(prevOldAngle);
                if (!allAngles.remove(prevOldAngle)) {
//                    System.err.print("start: " + movedNode.id + " " + oldNodeAngle.angle + " " + oldNodeAngle.node.id + " - ");
//                    for (double v : allAngles) {
//                        System.err.print(v + " ");
//                    }
//                    System.err.println();
//                    for (NodeAngle nodeAngle : neighbors) System.err.printf("(%f, %d)", nodeAngle.angle, nodeAngle.node.id);
//                    System.err.println(" " + prevOldAngle);
                }
//                allAngles.remove(oldNextAngle);
                if (!allAngles.remove(oldNextAngle)) {
//                    System.err.print("start: " + movedNode.id + " " + oldNodeAngle.angle + " " + oldNodeAngle.node.id + " - ");
//                    for (double v : allAngles) {
//                        System.err.print(v + " ");
//                    }
//                    System.err.println();
//                    for (NodeAngle nodeAngle : neighbors) System.err.printf("(%f, %d)", nodeAngle.angle, nodeAngle.node.id);
//                    System.err.println(" " + oldNextAngle);
                }

                if (neighbors.size() > 2) {
                    double prevNextAngle = (next.angle - prev.angle + 2 * Math.PI) % (2 * Math.PI);
                    allAngles.add(prevNextAngle);
                }
            }

            neighbors.remove(oldNodeAngle);
        }

        NodeAngle newNodeAngle = new NodeAngle(movedNode, newAngle);
        nodeAngles.put(movedNode.id, newNodeAngle);
        neighbors.add(newNodeAngle);

        if (neighbors.size() > 1) {
            NodeAngle prev = neighbors.lower(newNodeAngle);
            if (prev == null) prev = neighbors.last();
            
            NodeAngle next = neighbors.higher(newNodeAngle);
            if (next == null) next = neighbors.first();
            
            double prevNewAngle = (newNodeAngle.angle - prev.angle + 2 * Math.PI) % (2 * Math.PI);
            double newNextAngle = (next.angle - newNodeAngle.angle + 2 * Math.PI) % (2 * Math.PI);
            if (neighbors.size() > 2) {
                double prevNextAngle = (next.angle - prev.angle + 2 * Math.PI) % (2 * Math.PI);
                allAngles.remove(prevNextAngle);
            }
            allAngles.add(prevNewAngle);
            allAngles.add(newNextAngle);
            

        }
        if (!allAngles.isEmpty()) {
            minAngleValue = allAngles.first();
        } else {
            minAngleValue = Math.PI * 2;
        }
    }

    @Override
    public double evaluation() {
        return minAngleValue;
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition v, int newX, int newY) {
        Node node = g.getNode(v.id);
        int oldX = v.x(), oldY = v.y();
        
        if (oldX == newX && oldY == newY || newX == -1 && newY == -1) {
            return evaluation();
        }

        updateNodeAngles(node, newX, newY);
//        for (Edge e : g.getEdges(node)) {
        for (Edge e : adj.get(node.id)) {
            Node neighbor = e.getRemaining(node);
            VarNodePosition neighborPos = positions.get(neighbor);
            if (neighborPos.x() == -1 && neighborPos.y() == -1) continue;
            double newAngle = Math.atan2(newY - neighborPos.y(), newX - neighborPos.x());
            updateNeighborAngle(neighbor, node, newAngle, false);
        }
//        propagateOneNodeMove(v, newX, newY);
        double newMinAngle = minAngleValue;
//        propagateOneNodeMove(v, oldX, oldY);

        updateNodeAngles(node, oldX, oldY);
//        for (Edge e : g.getEdges(node)) {
        for (Edge e : adj.get(node.id)) {
            Node neighbor = e.getRemaining(node);
            VarNodePosition neighborPos = positions.get(neighbor);
            if (neighborPos.x() == -1 && neighborPos.y() == -1) continue;
            double oldAngle = Math.atan2(oldY - neighborPos.y(), oldX - neighborPos.x());
            updateNeighborAngle(neighbor, node, oldAngle, false);
        }

        return newMinAngle;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition v, int newX, int newY) {
        Node node = g.getNode(v.id);
        int oldX = v.x(), oldY = v.y();

        if (oldX == newX && oldY == newY || newX == -1 && newY == -1) {
            return;
        }

//        v.assign(newX, new);

        updateNodeAngles(node, newX, newY);
        
//        for (Edge e : g.getEdges(movedNode)) {
        for (Edge e : adj.get(node.id)) {
            Node neighbor = e.getRemaining(node);
            VarNodePosition neighborPos = positions.get(neighbor);
            if (neighborPos.x() == -1 && neighborPos.y() == -1) continue;
            double newAngle = Math.atan2(newY - neighborPos.y(), newX - neighborPos.x());
//            System.err.print(newAngle + " ");
            updateNeighborAngle(neighbor, node, newAngle, true);
        }
//        System.err.println();

//        updateNodeAngles(node, oldX, oldY);
//        for (Edge e : adj.get(node.id)) {
//            Node neighbor = e.getRemaining(node);
//            VarNodePosition neighborPos = positions.get(neighbor);
//            if (neighborPos.x() == -1 && neighborPos.y() == -1) continue;
//            double oldAngle = Math.atan2(oldY - neighborPos.y(), oldX - neighborPos.x());
//            updateNeighborAngle(neighbor, node, oldAngle);
//        }
//        v.assign(oldX, oldY);
    }
    @Override
    public void initPropagation() {

    }
}

class NumberIntersectionEdges implements Function{
    Graph g;
    Map<Node, VarNodePosition> positions;
    List<Edge> edges;
    List<List<Edge>> adj;
    Map<Integer, Set<Integer>> intersectMap = new HashMap<>();
    int totalIntersections = 0;

    @Override
    public String toString() {
        return "numberIntersection=" + totalIntersections + " ";
    }

    private int encode(Edge e) {
        int u = Math.min(e.fromNode.id, e.toNode.id);
        int v = Math.max(e.fromNode.id, e.toNode.id);
        return u * g.getNodes().size() + v;
    }

    public NumberIntersectionEdges(Graph g, Map<Node, VarNodePosition> positions){
        this.g = g;
        this.positions = positions;
        adj = new ArrayList<>();
        Map<Integer, Boolean> marked = new HashMap<>();
        for (Node node : g.getNodes()) {
            adj.add(node.id, new ArrayList<>());
            for (Edge e : g.getEdges(node)) {
                int eId = encode(e);
                if (marked.containsKey(eId)) continue;
                marked.put(eId, true);
                adj.get(node.id).add(e);
            }
            marked.clear();
        }
        edges = new ArrayList<>();
        for (Edge e : g.getEdges()) {
            int eId = encode(e);
            if (marked.containsKey(eId)) continue;
            marked.put(eId, true);
            edges.add(e);
        }

        for (Edge e : edges)
            intersectMap.put(encode(e), new HashSet<>());

        for(int i = 0; i < edges.size(); i++) {
            Edge ei = edges.get(i);

            Segment2D si = createSegment(ei);
            if (si == null) continue;
            
            for(int j = i + 1; j < edges.size(); j++){
                Edge ej = edges.get(j);
                if (ej == null) continue;
                if (ei.fromNode.id == ej.fromNode.id || ei.toNode.id == ej.toNode.id ||
                    ei.fromNode.id == ej.toNode.id || ei.toNode.id == ej.fromNode.id) continue;
                Segment2D sj = createSegment(ej);
//                if (ei.fromNode.id == 6 && ei.toNode.id == 7 && ej.fromNode.id == 9 && ej.toNode.id == 10) {
//                    System.err.printf("(%d %d %d %d %d %d) ", ei.fromNode.id, ei.toNode.id,
//                            positions.get(ei.fromNode).x(), positions.get(ei.fromNode).y(),
//                            positions.get(ei.toNode).x(), positions.get(ei.toNode).y());
//                    System.err.printf("(%d %d %d %d %d %d) ", ej.fromNode.id, ej.toNode.id,
//                        positions.get(ej.fromNode).x(), positions.get(ej.fromNode).y(),
//                        positions.get(ej.toNode).x(), positions.get(ej.toNode).y());
//                    Point2D p = si.intersect(sj);
//                    if (p != null) {
//                        System.err.printf("((%f %f) (%f %f)) ((%f %f) (%f %f)) %f %f\n", si.getX().getX(), si.getX().getY(), si.getY().getX(), si.getY().getY(),
//                                sj.getX().getX(), sj.getX().getY(), sj.getY().getX(), sj.getY().getY(), p.getX(), p.getY());
//                    }
//                }
                
                if(sj != null && si.intersect(sj) != null) {
                    intersectMap.get(encode(ei)).add(encode(ej));
                    intersectMap.get(encode(ej)).add(encode(ei));
                    totalIntersections++;
                }
            }
        }
        // System.err.println(totalIntersections);
    }

    private Segment2D createSegment(Edge e) {
        VarNodePosition fromPos = positions.get(e.fromNode);
        VarNodePosition toPos = positions.get(e.toNode);
        
        if (fromPos.x() == -1 || fromPos.y() == -1 || toPos.x() == -1 || toPos.y() == -1)
            return null;
            
        return new Segment2D(
            new Point2D(fromPos.x(), fromPos.y()),
            new Point2D(toPos.x(), toPos.y())
        );
    }
    
    private int removeNodeIntersections(Node node) {
        int removedCount = 0;
        
        List<Edge> nodeEdges = adj.get(node.id);
        
        for (Edge e : nodeEdges) {
//            Set<Edge> intersectedEdges = intersectMap.get(e);
            Set<Integer> intersectedEdges = intersectMap.get(encode(e));
            removedCount += intersectedEdges.size();
//            for (Edge ie : intersectedEdges) {
//                intersectMap.get(ie).remove(e);
            for (Integer ie : intersectedEdges) {
                intersectMap.get(ie).remove(encode(e));
            }
            intersectedEdges.clear();
        }
        
        return removedCount;
    }
    
    private int addNodeIntersections(Node node) {
        int addedCount = 0;
//        List<Edge> nodeEdges = g.getEdges(node);
        List<Edge> nodeEdges = adj.get(node.id);

        for (Edge e : nodeEdges) {
            Segment2D se = createSegment(e);
            if (se == null) continue;
            int ei = encode(e);
            for (Edge f : edges) {
//                if (f == e || nodeEdges.contains(f)) continue;
                if (e.fromNode.id == f.fromNode.id || e.toNode.id == f.toNode.id ||
                        e.fromNode.id == f.toNode.id || e.toNode.id == f.fromNode.id) continue;
                if (encode(f) == ei) continue;
                Segment2D sf = createSegment(f);
                if (sf == null) continue;
                
                if (se.intersect(sf) != null) {
                    int fi = encode(f);
//                    intersectMap.get(e).add(f);
//                    intersectMap.get(f).add(e);
                    intersectMap.get(ei).add(fi);
                    intersectMap.get(fi).add(ei);
                    addedCount++;
                }
            }
        }
        
//        for (int i = 0; i < nodeEdges.size(); i++) {
//            Edge ei = nodeEdges.get(i);
//            Segment2D si = createSegment(ei);
//            if (si == null) continue;
//
//            for (int j = i + 1; j < nodeEdges.size(); j++) {
//                Edge ej = nodeEdges.get(j);
//                Segment2D sj = createSegment(ej);
//                if (sj == null) continue;
//
//                if (si.intersect(sj) != null) {
////                    intersectMap.get(ei).add(ej);
////                    intersectMap.get(ej).add(ei);
//                    int eiId = encode(ei), ejId = encode(ej);
//                    intersectMap.get(eiId).add(ejId);
//                    intersectMap.get(ejId).add(eiId);
//                    addedCount++;
//                }
//            }
//        }
        
        return addedCount;
    }

    @Override
    public double evaluation() {
        return -totalIntersections;
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition v, int newX, int newY) {
        if (newX == -1 && newY == -1) {
            return evaluation();
        }

        Node node = g.getNode(v.id);
        int oldX = v.x(), oldY = v.y();

        if (oldX == newX && oldY == newY) {
            return evaluation();
        }
        
        int currentIntersections = 0;
//        List<Edge> nodeEdges = g.getEdges(node);
        List<Edge> nodeEdges = adj.get(node.id);

        for (Edge e : nodeEdges) {
//            Set<Edge> intersectedEdges = intersectMap.get(e);
            Set<Integer> intersectedEdges = intersectMap.get(encode(e));
            currentIntersections += intersectedEdges.size();
        }
        
//        int internalIntersections = 0;
//        for (int i = 0; i < nodeEdges.size(); i++) {
//            Edge ei = nodeEdges.get(i);
//            for (int j = i + 1; j < nodeEdges.size(); j++) {
//                Edge ej = nodeEdges.get(j);
////                if (intersectMap.get(ei).contains(ej)) {
//                if (intersectMap.get(encode(ei)).contains(encode(ej))) {
//                    internalIntersections++;
//                }
//            }
//        }

        v.assign(newX, newY);
        
        int newIntersections = 0;
        
        for (Edge e : nodeEdges) {
            Segment2D se = createSegment(e);
            if (se == null) continue;
            int eId = encode(e);
            for (Edge f : edges) {
//                if (f == e || nodeEdges.contains(f)) continue;
                if (encode(f) == eId) continue;
                if (e.fromNode.id == f.fromNode.id || e.toNode.id == f.toNode.id ||
                        e.fromNode.id == f.toNode.id || e.toNode.id == f.fromNode.id) continue;
                Segment2D sf = createSegment(f);
                if (sf == null) continue;
                if (se.intersect(sf) != null) {
                    newIntersections++;
                }
            }
        }
        
//        for (int i = 0; i < nodeEdges.size(); i++) {
//            Edge ei = nodeEdges.get(i);
//            Segment2D si = createSegment(ei);
//            if (si == null) continue;
//            for (int j = i + 1; j < nodeEdges.size(); j++) {
//                Edge ej = nodeEdges.get(j);
//                Segment2D sj = createSegment(ej);
//                if (sj == null) continue;
//
//                if (si.intersect(sj) != null) {
//                    newIntersections++;
//                }
//            }
//        }
        
        v.assign(oldX, oldY);

//        int val = (totalIntersections - currentIntersections + newIntersections);

        return -(totalIntersections - currentIntersections + newIntersections);
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition v, int newX, int newY) {
        Node node = g.getNode(v.id);
        int oldX = v.x(), oldY = v.y();
        
        if (oldX == newX && oldY == newY) {
            return;
        }
        
        int removedCount = removeNodeIntersections(node);
//        totalIntersections -= removedCount >> 1;
        totalIntersections -= removedCount;
        v.assign(newX, newY);
        int addedCount = addNodeIntersections(node);
//        totalIntersections += addedCount >> 1;
        totalIntersections += addedCount;
        v.assign(oldX, oldY);
    }
    @Override
    public void initPropagation() {

    }
}

class Distance2Nodes implements Function {
    private VarNodePosition pos1;
    private VarNodePosition pos2;
    private double value;

    public Distance2Nodes(VarNodePosition pos1, VarNodePosition pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    @Override
    public double evaluation() {
        return value;
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (varNodePosition == pos1) {
            double dx = newX - pos2.x_pos;
            double dy = newY - pos2.y_pos;
            return Math.sqrt(dx * dx + dy * dy);
        } else if (varNodePosition == pos2) {
            double dx = newX - pos1.x_pos;
            double dy = newY - pos1.y_pos;
            return Math.sqrt(dx * dx + dy * dy);
        }
        return value; // not change
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (varNodePosition != pos1 && varNodePosition != pos2) return;
        if (varNodePosition == pos1) {
            double dx = newX - pos2.x_pos;
            double dy = newY - pos2.y_pos;
            value = Math.sqrt(dx * dx + dy * dy);
        } else if (varNodePosition == pos2) {
            double dx = newX - pos1.x_pos;
            double dy = newY - pos1.y_pos;
            value = Math.sqrt(dx * dx + dy * dy);
        }
    }

    @Override
    public void initPropagation() {
        double dx = (pos1.x_pos - pos2.x_pos);
        double dy = pos1.y_pos - pos2.y_pos;
        value = Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return "distance=" + value + " ";
    }
}

class SumDistanceNodeEdge implements Function {
    private final Graph g;
    private final Map<Node, VarNodePosition> positions;
    private double sumDistance = 0;
    private final Map<Long, Double> distances;
    private final List<Edge> edges;
    private final List<List<Edge>> adj;

    @Override
    public String toString() {
        return "sumDistance=" + sumDistance + " ";
    }

    private long encode(Node node, Edge edge) {
        return encode(edge) * g.getNodes().size() + node.id;
    }

    private long encode(Edge edge) {
        int u = Math.min(edge.fromNode.id, edge.toNode.id);
        int v = Math.max(edge.fromNode.id, edge.toNode.id);
        return (long) u * g.getNodes().size() + v;
    }

    private Segment2D createSegment(Edge e) {
        VarNodePosition fromPos = positions.get(e.fromNode);
        VarNodePosition toPos = positions.get(e.toNode);

        if (fromPos.x() == -1 || fromPos.y() == -1 || toPos.x() == -1 || toPos.y() == -1)
            return null;

        return new Segment2D(
                new Point2D(fromPos.x(), fromPos.y()),
                new Point2D(toPos.x(), toPos.y())
        );
    }
    private Segment2D createSegment(Node node, int newX, int newY) {
        VarNodePosition pos = positions.get(node);
        if (pos.x() == -1 || pos.y() == -1) return null;
        return new Segment2D(
                new Point2D(newX, newY),
                new Point2D(pos.x(), pos.y())
        );
    }

    public SumDistanceNodeEdge(Graph g, Map<Node, VarNodePosition> positions) {
        this.g = g;
        this.positions = positions;
        adj = new ArrayList<>();
        Map<Long, Boolean> marked = new HashMap<>();
        for (Node node : g.getNodes()) {
            adj.add(node.id, new ArrayList<>());
            for (Edge e : g.getEdges(node)) {
                long eId = encode(e);
                if (marked.containsKey(eId)) continue;
                marked.put(eId, true);
                adj.get(node.id).add(e);
            }
            marked.clear();
        }
        edges = new ArrayList<>();
        for (Edge e : g.getEdges()) {
            long eId = encode(e);
            if (marked.containsKey(eId)) continue;
            marked.put(eId, true);
            edges.add(e);
        }
        distances = new HashMap<>();

        for (Node node : g.getNodes()) {
            VarNodePosition pos = positions.get(node);
            if (pos.x() != -1 && pos.y() != -1) {
                Point2D point = new Point2D(pos.x(), pos.y());
                for (Edge edge : edges) {
                    if (edge.fromNode.id == node.id || edge.toNode.id == node.id) continue;

                    Segment2D segment = createSegment(edge);
                    if (segment != null) {
                        double distance = point.distance(segment);
                        long key = encode(node, edge);
                        distances.put(key, distance);
                        sumDistance += distance;
                    }
                }
            }
        }
    }

    @Override
    public double evaluation() {
        return sumDistance;
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        int oldX = varNodePosition.x(), oldY = varNodePosition.y();
        if (newX == -1 && newY == -1) return evaluation();
        if (oldX == newX && oldY == newY) {
            return evaluation();
        }
        Node node = g.getNode(varNodePosition.id);
        double newSumDistance = sumDistance;
        Point2D point = new Point2D(newX, newY);
        for (Edge edge : edges) {
            if (edge.fromNode.id == node.id || edge.toNode.id == node.id) continue;
            Segment2D segment = createSegment(edge);
            if (segment == null) continue;

            double oldD = distances.getOrDefault(encode(node, edge), -1.);
            if (oldD > -1.) {
                newSumDistance -= oldD;
            }
            double distance = point.distance(segment);
            newSumDistance += distance;
        }

        for (Node adjNode : g.getNodes()) {
            if (adjNode.id == node.id) continue;

            VarNodePosition pos = positions.get(adjNode);
            if (pos.x() == -1 || pos.y() == -1) continue;
            Point2D pointNode = new Point2D(pos.x(), pos.y());
            for (Edge edge : adj.get(node.id)) {
                if (edge.toNode.id == adjNode.id) continue;
                long key = encode(adjNode, edge);

                // visited.put(key, true);
                double oldD = distances.getOrDefault(key, -1.);
                if (oldD > -1.) {
                    newSumDistance -= oldD;
                }

                Segment2D segment = createSegment(edge.toNode, newX, newY);
                if (segment != null) {
                    double distance = pointNode.distance(segment);
                    newSumDistance += distance;
                }
            }
        }
        return newSumDistance;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (newX == -1 && newY == -1) return;
        int oldX = varNodePosition.x(), oldY = varNodePosition.y();
        if (oldX == newX && oldY == newY) return;
        Node node = g.getNode(varNodePosition.id);
        Point2D point = new Point2D(newX, newY);
        for (Edge edge : edges) {
            if (edge.fromNode.id == node.id || edge.toNode.id == node.id) continue;
            Segment2D segment = createSegment(edge);
            if (segment == null) continue;

            double distance = point.distance(segment);

            long key = encode(node, edge);
            double oldD = distances.getOrDefault(key, -1.);
            if (oldD > -1.) {
                if (Math.abs(oldD - distance) < Point2D.eps) {
                    continue;
                }
                sumDistance -= oldD;
            }
            distances.put(key, distance);
            sumDistance += distance;
        }

        for (Node adjNode : g.getNodes()) {
            if (adjNode.id == node.id) continue;
            VarNodePosition pos = positions.get(adjNode);
            if (pos.x() == -1 || pos.y() == -1) continue;
            Point2D pointNode = new Point2D(pos.x(), pos.y());
            for (Edge edge : adj.get(node.id)) {
                if (edge.toNode.id == adjNode.id) continue;
                long key = encode(adjNode, edge);
                Segment2D segment = createSegment(edge.toNode, newX, newY);;
                if (segment != null) {
                    double distance = pointNode.distance(segment);
                    double oldD = distances.getOrDefault(key, -1.);
                    if (oldD > -1.) {
                        if (Math.abs(oldD - distance) < Point2D.eps) {
                            continue;
                        }
                        sumDistance -= oldD;
                    }
                    distances.put(key, distance);
                    sumDistance += distance;
                }
            }
        }
    }

    @Override
    public void initPropagation() {

    }
}

class MinDistanceNodeEdge implements Function {
    private final Graph g;
    private final Map<Node, VarNodePosition> positions;
//    private final Queue<Pair<Double, Long>> pq;
//    private final TreeSet<Pair<Double, Long>> pq;
    private final TreeMultiset<Double> pq;
    private final Map<Long, Double> distances;
    private final List<Edge> edges;
    private final List<List<Edge>> adj;

    @Override
    public String toString() {
        return "minDistance=" + evaluation() + " ";
    }

    private long encode(Node node, Edge edge) {
        return encode(edge) * g.getNodes().size() + node.id;
    }
    private long encode(Edge edge) {
        int u = Math.min(edge.fromNode.id, edge.toNode.id);
        int v = Math.max(edge.fromNode.id, edge.toNode.id);
        return (long) u * g.getNodes().size() + v;
    }

    private Segment2D createSegment(Edge e) {
        VarNodePosition fromPos = positions.get(e.fromNode);
        VarNodePosition toPos = positions.get(e.toNode);

        if (fromPos.x() == -1 || fromPos.y() == -1 || toPos.x() == -1 || toPos.y() == -1)
            return null;

        return new Segment2D(
                new Point2D(fromPos.x(), fromPos.y()),
                new Point2D(toPos.x(), toPos.y())
        );
    }
    private Segment2D createSegment(Node node, int newX, int newY) {
        VarNodePosition pos = positions.get(node);
        if (pos.x() == -1 || pos.y() == -1) return null;
        return new Segment2D(
                new Point2D(newX, newY),
                new Point2D(pos.x(), pos.y())
        );
    }

    public MinDistanceNodeEdge(Graph g, Map<Node, VarNodePosition> positions) {
        this.g = g;
        this.positions = positions;
        adj = new ArrayList<>();
        Map<Long, Boolean> marked = new HashMap<>();
        for (Node node : g.getNodes()) {
            adj.add(node.id, new ArrayList<>());
            for (Edge e : g.getEdges(node)) {
                long eId = encode(e);
                if (marked.containsKey(eId)) continue;
                marked.put(eId, true);
                adj.get(node.id).add(e);
            }
            marked.clear();
        }
        edges = new ArrayList<>();
        for (Edge e : g.getEdges()) {
            long eId = encode(e);
            if (marked.containsKey(eId)) continue;
            marked.put(eId, true);
            edges.add(e);
        }
        distances = new HashMap<>();
//        pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a.a));
//        pq = new TreeSet<>(Comparator.comparingDouble((Pair<Double, Long> p) -> p.a).thenComparing(p -> p.b));
        pq = new TreeMultiset<>(new DoubleCompare());

        for (Node node : g.getNodes()) {
            VarNodePosition pos = positions.get(node);
            if (pos.x() != -1 && pos.y() != -1) {
                Point2D point = new Point2D(pos.x(), pos.y());
                for (Edge edge : edges) {
                    if (edge.fromNode.id == node.id || edge.toNode.id == node.id) continue;

                    Segment2D segment = createSegment(edge);
                    if (segment != null) {
                        double distance = point.distance(segment);
                        long key = encode(node, edge);
                        distances.put(key, distance);
//                        pq.add(new Pair<>(distance, key));
                        pq.add(distance);
                    }
                }
            }
        }
//        System.err.println(pq.size() + " " + distances.size() + " " + pq.peek().a);
    }

    @Override
    public double evaluation() {
//        while (!pq.isEmpty()) {
//            Pair<Double, Long> p = pq.peek();
////            if (Double.compare(distances.getOrDefault(p.b, Double.POSITIVE_INFINITY), p.a) != 0) {
//            if (Math.abs(distances.getOrDefault(p.b, Double.POSITIVE_INFINITY) - p.a) > Point2D.eps) {
//                pq.poll();
//                continue;
//            }
//            return p.a;
//        }
////        return 0;
//        return Double.POSITIVE_INFINITY;
        if (pq.isEmpty()) return Double.POSITIVE_INFINITY;
        return pq.first();
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        int oldX = varNodePosition.x(), oldY = varNodePosition.y();
        if (newX == -1 && newY == -1) return evaluation();
        if (oldX == newX && oldY == newY) {
            return evaluation();
        }
        double min = Double.POSITIVE_INFINITY;
        Node node = g.getNode(varNodePosition.id);
//        List<Edge> edges = g.getEdges(node);
        Point2D point = new Point2D(newX, newY);
//        Map<Long, Boolean> visited = new HashMap<>();
        TreeMultiset<Double> visited = new TreeMultiset<>(new DoubleCompare());
        for (Edge edge : edges) {
            if (edge.fromNode.id == node.id || edge.toNode.id == node.id) continue;
            Segment2D segment = createSegment(edge);
            if (segment == null) continue;

            double oldD = distances.getOrDefault(encode(node, edge), -1.);
            if (oldD > -1.) {
                visited.add(oldD);
            }
            double distance = point.distance(segment);
//            visited.put(encode(node, edge), true);
            min = Math.min(min, distance);
        }

        for (Node adjNode : g.getNodes()) {
            if (adjNode.id == node.id) continue;

            VarNodePosition pos = positions.get(adjNode);
            if (pos.x() == -1 || pos.y() == -1) continue;
            Point2D pointNode = new Point2D(pos.x(), pos.y());
            for (Edge edge : adj.get(node.id)) {
                if (edge.toNode.id == adjNode.id) continue;
                long key = encode(adjNode, edge);

                // visited.put(key, true);
                double oldD = distances.getOrDefault(key, -1.);
                if (oldD > -1.) {
                    visited.add(oldD);
                }

                Segment2D segment = createSegment(edge.toNode, newX, newY);
                if (segment != null) {
                    double distance = pointNode.distance(segment);
                    min = Math.min(min, distance);
                }
            }
        }
//        if (node.id == 9 && newX == 9 && newY == 11) {
//            System.err.printf("(%d %d %d %d) min=%.10f ", oldX, oldY, newX, newY, min);
//        }
//        List<Pair<Double, Long>> tmp = new ArrayList<>();
//        while (!pq.isEmpty()) {
//            Pair<Double, Long> p = pq.poll();
//
////            if (Double.compare(distances.getOrDefault(p.b, Double.POSITIVE_INFINITY), p.a) != 0) {
//            if (Math.abs(distances.getOrDefault(p.b, Double.POSITIVE_INFINITY) - p.a) > Point2D.eps) {
//                continue;
//            }
//
//            tmp.add(p);
//            if (p.a >= min) {
//                pq.addAll(tmp);
//                return min;
//            }
//            if (visited.containsKey(p.b)) {
//                continue;
//            }
////            if (node.id == 9 && newX == 9 && newY == 11) {
////                int eid = (int) (p.b / g.getNodes().size());
////                int nid = (int) (p.b % g.getNodes().size());
////                Edge edge = null;
////                for (Edge e : g.getEdges()) {
////                    if (e.id == eid) {
////                        edge = e;
////                        break;
////                    }
////                }
////                Node node1 = null;
////                for (Node n : g.getNodes()) {
////                    if (n.id == nid) {
////                        node1 = n;
////                        break;
////                    }
////                }
////                VarNodePosition pos1 = positions.get(node1);
////                System.err.printf("p=(%.10f, %d %d %d, %d, %d) ", p.a, edge.id, edge.fromNode.id, edge.toNode.id, eid, nid);
////                System.err.printf("%.10f ", new Point2D(pos1.x(), pos1.y()).distance(createSegment(edge)));
////            }
//            pq.addAll(tmp);
//            return p.a;
//        }
//        pq.addAll(tmp);
        // for (Pair<Double, Long> p : pq) {
        //     if (visited.containsKey(p.b)) {
        //         continue;
        //     }
        //     if (p.a >= min) {
        //         return min;
        //     }
        //     return p.a;
        // }
        for (Double d : pq) {
            // if (d >= min) {
            if (d > min - Point2D.eps) {
                return min;
            }
            if (!visited.remove(d)) {
                return d;
            }
        }
        return min;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (newX == -1 && newY == -1) return;
        int oldX = varNodePosition.x(), oldY = varNodePosition.y();
        if (oldX == newX && oldY == newY) return;
        Node node = g.getNode(varNodePosition.id);
        Point2D point = new Point2D(newX, newY);
//        for (Edge edge : g.getEdges()) {
        for (Edge edge : edges) {
            if (edge.fromNode.id == node.id || edge.toNode.id == node.id) continue;
            Segment2D segment = createSegment(edge);
            if (segment == null) continue;

            double distance = point.distance(segment);
//            if (Double.compare(distance, 0) == 0) {
//                System.err.printf("%d %d %d %d %d %d %d %d %d %f\n",
//                        node.id, newX, newY, edge.fromNode.id, edge.toNode.id,
//                        positions.get(edge.fromNode).x(), positions.get(edge.fromNode).y(),
//                        positions.get(edge.toNode).x(), positions.get(edge.toNode).y(), distance);
//            }
            long key = encode(node, edge);
            double oldD = distances.getOrDefault(key, -1.);
            if (oldD > -1.) {
                if (Math.abs(oldD - distance) < Point2D.eps) {
                    continue;
                }
                // pq.remove(new Pair<>(distances.get(key), key));
                pq.remove(oldD);
            }
            distances.put(key, distance);
            // pq.add(new Pair<>(distance, key));
            pq.add(distance);
        }

        for (Node adjNode : g.getNodes()) {
            if (adjNode.id == node.id) continue;
            VarNodePosition pos = positions.get(adjNode);
            if (pos.x() == -1 || pos.y() == -1) continue;
            Point2D pointNode = new Point2D(pos.x(), pos.y());
            for (Edge edge : adj.get(node.id)) {
                if (edge.toNode.id == adjNode.id) continue;
                long key = encode(adjNode, edge);
                Segment2D segment = createSegment(edge.toNode, newX, newY);;
                if (segment != null) {
                    double distance = pointNode.distance(segment);
                    double oldD = distances.getOrDefault(key, -1.);
                    if (oldD > -1.) {
                        if (Math.abs(oldD - distance) < Point2D.eps) {
                            continue;
                        }
                        // pq.remove(new Pair<>(distances.get(key), key));
                        pq.remove(oldD);
                    }
                    distances.put(key, distance);
//                    if (Double.compare(distance, 0) == 0) {
//                        System.err.printf("%d %d %d %d %d %d %f\n",
//                                node.id, adjNode.id,
//                                pos.x(), pos.y(),
//                                newX, newY, distance);
//                    }
                    // pq.add(new Pair<>(distance, key));
                    pq.add(distance);
                }
            }
        }
    }

    @Override
    public void initPropagation() {

    }
}

class LexMultiValues{
    List<Double> values;
    public LexMultiValues(List<Double> values) {
        this.values = values;
    }

    public boolean better(LexMultiValues V){
        if (V == null) return true;
        for (int i = 0; i < values.size(); i++) {
            double a = values.get(i), b = V.values.get(i);
            if (a >= b + Point2D.eps) return true;
            if (a <= b + Point2D.eps) return false;
        }
        return false;
    }

    public boolean equals(LexMultiValues V){
        if (V == null) return false;
        for (int i = 0; i < values.size(); i++) {
            double a = values.get(i), b = V.values.get(i);
            if (Math.abs(a - b) > Point2D.eps) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (double v : values) {
            sb.append(String.format("%.10f ", v));
        }
        return sb.toString().trim();
    }
}

class ObjectiveFunction implements Function {
    private final Function f1;
    private final Function f2;
    private final Function f3;
    private final Function f4;
    private final double w1, w2, w3, w4;

    public ObjectiveFunction(Function f1, Function f2, Function f3, Function f4,  double w1, double w2, double w3, double w4) {
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.f4 = f4;
        this.w1 = w1;
        this.w2 = w2;
        this.w3 = w3;
        this.w4 = w4;
    }

    @Override
    public double evaluation() {
        return f1.evaluation() * w1 + f2.evaluation() * w2 + f3.evaluation() * w3 + f4.evaluation() * w4;
    }

    @Override
    public String toString() {
        return String.format("(f1=%.10f, f2=%.10f, f3=%.10f, f4=%.10f, evaluation=%.10f)",
                f1.evaluation(), f2.evaluation(), f3.evaluation(), f4.evaluation(), evaluation());
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        return f1.evaluateOneNodeMove(varNodePosition, newX, newY) * w1 +
                f2.evaluateOneNodeMove(varNodePosition, newX, newY) * w2 +
                f3.evaluateOneNodeMove(varNodePosition, newX, newY) * w3 +
                f4.evaluateOneNodeMove(varNodePosition, newX, newY) * w4;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        f1.propagateOneNodeMove(varNodePosition, newX, newY);
        f2.propagateOneNodeMove(varNodePosition, newX, newY);
        f3.propagateOneNodeMove(varNodePosition, newX, newY);
        f4.propagateOneNodeMove(varNodePosition, newX, newY);
    }

    @Override
    public void initPropagation() {
        // Not used in lexicographic multi-objective optimization
    }
}

class LexMultiFunctions{
    /*
    lexicographic multi-functions
     */
    List<Function> F;
    public LexMultiFunctions() {
        F = new ArrayList<>();
    }

    public void add(Function f) {
        F.add(f);
    }

    public LexMultiValues evaluation() {
        List<Double> vals = new ArrayList<>();
        for (Function f : F)
            vals.add(f.evaluation());
        return new LexMultiValues(vals);
    }

    public LexMultiValues evaluateOneNodeMove(VarNodePosition v, int x, int y){
        List<Double> vals = new ArrayList<>();
        for (Function f : F)
            vals.add(f.evaluateOneNodeMove(v, x, y));
        return new LexMultiValues(vals);
    }

    public void propagateOneNodeMove(VarNodePosition v, int x, int y) {
        for (Function f : F)
            f.propagateOneNodeMove(v, x, y);
    }

}

class LinkedNode<E> {
    public final E value;
    public LinkedNode<E> next;
    public LinkedNode<E> previous;

    public LinkedNode(E value, LinkedNode<E> next, LinkedNode<E> previous) {
        this.value = value;
        this.next = next;
        this.previous = previous;
    }

    public LinkedNode<E> next() {
        return next;
    }
    public LinkedNode<E> prev() {
        return previous;
    }
}

class DoubleLinkedList<E> {
    private LinkedNode<E> head;
    private LinkedNode<E> tail;
    private int length = 0;

    public DoubleLinkedList() {
        head = null;
        tail = null;
        length = 0;
    }
    public DoubleLinkedList(E value) {
        head = new LinkedNode<>(value, null, null);
        tail = head;
        length = 1;
    }
    public DoubleLinkedList(List<E> values) {
        if (values == null || values.isEmpty()) {
            head = null;
            tail = null;
            length = 0;
            return;
        }
        head = new LinkedNode<>(values.get(0), null, null);
        LinkedNode<E> current = head;
        for (int i = 1; i < values.size(); i++) {
            LinkedNode<E> newNode = new LinkedNode<>(values.get(i), null, current);
            current.next = newNode;
            current = newNode;
        }
        tail = current;
        length = values.size();
    }

    public int size() {
        return length;
    }

    public LinkedNode<E> add(E value) {
        LinkedNode<E> newNode = new LinkedNode<>(value, null, tail);
        if (tail == null) {
            head = newNode;
            tail = newNode;
            length = 1;
            return tail;
        }
        tail.next = newNode;
        tail = newNode;
        length++;
        return tail;
    }

    public LinkedNode<E> add(LinkedNode<E> node) {
        if (node == null) {
            return null;
        }
        if (tail == null) {
            head = node;
            tail = node;
            length = 1;
            return tail;
        }
        tail.next = node;
        node.previous = tail;
        tail = node;
        length++;
        return tail;
    }

    public LinkedNode<E> addAll(List<E> values) {
        LinkedNode<E> tail = this.tail;
        for (E value : values) {
            add(value);
        }
        return tail;
    }

    public LinkedNode<E> addFirst(E value) {
        LinkedNode<E> newNode = new LinkedNode<>(value, head, null);
        if (head == null) {
            head = newNode;
            tail = newNode;
            length = 1;
            return head;
        }
        head.previous = newNode;
        head = newNode;
        length++;
        return head;
    }

    public LinkedNode<E> insert(int index, E value) {
        if (index > length || index < 0) {
            return null;
        } else if (index == 0) {
            return addFirst(value);
        } else if (index == length) {
            return add(value);
        }

        LinkedNode<E> currentNode = get(index);
        LinkedNode<E> leadNode = currentNode.previous;
        LinkedNode<E> newNode = new LinkedNode<>(value, currentNode, leadNode);
        leadNode.next = newNode;
        currentNode.previous = newNode;
        length++;

        return newNode;
    }

    public LinkedNode<E> insert(LinkedNode<E> node, E value) {
        if (node == null) {
            return null;
        }
        LinkedNode<E> newNode = new LinkedNode<>(value, node.next, node);
        if (node.next != null) {
            node.next.previous = newNode;
        } else {
            tail = newNode;
        }
        node.next = newNode;
        length++;
        return newNode;
    }

    public LinkedNode<E> removeFirst() {
        if (head == null) {
            return null;
        }
        LinkedNode<E> removedNode = head;
        head = head.next;
        if (head != null) {
            head.previous = null;
        } else {
            tail = null;
        }
        length--;
        return removedNode;
    }

    public LinkedNode<E> removeLast() {
        if (tail == null) {
            return null;
        }
        LinkedNode<E> removedNode = tail;
        tail = tail.previous;
        if (tail != null) {
            tail.next = null;
        } else {
            head = null;
        }
        length--;
        return removedNode;
    }

    public void remove(LinkedNode<E> removedNode) {
        if (removedNode == null) {
            return;
        }
        if (removedNode == head) {
            removeFirst();
            return;
        } else if (removedNode == tail) {
            removeLast();
            return;
        }

        LinkedNode<E> previousNode = removedNode.previous;
        LinkedNode<E> nextNode = removedNode.next;

        if (previousNode != null) {
            previousNode.next = nextNode;
        }
        if (nextNode != null) {
            nextNode.previous = previousNode;
        }
        length--;
    }

    public void remove(int index) {
        if (index < 0 || index >= length) {
            return;
        }
        if (index == 0) {
            removeFirst();
            return;
        } else if (index == length - 1) {
            removeLast();
            return;
        }

        LinkedNode<E> nodeToRemove = get(index);
        LinkedNode<E> previousNode = nodeToRemove.previous;
        LinkedNode<E> nextNode = nodeToRemove.next;

        if (previousNode != null) {
            previousNode.next = nextNode;
        }
        if (nextNode != null) {
            nextNode.previous = previousNode;
        }
        length--;
    }

    public LinkedNode<E> get(int index) {
        LinkedNode<E> temp = head;
        while (index-- > 0) {
            temp = temp.next;
        }
        return temp;
    }

    public boolean isEmpty() {
        return length == 0;
    }

    public LinkedNode<E> getFirst() {
        return head;
    }
    public LinkedNode<E> getLast() {
        return tail;
    }

    public List<E> toList() {
        List<E> list = new ArrayList<>();
        LinkedNode<E> current = head;
        while (current != null) {
            list.add(current.value);
            current = current.next;
        }
        return list;
    }

    public List<LinkedNode<E>> toNodeList() {
        List<LinkedNode<E>> list = new ArrayList<>();
        LinkedNode<E> current = head;
        while (current != null) {
            list.add(current);
            current = current.next;
        }
        return list;
    }

}

class GraphFace {
    Graph g;
    // List<Node> nodes;
    DoubleLinkedList<Edge> face;
    // Map<Edge, LinkedNode<Edge>> edgeMap;
    // Map<Node, Edge> next, prev;

    GraphFace(Graph g, List<Node> nodes, DoubleLinkedList<Edge> face, Map<Edge, LinkedNode<Edge>> edgeMap) {
        this.g = g;
        // this.nodes = nodes;
        this.face = face;
        // this.edgeMap = edgeMap;
        // next = new HashMap<>();
        // prev = new HashMap<>();
        // LinkedNode<Edge> current = face.getFirst();
        // for (int i = 0; i < face.size(); i++) {
        //     Edge edge = current.value;
        //     Node fromNode = edge.fromNode;
        //     Node toNode = edge.toNode;
        //     next.put(fromNode, edge);
        //     prev.put(toNode, edge);
        //     current = current.next();
        // }
    }

    GraphFace(Graph g, DoubleLinkedList<Edge> face) {
        this.g = g;
        this.face = face;
    }

    // GraphFace split(Node u, Node v) {
    //     Edge newEdgeU = g.addEdge(u, v);
    //     Edge newEdgeV = g.addEdge(v, u);
    //     Edge edge = next.get(u);
    //     LinkedNode<Edge> edgeNode = edgeMap.get(edge);
    //     if (edgeNode == null) {
    //         return null; 
    //     }

    //     DoubleLinkedList<Edge> newFace = new DoubleLinkedList<>();
    //     LinkedNode<Edge> current = edgeNode;
    //     do {
    //         newFace.add(current.value);
    //         current = current.next();
    //     } while (current != edgeNode);

    //     return new GraphFace(g, nodes, newFace, edgeMap);
    // }
    GraphFace split(LinkedNode<Edge> u, LinkedNode<Edge> v) {
        if (u == null || v == null || u == v) {
            return null;
        }
        Edge newEdgeU = g.addEdge(u.value.fromNode, v.value.fromNode);
        Edge newEdgeV = g.addEdge(v.value.fromNode, u.value.fromNode);
        DoubleLinkedList<Edge> newFace = new DoubleLinkedList<>();
        LinkedNode<Edge> current = u;
        boolean check = false;
        do {
            LinkedNode<Edge> nextNode = current.next();
            face.remove(current);
            newFace.add(current);
            current = nextNode;
            if (current == null) {
                check = true;
                if ((current = face.getFirst()) == null) {
                    break;
                }
            }
        } while (current != v);

        if (check) face.addFirst(newEdgeU);
        else face.insert(current.prev(), newEdgeU);
        newFace.add(newEdgeV);
        return new GraphFace(g, newFace);
    }
}

class PlanarGraphGenerator {
    public static Graph generatePlanarGraph(int n, int m) {
        if (n < 1) n = 1;
        if (m < n - 1) m = n - 1; 
        if (m > n * 3 - 6) m = n * 3 - 6; 

        Random random = new Random();
        List<Node> nodes = new ArrayList<>();
        List<Integer> degrees = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            nodes.add(new Node(i));
            degrees.add(0);
        }

        List<DoubleLinkedList<Edge>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new DoubleLinkedList<>());
        }

        // build tree
        Graph graph = new Graph(nodes);
        Set<Integer> usedEdges = new HashSet<>();
        for (int i = 1; i < n; i++) {
            int j = random.nextInt(i);
            usedEdges.add(j * n + i);
            DoubleLinkedList<Edge> edge = adj.get(j);
            Edge eu = graph.addEdge(nodes.get(i), nodes.get(j));
            Edge ev = graph.addEdge(nodes.get(j), nodes.get(i));

            if (degrees.get(i) > 1) {
                // LinkedNode<Integer> node = edge.getFirst();
                adj.get(i).add(eu);
                edge.insert(random.nextInt(edge.size()), ev);
            }
            else {
                adj.get(i).add(eu);
                edge.add(ev);
            }
            // System.err.println(i + " " + j);
        }

        DoubleLinkedList<Edge> faceEdge = new DoubleLinkedList<>(graph.dfs(nodes.get(0), adj));
        // LinkedNode<Edge> current = faceEdge.getFirst();
        // Map<Edge, LinkedNode<Edge>> edgeMap = new HashMap<>();
        // for (int i = 0; i < faceEdge.size(); i++) {
            // Edge edge = current.value;
            // edgeMap.put(edge, current);
            // current = current.next();
        // }

        DoubleLinkedList<GraphFace> faces = new DoubleLinkedList<>(new GraphFace(graph, faceEdge));
        for (int i = n - 1; i < m; i++) {
            // System.err.printf("n=%d, i=%d, faces.size()=%d\n", n, i, faces.size());
            GraphFace face = faces.removeFirst().value;
            GraphFace newFace = null;

            List<LinkedNode<Edge>> faceEdges = face.face.toNodeList();
            // for (LinkedNode<Edge> edge : faceEdges) {
            //     System.err.printf("(%d %d) ", edge.value.fromNode.id, edge.value.toNode.id);
            // }
            // System.err.println();

            Collections.shuffle(faceEdges, random);

            int p = faceEdges.size();
            boolean found = false;
            for (int j = 1; !found && j < p; j++) {
                LinkedNode<Edge> u = faceEdges.get(j);
                LinkedNode<Edge> nextU = u.next();
                if (nextU == null) {
                    nextU = face.face.getFirst();
                }
                for (int k = 0; k < j; k++) {
                    LinkedNode<Edge> v = faceEdges.get(k);
                    LinkedNode<Edge> nextV = v.next();
                    if (nextV == null) {
                        nextV = face.face.getFirst();
                    }
                    if (u == v || nextU == v || nextV == u || u.value.fromNode.id == v.value.fromNode.id) {
                        continue; 
                    }
                    int eId = Math.min(u.value.fromNode.id, v.value.fromNode.id) * n + 
                            Math.max(u.value.fromNode.id, v.value.fromNode.id);
                    if (usedEdges.contains(eId)) {
                        continue; 
                    }

                    usedEdges.add(eId);
                    newFace = face.split(u, v);
                    // System.err.println(u.value.fromNode.id + " " + u.value.toNode.id + " " +
                    //         v.value.fromNode.id + " " + v.value.toNode.id);
                    if (newFace != null) {
                        found = true;
                        break; 
                    }
                }
            }

            if (face.face.size() > 3) {
                faces.add(face);
            }
            if (newFace.face.size() > 3) {
                faces.add(newFace);
            }
        }
        // faceEdge.addAll(graph.dfs(nodes.get(0)));


        return graph;
    }
}

class MoveNode {
    public final VarNodePosition varNodePosition;
    public final int newX, newY;

    public MoveNode(VarNodePosition varNodePosition, int newX, int newY) {
        this.varNodePosition = varNodePosition;
        this.newX = newX;
        this.newY = newY;
    }

    @Override
    public String toString() {
        return String.format("Move(%d, %d) to (%d, %d)", varNodePosition.id, varNodePosition.x(), varNodePosition.y(), newX, newY);
    }
}

class Move {
    public List<MoveNode> moves;
    public LexMultiValues values;

    public Move(VarNodePosition varNodePosition, int newX, int newY) {
        MoveNode move = new MoveNode(varNodePosition, newX, newY);
        this.moves = new ArrayList<>();
        moves.add(move);
        this.values = null;
    }

    public Move(VarNodePosition varNodePosition, int newX, int newY, LexMultiValues values) {
        MoveNode move = new MoveNode(varNodePosition, newX, newY);
        this.moves = new ArrayList<>();
        moves.add(move);
        this.values = values;
    }

    public Move(List<MoveNode> moves, LexMultiValues values) {
        this.moves = moves;
        this.values = values;
    }

    public MoveNode ins(VarNodePosition varNodePosition, int newX, int newY) {
        MoveNode moveNode = new MoveNode(varNodePosition, newX, newY);
        moves.add(moveNode);
        return moveNode;
    }

    public boolean better(Move o) {
        if (o == null || o.values == null) return true;
        if (values == null) return false;
        return values.better(o.values);
    }

    public boolean better(LexMultiValues o) {
        if (o == null) return true;
        if (values == null) return false;
        return values.better(o);
    }
}

interface NeighborExplorer {
    /**
     * Explore the neighborhood of the current solution.
     * @return The solution found in the neighborhood.
     */
    Move explore(boolean firstImprovement);
}

class OneRandomNeighborhood implements NeighborExplorer {
    private final int ROW, COL;
    private final Graph G;
    private final LexMultiFunctions F;
    private final List<VarNodePosition> varNodeList;
    private final int counter;

    public OneRandomNeighborhood(int ROW, int COL, Graph G, LexMultiFunctions F, List<VarNodePosition> varNodeList, int counter) {
        this.ROW = ROW;
        this.COL = COL;
        this.G = G;
        this.F = F;
        this.varNodeList = varNodeList;
        this.counter = counter;
    }

    @Override
    public Move explore(boolean firstImprovement) {
        List<Move> moves = new ArrayList<>();
            int iterations = counter;
            LexMultiValues values = F.evaluation();
            Random random = new Random();
            while (iterations-- > 0) {
                int i = random.nextInt(G.getNodes().size());
                VarNodePosition varNodePosition = varNodeList.get(i);
                int newX = random.nextInt(ROW);
                int newY = random.nextInt(COL);

                LexMultiValues current = F.evaluateOneNodeMove(varNodePosition, newX, newY);
                Move move = new Move(varNodePosition, newX, newY, current);
                if (current.better(values)) {
                    if (firstImprovement) {
                        moves.add(move);
                        break;
                    }
                    moves.clear();
                    moves.add(move);
                    values = current;
                }
                else if (current.equals(values)) {
                    moves.add(move);
                }
            }

        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(random.nextInt(moves.size()));
    }
}

class TwoRandomNeighborhood implements NeighborExplorer {
    private final int ROW, COL;
    private final Graph G;
    private final CBLSGPModel model;
    private final LexMultiFunctions F;
    private final List<VarNodePosition> varNodeList;
    private final int counter;

    public TwoRandomNeighborhood(int ROW, int COL, Graph G, CBLSGPModel model, LexMultiFunctions F, List<VarNodePosition> varNodeList, int counter) {
        this.ROW = ROW;
        this.COL = COL;
        this.G = G;
        this.model = model;
        this.F = F;
        this.varNodeList = varNodeList;
        this.counter = counter;
    }

    @Override
    public Move explore(boolean firstImprovement) {
        List<Move> moves = new ArrayList<>();
            int iterations = counter;
            LexMultiValues values = F.evaluation();
            Random random = new Random();
            while (iterations-- > 0) {
                int i1 = random.nextInt(G.getNodes().size()), i2;
                do {
                    i2 = random.nextInt(G.getNodes().size());
                } while (i1 == i2);

                VarNodePosition N1 = varNodeList.get(i1);
                VarNodePosition N2 = varNodeList.get(i2);

                int oldX1 = N1.x();
                int oldY1 = N2.y();

                int newX1 = random.nextInt(ROW);
                int newY1 = random.nextInt(COL);
                int newX2 = random.nextInt(ROW);
                int newY2 = random.nextInt(COL);

                F.propagateOneNodeMove(N1, newX1, newY1);
                model.move(N1, newX1, newY1);

                LexMultiValues current = F.evaluateOneNodeMove(N2, newX2, newY2);
                Move move = new Move(N1, newX1, newY1, current);
                move.ins(N2, newX2, newY2);

                if (current.better(values)) {
                    if (firstImprovement) {
                        moves.add(move);
                        break;
                    }
                    moves.clear();
                    moves.add(move);
                    values = current;
                }
                else if (current.equals(values)) {
                    moves.add(move);
                }

                F.propagateOneNodeMove(N1, oldX1, oldY1);
                model.move(N1, oldX1, oldY1);
            }

        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(random.nextInt(moves.size()));
    }
}

class PQTree {
    private int n, root, tot, top;
    private int[] pool, type, color;
    private List<List<Integer>> adj;
    private List<Integer> ans;
    private String s;

    public PQTree() {}

    public PQTree(int n) {
        this.n = n;
        root = tot = n + 1;
        adj = new ArrayList<>(n + 2);
        ans = new ArrayList<>();
        for (int i = 0; i <= n + 1; i++) adj.add(new ArrayList<>());
        for (int i = 1; i <= n; i++) adj.get(root).add(i);
        pool = new int[n + 1];
        type = new int[n + 1];
        color = new int[n + 1];
        s = "";
    }

    public void insert(String s) {
        this.s = s;
        dfs(root);
        work(root);
        while (adj.get(root).size() == 1) {
            root = adj.get(root).get(0);
        }
        remove(root);
    }

    public List<Integer> getAns() {
        dfsAns(root);
        return ans;
    }

    private void fail() {

    }

    private int newNode(int y) {
        int x = top > 0 ? pool[top--] : ++tot;
        type[x] = y;
        return x;
    }

    private void delete(int u) {
        adj.get(u).clear();
        pool[++top] = u;
    }

    private void dfs(int u) {
        if (u > 0 && u <= n) {
            color[u] = s.charAt(u - 1) == '1' ? 1 : 0;
            return ;
        }

        boolean c0 = false, c1 = false;
        for (int v : adj.get(u)) {
            dfs(v);
            if (color[v] != 1) c0 = true;
            if (color[v] > 0) c1 = true;
        }

        if (c0 && c1) {
            color[u] = 2;
        }
        else if (c0) {
            color[u] = 0;
        }
        else if (c1) {
            color[u] = 1;
        }
    }

    private boolean check(List<Integer> v) {
        int j = -1;
        for (int i = 0; i < v.size(); i++) {
            if (color[v.get(i)] == 2) {
                if (j != -1) return false;
                j = i;
                break;
            }
        }

        if (j == -1) {
            for (int i = 0; i < v.size(); i++) {
                if (color[v.get(i)] > 0) {
                    j = i;
                    break;
                }
            }
        }
        
        for (int i = 0; i < j; i++) {
            if (color[v.get(i)] > 0) {
                return false;
            }
        }

        for (int i = j + 1; i < v.size(); i++) {
            if (color[v.get(i)] != 1) {
                return false;
            }
        }

        return true;
    }

    private List<Integer> split(int u) {
        if (color[u] == 2) {
            List<Integer> v = new ArrayList<>();
            v.add(u);
            return v;
        }

        List<Integer> ng = new ArrayList<>();
        if (type[u] == 1) {
            if (!check(adj.get(u))) {
                Collections.reverse(adj.get(u));
                if (!check(adj.get(u))) {
                    fail();
                }
            }
            for (int v : adj.get(u)) {
                if (color[u] != 2) {
                    ng.add(v);
                }
                else {
                    List<Integer> w = split(v);
                    ng.addAll(w);
                }
            }
        }
        else {
            List<List<Integer>> son = new ArrayList<>();
            for (int col = 0; col < 3; col++) {
                son.add(new ArrayList<>());
            }
            for (int v : adj.get(u)) {
                son.get(color[v]).add(v);
            }
            if (son.get(2).size() > 1) {
                fail();
            }
            if (!son.get(0).isEmpty()) {
                int n0 = newNode(0);
                // adj.set(n0, son.get(0));
                adj.get(n0).addAll(son.get(0));
                ng.add(n0);
            }
            if (!son.get(2).isEmpty()) {
                List<Integer> w = split(son.get(2).get(0));
                ng.addAll(w);
            }
            if (!son.get(1).isEmpty()) {
                int n1 = newNode(1);
                // adj.set(n1, son.get(1));
                adj.get(n1).addAll(son.get(1));
                ng.add(n1);
            }
        }

        delete(u);
        return ng;

    }

    private void work(int u) {
        if (color[u] != 2) {
            return ;
        }
        if (type[u] > 0) {
            int l = (int)1e9, r = (int)-1e9;
            for (int i = 0; i < adj.get(u).size(); i++) {
                if (color[adj.get(u).get(i)] > 0) {
                    l = Math.min(l, i);
                    r = Math.max(r, i);
                }
            }

            for (int i = l + 1; i < r; i++) {
                if (color[adj.get(u).get(i)] != 1) {
                    fail();
                }
            }

            if (l == r & color[adj.get(u).get(l)] == 2) {
                work(adj.get(u).get(l));
                return ;
            }

            List<Integer> ng = new ArrayList<>();
            for (int i = 0; i < l; i++) {
                ng.add(adj.get(u).get(i));
            }
            List<Integer> w = split(adj.get(u).get(l));
            ng.addAll(w);
            for (int i = l + 1; i < r; i++) {
                ng.add(adj.get(u).get(i));
            }
            if (l != r) {
                w = split(adj.get(u).get(r));
                Collections.reverse(w);
                ng.addAll(w);
            }
            for (int i = r + 1; i < adj.get(u).size(); i++) {
                ng.add(adj.get(u).get(i));
            }
            // adj.set(u, ng);
            adj.get(u).clear();
            adj.get(u).addAll(ng);
            return ;
        }
        else {
            List<List<Integer>> son = new ArrayList<>();
            for (int col = 0; col < 3; col++) {
                son.add(new ArrayList<>());
            }
            for (int v : adj.get(u)) {
                son.get(color[v]).add(v);
            }

            if (son.get(1).isEmpty() && son.get(2).size() == 1) {
                work(son.get(2).get(0));
                return ;
            }

            adj.get(u).clear();
            if (son.get(2).size() > 2) {
                fail();
            }

            adj.get(u).addAll(son.get(0));
            int n1 = newNode(1);
            adj.get(u).add(n1);
            if (!son.get(2).isEmpty()) {
                List<Integer> w = split(son.get(2).get(0));
                adj.get(n1).addAll(w);
            }
            if (!son.get(1).isEmpty()) {
                int n2 = newNode(0);
                adj.get(n1).add(n2);
                adj.get(n2).addAll(son.get(1));
            }

            if (son.get(2).size() > 1) {
                List<Integer> w = split(son.get(2).get(1));
                Collections.reverse(w);
                adj.get(n1).addAll(w);
            }
        }
    }

    private void remove(int u) {
        for (int v : adj.get(u)) {
            int tv = v;
            while (adj.get(tv).size() == 1) {
                int t = tv;
                tv = adj.get(tv).get(0);
                delete(t);
            }
            v = tv;
            remove(v);
        }
    }

    private void dfsAns(int u) {
        if (u > 0 && u <= n) {
            ans.add(u);
            return ;
        }
        for (int v : adj.get(u)) {
            dfsAns(v);
        }
    }
}

class STNumbering {
    STNumbering() {}

    int n, s, t;
    private int[] tin, low, parent, pos, ans;
    private boolean[] b, vis;
    private int timer;
    private List<List<Integer>> adj, st;
    private int count = 0;

    STNumbering(int n, int s, int t) {
        this.n = n;
        this.s = s;
        this.t = t;
        tin = new int[n + 1];
        low = new int[n + 1];
        parent = new int[n + 1];
        pos = new int[n + 1];
        b = new boolean[n + 1];
        timer = 0;
        ans = new int[n + 1];
        vis = new boolean[n + 1];
        // adj = new ArrayList<>();
        // for (int i = 0; i <= n; i++) {
        //     adj.add(new ArrayList<>());
        // }
        st = new ArrayList<>();
        for (int i = 0; i <= n; i++) {
            st.add(new ArrayList<>());
        }
    }

    // STNumbering(int n, int s, int t, List<List<Integer>> adj) {
    //     // this.n = n;
    //     // this.s = s;
    //     // this.t = t;
    //     // tin = new int[n + 1];
    //     // low = new int[n + 1];
    //     // parent = new int[n + 1];
    //     // pos = new int[n + 1];
    //     // b = new boolean[n + 1];
    //     // timer = 0;
    //     // st = new ArrayList<>();
    //     // for (int i = 0; i <= n; i++) {
    //     //     st.add(new ArrayList<>());
    //     // }
    //     this(n, s, t);
    //     this.adj = adj;
    // }

    STNumbering(int n, int s, int t, List<DoubleLinkedList<Edge>> adj) {
        this(n, s, t);
        this.adj = new ArrayList<>();
        for (int i = 0; i <= n; i++) {
            this.adj.add(new ArrayList<>());
        }

        for (int i = 0; i < n; i++) {
            DoubleLinkedList<Edge> e = adj.get(i);
            LinkedNode<Edge> cur = e.getFirst();
            while (cur != null) {
                int v = cur.value.toNode.id;
                this.adj.get(i).add(v);
                cur = cur.next();
            }
        }
        this.adj.get(s).add(t);
    }

    private void tarjan(int u, int p) {
        tin[u] = low[u] = ++timer;
        pos[timer] = u;
        b[u] = u == t;
        parent[u] = p;

        for (int v : adj.get(u)) {
            if (v == p) continue;
            if (tin[v] == 0) {
                tarjan(v, u);
                b[u] |= b[v];
                low[u] = Math.min(low[u], low[v]);
                if (low[v] >= tin[u]) {
                    count++;
                }
            }
            else {
                low[u] = Math.min(low[u], tin[v]);
            }
        }

        if (!b[u]) {
            st.get(p).add(u);
            st.get(pos[low[u]]).add(u);
        }
    }


    private void dfs(int u) {
        if (vis[u]) {
            return ;
        }
        vis[u] = true;
        ans[u] = count++;
        for (int v : st.get(u)) {
            dfs(v);
        }
    }

    private void solve(int u) {
        if (u == -1) {
            return ;
        }
        solve(parent[u]);
        dfs(u);
    }

    public boolean process() {
        timer = count = 0;
        tarjan(s, -1);
        if (count != 1) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (tin[i] == 0) {
                return false;
            }
        }

        count = 0;
        solve(t);
        return true;
    }

    public int[] getAns() {
        return ans;
    }
}

public class Main {
    static final int numDirections = 8;
    static final Integer[] dirX = {0, 1, 0, -1, 1, 1, -1, -1};
    static final Integer[] dirY = {1, 0, -1, 0, 1, -1, -1, 1};

    public static void planarGen() throws IOException {
        List<Integer> N = List.of(10, 20, 50, 100, 200);
        Random random = new Random();

        for (int i = 0; i < N.size(); i++) {
            Kattio io = new Kattio(null, "tests/" + i + ".in");

            int n = N.get(i);
            int m = random.nextInt(Math.min(n * 3 - 6, n * 2), n * 3 - 5); 
            Graph g = PlanarGraphGenerator.generatePlanarGraph(n, m);
            io.printf("%d %d\n%d %d\n", n, n, n, m);
            Set<Integer> used = new HashSet<>();
            List<Edge> edges = g.getEdges();
            Collections.shuffle(edges, random);
            for (Edge edge : edges) {
                int eId = Math.min(edge.fromNode.id, edge.toNode.id) * n + Math.max(edge.fromNode.id, edge.toNode.id);
                if (used.contains(eId)) continue;
                used.add(eId);
                io.printf("%d %d\n", edge.fromNode.id + 1, edge.toNode.id + 1);
            }

            io.close();
            System.out.printf("Test %d: H=%d, W=%d, n=%d, m=%d\n", i, n, n, n, m);
        }
    }

    public static void completeGen() throws IOException {
        List<Integer> N = List.of(5, 6);

        int start = 15;
        for (int c = 0; c < N.size(); c++) {
            Kattio io = new Kattio(null, "tests/" + (start + c) + ".in");

            int n = N.get(c);
            int m = n * (n - 1) >> 1;
            io.printf("%d %d\n%d %d\n", n, n, n, m);
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    io.printf("%d %d\n", i + 1, j + 1);
                }
            }
            io.close();
            System.out.printf("Test %d: H=%d, W=%d, n=%d, m=%d\n", c, n, n, n, m);
        }
    }

    public static void circleGen() throws IOException {
        List<Integer> N = List.of(10, 20);

        int start = 7;
        for (int c = 0; c < N.size(); c++) {
            Kattio io = new Kattio(null, "tests/" + (start + c) + ".in");

            int n = N.get(c);
            int m = n;
            io.printf("%d %d\n%d %d\n", n, n, n, m);
            for (int i = 0; i < n; i++) {
                io.printf("%d %d\n", i + 1, (i + 1) % n + 1);
            }
            io.close();
            System.out.printf("Test %d: H=%d, W=%d, n=%d, m=%d\n", c, n, n, n, m);
        }
    }

    public static void completeBipartiteGen() throws IOException {
        List<Integer> N = List.of(6, 8);

        int start = 11;
        for (int c = 0; c < N.size(); c++) {
            Kattio io = new Kattio(null, "tests/" + (start + c) + ".in");

            int n = N.get(c);
            int m = n * n >> 2;
            io.printf("%d %d\n%d %d\n", n, n, n, m);
            for (int i = 0; i < (n / 2); i++) {
                for (int j = n / 2; j < n; j++) {
                    io.printf("%d %d\n", i + 1, j + 1);
                }
            }
            io.close();
            System.out.printf("Test %d: H=%d, W=%d, n=%d, m=%d\n", c, n, n, n, m);
        }
    }

    public static void wheelGen() throws IOException {
        List<Integer> N = List.of(6,11);

        int start = 17;
        for (int c = 0; c < N.size(); c++) {
            Kattio io = new Kattio(null, "tests/" + (start + c) + ".in");

            int n = N.get(c);
            int m = n - 1 << 1;
            io.printf("%d %d\n%d %d\n", n, n, n, m);
            for (int i = 1; i < n; i++) {
                io.printf("%d %d\n", 1, i + 1);
            }
            for (int i = 1; i < n - 1; i++) {
                io.printf("%d %d\n", i + 1, i + 2);
            }
            io.printf("%d %d\n", n, 2);
            io.close();
            System.out.printf("Test %d: H=%d, W=%d, n=%d, m=%d\n", start + c, n, n, n, m);
        }
    }

    public static void ncubesGen() throws IOException {
        List<Integer> N = List.of(4,5,3);

        int start = 19;
        for (int c = 0; c < N.size(); c++) {
            Kattio io = new Kattio(null, "tests/" + (start + c) + ".in");

            int n = 1 << N.get(c);
            int m = N.get(c) << (N.get(c) - 1);
            io.printf("%d %d\n%d %d\n", n, n, n, m);
            for (int i = 0; i < n; i++) {
                // for (int j = 0; j < i; j++) {
                //     if (Integer.bitCount(i ^ j) == 1) {
                //         io.printf("%d %d\n", i + 1, j + 1);
                //     }
                // }
                for (int j = 0; j < N.get(c); j++) {
                    if ((i >> j & 1) == 1) {
                        io.printf("%d %d\n", i + 1, (i ^ 1 << j) + 1);
                    }
                }
            }
            io.close();
            System.out.printf("Test %d: H=%d, W=%d, n=%d, m=%d\n", start + c, n, n, n, m);
        }
    }

    public static void generateInitialSolution(int ROW, int COL, CBLSGPModel model, Graph G, List<DoubleLinkedList<Edge>> adj) {
        int n = G.getNodes().size();
        List<List<Integer>> radius = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            radius.add(new ArrayList<>());
        }
        for (Node node : G.getNodes()) {
            List<Integer> dist = G.bfs(node, adj);
            radius.get(Collections.max(dist) + 1 >> 1).add(node.id);
        }


        int count = 0;
        for (int i = 0; i < n; i++) {
            count += radius.get(i).size() > 0 ? 1 : 0;
        }

        double centerX = COL / 2.0;
        double centerY = ROW / 2.0;
        Point2D center = new Point2D(centerX, centerY);
        boolean isFirst = true;
        for (int i = 0, j = 0; i < n; i++) {
            if (radius.get(i).size() > 0) {
                if (isFirst) {
                    if (radius.get(i).size() == 1) {
                        int id = radius.get(i).get(0);
                        VarNodePosition v = model.getVarNode(id);
                        model.move(v, (int) centerX, (int) centerY);
                        count--;

                        isFirst = false;
                        continue;
                    }
                    isFirst = false;
                }

                j++;
                int m = radius.get(i).size();
                double angle = 2 * Math.PI / m;
                double polar = 0;
                for (Integer id : radius.get(i)) {
                    VarNodePosition v = model.getVarNode(id);
                    Point2D cutPoint = center.ray(polar, 0, 0, COL, ROW);
                    // if (cutPoint == null) {
                    //     System.err.println(center.x() + " " + center.y() + " " + polar + " " + i + " " + j + " " + Math.cos(polar) + " " + Math.sin(polar));
                    // }

                    double distance = center.distance(cutPoint) / count * j;
                    double x = centerX + distance * Math.cos(polar) + 0.5;
                    double y = centerY + distance * Math.sin(polar) + 0.5;
                    model.move(v, (int) x, (int) y);

                    polar += angle;
                    // System.err.printf("(%d, %d) ", x, y);
                }
            }
        }

    }

    public static void test1() throws IOException {
        Kattio io = new Kattio("tests/1.in");
        // int ROW = 20;
        // int COL = 20; // the graph is presented on a grid ROW x COL
        int ROW = io.nextInt();
        int COL = io.nextInt(); // the graph is presented on a grid ROW x COL
        // int n  = 20;// number of nodes 0,1, 2, ..., n-1
        int n = io.nextInt(); // number of nodes 0,1, 2, ..., n-1
        List<Node> nodes = new ArrayList<>();
        for(int i = 0; i < n; i++){
            nodes.add(new Node(i));
        }
        Graph G = new Graph(nodes);
        int n_edges = io.nextInt(); // number of edges
        List<DoubleLinkedList<Edge>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new DoubleLinkedList<>());
        }
        for (int i = 0; i < n_edges; i++) {
            int u = io.nextInt() - 1;
            int v = io.nextInt() - 1;
            // if (u == v) continue; // skip self-loops
            
            io.printf("(%d, %d), ", u, v); 

            Node fromNode = G.getNode(u);
            Node toNode = G.getNode(v);
            if (fromNode == null || toNode == null) continue;

            Edge eu = G.addEdge(fromNode, toNode);
            adj.get(u).add(eu);
            Edge ev = G.addEdge(toNode, fromNode);
            adj.get(v).add(ev);
        }
        io.println();

        Map<Node, VarNodePosition> varPos = new HashMap<>();
        List<VarNodePosition> varPosList = new ArrayList<>();
        Set<Integer> DX = new HashSet<>();
        Set<Integer> DY = new HashSet<>();

        for(int i = 0; i <= COL; i++) DX.add(i);
        for(int i = 0; i <= ROW; i++) DY.add(i);
        // varPos.get(node) is the coordinate of node, to be optimized, so that G is displayed nicely on the plane
        for(int i = 0; i < n; i++){
            varPos.put(nodes.get(i), new VarNodePosition(i,DX,DY));
            varPosList.add(varPos.get(nodes.get(i)));
        }

        CBLSGPModel model = new CBLSGPModel();
//         Random rnd = new Random(23480329);
        for (Node node : G.getNodes()) {
            VarNodePosition v = varPos.get(node);
            model.addVarNode(v);
            // model.move(v, rnd.nextInt(COL + 1), rnd.nextInt(ROW + 1));
//            F.propagateOneNodeMove(v, v.x(), v.y());
        }

        generateInitialSolution(ROW, COL, model, G, adj);

        NumberIntersectionEdges F3 = new NumberIntersectionEdges(G,varPos);// to be minimized
        MinAngle F2 = new MinAngle(G,varPos);// to be maximized
        SumAngle F2a = new SumAngle(G,varPos);// to be maximized
        MinDistanceEdge F1 = new MinDistanceEdge(G,varPos);// to be maximized
        SumDistanceEdge F1a = new SumDistanceEdge(G,varPos);// to be maximized
        MinDistanceNodeEdge F4 = new MinDistanceNodeEdge(G,varPos);// to be maximized
        SumDistanceNodeEdge F4a = new SumDistanceNodeEdge(G,varPos);// to be maximized

        LexMultiFunctions F = new LexMultiFunctions();
        F.add(F3);
        F.add(F2);
        F.add(F2a);
        F.add(F1);
        F.add(F1a);
        F.add(F4);
        F.add(F4a);

        model.close();


        // for (Node node : G.getNodes()) {
        //     VarNodePosition v = varPos.get(node);
        //     System.err.printf("%d: (%d, %d)\n", node.id, v.x(), v.y());
        // }

        LexMultiValues solutionEval = F.evaluation();
        List<NodePosition> solutionPositions = new ArrayList<>();
        for (Node node : G.getNodes()) {
            VarNodePosition v = varPos.get(node);
            solutionPositions.add(new NodePosition(v.id, v.x(), v.y()));
        }

        StringBuilder str = new StringBuilder();
        for (Function f : F.F) {
            str.append(f);
        }
        
        boolean firstImprovement = true;
        List<NeighborExplorer> explorers = new ArrayList<>();
        explorers.add(new OneRandomNeighborhood(ROW, COL, G, F, varPosList, 1000));
        explorers.add(new TwoRandomNeighborhood(ROW, COL, G, model, F, varPosList, 1000));
        final int maxIterations = 10000;
        for (int iter = 0; iter < maxIterations; iter++) {
            Move selectedMove = null;
            for (int k = 0; k < 2; k++) {
                Move move = explorers.get(k).explore(firstImprovement);
                if (move != null) {
                    if (firstImprovement) {
                        selectedMove = move;
                        break;
                    }
                    else {
                        if (move.better(selectedMove)) {
                            selectedMove = move;
                        }
                    }
                }
            }
            if (selectedMove != null) {
                for (MoveNode moveNode : selectedMove.moves) {
                    VarNodePosition pos = moveNode.varNodePosition;
                    int newX = moveNode.newX;
                    int newY = moveNode.newY;
                    
                    F.propagateOneNodeMove(pos, newX, newY);
                    model.move(pos, newX, newY);
                }
                if (selectedMove.better(solutionEval)) {
                    solutionEval = selectedMove.values;
                    str.setLength(0);
                    for (Function f : F.F) {
                        str.append(f);
                    }
                    model.setNodePositionsValue(solutionPositions);
                }
            }
            else {
                // generateInitialSolution(ROW, COL, model, G, adj);
                Random rnd = new Random();
                for (int i = 0; i < n; i++) {
                    VarNodePosition v = varPos.get(nodes.get(i));
                    int newX = rnd.nextInt(COL + 1);
                    int newY = rnd.nextInt(ROW + 1);
                    F.propagateOneNodeMove(v, newX, newY);
                    model.move(v, newX, newY);
                }
            }
        }


        System.err.printf("%s\n%s\n", str, solutionEval);
        for (NodePosition pos : solutionPositions) {
            System.err.printf("%d: (%d, %d),\n", pos.id(), pos.x(), pos.y());
        }

        for (NodePosition pos : solutionPositions) {
            io.printf("%d: (%d, %d), ", pos.id(), pos.x(), pos.y());
        }
        io.println();
        {
            for (int i = 0; i < n; i++) {
                VarNodePosition pos = varPos.get(nodes.get(i));
                NodePosition pos1 = solutionPositions.get(i);
                pos.assign(pos1.x(), pos1.y());
            }
            NumberIntersectionEdges f3 = new NumberIntersectionEdges(G, varPos);// to be minimized
            MinAngle f2 = new MinAngle(G, varPos);// to be maximized
            MinDistanceEdge f1 = new MinDistanceEdge(G, varPos);// to be maximized
            MinDistanceNodeEdge f4 = new MinDistanceNodeEdge(G, varPos);// to be maximized
            Function fObj = new ObjectiveFunction(f3, f2, f1, f4, 100.0, 200.0, 1.0, 4);
            System.err.println(fObj);
        }

        io.close();
    }
    public static void main(String[] args){
        long startTime = System.currentTimeMillis();
        try {
            test1();
            // planarGen();
            // completeGen();
            // circleGen();
            // completeBipartiteGen();
            // wheelGen();
            // ncubesGen();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.err.printf("Time taken: %.3f s\n", (endTime - startTime) / 1000.);
    }
}

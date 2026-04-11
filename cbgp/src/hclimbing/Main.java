package hclimbing;

import java.io.File;
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
        if (!(obj instanceof Pair<?, ?> other))
            return false;
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

    public static double squareDistance(double x1, double y1, double x2, double y2) {
        return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
    }

    public static double angle(double x1, double y1, double x2, double y2, double x3, double y3) {
        double d1 = distance(x1, y1, x2, y2);
        double d2 = distance(x1, y1, x3, y3);
        if (d1 < Point2D.eps || d2 < Point2D.eps)
            return 0;
        double cosA = dot(x2 - x1, y2 - y1, x3 - x1, y3 - y1) / d1 / d2;
        return Math.acos(Math.max(-1, Math.min(1, cosA)));
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
    public static double eps = 1e-12;
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
        if (!(obj instanceof Point2D other))
            return false;
        return Math.abs(x - other.x) < eps && Math.abs(y - other.y) < eps;
        // return Double.compare(x, other.x) == 0 && Double.compare(y, other.y) == 0;
    }

    public double distance(Point2D o) {
        // return Math.hypot(x - o.x, y - o.y);
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
        // return distance(new Line2D(x.getY() - y.getY(), y.getX() - x.getX(),
        // x.cross(y)));
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

    public double ccw(Point2D o1, Point2D o2) {
        double c = cross(o1, o2);
        return c > eps ? 1 : c < -eps ? -1 : 0;
    }

    public int half() {
        if (y > eps || (Math.abs(y) <= eps && x > eps)) return 1;
        return -1;
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

    public double fastMinAngle(List<Point2D> points) {
        if (points.size() < 2) return 0; 

        List<Point2D> sortedPoints = new ArrayList<>(points);
        sortedPoints.sort((p1, p2) -> {
            Point2D v1 = new Point2D(p1.getX() - this.x, p1.getY() - this.y);
            Point2D v2 = new Point2D(p2.getX() - this.x, p2.getY() - this.y);
            if (v1.half() != v2.half()) {
                return Integer.compare(v2.half(), v1.half()); 
            }
            double cr = v1.cross(new Point2D(0, 0), v2);
            if (Math.abs(cr) > eps) return cr > 0 ? -1 : 1;
            double d1 = v1.distance(new Point2D(0,0));
            double d2 = v2.distance(new Point2D(0,0));
            return Double.compare(d1, d2);
        });

        double min = Double.MAX_VALUE;
        for (int i = 0; i < sortedPoints.size(); i++) {
            Point2D p1 = sortedPoints.get(i);
            Point2D p2 = sortedPoints.get(i + 1 < sortedPoints.size() ? i + 1 : 0); 
            
            double angle = Geometry.angle(this.x, this.y, p1.getX(), p1.getY(), p2.getX(), p2.getY());
            
            if (angle > eps && angle < min) {
                min = angle;
            }
        }
        
        return min == Double.MAX_VALUE ? 0 : min;
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
            // if (Math.abs(angle.get(id.get(i + 1)) - angle.get(id.get(i))) < 1e-15) {
            if (Math.abs(angle.get(id.get(i + 1)) - angle.get(id.get(i))) < eps) {
                cnt++;
            } else {
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
        // System.err.println("vx = " + vx + " vy = " + vy + " minX = " + minX + " minY
        // = " + minY + " maxX = " + maxX + " maxY = " + maxY);
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
        @SuppressWarnings("unused")
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
        if (Math.abs(d) < Point2D.eps) {
            if (Math.abs(b * line.c - line.b * c) < Point2D.eps && Math.abs(a * line.c - line.a * c) < Point2D.eps) {
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

    public boolean fastOnSegment(Point2D p) {
        if (Math.abs(p.cross(this.x, this.y)) > Point2D.eps) return false;
        
        return  p.getX() >= Math.min(x.getX(), y.getX()) - Point2D.eps && p.getX() <= Math.max(x.getX(), y.getX()) + Point2D.eps &&
                p.getY() >= Math.min(x.getY(), y.getY()) - Point2D.eps && p.getY() <= Math.max(x.getY(), y.getY()) + Point2D.eps;
    }

    public boolean fastIntersect(Segment2D seg) {
        if (this.x.equals(seg.getX()) || this.x.equals(seg.getY()) || this.y.equals(seg.getX()) || this.y.equals(seg.getY())) {
            if (this.fastOnSegment(seg.getX()) && !this.x.equals(seg.getX()) && !this.y.equals(seg.getX())) return true;
            if (this.fastOnSegment(seg.getY()) && !this.x.equals(seg.getY()) && !this.y.equals(seg.getY())) return true;
            if (seg.fastOnSegment(this.x) && !seg.getX().equals(this.x) && !seg.getY().equals(this.x)) return true;
            if (seg.fastOnSegment(this.y) && !seg.getX().equals(this.y) && !seg.getY().equals(this.y)) return true;
            return false;
        }

        if (Math.max(x.getX(), y.getX()) + Point2D.eps < Math.min(seg.getX().getX(), seg.getY().getX()) || Math.max(seg.getX().getX(), seg.getY().getX()) + Point2D.eps < Math.min(x.getX(), y.getX()) ||
            Math.max(x.getY(), y.getY()) + Point2D.eps < Math.min(seg.getX().getY(), seg.getY().getY()) || Math.max(seg.getX().getY(), seg.getY().getY()) + Point2D.eps < Math.min(x.getY(), y.getY())) {
            return false;
        }
        
        double d1 = x.cross(y, seg.getX());
        double d2 = x.cross(y, seg.getY());
        double d3 = seg.getX().cross(seg.getY(), x);
        double d4 = seg.getX().cross(seg.getY(), y);
        
        if (((d1 > Point2D.eps && d2 < -Point2D.eps) || (d1 < -Point2D.eps && d2 > Point2D.eps)) && ((d3 > Point2D.eps && d4 < -Point2D.eps) || (d3 < -Point2D.eps && d4 > Point2D.eps))) {
            return true;
        }

        return (Math.abs(d1) <= Point2D.eps && fastOnSegment(seg.getX()) || Math.abs(d2) <= Point2D.eps && fastOnSegment(seg.getY()) ||
                Math.abs(d3) <= Point2D.eps && seg.fastOnSegment(this.x) || Math.abs(d4) <= Point2D.eps && seg.fastOnSegment(this.y));
    }

    public Point2D intersect(Segment2D seg) {
        // if (x.getX() == seg.getX().getX() && x.getY() == seg.getX().getY() &&
        // y.getX() == seg.getY().getX() && y.getY() == seg.getY().getY() ||
        // x.getX() == seg.getY().getX() && x.getY() == seg.getY().getY() && y.getX() ==
        // seg.getX().getX() && y.getY() == seg.getX().getY()) {
        // if (x.equals(seg.x) && y.equals(seg.y) || x.equals(seg.y) && y.equals(seg.x))
        // {
        // return null;
        // }
        Line2D l1 = new Line2D(x.getY() - y.getY(), y.getX() - x.getX(), x.cross(y));
        Line2D l2 = new Line2D(seg.x.getY() - seg.y.getY(), seg.y.getX() - seg.x.getX(), seg.x.cross(seg.y));
        // if (x.getX() == 17 && x.getY() == 10 && y.getX() == 11 && y.getY() == 15 &&
        // seg.getX().getX() == 13 && seg.getX().getY() == 16 && seg.getY().getX() == 10
        // && seg.getY().getY() == 6) {
        // System.err.printf("(%f %f %f) (%f %f %f)\n", l1.getA(), l1.getB(), l1.getC(),
        // l2.getA(), l2.getB(), l2.getC());
        // }

        Point2D p = l1.intersect(l2);
        if (p == null)
            return null;
        if (p == Point2D.infPoint) {
            if (seg.fastOnSegment(this.x)) return this.x;
            if (seg.fastOnSegment(this.y)) return this.y;
            if (this.fastOnSegment(seg.x)) return seg.x;
            if (this.fastOnSegment(seg.y)) return seg.y;
            return null;
        }

        if (this.fastOnSegment(p) && seg.fastOnSegment(p)) {
            return p;
        }
        
        return null;
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
    public Kattio() {
        this(System.in, System.out);
    }

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
        // System.err.println("Reading from file: " + inputFile);
        try {
            r = new BufferedReader(new FileReader(inputFile));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }

    public Kattio(String inputFile, String outputFile) throws IOException {
        super(outputFile != null ? (new BufferedOutputStream(new FileOutputStream(outputFile))) : System.out);
        try {
            r = inputFile != null ? new BufferedReader(new FileReader(inputFile)) : null;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }

    public String next() {
        try {
            while (st == null || !st.hasMoreTokens())
                st = new StringTokenizer(r.readLine());
            return st.nextToken();
        } catch (Exception ignored) {
        }
        return null;
    }

    public int nextInt() {
        return Integer.parseInt(next());
    }

    public double nextDouble() {
        return Double.parseDouble(next());
    }

    public long nextLong() {
        return Long.parseLong(next());
    }
}

interface Function {
    public double evaluation();

    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY);

    public double evaluateTwoNodesMove(VarNodePosition node1, int newX1, int newY1, VarNodePosition node2, int newX2, int newY2);

    public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY);

    public void initPropagation();
}

class VarIntLS {
    int id;
    Set<Integer> domain;
    int _value;

    public VarIntLS(int id, Set<Integer> domain) {
        this.id = id;
        this.domain = domain;
    }

    public int value() {
        return _value;
    }

}

class NodePosition {
    int id;
    int x_pos;// x-coordinate
    int y_pos;// y-coordinate

    public NodePosition(int id, int x_pos, int y_pos) {
        this.id = id;
        this.x_pos = x_pos;
        this.y_pos = y_pos;
    }

    public int id() {
        return id;
    }

    public int x() {
        return x_pos;
    }

    public int y() {
        return y_pos;
    }

    public void assign(int id, int newX, int newY) {
        this.id = id;
        x_pos = newX;
        y_pos = newY;
    }

    public void assign(int newX, int newY) {
        x_pos = newX;
        y_pos = newY;
    }
}

class VarNodePosition {
    int id;
    int x_pos;// x-coordinate
    int y_pos;// y-coordinate
    Set<Integer> DX;// domain for x-coordinate
    Set<Integer> DY;// domain for y-coordinate

    public VarNodePosition(int id, Set<Integer> DX, Set<Integer> DY) {
        this.id = id;
        this.DX = DX;
        this.DY = DY;
        this.x_pos = -1;
        this.y_pos = -1;
    }

    public int x() {
        return x_pos;
    }

    public int y() {
        return y_pos;
    }

    public void assign(int newX, int newY) {
        x_pos = newX;
        y_pos = newY;
    }

    public NodePosition getNodePosition() {
        return new NodePosition(id, x_pos, y_pos);
    }
}

class Node {
    int id;

    public Node(int id) {
        this.id = id;
    }
}

class Edge {
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

    public Node getRemaining(Node node) {
        if (node == fromNode)
            return toNode;
        if (node == toNode)
            return fromNode;
        return null;
    }
}

class Graph {
    private final List<Node> nodes;
    private final Map<Integer, Node> nodeMap;
    private final Map<Node, List<Edge>> A; // A[v] is the list of adjacent edges of v

    public Graph(List<Node> nodes) {
        this.nodes = nodes;
        nodeMap = new HashMap<>();
        for (Node n : nodes)
            nodeMap.put(n.id, n);
        A = new HashMap<>();
        for (Node n : nodes)
            A.put(n, new ArrayList<>());
    }

    public Graph(List<Node> nodes, Map<Node, List<Edge>> a) {
        this.nodes = nodes;
        nodeMap = new HashMap<>();
        for (Node n : nodes)
            nodeMap.put(n.id, n);
        A = a;
    }

    private int edgeId = 0;

    public Edge addEdge(Node u, Node v) {
        Edge e = new Edge(edgeId++, u, v);
        A.get(u).add(e);
        return e;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges(Node u) {
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
            if (next == null || next == prev || visited.contains(edge))
                continue;
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
        // System.err.println("Edge: " + edge.fromNode.id + " -> " + edge.toNode.id);
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
        // Node current = stack.pop();
        // LinkedNode<Edge> edge = adj.get(current.id).getFirst();
        // while (edge != null) {
        // Node next = edge.value.getRemaining(current);
        // if (next != null && !visited.contains(next)) {
        // visited.add(next);
        // stack.push(next);
        // result.add(edge.value);
        // }
        // edge = edge.next;
        // }
        // }
        // for (DoubleLinkedList<Edge> edges : adj) {
        // for (LinkedNode<Edge> edge = edges.getFirst(); edge != null; edge =
        // edge.next) {
        // System.err.println("Edge: " + edge.value.fromNode.id + " -> " +
        // edge.value.toNode.id);
        // }
        // }

        result = new ArrayList<>();
        visited = new HashSet<>();
        dfs(start, start, adj);
        // System.err.println(result.size());
        // for (Edge edge : result) {
        // System.err.println("Edge: " + edge.fromNode.id + " -> " + edge.toNode.id);
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
        for (int i = 0; i < n; i++) {
            if (distances.get(i) == Integer.MAX_VALUE) {
                queue.add(getNodeI(i));
                distances.set(i, 0);
                while (!queue.isEmpty()) {
                    Node current = queue.poll();
                    LinkedNode<Edge> edge = adj.get(current.id).getFirst();
                    // System.err.print(current.id + ": ");
                    while (edge != null) {
                        Node next = edge.value.getRemaining(current);
                        // System.err.print("  " + next.id);
                        if (next != null && distances.get(next.id) == Integer.MAX_VALUE) {
                            distances.set(next.id, distances.get(current.id) + 1);
                            queue.add(next);
                        }
                        edge = edge.next;
                    }
                    // System.err.println();
                }
            }
        }
        // queue.add(start);
        // distances.set(start.id, 0);
        // while (!queue.isEmpty()) {
        //     Node current = queue.poll();
        //     LinkedNode<Edge> edge = adj.get(current.id).getFirst();
        //     // System.err.print(current.id + ": ");
        //     while (edge != null) {
        //         Node next = edge.value.getRemaining(current);
        //         // System.err.print("  " + next.id);
        //         if (next != null && distances.get(next.id) == Integer.MAX_VALUE) {
        //             distances.set(next.id, distances.get(current.id) + 1);
        //             queue.add(next);
        //         }
        //         edge = edge.next;
        //     }
        //     // System.err.println();
        // }

        return distances;
    }

}

class CBLSGPModel {
    List<VarNodePosition> varNodePositions;
    List<Function> F;

    // additional data structures defined here to efficiently perform the move

    public CBLSGPModel() {
        varNodePositions = new ArrayList<>();
        F = new ArrayList<>();
    }

    public VarNodePosition getVarNode(int id) {
        return varNodePositions.get(id);
    }

    public void addVarNode(VarNodePosition varNodePosition) {
        varNodePositions.add(varNodePosition);
    }

    public void addFunction(Function f) {
        F.add(f);
    }

    public void move(VarNodePosition varNode, int x, int y) {
        // perform propagation to update functions in F
        varNode.assign(x, y);
        // for (Function f : F) {
        // f.propagateOneNodeMove(varNode, x, y);
        // }
    }

    public List<NodePosition> getNodePositions() {
        List<NodePosition> nodePositions = new ArrayList<>();
        for (VarNodePosition v : varNodePositions) {
            nodePositions.add(v.getNodePosition());
        }
        return nodePositions;
    }

    public void setNodePositionsValue(List<NodePosition> nodePositions) {
        for (int i = 0; i < varNodePositions.size(); i++) {
            // varNodePositions.get(i).assign(nodePositions.get(i).x(),
            // nodePositions.get(i).y());
            VarNodePosition pos = varNodePositions.get(i);
            nodePositions.get(i).assign(pos.id, pos.x(), pos.y());
        }
    }

    public void close() {
        for (Function f : F)
            f.initPropagation();
    }
}

class DoubleCompare implements Comparator<Double> {
    @Override
    public int compare(Double a, Double b) {
        if (Math.abs(a - b) < Point2D.eps)
            return 0;
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
        // System.err.println(" " + value);
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

    // TreeMap<T, Integer> map() {
    // Object map() {
    // return map;
    // }
    Set<Map.Entry<T, Integer>> entrySet() {
        return map.entrySet();
    }
}


class SumFunction implements Function {
    List<Function> functions;
    double cache = -1;

    SumFunction(List<Function> functions) {
        this.functions = functions;
    }

    @Override
    public double evaluation() {
        if (cache >= 0)
            return cache;
        double sum = 0;
        for (Function f : functions)
            sum += f.evaluation();
        return cache = sum;
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        double sum = 0;
        for (Function f : functions) {
            double val = f.evaluateOneNodeMove(varNodePosition, newX, newY);
            sum += val;
        }
        return sum;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        for (Function f : functions) {
            f.propagateOneNodeMove(varNodePosition, newX, newY);
        }
        cache = -1;
    }

    @Override
    public double evaluateTwoNodesMove(VarNodePosition node1, int newX1, int newY1, VarNodePosition node2, int newX2, int newY2) {
        double sum = 0;
        for (Function f : functions) {
            sum += f.evaluateTwoNodesMove(node1, newX1, newY1, node2, newX2, newY2);
        }
        return sum;
    }

    @Override
    public void initPropagation() {
        for (Function f : functions)
            f.initPropagation();
    }
}   
class Angle implements Function {
    VarNodePosition w;
    VarNodePosition u;
    VarNodePosition v;
    double cache = -1;

    Angle(VarNodePosition w, VarNodePosition u, VarNodePosition v) {
        this.w = w;
        this.u = u;
        this.v = v;
    }

    @Override
    public double evaluation() {
        if (cache >= 0)
            return cache;
        return cache = Geometry.angle(w.x(), w.y(), u.x(), u.y(), v.x(), v.y());
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (varNodePosition == w || varNodePosition == u || varNodePosition == v) {
            double wx = (varNodePosition == w) ? newX : w.x();
            double wy = (varNodePosition == w) ? newY : w.y();
            double ux = (varNodePosition == u) ? newX : u.x();
            double uy = (varNodePosition == u) ? newY : u.y();
            double vx = (varNodePosition == v) ? newX : v.x();
            double vy = (varNodePosition == v) ? newY : v.y();
            return Geometry.angle(wx, wy, ux, uy, vx, vy);
        }
        return -1;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (varNodePosition == w || varNodePosition == u || varNodePosition == v) {
            double wx = (varNodePosition == w) ? newX : w.x();
            double wy = (varNodePosition == w) ? newY : w.y();
            double ux = (varNodePosition == u) ? newX : u.x();
            double uy = (varNodePosition == u) ? newY : u.y();
            double vx = (varNodePosition == v) ? newX : v.x();
            double vy = (varNodePosition == v) ? newY : v.y();
            cache = Geometry.angle(wx, wy, ux, uy, vx, vy);
        } else
            cache = -1;
    }

    @Override
    public double evaluateTwoNodesMove(VarNodePosition node1, int newX1, int newY1, VarNodePosition node2, int newX2, int newY2) {
        double wx = (node1 == w) ? newX1 : (node2 == w) ? newX2 : w.x();
        double wy = (node1 == w) ? newY1 : (node2 == w) ? newY2 : w.y();
        double ux = (node1 == u) ? newX1 : (node2 == u) ? newX2 : u.x();
        double uy = (node1 == u) ? newY1 : (node2 == u) ? newY2 : u.y();
        double vx = (node1 == v) ? newX1 : (node2 == v) ? newX2 : v.x();
        double vy = (node1 == v) ? newY1 : (node2 == v) ? newY2 : v.y();
        return Geometry.angle(wx, wy, ux, uy, vx, vy);
    }

    @Override
    public void initPropagation() {
    }
}

class Length implements Function {
    VarNodePosition u;
    VarNodePosition v;
    double cache = -1;

    Length(VarNodePosition u, VarNodePosition v) {
        this.u = u;
        this.v = v;
    }

    @Override
    public double evaluation() {
        if (cache >= 0)
            return cache;
        return cache = new Point2D(u.x(), u.y()).distance(new Point2D(v.x(), v.y()));
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (varNodePosition == u) {
            return new Point2D(newX, newY).distance(new Point2D(v.x(), v.y()));
        } else if (varNodePosition == v) {
            return new Point2D(newX, newY).distance(new Point2D(u.x(), u.y()));
        }
        return -1;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (varNodePosition == u) {
            cache = new Point2D(newX, newY).distance(new Point2D(v.x(), v.y()));
        } else if (varNodePosition == v) {
            cache = new Point2D(newX, newY).distance(new Point2D(u.x(), u.y()));
        } else
            cache = -1;

    }

    @Override
    public double evaluateTwoNodesMove(VarNodePosition node1, int newX1, int newY1, VarNodePosition node2, int newX2, int newY2) {
        double ux = (node1 == u) ? newX1 : (node2 == u) ? newX2 : u.x();
        double uy = (node1 == u) ? newY1 : (node2 == u) ? newY2 : u.y();
        double vx = (node1 == v) ? newX1 : (node2 == v) ? newX2 : v.x();
        double vy = (node1 == v) ? newY1 : (node2 == v) ? newY2 : v.y();
        return new Point2D(ux, uy).distance(new Point2D(vx, vy));
    }

    @Override

    public void initPropagation() {

    }
}

class Distance implements Function {
    VarNodePosition u;
    VarNodePosition v;
    VarNodePosition w;
    double cache = -1;

    Distance(VarNodePosition w, VarNodePosition u, VarNodePosition v) {
        this.u = u;
        this.v = v;
        this.w = w;
    }

    @Override
    public double evaluation() {
        if (cache >= 0)
            return cache;
        return cache = new Point2D(w.x(), w.y())
                .distance(new Segment2D(new Point2D(u.x(), u.y()), new Point2D(v.x(), v.y())));
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (varNodePosition == w || varNodePosition == u || varNodePosition == v) {
            Point2D pw = (varNodePosition == w) ? new Point2D(newX, newY) : new Point2D(w.x(), w.y());
            Point2D pu = (varNodePosition == u) ? new Point2D(newX, newY) : new Point2D(u.x(), u.y());
            Point2D pv = (varNodePosition == v) ? new Point2D(newX, newY) : new Point2D(v.x(), v.y());
            return pw.distance(new Segment2D(pu, pv));
        }
        return -1;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (varNodePosition == w || varNodePosition == u || varNodePosition == v) {
            Point2D pw = (varNodePosition == w) ? new Point2D(newX, newY) : new Point2D(w.x(), w.y());
            Point2D pu = (varNodePosition == u) ? new Point2D(newX, newY) : new Point2D(u.x(), u.y());
            Point2D pv = (varNodePosition == v) ? new Point2D(newX, newY) : new Point2D(v.x(), v.y());
            cache = pw.distance(new Segment2D(pu, pv));
        } else
            cache = -1;
    }

    @Override
    public double evaluateTwoNodesMove(VarNodePosition node1, int newX1, int newY1, VarNodePosition node2, int newX2, int newY2) {
        Point2D pw = (node1 == w) ? new Point2D(newX1, newY1) : (node2 == w) ? new Point2D(newX2, newY2) : new Point2D(w.x(), w.y());
        Point2D pu = (node1 == u) ? new Point2D(newX1, newY1) : (node2 == u) ? new Point2D(newX2, newY2) : new Point2D(u.x(), u.y());
        Point2D pv = (node1 == v) ? new Point2D(newX1, newY1) : (node2 == v) ? new Point2D(newX2, newY2) : new Point2D(v.x(), v.y());
        return pw.distance(new Segment2D(pu, pv));
    }

    @Override
    public void initPropagation() {
    }
}

class CrossEdge implements Function {
    VarNodePosition u;
    VarNodePosition v;
    VarNodePosition w;
    VarNodePosition z;
    double cache = -1;

    CrossEdge(VarNodePosition w, VarNodePosition z, VarNodePosition u, VarNodePosition v) {
        this.u = u;
        this.v = v;
        this.w = w;
        this.z = z;
    }

    @Override
    public double evaluation() {
        if (cache >= 0)
            return cache;
        Segment2D s1 = new Segment2D(new Point2D(w.x(), w.y()), new Point2D(z.x(), z.y()));
        Segment2D s2 = new Segment2D(new Point2D(u.x(), u.y()), new Point2D(v.x(), v.y()));
        return cache = s1.fastIntersect(s2) ? 1.0 : 0.0;
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (varNodePosition == w || varNodePosition == z || varNodePosition == u || varNodePosition == v) {
            Point2D pw = (varNodePosition == w) ? new Point2D(newX, newY) : new Point2D(w.x(), w.y());
            Point2D pz = (varNodePosition == z) ? new Point2D(newX, newY) : new Point2D(z.x(), z.y());
            Point2D pu = (varNodePosition == u) ? new Point2D(newX, newY) : new Point2D(u.x(), u.y());
            Point2D pv = (varNodePosition == v) ? new Point2D(newX, newY) : new Point2D(v.x(), v.y());
            Segment2D s1 = new Segment2D(pw, pz);
            Segment2D s2 = new Segment2D(pu, pv);
            return s1.fastIntersect(s2) ? 1.0 : 0.0;
        }
        return -1;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (varNodePosition == w || varNodePosition == z || varNodePosition == u || varNodePosition == v) {
            Point2D pw = (varNodePosition == w) ? new Point2D(newX, newY) : new Point2D(w.x(), w.y());
            Point2D pz = (varNodePosition == z) ? new Point2D(newX, newY) : new Point2D(z.x(), z.y());
            Point2D pu = (varNodePosition == u) ? new Point2D(newX, newY) : new Point2D(u.x(), u.y());
            Point2D pv = (varNodePosition == v) ? new Point2D(newX, newY) : new Point2D(v.x(), v.y());
            Segment2D s1 = new Segment2D(pw, pz);
            Segment2D s2 = new Segment2D(pu, pv);
            cache = s1.fastIntersect(s2) ? 1.0 : 0.0;
        } else
            cache = -1;
    }

    @Override
    public double evaluateTwoNodesMove(VarNodePosition node1, int newX1, int newY1, VarNodePosition node2, int newX2, int newY2) {
        Point2D pw = (node1 == w) ? new Point2D(newX1, newY1) : (node2 == w) ? new Point2D(newX2, newY2) : new Point2D(w.x(), w.y());
        Point2D pz = (node1 == z) ? new Point2D(newX1, newY1) : (node2 == z) ? new Point2D(newX2, newY2) : new Point2D(z.x(), z.y());
        Point2D pu = (node1 == u) ? new Point2D(newX1, newY1) : (node2 == u) ? new Point2D(newX2, newY2) : new Point2D(u.x(), u.y());
        Point2D pv = (node1 == v) ? new Point2D(newX1, newY1) : (node2 == v) ? new Point2D(newX2, newY2) : new Point2D(v.x(), v.y());
        Segment2D s1 = new Segment2D(pw, pz);
        Segment2D s2 = new Segment2D(pu, pv);
        return s1.fastIntersect(s2) ? 1.0 : 0.0;
    }

    @Override
    public void initPropagation() {
    }
}

// class MinDistanceEdge implements Function {
//     Graph g;
//     List<List<Edge>> adj;
//     Map<Node, VarNodePosition> positions;
//     Map<Integer, Length> lengthFuncs;
//     TreeMultiset<Double> pq;

//     @Override
//     public String toString() {
//         return "minEdgeLength=" + evaluation() + " ";
//     }

//     private int encode(Edge e) {
//         int u = Math.min(e.fromNode.id, e.toNode.id);
//         int v = Math.max(e.fromNode.id, e.toNode.id);
//         return u * g.getNodes().size() + v;
//     }

//     public MinDistanceEdge(Graph g, Map<Node, VarNodePosition> positions) {
//         this.g = g;
//         this.positions = positions;
//         lengthFuncs = new HashMap<>();
//         pq = new TreeMultiset<>(new DoubleCompare());
//         adj = new ArrayList<>();
//         Map<Integer, Boolean> marked = new HashMap<>();
//         for (Node node : g.getNodes()) {
//             adj.add(new ArrayList<>());
//             for (Edge e : g.getEdges(node)) {
//                 int eId = encode(e);
//                 if (marked.containsKey(eId))
//                     continue;
//                 marked.put(eId, true);
//                 adj.get(node.id).add(e);
//             }
//             marked.clear();
//         }

//         for (Node node : g.getNodes()) {
//             for (Edge e : adj.get(node.id)) {
//                 int eId = encode(e);
//                 if (lengthFuncs.containsKey(eId))
//                     continue;
//                 Node u = e.fromNode, v = e.toNode;
//                 VarNodePosition posU = positions.get(u), posV = positions.get(v);
//                 Length lenFunc = new Length(posU, posV);
//                 lengthFuncs.put(eId, lenFunc);
//                 pq.add(lenFunc.evaluation());
//             }
//         }
//     }

//     @Override
//     public double evaluation() {
//         if (pq.isEmpty())
//             return Double.POSITIVE_INFINITY;
//         return pq.first();
//     }

//     @Override
//     public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
//         int oldX = varNodePosition.x(), oldY = varNodePosition.y();
//         if (oldX == newX && oldY == newY) {
//             return evaluation();
//         }
//         if (newX == -1 && newY == -1) {
//             Node node = g.getNode(varNodePosition.id);
//             TreeMultiset<Double> visited = new TreeMultiset<>(new DoubleCompare());
//             for (Edge e : adj.get(node.id)) {
//                 Length lenFunc = lengthFuncs.get(encode(e));
//                 if (lenFunc != null) {
//                     visited.add(lenFunc.evaluation());
//                 }
//             }
//             for (Double d : pq) {
//                 if (!visited.remove(d)) {
//                     return d;
//                 }
//             }
//             return Double.POSITIVE_INFINITY;
//         }
//         double min = Double.POSITIVE_INFINITY;
//         Node node = g.getNode(varNodePosition.id);
//         TreeMultiset<Double> visited = new TreeMultiset<>(new DoubleCompare());
//         for (Edge e : adj.get(node.id)) {
//             Length lenFunc = lengthFuncs.get(encode(e));
//             if (lenFunc != null) {
//                 visited.add(lenFunc.evaluation());
//                 double d = lenFunc.evaluateOneNodeMove(varNodePosition, newX, newY);
//                 min = Math.min(min, d);
//             }
//         }

//         for (Double d : pq) {
//             if (d > min - Point2D.eps) {
//                 return min;
//             }
//             if (!visited.remove(d)) {
//                 return d;
//             }
//         }
//         return min;
//     }

//     @Override
//     public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
//         if (newX == -1 && newY == -1)
//             return;
//         int oldX = varNodePosition.x(), oldY = varNodePosition.y();
//         if (oldX == newX && oldY == newY)
//             return;
//         Node node = g.getNode(varNodePosition.id);

//         for (Edge e : adj.get(node.id)) {
//             Length lenFunc = lengthFuncs.get(encode(e));
//             if (lenFunc == null)
//                 continue;
//             double oldDistance = lenFunc.evaluation();

//             lenFunc.propagateOneNodeMove(varNodePosition, newX, newY);
//             double d = lenFunc.evaluation();

//             if (oldDistance > -1) {
//                 if (Math.abs(oldDistance - d) < Point2D.eps) {
//                     continue;
//                 }
//                 pq.remove(oldDistance);
//             }
//             pq.add(d);
//         }
//     }

//     @Override
//     public void initPropagation() {
//     }
// }
class MinDistanceEdge implements Function{
//    double minDistance;
    Graph g;
    List<List<Edge>> adj;
    Map<Node, VarNodePosition> positions;
    Map<Integer, Double> distances;
    TreeMultiset<Double> pq;

    @Override
    public String toString() {
        return "minEdgeLength=" + evaluation() + " ";
    }

    private int encode(Edge e) {
        int u = Math.min(e.fromNode.id, e.toNode.id);
        int v = Math.max(e.fromNode.id, e.toNode.id);
        return u * g.getNodes().size() + v;
    }
    public MinDistanceEdge(Graph g, Map<Node, VarNodePosition> positions){
        this.g = g;
        this.positions = positions;
        distances = new HashMap<>();
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
                pq.add(d);
            }
        }
    }

    @Override
    public double evaluation() {
        if (pq.isEmpty()) return Double.POSITIVE_INFINITY;
        return pq.first();
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        int oldX = varNodePosition.x(), oldY = varNodePosition.y();
        if (oldX == newX && oldY == newY) {
            return evaluation();
        }
        if (newX == -1 && newY == -1) {
            Node node = g.getNode(varNodePosition.id);
            TreeMultiset<Double> visited = new TreeMultiset<>(new DoubleCompare());
            for (Edge e : adj.get(node.id)) {
                Node u = e.getRemaining(node);
                VarNodePosition posU = positions.get(u);
                if (posU.x() == -1 || posU.y() == -1) continue;
                double d = distances.getOrDefault(encode(e), -1.);
                if (d > -1.) {
                    visited.add(d);
                }
            }
            for (Double d : pq) {
                if (!visited.remove(d)) {
                    return d;
                }
            }
            return Double.POSITIVE_INFINITY;
        }
        double min = Double.POSITIVE_INFINITY;
        Node node = g.getNode(varNodePosition.id);
        TreeMultiset<Double> visited = new TreeMultiset<>(new DoubleCompare());
        for (Edge e : adj.get(node.id)) {
            Node u = e.getRemaining(node);
            VarNodePosition posU = positions.get(u);
            if (posU.x() == -1 || posU.y() == -1) continue;
            double oldD = distances.getOrDefault(encode(e), -1.);
            if (oldD > -1.) {
                visited.add(oldD);
            }
            double d = Geometry.distance(posU.x(), posU.y(), newX, newY);
            min = Math.min(min, d);
        }

        for (Double d : pq) {
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
                pq.remove(oldDistance);
            }
            distances.put(eId, d);
            pq.add(d);
        }
    }

    @Override
    public double evaluateTwoNodesMove(VarNodePosition node1, int newX1, int newY1, VarNodePosition node2, int newX2, int newY2) {
        int oldX1 = node1.x(), oldY1 = node1.y();
        int oldX2 = node2.x(), oldY2 = node2.y();
        if (oldX1 == newX1 && oldY1 == newY1 && oldX2 == newX2 && oldY2 == newY2) {
            return evaluation();
        }

        if (newX2 == -1 && newY2 == -1) {
            VarNodePosition temp = node1;
            node1 = node2;
            node2 = temp;
            newX2 = newX1;
            newY2 = newY1;
            newX1 = -1;
            newY1 = -1; 
        }

        if (newX1 == -1 && newY1 == -1) {
            if (newX2 == -1 && newY2 == -1) {
                Node node = g.getNode(node1.id);
                TreeMultiset<Double> visited = new TreeMultiset<>(new DoubleCompare());
                if (oldX1 != -1 && oldY1 != -1) {
                    for (Edge e : adj.get(node.id)) {
                        Node u = e.getRemaining(node);
                        VarNodePosition posU = positions.get(u);
                        if (posU.x() == -1 || posU.y() == -1) continue;
                        double d = distances.getOrDefault(encode(e), -1.);
                        if (d > -1.) {
                            visited.add(d);
                        }
                    }
                }
                if (oldX2 != -1 && oldY2 != -1) {
                    node = g.getNode(node2.id);
                    for (Edge e : adj.get(node.id)) {
                        Node u = e.getRemaining(node);
                        VarNodePosition posU = positions.get(u);
                        if (posU.x() == -1 || posU.y() == -1) continue;
                        if (posU == node1) continue;
                        double d = distances.getOrDefault(encode(e), -1.);
                        if (d > -1.) {
                            visited.add(d);
                        }
                    }
                }
                for (Double d : pq) {
                    if (!visited.remove(d)) {
                        return d;
                    }
                }
                return Double.POSITIVE_INFINITY;
            }

            Node node = g.getNode(node1.id);
            Double mn = Double.POSITIVE_INFINITY;
            TreeMultiset<Double> visited = new TreeMultiset<>(new DoubleCompare());
            for (Edge e : adj.get(node.id)) {
                Node u = e.getRemaining(node);
                VarNodePosition posU = positions.get(u);
                if (posU.x() == -1 || posU.y() == -1) continue;
                double d = distances.getOrDefault(encode(e), -1.);
                if (d > -1.) {
                    visited.add(d);
                }
                mn = Math.min(mn, Geometry.distance(posU.x(), posU.y(), newX1, newY1));
            }
            node = g.getNode(node2.id);
            for (Edge e : adj.get(node.id)) {
                Node u = e.getRemaining(node);
                VarNodePosition posU = positions.get(u);
                if (posU.x() == -1 || posU.y() == -1) continue;
                if (posU == node1) continue;
                double d = distances.getOrDefault(encode(e), -1.);
                if (d > -1.) {
                    visited.add(d);
                }
                mn = Math.min(mn, Geometry.distance(posU.x(), posU.y(), newX2, newY2));
            }
            for (Double d : pq) {
                if (!visited.remove(d)) {
                    return Math.min(d, mn);
                }
            }
            return mn;
        }

        double min = Double.POSITIVE_INFINITY;
        Node node = g.getNode(node1.id);
        TreeMultiset<Double> visited = new TreeMultiset<>(new DoubleCompare());
        for (Edge e : adj.get(node.id)) {
            Node u = e.getRemaining(node);
            VarNodePosition posU = positions.get(u);
            if (posU.x() == -1 || posU.y() == -1) continue;
            double oldD = distances.getOrDefault(encode(e), -1.);
            if (oldD > -1.) {
                visited.add(oldD);
            }
            double d = Geometry.distance(posU.x(), posU.y(), newX1, newY1);
            min = Math.min(min, d);
        }
        node = g.getNode(node2.id);
        for (Edge e : adj.get(node.id)) {
            Node u = e.getRemaining(node);
            VarNodePosition posU = positions.get(u);
            if (posU.x() == -1 || posU.y() == -1) continue;
            if (posU == node1) continue;
            double oldD = distances.getOrDefault(encode(e), -1.);
            if (oldD > -1.) {
                visited.add(oldD);
            }
            double d = Geometry.distance(posU.x(), posU.y(), newX2, newY2);
            min = Math.min(min, d);
        }

        for (Double d : pq) {
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
    public void initPropagation() {

    }
}

// class MinAngle implements Function {
//     Graph g;
//     List<List<Edge>> adj;
//     Map<Node, VarNodePosition> positions;
//     Map<Node, TreeSet<NodeAngle>> nodeNeighbors = new HashMap<>();
//     TreeMultiset<Double> allAngles = new TreeMultiset<>(new DoubleCompare());
//     Map<Integer, Map<Integer, NodeAngle>> nodeAngleMap = new HashMap<>();
//     double minAngleValue = Double.POSITIVE_INFINITY;

//     @Override
//     public String toString() {
//         return "minAngle=" + minAngleValue + " ";
//     }

//     private static class NodeAngle implements Comparable<NodeAngle> {
//         Node node;
//         double angle;

//         NodeAngle(Node node, double angle) {
//             this.node = node;
//             this.angle = angle;
//         }

//         @Override
//         public int compareTo(NodeAngle o) {
//             if (Math.abs(angle - o.angle) >= Point2D.eps) {
//                 return Double.compare(angle, o.angle);
//             }
//             return Integer.compare(node.id, o.node.id);
//         }

//         @Override
//         public boolean equals(Object o) {
//             if (!(o instanceof NodeAngle na))
//                 return false;
//             return node.id == na.node.id && Math.abs(angle - na.angle) < Point2D.eps;
//         }
//     }

//     public MinAngle(Graph g, Map<Node, VarNodePosition> positions) {
//         this.g = g;
//         this.positions = positions;
//         adj = new ArrayList<>();

//         Map<Integer, Boolean> marked = new HashMap<>();
//         for (Node node : g.getNodes()) {
//             nodeAngleMap.put(node.id, new HashMap<>());
//             nodeNeighbors.put(node, new TreeSet<>());
//             adj.add(new ArrayList<>());
//             for (Edge e : g.getEdges(node)) {
//                 int u = Math.min(e.fromNode.id, e.toNode.id);
//                 int v = Math.max(e.fromNode.id, e.toNode.id);
//                 int eId = u * g.getNodes().size() + v;
//                 if (marked.containsKey(eId))
//                     continue;
//                 marked.put(eId, true);
//                 adj.get(node.id).add(e);
//             }
//             marked.clear();
//         }

//         for (Node node : g.getNodes()) {
//             VarNodePosition nodePos = positions.get(node);
//             updateNodeAngles(node, nodePos.x(), nodePos.y());
//         }

//         if (!allAngles.isEmpty()) {
//             minAngleValue = allAngles.first();
//         }
//     }

//     private void updateNodeAngles(Node node, int newX, int newY) {
//         if (newX == -1 && newY == -1)
//             return;

//         TreeSet<NodeAngle> neighbors = nodeNeighbors.get(node);
//         VarNodePosition nodePos = positions.get(node);

//         if (neighbors.size() > 1) {
//             List<NodeAngle> neighborList = new ArrayList<>(neighbors);
//             for (int i = 0; i < neighborList.size(); i++) {
//                 NodeAngle current = neighborList.get(i);
//                 NodeAngle next = neighborList.get((i + 1) % neighborList.size());
//                 VarNodePosition uPos = positions.get(current.node);
//                 VarNodePosition vPos = positions.get(next.node);
//                 Angle angFunc = new Angle(nodePos, uPos, vPos);
//                 double angleVal = angFunc.evaluation();
//                 allAngles.remove(angleVal);
//             }
//         }

//         neighbors.clear();

//         for (Edge e : adj.get(node.id)) {
//             Node neighbor = e.getRemaining(node);
//             VarNodePosition neighborPos = positions.get(neighbor);

//             if (neighborPos.x() != -1 && neighborPos.y() != -1) {
//                 double angle = Math.atan2(neighborPos.y() - newY, neighborPos.x() - newX);
//                 NodeAngle nodeAngle = new NodeAngle(neighbor, angle);
//                 nodeAngleMap.get(node.id).put(neighbor.id, nodeAngle);
//                 neighbors.add(nodeAngle);
//             }
//         }

//         if (neighbors.size() > 1) {
//             List<NodeAngle> neighborList = new ArrayList<>(neighbors);
//             for (int i = 0; i < neighborList.size(); i++) {
//                 NodeAngle current = neighborList.get(i);
//                 NodeAngle next = neighborList.get((i + 1) % neighborList.size());
//                 VarNodePosition uPos = positions.get(current.node);
//                 VarNodePosition vPos = positions.get(next.node);
//                 Angle angFunc = new Angle(nodePos, uPos, vPos);
//                 double angleVal = angFunc.evaluateOneNodeMove(nodePos, newX, newY);
//                 allAngles.add(angleVal);
//             }
//         }
//     }

//     private void updateNeighborAngle(Node node, Node movedNode, int newX, int newY, boolean persist) {
//         TreeSet<NodeAngle> neighbors = nodeNeighbors.get(node);
//         VarNodePosition nodePos = positions.get(node);
//         VarNodePosition movedNodePos = positions.get(movedNode);

//         NodeAngle oldNodeAngle = null;
//         Map<Integer, NodeAngle> nodeAngles = nodeAngleMap.computeIfAbsent(node.id, k -> new HashMap<>());
//         try {
//             oldNodeAngle = nodeAngles.get(movedNode.id);
//         } catch (Exception e) {
//         }

//         if (oldNodeAngle != null) {
//             if (neighbors.size() > 1) {
//                 NodeAngle prev = neighbors.lower(oldNodeAngle);
//                 if (prev == null)
//                     prev = neighbors.last();

//                 NodeAngle next = neighbors.higher(oldNodeAngle);
//                 if (next == null)
//                     next = neighbors.first();

//                 double prevOldAngle = new Angle(nodePos, positions.get(prev.node), movedNodePos).evaluation();
//                 allAngles.remove(prevOldAngle);
//                 double oldNextAngle = new Angle(nodePos, movedNodePos, positions.get(next.node)).evaluation();
//                 allAngles.remove(oldNextAngle);
//                 if (neighbors.size() > 2) {
//                     double prevNextAngle = new Angle(nodePos, positions.get(prev.node), positions.get(next.node))
//                             .evaluation();
//                     allAngles.add(prevNextAngle);
//                 }
//             }

//             neighbors.remove(oldNodeAngle);
//         }

//         double newGeomAngle = Math.atan2(newY - nodePos.y(), newX - nodePos.x());
//         NodeAngle newNodeAngle = new NodeAngle(movedNode, newGeomAngle);
//         if (persist)
//             nodeAngles.put(movedNode.id, newNodeAngle);
//         neighbors.add(newNodeAngle);

//         if (neighbors.size() > 1) {
//             NodeAngle prev = neighbors.lower(newNodeAngle);
//             if (prev == null)
//                 prev = neighbors.last();

//             NodeAngle next = neighbors.higher(newNodeAngle);
//             if (next == null)
//                 next = neighbors.first();

//             double prevNewAngle = new Angle(nodePos, positions.get(prev.node), movedNodePos)
//                     .evaluateOneNodeMove(movedNodePos, newX, newY);
//             allAngles.add(prevNewAngle);
//             double newNextAngle = new Angle(nodePos, movedNodePos, positions.get(next.node))
//                     .evaluateOneNodeMove(movedNodePos, newX, newY);
//             allAngles.add(newNextAngle);
//             if (neighbors.size() > 2) {
//                 double prevNextAngle = new Angle(nodePos, positions.get(prev.node), positions.get(next.node))
//                         .evaluation();
//                 allAngles.remove(prevNextAngle);
//             }
//         }
//         if (!persist) {
//             neighbors.remove(newNodeAngle);
//             if (oldNodeAngle != null) {
//                 neighbors.add(oldNodeAngle);
//             }
//         }
//     }

//     @Override
//     public double evaluation() {
//         if (allAngles.isEmpty())
//             return Double.POSITIVE_INFINITY;
//         double v = allAngles.first();
//         return v * 100000 - allAngles.countContains(v);
//     }

//     @Override
//     public double evaluateOneNodeMove(VarNodePosition v, int newX, int newY) {
//         Node node = g.getNode(v.id);
//         int oldX = v.x(), oldY = v.y();

//         if (oldX == newX && oldY == newY) {
//             return evaluation();
//         }

//         TreeMultiset<Double> oldAllAngles = new TreeMultiset<>(new DoubleCompare());
//         for (Double val : allAngles)
//             oldAllAngles.add(val);

//         updateNodeAngles(node, newX, newY);
//         for (Edge e : adj.get(node.id)) {
//             Node neighbor = e.getRemaining(node);
//             VarNodePosition neighborPos = positions.get(neighbor);
//             if (neighborPos.x() == -1 && neighborPos.y() == -1)
//                 continue;
//             updateNeighborAngle(neighbor, node, newX, newY, false);
//         }

//         double res = evaluation();

//         updateNodeAngles(node, oldX, oldY);
//         allAngles.clear();
//         for (Double val : oldAllAngles)
//             allAngles.add(val);

//         return res;
//     }

//     @Override
//     public void propagateOneNodeMove(VarNodePosition v, int newX, int newY) {
//         Node node = g.getNode(v.id);
//         int oldX = v.x(), oldY = v.y();

//         if (oldX == newX && oldY == newY || newX == -1 && newY == -1) {
//             return;
//         }

//         updateNodeAngles(node, newX, newY);
//         for (Edge e : adj.get(node.id)) {
//             Node neighbor = e.getRemaining(node);
//             VarNodePosition neighborPos = positions.get(neighbor);
//             if (neighborPos.x() == -1 && neighborPos.y() == -1)
//                 continue;
//             updateNeighborAngle(neighbor, node, newX, newY, true);
//         }
//         if (!allAngles.isEmpty()) {
//             minAngleValue = allAngles.first();
//         }
//     }

//     @Override
//     public void initPropagation() {
//     }
// }
class MinAngle implements Function {
    Graph g;
    List<List<Edge>> adj;
    Map<Node, VarNodePosition> positions;
    Map<Node, TreeSet<NodeAngle>> nodeNeighbors = new HashMap<>();
    TreeMultiset<Double> allAngles = new TreeMultiset<>(new DoubleCompare());
    Map<Integer, Map<Integer, NodeAngle>> nodeAngleMap = new HashMap<>();
    double minAngleValue = Double.POSITIVE_INFINITY;

    @Override
    public String toString() {
        return "minAngle=" + minAngleValue + " ";
    }

    private static class NodeAngle implements Comparable<NodeAngle> {
        Node node;
        int dx, dy;
        
        NodeAngle(Node node, int dx, int dy) {
            this.node = node;
            this.dx = dx;
            this.dy = dy;
        }
        
        private int half() {
            if (dy > 0 || (dy == 0 && dx > 0)) return 1;
            return -1;
        }
        
        @Override
        public int compareTo(NodeAngle o) {
            int h1 = this.half();
            int h2 = o.half();
            if (h1 != h2) return Integer.compare(h2, h1);
            
            long cross = (long) this.dx * o.dy - (long) this.dy * o.dx;
            if (cross != 0) return cross > 0 ? -1 : 1;
            
            long d1 = (long) this.dx * this.dx + (long) this.dy * this.dy;
            long d2 = (long) o.dx * o.dx + (long) o.dy * o.dy;
            if (d1 != d2) return Long.compare(d1, d2);
            
            return Integer.compare(this.node.id, o.node.id);
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof NodeAngle)) return false;
            return this.compareTo((NodeAngle) o) == 0;
        }
    }

    private double getAngleDiff(NodeAngle current, NodeAngle next) {
        long dot = (long) current.dx * next.dx + (long) current.dy * next.dy;
        double d1 = Math.hypot(current.dx, current.dy);
        double d2 = Math.hypot(next.dx, next.dy);
        if (d1 < Point2D.eps || d2 < Point2D.eps) return 0;
        
        double cosA = dot / (d1 * d2);
        cosA = Math.max(-1.0, Math.min(1.0, cosA)); 
        double angle = Math.acos(cosA);
        
        long cross = (long) current.dx * next.dy - (long) current.dy * next.dx;
        if (cross < 0) {
            angle = 2 * Math.PI - angle;
        }
        return angle;
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
        if (newX == -1 && newY == -1) return;
        
        TreeSet<NodeAngle> neighbors = nodeNeighbors.get(node);
        
        if (neighbors.size() > 1) {
            List<NodeAngle> neighborList = new ArrayList<>(neighbors);
            for (int i = 0; i < neighborList.size(); i++) {
                NodeAngle current = neighborList.get(i);
                NodeAngle next = neighborList.get((i + 1) % neighborList.size());
                double angle = getAngleDiff(current, next);
                allAngles.remove(angle);
            }
        }
        
        neighbors.clear();
        
        for (Edge e : adj.get(node.id)) {
            Node neighbor = e.getRemaining(node);
            VarNodePosition neighborPos = positions.get(neighbor);
            
            if (neighborPos.x() != -1 && neighborPos.y() != -1) {
                int dx = neighborPos.x() - newX;
                int dy = neighborPos.y() - newY;
                NodeAngle nodeAngle = new NodeAngle(neighbor, dx, dy);
                nodeAngleMap.get(node.id).put(neighbor.id, nodeAngle);
                neighbors.add(nodeAngle);
            }
        }
        
        if (neighbors.size() > 1) {
            List<NodeAngle> neighborList = new ArrayList<>(neighbors);
            for (int i = 0; i < neighborList.size(); i++) {
                NodeAngle current = neighborList.get(i);
                NodeAngle next = neighborList.get((i + 1) % neighborList.size());
                double angle = getAngleDiff(current, next);
                allAngles.add(angle);
            }
        }
        
        if (!allAngles.isEmpty()) {
            minAngleValue = allAngles.first();
        } else {
            minAngleValue = Math.PI * 2; 
        }
    }
    
    private void updateNeighborAngle(Node node, Node movedNode, int dx, int dy, boolean debug) {
        TreeSet<NodeAngle> neighbors = nodeNeighbors.get(node);
        NodeAngle oldNodeAngle = null;
        Map<Integer, NodeAngle> nodeAngles = nodeAngleMap.computeIfAbsent(node.id, k -> new HashMap<>());
        try {
            oldNodeAngle = nodeAngles.get(movedNode.id);
        } catch (Exception e) {}
        
        if (oldNodeAngle != null) {
            if (neighbors.size() > 1) {
                NodeAngle prev = neighbors.lower(oldNodeAngle);
                if (prev == null) prev = neighbors.last();
                NodeAngle next = neighbors.higher(oldNodeAngle);
                if (next == null) next = neighbors.first();

                double prevOldAngle = getAngleDiff(prev, oldNodeAngle);
                double oldNextAngle = getAngleDiff(oldNodeAngle, next);
                allAngles.remove(prevOldAngle);
                allAngles.remove(oldNextAngle);

                if (neighbors.size() > 2) {
                    double prevNextAngle = getAngleDiff(prev, next);
                    allAngles.add(prevNextAngle);
                }
            }
            neighbors.remove(oldNodeAngle);
        }

        NodeAngle newNodeAngle = new NodeAngle(movedNode, dx, dy);
        nodeAngles.put(movedNode.id, newNodeAngle);
        neighbors.add(newNodeAngle);

        if (neighbors.size() > 1) {
            NodeAngle prev = neighbors.lower(newNodeAngle);
            if (prev == null) prev = neighbors.last();
            
            NodeAngle next = neighbors.higher(newNodeAngle);
            if (next == null) next = neighbors.first();
            
            double prevNewAngle = getAngleDiff(prev, newNodeAngle);
            double newNextAngle = getAngleDiff(newNodeAngle, next);
            
            if (neighbors.size() > 2) {
                double prevNextAngle = getAngleDiff(prev, next);
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

    private NodeAngle getPrev(TreeSet<NodeAngle> set, NodeAngle target, int ignoreId) {
        if (set.isEmpty() || (set.size() == 1 && set.first().node.id == ignoreId)) return null;
        NodeAngle p = set.lower(target);
        if (p == null) p = set.last();
        if (p.node.id == ignoreId) {
            p = set.lower(p);
            if (p == null) p = set.last();
        }
        return p;
    }

    private NodeAngle getNext(TreeSet<NodeAngle> set, NodeAngle target, int ignoreId) {
        if (set.isEmpty() || (set.size() == 1 && set.first().node.id == ignoreId)) return null;
        NodeAngle n = set.higher(target);
        if (n == null) n = set.first();
        if (n.node.id == ignoreId) {
            n = set.higher(n);
            if (n == null) n = set.first();
        }
        return n;
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition vPosObj, int newX, int newY) {
        Node u = g.getNode(vPosObj.id);
        int oldX = vPosObj.x(), oldY = vPosObj.y();

        if (oldX == newX && oldY == newY) return evaluation();
        
        if (newX == -1 && newY == -1) {
            propagateOneNodeMove(vPosObj, -1, -1);
            double res = evaluation();
            propagateOneNodeMove(vPosObj, oldX, oldY);
            return res;
        }

        TreeMultiset<Double> removedAngles = new TreeMultiset<>(new DoubleCompare());
        List<Double> addedAngles = new ArrayList<>();

        TreeSet<NodeAngle> uNeighbors = nodeNeighbors.get(u);
        if (uNeighbors.size() > 1) {
            List<NodeAngle> list = new ArrayList<>(uNeighbors);
            for (int i = 0; i < list.size(); i++) {
                removedAngles.add(getAngleDiff(list.get(i), list.get((i + 1) % list.size())));
            }
            
            List<NodeAngle> newList = new ArrayList<>();
            for (Edge e : adj.get(u.id)) {
                Node neighbor = e.getRemaining(u);
                VarNodePosition nPos = positions.get(neighbor);
                if (nPos.x() != -1 && nPos.y() != -1) {
                    newList.add(new NodeAngle(neighbor, nPos.x() - newX, nPos.y() - newY));
                }
            }
            if (newList.size() > 1) {
                Collections.sort(newList); 
                for (int i = 0; i < newList.size(); i++) {
                    addedAngles.add(getAngleDiff(newList.get(i), newList.get((i + 1) % newList.size())));
                }
            }
        }

        for (Edge e : adj.get(u.id)) {
            Node v = e.getRemaining(u);
            VarNodePosition vPos = positions.get(v);
            if (vPos.x() == -1 && vPos.y() == -1) continue;

            TreeSet<NodeAngle> vNeighbors = nodeNeighbors.get(v);
            if (vNeighbors.size() > 1) {
                NodeAngle old_vu = new NodeAngle(u, oldX - vPos.x(), oldY - vPos.y());
                NodeAngle prev = getPrev(vNeighbors, old_vu, u.id);
                NodeAngle next = getNext(vNeighbors, old_vu, u.id);

                if (prev != null && next != null) {
                    boolean onlyTwo = (prev.node.id == next.node.id);
                    removedAngles.add(getAngleDiff(prev, old_vu));
                    removedAngles.add(getAngleDiff(old_vu, next));
                    if (!onlyTwo) addedAngles.add(getAngleDiff(prev, next)); 

                    NodeAngle new_vu = new NodeAngle(u, newX - vPos.x(), newY - vPos.y());
                    NodeAngle newPrev = getPrev(vNeighbors, new_vu, u.id);
                    NodeAngle newNext = getNext(vNeighbors, new_vu, u.id);

                    boolean newOnlyTwo = (newPrev.node.id == newNext.node.id);
                    if (!newOnlyTwo) removedAngles.add(getAngleDiff(newPrev, newNext)); 
                    addedAngles.add(getAngleDiff(newPrev, new_vu));
                    addedAngles.add(getAngleDiff(new_vu, newNext));
                }
            }
        }

        double minA = Double.POSITIVE_INFINITY;
        for (double a : addedAngles) {
            minA = Math.min(minA, a);
        }
        for (Double a : allAngles) {
            if (removedAngles.remove(a)) continue; 
            minA = Math.min(minA, a);
            break; 
        }

        return minA == Double.POSITIVE_INFINITY ? 0 : minA;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition v, int newX, int newY) {
        Node node = g.getNode(v.id);
        int oldX = v.x(), oldY = v.y();

        if (oldX == newX && oldY == newY || newX == -1 && newY == -1) return;

        updateNodeAngles(node, newX, newY);
        for (Edge e : adj.get(node.id)) {
            Node neighbor = e.getRemaining(node);
            VarNodePosition neighborPos = positions.get(neighbor);
            if (neighborPos.x() == -1 && neighborPos.y() == -1) continue;
            
            int dx = newX - neighborPos.x();
            int dy = newY - neighborPos.y();
            updateNeighborAngle(neighbor, node, dx, dy, true);
        }
    }

    @Override
    public double evaluateTwoNodesMove(VarNodePosition node1, int newX1, int newY1, VarNodePosition node2, int newX2, int newY2) {
        int oldX1 = node1.x(), oldY1 = node1.y();
        int oldX2 = node2.x(), oldY2 = node2.y();

        if (oldX1 == newX1 && oldY1 == newY1 && oldX2 == newX2 && oldY2 == newY2) {
            return evaluation();
        }

        propagateOneNodeMove(node1, newX1, newY1);
        double minA = evaluateOneNodeMove(node2, newX2, newY2);
        propagateOneNodeMove(node1, oldX1, oldY1);
        
        return minA;
    }

    @Override
    public void initPropagation() {}
}


// class NumberIntersectionEdges implements Function {
//     Graph g;
//     Map<Node, VarNodePosition> positions;
//     List<Edge> edges;
//     List<List<Edge>> adj;
//     Map<Integer, Set<Integer>> intersectMap = new HashMap<>();
//     Map<Long, CrossEdge> crossEdgeFuncs = new HashMap<>();
//     int totalIntersections = 0;

//     @Override
//     public String toString() {
//         return "numberIntersection=" + totalIntersections + " ";
//     }

//     private int encode(Edge e) {
//         int u = Math.min(e.fromNode.id, e.toNode.id);
//         int v = Math.max(e.fromNode.id, e.toNode.id);
//         return u * g.getNodes().size() + v;
//     }

//     private long encodePair(Edge e1, Edge e2) {
//         int u1 = encode(e1);
//         int u2 = encode(e2);
//         return ((long) Math.min(u1, u2) << 32) | (Math.max(u1, u2) & 0xFFFFFFFFL);
//     }

//     public NumberIntersectionEdges(Graph g, Map<Node, VarNodePosition> positions) {
//         this.g = g;
//         this.positions = positions;
//         adj = new ArrayList<>();
//         Map<Integer, Boolean> marked = new HashMap<>();
//         for (Node node : g.getNodes()) {
//             adj.add(node.id, new ArrayList<>());
//             for (Edge e : g.getEdges(node)) {
//                 int eId = encode(e);
//                 if (marked.containsKey(eId))
//                     continue;
//                 marked.put(eId, true);
//                 adj.get(node.id).add(e);
//             }
//             marked.clear();
//         }
//         edges = new ArrayList<>();
//         for (Edge e : g.getEdges()) {
//             int eId = encode(e);
//             if (marked.containsKey(eId))
//                 continue;
//             marked.put(eId, true);
//             edges.add(e);
//         }

//         for (Edge e : edges)
//             intersectMap.put(encode(e), new HashSet<>());

//         for (int i = 0; i < edges.size(); i++) {
//             Edge ei = edges.get(i);

//             for (int j = i + 1; j < edges.size(); j++) {
//                 Edge ej = edges.get(j);
//                 if (ej == null)
//                     continue;
//                 if (ei.fromNode.id == ej.fromNode.id || ei.toNode.id == ej.toNode.id ||
//                         ei.fromNode.id == ej.toNode.id || ei.toNode.id == ej.fromNode.id)
//                     continue;

//                 CrossEdge ce = new CrossEdge(positions.get(ei.fromNode), positions.get(ei.toNode),
//                         positions.get(ej.fromNode), positions.get(ej.toNode));
//                 crossEdgeFuncs.put(encodePair(ei, ej), ce);

//                 if (ce.evaluation() > 0.5) {
//                     intersectMap.get(encode(ei)).add(encode(ej));
//                     intersectMap.get(encode(ej)).add(encode(ei));
//                     totalIntersections++;
//                 }
//             }
//         }
//     }

//     private int removeNodeIntersections(Node node) {
//         int removedCount = 0;
//         List<Edge> nodeEdges = adj.get(node.id);
//         for (Edge e : nodeEdges) {
//             Set<Integer> intersectedEdges = intersectMap.get(encode(e));
//             removedCount += intersectedEdges.size();
//             for (Integer ie : intersectedEdges) {
//                 intersectMap.get(ie).remove(encode(e));
//             }
//             intersectedEdges.clear();
//         }
//         return removedCount;
//     }

//     private int addNodeIntersections(Node node, int newX, int newY, VarNodePosition varNodePosition) {
//         int addedCount = 0;
//         List<Edge> nodeEdges = adj.get(node.id);

//         for (Edge e : nodeEdges) {
//             int ei = encode(e);
//             for (Edge f : edges) {
//                 if (e.fromNode.id == f.fromNode.id || e.toNode.id == f.toNode.id ||
//                         e.fromNode.id == f.toNode.id || e.toNode.id == f.fromNode.id)
//                     continue;
//                 if (encode(f) == ei)
//                     continue;

//                 CrossEdge ce = crossEdgeFuncs.get(encodePair(e, f));
//                 if (ce != null) {
//                     double eval = ce.evaluateOneNodeMove(varNodePosition, newX, newY);
//                     if (eval > 0.5) {
//                         int fi = encode(f);
//                         intersectMap.get(ei).add(fi);
//                         intersectMap.get(fi).add(ei);
//                         addedCount++;
//                     }
//                 }
//             }
//         }
//         return addedCount;
//     }

//     @Override
//     public double evaluation() {
//         return -totalIntersections;
//     }

//     @Override
//     public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
//         int oldX = varNodePosition.x(), oldY = varNodePosition.y();
//         if (oldX == newX && oldY == newY)
//             return evaluation();
//         if (newX == -1 && newY == -1) {
//             Node node = g.getNode(varNodePosition.id);
//             int removed = 0;
//             List<Edge> nodeEdges = adj.get(node.id);
//             for (Edge e : nodeEdges) {
//                 removed += intersectMap.get(encode(e)).size();
//             }
//             return -totalIntersections + removed;
//         }

//         Node node = g.getNode(varNodePosition.id);
//         int removed = 0;
//         List<Edge> nodeEdges = adj.get(node.id);
//         for (Edge e : nodeEdges) {
//             removed += intersectMap.get(encode(e)).size();
//         }

//         int addedCount = 0;
//         for (Edge e : nodeEdges) {
//             int ei = encode(e);
//             for (Edge f : edges) {
//                 if (e.fromNode.id == f.fromNode.id || e.toNode.id == f.toNode.id ||
//                         e.fromNode.id == f.toNode.id || e.toNode.id == f.fromNode.id)
//                     continue;
//                 if (encode(f) == ei)
//                     continue;
//                 CrossEdge ce = crossEdgeFuncs.get(encodePair(e, f));
//                 if (ce != null && ce.evaluateOneNodeMove(varNodePosition, newX, newY) > 0.5) {
//                     addedCount++;
//                 }
//             }
//         }
//         return -totalIntersections + removed - addedCount;
//     }

//     @Override
//     public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
//         if (newX == -1 && newY == -1)
//             return;
//         int oldX = varNodePosition.x(), oldY = varNodePosition.y();
//         if (oldX == newX && oldY == newY)
//             return;

//         Node node = g.getNode(varNodePosition.id);
//         totalIntersections -= removeNodeIntersections(node);

//         // Propagate down to CrossEdge items concerning this node
//         List<Edge> nodeEdges = adj.get(node.id);
//         for (Edge e : nodeEdges) {
//             for (Edge f : edges) {
//                 if (e.fromNode.id == f.fromNode.id || e.toNode.id == f.toNode.id ||
//                         e.fromNode.id == f.toNode.id || e.toNode.id == f.fromNode.id)
//                     continue;
//                 if (encode(f) == encode(e))
//                     continue;
//                 CrossEdge ce = crossEdgeFuncs.get(encodePair(e, f));
//                 if (ce != null)
//                     ce.propagateOneNodeMove(varNodePosition, newX, newY);
//             }
//         }

//         totalIntersections += addNodeIntersections(node, newX, newY, varNodePosition);
//     }

//     @Override
//     public void initPropagation() {
//     }
// }
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
    public List<VarNodePosition> getConflictNodes() {
        Set<Integer> conflictNodeIds = new HashSet<>();
        for (Map.Entry<Integer, Set<Integer>> entry : intersectMap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                int edgeId = entry.getKey();
                int u = edgeId / g.getNodes().size();
                int v = edgeId % g.getNodes().size();
                conflictNodeIds.add(u);
                conflictNodeIds.add(v);
            }
        }
        
        List<VarNodePosition> res = new ArrayList<>();
        for (Integer id : conflictNodeIds) {
            res.add(positions.get(g.getNode(id)));
        }
        return res;
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
                
                // if(sj != null && si.intersect(sj) != null) {
                if (sj != null && si.fastIntersect(si)) {
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
                
                // if (se.intersect(sf) != null) {
                if (se.fastIntersect(sf)) {
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

        Node node = g.getNode(v.id);
        int oldX = v.x(), oldY = v.y();

        if (oldX == newX && oldY == newY) {
            return evaluation();
        }

        if (newX == -1 && newY == -1) {
            int current = totalIntersections;
            for (Edge e : adj.get(node.id)) {
                Set<Integer> intersectedEdges = intersectMap.get(encode(e));
                current -= intersectedEdges.size();
            }
            return -current;
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
                if (se.fastIntersect(sf)) {
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
    public double evaluateTwoNodesMove(VarNodePosition node1, int newX1, int newY1, VarNodePosition node2, int newX2, int newY2) {
        int oldX1 = node1.x(), oldY1 = node1.y();
        int oldX2 = node2.x(), oldY2 = node2.y();

        if (oldX1 == newX1 && oldY1 == newY1 && oldX2 == newX2 && oldY2 == newY2) {
            return evaluation();
        }

        Set<Integer> changedEdgeIds = new HashSet<>();
        for (Edge e : adj.get(node1.id)) changedEdgeIds.add(encode(e));
        for (Edge e : adj.get(node2.id)) changedEdgeIds.add(encode(e));

        int removedCount = 0;
        for (int eId : changedEdgeIds) {
            Set<Integer> intersected = intersectMap.getOrDefault(eId, null);
            if (intersected != null) {
                for (int otherId : intersected) {
                    if (!changedEdgeIds.contains(otherId) || eId < otherId) {
                        removedCount++;
                    }
                }
            }
        }

        node1.assign(newX1, newY1);
        node2.assign(newX2, newY2);

        int addedCount = 0;
        List<Edge> changedEdges = new ArrayList<>();
        for (Edge e : edges) {
            if (changedEdgeIds.contains(encode(e))) {
                changedEdges.add(e);
            }
        }

        List<Edge> unchangedEdges = new ArrayList<>();
        for (Edge e : edges) {
            if (!changedEdgeIds.contains(encode(e))) {
                unchangedEdges.add(e);
            }
        }
        for (int i = 0; i < changedEdges.size(); i++) {
            Edge e1 = changedEdges.get(i);
            Segment2D se1 = createSegment(e1);
            if (se1 == null) continue;

            for (int j = i + 1; j < changedEdges.size(); j++) {
                Edge e2 = changedEdges.get(j);
                if (e1.fromNode.id == e2.fromNode.id || e1.toNode.id == e2.toNode.id ||
                    e1.fromNode.id == e2.toNode.id || e1.toNode.id == e2.fromNode.id) continue;
                Segment2D se2 = createSegment(e2);
                if (se2 == null) continue;
                if (se1.fastIntersect(se2)) addedCount++;
            }

            for (Edge f : unchangedEdges) {
                if (e1.fromNode.id == f.fromNode.id || e1.toNode.id == f.toNode.id ||
                    e1.fromNode.id == f.toNode.id || e1.toNode.id == f.fromNode.id) continue;
                
                Segment2D sf = createSegment(f); 
                if (sf == null) continue;
                if (se1.fastIntersect(sf)) addedCount++;
            }
        }

        node1.assign(oldX1, oldY1);
        node2.assign(oldX2, oldY2);

        return -(totalIntersections - removedCount + addedCount);
    }
    @Override
    public void initPropagation() {

    }
}


// class MinDistanceNodeEdge implements Function {
//     private final Graph g;
//     private final Map<Node, VarNodePosition> positions;
//     private final TreeMultiset<Double> pq;
//     private final Map<Long, Distance> distanceFuncs;
//     private final List<Edge> edges;
//     private final List<List<Edge>> adj;

//     @Override
//     public String toString() {
//         return "minDistance=" + evaluation() + " ";
//     }

//     private long encode(Node node, Edge edge) {
//         return encode(edge) * g.getNodes().size() + node.id;
//     }

//     private long encode(Edge edge) {
//         int u = Math.min(edge.fromNode.id, edge.toNode.id);
//         int v = Math.max(edge.fromNode.id, edge.toNode.id);
//         return (long) u * g.getNodes().size() + v;
//     }

//     public MinDistanceNodeEdge(Graph g, Map<Node, VarNodePosition> positions) {
//         this.g = g;
//         this.positions = positions;
//         adj = new ArrayList<>();
//         Map<Long, Boolean> marked = new HashMap<>();
//         for (Node node : g.getNodes()) {
//             adj.add(node.id, new ArrayList<>());
//             for (Edge e : g.getEdges(node)) {
//                 long eId = encode(e);
//                 if (marked.containsKey(eId))
//                     continue;
//                 marked.put(eId, true);
//                 adj.get(node.id).add(e);
//             }
//             marked.clear();
//         }
//         edges = new ArrayList<>();
//         for (Edge e : g.getEdges()) {
//             long eId = encode(e);
//             if (marked.containsKey(eId))
//                 continue;
//             marked.put(eId, true);
//             edges.add(e);
//         }
//         distanceFuncs = new HashMap<>();
//         pq = new TreeMultiset<>(new DoubleCompare());

//         for (Node node : g.getNodes()) {
//             VarNodePosition pos = positions.get(node);
//             for (Edge edge : edges) {
//                 if (edge.fromNode.id == node.id || edge.toNode.id == node.id)
//                     continue;
//                 VarNodePosition posU = positions.get(edge.fromNode);
//                 VarNodePosition posV = positions.get(edge.toNode);
//                 Distance distFunc = new Distance(pos, posU, posV);
//                 long key = encode(node, edge);
//                 distanceFuncs.put(key, distFunc);
//                 pq.add(distFunc.evaluation());
//             }
//         }
//     }

//     @Override
//     public double evaluation() {
//         if (pq.isEmpty())
//             return Double.POSITIVE_INFINITY;
//         return pq.first();
//     }

//     @Override
//     public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
//         int oldX = varNodePosition.x(), oldY = varNodePosition.y();
//         if (oldX == newX && oldY == newY) {
//             return evaluation();
//         }
//         if (newX == -1 && newY == -1) {
//             TreeMultiset<Double> visited = new TreeMultiset<>(new DoubleCompare());
//             Node node = g.getNode(varNodePosition.id);
//             for (Edge edge : edges) {
//                 if (edge.fromNode.id == node.id || edge.toNode.id == node.id)
//                     continue;
//                 long key = encode(node, edge);
//                 Distance distFunc = distanceFuncs.get(key);
//                 if (distFunc != null) {
//                     visited.add(distFunc.evaluation());
//                 }
//             }

//             for (Node adjNode : g.getNodes()) {
//                 if (adjNode.id == node.id)
//                     continue;
//                 for (Edge edge : adj.get(node.id)) {
//                     if (edge.toNode.id == adjNode.id)
//                         continue;
//                     long key = encode(adjNode, edge);
//                     Distance distFunc = distanceFuncs.get(key);
//                     if (distFunc != null) {
//                         visited.add(distFunc.evaluation());
//                     }
//                 }
//             }

//             for (Double d : pq) {
//                 if (!visited.remove(d)) {
//                     return d;
//                 }
//             }
//             return Double.POSITIVE_INFINITY;
//         }

//         double min = Double.POSITIVE_INFINITY;
//         Node node = g.getNode(varNodePosition.id);
//         TreeMultiset<Double> visited = new TreeMultiset<>(new DoubleCompare());

//         for (Edge edge : edges) {
//             if (edge.fromNode.id == node.id || edge.toNode.id == node.id)
//                 continue;
//             long key = encode(node, edge);
//             Distance distFunc = distanceFuncs.get(key);
//             if (distFunc != null) {
//                 visited.add(distFunc.evaluation());
//                 double d = distFunc.evaluateOneNodeMove(varNodePosition, newX, newY);
//                 min = Math.min(min, d);
//             }
//         }

//         for (Node adjNode : g.getNodes()) {
//             if (adjNode.id == node.id)
//                 continue;
//             for (Edge edge : adj.get(node.id)) {
//                 if (edge.toNode.id == adjNode.id)
//                     continue;
//                 long key = encode(adjNode, edge);
//                 Distance distFunc = distanceFuncs.get(key);
//                 if (distFunc != null) {
//                     visited.add(distFunc.evaluation());
//                     double d = distFunc.evaluateOneNodeMove(varNodePosition, newX, newY);
//                     min = Math.min(min, d);
//                 }
//             }
//         }

//         for (Double d : pq) {
//             if (d > min - Point2D.eps) {
//                 return min;
//             }
//             if (!visited.remove(d)) {
//                 return d;
//             }
//         }
//         return min;
//     }

//     @Override
//     public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
//         if (newX == -1 && newY == -1)
//             return;
//         int oldX = varNodePosition.x(), oldY = varNodePosition.y();
//         if (oldX == newX && oldY == newY)
//             return;
//         Node node = g.getNode(varNodePosition.id);

//         for (Edge edge : edges) {
//             if (edge.fromNode.id == node.id || edge.toNode.id == node.id)
//                 continue;
//             long key = encode(node, edge);
//             Distance distFunc = distanceFuncs.get(key);
//             if (distFunc != null) {
//                 double oldD = distFunc.evaluation();
//                 distFunc.propagateOneNodeMove(varNodePosition, newX, newY);
//                 double distance = distFunc.evaluation();
//                 if (oldD > -1.) {
//                     if (Math.abs(oldD - distance) >= Point2D.eps) {
//                         pq.remove(oldD);
//                     }
//                 }
//                 if (Math.abs(oldD - distance) >= Point2D.eps) {
//                     pq.add(distance);
//                 }
//             }
//         }

//         for (Node adjNode : g.getNodes()) {
//             if (adjNode.id == node.id)
//                 continue;
//             for (Edge edge : adj.get(node.id)) {
//                 if (edge.toNode.id == adjNode.id)
//                     continue;
//                 long key = encode(adjNode, edge);
//                 Distance distFunc = distanceFuncs.get(key);
//                 if (distFunc != null) {
//                     double oldD = distFunc.evaluation();
//                     distFunc.propagateOneNodeMove(varNodePosition, newX, newY);
//                     double distance = distFunc.evaluation();
//                     if (oldD > -1.) {
//                         if (Math.abs(oldD - distance) >= Point2D.eps) {
//                             pq.remove(oldD);
//                         }
//                     }
//                     if (Math.abs(oldD - distance) >= Point2D.eps) {
//                         pq.add(distance);
//                     }
//                 }
//             }
//         }
//     }

//     @Override
//     public void initPropagation() {
//     }
// }
class MinDistanceNodeEdge implements Function {
    private final Graph g;
    private final Map<Node, VarNodePosition> positions;
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
                        pq.add(distance);
                    }
                }
            }
        }
    }

    @Override
    public double evaluation() {
        if (pq.isEmpty()) return Double.POSITIVE_INFINITY;
        return pq.first();
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        int oldX = varNodePosition.x(), oldY = varNodePosition.y();
        if (oldX == newX && oldY == newY) {
            return evaluation();
        }
        if (newX == -1 && newY == -1) {
            TreeMultiset<Double> visited = new TreeMultiset<>(new DoubleCompare());
            Node node = g.getNode(varNodePosition.id);
            for (Edge edge : edges) {
                if (edge.fromNode.id == node.id || edge.toNode.id == node.id) continue;
                long key = encode(node, edge);
                double oldD = distances.getOrDefault(key, -1.);
                if (oldD > -1.) {
                    visited.add(oldD);
                }
            }

            for (Node adjNode : g.getNodes()) {
                if (adjNode.id == node.id) continue;
                VarNodePosition pos = positions.get(adjNode);
                if (pos.x() == -1 || pos.y() == -1) continue;
                for (Edge edge : adj.get(node.id)) {
                    if (edge.toNode.id == adjNode.id) continue;
                    long key = encode(adjNode, edge);
                    double oldD = distances.getOrDefault(key, -1.);
                    if (oldD > -1.) {
                        visited.add(oldD);
                    }
                }
            }

            for (Double d : pq) {
                if (!visited.remove(d)) {
                    return d;
                }
            }
            return Double.POSITIVE_INFINITY;
        }

        double min = Double.POSITIVE_INFINITY;
        Node node = g.getNode(varNodePosition.id);
        Point2D point = new Point2D(newX, newY);
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
        for (Double d : pq) {
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
                pq.remove(oldD);
            }
            distances.put(key, distance);
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
                        pq.remove(oldD);
                    }
                    distances.put(key, distance);
                    pq.add(distance);
                }
            }
        }
    }

    @Override
    public double evaluateTwoNodesMove(VarNodePosition node1, int newX1, int newY1, VarNodePosition node2, int newX2, int newY2) {
        int oldX1 = node1.x(), oldY1 = node1.y();
        int oldX2 = node2.x(), oldY2 = node2.y();

        if (oldX1 == newX1 && oldY1 == newY1 && oldX2 == newX2 && oldY2 == newY2) {
            return evaluation();
        }

        int N = g.getNodes().size();
        Set<Long> affectedKeys = new HashSet<>();

        for (Edge e : edges) {
            if (e.fromNode.id != node1.id && e.toNode.id != node1.id) {
                affectedKeys.add(encode(g.getNode(node1.id), e));
            }
        }
        
        for (Edge e : edges) {
            if (e.fromNode.id != node2.id && e.toNode.id != node2.id) {
                affectedKeys.add(encode(g.getNode(node2.id), e));
            }
        }
        
        for (Edge e : adj.get(node1.id)) {
            for (Node v : g.getNodes()) {
                if (v.id != e.fromNode.id && v.id != e.toNode.id) {
                    affectedKeys.add(encode(v, e));
                }
            }
        }
        
        for (Edge e : adj.get(node2.id)) {
            for (Node v : g.getNodes()) {
                if (v.id != e.fromNode.id && v.id != e.toNode.id) {
                    affectedKeys.add(encode(v, e));
                }
            }
        }

        TreeMultiset<Double> visited = new TreeMultiset<>(new DoubleCompare());
        for (long key : affectedKeys) {
            double oldD = distances.getOrDefault(key, -1.);
            if (oldD > -1.) {
                visited.add(oldD);
            }
        }

        node1.assign(newX1, newY1);
        node2.assign(newX2, newY2);

        double minD = Double.POSITIVE_INFINITY;

        for (long key : affectedKeys) {
            int nId = (int) (key % N);
            long edgeCode = key / N;
            int uId = (int) (edgeCode / N);
            int vId = (int) (edgeCode % N);

            VarNodePosition posN = positions.get(g.getNode(nId));
            VarNodePosition posU = positions.get(g.getNode(uId));
            VarNodePosition posV = positions.get(g.getNode(vId));

            if (posN.x() == -1 || posN.y() == -1 || posU.x() == -1 || posU.y() == -1 || posV.x() == -1 || posV.y() == -1) {
                continue;
            }

            Segment2D seg = new Segment2D(new Point2D(posU.x(), posU.y()), new Point2D(posV.x(), posV.y()));
            double d = new Point2D(posN.x(), posN.y()).distance(seg);
            minD = Math.min(minD, d);
        }

        node1.assign(oldX1, oldY1);
        node2.assign(oldX2, oldY2);

        for (Double d : pq) {
            if (d > minD - Point2D.eps) {
                return minD;
            }
            if (!visited.remove(d)) {
                return d; 
            }
        }
        return minD;
    }
    @Override
    public void initPropagation() {

    }
}

class LexMultiValues {
    List<Double> values;

    public LexMultiValues(List<Double> values) {
        this.values = values;
    }

    public boolean better(LexMultiValues V) {
        if (V == null)
            return true;
        for (int i = 0; i < values.size(); i++) {
            double a = values.get(i), b = V.values.get(i);
            if (a >= b + Point2D.eps)
                return true;
            if (a <= b - Point2D.eps)
                return false;
        }
        return false;
    }

    public boolean equals(LexMultiValues V) {
        if (V == null)
            return false;
        for (int i = 0; i < values.size(); i++) {
            double a = values.get(i), b = V.values.get(i);
            if (Math.abs(a - b) > Point2D.eps)
                return false;
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

    public ObjectiveFunction(Function f1, Function f2, Function f3, Function f4, double w1, double w2, double w3,
            double w4) {
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
    public double evaluateTwoNodesMove(VarNodePosition node1, int newX1, int newY1, VarNodePosition node2, int newX2, int newY2) {
        return f1.evaluateTwoNodesMove(node1, newX1, newY1, node2, newX2, newY2) * w1 +
                f2.evaluateTwoNodesMove(node1, newX1, newY1, node2, newX2, newY2) * w2 +
                f3.evaluateTwoNodesMove(node1, newX1, newY1, node2, newX2, newY2) * w3 +
                f4.evaluateTwoNodesMove(node1, newX1, newY1, node2, newX2, newY2) * w4;
    }

    @Override
    public void initPropagation() {
    }
}

class LexMultiFunctions {
    /*
     * lexicographic multi-functions
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

    public LexMultiValues evaluateOneNodeMove(VarNodePosition v, int x, int y) {
        List<Double> vals = new ArrayList<>();
        for (Function f : F)
            vals.add(f.evaluateOneNodeMove(v, x, y));
        return new LexMultiValues(vals);
    }
    public LexMultiValues evaluateTwoNodesMove(VarNodePosition v1, int x1, int y1, VarNodePosition v2, int x2, int y2) {
        List<Double> vals = new ArrayList<>();
        for (Function f : F)
            vals.add(f.evaluateTwoNodesMove(v1, x1, y1, v2, x2, y2));
        return new LexMultiValues(vals);
    }


    public void propagateOneNodeMove(VarNodePosition v, int x, int y) {
        for (Function f : F)
            f.propagateOneNodeMove(v, x, y);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Function f : F) {
            sb.append(String.format("%.10f ", f.evaluation()));
        }
        return sb.toString().trim();
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
        return String.format("Move(%d, %d) to (%d, %d)", varNodePosition.id, varNodePosition.x(), varNodePosition.y(),
                newX, newY);
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
        if (o == null || o.values == null)
            return true;
        if (values == null)
            return false;
        return values.better(o.values);
    }

    public boolean better(LexMultiValues o) {
        if (o == null)
            return true;
        if (values == null)
            return false;
        return values.better(o);
    }
}

interface NeighborExplorer {
    /**
     * Explore the neighborhood of the current solution.
     * 
     * @return The solution found in the neighborhood.
     */
    Move explore(boolean firstImprovement);
}

class VNSNeighborhood implements NeighborExplorer {

    private final int ROW, COL;
    private final Graph G;
    private final LexMultiFunctions F;
    private final List<VarNodePosition> varNodeList;
    private final int counter;

    public VNSNeighborhood(int ROW, int COL, Graph G, LexMultiFunctions F, List<VarNodePosition> varNodeList, int counter) {
        this.ROW = ROW;
        this.COL = COL;
        this.G = G;
        this.F = F;
        this.varNodeList = varNodeList;
        this.counter = counter;
    }



    @Override
    public Move explore(boolean firstImprovement) {
        boolean found = false;
        LexMultiValues bestEval = F.evaluation();
        Move selectedMove = null;
        // FIRST try with one node move
        for(VarNodePosition varNodePosition : varNodeList) {
            for(int x = 0; x <= COL; x++) {
                for(int y = 0; y <= ROW; y++) {
                    if (x == varNodePosition.x() && y == varNodePosition.y()) continue;
                    LexMultiValues current = F.evaluateOneNodeMove(varNodePosition, x, y);
                    if (current.better(bestEval)) {
                        Move move = new Move(varNodePosition, x, y, current);
                        selectedMove = move;    
                        if (firstImprovement) {
                            return move;                            
                        }           
                        bestEval = current;             
                    }
                }
                if (found) break;
            }
        }   
        // SECOND try with two nodes move
        for(VarNodePosition varNodePosition1 : varNodeList) {
            for(VarNodePosition varNodePosition2 : varNodeList) {
                if (varNodePosition1.id >= varNodePosition2.id) continue;
                for(int x1 = 0; x1 <= COL; x1++) {
                    for(int y1 = 0; y1 <= ROW; y1++) {
                        if (x1 == varNodePosition1.x() && y1 == varNodePosition1.y()) continue;
                        for(int x2 = 0; x2 <= COL; x2++) {
                            for(int y2 = 0; y2 <= ROW; y2++) {
                                if (x2 == varNodePosition2.x() && y2 == varNodePosition2.y()) continue;
                                LexMultiValues current = F.evaluateTwoNodesMove(varNodePosition1, x1, y1, varNodePosition2, x2, y2);
                                Move move = new Move(List.of(new MoveNode(varNodePosition1, x1, y1), new MoveNode(varNodePosition2, x2, y2)), current);
                                if (current.better(bestEval)) {                                    
                                    selectedMove = move;    
                                    if (firstImprovement) {
                                        return move;                            
                                    }           
                                    bestEval = current;             
                                }
                            }
                        }
                    }
                }
            }
        }
        return selectedMove;
    }
}   
class OneRandomNeighborhood implements NeighborExplorer {
    private final int ROW, COL;
    private final Graph G;
    private final LexMultiFunctions F;
    private final List<VarNodePosition> varNodeList;
    private final int counter;

    public OneRandomNeighborhood(int ROW, int COL, Graph G, LexMultiFunctions F, List<VarNodePosition> varNodeList,
            int counter) {
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
        Random random = java.util.concurrent.ThreadLocalRandom.current();
        Collections.shuffle(varNodeList, random);
        int i = 0;
        while (iterations-- > 0 && i < varNodeList.size()) {
            // int i = random.nextInt(G.getNodes().size());
            VarNodePosition varNodePosition = varNodeList.get(i++);
            boolean found = false;
            // int d = random.nextInt(2) + 1;
            int d = 2;
            // for (int d = 1; d < 4 && !found; d++) {
            int LX = Math.max(0, varNodePosition.x() - d);
            int RX = Math.min(COL, varNodePosition.x() + d);
            int LY = Math.max(0, varNodePosition.y() - d);
            int RY = Math.min(ROW, varNodePosition.y() + d);
            // int newX = random.nextInt(RX - LX + 1) + LX;
            // int newY = random.nextInt(RY - LY + 1) + LY;

            for (int newX = LX; newX <= RX && !found; newX++) {
                for (int newY = LY; newY <= RY && !found; newY++) {
                    if (newX == varNodePosition.x() && newY == varNodePosition.y())
                        continue;
                    LexMultiValues current = F.evaluateOneNodeMove(varNodePosition, newX, newY);
                    if (current.better(values)) {
                        Move move = new Move(varNodePosition, newX, newY, current);
                        if (firstImprovement) {
                            moves.add(move);
                            found = true;
                            break;
                        }
                        moves.clear();
                        moves.add(move);
                        values = current;
                    } else if (current.equals(values)) {
                        Move move = new Move(varNodePosition, newX, newY, current);
                        moves.add(move);
                    }
                }
            }
            // }
            if (found)
                break;
        }

        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(random.nextInt(moves.size()));
    }
}

class OneRandomNeighborhoodCOL implements NeighborExplorer {
    private final int COL;
    private final LexMultiFunctions F;
    private final List<VarNodePosition> varNodeList;
    private final int counter;

    public OneRandomNeighborhoodCOL(int COL, LexMultiFunctions F, List<VarNodePosition> varNodeList, int counter) {
        this.COL = COL;
        this.F = F;
        this.varNodeList = varNodeList;
        this.counter = counter;
    }

    @Override
    public Move explore(boolean firstImprovement) {
        List<Move> moves = new ArrayList<>();
        int iterations = counter;
        LexMultiValues values = F.evaluation();
        Random random = java.util.concurrent.ThreadLocalRandom.current();
        Collections.shuffle(varNodeList, random);
        int i = 0;
        while (iterations-- > 0 && i < varNodeList.size()) {
            // int i = random.nextInt(G.getNodes().size());
            VarNodePosition varNodePosition = varNodeList.get(i++);
            boolean found = false;
            // int d = random.nextInt(2) + 1;
            int d = 4;
            // for (int d = 1; d < 4 && !found; d++) {
            int LX = Math.max(0, varNodePosition.x() - d);
            int RX = Math.min(COL, varNodePosition.x() + d);
            // int newX = random.nextInt(RX - LX + 1) + LX;
            // int newY = random.nextInt(RY - LY + 1) + LY;

            int newY = varNodePosition.y();
            for (int newX = LX; newX <= RX && !found; newX++) {
                if (newX == varNodePosition.x())
                    continue;
                LexMultiValues current = F.evaluateOneNodeMove(varNodePosition, newX, newY);
                if (current.better(values)) {
                    Move move = new Move(varNodePosition, newX, newY, current);
                    if (firstImprovement) {
                        moves.add(move);
                        found = true;
                        break;
                    }
                    moves.clear();
                    moves.add(move);
                    values = current;
                } else if (current.equals(values)) {
                    Move move = new Move(varNodePosition, newX, newY, current);
                    moves.add(move);
                }
            }
            // }
            if (found)
                break;
        }

        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(random.nextInt(moves.size()));
    }
}

class OneRandomNeighborhoodROW implements NeighborExplorer {
    private final int ROW;
    private final LexMultiFunctions F;
    private final List<VarNodePosition> varNodeList;
    private final int counter;

    public OneRandomNeighborhoodROW(int ROW, LexMultiFunctions F, List<VarNodePosition> varNodeList, int counter) {
        this.ROW = ROW;
        this.F = F;
        this.varNodeList = varNodeList;
        this.counter = counter;
    }

    @Override
    public Move explore(boolean firstImprovement) {
        List<Move> moves = new ArrayList<>();
        int iterations = counter;
        LexMultiValues values = F.evaluation();
        Random random = java.util.concurrent.ThreadLocalRandom.current();
        Collections.shuffle(varNodeList, random);
        int i = 0;
        while (iterations-- > 0 && i < varNodeList.size()) {
            // int i = random.nextInt(G.getNodes().size());
            VarNodePosition varNodePosition = varNodeList.get(i++);
            boolean found = false;
            // int d = random.nextInt(2) + 1;
            int d = 4;
            // for (int d = 1; d < 4 && !found; d++) {
            int newX = varNodePosition.x();
            int LY = Math.max(0, varNodePosition.y() - d);
            int RY = Math.min(ROW, varNodePosition.y() + d);
            // int newX = random.nextInt(RX - LX + 1) + LX;
            // int newY = random.nextInt(RY - LY + 1) + LY;

            for (int newY = LY; newY <= RY && !found; newY++) {
                if (newX == varNodePosition.x() && newY == varNodePosition.y())
                    continue;
                LexMultiValues current = F.evaluateOneNodeMove(varNodePosition, newX, newY);
                if (current.better(values)) {
                    Move move = new Move(varNodePosition, newX, newY, current);
                    if (firstImprovement) {
                        moves.add(move);
                        found = true;
                        break;
                    }
                    moves.clear();
                    moves.add(move);
                    values = current;
                } else if (current.equals(values)) {
                    Move move = new Move(varNodePosition, newX, newY, current);
                    moves.add(move);
                }
            }
            // }
            if (found)
                break;
        }

        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(random.nextInt(moves.size()));
    }
}

class OneRandomMove implements NeighborExplorer {
    private final int ROW, COL;
    private final Graph G;
    private final LexMultiFunctions F;
    private final List<VarNodePosition> varNodeList;
    private final int counter;

    public OneRandomMove(int ROW, int COL, Graph G, LexMultiFunctions F, List<VarNodePosition> varNodeList,
            int counter) {
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
        Random random = java.util.concurrent.ThreadLocalRandom.current();
        while (iterations-- > 0) {
            int i = random.nextInt(G.getNodes().size());
            VarNodePosition varNodePosition = varNodeList.get(i);
            int oldX = varNodePosition.x(), oldY = varNodePosition.y();
            int newX, newY;
            do {
                newX = random.nextInt(COL + 1);
                newY = random.nextInt(ROW + 1);
            } while (newX == oldX && newY == oldY);

            LexMultiValues current = F.evaluateOneNodeMove(varNodePosition, newX, newY);
            if (current.better(values)) {
                Move move = new Move(varNodePosition, newX, newY, current);
                if (firstImprovement) {
                    moves.add(move);
                    break;
                }
                moves.clear();
                moves.add(move);
                values = current;
            } else if (current.equals(values)) {

                Move move = new Move(varNodePosition, newX, newY, current);
                moves.add(move);
            }
        }

        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(random.nextInt(moves.size()));
    }
}

class OneRandomCOL implements NeighborExplorer {
    private final int COL;
    private final Graph G;
    private final LexMultiFunctions F;
    private final List<VarNodePosition> varNodeList;
    private final int counter;

    public OneRandomCOL(int COL, Graph G, LexMultiFunctions F, List<VarNodePosition> varNodeList, int counter) {
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
        Random random = java.util.concurrent.ThreadLocalRandom.current();
        while (iterations-- > 0) {
            int i = random.nextInt(G.getNodes().size());
            VarNodePosition varNodePosition = varNodeList.get(i);
            int newX;
            do {
                newX = random.nextInt(COL + 1);
            } while (newX == varNodePosition.x());
            int newY = varNodePosition.y();

            LexMultiValues current = F.evaluateOneNodeMove(varNodePosition, newX, newY);
            if (current.better(values)) {
                Move move = new Move(varNodePosition, newX, newY, current);
                if (firstImprovement) {
                    moves.add(move);
                    break;
                }
                moves.clear();
                moves.add(move);
                values = current;
            } else if (current.equals(values)) {
                Move move = new Move(varNodePosition, newX, newY, current);
                moves.add(move);
            }
        }

        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(random.nextInt(moves.size()));
    }
}

class OneRandomROW implements NeighborExplorer {
    private final int ROW;
    private final Graph G;
    private final LexMultiFunctions F;
    private final List<VarNodePosition> varNodeList;
    private final int counter;

    public OneRandomROW(int ROW, Graph G, LexMultiFunctions F, List<VarNodePosition> varNodeList, int counter) {
        this.ROW = ROW;
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
        Random random = java.util.concurrent.ThreadLocalRandom.current();
        while (iterations-- > 0) {
            int i = random.nextInt(G.getNodes().size());
            VarNodePosition varNodePosition = varNodeList.get(i);
            int newX = varNodePosition.x();
            int newY;
            do {
                newY = random.nextInt(ROW + 1);
            } while (newY == varNodePosition.y());

            LexMultiValues current = F.evaluateOneNodeMove(varNodePosition, newX, newY);
            if (current.better(values)) {
                Move move = new Move(varNodePosition, newX, newY, current);
                if (firstImprovement) {
                    moves.add(move);
                    break;
                }
                moves.clear();
                moves.add(move);
                values = current;
            } else if (current.equals(values)) {
                Move move = new Move(varNodePosition, newX, newY, current);
                moves.add(move);
            }
        }

        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(random.nextInt(moves.size()));
    }
}

class TwoRandomMove implements NeighborExplorer {
    private final int ROW, COL;
    private final Graph G;
    private final CBLSGPModel model;
    private final LexMultiFunctions F;
    private final List<VarNodePosition> varNodeList;
    private final int counter;

    public TwoRandomMove(int ROW, int COL, Graph G, CBLSGPModel model, LexMultiFunctions F,
            List<VarNodePosition> varNodeList, int counter) {
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
        Random random = java.util.concurrent.ThreadLocalRandom.current();
        while (iterations-- > 0) {
            int i1 = random.nextInt(G.getNodes().size()), i2;
            do {
                i2 = random.nextInt(G.getNodes().size());
            } while (i1 == i2);

            VarNodePosition N1 = varNodeList.get(i1);
            VarNodePosition N2 = varNodeList.get(i2);

            int oldX1 = N1.x();
            int oldY1 = N1.y();
            int oldX2 = N2.x();
            int oldY2 = N2.y();
            int newX1, newY1;
            int newX2, newY2;

            do {
                newX1 = random.nextInt(COL + 1);
                newY1 = random.nextInt(ROW + 1);
            } while (newX1 == oldX1 && newY1 == oldY1);

            do {
                newX2 = random.nextInt(COL + 1);
                newY2 = random.nextInt(ROW + 1);
            } while (newX2 == oldX2 && newY2 == oldY2);

            F.propagateOneNodeMove(N1, newX1, newY1);
            model.move(N1, newX1, newY1);
            LexMultiValues current = F.evaluateOneNodeMove(N2, newX2, newY2);
            if (current.better(values)) {
                Move move = new Move(N1, newX1, newY1, current);
                move.ins(N2, newX2, newY2);

                if (firstImprovement) {
                    moves.add(move);
                    F.propagateOneNodeMove(N1, oldX1, oldY1);
                    model.move(N1, oldX1, oldY1);
                    break;
                }
                moves.clear();
                moves.add(move);
                values = current;
            } else if (current.equals(values)) {
                Move move = new Move(N1, newX1, newY1, current);
                move.ins(N2, newX2, newY2);

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

class TwoRandomNeighborhood implements NeighborExplorer {
    private final int ROW, COL;
    private final Graph G;
    private final CBLSGPModel model;
    private final LexMultiFunctions F;
    private final List<VarNodePosition> varNodeList;
    private final int counter;

    public TwoRandomNeighborhood(int ROW, int COL, Graph G, CBLSGPModel model, LexMultiFunctions F,
            List<VarNodePosition> varNodeList, int counter) {
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
        Random random = java.util.concurrent.ThreadLocalRandom.current();
        while (iterations-- > 0) {
            int i1 = random.nextInt(G.getNodes().size()), i2;
            do {
                i2 = random.nextInt(G.getNodes().size());
            } while (i1 == i2);

            VarNodePosition N1 = varNodeList.get(i1);
            VarNodePosition N2 = varNodeList.get(i2);

            int oldX1 = N1.x();
            int oldY1 = N1.y();
            // int d1 = random.nextInt(2) + 1;
            int d1 = 3;

            int LX1 = Math.max(0, oldX1 - d1);
            int RX1 = Math.min(COL, oldX1 + d1);
            int LY1 = Math.max(0, oldY1 - d1);
            int RY1 = Math.min(ROW, oldY1 + d1);

            // int newX1 = random.nextInt(COL);
            // int newY1 = random.nextInt(ROW);
            // int newX2 = random.nextInt(COL);
            // int newY2 = random.nextInt(ROW);
            int newX1 = random.nextInt(RX1 - LX1 + 1) + LX1;
            int newY1 = random.nextInt(RY1 - LY1 + 1) + LY1;

            boolean found = false;
            // for (int newX1 = LX1; newX1 <= RX1 && !found; newX1++) {
            // for (int newY1 = LY1; newY1 <= RY1 && !found; newY1++) {
            // if (newX1 == oldX1 && newY1 == oldY1) continue;
            // int d2 = random.nextInt(2) + 1;
            int d2 = 3;
            int LX2 = Math.max(0, N2.x() - d2);
            int RX2 = Math.min(COL, N2.x() + d2);
            int LY2 = Math.max(0, N2.y() - d2);
            int RY2 = Math.min(ROW, N2.y() + d2);
            int newX2 = random.nextInt(RX2 - LX2 + 1) + LX2;
            int newY2 = random.nextInt(RY2 - LY2 + 1) + LY2;
            F.propagateOneNodeMove(N1, newX1, newY1);
            model.move(N1, newX1, newY1);

            // for (int newX2 = LX2; newX2 <= RX2 && !found; newX2++) {
            // for (int newY2 = LY2; newY2 <= RY2 && !found; newY2++) {
            // if (newX2 == N2.x() && newY2 == N2.y()) continue;
            LexMultiValues current = F.evaluateOneNodeMove(N2, newX2, newY2);
            if (current.better(values)) {
                Move move = new Move(N1, newX1, newY1, current);
                move.ins(N2, newX2, newY2);


                if (firstImprovement) {
                    moves.add(move);
                    F.propagateOneNodeMove(N1, oldX1, oldY1);
                    model.move(N1, oldX1, oldY1);
                    found = true;
                    break;
                }
                moves.clear();
                moves.add(move);
                values = current;
            } else if (current.equals(values)) {
                Move move = new Move(N1, newX1, newY1, current);
                move.ins(N2, newX2, newY2);


                moves.add(move);
            }

            // }
            // }

            F.propagateOneNodeMove(N1, oldX1, oldY1);
            model.move(N1, oldX1, oldY1);
            // }
            // }
            if (found)
                break;
        }

        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(random.nextInt(moves.size()));
    }
}

class TwoRandomSwap implements NeighborExplorer {
    private final Graph G;
    private final CBLSGPModel model;
    private final LexMultiFunctions F;
    private final List<VarNodePosition> varNodeList;
    private final int counter;

    public TwoRandomSwap(Graph G, CBLSGPModel model, LexMultiFunctions F, List<VarNodePosition> varNodeList,
            int counter) {
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
        Random random = java.util.concurrent.ThreadLocalRandom.current();
        while (iterations-- > 0) {
            int i1 = random.nextInt(G.getNodes().size()), i2;
            do {
                i2 = random.nextInt(G.getNodes().size());
            } while (i1 == i2);

            VarNodePosition N1 = varNodeList.get(i1);
            VarNodePosition N2 = varNodeList.get(i2);

            int oldX1 = N1.x(), oldY1 = N1.y();
            F.propagateOneNodeMove(N1, N2.x(), N2.y());
            model.move(N1, N2.x(), N2.y());

            LexMultiValues current = F.evaluateOneNodeMove(N2, oldX1, oldY1);

            if (current.better(values)) {
                Move move = new Move(N1, N2.x(), N2.y(), current);
                move.ins(N2, oldX1, oldY1);

                if (firstImprovement) {
                    F.propagateOneNodeMove(N1, oldX1, oldY1);
                    model.move(N1, oldX1, oldY1);

                    moves.add(move);
                    break;
                }
                moves.clear();
                moves.add(move);
                values = current;
            } else if (current.equals(values)) {
                Move move = new Move(N1, N2.x(), N2.y(), current);
                move.ins(N2, oldX1, oldY1);
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

class ThreeRandomExchange implements NeighborExplorer {
    private final Graph G;
    private final CBLSGPModel model;
    private final LexMultiFunctions F;
    private final List<VarNodePosition> varNodeList;
    private final int counter;

    public ThreeRandomExchange(Graph G, CBLSGPModel model, LexMultiFunctions F, List<VarNodePosition> varNodeList,
            int counter) {
        this.G = G;
        this.model = model;
        this.F = F;
        this.varNodeList = varNodeList;
        this.counter = counter;
    }

    @Override
    public Move explore(boolean firstImprovement) {
        if (G.getNodes().size() < 3)
            return null;
        List<Move> moves = new ArrayList<>();
        int iterations = counter;
        LexMultiValues values = F.evaluation();
        Random random = java.util.concurrent.ThreadLocalRandom.current();
        while (iterations-- > 0) {
            int i1 = random.nextInt(G.getNodes().size()), i2, i3;
            do {
                i2 = random.nextInt(G.getNodes().size());
            } while (i1 == i2);
            do {
                i3 = random.nextInt(G.getNodes().size());
            } while (i1 == i3 || i2 == i3);

            VarNodePosition N1 = varNodeList.get(i1);
            VarNodePosition N2 = varNodeList.get(i2);
            VarNodePosition N3 = varNodeList.get(i3);

            int oldX1 = N1.x(), oldY1 = N1.y();
            int oldX2 = N2.x(), oldY2 = N2.y();
            F.propagateOneNodeMove(N1, N2.x(), N2.y());
            model.move(N1, N2.x(), N2.y());
            F.propagateOneNodeMove(N2, N3.x(), N3.y());
            model.move(N2, N3.x(), N3.y());

            LexMultiValues current = F.evaluateOneNodeMove(N3, oldX1, oldY1);

            if (current.better(values)) {
                Move move = new Move(N1, oldX2, oldY2, current);
                move.ins(N2, N3.x(), N3.y());
                move.ins(N3, oldX1, oldY1);

                if (firstImprovement) {
                    moves.add(move);
                    F.propagateOneNodeMove(N1, oldX1, oldY1);
                    model.move(N1, oldX1, oldY1);
                    F.propagateOneNodeMove(N2, oldX2, oldY2);
                    model.move(N2, oldX2, oldY2);
                    break;
                }
                moves.clear();
                moves.add(move);
                values = current;
            } else if (current.equals(values)) {
                Move move = new Move(N1, oldX2, oldY2, current);
                move.ins(N2, N3.x(), N3.y());
                move.ins(N3, oldX1, oldY1);

                moves.add(move);
            }

            F.propagateOneNodeMove(N1, oldX1, oldY1);
            model.move(N1, oldX1, oldY1);
            F.propagateOneNodeMove(N2, oldX2, oldY2);
            model.move(N2, oldX2, oldY2);
        }

        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(random.nextInt(moves.size()));
    }
}

class AnyNode implements NeighborExplorer {
    private final int ROW, COL;
    @SuppressWarnings("unused")
    private final Graph G;
    @SuppressWarnings("unused")
    private final CBLSGPModel model;
    private final LexMultiFunctions F;
    private final List<VarNodePosition> varNodeList;
    private final int counter;

    public AnyNode(int ROW, int COL, Graph G, CBLSGPModel model, LexMultiFunctions F, List<VarNodePosition> varNodeList,
            int counter) {
        this.COL = COL;
        this.ROW = ROW;
        this.G = G;
        this.model = model;
        this.F = F;
        this.varNodeList = varNodeList;
        this.counter = counter;
    }

    @Override
    public Move explore(boolean firstImprovement) {
        List<Move> moves = new ArrayList<>();
        LexMultiValues values = F.evaluation();
        VarNodePosition node = varNodeList
                .get(java.util.concurrent.ThreadLocalRandom.current().nextInt(varNodeList.size()));
        int oldX = node.x(), oldY = node.y();
        int iteration = 0;
        java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();
        while (iteration < counter) {
            int newX = random.nextInt(COL + 1);
            int newY = random.nextInt(ROW + 1);
            if (newX == oldX && newY == oldY)
                continue;
            LexMultiValues current = F.evaluateOneNodeMove(node, newX, newY);
            if (current.better(values)) {
                Move move = new Move(node, newX, newY, current);
                if (firstImprovement) {
                    moves.add(move);
                    return move;
                }
                moves.clear();
                moves.add(move);
                values = current;
            } else if (current.equals(values)) {
                Move move = new Move(node, newX, newY, current);
                moves.add(move);
            }
            iteration++;
        }

        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(moves.size()));
    }
}

class MinRegretNode implements NeighborExplorer {
    private final int ROW, COL;
    @SuppressWarnings("unused")
    private final Graph G;
    @SuppressWarnings("unused")
    private final CBLSGPModel model;
    private final LexMultiFunctions F;
    private final List<VarNodePosition> varNodeList;
    private final int counter;

    public MinRegretNode(int ROW, int COL, Graph G, CBLSGPModel model, LexMultiFunctions F,
            List<VarNodePosition> varNodeList, int counter) {

        this.COL = COL;
        this.ROW = ROW;
        this.G = G;
        this.model = model;
        this.F = F;
        this.varNodeList = varNodeList;
        this.counter = counter;
    }

    @Override
    public Move explore(boolean firstImprovement) {
        List<Move> moves = new ArrayList<>();
        LexMultiValues values = F.evaluation();
        List<Pair<LexMultiValues, VarNodePosition>> nodeValues = new ArrayList<>();
        for (VarNodePosition N : varNodeList) {
            LexMultiValues current = F.evaluateOneNodeMove(N, -1, -1);
            // if (worseValues == null || worseValues.better(current)) {
            nodeValues.add(new Pair<>(current, N));
        }

        Collections.sort(nodeValues, (a, b) -> a.a.better(b.a) ? -1 : a.a.equals(b.a) ? 0 : 1);
        java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();
        for (int idx = 0; idx < Math.min(2, nodeValues.size()); idx++) {
            VarNodePosition node = nodeValues.get(idx).b;
            int oldX = node.x(), oldY = node.y();
            int iteration = 0;
            while (iteration < counter) {
                int newX = random.nextInt(COL + 1);
                int newY = random.nextInt(ROW + 1);
                if (newX == oldX && newY == oldY)
                    continue;
                LexMultiValues current = F.evaluateOneNodeMove(node, newX, newY);
                if (current.better(values)) {
                    Move move = new Move(node, newX, newY, current);
                    if (firstImprovement) {
                        moves.add(move);
                        return move;
                    }
                    moves.clear();
                    moves.add(move);
                    values = current;
                } else if (current.equals(values)) {
                    Move move = new Move(node, newX, newY, current);
                    moves.add(move);
                }
                iteration++;
            }
        }

        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(moves.size()));
    }
}

class CentroidMove implements NeighborExplorer {
    private final int ROW, COL;
    private final Graph G;
    private final CBLSGPModel model;
    private final LexMultiFunctions F;
    private final List<VarNodePosition> varNodeList;
    private final int counter;

    public CentroidMove(int ROW, int COL, Graph G, CBLSGPModel model, LexMultiFunctions F,
            List<VarNodePosition> varNodeList, int counter) {
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
        LexMultiValues values = F.evaluation();
        java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();
        int iterations = counter;
        while (iterations-- > 0) {
            int i = random.nextInt(varNodeList.size());
            VarNodePosition varNodePosition = varNodeList.get(i);
            Node N = G.getNode(varNodePosition.id);
            List<Edge> adjEdges = G.getEdges(N);
            if (adjEdges == null || adjEdges.isEmpty())
                continue;

            double sumX = 0, sumY = 0;
            for (Edge e : adjEdges) {
                Node neighbor = e.getRemaining(N);
                VarNodePosition nPos = model.getVarNode(neighbor.id);
                sumX += nPos.x();
                sumY += nPos.y();
            }
            int targetX = (int) Math.round(sumX / adjEdges.size());
            int targetY = (int) Math.round(sumY / adjEdges.size());

            int newX = Math.max(0, Math.min(COL, targetX + random.nextInt(3) - 1));
            int newY = Math.max(0, Math.min(ROW, targetY + random.nextInt(3) - 1));

            if (newX == varNodePosition.x() && newY == varNodePosition.y())
                continue;

            LexMultiValues current = F.evaluateOneNodeMove(varNodePosition, newX, newY);
            if (current.better(values)) {
                Move move = new Move(varNodePosition, newX, newY, current);
                if (firstImprovement) {
                    moves.add(move);
                    break;
                }
                moves.clear();
                moves.add(move);
                values = current;
            } else if (current.equals(values)) {
                Move move = new Move(varNodePosition, newX, newY, current);
                moves.add(move);
            }
        }
        if (moves.isEmpty())
            return null;
        return moves.get(random.nextInt(moves.size()));
    }
}

class NeighborSwap implements NeighborExplorer {
    private final Graph G;
    private final CBLSGPModel model;
    private final LexMultiFunctions F;
    private final int counter;
    private final List<Edge> allEdges;

    public NeighborSwap(Graph G, CBLSGPModel model, LexMultiFunctions F, int counter) {
        this.G = G;
        this.model = model;
        this.F = F;
        this.counter = counter;
        this.allEdges = G.getEdges();
    }

    @Override
    public Move explore(boolean firstImprovement) {
        if (allEdges == null || allEdges.isEmpty())
            return null;
        List<Move> moves = new ArrayList<>();
        int iterations = counter;
        LexMultiValues values = F.evaluation();
        java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();
        while (iterations-- > 0) {
            Edge e = allEdges.get(random.nextInt(allEdges.size()));
            VarNodePosition N1 = model.getVarNode(e.fromNode.id);
            VarNodePosition N2 = model.getVarNode(e.toNode.id);

            int oldX1 = N1.x(), oldY1 = N1.y();
            F.propagateOneNodeMove(N1, N2.x(), N2.y());
            model.move(N1, N2.x(), N2.y());

            LexMultiValues current = F.evaluateOneNodeMove(N2, oldX1, oldY1);
            if (current.better(values)) {
                Move move = new Move(N1, N2.x(), N2.y(), current);
                move.ins(N2, oldX1, oldY1);


                if (firstImprovement) {
                    F.propagateOneNodeMove(N1, oldX1, oldY1);
                    model.move(N1, oldX1, oldY1);
                    moves.add(move);
                    break;
                }
                moves.clear();
                moves.add(move);
                values = current;
            } else if (current.equals(values)) {
                Move move = new Move(N1, N2.x(), N2.y(), current);
                move.ins(N2, oldX1, oldY1);
                moves.add(move);
            }

            F.propagateOneNodeMove(N1, oldX1, oldY1);
            model.move(N1, oldX1, oldY1);
        }

        if (moves.isEmpty())
            return null;
        return moves.get(random.nextInt(moves.size()));
    }
}

class ConflictNodeMove implements NeighborExplorer {
    private final int ROW, COL;
    private final LexMultiFunctions F;
    private final NumberIntersectionEdges intersectionFunc;
    private final int counter;

    public ConflictNodeMove(int ROW, int COL, LexMultiFunctions F, NumberIntersectionEdges intersectionFunc, int counter) {
        this.ROW = ROW;
        this.COL = COL;
        this.F = F;
        this.intersectionFunc = intersectionFunc;
        this.counter = counter;
    }

    @Override
    public Move explore(boolean firstImprovement) {
        List<VarNodePosition> conflictNodes = intersectionFunc.getConflictNodes();
        if (conflictNodes.isEmpty()) return null; 

        List<Move> moves = new ArrayList<>();
        int iterations = counter;
        LexMultiValues values = F.evaluation();
        java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();

        while (iterations-- > 0) {
            VarNodePosition node = conflictNodes.get(random.nextInt(conflictNodes.size()));
            int oldX = node.x(), oldY = node.y();
            int newX, newY;
            do {
                newX = random.nextInt(COL + 1);
                newY = random.nextInt(ROW + 1);
            } while (newX == oldX && newY == oldY);

            LexMultiValues current = F.evaluateOneNodeMove(node, newX, newY);
            if (current.better(values)) {
                Move move = new Move(node, newX, newY, current);
                if (firstImprovement) return move;
                moves.clear();
                moves.add(move);
                values = current;
            } else if (current.equals(values)) {
                moves.add(new Move(node, newX, newY, current));
            }
        }

        if (moves.isEmpty()) return null;
        return moves.get(random.nextInt(moves.size()));
    }
}

class ConflictGridSearch implements NeighborExplorer {
    private final int ROW, COL;
    private final LexMultiFunctions F;
    private final NumberIntersectionEdges intersectionFunc;

    public ConflictGridSearch(int ROW, int COL, LexMultiFunctions F, NumberIntersectionEdges intersectionFunc) {
        this.ROW = ROW;
        this.COL = COL;
        this.F = F;
        this.intersectionFunc = intersectionFunc;
    }

    @Override
    public Move explore(boolean firstImprovement) {
        List<VarNodePosition> conflictNodes = intersectionFunc.getConflictNodes();
        if (conflictNodes.isEmpty()) return null;

        LexMultiValues values = F.evaluation();
        Move bestMove = null;
        java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();

        VarNodePosition node = conflictNodes.get(random.nextInt(conflictNodes.size()));
        int stepX = Math.max(1, COL / 40);
        int stepY = Math.max(1, ROW / 40);
        for (int newX = 0; newX <= COL; newX += stepX) {
            for (int newY = 0; newY <= ROW; newY += stepY) {
                if (newX == node.x() && newY == node.y()) continue;
                LexMultiValues current = F.evaluateOneNodeMove(node, newX, newY);
                if (current.better(values)) {
                    Move move = new Move(node, newX, newY, current);
                    if (firstImprovement) return move;
                    bestMove = move;
                    values = current; 
                }
            }
        }
        return bestMove;
    }
}
public class Main {
    static final int numDirections = 8;
    static final Integer[] dirX = { 0, 1, 0, -1, 1, 1, -1, -1 };
    static final Integer[] dirY = { 1, 0, -1, 0, 1, -1, -1, 1 };

    public static void generateInitialSolution(int ROW, int COL, CBLSGPModel model, Graph G,
            List<DoubleLinkedList<Edge>> adj) {
        int n = G.getNodes().size();
        List<List<Integer>> radius = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            radius.add(new ArrayList<>());
        }
        for (Node node : G.getNodes()) {
            List<Integer> dist = G.bfs(node, adj);
            // System.err.println("Node " + Collections.max(dist) + ": " + dist);
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
                    // System.err.println(center.x() + " " + center.y() + " " + polar + " " + i + "
                    // " + j + " " + Math.cos(polar) + " " + Math.sin(polar));
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

    public static void generateInitialSolution1(int ROW, int COL, CBLSGPModel model, Graph G,
            List<DoubleLinkedList<Edge>> adj, LexMultiFunctions F) {
        int n = G.getNodes().size();
        List<Integer> nodes = new ArrayList<>();
        for (Node node : G.getNodes()) {
            nodes.add(node.id);
        }
        Collections.shuffle(nodes, java.util.concurrent.ThreadLocalRandom.current());
        for (int i = 0; i < n; i++) {
            int id = nodes.get(i);
            VarNodePosition v = model.getVarNode(id);
            if (i == 0) {
                if (n < 2) {
                    F.propagateOneNodeMove(v, COL >> 1, ROW >> 1);
                    model.move(v, COL >> 1, ROW >> 1);
                } else {
                    F.propagateOneNodeMove(v, 0, 0);
                    model.move(v, 0, 0);
                }
                continue;
            }
            if (i == 1) {
                F.propagateOneNodeMove(v, COL, ROW);
                model.move(v, COL, ROW);
                continue;
            }
            int bestX = v.x(), bestY = v.y();
            LexMultiValues bestValues = F.evaluation();
            for (int x = 0; x <= COL; x++) {
                for (int y = 0; y <= ROW; y++) {
                    LexMultiValues current = F.evaluateOneNodeMove(v, x, y);
                    if (current.better(bestValues)) {
                        bestValues = current;
                        bestX = x;
                        bestY = y;
                    }
                }
            }
            F.propagateOneNodeMove(v, bestX, bestY);
            model.move(v, bestX, bestY);
        }
    }

    public static String dir;

    @SuppressWarnings("unused")
    public static void test1(int tt, int timeLimit, int maxIterations) throws IOException {
        // timeLimit is in milliseconds
        long startTime = System.currentTimeMillis();
        Kattio io = new Kattio("tests/" + tt + ".in", dir + "/" + tt + ".out");
        System.err.println("test " + tt + " running...");

        // int ROW = 20;
        // int COL = 20; // the graph is presented on a grid ROW x COL
        int ROW = io.nextInt();
        int COL = io.nextInt(); // the graph is presented on a grid ROW x COL
        ROW *= 4;
        COL *= 4;
        // int n = 20;// number of nodes 0,1, 2, ..., n-1
        int n = io.nextInt(); // number of nodes 0,1, 2, ..., n-1
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            nodes.add(new Node(i));
        }
        Graph G = new Graph(nodes);
        int n_edges = io.nextInt(); // number of edges
        List<DoubleLinkedList<Edge>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new DoubleLinkedList<>());
        }
        List<Pair<Integer, Integer>> edges = new ArrayList<>();
        int off_set = 0;
        for (int i = 0; i < n_edges; i++) {
            int u = io.nextInt() - 1;
            int v = io.nextInt() - 1;
            if (u < 0 || v < 0) off_set = 1;
            edges.add(new Pair<>(u, v));
        }
        for (Pair<Integer, Integer> edge : edges) {
            edge.a += off_set;
            edge.b += off_set;
            int u = edge.a;
            int v = edge.b;
            // System.err.println(u + " " + v);
            // int u = io.nextInt();
            // int v = io.nextInt();
            // if (u == v) continue; // skip self-loops

            io.printf("(%d, %d), ", u, v);

            Node fromNode = G.getNode(u);
            Node toNode = G.getNode(v);
            if (fromNode == null || toNode == null)
                continue;

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

        for (int i = 0; i <= COL; i++)
            DX.add(i);
        for (int i = 0; i <= ROW; i++)
            DY.add(i);
        // varPos.get(node) is the coordinate of node, to be optimized, so that G is
        // displayed nicely on the plane
        for (int i = 0; i < n; i++) {
            varPos.put(nodes.get(i), new VarNodePosition(i, DX, DY));
            varPosList.add(varPos.get(nodes.get(i)));
        }

        CBLSGPModel model = new CBLSGPModel();
        // Random rnd = new Random(23480329);
        for (Node node : G.getNodes()) {
            VarNodePosition v = varPos.get(node);
            model.addVarNode(v);
            // model.move(v, rnd.nextInt(COL + 1), rnd.nextInt(ROW + 1));
            // F.propagateOneNodeMove(v, v.x(), v.y());
        }
        List<Integer> X = new ArrayList<>(), Y = new ArrayList<>();
        int check = 0;
        try {
            check = io.nextInt();
            check = 0;
            if (check == 1) {
                for (int i = 0; i < n; i++) {
                    int x = io.nextInt() * 2;
                    int y = io.nextInt() * 4;
                    VarNodePosition v = varPos.get(nodes.get(i));
                    model.move(v, x, y);
                    X.add(x);
                    Y.add(y);
                }
            } else {
                generateInitialSolution(ROW, COL, model, G, adj);
            }
        } catch (Exception e) {
            generateInitialSolution(ROW, COL, model, G, adj);
        }

        NumberIntersectionEdges F3 = new NumberIntersectionEdges(G, varPos);// to be minimized
        // EdgeLengthVariance F5 = new EdgeLengthVariance(G,varPos);// to be minimized
        // NodePosVariance F6 = new NodePosVariance(G,varPos);// to be minimized
        MinAngle F2 = new MinAngle(G, varPos);// to be maximized
        // SumAngle F2a = new SumAngle(G,varPos);// to be maximized
        MinDistanceEdge F1 = new MinDistanceEdge(G, varPos);// to be maximized
        // SumDistanceEdge F1a = new SumDistanceEdge(G,varPos);// to be maximized
        MinDistanceNodeEdge F4 = new MinDistanceNodeEdge(G, varPos);// to be maximized
        // SumDistanceNodeEdge F4a = new SumDistanceNodeEdge(G,varPos);// to be
        // maximized
        // MinDistance2Nodes F7 = new MinDistance2Nodes(varPos);// to be maximized

        // LexMultiFunctions F0 = new LexMultiFunctions();
        // F0.add(F3);
        LexMultiFunctions F = new LexMultiFunctions();
        F.add(F3); // NumberOfIntersectionEdges
        F.add(F4); // MinDistanceNodeEdge
        F.add(F2); // MinAngle
        F.add(F1); // MinDistanceEdge
        // F.add(F2a); // SumAngle
        // F.add(F5); // EdgeLengthVariance
        // F.add(F7); // MinDistance2Nodes
        // F.add(F6); // NodePosVariance
        // F.add(F4a); // SumDistanceNodeEdge
        // F.add(F1a); // SumDistanceEdge

        model.close();

        // for (Node node : G.getNodes()) {
        // VarNodePosition v = varPos.get(node);
        // System.err.printf("%d: (%d, %d)\n", node.id, v.x(), v.y());
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
            str.append("\n");
        }

        boolean firstImprovement = true;
        final int numRandom = 5000;
        List<NeighborExplorer> macroExplorers = new ArrayList<>();
        // macroExplorers.add(new CentroidMove(ROW, COL, G, model, F, varPosList, numRandom));
        // macroExplorers.add(new NeighborSwap(G, model, F, numRandom));
        macroExplorers.add(new ConflictGridSearch(ROW, COL, F, F3));
        macroExplorers.add(new ConflictNodeMove(ROW, COL, F, F3, numRandom));
        macroExplorers.add(new OneRandomMove(ROW, COL, G, F, varPosList, numRandom));
        macroExplorers.add(new OneRandomCOL(COL, G, F, varPosList, numRandom));
        macroExplorers.add(new OneRandomROW(ROW, G, F, varPosList, numRandom));
        macroExplorers.add(new TwoRandomNeighborhood(ROW, COL, G, model, F, varPosList, numRandom));
        macroExplorers.add(new TwoRandomMove(ROW, COL, G, model, F, varPosList, numRandom));
        macroExplorers.add(new OneRandomNeighborhood(ROW, COL, G, F, varPosList, numRandom));
        macroExplorers.add(new OneRandomNeighborhoodCOL(COL, F, varPosList, numRandom));
        macroExplorers.add(new OneRandomNeighborhoodROW(ROW, F, varPosList, numRandom));
        macroExplorers.add(new TwoRandomSwap(G, model, F, varPosList, numRandom / 2));
        macroExplorers.add(new ThreeRandomExchange(G, model, F, varPosList, numRandom / 2));
        macroExplorers.add(new MinRegretNode(ROW, COL, G, model, F, varPosList, numRandom / 10)); 
        List<NeighborExplorer> microExplorers = new ArrayList<>();

        // microExplorers.add(new OneRandomNeighborhood(ROW, COL, G, F, varPosList, numRandom));
        // microExplorers.add(new OneRandomNeighborhoodCOL(COL, F, varPosList, numRandom));
        // microExplorers.add(new OneRandomNeighborhoodROW(ROW, F, varPosList, numRandom));
        // microExplorers.add(new MinRegretNode(ROW, COL, G, model, F, varPosList, numRandom / 2)); 

        List<NeighborExplorer> perturbationExplorers = new ArrayList<>();
        // perturbationExplorers.add(new TwoRandomSwap(G, model, F, varPosList, numRandom / 2));
        // perturbationExplorers.add(new ThreeRandomExchange(G, model, F, varPosList, numRandom / 2));

        //final int maxIterations = 10000;
        int stagnantCount = 0;    
        int maxStagnant = 300;     

        for (int iter = 0; iter < maxIterations; iter++) {
            if(System.currentTimeMillis() - startTime > timeLimit) { //timeout check
                break;
            }
            Move selectedMove = null;

            if (stagnantCount >= maxStagnant) {
                for (NeighborExplorer explorer : perturbationExplorers) {
                    Move move = explorer.explore(firstImprovement);
                    if (move != null && (selectedMove == null || move.better(selectedMove))) {
                        selectedMove = move;
                    }
                }
                
                if (selectedMove == null) {
                    Random rnd = java.util.concurrent.ThreadLocalRandom.current();
                    List<VarNodePosition> conflictNodes = F3.getConflictNodes();
                    if (!conflictNodes.isEmpty()) {
                        int numPerturb = Math.max(1, Math.min(n / 3, conflictNodes.size()));
                        Collections.shuffle(conflictNodes);
                        
                        for (int i = 0; i < numPerturb; i++) {
                            VarNodePosition v = conflictNodes.get(i);
                            int newX = rnd.nextInt(COL + 1);
                            int newY = rnd.nextInt(ROW + 1);
                            F.propagateOneNodeMove(v, newX, newY);
                            model.move(v, newX, newY);
                        }
                    } else {
                        int numPerturb = Math.max(1, n / 3);
                        for (int i = 0; i < numPerturb; i++) {
                            VarNodePosition v = varPosList.get(rnd.nextInt(n));
                            int newX = rnd.nextInt(COL + 1);
                            int newY = rnd.nextInt(ROW + 1);
                            F.propagateOneNodeMove(v, newX, newY);
                            model.move(v, newX, newY);
                        }
                    }
                    // for (int i = 0; i < n; i++) {
                    //     VarNodePosition v = varPosList.get(i);
                    //     int newX = rnd.nextInt(COL + 1);
                    //     int newY = rnd.nextInt(ROW + 1);
                    //     F.propagateOneNodeMove(v, newX, newY);
                    //     model.move(v, newX, newY);
                    // }
                    stagnantCount = 0;
                    continue; 
                }
            } 
            else {
                for (NeighborExplorer explorer : microExplorers) {
                    Move move = explorer.explore(firstImprovement);
                    if (move != null && (selectedMove == null || move.better(selectedMove))) {
                        selectedMove = move;
                        if (firstImprovement) break; 
                    }
                }
                
                if (selectedMove == null) {
                    for (NeighborExplorer explorer : macroExplorers) {
                        Move move = explorer.explore(firstImprovement);
                        if (move != null && (selectedMove == null || move.better(selectedMove))) {
                            selectedMove = move;
                            if (firstImprovement) break;
                        }
                    }
                }
            }

            if (selectedMove != null) {
                for (MoveNode moveNode : selectedMove.moves) {
                    VarNodePosition pos = moveNode.varNodePosition;
                    F.propagateOneNodeMove(pos, moveNode.newX, moveNode.newY);
                    model.move(pos, moveNode.newX, moveNode.newY);
                }
                
                if (selectedMove.better(solutionEval)) {
                    solutionEval = selectedMove.values;
                    model.setNodePositionsValue(solutionPositions);
                    stagnantCount = 0; 
                    
                    str.setLength(0);
                    for (Function f : F.F) {
                        str.append(f).append("\n");
                    }
                    System.err.println(str.toString());
                    System.out.println("Step " + iter + "(" + (System.currentTimeMillis() - startTime) + " ms): " + F.toString());
                } else {
                    stagnantCount++;
                }
            } else {
                stagnantCount++; 
            }
        }
        // List<NeighborExplorer> explorers = new ArrayList<>();
        // explorers.add(new CentroidMove(ROW, COL, G, model, F, varPosList, numRandom));
        // explorers.add(new NeighborSwap(G, model, F, numRandom));
        // explorers.add(new MinRegretNode(ROW, COL, G, model, F, varPosList, numRandom));
        // explorers.add(new AnyNode(ROW, COL, G, model, F, varPosList, numRandom * 2));
        // explorers.add(new OneRandomNeighborhood(ROW, COL, G, F, varPosList, numRandom));
        // explorers.add(new OneRandomMove(ROW, COL, G, F, varPosList, numRandom));
        // explorers.add(new OneRandomNeighborhoodCOL(COL, F, varPosList, numRandom));
        // explorers.add(new OneRandomNeighborhoodROW(ROW, F, varPosList, numRandom));
        // explorers.add(new TwoRandomNeighborhood(ROW, COL, G, model, F, varPosList, numRandom));
        // explorers.add(new TwoRandomMove(ROW, COL, G, model, F, varPosList, numRandom));
        // explorers.add(new OneRandomCOL(COL, G, F, varPosList, numRandom));
        // explorers.add(new OneRandomROW(ROW, G, F, varPosList, numRandom));
        // explorers.add(new TwoRandomSwap(G, model, F, varPosList, numRandom));
        // explorers.add(new ThreeRandomExchange(G, model, F, varPosList, numRandom));
        // final int maxIterations = 2000;
        // // final int maxIterations = 0;
        // for (int iter = 0; iter < maxIterations; iter++) {
        //     Move selectedMove = null;
        //     for (int k = 0; k < explorers.size(); k++) {
        //         Move move = explorers.get(k).explore(firstImprovement);
        //         if (move != null) {
        //             if (firstImprovement) {
        //                 selectedMove = move;
        //                 break;
        //             } else {
        //                 if (move.better(selectedMove)) {
        //                     selectedMove = move;
        //                 }
        //             }
        //         }
        //     }
        //     if (selectedMove != null) {
        //         // System.err.println("iter " + iter + ": " + selectedMove);
        //         for (MoveNode moveNode : selectedMove.moves) {
        //             VarNodePosition pos = moveNode.varNodePosition;
        //             int newX = moveNode.newX;
        //             int newY = moveNode.newY;
        //             // System.err.println(pos.id + ": (" + pos.x() + "->" + newX + ", " + pos.y() +
        //             // "->" + newY + ")");
        //             F.propagateOneNodeMove(pos, newX, newY);
        //             model.move(pos, newX, newY);
        //         }
        //         // for (Function f : F.F) {
        //         // System.err.print(f + " ");
        //         // }
        //         // System.err.println();
        //         if (selectedMove.better(solutionEval)) {
        //             solutionEval = selectedMove.values;
        //             str.setLength(0);
        //             for (Function f : F.F) {
        //                 str.append(f);
        //                 str.append("\n");
        //             }
        //             model.setNodePositionsValue(solutionPositions);
        //         }
        //     } else {
        //         // generateInitialSolution1(ROW, COL, model, G, adj, F0);
        //         Random rnd = java.util.concurrent.ThreadLocalRandom.current();
        //         // for (int i = 0; i < n; i++) {
        //         // VarNodePosition v = varPos.get(nodes.get(i));
        //         // int newX = rnd.nextInt(COL + 1);
        //         // int newY = rnd.nextInt(ROW + 1);
        //         // F.propagateOneNodeMove(v, newX, newY);
        //         // model.move(v, newX, newY);
        //         // }
        //         // if (check == 1) {
        //         if (false) {
        //             if (rnd.nextInt(2) == 1) {
        //                 for (int i = 0; i < n; i++) {
        //                     X.set(i, COL - X.get(i));
        //                 }
        //             }
        //             if (rnd.nextInt(2) == 0) {
        //                 for (int i = 0; i < n; i++) {
        //                     Y.set(i, ROW - Y.get(i));
        //                 }
        //             }
        //             if (rnd.nextInt(2) == 1) {
        //                 for (int i = 0; i < n; i++) {
        //                     int t = X.get(i);
        //                     X.set(i, Y.get(i));
        //                     Y.set(i, t);
        //                 }
        //             }
        //             for (int i = 0; i < n; i++) {
        //                 VarNodePosition v = varPos.get(nodes.get(i));
        //                 int newX = X.get(i);
        //                 int newY = Y.get(i);
        //                 F.propagateOneNodeMove(v, newX, newY);
        //                 model.move(v, newX, newY);
        //             }
        //         } else {
        //             for (int i = 0; i < n; i++) {
        //                 VarNodePosition v = varPos.get(nodes.get(i));
        //                 int newX = rnd.nextInt(COL + 1);
        //                 int newY = rnd.nextInt(ROW + 1);
        //                 F.propagateOneNodeMove(v, newX, newY);
        //                 model.move(v, newX, newY);
        //             }
        //         }
        //         // break;
        //     }
        // }


        io.printf("%s\n%s\n\n", str, solutionEval);
        for (NodePosition pos : solutionPositions) {
            // System.err.printf("%d: (%d, %d),\n", pos.id(), pos.x(), pos.y());
            io.printf("%d: (%d, %d),\n", pos.id(), pos.x(), pos.y());
        }

        long endTime = System.currentTimeMillis();
        io.printf("\nTime taken: %.3f s\n", (endTime - startTime) / 1000.);

        for (NodePosition pos : solutionPositions) {
            // System.err.printf("%d %d ", pos.x(), pos.y());
            io.printf("%d %d ", pos.x(), pos.y());
        }
        io.println();
        for (NodePosition pos : solutionPositions) {
            io.printf("%d: (%d, %d), ", pos.id(), pos.x(), pos.y());
        }
        io.println();

        System.err.printf("Time taken: %.3f s\n", (endTime - startTime) / 1000.);

        {
            for (int i = 0; i < n; i++) {
                VarNodePosition pos = varPos.get(nodes.get(i));
                NodePosition pos1 = solutionPositions.get(i);
                pos.assign(pos1.x(), pos1.y());
            }
            // NumberIntersectionEdges f3 = new NumberIntersectionEdges(G, varPos);// to be
            // minimized
            // MinAngle f2 = new MinAngle(G, varPos);// to be maximized
            // MinDistanceEdge f1 = new MinDistanceEdge(G, varPos);// to be maximized
            // MinDistanceNodeEdge f4 = new MinDistanceNodeEdge(G, varPos);// to be
            // maximized
            // Function fObj = new ObjectiveFunction(f3, f2, f1, f4, 100.0, 200.0, 1.0, 4);
            // System.err.println(fObj);
            NumberIntersectionEdges f3 = new NumberIntersectionEdges(G, varPos);// to be minimized
            MinAngle f2 = new MinAngle(G, varPos);// to be maximized
            // SumAngle f2a = new SumAngle(G, varPos);// to be maximized
            MinDistanceEdge f1 = new MinDistanceEdge(G, varPos);// to be maximized
            // SumDistanceEdge f1a = new SumDistanceEdge(G, varPos);// to be maximized
            MinDistanceNodeEdge f4 = new MinDistanceNodeEdge(G, varPos);// to be maximized
            // SumDistanceNodeEdge f4a = new SumDistanceNodeEdge(G, varPos);// to be
            // maximized
            // System.err.printf("%s%s%s%s%s%s%s\n", f3, f2, f2a, f1, f1a, f4, f4a);
            // io.printf("%s%s%s%s%s%s%s\n", f3, f2, f2a, f1, f1a, f4, f4a);
            io.printf("%s%s%s%s\n", f3, f2, f1, f4);
        }

        io.close();
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        try {
            // noinspection StatementWithEmptyBody,ConstantValue
            if (true) {
                // if (false) {
                long startTime = System.currentTimeMillis();
                dir = "tests/" + startTime;
                File newDirectory = new File(dir);
                if (newDirectory.mkdir()) {
                    System.out.println("Directory: " + dir + " created");
                    // for (int i = 5; i < 10; i++) test1(i);
                    // Integer[] tests = {5, 6, 7, 8, 9};
                    // Integer[] tests = {10, 11, 12, 13, 14};
                    // Integer[] tests = { 16 };
                    // Integer[] tests = {17, 18, 19};
                    //Integer[] tests = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21 };
                    Integer[] tests = {0};
                    for (int i : tests)
                        test1(i,100000,100000);
                } else {
                    System.out.println("Directory: " + dir + " not created");
                }
            } else {
                // planarGen();
                // connectedGen();
                // completeGen();
                // circleGen();
                // completeBipartiteGen();
                // wheelGen();
                // ncubesGen();
                // gridGen();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

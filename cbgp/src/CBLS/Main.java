package CBLS;


import javax.swing.text.Segment;
import java.util.*;

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

    public double getY() {
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
    public void addEdge(Node u, Node v){
        Edge e = new Edge(edgeId++, u, v);
        A.get(u).add(e);
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
}

class CBLSGPModel{
    List<VarNodePosition> varNodePositions;
    List<Function> F;

    // additional data structures defined here to efficiently perform the move
    
    public CBLSGPModel() {
        varNodePositions = new ArrayList<>();
        F = new ArrayList<>();
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

class MinDistanceEdge implements Function{
//    double minDistance;
    Graph g;
    List<List<Edge>> adj;
    Map<Node, VarNodePosition> positions;
    Map<Integer, Double> distances;
//    Queue<Pair<Double, Integer>> pq;
//    TreeSet<Pair<Double, Integer>> pq;
    TreeMultiset<Double> pq;

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


class MinAngle implements Function {
    Graph g;
    List<List<Edge>> adj;
    Map<Node, VarNodePosition> positions;
    Map<Node, TreeSet<NodeAngle>> nodeNeighbors = new HashMap<>();
    // TreeMultiset<Double> allAngles = new TreeMultiset<>();
    TreeMultiset<Double> allAngles = new TreeMultiset<>(new DoubleCompare());
    Map<Integer, Map<Integer, NodeAngle>> nodeAngleMap = new HashMap<>();
    double minAngleValue = Double.POSITIVE_INFINITY;

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
        System.err.println(totalIntersections);
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
            if (a > b) return true;
            if (a < b) return false;
        }
        return false;
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

public class Main {
    public static void test1(){
        int ROW = 20;
        int COL = 20; // the graph is presented on a grid ROW x COL
        int n  = 20;// number of nodes 0,1, 2, ..., n-1
        List<Node> nodes = new ArrayList<>();
        for(int i = 0; i < n; i++){
            nodes.add(new Node(i));
        }
        Graph G = new Graph(nodes);
//        G.addEdge(nodes.get(0),nodes.get(1));
//        G.addEdge(nodes.get(0),nodes.get(2));
//        G.addEdge(nodes.get(0),nodes.get(4));
//        G.addEdge(nodes.get(1),nodes.get(3));
//        G.addEdge(nodes.get(1),nodes.get(4));
//        G.addEdge(nodes.get(2),nodes.get(4));
//        G.addEdge(nodes.get(1),nodes.get(0));
//        G.addEdge(nodes.get(2),nodes.get(0));
//        G.addEdge(nodes.get(4),nodes.get(0));
//        G.addEdge(nodes.get(3),nodes.get(1));
//        G.addEdge(nodes.get(4),nodes.get(1));
//        G.addEdge(nodes.get(4),nodes.get(2));

//        G.addEdge(nodes.get(1), nodes.get(2));
//        G.addEdge(nodes.get(2), nodes.get(1));
//        G.addEdge(nodes.get(1), nodes.get(6));
//        G.addEdge(nodes.get(6), nodes.get(1));
//        G.addEdge(nodes.get(2), nodes.get(3));
//        G.addEdge(nodes.get(3), nodes.get(2));
//        G.addEdge(nodes.get(2), nodes.get(7));
//        G.addEdge(nodes.get(7), nodes.get(2));
//        G.addEdge(nodes.get(3), nodes.get(4));
//        G.addEdge(nodes.get(4), nodes.get(3));
//        G.addEdge(nodes.get(3), nodes.get(8));
//        G.addEdge(nodes.get(8), nodes.get(3));
//        G.addEdge(nodes.get(4), nodes.get(5));
//        G.addEdge(nodes.get(5), nodes.get(4));
//        G.addEdge(nodes.get(4), nodes.get(9));
//        G.addEdge(nodes.get(9), nodes.get(4));
//        G.addEdge(nodes.get(5), nodes.get(10));
//        G.addEdge(nodes.get(10), nodes.get(5));
//        G.addEdge(nodes.get(6), nodes.get(7));
//        G.addEdge(nodes.get(7), nodes.get(6));
//        G.addEdge(nodes.get(6), nodes.get(11));
//        G.addEdge(nodes.get(11), nodes.get(6));
//        G.addEdge(nodes.get(7), nodes.get(8));
//        G.addEdge(nodes.get(8), nodes.get(7));
//        G.addEdge(nodes.get(7), nodes.get(12));
//        G.addEdge(nodes.get(12), nodes.get(7));
//        G.addEdge(nodes.get(8), nodes.get(9));
//        G.addEdge(nodes.get(9), nodes.get(8));
//        G.addEdge(nodes.get(9), nodes.get(10));
//        G.addEdge(nodes.get(10), nodes.get(9));
//        G.addEdge(nodes.get(9), nodes.get(14));
//        G.addEdge(nodes.get(14), nodes.get(9));
//        G.addEdge(nodes.get(10), nodes.get(15));
//        G.addEdge(nodes.get(15), nodes.get(10));
//        G.addEdge(nodes.get(11), nodes.get(12));
//        G.addEdge(nodes.get(12), nodes.get(11));
//        G.addEdge(nodes.get(11), nodes.get(16));
//        G.addEdge(nodes.get(16), nodes.get(11));
//        G.addEdge(nodes.get(12), nodes.get(13));
//        G.addEdge(nodes.get(13), nodes.get(12));
//        G.addEdge(nodes.get(12), nodes.get(17));
//        G.addEdge(nodes.get(17), nodes.get(12));
//        G.addEdge(nodes.get(13), nodes.get(14));
//        G.addEdge(nodes.get(14), nodes.get(13));
//        G.addEdge(nodes.get(13), nodes.get(18));
//        G.addEdge(nodes.get(18), nodes.get(13));
//        G.addEdge(nodes.get(14), nodes.get(15));
//        G.addEdge(nodes.get(15), nodes.get(14));
//        G.addEdge(nodes.get(14), nodes.get(19));
//        G.addEdge(nodes.get(19), nodes.get(14));
//        G.addEdge(nodes.get(15), nodes.get(20));
//        G.addEdge(nodes.get(20), nodes.get(15));
//        G.addEdge(nodes.get(16), nodes.get(17));
//        G.addEdge(nodes.get(17), nodes.get(16));
//        G.addEdge(nodes.get(17), nodes.get(18));
//        G.addEdge(nodes.get(18), nodes.get(17));
//        G.addEdge(nodes.get(18), nodes.get(19));
//        G.addEdge(nodes.get(19), nodes.get(18));
//        G.addEdge(nodes.get(19), nodes.get(20));
//        G.addEdge(nodes.get(20), nodes.get(19));

        G.addEdge(nodes.get(0), nodes.get(1)); G.addEdge(nodes.get(1), nodes.get(0));
        G.addEdge(nodes.get(0), nodes.get(5)); G.addEdge(nodes.get(5), nodes.get(0));
        G.addEdge(nodes.get(1), nodes.get(2)); G.addEdge(nodes.get(2), nodes.get(1));
        G.addEdge(nodes.get(1), nodes.get(6)); G.addEdge(nodes.get(6), nodes.get(1));
        G.addEdge(nodes.get(2), nodes.get(3)); G.addEdge(nodes.get(3), nodes.get(2));
        G.addEdge(nodes.get(2), nodes.get(7)); G.addEdge(nodes.get(7), nodes.get(2));
        G.addEdge(nodes.get(3), nodes.get(4)); G.addEdge(nodes.get(4), nodes.get(3));
        G.addEdge(nodes.get(3), nodes.get(8)); G.addEdge(nodes.get(8), nodes.get(3));
        G.addEdge(nodes.get(4), nodes.get(9)); G.addEdge(nodes.get(9), nodes.get(4));
        G.addEdge(nodes.get(5), nodes.get(6)); G.addEdge(nodes.get(6), nodes.get(5));
        G.addEdge(nodes.get(5), nodes.get(10)); G.addEdge(nodes.get(10), nodes.get(5));
        G.addEdge(nodes.get(6), nodes.get(7)); G.addEdge(nodes.get(7), nodes.get(6));
        G.addEdge(nodes.get(6), nodes.get(11)); G.addEdge(nodes.get(11), nodes.get(6));
        G.addEdge(nodes.get(7), nodes.get(8)); G.addEdge(nodes.get(8), nodes.get(7));
        G.addEdge(nodes.get(8), nodes.get(9)); G.addEdge(nodes.get(9), nodes.get(8));
        G.addEdge(nodes.get(8), nodes.get(13)); G.addEdge(nodes.get(13), nodes.get(8));
        G.addEdge(nodes.get(9), nodes.get(14)); G.addEdge(nodes.get(14), nodes.get(9));
        G.addEdge(nodes.get(10), nodes.get(11)); G.addEdge(nodes.get(11), nodes.get(10));
        G.addEdge(nodes.get(10), nodes.get(15)); G.addEdge(nodes.get(15), nodes.get(10));
        G.addEdge(nodes.get(11), nodes.get(12)); G.addEdge(nodes.get(12), nodes.get(11));
        G.addEdge(nodes.get(11), nodes.get(16)); G.addEdge(nodes.get(16), nodes.get(11));
        G.addEdge(nodes.get(12), nodes.get(13)); G.addEdge(nodes.get(13), nodes.get(12));
        G.addEdge(nodes.get(12), nodes.get(17)); G.addEdge(nodes.get(17), nodes.get(12));
        G.addEdge(nodes.get(13), nodes.get(14)); G.addEdge(nodes.get(14), nodes.get(13));
        G.addEdge(nodes.get(13), nodes.get(18)); G.addEdge(nodes.get(18), nodes.get(13));
        G.addEdge(nodes.get(14), nodes.get(19)); G.addEdge(nodes.get(19), nodes.get(14));
        G.addEdge(nodes.get(15), nodes.get(16)); G.addEdge(nodes.get(16), nodes.get(15));
        G.addEdge(nodes.get(16), nodes.get(17)); G.addEdge(nodes.get(17), nodes.get(16));
        G.addEdge(nodes.get(17), nodes.get(18)); G.addEdge(nodes.get(18), nodes.get(17));
        G.addEdge(nodes.get(18), nodes.get(19)); G.addEdge(nodes.get(19), nodes.get(18));

//        G.addEdge(nodes.get(0), nodes.get(1));
//        G.addEdge(nodes.get(1), nodes.get(0));
//
//        G.addEdge(nodes.get(1), nodes.get(2));
//        G.addEdge(nodes.get(2), nodes.get(1));
//
//        G.addEdge(nodes.get(2), nodes.get(3));
//        G.addEdge(nodes.get(3), nodes.get(2));
//
//        G.addEdge(nodes.get(3), nodes.get(4));
//        G.addEdge(nodes.get(4), nodes.get(3));
//
//        G.addEdge(nodes.get(4), nodes.get(5));
//        G.addEdge(nodes.get(5), nodes.get(4));
//
//        G.addEdge(nodes.get(5), nodes.get(6));
//        G.addEdge(nodes.get(6), nodes.get(5));
//
//        G.addEdge(nodes.get(6), nodes.get(7));
//        G.addEdge(nodes.get(7), nodes.get(6));
//
//        G.addEdge(nodes.get(7), nodes.get(8));
//        G.addEdge(nodes.get(8), nodes.get(7));
//
//        G.addEdge(nodes.get(8), nodes.get(9));
//        G.addEdge(nodes.get(9), nodes.get(8));
//
//        G.addEdge(nodes.get(9), nodes.get(10));
//        G.addEdge(nodes.get(10), nodes.get(9));
//
//        G.addEdge(nodes.get(10), nodes.get(11));
//        G.addEdge(nodes.get(11), nodes.get(10));
//
//        G.addEdge(nodes.get(11), nodes.get(12));
//        G.addEdge(nodes.get(12), nodes.get(11));
//
//        G.addEdge(nodes.get(12), nodes.get(13));
//        G.addEdge(nodes.get(13), nodes.get(12));
//
//        G.addEdge(nodes.get(13), nodes.get(14));
//        G.addEdge(nodes.get(14), nodes.get(13));
//
//        G.addEdge(nodes.get(14), nodes.get(15));
//        G.addEdge(nodes.get(15), nodes.get(14));
//
//        G.addEdge(nodes.get(15), nodes.get(16));
//        G.addEdge(nodes.get(16), nodes.get(15));
//
//        G.addEdge(nodes.get(16), nodes.get(17));
//        G.addEdge(nodes.get(17), nodes.get(16));
//
//        G.addEdge(nodes.get(17), nodes.get(18));
//        G.addEdge(nodes.get(18), nodes.get(17));
//
//        G.addEdge(nodes.get(18), nodes.get(19));
//        G.addEdge(nodes.get(19), nodes.get(18));
//
//        G.addEdge(nodes.get(0), nodes.get(4));
//        G.addEdge(nodes.get(4), nodes.get(0));
//
//        G.addEdge(nodes.get(1), nodes.get(5));
//        G.addEdge(nodes.get(5), nodes.get(1));
//
//        G.addEdge(nodes.get(2), nodes.get(6));
//        G.addEdge(nodes.get(6), nodes.get(2));
//
//        G.addEdge(nodes.get(3), nodes.get(7));
//        G.addEdge(nodes.get(7), nodes.get(3));
//
//        G.addEdge(nodes.get(4), nodes.get(8));
//        G.addEdge(nodes.get(8), nodes.get(4));
//
//        G.addEdge(nodes.get(5), nodes.get(9));
//        G.addEdge(nodes.get(9), nodes.get(5));
//
//        G.addEdge(nodes.get(6), nodes.get(10));
//        G.addEdge(nodes.get(10), nodes.get(6));
//
//        G.addEdge(nodes.get(7), nodes.get(11));
//        G.addEdge(nodes.get(11), nodes.get(7));
//
//        G.addEdge(nodes.get(8), nodes.get(12));
//        G.addEdge(nodes.get(12), nodes.get(8));
//
//        G.addEdge(nodes.get(9), nodes.get(13));
//        G.addEdge(nodes.get(13), nodes.get(9));
//
//        G.addEdge(nodes.get(10), nodes.get(14));
//        G.addEdge(nodes.get(14), nodes.get(10));

//        Segment2D segment1 = new Segment2D(new Point2D(2, 16), new Point2D(7, 8));
//        Segment2D segment2 = new Segment2D(new Point2D(0, 12), new Point2D(7, 13));
//        Segment2D segment3 = new Segment2D(new Point2D(0, 7), new Point2D(7, 13));
//        Point2D p1 = segment1.intersect(segment2);
//        Point2D p2 = segment1.intersect(segment3);
//        Point2D p3 = segment2.intersect(segment3);
//        System.err.println(p1 + " " + p2 + " " + p3);
//        if (p1 == null) return ;

        // TreeMultiset<Double> tmp = new TreeMultiset<>(new DoubleCompare());
        // tmp.add(0.);
        // tmp.add(1e-10);
        // System.err.println(tmp.countContains(0.));

        Map<Node, VarNodePosition> varPos = new HashMap<>();
        Set<Integer> DX = new HashSet<>();
        Set<Integer> DY = new HashSet<>();

        for(int i = 0; i <= COL; i++) DX.add(i);
        for(int i = 0; i <= ROW; i++) DY.add(i);
        // varPos.get(node) is the coordinate of node, to be optimized, so that G is displayed nicely on the plane
        for(int i = 0; i < n; i++){
            varPos.put(nodes.get(i), new VarNodePosition(i,DX,DY));
        }

        CBLSGPModel model = new CBLSGPModel();
        Random rnd = new Random(23480329);
        for (Node node : G.getNodes()) {
            VarNodePosition v = varPos.get(node);
            model.addVarNode(v);
            model.move(v, rnd.nextInt(COL + 1), rnd.nextInt(ROW + 1));
//            F.propagateOneNodeMove(v, v.x(), v.y());
        }

        NumberIntersectionEdges F3 = new NumberIntersectionEdges(G,varPos);// to be minimized
        MinAngle F2 = new MinAngle(G,varPos);// to be maximized
        MinDistanceEdge F1 = new MinDistanceEdge(G,varPos);// to be maximized
        MinDistanceNodeEdge F4 = new MinDistanceNodeEdge(G,varPos);// to be maximized

        LexMultiFunctions F = new LexMultiFunctions();
        Function FObj = new ObjectiveFunction(F3, F2, F1, F4, 100.0, 200.0, 1.0, 4);
        model.addFunction(FObj);
        F.add(FObj);
//        F.add(F3); F.add(F2); F.add(F1);

        model.close();


        for (Node node : G.getNodes()) {
            VarNodePosition v = varPos.get(node);
            System.err.printf("%d: (%d, %d)\n", node.id, v.x(), v.y());
        }
        LexMultiValues solutionEval = null;
        List<NodePosition> solutionPositions = new ArrayList<>();
        for (Node node : G.getNodes()) {
            VarNodePosition v = varPos.get(node);
            solutionPositions.add(new NodePosition(v.id, v.x(), v.y()));
        }

        boolean check = true;
        final int maxCounter = 3;
        int counter = 0;
        StringBuilder str = new StringBuilder();
        while (check || counter++ < maxCounter) {
            check = false;
            { // simple hill climbing
                System.err.println("simple hill climbing");
                boolean improved = true;
                // for(int it = 1; it <= 100000 && improved; it++){
                LexMultiValues bestEval = new LexMultiValues(List.of(FObj.evaluation()));
//                int it = 0;
                while (improved) {
//                    System.err.println(it++);
                    improved = false;
                    VarNodePosition selNode = null;
                    int selX = -1;
                    int selY = -1;
//                    StringBuilder str = new StringBuilder();
                    for (Node node : G.getNodes()) {
                        VarNodePosition v = varPos.get(node);
                        for (int x : DX) {
                            for (int y : DY) {
//                                System.err.println("Evaluating node " + node.id + " at (" + x + ", " + y + ")... ");
                                LexMultiValues eval = F.evaluateOneNodeMove(v, x, y);

//                                System.err.print("[" + eval + " " + node.id + " " + x + " " + y + "] ");
//                                for (Double f : eval.values) {
//                                    System.err.printf("%.2f ", f);
//                                }
//                                System.err.println();
                                if (eval.better(bestEval)) {

//                                    str.setLength(0);
//                                    for (Function f : F.F) {
//                                        str.append(f);
//                                    }
                                    selNode = v;
                                    selX = x;
                                    selY = y;
                                    bestEval = eval;
                                    improved = true;
                                }
                            }
                        }
                    }
                    if (selNode == null) {
                        break;
                    }

                    // perform the move
//                    System.err.println("Best move: " + selNode.id + " to (" + selX + ", " + selY + ") with evaluation: " + bestEval);
                    F.propagateOneNodeMove(selNode, selX, selY);
//                    for (Function f : F.F) {
//                        str.append(f);
//                    }
//                    System.err.println(bestEval + " " + str + " " + selNode.id + " " + selX + " " + selY);
                    model.move(selNode, selX, selY);

                    if (bestEval.better(solutionEval)) {
                        str.setLength(0);
                        for (Function f : F.F) {
                            str.append(f);
                        }
                        check = true;
                        counter = 0;
                        solutionEval = bestEval;
                        model.setNodePositionsValue(solutionPositions);
                    }
                }
            }

            if (false)
            { // tabu search
                System.err.println("tabu search");
                int tabuSize = 5;
                Queue<NodePosition> tabuList = new LinkedList<>();
                LexMultiValues currentEval = new LexMultiValues(List.of(FObj.evaluation()));
                int iteration = 0, maxIteration = 100;
                Map<Long, Boolean> isTabu = new HashMap<>();
                while (iteration < maxIteration) {
                    iteration++;

                    int selX = -1, selY = -1;
                    LexMultiValues bestEval = null;
                    VarNodePosition selNode = null;
                    for (Node node : G.getNodes()) {
                        VarNodePosition v = varPos.get(node);
                        for (int x : DX) {
                            for (int y : DY) {
                                // boolean isTabu = false;
                                // for (NodePosition pos : tabuList) {
                                //     if (pos.id() == node.id && pos.x() == x && pos.y() == y) {
                                //         isTabu = true;
                                //         break;
                                //     }
                                // }
                                // if (isTabu) continue;
                                long code = (long) node.id * (COL + 1) * (ROW + 1) + (long) x * (ROW + 1) + y;
                                if (isTabu.containsKey(code)) continue;

                                LexMultiValues eval = F.evaluateOneNodeMove(v, x, y);
                                if (eval.better(bestEval)) {

                                    selNode = v;
                                    selX = x;
                                    selY = y;
                                    bestEval = eval;
                                }
                            }
                        }
                    }

                    if (bestEval == null) {
                        continue;
                    }

                    long code = (long) selNode.id * (COL + 1) * (ROW + 1) + (long) selX * (ROW + 1) + selY;
                    isTabu.put(code, true);
                    tabuList.add(new NodePosition(selNode.id, selNode.x(), selNode.y()));
                    if (tabuList.size() > tabuSize) {
                        NodePosition pos = tabuList.poll();
                        long code1 = (long) pos.id() * (COL + 1) * (ROW + 1) + (long) pos.x() * (ROW + 1) + pos.y();
                        isTabu.remove(code1);
                    }

                    F.propagateOneNodeMove(selNode, selX, selY);
                    model.move(selNode, selX, selY);
                    currentEval = bestEval;
                    if (currentEval.better(solutionEval)) {
                        str.setLength(0);
                        for (Function f : F.F) {
                            str.append(f);
                        }

                        check = true;
                        counter = 0;
                        solutionEval = currentEval;
                        model.setNodePositionsValue(solutionPositions);
                    }
                }
            }

            { // simulated annealing
                System.err.println("simulated annealing");
                double temperature = 100.0;
                double coolingRate = 0.9995;

                Random random = new Random(290834301);
                LexMultiValues currentEval = new LexMultiValues(List.of(FObj.evaluation()));

                while (temperature > 0.01) {
                    Node randomeNode = G.getNodes().get(random.nextInt(G.getNodes().size()));
                    VarNodePosition v = varPos.get(randomeNode);
                    int newX = random.nextInt(COL + 1);
                    int newY = random.nextInt(ROW + 1);

                    LexMultiValues newEval = F.evaluateOneNodeMove(v, newX, newY);
                    boolean accept = false;
                    if (newEval.better(currentEval)) {
                        accept = true;
                    } else {
                        double delta = newEval.values.getFirst() - currentEval.values.getFirst();
                        if (Math.exp(delta / temperature) > random.nextDouble()) {
                            accept = true;
                        }
                    }

                    if (accept) {
                        F.propagateOneNodeMove(v, newX, newY);
                        model.move(v, newX, newY);

                        currentEval = newEval;
                        if (currentEval.better(solutionEval)) {
                            str.setLength(0);
                            for (Function f : F.F) {
                                str.append(f);
                            }

                            check = true;
                            counter = 0;
                            solutionEval = currentEval;
                            model.setNodePositionsValue(solutionPositions);
                        }
                    }

                    temperature *= coolingRate;
                }
            }

//            if (false)
            if (n > 1) { // two points move
                System.err.println("two points move");
                int iteration = 0, maxIteration = 30000;
                final double acceptWorseRate = 0.0005;
                LexMultiValues bestEval = new LexMultiValues(List.of(FObj.evaluation()));
                Random random = new Random(8234091);
                while (iteration < maxIteration) {
                    iteration++;
                    int i1 = random.nextInt(G.getNodes().size()), i2;
                    do {
                        i2 = random.nextInt(G.getNodes().size());
                    } while (i1 == i2);
                    Node node1 = G.getNodes().get(i1);
                    Node node2 = G.getNodes().get(i2);
                    VarNodePosition v1 = varPos.get(node1);
                    VarNodePosition v2 = varPos.get(node2);
                    int oldX1 = v1.x();
                    int oldY1 = v1.y();
                    // int oldX2 = v2.x();
                    // int oldY2 = v2.y();

                    int newX1 = random.nextInt(COL + 1);
                    int newY1 = random.nextInt(ROW + 1);
                    int newX2 = random.nextInt(COL + 1);
                    int newY2 = random.nextInt(ROW + 1);
//                    v1.assign(newX1, newY1);
//                    v2.assign(newX2, newY2);
                    F.propagateOneNodeMove(v1, newX1, newY1);
                    // F.propagateOneNodeMove(v2, newX2, newY2);
                    model.move(v1, newX1, newY1);
                    // model.move(v2, newX2, newY2);
                    LexMultiValues newEval = F.evaluateOneNodeMove(v2, newX2, newY2);
                    if (newEval.better(bestEval) || random.nextDouble() < acceptWorseRate) {

                        bestEval = newEval;
//                        model.setNodePositionsValue(solutionPositions);
                        F.propagateOneNodeMove(v2, newX2, newY2);
                        model.move(v2, newX2, newY2);
                        if (newEval.better(solutionEval)) {
                            str.setLength(0);
                            for (Function f : F.F) {
                                str.append(f);
                            }

                            check = true;
                            counter = 0;
                            solutionEval = newEval;
                            model.setNodePositionsValue(solutionPositions);
                        }

                    }
                    else {
//                        v1.assign(oldX1, oldY1);
                        // v2.assign(oldX2, oldY2);
                        F.propagateOneNodeMove(v1, oldX1, oldY1);
                        // F.propagateOneNodeMove(v2, oldX2, oldY2);
                        model.move(v1, oldX1, oldY1);
                        // model.move(v2, oldX2, oldY2);
                    }
                }
            }

//            if (false)
            if (n > 2) { // three points move
                System.err.println("three points move");
                int iteration = 0;
                int maxIteration = 10000;
                double acceptWorseRate = 0.0005;
                LexMultiValues bestEval = new LexMultiValues(List.of(FObj.evaluation()));
                Random random = new Random(289070329);
                while (iteration < maxIteration) {
                    iteration++;
                    int i1 = random.nextInt(G.getNodes().size()), i2, i3;
                    do {
                        i2 = random.nextInt(G.getNodes().size());
                    } while (i1 == i2);
                    do {
                        i3 = random.nextInt(G.getNodes().size());
                    } while (i1 == i3 || i2 == i3);

                    Node node1 = G.getNodes().get(i1);
                    Node node2 = G.getNodes().get(i2);
                    Node node3 = G.getNodes().get(i3);
                    VarNodePosition v1 = varPos.get(node1);
                    VarNodePosition v2 = varPos.get(node2);
                    VarNodePosition v3 = varPos.get(node3);
                    int oldX1 = v1.x();
                    int oldY1 = v1.y();
                    int oldX2 = v2.x();
                    int oldY2 = v2.y();
                    // int oldX3 = v3.x();
                    // int oldY3 = v3.y();

                    int newX1 = random.nextInt(COL + 1);
                    int newY1 = random.nextInt(ROW + 1);
                    int newX2 = random.nextInt(COL + 1);
                    int newY2 = random.nextInt(ROW + 1);
                    int newX3 = random.nextInt(COL + 1);
                    int newY3 = random.nextInt(ROW + 1);

                    F.propagateOneNodeMove(v1, newX1, newY1);
                    F.propagateOneNodeMove(v2, newX2, newY2);
//                        F.propagateOneNodeMove(v3, newX3, newY3);

//                        v1.assign(newX1, newY1);
                    model.move(v1, newX1, newY1);
                    // v2.assign(newX2, newY2);
                    model.move(v2, newX2, newY2);
                    // v3.assign(newX3, newY3);
//                        model.move(v3, newX3, newY3);

                    LexMultiValues newEval = F.evaluateOneNodeMove(v3, newX3, newY3);
                    if (newEval.better(bestEval) || random.nextDouble() < acceptWorseRate) {
                        bestEval = newEval;
                        F.propagateOneNodeMove(v3, newX3, newY3);
                        model.move(v3, newX3, newY3);
                        if (newEval.better(solutionEval)) {
                            str.setLength(0);
                            for (Function f : F.F) {
                                str.append(f);
                            }

                            check = true;
                            counter = 0;
                            solutionEval = newEval;
                            model.setNodePositionsValue(solutionPositions);
                        }
                    } else {
                        F.propagateOneNodeMove(v1, oldX1, oldY1);
                        F.propagateOneNodeMove(v2, oldX2, oldY2);
//                        F.propagateOneNodeMove(v3, oldX3, oldY3);

                        // v1.assign(oldX1, oldY1);
                        model.move(v1, oldX1, oldY1);
                        // v2.assign(oldX2, oldY2);
                        model.move(v2, oldX2, oldY2);
                        // v3.assign(oldX3, oldY3);
//                        model.move(v3, oldX3, oldY3);


                    }
                }
            }

//            if (false)
            if (n > 3) { // four points move
                System.err.println("four points move");
                int iteration = 0;
                int maxIteration = 10000;
                double acceptWorseRate = 0.0005;
                LexMultiValues bestEval = new LexMultiValues(List.of(FObj.evaluation()));
                Random random = new Random(278934081);
                while (iteration < maxIteration) {
                    iteration++;
                    int i1 = random.nextInt(G.getNodes().size()), i2, i3, i4;
                    do {
                        i2 = random.nextInt(G.getNodes().size());
                    } while (i1 == i2);
                    do {
                        i3 = random.nextInt(G.getNodes().size());
                    } while (i1 == i3 || i2 == i3);
                    do {
                        i4 = random.nextInt(G.getNodes().size());
                    } while (i1 == i4 || i2 == i4 || i3 == i4);

                    Node node1 = G.getNodes().get(i1);
                    Node node2 = G.getNodes().get(i2);
                    Node node3 = G.getNodes().get(i3);
                    Node node4 = G.getNodes().get(i4);
                    VarNodePosition v1 = varPos.get(node1);
                    VarNodePosition v2 = varPos.get(node2);
                    VarNodePosition v3 = varPos.get(node3);
                    VarNodePosition v4 = varPos.get(node4);
                    int oldX1 = v1.x();
                    int oldY1 = v1.y();
                    int oldX2 = v2.x();
                    int oldY2 = v2.y();
                    int oldX3 = v3.x();
                    int oldY3 = v3.y();
//                    int oldX4 = v4.x();
//                    int oldY4 = v4.y();

                    int newX1 = random.nextInt(COL + 1);
                    int newY1 = random.nextInt(ROW + 1);
                    int newX2 = random.nextInt(COL + 1);
                    int newY2 = random.nextInt(ROW + 1);
                    int newX3 = random.nextInt(COL + 1);
                    int newY3 = random.nextInt(ROW + 1);
                    int newX4 = random.nextInt(COL + 1);
                    int newY4 = random.nextInt(ROW + 1);

                    F.propagateOneNodeMove(v1, newX1, newY1);
                    F.propagateOneNodeMove(v2, newX2, newY2);
                    F.propagateOneNodeMove(v3, newX3, newY3);

//                        v1.assign(newX1, newY1);
                    model.move(v1, newX1, newY1);
                    // v2.assign(newX2, newY2);
                    model.move(v2, newX2, newY2);
                    // v3.assign(newX3, newY3);
                    model.move(v3, newX3, newY3);

                    LexMultiValues newEval = F.evaluateOneNodeMove(v4, newX4, newY4);
                    if (newEval.better(bestEval) || random.nextDouble() < acceptWorseRate) {
                        bestEval = newEval;
                        F.propagateOneNodeMove(v4, newX4, newY4);
                        model.move(v4, newX4, newY4);
                        if (newEval.better(solutionEval)) {
                            str.setLength(0);
                            for (Function f : F.F) {
                                str.append(f);
                            }

                            check = true;
                            counter = 0;
                            solutionEval = newEval;
                            model.setNodePositionsValue(solutionPositions);
                        }
                    } else {
                        F.propagateOneNodeMove(v1, oldX1, oldY1);
                        F.propagateOneNodeMove(v2, oldX2, oldY2);
                        F.propagateOneNodeMove(v3, oldX3, oldY3);

                        // v1.assign(oldX1, oldY1);
                        model.move(v1, oldX1, oldY1);
                        // v2.assign(oldX2, oldY2);
                        model.move(v2, oldX2, oldY2);
                        // v3.assign(oldX3, oldY3);
                        model.move(v3, oldX3, oldY3);
                    }
                }
            }

        //     { // vns
        //         int maxK = 4;
        //         int iteration = 0, maxIteration = 1000;

        //         while (iteration < maxIteration) {
        //             iteration++;
                    
        //             int k = 1;
        //             while (k <= maxK) {
        //                 Map<Node, VarNodePosition> newVarPos = new HashMap<>();
        //                 Random random = new Random();

        //                 for (int i = 0; i < k; i++) {
        //                     Node randomNode = G.getNodes().get(random.nextInt(G.getNodes().size()));
        //                     VarNodePosition v = varPos.get(randomNode);
        //                     int newX = random.nextInt(COL + 1);
        //                     int newY = random.nextInt(ROW + 1);
        //                     v.assign(newX, newY);
        //                     newVarPos.put(randomNode, v);
        //                 }
        //             }
        //         }
        //     }
        }

//         for (Node node : G.getNodes()) {
//             VarNodePosition v = varPos.get(node);
//             System.err.printf("%d: (%d, %d),\n", node.id, v.x(), v.y());
//         }
        System.err.printf("%s %s\n", str, solutionEval);
        for (NodePosition pos : solutionPositions) {
            System.err.printf("%d: (%d, %d),\n", pos.id(), pos.x(), pos.y());
        }

//        {
////            for (Node node : G.getNodes()) {
//            for (int i = 0; i < n; i++) {
//                VarNodePosition pos = varPos.get(nodes.get(i));
//                NodePosition pos1 = solutionPositions.get(i);
//                pos.assign(pos1.x(), pos1.y());
//            }
//            NumberIntersectionEdges f3 = new NumberIntersectionEdges(G, varPos);// to be minimized
//            MinAngle f2 = new MinAngle(G, varPos);// to be maximized
//            MinDistanceEdge f1 = new MinDistanceEdge(G, varPos);// to be maximized
//            MinDistanceNodeEdge f4 = new MinDistanceNodeEdge(G, varPos);// to be maximized
//
//            Function fObj = new ObjectiveFunction(f3, f2, f1, f4, 100.0, 200.0, 1.0, 4);
//            System.err.println(fObj);
//        }
    }
    public static void main(String[] args){
        long startTime = System.currentTimeMillis();
        test1();
        long endTime = System.currentTimeMillis();
        System.err.printf("Time taken: %.3f s\n", (endTime - startTime) / 1000.);
    }
}

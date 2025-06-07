package CBLS;


import java.util.*;

class Pair<K, V> {
    public K a;
    public V b;

    public Pair(K a, V b) {
        this.a = a;
        this.b = b;
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
    private double x, y;
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Point2D other)) return false;
        return Math.abs(x - other.x) < 1e-15 && Math.abs(y - other.y) < 1e-15;
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
            if (Math.abs(angle.get(id.get(i + 1)) - angle.get(id.get(i))) < 1e-15) {
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
            if (Math.abs(Math.atan2(p.getY() - y, p.getX() - x) - v) < 1e-15) {
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
            if (Math.abs(xx + xy - d2) < 1e-12) return x;
//            if (y.distance(seg.x) + y.distance(seg.y) < seg.x.distance(seg.y) + 1e-15) return y;
            if (Math.abs(yx + yy - d2) < 1e-12) return y;
//            if (seg.x.distance(x) + seg.x.distance(y) < x.distance(y) + 1e-15) return seg.x;
            if (Math.abs(xx + yx - d1) < 1e-12) return seg.x;
//            if (seg.y.distance(x) + seg.y.distance(y) < x.distance(y) + 1e-15) return seg.y;
            if (Math.abs(yy + xy - d1) < 1e-12) return seg.y;
            return null;
        }

//        if (x.equals(seg.x) || x.equals(seg.y) || y.equals(seg.x) || y.equals(seg.y)) {
//            return null;
//        }

        if (p.distance(x) + p.distance(y) > d1 + 1e-12) return null;
        if (p.distance(seg.x) + p.distance(seg.y) > d2 + 1e-12) return null;
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
    private List<Node> nodes;
    private Map<Integer, Node> nodeMap;
    private Map<Node, List<Edge>> A; // A[v] is the list of adjacent edges of v

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
            nodePositions.get(i).assign(varNodePositions.get(i).x(), varNodePositions.get(i).y());
        }
    }

    public void close(){
        for(Function f: F) f.initPropagation();
    }
}
class MinDistanceEdge implements Function{
    double minDistance;
    Graph g;
    Map<Node, VarNodePosition> positions;
    Map<Integer, Double> distances;
    Queue<Pair<Double, Integer>> pq;

    private int encode(Edge e) {
        int u = Math.min(e.fromNode.id, e.toNode.id);
        int v = Math.max(e.fromNode.id, e.toNode.id);
        return u * g.getNodes().size() + v;
    }
    public MinDistanceEdge(Graph g, Map<Node, VarNodePosition> positions){
        minDistance = Double.POSITIVE_INFINITY;
        this.g = g;
        this.positions = positions;
        distances = new HashMap<>();
        pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a.a));
        for (Node node: g.getNodes()){
            for(Edge e: g.getEdges(node)){
                Node u = e.fromNode, v = e.toNode;
                VarNodePosition posU = positions.get(u), posV = positions.get(v);
                double d = Geometry.distance(posU.x(), posU.y(), posV.x(), posV.y());
                distances.put(encode(e), d);
                minDistance = Math.min(minDistance, d);
//                distances.put(encode(e), Double.POSITIVE_INFINITY);
                pq.add(new Pair<>(d, encode(e)));
            }
        }
//        System.err.println(pq.size() + " " + distances.size() + " " + minDistance);
    }

    @Override
    public double evaluation() {
        while (!pq.isEmpty()) {
            Pair<Double, Integer> p = pq.peek();
            if (Double.compare(distances.getOrDefault(p.b, Double.POSITIVE_INFINITY), p.a) != 0) {
                pq.poll();
                continue;
            }
            return p.a;
        }
        return 0;
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
        Map<Integer, Boolean> visited = new HashMap<>();
        for (Edge e : g.getEdges(node)) {
            Node u = e.getRemaining(node);
//            if (node.id >= u.id) continue;
            VarNodePosition posU = positions.get(u);
            double d = Geometry.distance(posU.x(), posU.y(), newX, newY);
//            System.err.print("(" + u.id + " " + posU.x() + " " + posU.y() + " " + d + ") ");
            visited.put(encode(e), true);
            min = Math.min(min, d);
        }
//        System.err.print("0" + min + " " + newX + " " + newY);

        List<Pair<Double, Integer>> tmp = new ArrayList<>();
        while (!pq.isEmpty()) {
            Pair<Double, Integer> p = pq.poll();

            if (Double.compare(distances.getOrDefault(p.b, Double.POSITIVE_INFINITY), p.a) != 0) {
                continue;
            }
            tmp.add(p);
            if (p.a >= min) {
                pq.addAll(tmp);
                return min;
            }
            if (visited.containsKey(p.b)) {
                continue;
            }
            pq.addAll(tmp);
            return p.a;
        }
        pq.addAll(tmp);
//        return Double.POSITIVE_INFINITY;
        return min;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        if (newX == -1 && newY == -1) return;
        int oldX = varNodePosition.x(), oldY = varNodePosition.y();
//        System.err.printf("propagate (%d %d %d %d) ", oldX, oldY, newX, newY);
        if (oldX == newX && oldY == newY) return;
        Node node = g.getNode(varNodePosition.id);
        for (Edge e : g.getEdges(node)) {
            Node u = e.getRemaining(node);
            VarNodePosition posU = positions.get(u);
            double d = Geometry.distance(posU.x(), posU.y(), newX, newY);
//            System.err.printf("(%d %d %f) ", e.fromNode.id, e.toNode.id, d);
            int eId = encode(e);
            distances.put(eId, d);
            pq.add(new Pair<>(d, eId));
        }
    }
    @Override
    public void initPropagation() {

    }
}

class TreeMultiset<T extends Comparable<T>> {
    private final TreeMap<T, Integer> map = new TreeMap<>();

    void add(T value) {
        map.put(value, map.getOrDefault(value, 0) + 1);
    }

    void remove(T value) {
        Integer count = map.get(value);
        if (count != null) {
            if (count > 1) {
                map.put(value, count - 1);
            } else {
                map.remove(value);
            }
        }
    }

    T first() {
        return map.isEmpty() ? null : map.firstKey();
    }

    boolean isEmpty() {
        return map.isEmpty();
    }

    Set<Map.Entry<T, Integer>> entrySet() {
        return map.entrySet();
    }
}

class MinAngle implements Function {
    Graph g;
    Map<Node, VarNodePosition> positions;
    Map<Node, TreeSet<NodeAngle>> nodeNeighbors = new HashMap<>();
    TreeMultiset<Double> allAngles = new TreeMultiset<>();
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
            int angleComp = Double.compare(angle, o.angle);
            return angleComp != 0 ? angleComp : Integer.compare(node.id, o.node.id);
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof NodeAngle na)) return false;
            return node.id == na.node.id && Double.compare(angle, na.angle) == 0;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(node.id, angle);
        }
    }
    
    public MinAngle(Graph g, Map<Node, VarNodePosition> positions) {
        this.g = g;
        this.positions = positions;
        
        for (Node node : g.getNodes()) {
            nodeNeighbors.put(node, new TreeSet<>());
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
        VarNodePosition nodePos = positions.get(node);
        if (nodePos.x() == -1 && nodePos.y() == -1) return;
        if (newX == -1 && newY == -1) return;
//        if (nodePos.x() == newX && nodePos.y() == newY) return;
        
        TreeSet<NodeAngle> neighbors = nodeNeighbors.get(node);
        
        if (neighbors.size() >= 2) {
            List<NodeAngle> neighborList = new ArrayList<>(neighbors);
            for (int i = 0; i < neighborList.size(); i++) {
                NodeAngle current = neighborList.get(i);
                NodeAngle next = neighborList.get((i + 1) % neighborList.size());
                double angle = (next.angle - current.angle + 2 * Math.PI) % (2 * Math.PI);
                allAngles.remove(angle);
            }
        }
        
        neighbors.clear();
        
        List<Node> connectedNodes = new ArrayList<>();
        for (Edge e : g.getEdges(node)) {
            Node neighbor = e.getRemaining(node);
            VarNodePosition neighborPos = positions.get(neighbor);
            
            if (neighborPos.x() != -1 && neighborPos.y() != -1) {
                connectedNodes.add(neighbor);
                double angle = Math.atan2(neighborPos.y() - newY, neighborPos.x() - newX);
                neighbors.add(new NodeAngle(neighbor, angle));
            }
        }
        
        if (neighbors.size() >= 2) {
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
    
    private void updateNeighborAngle(Node node, Node movedNode, double newAngle) {
        TreeSet<NodeAngle> neighbors = nodeNeighbors.get(node);
        if (neighbors.size() < 2) return;
        
        NodeAngle oldNodeAngle = null;
        for (NodeAngle na : neighbors) {
            if (na.node.id == movedNode.id) {
                oldNodeAngle = na;
                break;
            }
        }

        if (oldNodeAngle != null) {
            NodeAngle prev = neighbors.lower(oldNodeAngle);
            if (prev == null) prev = neighbors.last();
            
            NodeAngle next = neighbors.higher(oldNodeAngle);
            if (next == null) next = neighbors.first();
            
            double prevOldAngle = (oldNodeAngle.angle - prev.angle + 2 * Math.PI) % (2 * Math.PI);
            double oldNextAngle = (next.angle - oldNodeAngle.angle + 2 * Math.PI) % (2 * Math.PI);
            allAngles.remove(prevOldAngle);
            allAngles.remove(oldNextAngle);
            
            neighbors.remove(oldNodeAngle);
            NodeAngle newNodeAngle = new NodeAngle(movedNode, newAngle);
            neighbors.add(newNodeAngle);
            
            prev = neighbors.lower(newNodeAngle);
            if (prev == null) prev = neighbors.last();
            
            next = neighbors.higher(newNodeAngle);
            if (next == null) next = neighbors.first();
            
            double prevNewAngle = (newNodeAngle.angle - prev.angle + 2 * Math.PI) % (2 * Math.PI);
            double newNextAngle = (next.angle - newNodeAngle.angle + 2 * Math.PI) % (2 * Math.PI);
            allAngles.add(prevNewAngle);
            allAngles.add(newNextAngle);
            
            if (!allAngles.isEmpty()) {
                minAngleValue = allAngles.first();
            } else {
                minAngleValue = Math.PI * 2;
            }
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
            return minAngleValue;
        }

        updateNodeAngles(node, newX, newY);
        for (Edge e : g.getEdges(node)) {
            Node neighbor = e.getRemaining(node);
            VarNodePosition neighborPos = positions.get(neighbor);
            double newAngle = Math.atan2(newY - neighborPos.y(), newX - neighborPos.x());
            updateNeighborAngle(neighbor, node, newAngle);
        }
        
        double newMinAngle = minAngleValue;

        updateNodeAngles(node, oldX, oldY);
        for (Edge e : g.getEdges(node)) {
            Node neighbor = e.getRemaining(node);
            VarNodePosition neighborPos = positions.get(neighbor);
            if (neighborPos.x() == -1 && neighborPos.y() == -1) continue;
            double oldAngle = Math.atan2(oldY - neighborPos.y(), oldX - neighborPos.x());
            updateNeighborAngle(neighbor, node, oldAngle);
        }

        return newMinAngle;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition v, int newX, int newY) {
        Node movedNode = g.getNode(v.id);
        int oldX = v.x(), oldY = v.y();
        if (oldX == newX && oldY == newY || newX == -1 && newY == -1) {
            return;
        }

        v.assign(newX, newY);

        updateNodeAngles(movedNode, newX, newY);
        
        for (Edge e : g.getEdges(movedNode)) {
            Node neighbor = e.getRemaining(movedNode);
            VarNodePosition neighborPos = positions.get(neighbor);
            if (neighborPos.x() == -1 && neighborPos.y() == -1) continue;
            double newAngle = Math.atan2(newY - neighborPos.y(), newX - neighborPos.x());
            updateNeighborAngle(neighbor, movedNode, newAngle);
        }
        v.assign(oldX, oldY);
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
            int eId = encode(e);
            for (Edge f : edges) {
                if (f == e || nodeEdges.contains(f)) continue;
                if (e.fromNode.id == f.fromNode.id || e.toNode.id == f.toNode.id ||
                        e.fromNode.id == f.toNode.id || e.toNode.id == f.fromNode.id) continue;
                if (encode(f) == eId) continue;
                Segment2D sf = createSegment(f);
                if (sf == null) continue;
                
                if (se.intersect(sf) != null) {
                    int ei = encode(e), fi = encode(f);
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
            return -totalIntersections;
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
                if (f == e || nodeEdges.contains(f)) continue;
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

class MinDistanceNodeEdge implements Function {
    private final Graph g;
    private final Map<Node, VarNodePosition> positions;
    private final Queue<Pair<Double, Long>> pq;
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
        pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a.a));

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
                        pq.add(new Pair<>(distance, key));
                    }
                }
            }
        }
//        System.err.println(pq.size() + " " + distances.size() + " " + pq.peek().a);
    }

    @Override
    public double evaluation() {
        while (!pq.isEmpty()) {
            Pair<Double, Long> p = pq.peek();
            if (Double.compare(distances.getOrDefault(p.b, Double.POSITIVE_INFINITY), p.a) != 0) {
                pq.poll();
                continue;
            }
            return p.a;
        }
//        return 0;
        return Double.POSITIVE_INFINITY;
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
        Map<Long, Boolean> visited = new HashMap<>();
        for (Edge edge : edges) {
            if (edge.fromNode.id == node.id || edge.toNode.id == node.id) continue;
            Segment2D segment = createSegment(edge);
            if (segment == null) continue;

            double distance = point.distance(segment);
            visited.put(encode(node, edge), true);
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

                visited.put(key, true);

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
        List<Pair<Double, Long>> tmp = new ArrayList<>();
        while (!pq.isEmpty()) {
            Pair<Double, Long> p = pq.poll();

            if (Double.compare(distances.getOrDefault(p.b, Double.POSITIVE_INFINITY), p.a) != 0) {
                continue;
            }

            tmp.add(p);
            if (p.a >= min) {
                pq.addAll(tmp);
                return min;
            }
            if (visited.containsKey(p.b)) {
                continue;
            }
//            if (node.id == 9 && newX == 9 && newY == 11) {
//                int eid = (int) (p.b / g.getNodes().size());
//                int nid = (int) (p.b % g.getNodes().size());
//                Edge edge = null;
//                for (Edge e : g.getEdges()) {
//                    if (e.id == eid) {
//                        edge = e;
//                        break;
//                    }
//                }
//                Node node1 = null;
//                for (Node n : g.getNodes()) {
//                    if (n.id == nid) {
//                        node1 = n;
//                        break;
//                    }
//                }
//                VarNodePosition pos1 = positions.get(node1);
//                System.err.printf("p=(%.10f, %d %d %d, %d, %d) ", p.a, edge.id, edge.fromNode.id, edge.toNode.id, eid, nid);
//                System.err.printf("%.10f ", new Point2D(pos1.x(), pos1.y()).distance(createSegment(edge)));
//            }
            pq.addAll(tmp);
            return p.a;
        }
        pq.addAll(tmp);
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
            distances.put(key, distance);
            pq.add(new Pair<>(distance, key));
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
                    distances.put(key, distance);
//                    if (Double.compare(distance, 0) == 0) {
//                        System.err.printf("%d %d %d %d %d %d %f\n",
//                                node.id, adjNode.id,
//                                pos.x(), pos.y(),
//                                newX, newY, distance);
//                    }
                    pq.add(new Pair<>(distance, key));
                }
            }
        }
    }

    @Override
    public void initPropagation() {

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
        Function FObj = new ObjectiveFunction(F3, F2, F1, F4, 10.0, 10.0, 1.0, 1);
        model.addFunction(FObj);
        F.add(FObj);
//        F.add(F3); F.add(F2); F.add(F1);

        for (Node node : G.getNodes()) {
            VarNodePosition v = varPos.get(node);
            System.err.printf("%d: (%d, %d)\n", node.id, v.x(), v.y());
        }
        model.close();

        LexMultiValues solutionEval = null;
        List<NodePosition> solutionPositions = model.getNodePositions();
        boolean check = true;
        while (check) {
            check = false;
            { // simple hill climbing
                System.err.println("simple hill climbing");
                boolean improved = true;
                // for(int it = 1; it <= 100000 && improved; it++){
                LexMultiValues bestEval = solutionEval;
                while (improved) {
                    improved = false;
                    VarNodePosition selNode = null;
                    int selX = -1;
                    int selY = -1;
//                StringBuilder str = new StringBuilder();
                    for (Node node : G.getNodes()) {
                        VarNodePosition v = varPos.get(node);
                        for (int x : DX) {
                            for (int y : DY) {
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
                    F.propagateOneNodeMove(selNode, selX, selY);
//                for (Function f : F.F) {
//                    str.append(f);
//                }
//                System.err.println(bestEval + " " + str + " " + selNode.id + " " + selX + " " + selY);
                    model.move(selNode, selX, selY);
                    if (bestEval.better(solutionEval)) {
                        check = true;
                        solutionEval = bestEval;
                        model.setNodePositionsValue(solutionPositions);
                    }
                }
            }

            { // tabu search
                System.err.println("tabu search");
                int tabuSize = 5;
                Queue<NodePosition> tabuList = new LinkedList<>();
                LexMultiValues currentEval = new LexMultiValues(List.of(FObj.evaluation()));
                int iteration = 0, maxIteration = 100;
                while (iteration < maxIteration) {
                    iteration++;

                    int selX = -1, selY = -1;
                    LexMultiValues bestEval = null;
                    VarNodePosition selNode = null;
                    for (Node node : G.getNodes()) {
                        VarNodePosition v = varPos.get(node);
                        for (int x : DX) {
                            for (int y : DY) {
                                boolean isTabu = false;
                                for (NodePosition pos : tabuList) {
                                    if (pos.id() == node.id && pos.x() == x && pos.y() == y) {
                                        isTabu = true;
                                        break;
                                    }
                                }
                                if (isTabu) continue;

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

                    tabuList.add(new NodePosition(selNode.id, selNode.x(), selNode.y()));
                    if (tabuList.size() > tabuSize) {
                        tabuList.poll();
                    }

                    model.move(selNode, selX, selY);
                    F.propagateOneNodeMove(selNode, selX, selY);
                    currentEval = bestEval;
                    if (currentEval.better(solutionEval)) {
                        check = true;
                        solutionEval = currentEval;
                        model.setNodePositionsValue(solutionPositions);
                    }
                }
            }

            { // simulated annealing
                System.err.println("simulated annealing");
                double temperature = 100.0;
                double coolingRate = 0.995;

                Random random = new Random();
                LexMultiValues currentEval = new LexMultiValues(List.of(FObj.evaluation()));

                while (temperature > 0.1) {
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
                            check = true;
                            solutionEval = currentEval;
                            model.setNodePositionsValue(solutionPositions);
                        }
                    }

                    temperature *= coolingRate;
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
        //                     Node randomeNode = G.getNodes().get(random.nextInt(G.getNodes().size()));
        //                     VarNodePosition v = varPos.get(randomeNode);
        //                     int newX = random.nextInt(COL + 1);
        //                     int newY = random.nextInt(ROW + 1);
        //                     v.assign(newX, newY);
        //                     newVarPos.put(randomeNode, v);
        //                 }
        //             }
        //         }
        //     }
        }

//         for (Node node : G.getNodes()) {
//             VarNodePosition v = varPos.get(node);
//             System.err.printf("%d: (%d, %d),\n", node.id, v.x(), v.y());
//         }
        for (NodePosition pos : solutionPositions) {
            System.err.printf("%d: (%d, %d),\n", pos.id(), pos.x(), pos.y());
        }
    }
    public static void main(String[] args){
        test1();
    }
}

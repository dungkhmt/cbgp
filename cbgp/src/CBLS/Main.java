package CBLS;


import java.util.*;

interface Function{
    double evaluation();
    double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY);
    void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY);

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

class VarNodePosition{
    int id;
    int x_pos;// x-coordinate
    int y_pos;// y-coordinate
    Set<Integer> DX;// domain for x-coordinate
    Set<Integer> DY;// domain for y-coordinate
    public VarNodePosition(int id, Set<Integer> DX, Set<Integer> Dy){
        this.id = id; this.DX = DX; this.DY = DY;
    }
    public int x(){ return x_pos; }
    public int y(){ return y_pos; }
    public void assign(int newX, int newY){
        x_pos = newX; y_pos = newY;
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
}
class Graph{
    private List<Node> nodes;
    private Map<Node, List<Edge>> A; // A[v] is the list of adjacent edges of v

    public Graph(List<Node> nodes, Map<Node, List<Edge>> a) {
        this.nodes = nodes;
        A = a;
    }
    public Graph(List<Node> nodes){
        this.nodes = nodes;
        A = new HashMap<>();
        for(Node n: nodes) A.put(n, new ArrayList<>());
    }
    public void addEdge(Node u, Node v){
        Edge e = new Edge(u,v);
        A.get(u).add(e);
    }
    public List<Node> getNodes(){ return nodes; }
}

class CBLSGPModel{
    List<VarNodePosition> varNodePositions;
    List<Function> F;

    // additional data structures defined here to efficiently perform the move

    public void addVarNode(VarNodePosition varNodePosition){
        varNodePositions.add(varNodePosition);
    }
    public void addFunction(Function f){
        F.add(f);
    }
    public void move(VarNodePosition varNode, int x, int y){
        // perform propagation to update functions in F
        varNode.assign(x,y);
    }
}
class MinDistanceEdge implements Function{

    public MinDistanceEdge(Graph g, Map<Node, VarNodePosition> postions){

    }
    @Override
    public double evaluation() {
        return 0;
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        return 0;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {

    }
}
class MinAngle implements Function{
    public MinAngle(Graph g, Map<Node, VarNodePosition> postions){

    }

    @Override
    public double evaluation() {
        return 0;
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        return 0;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {

    }
}

class NumberIntersectionEdges implements  Function{
    // represent the number of pairs of intersecting edges of G
    public NumberIntersectionEdges(Graph g, Map<Node, VarNodePosition> postions){

    }

    @Override
    public double evaluation() {
        return 0;
    }

    @Override
    public double evaluateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {
        return 0;
    }

    @Override
    public void propagateOneNodeMove(VarNodePosition varNodePosition, int newX, int newY) {

    }
}
class LexMultiValues{
    public boolean better(LexMultiValues V){
        return true;
    }
}
class LexMultiFunctions{
    /*
    lexicographic multi-functions
     */
    List<Function> F;
    public LexMultiFunctions(){
        F = new ArrayList<>();
    }
    public void add(Function f){
        F.add(f);
    }





    public LexMultiValues evaluateOneNodeMove(VarNodePosition node, int x, int y) {
        return null;
    }
}
public class Main {

    public static void test1(){
        int ROW = 20;
        int COL = 20; // the graph is presented on a grid ROW x  COL
        int n  = 5;// number of nodes 0,1, 2, . . ., n-1
        List<Node> nodes = new ArrayList<>();
        for(int i = 0; i < n; i++){
            nodes.add(new Node(i));
        }
        Graph G = new Graph(nodes);
        G.addEdge(nodes.get(0),nodes.get(1));
        G.addEdge(nodes.get(0),nodes.get(2));
        G.addEdge(nodes.get(0),nodes.get(4));
        G.addEdge(nodes.get(1),nodes.get(3));
        G.addEdge(nodes.get(1),nodes.get(4));
        G.addEdge(nodes.get(2),nodes.get(4));

        Map<Node, VarNodePosition> varPos = new HashMap<>();
        Set<Integer> DX = new HashSet<>();
        Set<Integer> DY = new HashSet<>();

        for(int i = 0; i <= COL; i++) DX.add(i);
        for(int i = 0; i <= ROW; i++) DY.add(i);
        // varPos.get(node) is the coordinate of node, to be optimized, so that G is displayed nicely on the plane
        for(int i = 0; i < n; i++){
            varPos.put(nodes.get(i), new VarNodePosition(i,DX,DY));
        }

        MinDistanceEdge F1 = new MinDistanceEdge(G,varPos);// to be maximized
        MinAngle F2 = new MinAngle(G,varPos);// to be maximized
        NumberIntersectionEdges F3 = new NumberIntersectionEdges(G,varPos);// to be minimized

        LexMultiFunctions F = new LexMultiFunctions();
        F.add(F1); F.add(F2); F.add(F3);

        CBLSGPModel model = new CBLSGPModel();

        // simple hill climbing
        for(int it = 1; it <= 100000; it++){
            VarNodePosition selNode = null; int selX = -1; int selY = -1;
            LexMultiValues bestEval = null;
            for(Node node: G.getNodes()){
                VarNodePosition v = varPos.get(node);
                for(int x : DX){
                    for(int y: DY){
                        LexMultiValues eval = F.evaluateOneNodeMove(v,x,y);
                        if(eval.better(bestEval)){
                            selNode = v; selX = x; selY = y;
                        }
                    }
                }
            }
            if(selNode == null){
                break;
            }
            // perform the move
            model.move(selNode,selX,selY);
        }
    }
    public static void main(String[] args){
        test1();
    }
}

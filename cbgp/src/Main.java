
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

class VarIntCP{
    Domain domain;
    String name;
    public VarIntCP(Set<Integer> domain, String name){
        this.domain = new Domain(domain);
        this.name = name;
    }
    public VarIntCP(int min, int max, String name){
        this.domain = new Domain(min,max);
        //System.out.println("VarIntCP, domain = " + domain.toString());
        this.name = name;
    }

    Domain getDomain(){ return this.domain; }
    public void print(){
        System.out.println("Var " + name + ": " + this.domain.toString());
    }
}
interface ConstraintCP{
    boolean contains(VarIntCP x);
    List<VarIntCP> getVariables();
    boolean reviseAC3(VarIntCP x, CPModel m);
    boolean satisfy(CPModel m);
    public String name();
}
class Leq implements ConstraintCP{
    // ax <= by + v
    List<VarIntCP> variables;
    HashMap<VarIntCP, Integer> mVar2Index;
    VarIntCP x,y;
    public String name;
    int a,b;
    int v;

    public PrintWriter log;
    @Override
    public String name(){
        return this.name;
    }
    public Leq(VarIntCP x, VarIntCP y, int v){
        variables = new ArrayList<VarIntCP>();
        mVar2Index = new HashMap<VarIntCP, Integer>();
        variables.add(x); variables.add(y);
        mVar2Index.put(x,0); mVar2Index.put(y,1);
        this.x = x; this.y = y; this.v = v;
        this.a = 1; this.b = 1;
    }
    public Leq(VarIntCP x, VarIntCP y, String name, PrintWriter log){
        this.log = log;
        this.name = name;
        variables = new ArrayList<VarIntCP>();
        mVar2Index = new HashMap<VarIntCP, Integer>();
        variables.add(x); variables.add(y);
        mVar2Index.put(x,0); mVar2Index.put(y,1);
        this.x = x; this.y = y; this.v = 0; this.a = 1; this.b = 1;
    }
    public Leq(int a, VarIntCP x, int b, VarIntCP y, int v, String name){
        this.log = null;
        this.name = name;
        variables = new ArrayList<VarIntCP>();
        mVar2Index = new HashMap<VarIntCP, Integer>();
        variables.add(x); variables.add(y);
        mVar2Index.put(x,0); mVar2Index.put(y,1);
        this.x = x; this.y = y; this.v = v; this.a = a; this.b = b;
    }

    @Override
    public boolean contains(VarIntCP x) {
        return mVar2Index.get(x) != null;
    }

    @Override
    public List<VarIntCP> getVariables() {
        return variables;
    }

    @Override
    public boolean reviseAC3(VarIntCP var, CPModel m) {
        //log.println(name() + "::reviseAC3 with var = " + var.name);
        if(var != this.x && var != this.y){
            //log.println(name() + "::reviseAC3 with var = " + var.name + " NONE");
            return false;
        }
        //System.out.println(name + "reviseAC3");
        List<Integer> R = new ArrayList<Integer>();

        if(var == this.x) {
            //log.println(name() + "::reviseAC3 with var = " + var.name + " meet x");
            for (int vx : m.getDomain(x).getSet()) {
                //log.println(name() + "::reviseAC3 with var = " + var.name + " meet x consider vx = " + vx);
                boolean ok = false;
                for (int vy : m.getDomain(y).getSet()) {
                    //log.println(name() + "::reviseAC3 with var = " + var.name + " meet x consider vx = " + vx + " consider vy = " + vy);
                    if(a*vx <= b*vy + v){ ok = true; break; }
                }
                if(!ok) R.add(vx);
            }
            for(int vx: R){
                m.getDomain(x).remove(vx);
                //log.println(name + " -> remove (" + vx + ") from domain " + var.name);
            }
            return R.size() > 0;
        } else if(var == this.y){
            //log.println(name() + "::reviseAC3 with var = " + var.name + " meet y");
            for(int vy: m.getDomain(y).getSet()){
                boolean ok = false;
                for(int vx: m.getDomain(x).getSet()){
                    if(a*vx <= b*vy + v){ ok = true; break; }
                }
                if(!ok) R.add(vy);
            }
            for(int vy: R){
                m.getDomain(y).remove(vy);
                //log.println(name + " -> remove (" + vy + ") from domain " + var.name);

            }
            return R.size() > 0;
        }
        return false;
    }

    @Override
    public boolean satisfy(CPModel m) {
        Set<Integer> X = m.getDomain(this.x).getSet();
        Set<Integer> Y = m.getDomain(this.y).getSet();
        if(X.size() == 1 && Y.size() == 1){
            for(int vx: X){
                for(int vy: Y){
                    if(a*vx <= b*vy + this.v) return true;
                }
            }
        }
        return false;
    }

}
class Eq implements ConstraintCP{
    // ax = by + v
    List<VarIntCP> variables;
    HashMap<VarIntCP, Integer> mVar2Index;
    VarIntCP x,y;
    public String name;
    int a,b;
    int v;

    public PrintWriter log;
    @Override
    public String name(){
        return this.name;
    }
    public Eq(VarIntCP x, VarIntCP y, int v){
        variables = new ArrayList<VarIntCP>();
        mVar2Index = new HashMap<VarIntCP, Integer>();
        variables.add(x); variables.add(y);
        mVar2Index.put(x,0); mVar2Index.put(y,1);
        this.x = x; this.y = y; this.v = v;
        this.a = 1; this.b = 1;
    }
    public Eq(int a, VarIntCP x, int b, VarIntCP y, int v, String name){
        this.log = null;
        this.name = name;
        variables = new ArrayList<VarIntCP>();
        mVar2Index = new HashMap<VarIntCP, Integer>();
        variables.add(x); variables.add(y);
        mVar2Index.put(x,0); mVar2Index.put(y,1);
        this.x = x; this.y = y; this.v = v; this.a = a; this.b = b;
    }
    public Eq(VarIntCP x, VarIntCP y, String name, PrintWriter log){
        this.log = log;
        this.name = name;
        variables = new ArrayList<VarIntCP>();
        mVar2Index = new HashMap<VarIntCP, Integer>();
        variables.add(x); variables.add(y);
        mVar2Index.put(x,0); mVar2Index.put(y,1);
        this.x = x; this.y = y; this.v = 0; this.a = 1; this.b = 1;
    }

    @Override
    public boolean contains(VarIntCP x) {
        return mVar2Index.get(x) != null;
    }

    @Override
    public List<VarIntCP> getVariables() {
        return variables;
    }

    @Override
    public boolean reviseAC3(VarIntCP var, CPModel m) {
        //log.println(name() + "::reviseAC3 with var = " + var.name);
        //System.out.println(name() + "::reviseAC3 with var = " + var.name);
        if(var != this.x && var != this.y){
            //log.println(name() + "::reviseAC3 with var = " + var.name + " NONE");
            return false;
        }
        //System.out.println(name + "reviseAC3");
        List<Integer> R = new ArrayList<Integer>();

        if(var == this.x) {
            //log.println(name() + "::reviseAC3 with var = " + var.name + " meet x");
            for (int vx : m.getDomain(x).getSet()) {
                //log.println(name() + "::reviseAC3 with var = " + var.name + " meet x consider vx = " + vx);
                boolean ok = false;
                for (int vy : m.getDomain(y).getSet()) {
                    //log.println(name() + "::reviseAC3 with var = " + var.name + " meet x consider vx = " + vx + " consider vy = " + vy);
                    if(a*vx == b*vy + v){ ok = true; break; }
                }
                if(!ok) R.add(vx);
            }
            for(int vx: R){
                m.getDomain(x).remove(vx);
                //log.println(name + " -> remove (" + vx + ") from domain " + var.name);
                //System.out.println(name + " -> remove (" + vx + ") from domain " + var.name);
            }
            return R.size() > 0;
        } else if(var == this.y){
            //log.println(name() + "::reviseAC3 with var = " + var.name + " meet y");
            for(int vy: m.getDomain(y).getSet()){
                boolean ok = false;
                for(int vx: m.getDomain(x).getSet()){
                    if(a*vx == b*vy + v){ ok = true; break; }
                }
                if(!ok) R.add(vy);
            }
            for(int vy: R){
                m.getDomain(y).remove(vy);
                //log.println(name + " -> remove (" + vy + ") from domain " + var.name);
                //System.out.println(name + " -> remove (" + vy + ") from domain " + var.name);

            }
            return R.size() > 0;
        }
        return false;
    }

    @Override
    public boolean satisfy(CPModel m) {
        Set<Integer> X = m.getDomain(this.x).getSet();
        Set<Integer> Y = m.getDomain(this.y).getSet();
        if(X.size() == 1 && Y.size() == 1){
            for(int vx: X){
                for(int vy: Y){
                    if(a*vx == b*vy + this.v) return true;
                }
            }
        }
        return false;
    }

}

class Neq implements ConstraintCP{
    // ax != by + v
    List<VarIntCP> variables;
    HashMap<VarIntCP, Integer> mVar2Index;
    VarIntCP x,y;
    public String name;
    int a,b;
    int v;

    public PrintWriter log;
    @Override
    public String name(){
        return this.name;
    }
    public Neq(VarIntCP x, VarIntCP y, int v){
        variables = new ArrayList<VarIntCP>();
        mVar2Index = new HashMap<VarIntCP, Integer>();
        variables.add(x); variables.add(y);
        mVar2Index.put(x,0); mVar2Index.put(y,1);
        this.x = x; this.y = y; this.v = v;
        this.a = 1; this.b = 1;
    }
    public Neq(int a, VarIntCP x, int b, VarIntCP y, int v, String name){
        this.log = null;
        this.name = name;
        variables = new ArrayList<VarIntCP>();
        mVar2Index = new HashMap<VarIntCP, Integer>();
        variables.add(x); variables.add(y);
        mVar2Index.put(x,0); mVar2Index.put(y,1);
        this.x = x; this.y = y; this.v = v; this.a = a; this.b = b;
    }
    public Neq(VarIntCP x, VarIntCP y, String name, PrintWriter log){
        this.log = log;
        this.name = name;
        variables = new ArrayList<VarIntCP>();
        mVar2Index = new HashMap<VarIntCP, Integer>();
        variables.add(x); variables.add(y);
        mVar2Index.put(x,0); mVar2Index.put(y,1);
        this.x = x; this.y = y; this.v = 0; this.a = 1; this.b = 1;
    }

    @Override
    public boolean contains(VarIntCP x) {
        return mVar2Index.get(x) != null;
    }

    @Override
    public List<VarIntCP> getVariables() {
        return variables;
    }

    @Override
    public boolean reviseAC3(VarIntCP var, CPModel m) {
        //log.println(name() + "::reviseAC3 with var = " + var.name);
        if(var != this.x && var != this.y){
            //log.println(name() + "::reviseAC3 with var = " + var.name + " NONE");
            return false;
        }
        //System.out.println(name + "reviseAC3");
        List<Integer> R = new ArrayList<Integer>();

        if(var == this.x) {
            //log.println(name() + "::reviseAC3 with var = " + var.name + " meet x");
            for (int vx : m.getDomain(x).getSet()) {
                //log.println(name() + "::reviseAC3 with var = " + var.name + " meet x consider vx = " + vx);
                boolean ok = false;
                for (int vy : m.getDomain(y).getSet()) {
                    //log.println(name() + "::reviseAC3 with var = " + var.name + " meet x consider vx = " + vx + " consider vy = " + vy);
                    if(vx != vy + v){ ok = true; break; }
                }
                if(!ok) R.add(vx);
            }
            for(int vx: R){
                m.getDomain(x).remove(vx);
                //log.println(name + " -> remove (" + vx + ") from domain " + var.name);
            }
            return R.size() > 0;
        } else if(var == this.y){
            //log.println(name() + "::reviseAC3 with var = " + var.name + " meet y");
            for(int vy: m.getDomain(y).getSet()){
                boolean ok = false;
                for(int vx: m.getDomain(x).getSet()){
                    if(vx != vy + v){ ok = true; break; }
                }
                if(!ok) R.add(vy);
            }
            for(int vy: R){
                m.getDomain(y).remove(vy);
                //log.println(name + " -> remove (" + vy + ") from domain " + var.name);

            }
            return R.size() > 0;
        }
        return false;
    }

    @Override
    public boolean satisfy(CPModel m) {
        Set<Integer> X = m.getDomain(this.x).getSet();
        Set<Integer> Y = m.getDomain(this.y).getSet();
        if(X.size() == 1 && Y.size() == 1){
            for(int vx: X){
                for(int vy: Y){
                    if(a*vx != b*vy + this.v) return true;
                }
            }
        }
        return false;
    }
}

class DistanceAtLeast implements ConstraintCP{

    public DistanceAtLeast(VarIntCP x1, VarIntCP y1, VarIntCP x2, VarIntCP y2, double D){

    }
    @Override
    public boolean contains(VarIntCP x) {
        return false;
    }

    @Override
    public List<VarIntCP> getVariables() {
        return null;
    }

    @Override
    public boolean reviseAC3(VarIntCP x, CPModel m) {
        return false;
    }

    @Override
    public boolean satisfy(CPModel m) {
        return false;
    }

    @Override
    public String name() {
        return null;
    }
}

class AngleAtLeast implements ConstraintCP{

    public AngleAtLeast(VarIntCP x1, VarIntCP y1, VarIntCP x2, VarIntCP y2, VarIntCP x3, VarIntCP y3, double agl){

    }
    @Override
    public boolean contains(VarIntCP x) {
        return false;
    }

    @Override
    public List<VarIntCP> getVariables() {
        return null;
    }

    @Override
    public boolean reviseAC3(VarIntCP x, CPModel m) {
        return false;
    }

    @Override
    public boolean satisfy(CPModel m) {
        return false;
    }

    @Override
    public String name() {
        return null;
    }
}
class Domain{
    Set<Integer> domain;
    public Domain(Set<Integer> dom){
        this.domain = new TreeSet<Integer>();
        for(int v: dom) this.domain.add(v);
    }
    Set<Integer> getSet(){ return domain; }
    public void remove(int v){
        domain.remove(v);
    }
    public String toString(){
        //System.out.println("Domain.toString sz = " + domain.size());
        String s = "";
        for(int v: domain) s = s + v + " ";
        return s;
    }
    public Domain(int min, int max){
        this.domain = new TreeSet<Integer>();
        for(int v = min; v <= max; v++){
            //System.out.println("Constructor Domain add " + v);
            this.domain.add(v);
        }
        //System.out.println("Domain sz = " + this.domain.size());
    }
    public Domain clone(){
        //TreeSet<Integer> S = new TreeSet<Integer>();
        Domain D = new Domain(domain);
        return D;
    }
}
class CPModel{
    List<ConstraintCP> constraints;
    List<VarIntCP> variables;
    HashMap<VarIntCP, Integer> mVariable2Index;
    Domain[] domains;
    public PrintWriter log;


    public CPModel(PrintWriter log){
        this.log = log;
        this.constraints = new ArrayList<ConstraintCP>();
    }
    public void addConstraint(ConstraintCP c){
        constraints.add(c);
    }
    public VarIntCP selectNonSingletonVariable(){
        for(VarIntCP x: variables){
            if(getDomain(x).getSet().size() > 1) return x; // first-fit
        }
        return null;
    }
    public Domain getDomain(VarIntCP x){
        if(mVariable2Index.get(x) != null){
            int i = mVariable2Index.get(x);
            return domains[i];
        }
        return null;
    }
    public int getValue(VarIntCP x){
        if(mVariable2Index.get(x) != null){
            int i = mVariable2Index.get(x);
            for(int v: domains[i].getSet()) return v;
        }
        return -1;
    }
    public CPModel clone(){
        CPModel model = new CPModel(this.log);
        model.constraints = this.constraints;
        model.variables = this.variables;
        model.mVariable2Index = this.mVariable2Index;
        Domain[] clDomain = new Domain[domains.length];
        for(int i = 0; i < domains.length; i++)
            clDomain[i] = domains[i].clone();
        model.domains = clDomain;
        model.log = this.log;
        return model;
    }
    public void print(PrintWriter log){
        for(VarIntCP var: variables){
            Domain D = domains[mVariable2Index.get(var)];
            log.println(var.name + " = " + D.toString());
        }
    }
    public void print(){
        for(VarIntCP var: variables){
            Domain D = domains[mVariable2Index.get(var)];
            System.out.println(var.name + " = " + D.toString());
        }
    }
    public void setValue(VarIntCP x, int v){
        if(mVariable2Index.get(x) == null) return;
        int i = mVariable2Index.get(x);
        domains[i].getSet().clear();
        domains[i].getSet().add(v);// singleton
    }
    class VarIntCPComparator implements Comparator<VarIntCP>{
        @Override
        public int compare(VarIntCP v1, VarIntCP v2){
            return v1.name.compareTo(v2.name);
        }
    }
    public void close(){
        Set<VarIntCP> Vars = new TreeSet<VarIntCP>(new VarIntCPComparator());
        for(ConstraintCP c: constraints){
            List<VarIntCP> vars = c.getVariables();
            for(VarIntCP var: vars) Vars.add(var);
        }
        variables = new ArrayList<VarIntCP>();
        for(VarIntCP var: Vars) variables.add(var);
        mVariable2Index = new HashMap<VarIntCP, Integer>();
        for(int i = 0; i < variables.size(); i++) mVariable2Index.put(variables.get(i),i);
        domains = new Domain[variables.size()];
        for(int i = 0; i < variables.size(); i++){
            domains[i] = new Domain(variables.get(i).getDomain().getSet());
        }
    }
    List<ConstraintCP> getContraints(){
        return constraints;
    }
    List<VarIntCP> getVariables(){
        return variables;
    }
    boolean reviseAC3(ConstraintCP c, VarIntCP x){
        if(mVariable2Index.get(x) != null){
            int i = mVariable2Index.get(x);
            Domain D = domains[i];

        }
        return false;
    }
    boolean fail(){
        for(Domain D: domains){
            if(D.getSet().size() == 0) return true;
        }
        return false;
    }
    boolean solution(){
        boolean ok = true;
        for(ConstraintCP c: constraints)
            if(!c.satisfy(this)) {
                ok = false; break;
            };
        return ok;
    }
}
class PairInt{
    int i,j;
    public PairInt(int i, int j){
        this.i = i; this.j = j;
    }
    public String toString(){ return "{" + i + "," + j + "}"; }
}
class Propagator{
    public PrintWriter log;
    public boolean propagateAC3(CPModel m){
        List<ConstraintCP> constraints = m.getContraints();
        List<VarIntCP> variables = m.getVariables();
        boolean[][] inQueue = new boolean[constraints.size()][variables.size()];
        HashMap<ConstraintCP, Integer> mConstraint2Index = new HashMap<ConstraintCP, Integer>();
        for(int i = 0; i < constraints.size(); i++)
            mConstraint2Index.put(constraints.get(i),i);
        HashMap<VarIntCP, Integer> mVariable2Index = new HashMap<VarIntCP, Integer>();
        for(int j = 0; j < variables.size(); j++)
            mVariable2Index.put(variables.get(j),j);
        //HashMap<ConstraintCP, Integer> mConstraint2Index = new HashMap<ConstraintCP, Integer>();
        //HashMap<VarIntCP, Integer> mVariable2Index = new HashMap<VarIntCP, Integer>();

        Queue<PairInt> Q = new LinkedList<PairInt>();
        for(int i = 0; i < constraints.size(); i++){
            ConstraintCP c = constraints.get(i);
            List<VarIntCP> vars = c.getVariables();
            for(VarIntCP v: vars){
                int j = mVariable2Index.get(v);
                PairInt pi = new PairInt(i,j);
                Q.add(pi); inQueue[i][j] = true;
                //log.println("PropagateAC3, PUSH " + pi.toString() + ": " + c.name() + " var " + v.name);
            }
        }
        int cnt = 0;
        while(!Q.isEmpty()){
            //cnt++; if(cnt > 100) break;
            PairInt pi = Q.remove();
            //log.println("AC3 POP " + pi.toString() + ": " + constraints.get(pi.i).name() + " var " + variables.get(pi.j).name);
            inQueue[pi.i][pi.j] = false;
            ConstraintCP c = constraints.get(pi.i);
            VarIntCP x = variables.get(pi.j);
            //System.out.println("start " + c.name() + "::reviseAC3");
            boolean change = c.reviseAC3(x,m); //m.reviseAC3(c,x);
            //log.println("AC3 POP " + pi.toString() + ": " + constraints.get(pi.i).name() + " var " + variables.get(pi.j).name + " reviseAC3 = " + change);

            if(change){
                if(m.getDomain(x).getSet().size() == 0) return false;

                for(int i = 0; i < constraints.size(); i++) if(i != pi.i){
                    ConstraintCP c1 = constraints.get(i);
                    if(c1.contains(x)){
                        List<VarIntCP> vars1 = c1.getVariables();
                        for(VarIntCP x1: vars1){
                            int j = mVariable2Index.get(x1);
                            if(j != pi.j && !inQueue[i][j]){
                                PairInt pij = new PairInt(i,j);
                                Q.add(pij); inQueue[i][j] = true;
                                //log.println("AC3 from " + pi.toString() + " -> PUSH " + pij.toString() + ": " + constraints.get(pij.i).name() + " var " + variables.get(pij.j).name);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

}
class CPSearch{
    private Propagator propagator;
    PrintWriter log = null;
    CPModel solution = null;
    public CPSearch(PrintWriter log){
        this.log = log;
        log.println("CPSearch");
    }
    public void onSolution(CPModel m){
        //System.out.println("CPSearch::onSolution");
    }
    public void onFailure(CPModel m){

    }
    public void search(CPModel m){
        propagator = new Propagator();
        propagator.log = this.log;
        //HashMap<String, Integer> solution = new HashMap<String, Integer>();
        Stack<CPModel> S = new Stack<CPModel>();
        S.add(m);
        int cnt = 0;
        while(!S.isEmpty()){
            //cnt++; if(cnt > 100) break;
            m = S.pop();
            //log.println("POP m = "); m.print(log);
            //System.out.println("POP m = "); m.print();
            VarIntCP x = m.selectNonSingletonVariable();
            if(x == null) continue;
            for(int v: m.getDomain(x).getSet()){
                CPModel mv = m.clone();
                mv.setValue(x,v);
                propagator.propagateAC3(mv);
                if(mv.fail()){
                    //onFailure(mv);
                }else if(mv.solution()){
                    onSolution(mv); solution = mv;
                    //for(VarIntCP var: mv.getVariables()){
                    //    solution.put(var.name,mv.getValue(var));
                    //}
                    break;
                    //log.println("Solution: ");
                    //mv.print(log);
                    //log.println("-----------");
                }else {
                    S.add(mv);
                    //log.println("PUSH mv = "); mv.print(log);
                    //System.out.println("PUSH m = "); mv.print();
                }
            }
        }

        //if(log != null) log.close();
    }
}
class SudokuSearch extends CPSearch{
    VarIntCP[][] x;
    public SudokuSearch(VarIntCP[][] x, PrintWriter log){
        super(log);
        this.x = x;
    }
    @Override
    public void onSolution(CPModel m){
        System.out.println("SudokuSearch::onSolution");
        int N = x.length;
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                int v = m.getValue(x[i][j]);
                System.out.print(v + " ");
            }
            System.out.println();
        }
        System.out.println("------------");
    }
}
class AppSudoku{
    public static void main(String[] args){
        int N = 9;        int N2 = 3;
        //int N = 4;        int N2 = 2;

        VarIntCP[][] x = new VarIntCP[N][N];
        for(int i = 0; i < N; i++)
            for(int j = 0;j < N; j++)
                x[i][j] = new VarIntCP(1,N,"x[" + i + "," + j + "]");
        PrintWriter log = null;
        try{
            log = new PrintWriter(System.out);//new PrintWriter("log.txt");
        }catch (Exception e){ e.printStackTrace();}
        CPModel m = new CPModel(log);
        // alldifferent on rows
        for(int i = 0; i < N; i++){
            for(int j1 = 0; j1 < N-1; j1++)
                for(int j2 = j1+1; j2<N; j2++)
                    m.addConstraint(new Neq(x[i][j1],x[i][j2],"x[" + i + "," + j1 + "] != x[" + i + "," + j2 + "]",log));
        }
        // alldifferent on columns
        for(int j = 0; j < N; j++){
            for(int i1 = 0; i1 < N-1; i1++)
                for(int i2 = i1+1; i2< N; i2++)
                    m.addConstraint(new Neq(x[i1][j],x[i2][j],"x[" + i1 + "," + j + "] != x[ " + i2 + "," + j + "]",log));
        }
        // alldifferent on sub-squares
        for(int I = 0; I < N2; I++){
            for(int J = 0; J < N2; J++){
                for(int i1 = 0; i1 < N2; i1++) {
                    for (int j1 = 0; j1 < N2; j1++) {
                        for (int i2 = 0; i2 < N2; i2++) {
                            for (int j2 = 0; j2 < N2; j2++) {
                                if(i1 < i2 || i1 == i2 && j1 < j2){
                                    m.addConstraint(new Neq(x[N2*I+i1][N2*J+j1],x[N2*I+i2][N2*J+j2],x[N2*I+i1][N2*J+j1].name + " != " + x[N2*I+i2][N2*J+j2].name,log));
                                }
                            }
                        }
                    }
                }
            }
        }
        m.close();
        //CPSearch se = new CPSearch(log);
        CPSearch se = new SudokuSearch(x,log);
        se.search(m);
        if(log != null) log.close();
    }
}
class CSPSearch1 extends CPSearch{
    VarIntCP x,y,z;
    public CSPSearch1(VarIntCP x, VarIntCP y, VarIntCP z, PrintWriter log){
        super(log);
        this.x = x; this.y = y; this.z = z;
    }
    @Override
    public void onSolution(CPModel m){
        //System.out.println("CSPSearch1::onSolution");
        //System.out.println("x = " + m.getValue(x) + " y = " + m.getValue(y) + " z = " + m.getValue(z));
        //System.out.println("------------");
    }

}
class CSP1{
    public void run(){
        try{
            Scanner in = new Scanner(System.in);
            HashMap<String, VarIntCP> mID2Var = new HashMap<String, VarIntCP>();
            PrintWriter log = new PrintWriter(System.out);
            CPModel m = new CPModel(log);
            while(true){
                String line = in.nextLine();
                if(line.equals("#")) break;
                String[] st = line.split(" ");
                if(st[0].equals("Var")){// create variable
                    String name = st[1];
                    int min = Integer.valueOf(st[2]);
                    int max = Integer.valueOf(st[3]);
                    VarIntCP x = new VarIntCP(min,max,name);
                    mID2Var.put(name,x);
                }else if(st[0].equals("Eq")){
                    int a = Integer.valueOf(st[1]);
                    String x = st[2];
                    int b = Integer.valueOf(st[3]);
                    String y = st[4];
                    int v = Integer.valueOf(st[5]);
                    VarIntCP X = mID2Var.get(x);
                    VarIntCP Y = mID2Var.get(y);
                    ConstraintCP c = new Eq(a,X,b,Y,v,"Eq");
                    m.addConstraint(c);
                }else if(st[0].equals("Neq")){
                    int a = Integer.valueOf(st[1]);
                    String x = st[2];
                    int b = Integer.valueOf(st[3]);
                    String y = st[4];
                    int v = Integer.valueOf(st[5]);
                    VarIntCP X = mID2Var.get(x);
                    VarIntCP Y = mID2Var.get(y);
                    ConstraintCP c = new Neq(a,X,b,Y,v,"Neq");
                    m.addConstraint(c);

                }else if(st[0].equals("Leq")){
                    int a = Integer.valueOf(st[1]);
                    String x = st[2];
                    int b = Integer.valueOf(st[3]);
                    String y = st[4];
                    int v = Integer.valueOf(st[5]);
                    VarIntCP X = mID2Var.get(x);
                    VarIntCP Y = mID2Var.get(y);
                    ConstraintCP c = new Leq(a,X,b,Y,v,"Leq");
                    m.addConstraint(c);
                }else{

                }
            }
            m.close();
            //VarIntCP X = mID2Var.get("X");
            //VarIntCP Y = mID2Var.get("Y");
            //VarIntCP Z = mID2Var.get("Z");

            //CSPSearch1 se = new CSPSearch1(X,Y,Z,m.log);
            CPSearch se = new CPSearch(log);
            se.search(m);
            if(se.solution == null){
                System.out.println("-1");
            }else{
                System.out.println(mID2Var.keySet().size());
                for(String v: mID2Var.keySet()){
                    VarIntCP x = mID2Var.get(v);
                    System.out.println(v + " " + se.solution.getValue(x));
                }
            }
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public static void main(String[] args){
    }
}
class CPTest{
    public static void main(String[] args){
        int n = 8;
        VarIntCP[] x = new VarIntCP[n];
        for(int i =0; i < n; i++)
            x[i] = new VarIntCP(1,n,"x[" + i + "]");
        CPModel m = new CPModel(null);
        for(int i = 0; i < n; i++){
            for(int j = i+1; j < n; j++){
                m.addConstraint(new Neq(x[i],x[j],x[i].name + " != " + x[j].name,null));
            }
        }
        m.close();
        //m.print();
        CPSearch se = new CPSearch(null);
        se.search(m);
        /*
        VarIntCP x = new VarIntCP(1,3,"x");
        VarIntCP y = new VarIntCP(1,4,"y");
        VarIntCP z = new VarIntCP(2,3,"z");
        CPModel m = new CPModel();
        m.addConstraint(new Neq(x,y));
        m.addConstraint(new Neq(x,z));
        m.addConstraint(new Neq(y,z));
        m.addConstraint(new Neq(z,y,-1));
        //m.addConstraint(new Eq(x,z,2));
        m.close();
        //m.print();
        CPSearch se = new CPSearch();
        se.search(m);
        */
    }

}

class GraphPresentation{
    public static void main(String[] args){
        int n = 10; //nodes are numbered from 1, 2, . . ., n
        List<Integer>[] A = new List[n];
        CPModel m = new CPModel(null);
        VarIntCP[] x = new VarIntCP[n];
        VarIntCP[] y = new VarIntCP[n];


        for(int i = 0; i < n; i++) {
            for(int j: A[i]) {
                m.addConstraint(new DistanceAtLeast(x[i],y[i],x[j],y[j],10));
            }
        }
        for(int i = 0; i < n; i++){
            for(int j1: A[i]){
                for(int j2: A[i]){
                    if(j1 < j2){
                        m.addConstraint(new AngleAtLeast(x[i],y[i],x[j1],y[j1],x[j2],y[j2],20));
                    }
                }
            }
        }

        m.close();
    }

}
public class Main {
    public static void main(String[] args){
        //AppSudoku.main();
        //CSP1.main(null);
        CSP1 app = new CSP1();
        app.run();
    }
}

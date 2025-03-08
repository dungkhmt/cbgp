
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
    String name;
    VarIntCP x1, y1, x2, y2;
    Map<VarIntCP, Integer> mVarIndex;
    List<VarIntCP> variables;
    double D;

    public DistanceAtLeast(VarIntCP x1, VarIntCP y1, VarIntCP x2, VarIntCP y2, double D){
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.D = D;
        mVarIndex = new HashMap<>();
        mVarIndex.put(x1, 0); mVarIndex.put(y1, 1); mVarIndex.put(x2, 2); mVarIndex.put(y2, 3);
        variables = new ArrayList<>();
        variables.add(x1); variables.add(y1); variables.add(x2); variables.add(y2);
    }
    @Override
    public boolean contains(VarIntCP x) {
        return mVarIndex.get(x) != null;
    }

    @Override
    public List<VarIntCP> getVariables() {
        return variables;
    }

    @Override
    public boolean reviseAC3(VarIntCP x, CPModel m) {
        if (x != this.x1 && x != this.y1 && x != this.x2 && x != this.y2)
            return false;

        List<Integer> R = new ArrayList<>();
        if (x == this.x1) {
            for (int x1 : m.getDomain(this.x1).getSet()) {
                boolean ok = false;
                Set<Integer> Y1 = m.getDomain(this.y1).getSet();
                if (Y1.size() == 1) {
                    begin:
                    for (int y1 : Y1) {
                        Set<Integer> X2 = m.getDomain(this.x2).getSet();
                        if (X2.size() == 1) {
                            for (int x2 : X2) {
                                Set<Integer> Y2 = m.getDomain(this.y2).getSet();
                                if (Y2.size() == 1) {
                                    for (int y2 : Y2) {
                                        if (Geometry.distance(x1, y1, x2, y2) >= D) {
                                            ok = true;
                                            break begin;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!ok) R.add(x1);
            }
            for (int x1 : R) {
                m.getDomain(this.x1).remove(x1);
            }
            return R.size() > 0;
        }
        else if (x == this.y1) {
            for (int y1 : m.getDomain(this.y1).getSet()) {
                boolean ok = false;
                Set<Integer> X1 = m.getDomain(this.x1).getSet();
                if (X1.size() == 1) {
                    begin:
                    for (int x1 : X1) {
                        Set<Integer> X2 = m.getDomain(this.x2).getSet();
                        if (X2.size() == 1) {
                            for (int x2 : X2) {
                                Set<Integer> Y2 = m.getDomain(this.y2).getSet();
                                if (Y2.size() == 1) {
                                    for (int y2 : Y2) {
                                        if (Geometry.distance(x1, y1, x2, y2) >= D) {
                                            ok = true;
                                            break begin;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!ok) R.add(y1);
            }
            for (int y1 : R) {
                m.getDomain(this.y1).remove(y1);
            }
            return R.size() > 0;
        }
        else if (x == this.x2) {
            for (int x2 : m.getDomain(this.x2).getSet()) {
                boolean ok = false;
                Set<Integer> X1 = m.getDomain(this.x1).getSet();
                if (X1.size() == 1) {
                    begin:
                    for (int x1 : X1) {
                        Set<Integer> Y1 = m.getDomain(this.y1).getSet();
                        if (Y1.size() == 1) {
                            for (int y1 : Y1) {
                                Set<Integer> Y2 = m.getDomain(this.y2).getSet();
                                if (Y2.size() == 1) {
                                    for (int y2 : Y2) {
                                        if (Geometry.distance(x1, y1, x2, y2) >= D) {
                                            ok = true;
                                            break begin;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!ok) R.add(x2);
            }
            for (int x2 : R) {
                m.getDomain(this.x2).remove(x2);
            }
            return R.size() > 0;
        }
        else if (x == this.y2) {
            for (int y2 : m.getDomain(this.y2).getSet()) {
                boolean ok = false;
                Set<Integer> X1 = m.getDomain(this.x1).getSet();
                if (X1.size() == 1) {
                    begin:
                    for (int x1 : X1) {
                        Set<Integer> Y1 = m.getDomain(this.y1).getSet();
                        if (Y1.size() == 1) {
                            for (int y1 : Y1) {
                                Set<Integer> X2 = m.getDomain(this.x2).getSet();
                                if (X2.size() == 1) {
                                    for (int x2 : X2) {
                                        if (Geometry.distance(x1, y1, x2, y2) >= D) {
                                            ok = true;
                                            break begin;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!ok) R.add(y2);
            }
            for (int y2 : R) {
                m.getDomain(this.y2).remove(y2);
            }
            return R.size() > 0;
        }

        return false;
    }

    @Override
    public boolean satisfy(CPModel m) {
        Set<Integer> X1 = m.getDomain(x1).getSet();
        Set<Integer> Y1 = m.getDomain(y1).getSet();
        Set<Integer> X2 = m.getDomain(x2).getSet();
        Set<Integer> Y2 = m.getDomain(y2).getSet();
        if (X1.size() == 1 && Y1.size() == 1 && X2.size() == 1 && Y2.size() == 1) {
            for (int x1 : X1) {
                for (int y1 : Y1) {
                    for (int x2 : X2) {
                        for (int y2 : Y2) {
                            if (Geometry.distance(x1, y1, x2, y2) >= D) return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String name() {
        return name;
    }
}

class AngleAtLeast implements ConstraintCP{
    String name;
    VarIntCP x1, y1, x2, y2, x3, y3;
    Map<VarIntCP, Integer> mVarIndex;
    List<VarIntCP> variables;
    double D;
    public AngleAtLeast(VarIntCP x1, VarIntCP y1, VarIntCP x2, VarIntCP y2, VarIntCP x3, VarIntCP y3, double agl){
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.x3 = x3; this.y3 = y3; this.D = agl / 180 * Math.PI;
        mVarIndex = new HashMap<>();
        mVarIndex.put(x1, 0); mVarIndex.put(y1, 1); mVarIndex.put(x2, 2); mVarIndex.put(y2, 3); mVarIndex.put(x3, 4); mVarIndex.put(y3, 5);
        variables = new ArrayList<>();
        variables.add(x1); variables.add(y1); variables.add(x2); variables.add(y2); variables.add(x3); variables.add(y3);
    }
    @Override
    public boolean contains(VarIntCP x) {
        return mVarIndex.get(x) != null;
    }

    @Override
    public List<VarIntCP> getVariables() {
        return variables;
    }



    @Override
    public boolean reviseAC3(VarIntCP x, CPModel m) {
        if (x != this.x1 && x != this.y1 && x != this.x2 && x != this.y2 && x != this.x3 && x != this.y3)
            return false;
        List<Integer> R = new ArrayList<>();
        if (x == this.x1) {
            for (int x1 : m.getDomain(this.x1).getSet()) {
                boolean ok = false;
                Set<Integer> Y1 = m.getDomain(this.y1).getSet();
                if (Y1.size() == 1) {
                    begin:
                    for (int y1 : Y1) {
                        Set<Integer> X2 = m.getDomain(this.x2).getSet();
                        if (X2.size() == 1) {
                            for (int x2 : X2) {
                                Set<Integer> Y2 = m.getDomain(this.y2).getSet();
                                if (Y2.size() == 1) {
                                    for (int y2 : Y2) {
                                        Set<Integer> X3 = m.getDomain(this.x3).getSet();
                                        if (X3.size() == 1) {
                                            for (int x3 : X3) {
                                                for (int y3 : m.getDomain(this.y3).getSet()) {
                                                    if (Geometry.angle(x1, y1, x2, y2, x3, y3) >= D) {
                                                        ok = true;
                                                        break begin;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!ok) R.add(x1);
            }
            for (int x1 : R) {
                m.getDomain(this.x1).remove(x1);
            }
            return R.size() > 0;
        }
        else if (x == this.y1) {
            for (int y1 : m.getDomain(this.y1).getSet()) {
                boolean ok = false;
                Set<Integer> X1 = m.getDomain(this.x1).getSet();
                if (X1.size() == 1) {
                    begin:
                    for (int x1 : X1) {
                        Set<Integer> X2 = m.getDomain(this.x2).getSet();
                        if (X2.size() == 1) {
                            for (int x2 : X2) {
                                Set<Integer> Y2 = m.getDomain(this.y2).getSet();
                                if (Y2.size() == 1) {
                                    for (int y2 : Y2) {
                                        Set<Integer> X3 = m.getDomain(this.x3).getSet();
                                        if (X3.size() == 1) {
                                            for (int x3 : X3) {
                                                for (int y3 : m.getDomain(this.y3).getSet()) {
                                                    if (Geometry.angle(x1, y1, x2, y2, x3, y3) >= D) {
                                                        ok = true;
                                                        break begin;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!ok) R.add(y1);
            }
            for (int y1 : R) {
                m.getDomain(this.y1).remove(y1);
            }
            return R.size() > 0;
        }
        else if (x == this.x2) {
            for (int x2 : m.getDomain(this.x2).getSet()) {
                boolean ok = false;
                Set<Integer> X1 = m.getDomain(this.x1).getSet();
                if (X1.size() == 1) {
                    begin:
                    for (int x1 : X1) {
                        Set<Integer> Y1 = m.getDomain(this.y1).getSet();
                        if (Y1.size() == 1) {
                            for (int y1 : Y1) {
                                Set<Integer> Y2 = m.getDomain(this.y2).getSet();
                                if (Y2.size() == 1) {
                                    for (int y2 : Y2) {
                                        Set<Integer> X3 = m.getDomain(this.x3).getSet();
                                        if (X3.size() == 1) {
                                            for (int x3 : X3) {
                                                for (int y3 : m.getDomain(this.y3).getSet()) {
                                                    if (Geometry.angle(x1, y1, x2, y2, x3, y3) >= D) {
                                                        ok = true;
                                                        break begin;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!ok) {
                    R.add(x2);
                }
            }

            for (int x2 : R) {
                m.getDomain(this.x2).remove(x2);
            }
            return R.size() > 0;
        }
        else if (x == this.y2) {
            for (int y2 : m.getDomain(this.y2).getSet()) {
                boolean ok = false;
                Set<Integer> X1 = m.getDomain(this.x1).getSet();
                if (X1.size() == 1) {
                    begin:
                    for (int x1 : X1) {
                        Set<Integer> Y1 = m.getDomain(this.y1).getSet();
                        if (Y1.size() == 1) {
                            for (int y1 : Y1) {
                                Set<Integer> X2 = m.getDomain(this.x2).getSet();
                                if (X2.size() == 1) {
                                    for (int x2 : X2) {
                                        Set<Integer> X3 = m.getDomain(this.x3).getSet();
                                        if (X3.size() == 1) {
                                            for (int x3 : X3) {
                                                for (int y3 : m.getDomain(this.y3).getSet()) {
                                                    if (Geometry.angle(x1, y1, x2, y2, x3, y3) >= D) {
                                                        ok = true;
                                                        break begin;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!ok) R.add(y2);
            }
            for (int y2 : R) {
                m.getDomain(this.y2).remove(y2);
            }
            return R.size() > 0;
        }
        else if (x == this.x3) {
            for (int x3 : m.getDomain(this.x3).getSet()) {
                boolean ok = false;
                Set<Integer> X1 = m.getDomain(this.x1).getSet();
                if (X1.size() == 1) {
                    begin:
                    for (int x1 : X1) {
                        Set<Integer> Y1 = m.getDomain(this.y1).getSet();
                        if (Y1.size() == 1) {
                            for (int y1 : Y1) {
                                Set<Integer> X2 = m.getDomain(this.x2).getSet();
                                if (X2.size() == 1) {
                                    for (int x2 : X2) {
                                        Set<Integer> Y2 = m.getDomain(this.y2).getSet();
                                        if (Y2.size() == 1) {
                                            for (int y2 : Y2) {
                                                for (int y3 : m.getDomain(this.y3).getSet()) {
                                                    if (Geometry.angle(x1, y1, x2, y2, x3, y3) >= D) {
                                                        ok = true;
                                                        break begin;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!ok) R.add(x3);
            }
            for (int x3 : R) {
                m.getDomain(this.x3).remove(x3);
            }
            return R.size() > 0;
        }
        else if (x == this.y3) {
            for (int y3 : m.getDomain(this.y3).getSet()) {
                boolean ok = false;
                Set<Integer> X1 = m.getDomain(this.x1).getSet();
                if (X1.size() == 1) {
                    begin:
                    for (int x1 : X1) {
                        Set<Integer> Y1 = m.getDomain(this.y1).getSet();
                        if (Y1.size() == 1) {
                            for (int y1 : Y1) {
                                Set<Integer> X2 = m.getDomain(this.x2).getSet();
                                if (X2.size() == 1) {
                                    for (int x2 : X2) {
                                        Set<Integer> Y2 = m.getDomain(this.y2).getSet();
                                        if (Y2.size() == 1) {
                                            for (int y2 : Y2) {
                                                for (int x3 : m.getDomain(this.x3).getSet()) {
                                                    if (Geometry.angle(x1, y1, x2, y2, x3, y3) >= D) {
                                                        ok = true;
                                                        break begin;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!ok) R.add(y3);
            }
            for (int y3 : R) {
                m.getDomain(this.y3).remove(y3);
            }
            return R.size() > 0;
        }
        return false;
    }

    @Override
    public boolean satisfy(CPModel m) {
        Set<Integer> X1 = m.getDomain(x1).getSet();
        Set<Integer> Y1 = m.getDomain(y1).getSet();
        Set<Integer> X2 = m.getDomain(x2).getSet();
        Set<Integer> Y2 = m.getDomain(y2).getSet();
        Set<Integer> X3 = m.getDomain(x3).getSet();
        Set<Integer> Y3 = m.getDomain(y3).getSet();
        if (X1.size() == 1 && Y1.size() == 1 && X2.size() == 1 && Y2.size() == 1 && X3.size() == 1 && Y3.size() == 1) {
            for (int x1 : X1) {
                for (int y1 : Y1) {
                    for (int x2 : X2) {
                        for (int y2 : Y2) {
                            for (int x3 : X3) {
                                for (int y3 : Y3) {
                                    if (Geometry.angle(x1, y1, x2, y2, x3, y3) >= D) return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String name() {
        return name;
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

interface CPObjective {
    boolean isLessThan(CPObjective o);
}

class CPModel{
    CPObjective objective;
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
        model.objective = this.objective;
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

enum CPSearchOption {
    SEARCH_ALL_CONFIGURATIONS,
    SEARCH_ONE_CONFIGURATION,
    SEARCH_OPTIMIZE_CONFIGURATION
}
class Geometry {
    static double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }
    static double squareDistance(double x1, double y1, double x2, double y2){
        return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
    }
    static double angle(double x1, double y1, double x2, double y2, double x3, double y3){
        double cosA = dot(x2 - x1, y2 - y1, x3 - x1, y3 - y1) / distance(x1, y1, x2, y2) / distance(x1, y1, x3, y3);
        return Math.acos(cosA);
    }
    static double cross(double x1, double y1, double x2, double y2) {
        return x1 * y2 - y1 * x2;
    }
    static double dot(double x1, double y1, double x2, double y2) {
        return x1 * x2 + y1 * y2;
    }
}

class Point2D {
    private double x, y;
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public double distance(Point2D o) {
//        return Math.hypot(x - o.x, y - o.y);
        return Geometry.distance(x, y, o.getX(), o.getY());
    }
    public double distance(Line2D line) {
        return (line.getA() * x + line.getB() * y + line.getC()) / Math.hypot(line.getA(), line.getB());
    }

    public double cross(Point2D o) {
        return Geometry.cross(x, y, o.getY(), o.getX());
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
        Collections.sort(id, (a, b) -> {
            return Double.compare(angle.get(b), angle.get(a));
        });

        double min = Double.MAX_VALUE;
        double total = Math.PI * 2;
        for (int i = 0; i + 1 < points.size(); i++) {
            total -= angle.get(id.get(i + 1)) - angle.get(id.get(i));
            min = Math.min(min, angle.get(id.get(i + 1)) - angle.get(id.get(i)));
        }
        min = Math.min(min, total);
        return min;
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
        if (d == 0) return null;
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
        Line2D l1 = new Line2D(x.getY() - y.getY(), y.getX() - x.getX(), x.cross(y));
        Line2D l2 = new Line2D(seg.x.getY() - seg.y.getY(), seg.y.getX() - seg.x.getX(), seg.x.cross(seg.y));
        Point2D p = l1.intersect(l2);
        if (p == null) return null;
        if (p.distance(x) + p.distance(y) > x.distance(y) + 1e-9) return null;
        if (p.distance(seg.x) + p.distance(seg.y) > seg.x.distance(seg.y) + 1e-9) return null;
        return p;
    }
}



class CPSearch{
    private Propagator propagator;
    protected CPSearchOption option = CPSearchOption.SEARCH_ALL_CONFIGURATIONS;
    PrintWriter log = null;
    CPModel solution = null;
    public CPSearch(PrintWriter log){
        this.log = log;
        log.println("CPSearch");
    }
    public CPSearch(PrintWriter log, CPSearchOption option){
        this(log);
        this.option = option;
    }
    public void onSolution(CPModel m){
        //System.out.println("CPSearch::onSolution");
    }
    public void onFailure(CPModel m){

    }
    public boolean isWorseSolution(CPModel m){
        return false;
    }
    public void search(CPModel m){
        propagator = new Propagator();
        propagator.log = this.log;
        //HashMap<String, Integer> solution = new HashMap<String, Integer>();
        Stack<CPModel> S = new Stack<CPModel>();
        S.add(m);
        int cnt = 0;
        beginSearch:
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
                }else if(isWorseSolution(mv) && option == CPSearchOption.SEARCH_OPTIMIZE_CONFIGURATION) {
                    continue;
                } else if(mv.solution()){
                    onSolution(mv); solution = mv;
                    //for(VarIntCP var: mv.getVariables()){
                    //    solution.put(var.name,mv.getValue(var));
                    //}
                    if (option == CPSearchOption.SEARCH_ONE_CONFIGURATION) {
                        break beginSearch;
                    }
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

    public void printSolution(CPModel m) {

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

class GraphPresentationObjective implements CPObjective {
    private long intersectionCount;
    private double distanceCenterMedian;
    private double maxSumDistanceAndInverse;
    private double minAngle;
    private double minDistanceVertexEdge;
    GraphPresentationObjective() {
        intersectionCount = 0;
        distanceCenterMedian = 0;
        maxSumDistanceAndInverse = Double.MIN_VALUE;
        minAngle = Double.MAX_VALUE;
        minDistanceVertexEdge = Double.MAX_VALUE;
    }
    public double objective() {
        return -minDistanceVertexEdge - minAngle * 100 + maxSumDistanceAndInverse * 10000 + distanceCenterMedian * 1000000 + intersectionCount * 10000000L;
    }
    public boolean isLessThan(CPObjective o) {
        GraphPresentationObjective other = (GraphPresentationObjective) o;
        return Double.compare(objective(), other.objective()) < 0;
    }
}

class GraphPresentationSearch extends CPSearch{
    private VarIntCP[] x;
    private VarIntCP[] y;
    private int maxXCoordinate, minXCoordinate;
    private int maxYCoordinate, minYCoordinate;
    private int minMaxDistance = Integer.MAX_VALUE;
    private int minMaxCenterDifference = Integer.MAX_VALUE;
    private CPModel bestSolution;
    public GraphPresentationSearch(VarIntCP[] x, VarIntCP[] y, int minXCoordinate, int maxXCoordinate, int minYCoordinate, int maxYCoordinate, PrintWriter log){
        super(log, CPSearchOption.SEARCH_OPTIMIZE_CONFIGURATION);
        this.x = x;
        this.y = y;
        this.maxXCoordinate = maxXCoordinate;
        this.minXCoordinate = minXCoordinate;
        this.maxYCoordinate = maxYCoordinate;
        this.minYCoordinate = minYCoordinate;
    }
    @Override
    public void onSolution(CPModel m){
        int N = x.length;
        int maxDistance = Integer.MIN_VALUE;
        int totalX = 0, totalY = 0;
        int[] a = new int[N], b = new int[N];
        for (int i = 0; i < N; i++) {
            a[i] = m.getValue(x[i]);
            b[i] = m.getValue(y[i]);
            for (int j = 0; j < i; j++) {
                maxDistance = Math.max(maxDistance, (a[i] - a[j]) * (a[i] - a[j]) + (b[i] - b[j]) * (b[i] - b[j]));
            }
            totalX += a[i];
            totalY += b[i];
        }

        // | totalX / n - (maxX - minX) / 2 | ~ | total * 2 - (maxX - minX) * n |
        int centerDifference = Math.max(Math.abs(totalX * 2 - (maxXCoordinate - minXCoordinate) * N),
                                        Math.abs(totalY * 2 - (maxYCoordinate - minYCoordinate) * N));

        if (minMaxDistance > maxDistance || (minMaxDistance == maxDistance && minMaxCenterDifference > centerDifference)) {
            minMaxDistance = maxDistance;
            minMaxCenterDifference = centerDifference;
            bestSolution = m;
        }
        for (int i = 0; i < N; i++) {
            System.err.print("(" + m.getValue(x[i]) + " " + m.getValue(y[i]) + ") ");
        }
        System.err.println("-----------------\n");
    }

    @Override
    public boolean isWorseSolution(CPModel m) {
        return bestSolution != null && ((GraphPresentationObjective) m.objective).isLessThan((GraphPresentationObjective) bestSolution.objective);
    }

    @Override
    public void printSolution(CPModel m) {
        int n = x.length;
        for (int i = 0; i < n; i++) {
            System.out.println(x[i].name + " = " + m.getValue(x[i]) + " | " + y[i].name + " = " + bestSolution.getValue(y[i]));
        }
    }
}

class GraphPresentation{
    public static void main(String[] args){
        int n = 10; //nodes are numbered from 1, 2, . . ., n
        List<Integer>[] A = new List[n];
        CPModel m = new CPModel(null);
        VarIntCP[] x = new VarIntCP[n];
        VarIntCP[] y = new VarIntCP[n];
        for (int i = 0; i < n; i++) {
            x[i] = new VarIntCP(0, 20, "x[" + i + "]");
            y[i] = new VarIntCP(0, 20, "y[" + i + "]");
            A[i] = new ArrayList();
        }

        A[0].add(1);
        A[1].add(2); A[1].add(4); A[1].add(7);
        A[2].add(3); A[2].add(7); A[2].add(9);
        A[3].add(5); A[3].add(6);
        A[4].add(5); A[4].add(7); A[4].add(8); A[4].add(9);
        A[6].add(8); A[6].add(9);
        A[7].add(8);

        boolean[] used = new boolean[n];
        for(int i = 0; i < n; i++) {
            for(int j: A[i]) {
                m.addConstraint(new DistanceAtLeast(x[i],y[i],x[j],y[j],10));
                used[j] = true;
            }
            for (int j = 0; j < n; j++) {
                if (used[j]) {
                    used[j] = false;
                    continue;
                }
                m.addConstraint(new DistanceAtLeast(x[i], y[i], x[j], y[j], 2));
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
        System.out.println(n);

        PrintWriter log = new PrintWriter(System.out);
//        CPSearch se = new CPSearch(log);
        CPSearch se = new GraphPresentationSearch(x, y, 0, 20, 0, 20, log);
        se.search(m);
        se.printSolution(m);
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

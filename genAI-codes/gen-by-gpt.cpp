// pqtree_booth_lueker.cpp
// C++17. PQ-tree cho ràng buộc "S liên tiếp" đúng mô tả trong paper Booth–Lueker.
// API:
//   PQTree T(m);                  // vũ trụ U = {0..m-1}
//   bool ok = T.reduce(S);        // áp ràng buộc "S liên tiếp"
//   auto ord = T.frontier();      // lấy một hoán vị hợp lệ
//
// BUBBLE: gán FULL cho lá ∈ S, dồn need lên ancestor, xếp lá FULL vào hàng đợi.
// REDUCE: xử lý pertinent subtree từ lá lên, áp templates P0..P6, Q0..Q3.
// Lazy-stamp đảm bảo chỉ đụng các nút pertinent → O(n + Σ|S|) qua chuỗi reduce.

#include <bits/stdc++.h>
using namespace std;

struct PQTree {
    // ===== Kiểu dữ liệu theo paper =====
    enum Kind  { LEAF, P, Q };
    enum Label { UNMARKED, EMPTY, FULL, PARTIAL };

    struct Node {
        Kind type;                      // type(v)
        int  father = -1;               // father(v)
        vector<int> sons;               // sons(v)
        int  leaf_id = -1;              // id lá (nếu LEAF)

        // Trạng thái per-reduce (lazy bằng 'vis')
        int   vis = 0;                  // dấu lần reduce hiện tại
        Label label = UNMARKED;         // label(v)
        int   need = 0;                 // số con pertinent cần chờ
        int   done = 0;                 // số con pertinent đã xử lý
        bool  inQ  = false;             // đã vào queue chưa
    };

    vector<Node> T;
    vector<int> leaf_at;                // map leaf_id -> index node
    int root = -1;
    int stamp = 1;

    // ===== Khởi tạo =====
    explicit PQTree(int m=0){ init(m); }
    void init(int m){
        T.clear(); leaf_at.assign(m, -1); stamp = 1;
        root = new_node(P);
        T.reserve(2*m + 32);
        for(int i=0;i<m;i++){
            int u = new_node(LEAF);
            T[u].leaf_id = i;
            link(root, u);
            leaf_at[i] = u;
        }
    }

    // ===== REDUCE(T,S) =====
    bool reduce(vector<int> S){
        if(S.empty()) return true;
        sort(S.begin(), S.end());
        S.erase(unique(S.begin(), S.end()), S.end());

        deque<int> Q; int rootPert = -1;
        if(!BUBBLE(S, Q, rootPert)) return false;

        while(!Q.empty()){
            int u = Q.front(); Q.pop_front();
            Node &U = touch(u);

            if(U.type != LEAF){
                if(!apply_templates(u, rootPert)) return false;
            }

            int p = U.father;
            if(p == -1) continue;
            if(!is_pertinent(p)) continue;

            Node &P = touch(p);
            P.done += 1;
            if(P.done == P.need && !P.inQ){
                P.inQ = true;
                Q.push_back(p);
            }
        }
        return true;
    }

    // Một biên hợp lệ
    vector<int> frontier() const {
        vector<int> out; out.reserve(leaf_at.size());
        dfs_frontier(root, out);
        return out;
    }

private:
    // ===== Tiện ích cơ bản =====
    int new_node(Kind k){ T.push_back(Node{k}); return (int)T.size()-1; }
    void link(int f,int c){ T[f].sons.push_back(c); T[c].father=f; }

    Node& touch(int v){
        Node &x = T[v];
        if(x.vis != stamp){
            x.vis = stamp;
            x.label = UNMARKED;
            x.need = x.done = 0;
            x.inQ = false;
        }
        return x;
    }
    bool is_pertinent(int v) const {
        const Node &x = T[v];
        return x.vis == stamp && (x.need>0 || x.label==FULL || x.label==PARTIAL);
    }
    Label child_label(int v) const {
        const Node &x = T[v];
        if(x.vis != stamp) return EMPTY;   // ngoài pertinent ⇒ EMPTY
        return x.label;
    }
    void set_sons(int u, const vector<int>& seq){
        T[u].sons = seq;
        for(int w: T[u].sons) T[w].father = u;
    }
    void dfs_frontier(int v, vector<int>& out) const {
        const Node &x = T[v];
        if(x.type == LEAF){ out.push_back(x.leaf_id); return; }
        for(int w: x.sons) dfs_frontier(w, out);
    }

    // ===== BUBBLE =====
    bool BUBBLE(const vector<int>& S, deque<int>& Q, int& rootPertOut){
        ++stamp; rootPertOut = -1;

        for(int a: S){
            if(a<0 || a >= (int)leaf_at.size()) return false;
            int u = leaf_at[a];
            touch(u).label = FULL;               // gán FULL cho lá ∈ S
            if(!T[u].inQ){ T[u].inQ=true; Q.push_back(u); }
            // dồn need lên ancestor
            for(int p=T[u].father; p!=-1; p=T[p].father) touch(p).need += 1;
        }
        // tìm ứng viên gốc pertinent: cha không pertinent
        for(int v=0; v<(int)T.size(); ++v){
            const Node &x = T[v];
            if(x.vis!=stamp || x.need==0) continue;
            int p = x.father;
            if(p==-1 || T[p].vis!=stamp || T[p].need==0){ rootPertOut = v; break; }
        }
        return !Q.empty();
    }

    // ===== Templates (P0..P6, Q0..Q3) =====
    struct Split {
        vector<int> E, F, P;
        int unknown = 0;
    };
    Split split_children(int u) const {
        Split s;
        for(int w: T[u].sons){
            Label lb = child_label(w);
            if(lb==EMPTY) s.E.push_back(w);
            else if(lb==FULL) s.F.push_back(w);
            else if(lb==PARTIAL) s.P.push_back(w);
            else s.unknown++;
        }
        return s;
    }

    struct QView {
        int L=-1, R=-1; // chỉ số biên không-E trên sons(u)
        vector<pair<Label, vector<int>>> blocks; // chuỗi block non-E
        bool ok=true;
    };
    QView build_qview(int u) const {
        const auto &ch = T[u].sons;
        int m = (int)ch.size();
        QView vw;
        int L=0; while(L<m && child_label(ch[L])==EMPTY) ++L;
        if(L==m){ vw.L=-1; vw.R=-1; return vw; } // all E
        int R=m-1; while(R>=0 && child_label(ch[R])==EMPTY) --R;
        vw.L=L; vw.R=R;
        Label cur = child_label(ch[L]);
        if(cur==EMPTY){ vw.ok=false; return vw; }
        vector<int> curblk{ch[L]};
        for(int i=L+1;i<=R;++i){
            Label lb = child_label(ch[i]);
            if(lb==EMPTY){ vw.ok=false; return vw; } // E nằm giữa ⇒ illegal
            if(lb==cur) curblk.push_back(ch[i]);
            else{
                vw.blocks.push_back({cur, curblk});
                cur = lb; curblk.clear(); curblk.push_back(ch[i]);
            }
        }
        vw.blocks.push_back({cur, curblk});
        return vw;
    }

    bool is_root_of_pertinent(int u) const {
        int p=T[u].father;
        if(p==-1) return true;
        const Node &P = T[p];
        return (P.vis!=stamp) || (P.need==0);
    }

    bool apply_templates(int u, int rootPert){
        Node &X = T[u];
        if(X.type == P) return apply_P(u);
        else            return apply_Q(u);
    }

    // ---- P: P0..P6 ----
    bool apply_P(int u){
        Node &U = touch(u);
        Split s = split_children(u);
        if(s.unknown) return false;

        // P0: all E
        if(s.F.empty() && s.P.empty()){ U.label = EMPTY; return true; }
        // P1: all F
        if(s.E.empty() && s.P.empty()){ U.label = FULL;  return true; }

        // không có partial
        if(s.P.empty()){
            if(is_root_of_pertinent(u)){
                // P2 (root): gom FULL vào một P-con, E ở hai phía
                vector<int> seq;
                for(int w: s.E) seq.push_back(w);
                if(s.F.size() >= 2){
                    int p = new_node(P);
                    set_sons(p, s.F);
                    seq.push_back(p);
                }else{
                    for(int w: s.F) seq.push_back(w);
                }
                set_sons(u, seq);
                U.label = UNMARKED; // root hoàn tất
                return true;
            }else{
                // P3 (non-root): thay bằng Q với [E*][F*]
                int q = new_node(Q);
                vector<int> seq;
                for(int w: s.E) seq.push_back(w);
                for(int w: s.F) seq.push_back(w);
                set_sons(q, seq);
                set_sons(u, {q});
                U.label = PARTIAL;
                return true;
            }
        }

        // đúng một partial
        if((int)s.P.size()==1){
            if(is_root_of_pertinent(u)){
                // P4 (root): [E*] P [F*]
                vector<int> seq;
                if(!s.E.empty()){
                    if(s.E.size()==1) seq.push_back(s.E[0]);
                    else { int pe=new_node(P); set_sons(pe, s.E); seq.push_back(pe); }
                }
                seq.push_back(s.P[0]);
                if(!s.F.empty()){
                    if(s.F.size()==1) seq.push_back(s.F[0]);
                    else { int pf=new_node(P); set_sons(pf, s.F); seq.push_back(pf); }
                }
                set_sons(u, seq);
                U.label = UNMARKED;
                return true;
            }else{
                // P5 (non-root): thay bằng Q: [E*] P [F*]
                int q=new_node(Q);
                vector<int> seq;
                for(int w: s.E) seq.push_back(w);
                seq.push_back(s.P[0]);
                for(int w: s.F) seq.push_back(w);
                set_sons(q, seq);
                set_sons(u, {q});
                U.label = PARTIAL;
                return true;
            }
        }

        // hai partial (chỉ root)
        if((int)s.P.size()==2){
            if(!is_root_of_pertinent(u)) return false; // P6
            int q=new_node(Q);
            vector<int> mid = s.F;
            vector<int> seq; seq.reserve(2 + (int)mid.size());
            seq.push_back(s.P[0]);
            if(!mid.empty()){
                if(mid.size()==1) seq.push_back(mid[0]);
                else { int pf=new_node(P); set_sons(pf, mid); seq.push_back(pf); }
            }
            seq.push_back(s.P[1]);
            set_sons(q, seq);
            set_sons(u, {q});
            U.label = UNMARKED;
            return true;
        }

        // >2 partial ⇒ fail
        return false;
    }

    // ---- Q: Q0..Q3 ----
    bool apply_Q(int u){
        Node &U = touch(u);

        // Q0/Q1: all E hoặc all F
        bool allE=true, allF=true;
        for(int w: T[u].sons){
            Label lb = child_label(w);
            if(lb != EMPTY) allE=false;
            if(lb != FULL ) allF=false;
        }
        if(allE){ U.label = EMPTY; return true; }
        if(allF){ U.label = FULL;  return true; }

        // Chung: E chỉ ở hai đầu; vùng giữa là non-E
        QView vw = build_qview(u);
        if(!vw.ok) return false;           // E nằm giữa ⇒ bất hợp pháp
        if(vw.L==-1){ U.label = EMPTY; return true; } // all E (đã bao)

        int Pcnt=0; for(auto &bk: vw.blocks) if(bk.first==PARTIAL) ++Pcnt;

        if(Pcnt==0){
            // Q2: singly-partial chỉ với FULL block giữa (E ở rìa). Hợp lệ.
            U.label = PARTIAL;
            return true;
        }
        if(Pcnt==1){
            // Q2: một partial kèm FULL block liền kề, E chỉ ở rìa. Hợp lệ.
            U.label = is_root_of_pertinent(u) ? UNMARKED : PARTIAL;
            return true;
        }
        if(Pcnt==2){
            // Q3: doubly-partial [P][F*][P], chỉ hợp lệ tại root
            if(!is_root_of_pertinent(u)) return false;
            U.label = UNMARKED;
            return true;
        }
        return false;
    }
};

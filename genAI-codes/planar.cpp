#include <bits/stdc++.h>
using namespace std;

/* =======================
   TIỆN ÍCH CẠNH / ĐỒ THỊ
   ======================= */
static inline uint64_t undKey(int a,int b){ if(a>b) swap(a,b); return (uint64_t)a<<32 | (uint32_t)b; }

struct GraphSimple {
    int n;
    vector<vector<int>> adj;
    unordered_set<uint64_t> Eset;
    GraphSimple(int n=0):n(n),adj(n){}
    void addEdge(int u,int v){
        if(u==v) return;
        uint64_t k=undKey(u,v);
        if(Eset.insert(k).second){
            adj[u].push_back(v);
            adj[v].push_back(u);
        }
    }
    bool hasEdge(int u,int v) const { return Eset.count(undKey(u,v)); }
};

/* =======================
   DCEL: HALF-EDGE
   ======================= */
struct Half {
    int from=-1, to=-1;
    int twin=-1, next=-1, prev=-1, face=-1;
    Half(){}
    Half(int u,int v):from(u),to(v){}
};
struct DCEL {
    int n;
    vector<Half> H;
    vector<vector<int>> around;   // rotation CW: danh sách half-id rời v theo thứ tự CW
    unordered_map<uint64_t,int> dir2he; // (u,v)->half id
    unordered_set<uint64_t> Eund; // cạnh vô hướng đang có trong DCEL
    DCEL(int n=0):n(n),around(n){}
    static inline uint64_t dirKey(int a,int b){ return (uint64_t)a<<32 | (uint32_t)b; }

    int newHalf(int u,int v){
        int id=(int)H.size();
        H.emplace_back(u,v);
        dir2he[dirKey(u,v)]=id;
        around[u].push_back(id); // tạm; sẽ sắp đặt lại khi cần
        return id;
    }
    bool hasEdgeUnd(int a,int b) const { return Eund.count(undKey(a,b)); }
    void addEdgeUndMark(int a,int b){
        if(a==b) return;
        if(hasEdgeUnd(a,b)) return;
        int h1=newHalf(a,b), h2=newHalf(b,a);
        H[h1].twin=h2; H[h2].twin=h1;
        Eund.insert(undKey(a,b));
    }

    // Thiết lập next/prev theo rotation CW quanh mỗi đỉnh.
    // Quy tắc: với half h=u->v, next(h) = half (v->w_prev), nơi w_prev là hàng xóm đứng TRƯỚC u trong vòng CW quanh v.
    void wireByCW(const vector<vector<int>>& cwNei){
        // Đưa around[v] đúng thứ tự CW theo cwNei
        for(int v=0; v<n; v++){
            vector<int> rot;
            rot.reserve(cwNei[v].size());
            for(int w: cwNei[v]){
                auto it = dir2he.find(dirKey(v,w));
                if(it!=dir2he.end()) rot.push_back(it->second);
            }
            around[v].swap(rot);
        }
        // Gán next/prev theo quy tắc
        for(int vid=0; vid<n; vid++){
            auto &rot = around[vid];
            int m=(int)rot.size();
            // Với mỗi cạnh (vid -> nbr), ta cần tìm vị trí (nbr -> vid) trong vòng của nbr.
            for(int idx=0; idx<m; idx++){
                int h = rot[idx];
                int u = H[h].from, v = H[h].to; // u==vid
                // vị trí của (v->u) trong vòng v:
                auto &rotV = around[v];
                int M=(int)rotV.size(), posVU=-1;
                for(int i=0;i<M;i++){
                    if(H[rotV[i]].to==u){ posVU=i; break; }
                }
                if(posVU==-1) throw runtime_error("rotation incomplete");
                int prevIdx = (posVU - 1 + M) % M; // CW predecessor
                int hvwPrev = rotV[prevIdx];
                H[h].next = hvwPrev;
                H[hvwPrev].prev = h;
            }
        }
    }

    // Liệt kê các mặt (vòng theo next). Đồng thời gán face id cho half.
    vector<vector<int>> computeFaces(){
        for(auto &e:H) e.face=-1;
        vector<vector<int>> faces;
        for(int s=0;s<(int)H.size();s++){
            if(H[s].face!=-1) continue;
            vector<int> cyc;
            int x=s;
            do{
                cyc.push_back(x);
                H[x].face = (int)faces.size();
                x = H[x].next;
            }while(x!=s);
            faces.push_back(cyc);
        }
        return faces;
    }

    // Tìm trên một mặt có chứa cả u và v; trả về (a,b) là half-ids kết thúc tại u và v theo chu trình mặt đó.
    bool findOnFace_uv(int f, int u, int v, int &a, int &b){
        auto &ring = facesById[f];
        int m=(int)ring.size();
        int iu=-1, iv=-1;
        for(int i=0;i<m;i++){
            int he=ring[i];
            if(H[he].to==u) iu=i;
            if(H[he].to==v) iv=i;
        }
        if(iu==-1 || iv==-1) return false;
        a=ring[iu]; b=ring[iv]; return true;
    }

    // Chèn đường chéo trong một mặt: a kết thúc tại u, b kết thúc tại v, và dọc theo vòng ... a -> ... -> b ...
    // Sau chèn, mặt cũ tách làm hai.
    void insertDiagonalOnFace(int a,int b,int u,int v){
        int h1=newHalf(u,v), h2=newHalf(v,u);
        H[h1].twin=h2; H[h2].twin=h1;
        int nxtA=H[a].next, nxtB=H[b].next;

        // vòng 1: a -> h1 -> nxtB -> ... -> b -> h2 -> nxtA -> ...
        H[a].next=h1; H[h1].prev=a;
        H[h1].next=nxtB; H[nxtB].prev=h1;
        H[b].next=h2; H[h2].prev=b;
        H[h2].next=nxtA; H[nxtA].prev=h2;

        // chèn vào rotation: sau 'a' quanh u, sau 'b' quanh v
        insertAfterAround(u, a, h1);
        insertAfterAround(v, b, h2);

        Eund.insert(undKey(u,v));
        // cập nhật faces sẽ làm lại bằng computeFaces() khi cần
    }

    void insertAfterAround(int v, int afterHalf, int newHalfId){
        auto &rot = around[v];
        int pos=-1;
        for(int i=0;i<(int)rot.size();i++) if(rot[i]==afterHalf){ pos=i; break; }
        if(pos==-1){ rot.push_back(newHalfId); return; }
        rot.insert(rot.begin()+pos+1, newHalfId);
    }

    // LƯU Ý: facesById được cache tạm thời giữa các thao tác
    vector<vector<int>> facesById;
    void refreshFaces(){ facesById = computeFaces(); }

    // Tìm vị trí "nêm" u-v-w theo một mặt: cần ... u, v, w ... liên tiếp; trả về half kết thúc tại u và w.
    bool findWedge(int u,int v,int w,int &hu,int &hw){
        refreshFaces();
        for(auto &cyc: facesById){
            int m=cyc.size();
            for(int i=0;i<m;i++){
                int heU=cyc[i];
                int heV=cyc[(i+1)%m];
                int heW=cyc[(i+2)%m];
                if(H[heU].to==u && H[heV].to==v && H[heW].to==w){
                    hu=heU; hw=heW; return true;
                }
            }
        }
        return false;
    }

    // Tìm trên BẤT KỲ mặt chứa u và v, trả về (a,b) như trên
    bool findPositionsAnyFace(int u,int v,int &a,int &b){
        refreshFaces();
        for(int f=0; f<(int)facesById.size(); f++){
            if(findOnFace_uv(f,u,v,a,b)) return true;
        }
        return false;
    }

    // Dựng adjacency từ DCEL (không trùng cạnh)
    vector<vector<int>> buildAdj(){
        vector<unordered_set<int>> S(n);
        for(auto &e:H) if(e.from!=-1 && e.to!=-1) S[e.from].insert(e.to);
        vector<vector<int>> adj(n);
        for(int i=0;i<n;i++) adj[i]=vector<int>(S[i].begin(),S[i].end());
        return adj;
    }
};

/* ===============================================
   1) NHÚNG PHẲNG bằng DMP đơn giản trên DCEL
   - Khởi tạo từ một chu trình đầu tiên.
   - Lặp: chèn cạnh có 2 đầu trên cùng mặt; chèn đường đi mới giữa 2 đỉnh biên cùng mặt
     với các đỉnh trung gian chưa nhúng.
   Lưu ý: độ phức tạp O(n^2) nhưng đúng cho đồ thị phẳng.
   =============================================== */
static bool findFirstCycle(const GraphSimple &g, vector<int>&cycle){
    int n=g.n;
    vector<int> vis(n,0), par(n,-1), st;
    bool found=false; int su=-1, sv=-1;
    function<void(int)> dfs=[&](int u){
        vis[u]=1;
        for(int v: g.adj[u]){
            if(v==par[u]) continue;
            if(vis[v]==0){ par[v]=u; dfs(v); if(found) return; }
            else if(vis[v]==1 && !found){ su=u; sv=v; found=true; return; }
        }
        vis[u]=2;
    };
    for(int i=0;i<n && !found;i++) if(!g.adj[i].empty() && vis[i]==0) dfs(i);
    if(!found) return false;
    // build cycle su -> ... -> sv -> su
    vector<int> path;
    int x=su;
    path.push_back(sv);
    while(x!=sv){ path.push_back(x); x=par[x]; }
    reverse(path.begin()+1, path.end());
    cycle = path;
    return true;
}

static bool dmpEmbedPlanar(const GraphSimple &g, DCEL &dcel){
    int n=g.n;
    dcel = DCEL(n);

    // 1) lấy chu trình đầu tiên; nếu không có (cây) thì dùng 1 cạnh
    vector<int> C;
    if(!findFirstCycle(g,C)){
        // cây: lấy một cạnh bất kỳ
        int u=0, v=-1;
        for(int i=0;i<n;i++){ if(!g.adj[i].empty()){ u=i; v=g.adj[i][0]; break; } }
        if(v==-1){
            // đồ thị 1 đỉnh
            return true; // embedding rỗng, không cạnh
        }
        C = {u,v};
    }
    // 2) khởi tạo DCEL với vòng C (tạo 2 mặt: "ngoài" và "trong")
    // tạo các half với thứ tự CCW trên vành trong, CW trên vành ngoài
    int k=(int)C.size();
    vector<int> h_ccw(k), h_cw(k);
    for(int i=0;i<k;i++){
        int u=C[i], v=C[(i+1)%k];
        int h1=dcel.newHalf(u,v);
        int h2=dcel.newHalf(v,u);
        dcel.H[h1].twin=h2; dcel.H[h2].twin=h1;
        h_ccw[i]=h1; h_cw[i]=h2;
        dcel.Eund.insert(undKey(u,v));
    }
    // móc next/prev cho vòng ccw
    for(int i=0;i<k;i++){ dcel.H[h_ccw[i]].next = h_ccw[(i+1)%k]; dcel.H[h_ccw[(i+1)%k]].prev = h_ccw[i]; }
    // móc next/prev cho vòng cw (đi ngược)
    for(int i=0;i<k;i++){ dcel.H[h_cw[i]].next = h_cw[(i-1+k)%k]; dcel.H[h_cw[(i-1+k)%k]].prev = h_cw[i]; }
    // gán mặt
    dcel.refreshFaces();

    // 3) tập trạng thái: đã-nhúng
    vector<char> inEmb(n,false);
    for(int v: C) inEmb[v]=true;

    // 4) Vòng lặp chèn
    bool progress=true; int guard=0;
    while(progress){
        if(++guard > n* n + 4*(int)dcel.H.size()) break;
        progress=false;

        // 4a) chèn mọi cạnh có 2 đầu đã nằm trên cùng một mặt
        // duyệt mọi cặp u-v là cạnh gốc nhưng chưa có trong DCEL
        for(int u=0; u<n; u++){
            if(!inEmb[u]) continue;
            for(int v: g.adj[u]){
                if(u>v) continue;
                if(dcel.hasEdgeUnd(u,v)) continue;
                if(!inEmb[v]) continue;
                // tìm một mặt chứa cả u và v
                int a,b;
                if(dcel.findPositionsAnyFace(u,v,a,b)){
                    dcel.insertDiagonalOnFace(a,b,u,v);
                    progress=true;
                }
            }
        }

        // 4b) thử chèn đỉnh/đường đi mới: với mỗi mặt, BFS giữa 2 đỉnh biên thuộc mặt
        //     cho phép các đỉnh trung gian là CHƯA NHÚNG; hai đầu là đỉnh trên mặt đó.
        dcel.refreshFaces();
        for(auto &ring : dcel.facesById){
            // lấy danh sách đỉnh theo chu trình mặt
            vector<int> B; B.reserve(ring.size());
            for(int he: ring) B.push_back(dcel.H[he].to);
            int m=B.size();
            if(m<2) continue;
            // thử vài cặp cách xa để có tiến triển
            for(int i=0;i<m && !progress;i++){
                for(int j=i+1;j<m && !progress;j++){
                    int s=B[i], t=B[j];
                    // BFS trong đồ thị g: chỉ cho phép đỉnh trung gian với inEmb==false
                    vector<int> par(n,-1);
                    deque<int> dq;
                    vector<char> vis(n,0);
                    dq.push_back(s); vis[s]=1;
                    while(!dq.empty()){
                        int x=dq.front(); dq.pop_front();
                        if(x==t) break;
                        for(int y: g.adj[x]){
                            if(vis[y]) continue;
                            if(!inEmb[y] || y==t){ // cho phép đi qua chưa-nhúng; t có thể đã nhúng
                                vis[y]=1; par[y]=x; dq.push_back(y);
                            }
                        }
                    }
                    if(!vis[t]) continue;
                    // dựng đường đi s=path[0] ... path[k]=t
                    vector<int> path; for(int x=t;x!=-1;x=par[x]) path.push_back(x);
                    reverse(path.begin(), path.end());
                    // chèn từng cạnh mới của path theo mặt hiện tại
                    // tìm half kết thúc tại s và t trên mặt hiện tại
                    // với chu trình mặt 'ring', ta đã có thứ tự; lấy vị trí s,t
                    int posS=-1,posT=-1;
                    for(int p=0;p<m;p++){ if(B[p]==s) posS=p; if(B[p]==t) posT=p; }
                    if(posS==-1 || posT==-1) continue;

                    int heS = ring[posS];
                    int heT = ring[posT];
                    // chèn các đỉnh/cạnh path[1..k-1]
                    for(int p=1; p<(int)path.size(); p++){
                        int a = dcel.H[heS].to; // s hiện tại
                        int b = path[p];
                        // chèn cạnh (a,b) trên mặt hiện tại giữa heS và vị trí b trên mặt
                        // nếu b chưa ở trên mặt (chưa nhúng), ta chèn (a,b) dùng heS và heT (tạm), sau đó heS = half mới từ a->b
                        int A=heS, Bpos=-1;
                        // tìm half kết thúc tại b trên mặt hiện tại, nếu đã nhúng
                        int a2,b2;
                        if(dcel.findOnFace_uv(dcel.H[heS].face, b, dcel.H[heT].to, a2,b2)){
                            // b có trên mặt: a2 kết thúc tại b
                            Bpos = a2;
                        }
                        if(Bpos==-1){
                            // b chưa trên mặt: dùng heS và heT (cùng một mặt) để thêm (a,b),
                            // tách mặt làm hai; mặt chứa heT đổi id, nhưng heS->next mới thuộc mặt chứa b
                            dcel.insertDiagonalOnFace(heS, heT, a, b);
                            // heS giờ cần trỏ tới half kết thúc tại (a->b) để tiếp tục "lướt" dọc theo path
                            int h_ab = dcel.dir2he[DCEL::dirKey(a,b)];
                            heS = h_ab; // cập nhật s = b
                            inEmb[b]=true;
                        }else{
                            // b đã trên mặt: chèn dây (a,b) giữa heS và Bpos
                            dcel.insertDiagonalOnFace(heS, Bpos, a, b);
                            int h_ab = dcel.dir2he[DCEL::dirKey(a,b)];
                            heS = h_ab;
                        }
                    }
                    // đánh dấu tất cả đỉnh trên path là đã nhúng
                    for(int v: path) inEmb[v]=true;
                    progress=true;
                }
            }
            if(progress) break;
        }
    }

    // kiểm tra đã nhúng hết đỉnh chưa
    for(int u=0;u<n;u++){
        // đỉnh cô lập cho phép tồn tại (nếu input như vậy)
        // còn nếu có bậc>0 mà chưa inEmb thì thất bại
        if(!dcel.around[u].empty()) continue;
        if(!g.adj[u].empty()) return false;
    }
    return true;
}

/* ===============================================
   2) BICONNECTED: nối láng giềng kề nhau quanh mỗi đỉnh trong embedding
   =============================================== */
static void makeBiconnectedPlanar(DCEL &dcel){
    // để ổn định, lặp nhiều lần vì embedding thay đổi trong khi chèn
    bool changed=true; int guard=0;
    while(changed && guard++<dcel.n*4){
        changed=false;
        dcel.refreshFaces();
        // với mỗi đỉnh v, nối các cặp láng giềng kề nhau (u,w) trong vòng CW quanh v nếu chưa có cạnh
        for(int v=0; v<dcel.n; v++){
            auto &rot = dcel.around[v];
            int d=(int)rot.size();
            if(d<2) continue;
            for(int i=0;i<d;i++){
                int u = dcel.H[ rot[i] ].to;
                int w = dcel.H[ rot[(i+1)%d] ].to;
                if(u==w || dcel.hasEdgeUnd(u,w)) continue;
                int hu, hw;
                if(dcel.findWedge(u,v,w,hu,hw)){
                    dcel.insertDiagonalOnFace(hu,hw,u,w);
                    changed=true;
                }
            }
        }
    }
}

/* ===============================================
   3) TAM GIÁC HÓA: thêm đường chéo trong từng mặt
   =============================================== */
static void triangulateAllFaces(DCEL &dcel){
    bool changed=true; int guard=0;
    while(changed && guard++<dcel.n*10){
        changed=false;
        dcel.refreshFaces();
        for(auto &ring: dcel.facesById){
            int m=(int)ring.size();
            if(m<=3) continue;
            int a = dcel.H[ ring[0] ].to;
            // fan từ a: thêm (a, v_i) cho i=2..m-1 nếu chưa có
            for(int i=2;i<m;i++){
                int b = dcel.H[ ring[i] ].to;
                if(a==b || dcel.hasEdgeUnd(a,b)) continue;
                int ha,hb;
                if(dcel.findPositionsAnyFace(a,b,ha,hb)){
                    dcel.insertDiagonalOnFace(ha,hb,a,b);
                    changed=true;
                }
            }
        }
    }
}

/* ===============================================
   4) CANONICAL ORDERING (trên đồ thị tam giác hóa)
   - Chọn một mặt tam giác làm (v1,v2,v3).
   - Lặp: tìm u trên "biên ngoài" có đúng 2 láng giềng trên biên, u != v1,v2.
   - Ở đây dùng biến thể thực dụng: luôn tồn tại trên đồ thị tối đại phẳng.
   =============================================== */
static bool canonicalOrdering(const DCEL &dcelIn, vector<int> &ord){
    DCEL dcel = dcelIn; // làm việc trên bản sao (sẽ không phá DCEL gốc)
    int n = dcel.n;

    // lấy một mặt tam giác
    dcel.refreshFaces();
    int fTri=-1;
    for(int f=0; f<(int)dcel.facesById.size(); f++){
        if((int)dcel.facesById[f].size()==3){ fTri=f; break; }
    }
    if(fTri==-1) return false;
    int v1 = dcel.H[ dcel.facesById[fTri][0] ].to;
    int v2 = dcel.H[ dcel.facesById[fTri][1] ].to;
    int v3 = dcel.H[ dcel.facesById[fTri][2] ].to;

    // adjacency
    auto G = dcel.buildAdj();
    vector<int> deg(n,0); for(int i=0;i<n;i++) deg[i]=(int)G[i].size();
    vector<char> removed(n,false);
    ord.clear(); ord.reserve(n);
    ord.push_back(v1); ord.push_back(v2); ord.push_back(v3);

    int placed=3;
    // Greedy: mỗi vòng tìm một u chưa đặt, xuất hiện trên một mặt tam giác với 2 láng giềng chưa bị loại
    while(placed<n){
        bool ok=false;
        dcel.refreshFaces();
        for(auto &ring: dcel.facesById){
            if(ring.size()!=3) continue;
            int a = dcel.H[ring[0]].to;
            int b = dcel.H[ring[1]].to;
            int c = dcel.H[ring[2]].to;
            // thử lấy đỉnh thứ ba u khác v1,v2
            for(int u: {a,b,c}){
                if(removed[u]) continue;
                if(u==v1 || u==v2) continue;
                // hai đỉnh còn lại
                int p = (u==a?b:a);
                int q = (u==c?b:c);
                if(!removed[p] && !removed[q]){
                    ord.push_back(u);
                    removed[u]=true;
                    placed++;
                    ok=true;
                    break;
                }
            }
            if(ok) break;
        }
        if(!ok) return false;
    }
    return true;
}

/* ===============================================
   5) CHROBAK–PAYNE: gán (x,y) nguyên từ canonical ordering
   =============================================== */
struct Coord{ long long x=0,y=0; };
static bool chrobakPayne(const vector<vector<int>>& adj, const vector<int>& ord, vector<Coord>& P){
    int N=adj.size();
    P.assign(N,{});
    const int NIL=-1;
    vector<int> pred(N,NIL), succ(N,NIL), parent(N,NIL), mark(N,0);
    vector<char> on(N,false);
    vector<long long> gap(N,0), ax(N,0);

    int v1=ord[0], v2=ord[1], v3=ord[2];
    on[v1]=on[v2]=on[v3]=true;
    pred[v1]=NIL; succ[v1]=v3;
    pred[v3]=v1;  succ[v3]=v2;
    pred[v2]=v3;  succ[v2]=NIL;
    gap[v3]=1; gap[v2]=1;

    P[v1].y=0; P[v2].y=0; P[v3].y=1;
    parent[v1]=NIL; ax[v1]=0;
    parent[v3]=v1;  ax[v3]=1;
    parent[v2]=v3;  ax[v2]=1;

    int ts=0;
    for(int k=3;k<N;k++){
        int vk=ord[k];
        ++ts;
        for(int u: adj[vk]) mark[u]=ts;
        int seed=-1;
        for(int u: adj[vk]) if(on[u]){ seed=u; break; }
        if(seed==-1) return false;

        int p=seed,q=seed;
        while(pred[p]!=NIL && on[pred[p]] && mark[pred[p]]==ts) p=pred[p];
        while(succ[q]!=NIL && on[succ[q]] && mark[succ[q]]==ts) q=succ[q];

        if(succ[p]==NIL) return false;
        gap[ succ[p] ] += 1;
        gap[ q ]       += 1;

        long long D=0;
        vector<int> removed; vector<long long> pref;
        for(int u=succ[p]; u!=q; u=succ[u]){
            if(!on[u]) return false;
            D += gap[u];
            removed.push_back(u);
            pref.push_back(D);
        }
        D += gap[q];

        long long t1 = D - P[p].y + P[q].y;
        long long t2 = P[p].y + D + P[q].y;
        if((t1&1LL)||(t2&1LL)) return false;
        long long rel=t1/2, yvk=t2/2;
        P[vk].y=yvk;

        parent[vk]=p; ax[vk]=rel;
        for(size_t i=0;i<removed.size();i++){
            int u=removed[i];
            long long sumToU=pref[i];
            parent[u]=vk; ax[u]=sumToU-rel; on[u]=false;
        }
        succ[p]=vk; pred[vk]=p;
        succ[vk]=q; pred[q]=vk; on[vk]=true;
        gap[vk]=rel; gap[q]=D-rel;
    }
    // tích lũy x
    vector<vector<int>> child(N);
    for(int u=0;u<N;u++) if(parent[u]!=NIL) child[parent[u]].push_back(u);
    vector<long long> X(N,0);
    function<void(int)> dfs=[&](int u){
        for(int v: child[u]){ X[v]=X[u]+ax[v]; dfs(v); }
    };
    X[v1]=0; dfs(v1);
    for(int u=0;u<N;u++) P[u].x=X[u];
    return true;
}

/* ===============================================
   MAIN
   =============================================== */
int main(){
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int n,m;
    if(!(cin>>n>>m)) return 0;
    GraphSimple g(n);
    {
        unordered_set<uint64_t> seen; seen.reserve(m*2+7);
        for(int i=0;i<m;i++){
            int u,v; cin>>u>>v; --u;--v;
            if(u<0||u>=n||v<0||v>=n) { cout<<-1<<"\n"; return 0; }
            uint64_t k=undKey(u,v);
            if(u!=v && seen.insert(k).second) g.addEdge(u,v);
        }
    }
    // 1) DMP embedding
    DCEL dcel;
    if(!dmpEmbedPlanar(g, dcel)){ cout<<-1<<"\n"; return 0; }

    // 2) biconnected augmentation
    makeBiconnectedPlanar(dcel);

    // 3) triangulate
    triangulateAllFaces(dcel);

    // 4) canonical ordering
    vector<int> ord;
    if(!canonicalOrdering(dcel, ord)){ cout<<-1<<"\n"; return 0; }

    // 5) Chrobak–Payne layout
    auto adj = dcel.buildAdj();
    vector<Coord> P;
    if(!chrobakPayne(adj, ord, P)){ cout<<-1<<"\n"; return 0; }

    // Xuất toạ độ cho đỉnh gốc 1..n
    for(int u=0;u<n;u++){
        cout<<(u+1)<<" "<<P[u].x<<" "<<P[u].y<<"\n";
    }
    return 0;
}

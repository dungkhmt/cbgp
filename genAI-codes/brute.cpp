// test_c1p.cpp: duyệt toàn bộ hoán vị để kiểm tra tập ràng buộc
#include <bits/stdc++.h>
using namespace std;
bool consecutive(const vector<int>& p, const vector<int>& S){
    vector<int> pos(p.size());
    for(int i=0;i<(int)p.size();++i) pos[p[i]]=i;
    int mn=1e9,mx=-1;
    for(int x: S){ mn=min(mn,pos[x]); mx=max(mx,pos[x]); }
    // đoạn [mn..mx] phải chứa đúng |S| phần tử của S
    return mx-mn+1==(int)S.size();
}
int main(){
    vector<int> U={0,1,2,3,4,5,6,7};
    vector<vector<int>> Rs={{1,2,3,4},{1,4},{1,2,5}};
    do{
        bool ok=true;
        for(auto &S: Rs) if(!consecutive(U,S)){ ok=false; break; }
        if(ok){ cout<<"FOUND\n"; for(int x:U) cout<<x<<' '; cout<<"\n"; return 0; }
    }while(next_permutation(U.begin(),U.end()));
    cout<<"NO SOLUTION\n";
}

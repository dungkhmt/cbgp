// cp_layout.cpp
// g++ -std=c++17 -O2 -I include -I include/ogdf-release cp_layout.cpp -L . -lOGDF -lCOIN -o cp_layout.exe ; .\cp_layout 
#include <ogdf/basic/Graph.h>
#include <ogdf/basic/GridLayout.h>
#include <ogdf/basic/extended_graph_alg.h>
#include <ogdf/planarlayout/PlanarStraightLayout.h>

#include <algorithm>
#include <cassert>
#include <chrono>
#include <climits>
#include <cstdint>
#include <iostream>
#include <unordered_set>
#include <vector>

using namespace ogdf;
using namespace std;

using ll = long long;
using db = double;
using ld = long double;

const db eps = 1e-9;
const db inf = 1e18;
const db pi = acos(-1.0);

class Point {
public:
  db x, y;

  Point() : x(inf), y(inf) {}
  Point(db x, db y) : x(x), y(y) {}

  bool operator == (const Point &p) const {
    return abs(x - p.x) < eps && abs(y - p.y) < eps;
  }

  bool operator < (const Point &p) const {
    if (abs(x - p.x) > eps) return x < p.x;
    return y + eps < p.y;
  }

  Point operator + (const Point &p) const {
    return Point(x + p.x, y + p.y);
  }
  Point& operator += (Point &p) {
    x += p.x;
    y += p.y;
    return *this;
  }

  Point operator + (db k) const {
    return Point(x + k, y + k);
  }

  Point operator - () const {
    return Point(-x, -y);
  }

  Point operator - (const Point &p) const {
    return Point(x - p.x, y - p.y);
  }
  Point& operator -= (const Point &p) {
    x -= p.x;
    y -= p.y;
    return *this;
  }

  Point operator * (db k) const {
    return Point(x * k, y * k);
  }
  Point& operator *= (db k) {
    x *= k;
    y *= k;
    return *this;
  }

  Point operator / (db k) const {
    return Point(x / k, y / k);
  }
  Point& operator /= (db k) {
    x /= k;
    y /= k;
    return *this;
  }

  db norm() const {
    return sqrt(x * x + y * y);
  }
  db distance(const Point &p) const {
    return hypot(x - p.x, y - p.y);
  }

  db operator & (const Point &p) const {
    return x * p.x + y * p.y;
  }

  db operator ^ (const Point &p) const {
    return x * p.y - y * p.x;
  }

  db angle(const Point &p) const {
    return acos((operator^(p)) / (norm() * p.norm()));
  }
  db angle() const {
    return atan2(y, x);
  }
  
  Point rotate(db angle) const {
    return Point(x * cos(angle) - y * sin(angle), x * sin(angle) + y * cos(angle));
  }
  Point rotate90() const {
    return Point(-y, x);
  }
  Point rotate90Counter() const {
    return Point(y, -x);
  }
};

struct LineEquation {
  db a, b, c; // ax + by + c = 0
  LineEquation() : a(0), b(0), c(0) {}
  LineEquation(const Point &p1, const Point &p2) {
    a = p1.y - p2.y;
    b = p2.x - p1.x;
    c = -(a * p1.x + b * p1.y);
  }

  LineEquation(db a, db b, db c) : a(a), b(b), c(c) {}
  db distance(const Point &p) const {
    return abs(a * p.x + b * p.y + c) / sqrt(a * a + b * b);
  }

  Point intersection(const LineEquation &other) const {
    db det = a * other.b - b * other.a;
    if (abs(det) < eps) {
      if (abs(b * other.c - c * other.b) < eps) {
        return Point();
      }
      return Point(-inf, -inf);
    }
    db x = (b * other.c - c * other.b) / det;
    db y = (c * other.a - a * other.c) / det;
    return Point(x, y);
  }

  bool isContain(const Point &p) const {
    return abs(a * p.x + b * p.y + c) < eps;
  }

  bool isParallel(const LineEquation &other) const {
    return abs(a * other.b - b * other.a) < eps;
  }

  bool isCoincident(const LineEquation &other) const {
    return isParallel(other) && abs(a * other.c - c * other.a) < eps && abs(b * other.c - c * other.b) < eps;
  }

  db angle(const LineEquation &other) const {
    db cosTheta = (a * other.a + b * other.b) / (sqrt(a * a + b * b) * sqrt(other.a * other.a + other.b * other.b));
    return acos(cosTheta);
  }


  Point project(const Point &p) const {
    db d = distance(p);
    if (d < eps) return p;
    db x = p.x - a * d / (a * a + b * b);
    db y = p.y - b * d / (a * a + b * b);
    return Point(x, y);
  }
};

struct Segment {
  Point a, b;
  LineEquation line;

  Segment(const Point &a, const Point &b) : a(a), b(b) {
    line = LineEquation(a, b);
  }

  bool operator == (const Segment &s) const {
    return (a == s.a && b == s.b) || (a == s.b && b == s.a);
  }

  db length() const {
    return a.distance(b);
  }

  Point midpoint() const {
    return (a + b) / 2;
  }

  db angle() const {
    return atan2(b.y - a.y, b.x - a.x);
  }

  db distance(const Point &p) const {
    if (a == b) {
      return a.distance(p);
    }
    if ((a - b & b - p) > -eps) {
      return p.distance(b);
    }
    if ((a - b & a - p) < eps) {
      return p.distance(a);
    }
    return line.distance(p);
    // return inf;
  }

  bool isContain(const Point &p) const {
    db d1 = a.distance(p);
    db d2 = b.distance(p);
    return abs(d1 + d2 - length()) < eps;
  }

  Point project(const Point &p) const {
    db t = ((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / length() / length();
    if (t < 0) return a;
    if (t > 1) return b;
    return Point(a.x + t * (b.x - a.x), a.y + t * (b.y - a.y));
  }

  Point intersection(Segment &s) {
    // LineEquation line1(a, b);
    // LineEquation line2(s.a, s.b);
    LineEquation &line1 = line;
    LineEquation &line2 = s.line;
    Point intersectionPoint = line1.intersection(line2);
    if (intersectionPoint.x == -inf && intersectionPoint.y == -inf) {
      return Point(); 
    }
    double d1 = length();
    double d2 = s.length();
    if (intersectionPoint.x == inf && intersectionPoint.y == inf) {
      double xx = a.distance(s.a), xy = a.distance(s.b), yx = b.distance(s.a), yy = b.distance(s.b);
      if (abs(xx + xy - d2) < eps) return a;
      if (abs(yx + yy - d2) < eps) return b;
      if (abs(xx + yx - d1) < eps) return s.a;
      if (abs(xy + yy - d1) < eps) return s.b;
      return Point();
    }

    if (isContain(intersectionPoint) && s.isContain(intersectionPoint)) {
      return intersectionPoint;
    }
    return Point(); // No intersection
  }
};

template<class U, class V>
bool minimize(U &a, V b) { return a > b ? a = b, 1 : 0; }
template<class U, class V>
bool maximize(U &a, V b) { return a < b ? a = b, 1 : 0; }

int H, W, n, m;
vector<vector<int>> adj;
vector<pair<int, int>> edges;
vector<Point> points_ans, points_out;

struct Function {
  vector<db> F;
  vector<string> names = {
    "intersectionCount",
    "minAngle",
    "sumAngle",
    "minEdgeLength",
    "sumEdgeLength",
    "minDistance",
    "sumDistance"
  };
  Function(vector<db> F) : F(F) {}
  // Function(vector<db> F, vector<string> names) : F(F), names(names) {}

  bool operator < (const Function &other) const {
    int len = min(F.size(), other.F.size());
    for (int i = 0; i < len; i++) {
      if (abs(F[i] - other.F[i]) > eps) return F[i] < other.F[i];
    }
    return F.size() < other.F.size();
  }

  friend ostream& operator << (ostream &os, const Function &f) {
    // cerr << f.F.size() << " " << f.names.size() << "\n";
    assert(f.F.size() == f.names.size());
    for (int i = 0; i < f.F.size(); i++) {
      os << f.names[i] << "=" << f.F[i] << "\n";
    }
    return os;
  }
};

Function calc(vector<Point> &points) {
  for (int i = 0; i < n; i++) {
    if (points[i].x < 0 || points[i].x > W || points[i].y < 0 || points[i].y > H) {
      printf("0 Invalid point %d: (%.6lf, %.6lf)\n", i, points[i].x, points[i].y);
      exit(0);
    }
    // printf("0 Invalid point %d: (%.6lf, %.6lf)\n", i, points[i].x, points[i].y);
    // exit(0);
  }

  db minEdgeDistance = inf;
  db sumEdgeLength = 0;
  for (auto &[u, v] : edges) {
    db d = points[u].distance(points[v]);
    minimize(minEdgeDistance, d);
    sumEdgeLength += d;
  }

  int intersectionCount = 0;
  for (auto &[u, v] : edges) {
    Segment seg(points[u], points[v]);
    for (auto &[x, y] : edges) {
      if (u == x || u == y || v == x || v == y) continue;
      Segment otherSeg(points[x], points[y]);
      Point intersectionPoint = seg.intersection(otherSeg);
      if (intersectionPoint.x != inf && intersectionPoint.y != inf) {
        intersectionCount++;
      }
    }
  }

  db minAngle = inf;
  db sumAngle = 0;
  for (int u = 0; u < n; u++) {
    vector<db> angles;
    for (int v : adj[u]) {
      angles.push_back((points[v] - points[u]).angle());
    }
    sort(angles.begin(), angles.end());
    db curMinAngle = inf;
    for (int i = 0; i < angles.size(); i++) {
      db angleDiff = angles[(i + 1) % angles.size()] - angles[i];
      if (angleDiff < 0) angleDiff += 2 * pi;
      minimize(curMinAngle, angleDiff);
      // minimize(minAngle, angleDiff);
    }
    // std::cerr << curMinAngle << "\n";
    sumAngle += curMinAngle;
    minimize(minAngle, curMinAngle);
  }

  db minNodeToEdgeDistance = inf;
  db sumNodeToEdgeDistance = 0;
  // int ok = 0;
  for (int u = 0; u < n; u++) {
    for (auto &[v, w] : edges) {
      if (u == v || u == w) continue;
      db d = Segment(points[v], points[w]).distance(points[u]);
      minimize(minNodeToEdgeDistance, d);
      sumNodeToEdgeDistance += d;
      // if (!ok && abs(minNodeToEdgeDistance) < eps) {
      //   printf("0 Invalid point %d: (%.6lf, %.6lf) is on edge (%d: (%.6lf, %.6lf), %d: (%.6lf, %.6lf))\n", 
      //           u, points[u].x, points[u].y, v, points[v].x, points[v].y, w, points[w].x, points[w].y);
      //   ok = 1;
      // }
    }
  }

  // final weight calculation
  // printf("intersections = %d, min angle = %.6lf, min edge distance = %.6lf, min node to edge distance = %.6lf\n",
  //        intersectionCount, minAngle, minEdgeDistance, minNodeToEdgeDistance);
  // return  intersectionCount * -100 +
  //         minAngle * 200 +
  //         minEdgeDistance + 
  //         minNodeToEdgeDistance * 4;
  return Function({
    (db)intersectionCount,
    minAngle,
    sumAngle,
    minEdgeDistance,
    sumEdgeLength,
    minNodeToEdgeDistance,
    sumNodeToEdgeDistance
  });
}

int main() {
	std::ios::sync_with_stdio(false);
	std::cin.tie(nullptr);
	// start time
	auto start = std::chrono::high_resolution_clock::now();
	for (int _ = 10; _ < 15; _++) {
		std::stringstream ss; ss << _;
		// freopen(("tests/" + ss.str() + ".in").c_str(), "r", stdin);
		std::ifstream in(("tests/" + ss.str() + ".in").c_str());
		// freopen(("tests/" + ss.str() + ".out").c_str(), "w", stdout);
		std::ofstream out(("tests/" + ss.str() + ".out").c_str());
		// freopen("output.txt", "w", stdout);

		int _H, _W;
		in >> _H >> _W;
		int n, m;
		in >> n >> m;
		::n = n;
		::m = m;
		H = W = 1e9;

		Graph G;
		std::vector<node> id(n + 1);
		for (int i = 1; i <= n; ++i) {
			id[i] = G.newNode();
		}

		adj.assign(n, vector<int>());
    map<pair<int, int>, int> vis;
    edges.clear();
		for (int i = 0; i < m; i++) {
			int u, v;
			in >> u >> v;
			// std::cerr << u << " " << v << "\n";
			if (u < 1 || u > n || v < 1 || v > n || u == v) {
				std::cerr << _ <<  ": Invalid edge (" << u << ", " << v << ")!\n";
				continue;
			}
      if (vis.count({u, v}) || vis.count({v, u})) {
        std::cerr << _ <<  ": Duplicate edge (" << u << ", " << v << ")!\n";
        continue;
      }
      vis[{u, v}] = 1;
			out << "(" << (u - 1) << ", " << (v - 1) << "), ";
			adj[u - 1].push_back(v - 1);
			adj[v - 1].push_back(u - 1);
      edges.push_back({u - 1, v - 1});
			G.newEdge(id[u], id[v]);
		}
		out << "\n";

		if (!isPlanar(G)) {
			std::cerr << _ <<  ": Not a planar graph!\n";
			// return 0;
		}

		GridLayout grid(G);
		PlanarStraightLayout layout;
		layout.sizeOptimization(true);  
		layout.baseRatio(0.33);
		layout.callGrid(G, grid);

		int minX = INT_MAX, maxX = INT_MIN, minY = INT_MAX, maxY = INT_MIN;
		std::vector<std::pair<int, int>> coords(n + 1);
		vector<Point> points(n);
		for (int i = 1; i <= n; i++) {
			int x = grid.x(id[i]);
			int y = grid.y(id[i]);
			coords[i] = {x, y};
			minX = std::min(minX, x);
			maxX = std::max(maxX, x);
			minY = std::min(minY, y);
			maxY = std::max(maxY, y);
		}

		int W = maxX - minX, H = maxY - minY;
		// std::cerr << "Grid size: " << W << " x " << H << "  (theory: " << (2 * n - 4) << " x "
		// 		  << (n - 2) << ")\n";
		for (int i = 1; i <= n; i++) {
			out << i - 1 << ": (" << coords[i].first - minX << ", " << coords[i].second - minY << "), ";
		}
		out << "\n";

		for (int i = 1; i <= n; i++) {
			out << coords[i].first - minX << " " << coords[i].second - minY << " ";
			points[i - 1] = Point(coords[i].first - minX, coords[i].second - minY);
		}
		out << "\n";

		Function f = calc(points);
		out << f << "\n";

		for (int i = 1; i <= n; i++) {
			out << i - 1 << ": (" << coords[i].first - minX << ", " << coords[i].second - minY << ")\n";
		}
		out << "\n";

		// end time
		auto end = std::chrono::high_resolution_clock::now();
		std::chrono::duration<double> duration = end - start;
		out << "Time taken: " << duration.count() << " s" << std::endl;

		in.close();
		out.close();
	}

	return 0;
}


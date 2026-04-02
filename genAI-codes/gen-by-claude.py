import sys
import math
import random
import itertools
import os
import time


# ── Geometry helpers ──────────────────────────────────────────────────────────

def dist(x, y, u, v):
    return math.sqrt((x[u] - x[v]) ** 2 + (y[u] - y[v]) ** 2)


def cross2d(ax, ay, bx, by):
    """2-D cross product of vectors a and b."""
    return ax * by - ay * bx


def segments_intersect_properly(p1, p2, p3, p4):
    """
    True iff segment p1-p2 and segment p3-p4 have a proper interior crossing
    (i.e. the intersection point is not an endpoint of either segment).
    Uses the cross-product orientation test.
    """
    def orientation(a, b, c):
        val = (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0])
        if abs(val) < 1e-9:
            return 0       # collinear
        return 1 if val > 0 else -1

    def on_segment(a, b, c):
        """Is c on segment a-b (assuming a,b,c are collinear)?"""
        return (min(a[0], b[0]) <= c[0] <= max(a[0], b[0]) and
                min(a[1], b[1]) <= c[1] <= max(a[1], b[1]))

    o1 = orientation(p1, p2, p3)
    o2 = orientation(p1, p2, p4)
    o3 = orientation(p3, p4, p1)
    o4 = orientation(p3, p4, p2)

    if o1 != o2 and o3 != o4:
        return True

    # Collinear overlap cases — count as proper crossing only if they truly overlap
    if o1 == 0 and on_segment(p1, p2, p3): return True
    if o2 == 0 and on_segment(p1, p2, p4): return True
    if o3 == 0 and on_segment(p3, p4, p1): return True
    if o4 == 0 and on_segment(p3, p4, p2): return True

    return False


def count_crossings(x : list[int], y : list[int], edges : list[tuple[int, int]]):
    """F1: count crossing pairs."""
    count = 0
    n_e = len(edges)
    for i in range(n_e):
        u1, v1 = edges[i]
        p1 = (x[u1], y[u1])
        p2 = (x[v1], y[v1])

        for j in range(i + 1, n_e):
            u2, v2 = edges[j]
            # Skip adjacent edges (share an endpoint) — shared endpoint
            # intersections are not crossings by definition.
            if u1 == u2 or u1 == v2 or v1 == u2 or v1 == v2:
                continue
            p3 = (x[u2], y[u2])
            p4 = (x[v2], y[v2])
            if segments_intersect_properly(p1, p2, p3, p4):
                count += 1
    return count


def min_edge_length(x, y, edges):
    """F2: minimum edge length (we maximise, so negate in objective)."""
    if not edges:
        return 0.0
    return min(dist(x, y, u, v) for u, v in edges)


def min_incident_angle(x, y, n, adj):
    """
    F3: minimum angle A(u,w,v) over all pairs of edges (u,w),(v,w) sharing w.
    Returns 180 if no node has degree >= 2.
    """
    min_ang = math.pi   # 180 degrees
    for w in range(1, n + 1):
        nbrs = adj[w]
        if len(nbrs) < 2:
            continue
        wx, wy = x[w], y[w]
        for i in range(len(nbrs)):
            for j in range(i + 1, len(nbrs)):
                u, v = nbrs[i], nbrs[j]
                ax, ay = x[u] - wx, y[u] - wy
                bx, by = x[v] - wx, y[v] - wy
                la = math.hypot(ax, ay)
                lb = math.hypot(bx, by)
                if la < 1e-9 or lb < 1e-9:
                    continue
                cos_a = max(-1.0, min(1.0, (ax * bx + ay * by) / (la * lb)))
                ang = math.acos(cos_a)
                if ang < min_ang:
                    min_ang = ang
    return min_ang


def point_to_segment_dist(px, py, ax, ay, bx, by):
    """Distance from point (px,py) to segment (ax,ay)-(bx,by)."""
    dx, dy = bx - ax, by - ay
    if dx == 0 and dy == 0:
        return math.hypot(px - ax, py - ay)
    t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)
    t = max(0.0, min(1.0, t))
    return math.hypot(px - (ax + t * dx), py - (ay + t * dy))


def min_node_edge_dist(x, y, n, edges, adj):
    """
    F4: minimum D(s, u, v) for all edges (u,v) and nodes s where
    (s,u) and (s,v) are NOT edges.
    Returns a large value if no valid (s,u,v) triple exists.
    """
    edge_set = set(edges) | {(v, u) for u, v in edges}
    adj_set = {}
    for node in range(1, n + 1):
        adj_set[node] = set(adj[node])

    min_d = math.inf
    for u, v in edges:
        ax, ay = x[u], y[u]
        bx, by = x[v], y[v]
        for s in range(1, n + 1):
            if s == u or s == v:
                continue
            if v in adj_set[s] or u in adj_set[s]:
                continue
            d = point_to_segment_dist(x[s], y[s], ax, ay, bx, by)
            if d < min_d:
                min_d = d
    return min_d if min_d != math.inf else 0.0


# ── Objective vector (lexicographic: minimise F1, then -F2, -F3, -F4) ────────

def objective(x, y, n, edges, adj):
    f1 = count_crossings(x, y, edges)
    f2 = min_edge_length(x, y, edges)
    f3 = min_incident_angle(x, y, n, adj)
    f4 = min_node_edge_dist(x, y, n, edges, adj)
    return (f1, -f2, -f3, -f4)


# ── Simulated annealing ───────────────────────────────────────────────────────

def lexicographic_better(a, b):
    """Return True if objective vector a is strictly better than b."""
    return a < b


def simulated_annealing(W, H, n, edges, adj,
                         max_iter=80_000,
                         T_start=5.0,
                         T_end=0.01,
                         seed=42):
    random.seed(seed)

    # Integer coordinates in [1, W] x [1, H]
    x = {v: random.randint(1, W) for v in range(1, n + 1)}
    y = {v: random.randint(1, H) for v in range(1, n + 1)}

    best_x, best_y = dict(x), dict(y)
    cur_obj = objective(x, y, n, edges, adj)
    best_obj = cur_obj

    cool = (T_end / T_start) ** (1.0 / max_iter)
    T = T_start

    for it in range(max_iter):
        # Pick a random node and move it by a random step
        v = random.randint(1, n)
        step = max(1, int(T * 3))
        dx = random.randint(-step, step)
        dy = random.randint(-step, step)

        nx_ = max(1, min(W, x[v] + dx))
        ny_ = max(1, min(H, y[v] + dy))

        old_x, old_y = x[v], y[v]
        x[v], y[v] = nx_, ny_

        new_obj = objective(x, y, n, edges, adj)

        # Accept if better; with small probability accept slightly worse
        # (only on the primary objective F1 to avoid ruining good solutions)
        if lexicographic_better(new_obj, cur_obj):
            cur_obj = new_obj
            if lexicographic_better(new_obj, best_obj):
                best_obj = new_obj
                best_x, best_y = dict(x), dict(y)
                # print(best_obj)
        else:
            # Allow uphill moves only on F1 with Metropolis criterion
            delta_f1 = new_obj[0] - cur_obj[0]
            if delta_f1 <= 1 and random.random() < math.exp(-delta_f1 / (T + 1e-9)):
                cur_obj = new_obj
            else:
                x[v], y[v] = old_x, old_y   # revert

        T *= cool

    return best_x, best_y, best_obj


# ── Local search refinement (hill-climbing on full lexicographic objective) ───

def local_search(x, y, W, H, n, edges, adj, max_rounds=3):
    cur_obj = objective(x, y, n, edges, adj)
    improved = True
    rounds = 0
    while improved and rounds < max_rounds:
        improved = False
        rounds += 1
        nodes = list(range(1, n + 1))
        random.shuffle(nodes)
        for v in nodes:
            ox, oy = x[v], y[v]
            for dx, dy in [(-1,0),(1,0),(0,-1),(0,1),(-1,-1),(1,1),(-1,1),(1,-1)]:
                nx_ = max(1, min(W, x[v] + dx))
                ny_ = max(1, min(H, y[v] + dy))
                x[v], y[v] = nx_, ny_
                new_obj = objective(x, y, n, edges, adj)
                if lexicographic_better(new_obj, cur_obj):
                    cur_obj = new_obj
                    ox, oy = nx_, ny_
                    improved = True
                else:
                    x[v], y[v] = ox, oy
    return x, y, cur_obj


# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    timestamp = int(time.time() * 1000)
    out_dir = f"tests/{timestamp}"
    os.makedirs(out_dir, exist_ok=True)
    print(f"Directory: {out_dir} created")

    for test_idx in range(4, 22):
        in_file = f"tests/{test_idx}.in"
        out_file = f"{out_dir}/{test_idx}.out"
        log_file = f"{out_dir}/{test_idx}.log"
        print(f"test {test_idx} running...")
        
        start_time = time.time()
        
        try:
            with open(in_file, 'r') as f:
                data = f.read().split()
            
            if not data:
                continue

            idx = 0
            W, H = int(data[idx]), int(data[idx+1])
            idx += 2
            n, m = int(data[idx]), int(data[idx+1])
            idx += 2

            edges = []
            adj = {v: [] for v in range(1, n + 1)}
            offset = 0
            for _ in range(m):
                u, v = int(data[idx]), int(data[idx+1])
                if u == 0 or v == 0:
                    offset = 1
                idx += 2
                edges.append((u, v))
            new_edges = []
            for u, v in edges:
                u += offset
                v += offset
                new_edges.append((u, v))
                adj[u].append(v)
                adj[v].append(u)
            edges = new_edges

            # Phase 1: simulated annealing
            x, y, obj = simulated_annealing(W, H, n, edges, adj,
                                            max_iter=100_000,
                                            T_start=8.0,
                                            T_end=0.005)

            # Phase 2: local hill-climb to polish
            x, y, obj = local_search(x, y, W, H, n, edges, adj, max_rounds=5)

            # Output
            with open(out_file, 'w') as fout:
                for i in range(1, n + 1):
                    fout.write(f"{i - 1}: ({x[i]}, {y[i]}), ")
            
            print(f"Saved output to {out_file}")

            end_time = time.time()
            print(f"Time taken: {end_time - start_time:.3f} s")

            f1, neg_f2, neg_f3, neg_f4 = obj
            print(f"# F1 (crossings)     = {f1}", file=sys.stderr)
            print(f"# F2 (min edge len)  = {-neg_f2:.4f}", file=sys.stderr)
            print(f"# F3 (min angle deg) = {math.degrees(-neg_f3):.2f}", file=sys.stderr)
            print(f"# F4 (min node-edge) = {-neg_f4:.4f}\n", file=sys.stderr)

            with open(log_file, 'w') as flog:
                flog.write(f"F1 (crossings)     = {f1}\n")
                flog.write(f"F2 (min edge len)  = {-neg_f2:.4f}\n")
                flog.write(f"F3 (min angle deg) = {math.degrees(-neg_f3):.2f}\n")
                flog.write(f"F4 (min node-edge) = {-neg_f4:.4f}\n")
                flog.write(f"Time taken: {end_time - start_time:.3f} s\n")
            
        except FileNotFoundError:
            print(f"File {in_file} not found.")
        except Exception as e:
            print(f"Error on test {test_idx}: {e}")


if __name__ == "__main__":
    main()
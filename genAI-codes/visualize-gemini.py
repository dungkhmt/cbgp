import networkx as nx
import matplotlib.pyplot as plt

# Dữ liệu các cạnh của đồ thị từ bạn
data = """20 11
11 2
2 3
3 9
9 12
12 8
8 20
5 13
13 4
4 16
16 15
15 7
7 5
6 19
19 1
1 14
14 18
18 6
18 7
6 1
17 10
15 9
10 14
12 15
16 9
13 19
5 8
20 2
5 6
17 14
16 3
12 7
10 18
4 2
20 13
4 1"""

# Tạo một đồ thị vô hướng
G = nx.Graph()
# Đọc dữ liệu và thêm các cạnh vào đồ thị
edges = [tuple(map(int, line.split())) for line in data.strip().split('\n')]
G.add_edges_from(edges)

# Hàm để vẽ và lưu đồ thị với một thuật toán sắp xếp nhất định
def plot_graph(layout_func, filename, is_planar_layout=False):
    plt.figure(figsize=(10, 8)) # Tạo kích thước hình lớn hơn cho dễ nhìn
    try:
        if is_planar_layout:
             pos = layout_func(G)
        else:
            # Sử dụng seed để kết quả sắp xếp không đổi mỗi lần chạy
            pos = layout_func(G, seed=42)
            
        nx.draw(
            G, 
            pos, 
            with_labels=True, 
            node_color='skyblue', 
            node_size=700, 
            edge_color='gray',
            font_size=12,
            font_weight='bold'
        )
        title = filename.replace('_', ' ').replace('.png', '').title()
        plt.title(title, size=15)
        plt.savefig(filename)
        print(f"Đã tạo thành công file: {filename}")
        plt.close() # Đóng hình sau khi lưu

    except Exception as e:
        print(f"Không thể tạo file {filename}. Lỗi: {e}")


# Kiểm tra xem đồ thị có phải là đồ thị phẳng hay không
is_planar, _ = nx.check_planarity(G)

# Tạo và lưu hình ảnh với các thuật toán sắp xếp khác nhau
print("Đang tạo các hình ảnh đồ thị...")
plot_graph(nx.spring_layout, "do_thi_spring.png")
plot_graph(nx.kamada_kawai_layout, "do_thi_kamada_kawai.png")
plot_graph(nx.circular_layout, "do_thi_circular.png")

# Nếu đồ thị là phẳng, sử dụng thuật toán planar_layout
if is_planar:
    print("Đồ thị này là đồ thị phẳng. Đang tạo hình ảnh với planar layout...")
    plot_graph(nx.planar_layout, "do_thi_phang.png", is_planar_layout=True)
else:
    print("Đồ thị này không phải là đồ thị phẳng, không thể dùng planar layout.")

print("\nHoàn tất! Vui lòng kiểm tra các file PNG được tạo ra.")
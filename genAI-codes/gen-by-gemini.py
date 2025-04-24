import networkx as nx
import matplotlib.pyplot as plt

def visualize_graph(nodes, edges):
    """
    Visualizes a graph given its nodes and edges using NetworkX and Matplotlib.

    Args:
        nodes: A list of node labels (e.g., [1, 2, 3, 4, 5]).
        edges: A list of tuples representing the edges (e.g., [(1, 2), (2, 3), (1, 3), (1, 4), (2, 4)]).
    """
    # Create a directed graph (DiGraph)
    G = nx.DiGraph()  # Changed to DiGraph

    # Add nodes
    G.add_nodes_from(nodes)

    # Add edges
    G.add_edges_from(edges)

    # Define a layout for the nodes.  There are many options, and the best one
    # depends on the specific graph.  Here, I'm using a spring layout, which
    # often produces nice-looking visualizations for small to medium-sized graphs.
    # You can experiment with other layouts like nx.circular_layout, nx.planar_layout,
    # nx.shell_layout, or nx.spectral_layout.
    pos = nx.spring_layout(G)

    # Draw the nodes
    nx.draw_networkx_nodes(G, pos, node_color='skyblue', node_size=700)

    # Draw the edges with arrows
    nx.draw_networkx_edges(G, pos, edge_color='gray', width=1.0, arrows=True) # Added arrows

    # Draw the node labels
    nx.draw_networkx_labels(G, pos, font_size=12, font_family='sans-serif')

    # Add a title to the plot
    plt.title("Graph G Visualization")

    # Show the plot.  This will display the graph in a window.
    plt.show()

if __name__ == "__main__":
    # Define the nodes and edges for your graph G
    nodes_g = [1, 2, 3, 4, 5]
    edges_g = [(1, 2), (2, 3), (1, 3), (1, 4), (2, 4),(3,5)]

    # Visualize the graph
    visualize_graph(nodes_g, edges_g)

package program3;

import com.sun.istack.internal.NotNull;
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.alg.KosarajuStrongConnectivityInspector;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.ListenableUndirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import javax.print.DocFlavor;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

class Main {

    // Main
    public static void main(String[] args) {
        displayGraphGUI("PA-3 Graphs");
    }

    // Graph GUI
    public static class GraphGUI extends JFrame {
        private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
        private static final Dimension DEFAULT_SIZE = new Dimension(1000, 1000);

        // JGraph object
        private JGraph jgraph;

        // Graph (Undirected or Directed) with vertexes and edges
        private ListenableGraph<String, DefaultEdge> listenableGraph;

        // JGraph object model adapter with vertexes and edges
        private JGraphModelAdapter<String, DefaultEdge> jGraphModelAdapter;

        // Tabs
        private JTabbedPane tabs;

        // Graph scroll pane
        JScrollPane graphScrollPane;

        // Edge table
        private JTable edgeTable;

        // Vertex table
        private JTable vertexTable;

        // Adjacency matrix
        private JLabel[][] adjacencyMatrix = new JLabel[7][7];

        // Adjacency Linked List
        ArrayList<LinkedList<String>> adjacencyList = new ArrayList<>();

        // Matrix font and gap
        private static final int GAP = 1;
        private static final Font LABEL_FONT = new Font(Font.DIALOG, Font.PLAIN, 24);

        // SCC list
        private java.util.List sccList;

        // Boolean to determine of the graph is directed
        private boolean isDirected = true;

        // Constructor for GraphGUI
        private GraphGUI(String name) {
            // Inherits name from JFrame
            super(name);

            // Initialize GUI
            init();
        }

        // Initialize graph display
        public void init() {

            // Tabs
            tabs = new JTabbedPane();

            // Home panel
            JPanel homePanel = new JPanel();
            buildHomePage(homePanel);

            // Adjacency panel
            JPanel adjacencyPanel = new JPanel();
            buildAdjacencyPage(adjacencyPanel);

            // SCC panel
            JPanel sccPanel = new JPanel();
            buildSccPage(sccPanel);

            JPanel buttons = new JPanel();
            JPanel mainPanel = new JPanel();

            // Directed Radio button
            JRadioButton directedButton = new JRadioButton("Directed");
            directedButton.setSelected(true);
            directedButton.setFocusPainted(false);
            directedButton.setToolTipText("Use a directed graph. Reload graph after changing.");
            directedButton.addActionListener(e -> isDirected = true);

            // Undirected Radio button
            JRadioButton unDirectedButton = new JRadioButton("Undirected");
            unDirectedButton.setFocusPainted(false);
            unDirectedButton.setToolTipText("Use an undirected graph. Reload graph after changing.");
            unDirectedButton.addActionListener(e -> isDirected = false);

            // Group the radio buttons.
            ButtonGroup radioGroup = new ButtonGroup();
            radioGroup.add(directedButton);
            radioGroup.add(unDirectedButton);

            // Bottom buttons
            final JButton reloadButton = new JButton("Reload graph");
            reloadButton.setToolTipText("Reloads the adjacency list and matrix, SCC, and interactive graph");
            final JButton quitButton = new JButton("Quit");

            // Reload graph
            reloadButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {

                    // Get vertexes and edges from Home tables
                    java.util.List<String[]> vertexes = ((TableListModel) vertexTable.getModel()).getData();
                    java.util.List<String[]> edges = ((TableListModel) edgeTable.getModel()).getData();

                    // Create a new Directed or Undirected graph
                    if (isDirected) {
                        listenableGraph = new ListenableDirectedGraph<>(DefaultEdge.class);
                    } else {
                        listenableGraph = new ListenableUndirectedGraph<>(DefaultEdge.class);
                    }

                    // JGraph object model
                    jGraphModelAdapter = new JGraphModelAdapter<>(listenableGraph);

                    // JGraph object
                    jgraph = new JGraph(jGraphModelAdapter);

                    // New Adjacency list
                    adjacencyList = new ArrayList<>();

                    // Add vertexes to graph
                    vertexes.forEach(item ->
                    {
                        listenableGraph.addVertex(item[0]);
                        adjacencyList.add(new LinkedList<>());
                        adjacencyList.get(adjacencyList.size() - 1).add(item[0]);

                    });

                    // Add edges to graph
                    edges.forEach(item ->
                    {
                        if (listenableGraph.containsVertex(item[0]) && listenableGraph.containsVertex(item[1])) {
                            if(!isDirected && item[0].equals(item[1])) {
                                return;
                            }
                            listenableGraph.addEdge(item[0], item[1]);
                        }
                    });

                    // Position vertices within JGraph component
                    final int[] columns = {0};
                    final int[] x = {20};
                    final int[] y = {20};
                    listenableGraph.vertexSet().forEach(item -> {
                        if (columns[0] == 3) {
                            x[0] = 20;
                            y[0] += 200;
                            columns[0] = 0;
                        }

                        positionVertexAt(item, x[0], y[0]);

                        x[0] += 180;
                        columns[0]++;
                    });

                    // Setup graph display
                    adjustDisplaySettings(jgraph);
                    graphScrollPane.setViewportView(jgraph);

                    // Get Strongly Connected components if the graph is directed
                    if (isDirected) {
                        KosarajuStrongConnectivityInspector kosarajuStrongConnectivityInspector = new KosarajuStrongConnectivityInspector((DirectedGraph) listenableGraph);

                        sccList = kosarajuStrongConnectivityInspector.stronglyConnectedSets();

                        buildSccPage(sccPanel);

                    }

                    // Adjacency Matrix and List
                    adjacencyMatrix = new JLabel[vertexes.size() + 1][vertexes.size() + 1];

                    if(adjacencyMatrix.length > 0) {
                        adjacencyMatrix[0][0] = new JLabel(" ", SwingConstants.CENTER);

                        for (int i = 1; i <= vertexes.size(); i++) {
                            adjacencyMatrix[0][i] = new JLabel(" " + vertexes.get(i - 1)[0] + " ", SwingConstants.CENTER);
                        }

                        for (int i = 1; i <= vertexes.size(); i++)
                        {
                            adjacencyMatrix[i][0] = new JLabel(" " + vertexes.get(i - 1)[0] + " ", SwingConstants.CENTER);

                            for (int j = 1; j <= vertexes.size(); j++) {
                                adjacencyMatrix[i][j] =
                                        new JLabel((listenableGraph.getEdge(adjacencyMatrix[i][0].getText().trim(),
                                                adjacencyMatrix[0][j].getText().trim()) != null) ? " 1 " : " 0 ",
                                        SwingConstants.CENTER);

                                if(listenableGraph.getEdge(adjacencyMatrix[i][0].getText().trim(), adjacencyMatrix[0][j].getText().trim()) != null)
                                {
                                    adjacencyList.get(i-1).add(adjacencyMatrix[0][j].getText().trim());
                                }

                            }
                        }

                        buildAdjacencyPage(adjacencyPanel);
                    }

                    GraphIterator<String, DefaultEdge> iterator =
                            new DepthFirstIterator<>(listenableGraph);
                    while (iterator.hasNext()) {
                        System.out.println( iterator.next() );
                    }

                    dfSearch(vertexes, adjacencyList);
                }
            });

            //Quit button
            quitButton.addActionListener(e -> System.exit(0));

            // Add buttons to buttons panel
            buttons.add(directedButton);
            buttons.add(unDirectedButton);
            buttons.add(reloadButton);
            buttons.add(quitButton);

            // Setup main panel
            mainPanel.add(tabs, BorderLayout.CENTER);
            mainPanel.add(buttons, BorderLayout.PAGE_END);

            // Setup graph scroll pane
            graphScrollPane = new JScrollPane(null,
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            graphScrollPane.setPreferredSize(new Dimension(500, 320));

            // Add tabs
            tabs.addTab("Vertexes/Edges", null, homePanel, "Edit Vertexes and Edges");
            tabs.addTab("Adjacency", null, adjacencyPanel, "Adjacency List and Matrix");
            tabs.addTab("SCC", null, sccPanel, "Strongly Connected Components");
            tabs.addTab("Graph", null, graphScrollPane, "Interactive Graph");

            // Set main view
            getContentPane().add(mainPanel);
        }

        // Adjust graph size and background color
        private void adjustDisplaySettings(JGraph jg) {
            jg.setPreferredSize(DEFAULT_SIZE);
            jg.setBackground(DEFAULT_BG_COLOR);
        }

        // Position a vertex
        private void positionVertexAt(Object vertex, int x, int y) {
            DefaultGraphCell cell = jGraphModelAdapter.getVertexCell(vertex);
            Map attr = cell.getAttributes();
            Rectangle2D b = GraphConstants.getBounds(attr);

            GraphConstants.setBounds(attr, new Rectangle(x, y, (int) b.getWidth(), (int) b.getHeight()));

            Map<DefaultGraphCell, Map> cellAttr = new HashMap<DefaultGraphCell, Map>();
            cellAttr.put(cell, attr);
            jGraphModelAdapter.edit(cellAttr, null, null, null);
        }

        // Table List Model class for the GUI tables
        public class TableListModel extends AbstractTableModel {
            private java.util.List<String> columnNames = new ArrayList<>();
            private java.util.List<String[]> data = new ArrayList<>();

            public TableListModel(String[] columnNames) {
                Collections.addAll(this.columnNames, columnNames);
            }

            public void addRow(String[] row) {
                data.add(row);
                fireTableRowsInserted(data.size(), data.size());
            }

            public void deleteRow(int rowIndex) {
                data.remove(rowIndex);
                fireTableRowsDeleted(data.size(), data.size());
            }

            public Object[] getColumnOneData() {
                return data.stream().map(item -> item[0]).sorted(String::compareTo)
                        .collect(Collectors.<String>toList()).toArray();
            }

            public java.util.List<String[]> getData() {
                return data;
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                data.get(row)[col] = (String) value;
                fireTableCellUpdated(row, col);
            }

            @Override
            public String getColumnName(int index) {
                return columnNames.get(index);
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return true;
            }

            @Override
            public int getRowCount() {
                return data.size();
            }

            @Override
            public int getColumnCount() {
                return columnNames.size();
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return data.get(rowIndex)[columnIndex];
            }
        }

        /**
         * Builds the Adjacency Page - Adjacency List and Matrix
         *
         * @param page The Adjacency tab
         */
        private void buildAdjacencyPage(JPanel page) {

            // Remove previous components
            page.removeAll();

            // Build Matrix grid
            JPanel matrixPanel = new JPanel(new GridLayout(adjacencyMatrix.length, adjacencyMatrix[0].length, GAP, GAP));
            matrixPanel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
            matrixPanel.setBackground(Color.BLACK);
            for (int row = 0; row < adjacencyMatrix.length; row++) {
                for (int col = 0; col < adjacencyMatrix[row].length; col++) {
                    if(adjacencyMatrix[row][col] == null)
                    {
                        adjacencyMatrix[row][col] = new JLabel("  ", SwingConstants.CENTER);
                    }
                    adjacencyMatrix[row][col].setFont(LABEL_FONT); // make it big
                    adjacencyMatrix[row][col].setOpaque(true);
                    adjacencyMatrix[row][col].setBackground(Color.WHITE);
                    matrixPanel.add(adjacencyMatrix[row][col]);
                }
            }

            // Matrix pane
            JPanel matrixPane = new JPanel(new BorderLayout());
            matrixPane.setBorder(new TitledBorder("Matrix"));

            // List pane
            JPanel listPane = new JPanel(new BorderLayout());
            listPane.setBorder(new TitledBorder("List"));

            // Adjacency List Grid
            JPanel adjListPanel = new JPanel(new GridLayout(adjacencyList.size(), 1, GAP, GAP));

            // Add each row of the adjacency Linked Lists to the table
            for (LinkedList<String> list : adjacencyList) {

                String item = "";

                for (int i = 0; i < list.size(); i++) {
                    if (i == 0) {
                        item = list.get(0) + " -> ";
                    } else {
                        item += list.get(i);
                        if ((i + 1) != list.size()) {
                            item += ", ";
                        }
                    }
                }

                JLabel labelItem = new JLabel(" " + item + " ");

                labelItem.setOpaque(true);
                labelItem.setBackground(Color.WHITE);
                adjListPanel.add(labelItem);
            }

            // Scroll panes
            JScrollPane matrixScrollPane = new JScrollPane(matrixPanel);
            matrixScrollPane.setPreferredSize(new Dimension(300, 300));

            JScrollPane listScrollPane = new JScrollPane(adjListPanel);
            listScrollPane.setPreferredSize(new Dimension(100, 300));

            // Add matrix and list to scroll panes
            listPane.add(listScrollPane);
            matrixPane.add(matrixScrollPane);

            // Add matrix and list to adjacency panel
            page.add(matrixPane, BorderLayout.WEST);
            page.add(listPane, BorderLayout.EAST);

            // Refresh page
            page.revalidate();
            page.repaint();
        }

        /**
         * Builds the SCC Page - Strongly Connected Components table
         *
         * @param page The SCC tab
         */
        private void buildSccPage(JPanel page) {

            // Remove previous components
            page.removeAll();

            // List pane
            JPanel listPane = new JPanel(new BorderLayout());
            listPane.setBorder(new TitledBorder("SCC"));

            // SCC grid
            JPanel sccPanel = new JPanel(new GridLayout(sccList != null ? sccList.size() : 1, 1, GAP, GAP));

            // Add all SCC items to grid
            if(sccList != null) {
                // Add scc list to table
                sccList.forEach(item ->
                {
                    JLabel labelItem = new JLabel(" " + item + " ");

                    labelItem.setOpaque(true);
                    labelItem.setBackground(Color.WHITE);
                    sccPanel.add(labelItem);
                });
            }

            // Scroll pane
            JScrollPane listScrollPane = new JScrollPane(sccPanel);
            listScrollPane.setPreferredSize(new Dimension(100, 300));

            // Add scc to scroll pane
            listPane.add(listScrollPane);

            // Add scc list panel to scc page
            page.add(listPane, BorderLayout.CENTER);

            // Refresh page
            page.revalidate();
            page.repaint();
        }

        /**
         * Builds the Home Page
         *
         * @param page The home tab
         */
        private void buildHomePage(JPanel page) {

            JPanel vertexPane = new JPanel(new BorderLayout());
            vertexPane.setBorder(new TitledBorder("Vertexes"));

            JPanel edgePane = new JPanel(new BorderLayout());
            edgePane.setBorder(new TitledBorder("Edges"));

            JScrollPane vertexTablePane;
            JScrollPane edgeTablePane;

            // Vertex Table
            vertexTable = new JTable() {
                private static final long serialVersionUID = 1L;

                public boolean isCellEditable(int row, int column) {
                    return true;
                }
            };

            vertexTable.setModel(new TableListModel(new String[]{"Vertex"}));
            Dimension vertexTableDim = vertexTable.getPreferredScrollableViewportSize();
            vertexTableDim.setSize(vertexTable.getPreferredSize().getWidth(), 240);
            vertexTable.setPreferredScrollableViewportSize(vertexTableDim);
            vertexTable.setFillsViewportHeight(true);
            vertexTable.putClientProperty("terminateEditOnFocusLost", true);

            vertexTable.setRowSelectionAllowed(true);
            vertexTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            vertexTablePane = new JScrollPane(vertexTable);

            // Edge Table
            edgeTable = new JTable() {
                private static final long serialVersionUID = 2L;

                public boolean isCellEditable(int row, int column) {
                    return true;
                }

                ;
            };

            // Set up edge table
            edgeTable.setModel(new TableListModel(new String[]{"Source", "Target"}));
            Dimension edgeTableDim = edgeTable.getPreferredScrollableViewportSize();
            edgeTableDim.setSize(edgeTable.getPreferredSize().getWidth(), 240);
            edgeTable.setPreferredScrollableViewportSize(edgeTableDim);
            edgeTable.setFillsViewportHeight(true);

            //Set up the editor for the cells.
            JComboBox<String> edgeComboBox1 = new JComboBox<>();
            edgeTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(edgeComboBox1));

            //Set up the editor for the cells.
            JComboBox<String> edgeComboBox2 = new JComboBox<>();
            edgeTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(edgeComboBox2));
            edgeTable.putClientProperty("terminateEditOnFocusLost", true);

            vertexTable.setRowSelectionAllowed(true);
            vertexTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            edgeTablePane = new JScrollPane(edgeTable);

            // Update Edge Edit Combo Boxes on edit un-focus
            vertexTable.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    edgeComboBox1.removeAllItems();
                    edgeComboBox2.removeAllItems();

                    for (Object item : ((TableListModel) vertexTable.getModel()).getColumnOneData()) {
                        edgeComboBox1.addItem(item.toString());
                        edgeComboBox2.addItem(item.toString());
                    }
                }
            });

            final JPanel vertexActions = new JPanel();
            vertexActions.setLayout(new FlowLayout());

            final JPanel edgeActions = new JPanel();
            edgeActions.setLayout(new FlowLayout());

            final JButton addVertexButton = new JButton("+ Add")
            {
                @Override
                protected boolean processKeyBinding(KeyStroke ks, KeyEvent ke, int i, boolean bln) {
                    boolean b = super.processKeyBinding(ks, ke, i, bln);

                    if (b && ks.getKeyCode() == KeyEvent.VK_F1) {
                        requestFocusInWindow();
                    }

                    return b;
                }
            };
            final JButton deleteVertexButton = new JButton("- Delete");

            // Add a vertex action
            AbstractAction vertexAddAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    JTextField newVertex = new JTextField(10);

                    JPanel myPanel = new JPanel();
                    myPanel.add(new JLabel("Vertex:"));
                    myPanel.add(newVertex);

                    newVertex.addAncestorListener(new RequestFocusListener());

                    int result = JOptionPane.showConfirmDialog(null, myPanel,
                            "New Vertex", JOptionPane.OK_CANCEL_OPTION);

                    if (result == JOptionPane.OK_OPTION && newVertex.getText().trim().length() > 0) {
                        ((TableListModel) vertexTable.getModel()).addRow(new String[]{newVertex.getText().trim()});

                        edgeComboBox1.removeAllItems();
                        edgeComboBox2.removeAllItems();

                        for (Object item : ((TableListModel) vertexTable.getModel()).getColumnOneData()) {
                            edgeComboBox1.addItem(item.toString());
                            edgeComboBox2.addItem(item.toString());
                        }
                    }
                }
            };

            // Add Vertex button can be pressed using F1 and ENTER
            addVertexButton.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
            addVertexButton.getActionMap().put("Enter", vertexAddAction);
            addVertexButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "F1");
            addVertexButton.getActionMap().put("F1", vertexAddAction);
            addVertexButton.addActionListener(vertexAddAction);

            // Delete a vertex
            deleteVertexButton.addActionListener((event) -> {

                if (vertexTable.getSelectedRow() < 0) {
                    JOptionPane.showMessageDialog(null, "Select a Vertex first", "Vertex Not Selected",
                            JOptionPane.OK_OPTION);
                } else {
                    JDialog.setDefaultLookAndFeelDecorated(true);
                    int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the Vertex: " +
                                    ((TableListModel) vertexTable.getModel()).getValueAt(vertexTable.getSelectedRow(), 0) + "?", "Delete?",
                            JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        ((TableListModel) vertexTable.getModel()).deleteRow(vertexTable.getSelectedRow());

                        edgeComboBox1.removeAllItems();
                        edgeComboBox2.removeAllItems();

                        for (Object item : ((TableListModel) vertexTable.getModel()).getColumnOneData()) {
                            edgeComboBox1.addItem(item.toString());
                            edgeComboBox2.addItem(item.toString());
                        }
                    }
                }
            });

            // Add vertex buttons
            vertexActions.add(addVertexButton);
            vertexActions.add(deleteVertexButton);

            // Add vertex panels
            vertexPane.add(vertexActions, BorderLayout.NORTH);
            vertexPane.add(vertexTablePane, BorderLayout.SOUTH);

            final JButton addEdgeButton = new JButton("+ Add")
            {
                @Override
                protected boolean processKeyBinding(KeyStroke ks, KeyEvent ke, int i, boolean bln) {
                    boolean b = super.processKeyBinding(ks, ke, i, bln);

                    if (b && ks.getKeyCode() == KeyEvent.VK_F2) {
                        requestFocusInWindow();
                    }

                    return b;
                }
            };
            final JButton deleteEdgeButton = new JButton("- Delete");

            // Add an edge action
            AbstractAction edgeAddAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (((TableListModel) vertexTable.getModel()).getColumnOneData().length == 0) {
                        JOptionPane.showMessageDialog(null, "Add a Vertex first", "No Vertexes",
                                JOptionPane.OK_OPTION);
                    } else {

                        JComboBox<Object> sourceVertex = new JComboBox<>(((TableListModel) vertexTable.getModel()).getColumnOneData());
                        JComboBox<Object> targetVertex = new JComboBox<>(((TableListModel) vertexTable.getModel()).getColumnOneData());

                        JPanel myPanel = new JPanel();
                        myPanel.add(new JLabel("Source:"));
                        myPanel.add(sourceVertex);
                        myPanel.add(new JLabel(" ▶ ")); // a spacer
                        myPanel.add(new JLabel("Target:"));
                        myPanel.add(targetVertex);

                        int result = JOptionPane.showConfirmDialog(null, myPanel,
                                "New Edge", JOptionPane.OK_CANCEL_OPTION);
                        if (result == JOptionPane.OK_OPTION) {
                            ((TableListModel) edgeTable.getModel()).addRow(new String[]{(String) sourceVertex.getSelectedItem(),
                                    (String) targetVertex.getSelectedItem()});
                        }
                    }
                }
            };

            // Add Vertex button can be pressed using F2 and ENTER
            addEdgeButton.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
            addEdgeButton.getActionMap().put("Enter", edgeAddAction);
            addEdgeButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "F2");
            addEdgeButton.getActionMap().put("F2", edgeAddAction);
            addEdgeButton.addActionListener(edgeAddAction);

            // Delete an edge
            deleteEdgeButton.addActionListener((event) -> {

                if (edgeTable.getSelectedRow() < 0) {
                    JOptionPane.showMessageDialog(null, "Select an Edge first", "Edge Not Selected",
                            JOptionPane.OK_OPTION);
                } else {
                    JDialog.setDefaultLookAndFeelDecorated(true);
                    int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the Edge: " +
                                    ((TableListModel) edgeTable.getModel()).getValueAt(edgeTable.getSelectedRow(), 0) + " ▶ " +
                                    ((TableListModel) edgeTable.getModel()).getValueAt(edgeTable.getSelectedRow(), 1)
                                    + "?", "Delete?",
                            JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        ((TableListModel) edgeTable.getModel()).deleteRow(edgeTable.getSelectedRow());
                    }
                }
            });

            // Add edge buttons
            edgeActions.add(addEdgeButton);
            edgeActions.add(deleteEdgeButton);

            // Add edge panels
            edgePane.add(edgeActions, BorderLayout.NORTH);
            edgePane.add(edgeTablePane, BorderLayout.SOUTH);

            // Add home panels (Vertex pane and Edge pane)
            page.add(vertexPane, BorderLayout.WEST);
            page.add(edgePane, BorderLayout.EAST);
        }

        // Request focus listener
        // Used for the vertex add dialog
        public class RequestFocusListener implements AncestorListener {
            private boolean removeListener;

            public RequestFocusListener() {
                this(true);
            }

            public RequestFocusListener(boolean removeListener) {
                this.removeListener = removeListener;
            }

            @Override
            public void ancestorAdded(AncestorEvent e) {
                JComponent component = e.getComponent();
                component.requestFocusInWindow();

                if (removeListener)
                    component.removeAncestorListener(this);
            }

            @Override
            public void ancestorMoved(AncestorEvent e) {
            }

            @Override
            public void ancestorRemoved(AncestorEvent e) {
            }
        }

        // Vertex color for search algorithms
        private enum VertexColor {
            WHITE, GRAY, BLACK;
        }

        // Search attributes
        public class SearchAttributes
        {
            private java.util.List<String[]> vertexes;
            private ArrayList<LinkedList<String>> adjacencyList;
            private VertexColor[] color;
            private String[] predecessor;
            private int[] firstTime;
            private int[] lastTime;
            private int[] distance;
            private int time;

            public SearchAttributes(@NotNull java.util.List<String[]> vertexes,
                                    ArrayList<LinkedList<String>> adjacencyList)
            {
                this.vertexes = vertexes;
                this.adjacencyList = adjacencyList;
                this.color = new VertexColor[vertexes.size()];
                this.predecessor = new String[vertexes.size()];
                this.firstTime = new int[vertexes.size()];
                this.lastTime = new int[vertexes.size()];
                this.distance = new int[vertexes.size()];
                this.time = 0;
            }

            public ArrayList<LinkedList<String>> getAdjacencyList() {
                return adjacencyList;
            }

            public List<String[]> getVertexes() {
                return vertexes;
            }

            public VertexColor[] getColor() {
                return color;
            }

            public String[] getPredecessor() {
                return predecessor;
            }

            public int[] getFirstTime() {
                return firstTime;
            }

            public int[] getLastTime() {
                return lastTime;
            }

            public int[] getDistance() {
                return distance;
            }

            public int getTime() {
                return time;
            }

            public void setTime(int time) {
                this.time = time;
            }

            public int getVertexIndex(String vertex)
            {
                int index = 0;

                for(String[] vertexLabel : vertexes)
                {
                    if(vertexLabel[0].equals(vertex))
                    {
                        index = vertexes.indexOf(vertexLabel);
                    }
                }

                return index;
            }
        }

        // Depth-First Search
        public SearchAttributes dfSearch(java.util.List<String[]> vertexes,  ArrayList<LinkedList<String>> adjacencyList)
        {
            // Get search initial attributes
            SearchAttributes searchAttributes = new SearchAttributes(vertexes, adjacencyList);

            // Initialize colors and predecessors
            for(int u = 0; u < vertexes.size(); u++)
            {
                searchAttributes.getColor()[u] = VertexColor.WHITE;
                searchAttributes.getPredecessor()[u] = "^";
            }

            // Run the dfs visit function for all white nodes
            for(int u = 0; u < vertexes.size(); u++)
            {
               if(searchAttributes.getColor()[u] == VertexColor.WHITE)
               {
                   dfsVisit(searchAttributes, u);
               }
            }

            System.out.print(Arrays.toString(searchAttributes.getLastTime()));

            return searchAttributes;
        }

        // Depth-First Search Visit
        public void dfsVisit(SearchAttributes searchAttributes, int u)
        {
            // Set color to gray
            searchAttributes.getColor()[u] = VertexColor.GRAY;
            searchAttributes.getFirstTime()[u] = searchAttributes.getTime();
            searchAttributes.setTime(searchAttributes.getTime() + 1);

            for(int v = 0; v < searchAttributes.getAdjacencyList().get(u).size(); v++)
            {
                int vertexIndex = searchAttributes.getVertexIndex(searchAttributes.getAdjacencyList().get(u).get(v));

                // Check if color is white
                if(searchAttributes.getColor()[vertexIndex] == VertexColor.WHITE)
                {
                    // Add Predecessor
                    searchAttributes.getPredecessor()[vertexIndex] = searchAttributes.getVertexes().get(u)[0];

                    // Recursive call dfsVisit
                    dfsVisit(searchAttributes, vertexIndex);
                }
            }

            // Set color to black
            searchAttributes.getColor()[u] = VertexColor.BLACK;
            searchAttributes.getLastTime()[u] = searchAttributes.getTime();
            searchAttributes.setTime(searchAttributes.getTime() + 1);
        }
    }

    // Display graph
    private static void displayGraphGUI(String name) {

        //Create and set up a new GUI
        GraphGUI frame = new GraphGUI(name);

        //Exit on close
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Display the window.
        frame.pack();
        //Set location
        frame.setLocationRelativeTo(null);

        //Show the GUI
        frame.setSize(500, 470);
        frame.setVisible(true);
        frame.setResizable(true);

    } // createAndShowGUI

}

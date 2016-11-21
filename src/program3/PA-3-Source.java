package program3;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.alg.KosarajuStrongConnectivityInspector;
import org.jgrapht.ext.ExportException;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.ext.MatrixExporter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.ListenableUndirectedGraph;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

        // Ajacency matrix
        private JLabel[][] adjacencyMatrix = new JLabel[9][9];

        private static final int GAP = 1;
        private static final Font LABEL_FONT = new Font(Font.DIALOG, Font.PLAIN, 24);

        // Boolean to determine of the graph is directed
        private boolean isDirected = true;

        // Constructor for GraphGUI
        private GraphGUI(String name) {
            //Inherits name from JFrame
            super(name);

            init();
        }

        // Initialize graph display
        public void init() {

            // Tabs
            tabs = new JTabbedPane();

            JPanel homePanel = new JPanel();
            buildHomePage(homePanel);

            JPanel adjacencyPanel = new JPanel();
            buildAdjacencyPage(adjacencyPanel);

            JPanel buttons = new JPanel();
            JPanel mainPanel = new JPanel();

            JRadioButton directedButton = new JRadioButton("Directed");
            directedButton.setSelected(true);
            directedButton.setFocusPainted(false);
            directedButton.addActionListener(e -> isDirected = true);

            JRadioButton unDirectedButton = new JRadioButton("Undirected");
            unDirectedButton.setFocusPainted(false);
            unDirectedButton.addActionListener(e -> isDirected = false);

            //Group the radio buttons.
            ButtonGroup radioGroup = new ButtonGroup();
            radioGroup.add(directedButton);
            radioGroup.add(unDirectedButton);

            final JButton reloadButton = new JButton("Reload graph");
            final JButton quitButton = new JButton("Quit");

            // Reload graph
            reloadButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {


                    java.util.List<String[]> vertexes = ((TableListModel) vertexTable.getModel()).getData();
                    java.util.List<String[]> edges = ((TableListModel) edgeTable.getModel()).getData();

                    if (isDirected) {
                        listenableGraph = new ListenableDirectedGraph<>(DefaultEdge.class);
                    } else {
                        listenableGraph = new ListenableUndirectedGraph<>(DefaultEdge.class);
                    }

                    jGraphModelAdapter = new JGraphModelAdapter<>(listenableGraph);

                    jgraph = new JGraph(jGraphModelAdapter);

                    vertexes.forEach(item ->
                    {
                        listenableGraph.addVertex(item[0]);
                    });

                    edges.forEach(item ->
                    {
                        if (listenableGraph.containsVertex(item[0]) && listenableGraph.containsVertex(item[1])) {
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

                    // create a JGraphT graph
                    adjustDisplaySettings(jgraph);
                    graphScrollPane.setViewportView(jgraph);

                    MatrixExporter<String, DefaultEdge> matrixExporter = new MatrixExporter<>();

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                    try {
                        matrixExporter.exportGraph(listenableGraph, outputStream);

                        String adjanceyMatrix = "";

                        adjanceyMatrix = outputStream.toString(StandardCharsets.UTF_8.toString());

                        System.out.print(adjanceyMatrix);
                    } catch (ExportException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    if (isDirected) {
                        KosarajuStrongConnectivityInspector kosarajuStrongConnectivityInspector = new KosarajuStrongConnectivityInspector((DirectedGraph) listenableGraph);

                        java.util.List list = kosarajuStrongConnectivityInspector.stronglyConnectedSets();

                        list.forEach(item -> System.out.print(item + "\n"));
                    }

                    // Adjacency Matrix and List
                    adjacencyMatrix = new JLabel[vertexes.size() + 1][vertexes.size() + 1];

                    System.out.print(adjacencyMatrix.length);

                    if(adjacencyMatrix.length > 0) {
                        adjacencyMatrix[0][0] = new JLabel(" ", SwingConstants.CENTER);

                        for (int i = 1; i <= vertexes.size(); i++) {
                            adjacencyMatrix[0][i] = new JLabel(vertexes.get(i - 1)[0], SwingConstants.CENTER);
                        }

                        for (int i = 1; i <= vertexes.size(); i++)
                        {
                            adjacencyMatrix[i][0] = new JLabel(vertexes.get(i - 1)[0], SwingConstants.CENTER);

                            for (int j = 1; j <= vertexes.size(); j++) {
                                adjacencyMatrix[i][j] =
                                        new JLabel((listenableGraph.getEdge(adjacencyMatrix[i][0].getText(),
                                                adjacencyMatrix[0][j].getText()) != null) ? "1" : "0",
                                        SwingConstants.CENTER);

                            }
                        }

                        buildAdjacencyPage(adjacencyPanel);
                    }
                }
            });

            //Quit button
            quitButton.addActionListener(e -> System.exit(0));

            buttons.add(directedButton);
            buttons.add(unDirectedButton);
            buttons.add(reloadButton);
            buttons.add(quitButton);

            mainPanel.add(tabs, BorderLayout.CENTER);
            mainPanel.add(buttons, BorderLayout.PAGE_END);

            graphScrollPane = new JScrollPane(null,
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            graphScrollPane.setPreferredSize(new Dimension(500, 320));

            tabs.addTab("Home", null, homePanel, "Home");
            tabs.addTab("Adjacency", null, adjacencyPanel, "Adjacency");
            tabs.addTab("Graph", null, graphScrollPane, "Graph");

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

        // Table List Model class for GUI table of unsorted/sorted lists
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
            page.removeAll();
            JPanel matrixPannel = new JPanel(new GridLayout(adjacencyMatrix.length, adjacencyMatrix[0].length, GAP, GAP));
            matrixPannel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
            matrixPannel.setBackground(Color.BLACK);
            for (int row = 0; row < adjacencyMatrix.length; row++) {
                for (int col = 0; col < adjacencyMatrix[row].length; col++) {
                    if(adjacencyMatrix[row][col] == null)
                    {
                        adjacencyMatrix[row][col] = new JLabel("0", SwingConstants.CENTER);
                    }
                    adjacencyMatrix[row][col].setFont(LABEL_FONT); // make it big
                    adjacencyMatrix[row][col].setOpaque(true);
                    adjacencyMatrix[row][col].setBackground(Color.WHITE);
                    matrixPannel.add(adjacencyMatrix[row][col]);
                }
            }

            page.setLayout(new BorderLayout());
            page.add(matrixPannel, BorderLayout.WEST);
            page.add(new JTable(), BorderLayout.EAST);
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
            vertexTableDim.setSize(vertexTable.getPreferredSize().getWidth(), 200);
            vertexTable.setPreferredScrollableViewportSize(vertexTableDim);
            vertexTable.setFillsViewportHeight(true);

            vertexTable.putClientProperty("terminateEditOnFocusLost", true);

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
            edgeTableDim.setSize(edgeTable.getPreferredSize().getWidth(), 200);
            edgeTable.setPreferredScrollableViewportSize(edgeTableDim);
            edgeTable.setFillsViewportHeight(true);

            //Set up the editor for the cells.
            JComboBox<String> edgeComboBox1 = new JComboBox<>();
            edgeTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(edgeComboBox1));

            //Set up the editor for the cells.
            JComboBox<String> edgeComboBox2 = new JComboBox<>();
            edgeTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(edgeComboBox2));
            edgeTable.putClientProperty("terminateEditOnFocusLost", true);

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

            final JButton addVertexButton = new JButton("+ Add");
            final JButton deleteVertexButton = new JButton("- Delete");

            // Add a vertex
            addVertexButton.addActionListener((event) -> {

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
            });

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

            vertexActions.add(addVertexButton);
            vertexActions.add(deleteVertexButton);

            vertexPane.add(vertexActions, BorderLayout.NORTH);
            vertexPane.add(vertexTablePane, BorderLayout.SOUTH);

            final JButton addEdgeButton = new JButton("+ Add");
            final JButton deleteEdgeButton = new JButton("- Delete");

            // Add an edge
            addEdgeButton.addActionListener((event) -> {

                if (((TableListModel) vertexTable.getModel()).getColumnOneData().length == 0) {
                    JOptionPane.showMessageDialog(null, "Add a Vertex first", "No Vertexes",
                            JOptionPane.OK_OPTION);
                } else {

                    JComboBox<Object> sourceVertex = new JComboBox<>(((TableListModel) vertexTable.getModel()).getColumnOneData());
                    JComboBox targetVertex = new JComboBox(((TableListModel) vertexTable.getModel()).getColumnOneData());

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
            });

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

            edgeActions.add(addEdgeButton);
            edgeActions.add(deleteEdgeButton);

            edgePane.add(edgeActions, BorderLayout.NORTH);
            edgePane.add(edgeTablePane, BorderLayout.SOUTH);

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
        frame.setSize(500, 550);
        frame.setVisible(true);
        frame.setResizable(true);

    } // createAndShowGUI

}

package program3;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class Main {

    public static void main(String[] args)
    {
        homeGUI();
    }

    // Graph GUI
    public static class GraphGUI extends JFrame {
        private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
        private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);

        private JGraph jgraph;
        private ListenableGraph<String, DefaultEdge> listenableGraph;
        private JGraphModelAdapter<String, DefaultEdge> jGraphModelAdapter;

        private JTabbedPane tabs;

        // Edge table
        private JTable edgeTable;

        // Vertex table
        private JTable vertexTable;

        // Constructor for GraphGUI
        private GraphGUI(String name, JGraph jgraph,
                         ListenableGraph<String, DefaultEdge> listenableGraph,
                         JGraphModelAdapter<String, DefaultEdge>jGraphModelAdapter) {
            //Inherits name from JFrame
            super(name);
            this.jgraph = jgraph;
            this.listenableGraph = listenableGraph;
            this.jGraphModelAdapter = jGraphModelAdapter;

           init();
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

            public Object[] getColumnOneData()
            {
                return data.stream().map(item -> item[0]).sorted(String::compareTo)
                    .collect(Collectors.<String>toList()).toArray();
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
         * Builds the Home Page
         *
         * @param page The home tab
         */
        private void buildHomePage(JPanel page) {

            JPanel vertexPane = new JPanel(new BorderLayout());
            vertexPane.setBorder(new TitledBorder ("Vertexes"));

            JPanel edgePane = new JPanel(new BorderLayout());
            edgePane.setBorder(new TitledBorder ("Edges"));

            JScrollPane vertexTablePane;
            JScrollPane edgeTablePane;

            // Vertex Table
            vertexTable = new JTable() {
                private static final long serialVersionUID = 1L;

                public boolean isCellEditable(int row, int column) {
                    return true;
                };
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
                };
            };

            edgeTable.setModel(new TableListModel(new String[]{"Source", "Target"}));
            Dimension edgeTableDim = edgeTable.getPreferredScrollableViewportSize();
            edgeTableDim.setSize(edgeTable.getPreferredSize().getWidth(), 200);
            edgeTable.setPreferredScrollableViewportSize(edgeTableDim);
            edgeTable.setFillsViewportHeight(true);

            //Set up the editor for the cells.
            JComboBox edgeComboBox1 = new JComboBox();
            edgeTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(edgeComboBox1));

            //Set up the editor for the cells.
            JComboBox edgeComboBox2 = new JComboBox();
            edgeTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(edgeComboBox2));

            edgeTable.putClientProperty("terminateEditOnFocusLost", true);

            edgeTablePane = new JScrollPane(edgeTable);

            // Update Edge Edit Combo Boxes on edit un-focus
            vertexTable.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    edgeComboBox1.removeAllItems();
                    edgeComboBox2.removeAllItems();

                    for(Object item : ((TableListModel) vertexTable.getModel()).getColumnOneData())
                    {
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

            addVertexButton.addActionListener((event) -> {

                JTextField newVertex = new JTextField(10);

                JPanel myPanel = new JPanel();
                myPanel.add(new JLabel("Vertex:"));
                myPanel.add(newVertex);

                int result = JOptionPane.showConfirmDialog(null, myPanel,
                        "New Vertex", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    ((TableListModel) vertexTable.getModel()).addRow(new String[]{newVertex.getText()});

                    edgeComboBox1.removeAllItems();
                    edgeComboBox2.removeAllItems();

                    for(Object item : ((TableListModel) vertexTable.getModel()).getColumnOneData())
                    {
                        edgeComboBox1.addItem(item.toString());
                        edgeComboBox2.addItem(item.toString());
                    }
                }
            });

            // Delete vertex
            deleteVertexButton.addActionListener((event) -> {

                if(vertexTable.getSelectedRow() < 0)
                {
                    JOptionPane.showMessageDialog(null, "Select a Vertex first", "Vertex Not Selected",
                            JOptionPane.OK_OPTION);
                }
                else
                {
                    JDialog.setDefaultLookAndFeelDecorated(true);
                    int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the Vertex: " +
                                    ((TableListModel) vertexTable.getModel()).getValueAt(vertexTable.getSelectedRow(), 0) + "?", "Delete?",
                            JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        ((TableListModel) vertexTable.getModel()).deleteRow(vertexTable.getSelectedRow());

                        edgeComboBox1.removeAllItems();
                        edgeComboBox2.removeAllItems();

                        for(Object item : ((TableListModel) vertexTable.getModel()).getColumnOneData())
                        {
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

            addEdgeButton.addActionListener((event) -> {

                if(((TableListModel) vertexTable.getModel()).getColumnOneData().length == 0)
                {
                    JOptionPane.showMessageDialog(null, "Add a Vertex first", "No Vertexes",
                            JOptionPane.OK_OPTION);
                }
                else {

                    JComboBox sourceVertex = new JComboBox(((TableListModel) vertexTable.getModel()).getColumnOneData());
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

            deleteEdgeButton.addActionListener((event) -> {

                if(edgeTable.getSelectedRow() < 0)
                {
                    JOptionPane.showMessageDialog(null, "Select an Edge first", "Edge Not Selected",
                            JOptionPane.OK_OPTION);
                }
                else
                {
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


        // Initialize graph display
        public void init() {
            //
            tabs = new JTabbedPane();

            JPanel homePanel = new JPanel();
            buildHomePage(homePanel);

            tabs.addTab("Home", null, homePanel, "Home");
            tabs.addTab("Graph", null, jgraph, "Graph");

            // create a JGraphT graph
            adjustDisplaySettings(jgraph);
            getContentPane().add(tabs);
            setSize(DEFAULT_SIZE);

            // Position vertices within JGraph component
            final int[] columns = {0};
            final int[] x = {20};
            final int[] y = {20};
            listenableGraph.vertexSet().forEach(item -> {
                if(columns[0] == 3)
                {
                    x[0] = 20;
                    y[0] += 200;
                    columns[0] = 0;
                }

                positionVertexAt(item,  x[0], y[0]);

                x[0] += 180;
                columns[0]++;
            });
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

            GraphConstants.setBounds(attr, new Rectangle(x, y, (int) b.getWidth(), (int)b.getHeight()));

            Map<DefaultGraphCell, Map> cellAttr = new HashMap<DefaultGraphCell, Map>();
            cellAttr.put(cell, attr);
            jGraphModelAdapter.edit(cellAttr, null, null, null);
        }
    }

    public static void homeGUI()
    {
        ListenableGraph<String, DefaultEdge> listenableGraph = new ListenableDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

        JGraphModelAdapter<String, DefaultEdge> jGraphModelAdapter;
        // create a visualization using JGraph, via an adapter
        jGraphModelAdapter = new JGraphModelAdapter<>(listenableGraph);

        JGraph jgraph = new JGraph(jGraphModelAdapter);

        // add some sample data (graph manipulated via JGraphT)
        listenableGraph.addVertex("A");
        listenableGraph.addVertex("B");
        listenableGraph.addVertex("D");
        listenableGraph.addVertex("C");
        listenableGraph.addVertex("E");
        listenableGraph.addVertex("F");
        listenableGraph.addVertex("G");
        listenableGraph.addVertex("H");

        listenableGraph.addEdge("F", "A");
        listenableGraph.addEdge("F", "E");
        listenableGraph.addEdge("A", "B");
        listenableGraph.addEdge("A", "H");
        listenableGraph.addEdge("B", "C");
        listenableGraph.addEdge("C", "D");
        listenableGraph.addEdge("D", "H");
        listenableGraph.addEdge("H", "G");
        listenableGraph.addEdge("E", "G");

        displayGraph("One", jgraph, listenableGraph, jGraphModelAdapter);
    }

    // Display graph
    static void displayGraph(String name, JGraph jgraph,
                             ListenableGraph<String, DefaultEdge> listenableGraph,
                             JGraphModelAdapter<String, DefaultEdge>jGraphModelAdapter) {

        //Create and set up a new GUI
        GraphGUI frame = new GraphGUI(name, jgraph, listenableGraph, jGraphModelAdapter);

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

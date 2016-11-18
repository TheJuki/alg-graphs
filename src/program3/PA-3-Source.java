package program3;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Main {

    public static void main(String[] args)
    {
        createAndShowGUI();
    }

    public static class GUI extends JFrame {
        private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
        private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);

        // Constructor for GUI
        private GUI(String name) {
            //Inherits name from JFrame
            super(name);

           init();
        }

        //
        private JGraphModelAdapter<String, DefaultEdge> m_jgAdapter;

        /**
         * Main demo entry point.
         */
        public void init() {
            // create a JGraphT graph
            ListenableGraph<String, DefaultEdge> g = new ListenableDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

            // create a visualization using JGraph, via an adapter
            m_jgAdapter = new JGraphModelAdapter<>(g);

            JGraph jgraph = new JGraph(m_jgAdapter);

            adjustDisplaySettings(jgraph);
            getContentPane().add(jgraph);
            setSize(DEFAULT_SIZE);

            // add some sample data (graph manipulated via JGraphT)
            g.addVertex("A");
            g.addVertex("B");
            g.addVertex("D");
            g.addVertex("C");
            g.addVertex("E");
            g.addVertex("F");
            g.addVertex("G");
            g.addVertex("H");

            g.addEdge("F", "A");
            g.addEdge("F", "E");
            g.addEdge("A", "B");
            g.addEdge("A", "H");
            g.addEdge("B", "C");
            g.addEdge("C", "D");
            g.addEdge("D", "H");
            g.addEdge("H", "G");
            g.addEdge("E", "G");

            // position vertices nicely within JGraph component

            final int[] columns = {0};
            final int[] x = {20};
            final int[] y = {20};
            g.vertexSet().forEach(item -> {
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


            //positionVertexAt("A", 130, 40);
            //positionVertexAt("v2", 60, 200);
            //positionVertexAt("v3", 310, 230);
            //positionVertexAt("v4", 380, 70);

            // that's all there is to it!...
        }


        private void adjustDisplaySettings(JGraph jg) {
            jg.setPreferredSize(DEFAULT_SIZE);

            String colorStr = null;

            jg.setBackground(DEFAULT_BG_COLOR);
        }


        private void positionVertexAt(Object vertex, int x, int y) {
            DefaultGraphCell cell = m_jgAdapter.getVertexCell(vertex);
            Map attr = cell.getAttributes();
            Rectangle2D b = GraphConstants.getBounds(attr);

            GraphConstants.setBounds(attr, new Rectangle(x, y, (int) b.getWidth(), (int)b.getHeight()));

            Map<DefaultGraphCell, Map> cellAttr = new HashMap<DefaultGraphCell, Map>();
            cellAttr.put(cell, attr);
            m_jgAdapter.edit(cellAttr, null, null, null);
        }
    }

    static void createAndShowGUI() {
        //Create and set up a new GUI
        GUI frame = new GUI("PA-2 Mergesort");

        //Exit on close
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set up the content pane.
        //Display the window.
        frame.pack();
        //Set location
        frame.setLocationRelativeTo(null);

        //Show the GUI
        frame.setSize(500, 550);
        frame.setVisible(true);
        frame.setResizable(false);

    } // createAndShowGUI

    public class GraphPanel extends JPanel
    {
        private final int SIZE = 10;  // radius of each node

        private Point point1 = null, point2 = null;

        private ArrayList<Point> nodeList;   // Graph nodes
        private ArrayList<Edge> edgeList;    // Graph edges

        private int[][] a = new int[100][100];  // Graph adjacency matrix

        public GraphPanel()
        {
            nodeList = new ArrayList<Point>();
            edgeList = new ArrayList<Edge>();

            GraphListener listener = new GraphListener();
            addMouseListener (listener);
            addMouseMotionListener (listener);

            JButton print = new JButton("Print adjacency matrix");
            print.addActionListener (new ButtonListener());

            setBackground (Color.black);
            setPreferredSize (new Dimension(400, 300));
            add(print);
        }

        //  Draws the graph
        public void paintComponent (Graphics page)
        {
            super.paintComponent(page);

            // Draws the edge that is being dragged
            page.setColor (Color.green);
            if (point1 != null && point2 != null) {
                page.drawLine (point1.x, point1.y, point2.x, point2.y);
                page.fillOval (point2.x-3, point2.y-3, 6, 6);
            }
// Draws the nodes
            for (int i=0; i<nodeList.size(); i++) {
                page.setColor (Color.green);
                page.fillOval (nodeList.get(i).x-SIZE, nodeList.get(i).y-SIZE, SIZE*2, SIZE*2);
                page.setColor (Color.black);
                page.drawString (String.valueOf(i), nodeList.get(i).x-SIZE/2, nodeList.get(i).y+SIZE/2);
            }
// Draws the edges
            for (int i=0; i<edgeList.size(); i++) {
                page.setColor (Color.green);
                page.drawLine (edgeList.get(i).a.x, edgeList.get(i).a.y,edgeList.get(i).b.x, edgeList.get(i).b.y);
                page.fillOval (edgeList.get(i).b.x-3, edgeList.get(i).b.y-3, 6, 6);
            }
        }

        //  The listener for mouse events.
        private class GraphListener implements MouseListener, MouseMotionListener
        {
            public void mouseClicked (MouseEvent event)
            {
                nodeList.add(event.getPoint());
                repaint();
            }

            public void mousePressed (MouseEvent event)
            {
                point1 = event.getPoint();
            }

            public void mouseDragged (MouseEvent event)
            {
                point2 = event.getPoint();
                repaint();
            }

            public void mouseReleased (MouseEvent event)
            {
                point2 = event.getPoint();
                if (point1.x != point2.x && point1.y != point2.y)
                {
                    edgeList.add(new Edge(point1,point2));
                    repaint();
                }
            }

            //  Empty definitions for unused event methods.
            public void mouseEntered (MouseEvent event) {}
            public void mouseExited (MouseEvent event) {}
            public void mouseMoved (MouseEvent event) {}
        }

        // Represents the graph edges
        private class Edge {
            Point a, b;

            public Edge(Point a, Point b)
            {
                this.a = a;
                this.b = b;
            }
        }
        private class ButtonListener implements ActionListener
        {
            public void actionPerformed (ActionEvent event)
            {
// Initializes graph adjacency matrix
                for (int i=0; i<nodeList.size(); i++)
                    for (int j=0; j<nodeList.size(); j++) a[i][j]=0;

// Includes the edges in the graph adjacency matrix
                for (int i=0; i<edgeList.size(); i++)
                {
                    for (int j=0; j<nodeList.size(); j++)
                        if (distance(nodeList.get(j),edgeList.get(i).a)<=SIZE+3)
                            for (int k=0; k<nodeList.size(); k++)
                                if (distance(nodeList.get(k),edgeList.get(i).b)<=SIZE+3)
                                {
                                    System.out.println(j+"->"+k);
                                    a[j][k]=1;
                                }
                }
// Prints the graph adjacency matrix
                for (int i=0; i<nodeList.size(); i++)
                {
                    for (int j=0; j<nodeList.size(); j++)
                        System.out.print(a[i][j]+"\t");
                    System.out.println();
                }
            }

            // Euclidean distance function
            private int distance(Point p1, Point p2) {
                return (int)Math.sqrt((p1.x-p2.x)*(p1.x-p2.x)+(p1.y-p2.y)*(p1.y-p2.y));
            }
        }

    }

}

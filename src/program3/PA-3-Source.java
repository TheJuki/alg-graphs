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
import java.awt.geom.Rectangle2D;
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
            //positionVertexAt("v1", 130, 40);
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

}

/*
 * Playing with Search Algorithms
 * Giridhar Murali - for EECS492: Introduction to Artificial Intelligence
 */

package Search;

import java.io.*;
import java.util.*;
import java.awt.Polygon;
import java.awt.Point;
import java.awt.geom.*;
import java.lang.String;
import java.util.Vector;
import java.util.Collections;

/**
 *
 * @author Giridhar Murali
 */
class nodes {

    public int node_x, node_y, polygon_id, poly_size;
    public Vector<nodes> visible_nodes = new Vector<nodes>();
    public Vector weights = new Vector();

    public int weight_between_nodes(nodes n1, nodes n2) {
        int weight = ((n2.node_x - n1.node_x) * (n2.node_x - n1.node_x)) + ((n2.node_y - n1.node_y) * (n2.node_y - n1.node_y));
        return weight;
    }
};

class search_base {

    Vector<Vector<nodes>> queue = new Vector<Vector<nodes>>();
    Vector<Vector<nodes>> path_q = new Vector<Vector<nodes>>();

    protected void expand(Vector<Vector<nodes>> q, Vector<nodes> ex, Vector<nodes> p, Vector<Vector<nodes>> path_q, Vector<Vector<nodes>> log_nodes, Vector<Integer> log_queue_size) {
        nodes last_node = p.lastElement();
        Vector<nodes> temp_log = new Vector<nodes>();
        temp_log.add(last_node);
        //log_nodes.add(last_node);
        for (int i = 0; i < last_node.visible_nodes.size(); i++) {
            temp_log.add(last_node.visible_nodes.get(i));
        }
        log_nodes.add(temp_log);
        log_queue_size.add(q.size());
        int unexplored_nodes = 0;
        if (last_node.visible_nodes.size() == 0) {
            path_q.add(p);
        } else {
            for (int loop = 0; loop < last_node.visible_nodes.size(); loop++) {
                boolean in_explored = ex.contains(last_node.visible_nodes.get(loop));
                if (in_explored == false) {
                    Vector<nodes> temp_p = new Vector<nodes>();
                    temp_p.addAll(p);
                    temp_p.add(last_node.visible_nodes.get(loop));
                    q.add(temp_p);
                    unexplored_nodes++;
                }
            }
            if (unexplored_nodes == last_node.visible_nodes.size()) {
                path_q.add(p);
            }
        }
    }

    protected boolean possibility(Vector<Vector<nodes>> queue) {
        return !queue.isEmpty();
    }

    protected Vector<nodes> get_next_path(Vector<Vector<nodes>> q, nodes end_node) {
        return q.remove(0);
    }

    public boolean search(nodes start_node, nodes end_node, Vector<nodes> solution, Vector<Vector<nodes>> partial_path, Vector<Vector<nodes>> log_nodes, Vector<Integer> log_queue_size) {
        Vector<nodes> path = new Vector<nodes>();
        Vector<nodes> explored = new Vector<nodes>();
        path.add(start_node);
        queue.add(path);
        while (possibility(queue)) {
            Vector<nodes> temp_p = get_next_path(queue, end_node);
            nodes temp_node = temp_p.lastElement();
            if (temp_node != end_node) {
                explored.add(temp_node);
                expand(queue, explored, temp_p, path_q, log_nodes, log_queue_size);
            } else {
                solution.addAll(temp_p);
                partial_path.addAll(path_q);
                System.out.println("Path Length=" + path_q.size());
                partial_path.addAll(queue);
                System.out.println("Queue Length=" + queue.size());
                partial_path.remove(partial_path.size() - 2);
                return true;
            }
        }
        return false;
    }
}

class breadth_first extends search_base {

    protected Vector<nodes> get_next_path(Vector<Vector<nodes>> q, nodes end_node) {
        return q.remove(0);
    }
}

class depth_first extends search_base {

    protected Vector<nodes> get_next_path(Vector<Vector<nodes>> q, nodes end_node) {
        return q.remove(q.size() - 1);
    }
}

class best_first extends search_base {

    protected Vector<nodes> get_next_path(Vector<Vector<nodes>> q, nodes end_node) {
        int shortest = 9999999;
        int shortest_node = 0;
        for (int i = 0; i < q.size(); i++) {
            Vector<nodes> p_arb = q.get(i);
            nodes n_arb = p_arb.get(p_arb.size() - 1);
            int heuristic = (((end_node.node_x - n_arb.node_x) * (end_node.node_x - n_arb.node_x)) + ((end_node.node_y - n_arb.node_y) * (end_node.node_y - n_arb.node_y)));
            if (heuristic < shortest) {
                shortest_node = i;
                shortest = heuristic;
            }
        }
        return q.remove(shortest_node);
    }
}

class a_star extends search_base {

    protected Vector<nodes> get_next_path(Vector<Vector<nodes>> q, nodes end_node) {
        int shortest = 9999999;
        int shortest_node = 0;
        int weight = 0;
        for (int i = 0; i < q.size(); i++) {
            Vector<nodes> p_arb = q.get(i);
            nodes n_arb = p_arb.get(p_arb.size() - 1);
            //System.out.println("Size="+p_arb.size());
            if (p_arb.size() > 1) {
                nodes n_arb2 = p_arb.get(p_arb.size() - 2);
                weight = (((n_arb2.node_x - n_arb.node_x) * (n_arb2.node_x - n_arb.node_x)) + ((n_arb2.node_y - n_arb.node_y) * (n_arb2.node_y - n_arb.node_y)));
            }
            int heuristic = (((end_node.node_x - n_arb.node_x) * (end_node.node_x - n_arb.node_x)) + ((end_node.node_y - n_arb.node_y) * (end_node.node_y - n_arb.node_y)));

            if (heuristic + weight < shortest) {
                shortest_node = i;
                shortest = heuristic + weight;
            }
        }
        return q.remove(shortest_node);
    }
}

class iter_deep extends search_base {

    int currentDepthLimit = 2;

    protected Vector<nodes> get_next_path(Vector<Vector<nodes>> q, nodes end_node) {
        for (int i = 0; i < q.size(); i++) {
            Vector<nodes> p_arb = q.get(i);
            if (p_arb.size() < currentDepthLimit) {
                return q.remove(i);
            }
        }
        return null;
    }

    public boolean search(nodes start_node, nodes end_node, Vector<nodes> solution, Vector<Vector<nodes>> partial_path, Vector<Vector<nodes>> log_nodes, Vector<Integer> log_queue_size) {
        int maxDepthLimit = 50;
        while (currentDepthLimit < maxDepthLimit) {
            if (super.search(start_node, end_node, solution, path_q, log_nodes, log_queue_size)) {
                return true;
            }
            queue.clear();
            currentDepthLimit++;
            System.out.println("Current Depth Limit =" + currentDepthLimit);
        }
        return false;
    }

    protected boolean possibility(Vector<Vector<nodes>> queue) {
        int paths_present = 0;
        for (int i = 0; i < queue.size(); i++) {
            Vector select_path = queue.get(i);
            if (select_path.size() < currentDepthLimit) {
                paths_present++;
            }
        }
        if (paths_present > 0) {
            return true;
        } else {
            return false;
        }
    }
}

class ida_star extends search_base {

    int currentDepthLimit = 2;

    protected Vector<nodes> get_next_path(Vector<Vector<nodes>> q, nodes end_node) {
        int shortest = 9999999;
        int shortest_node = 0;
        int weight = 0;
        for (int i = 0; i < q.size(); i++) {
            Vector<nodes> p_arb = q.get(i);
            if (p_arb.size() < currentDepthLimit) {
                nodes n_arb = p_arb.get(p_arb.size() - 1);
                if (p_arb.size() > 1) {
                    nodes n_arb2 = p_arb.get(p_arb.size() - 2);
                    weight = (((n_arb2.node_x - n_arb.node_x) * (n_arb2.node_x - n_arb.node_x)) + ((n_arb2.node_y - n_arb.node_y) * (n_arb2.node_y - n_arb.node_y)));
                }
                int heuristic = (((end_node.node_x - n_arb.node_x) * (end_node.node_x - n_arb.node_x)) + ((end_node.node_y - n_arb.node_y) * (end_node.node_y - n_arb.node_y)));
                if (heuristic + weight < shortest) {
                    shortest_node = i;
                    shortest = heuristic + weight;
                }
            }
        }
        return q.remove(shortest_node);
    }

    public boolean search(nodes start_node, nodes end_node, Vector<nodes> solution, Vector<Vector<nodes>> partial_path, Vector<Vector<nodes>> log_nodes, Vector<Integer> log_queue_size) {
        int maxDepthLimit = 50;
        while (currentDepthLimit < maxDepthLimit) {
            if (super.search(start_node, end_node, solution, path_q, log_nodes, log_queue_size)) {
                return true;
            }
            queue.clear();
            currentDepthLimit++;
            System.out.println("Current Depth Limit =" + currentDepthLimit);
        }
        return false;
    }

    protected boolean possibility(Vector<Vector<nodes>> queue) {
        int paths_present = 0;
        for (int i = 0; i < queue.size(); i++) {
            Vector select_path = queue.get(i);
            if (select_path.size() < currentDepthLimit) {
                paths_present++;
            }
        }
        if (paths_present > 0) {
            return true;
        } else {
            return false;
        }
    }
}

class bidir extends search_base {

    Vector<Vector<nodes>> queue2 = new Vector<Vector<nodes>>();

    public boolean search(nodes start_node, nodes end_node, Vector<nodes> solution, Vector<Vector<nodes>> partial_path, Vector<Vector<nodes>> log_nodes, Vector<Integer> log_queue_size) {
        Vector<nodes> path1 = new Vector<nodes>();
        Vector<nodes> explored1 = new Vector<nodes>();
        Vector<nodes> path2 = new Vector<nodes>();
        Vector<nodes> explored2 = new Vector<nodes>();
        path1.add(start_node);
        path2.add(end_node);
        queue.add(path1);
        queue2.add(path2);
        while (possibility(queue) && possibility(queue2)) {
            Vector<nodes> temp_p1 = get_next_path(queue, end_node);
            Vector<nodes> temp_p2 = get_next_path(queue2, start_node);


            nodes temp_node1 = temp_p1.lastElement();
            nodes temp_node2 = temp_p2.lastElement();
            int connection = 0;
            nodes connecting_node = new nodes();
            if (temp_node1 != temp_node2) {
                for (int check1 = 0; check1 < temp_node1.visible_nodes.size(); check1++) {
                    for (int check2 = 0; check2 < temp_node2.visible_nodes.size(); check2++) {
                        if (temp_node1.visible_nodes.get(check1) == temp_node2.visible_nodes.get(check2)) {
                            connection = 1;
                            connecting_node = temp_node1.visible_nodes.get(check1);

                        }

                    }
                }
                if (connection == 0) {
                    explored1.add(temp_node1);
                    explored2.add(temp_node2);
                    expand(queue, explored1, temp_p1, path_q, log_nodes, log_queue_size);
                    expand(queue2, explored2, temp_p2, path_q, log_nodes, log_queue_size);
                } else {
                    solution.addAll(temp_p1);
                    Collections.reverse(temp_p2);
                    solution.add(connecting_node);
                    solution.addAll(temp_p2);
                    partial_path.addAll(path_q);
                    partial_path.addAll(queue);
                    partial_path.remove(partial_path.size() - 2);
                    return true;
                }

            } else {
                solution.addAll(temp_p1);
                Collections.reverse(temp_p2);
                temp_p2.remove(0);
                solution.addAll(temp_p2);
                partial_path.addAll(path_q);
                partial_path.addAll(queue);
                partial_path.remove(partial_path.size() - 2);
                return true;
            }
        }
        return false;
    }
}

class beam_search extends search_base {

    int beamwidth = 3;

    protected Vector<nodes> get_next_path(Vector<Vector<nodes>> q, nodes end_node) {
        int shortest = 9999999;
        int shortest_node = 0;
        if (q.size() >= beamwidth) {
            for (int i = 0; i < beamwidth; i++) {
                Vector<nodes> p_arb = q.get(i);
                nodes n_arb = p_arb.get(p_arb.size() - 1);
                int heuristic = (((end_node.node_x - n_arb.node_x) * (end_node.node_x - n_arb.node_x)) + ((end_node.node_y - n_arb.node_y) * (end_node.node_y - n_arb.node_y)));
                if (heuristic < shortest) {
                    shortest_node = i;
                    shortest = heuristic;
                }
            }
        } else {
            for (int i = 0; i < q.size(); i++) {
                Vector<nodes> p_arb = q.get(i);
                nodes n_arb = p_arb.get(p_arb.size() - 1);
                int heuristic = (((end_node.node_x - n_arb.node_x) * (end_node.node_x - n_arb.node_x)) + ((end_node.node_y - n_arb.node_y) * (end_node.node_y - n_arb.node_y)));
                if (heuristic < shortest) {
                    shortest_node = i;
                    shortest = heuristic;
                }
            }
        }

        return q.remove(shortest_node);
    }
}

public class Main {

    static ArrayList<Polygon> polygonsArr;
    Vector<nodes> node = new Vector<nodes>();

    /**
     * @param args the command line arguments
     */
    void print_paths(Vector<nodes> p) {
    }

    boolean check_intersect(nodes n1, nodes n2) {
        boolean result = false;
        int node_limit = 0;
        int i1, i2;
        Line2D.Float current_line = new Line2D.Float(n1.node_x, n1.node_y, n2.node_x, n2.node_y);
        for (int i = 0; i < node.size(); i++) {
            if (node_limit < node.get(i).poly_size - 1) {
                i1 = i;
                i2 = i1 + 1;
            } else {
                i1 = i;
                i2 = i1 - node.get(i).poly_size + 1;
                node_limit = -1;
            }
            node_limit++;
            if ((n1 == node.get(i1)) || (n1 == node.get(i2)) || (n2 == node.get(i1)) || (n2 == node.get(i2))) {
                continue;
            } else {
                result = current_line.intersectsLine(node.get(i1).node_x, node.get(i1).node_y, node.get(i2).node_x, node.get(i2).node_y);
                if (result == true) {
                    return result;
                }
            }
        }

        return result;
    }

    public void inter_polygon_mapping() {
        System.out.println("Inter Polygon Mapping Initiated..");
        for (int iter = 0; iter < node.size() - 1; iter++) {
            for (int iter2 = iter + 1; iter2 < node.size(); iter2++) {
                if (node.get(iter).polygon_id != node.get(iter2).polygon_id) {
                    if (check_intersect(node.get(iter), node.get(iter2)) == false) {
                        int weights_temp;
                        node.get(iter).visible_nodes.add(node.get(iter2));
                        weights_temp = node.get(iter).weight_between_nodes(node.get(iter), node.get(iter2));
                        node.get(iter).weights.add(weights_temp);
                        node.get(iter2).visible_nodes.add(node.get(iter));
                        node.get(iter2).weights.add(weights_temp);
                    }
                }

            }
        }
    }

    void map_input_nodes(nodes n) {
        for (int iter = 0; iter < node.size(); iter++) {
            if (node.get(iter) == n) {
                continue;
            }
            if (check_intersect(n, node.get(iter)) == false) {
                int weights_temp;
                n.visible_nodes.add(node.get(iter));
                weights_temp = node.get(iter).weight_between_nodes(n, node.get(iter));
                node.get(iter).weights.add(weights_temp);
                node.get(iter).visible_nodes.add(n);
                n.weights.add(weights_temp);
            }

        }
    }

    public static String removeChar(String s, char c) {
        StringBuilder r = new StringBuilder(s.length());
        r.setLength(s.length());
        int current = 0;
        for (int i = 0; i < s.length(); i++) {
            char cur = s.charAt(i);
            if (cur != c) {
                r.setCharAt(current++, cur);
            }
        }
        return r.toString();
    }

    public static void main(String[] args) {
        System.out.println("Started");
        Main m = new Main();
        try {
            System.out.println("Entered Try Block. Waiting for arguments...");
            FileInputStream fr = new FileInputStream(args[0]);
            System.out.println("FIS Step done...");
            DataInputStream in = new DataInputStream(fr);
            System.out.println("DIS Step done...");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            System.out.println("BIS Step done..File Read Complete!");
            String strLine;
            System.out.println("Entered Try Block");
            int polygon_counter = 1;
            int node_counter = -1;
            while ((strLine = br.readLine()) != null) {
                String[] points_str = strLine.split(" ");
                for (int i = 0; i < points_str.length; i++) {
                    node_counter++;
                    points_str[i] = removeChar(points_str[i], '[');
                    points_str[i] = removeChar(points_str[i], ']');
                    String[] tmp = points_str[i].split(",");
                    int x = Integer.parseInt(tmp[0].trim());
                    int y = Integer.parseInt(tmp[1].trim());
                    //System.out.println(x+","+y);
                    m.node.add(new nodes());
                    m.node.get(node_counter).node_x = x;
                    m.node.get(node_counter).node_y = y;
                    m.node.get(node_counter).polygon_id = polygon_counter;
                    m.node.get(node_counter).poly_size = points_str.length;
                }
                // Self-Connect Routine
                System.out.println("Self-Connect Routine Engaged" + node_counter + "Nodes" + points_str.length + "Points");
                int weight_temp = 0;
                for (int i = node_counter - points_str.length + 1; i < node_counter; i++) {
                    System.out.println("Entered Loop");
                    if (i == node_counter - points_str.length + 1) {
                        System.out.println("Entered If Condition");
                        m.node.get(i).visible_nodes.add(m.node.get(node_counter));
                        // System.out.println("1");
                        weight_temp = m.node.get(i).weight_between_nodes(m.node.get(i), m.node.get(node_counter));
                        // System.out.println("2");
                        m.node.get(i).weights.add(weight_temp);
                        // System.out.println("3");
                        m.node.get(node_counter).visible_nodes.add(m.node.get(i));
                        // System.out.println("4");
                        m.node.get(node_counter).weights.add(weight_temp);
                        System.out.println("First-last Matched!");
                    }
                    System.out.println("Starting Connections..");
                    m.node.get(i).visible_nodes.add(m.node.get(i + 1));
                    //System.out.println("1");
                    weight_temp = m.node.get(i).weight_between_nodes(m.node.get(i), m.node.get(i + 1));
                    //System.out.println("2");
                    m.node.get(i).weights.add(weight_temp);
                    //System.out.println("3");
                    m.node.get(i + 1).visible_nodes.add(m.node.get(i));
                    m.node.get(i + 1).weights.add(weight_temp);
                    System.out.println("Subsequent Matching Progress..");
                }
                //------------------------------------------------------------------------
                polygon_counter++;
            }
            in.close();
            System.out.println("Self Connect Routine Successful!");
            // Test printing of elements
    /*
            for(int j=0;j<polygonsArr.size();j++)
            {
            Polygon p = new Polygon();
            p= polygonsArr.get(j);
            System.out.println("Printing P: "+ p);
            int np,xp,yp;
            np= p.npoints;
            System.out.println("No. of points in Polygon"+j+"is"+np);
            for(int k=0;k<np;k++)
            {
            System.out.println("X_point="+ p.xpoints[k]+"Y_Point="+p.ypoints[k]);
            //yp= p.ypoints(k);
            }
            }*/
            //  self_connect_nodes();
            m.inter_polygon_mapping();
            //System.out.println("Interpolygon Mapping Done!");
            // Printing Polygon Elements
            int start_x, start_y, end_x, end_y;
            start_x = Integer.parseInt(args[1]);
            start_y = Integer.parseInt(args[2]);
            end_x = Integer.parseInt(args[3]);
            end_y = Integer.parseInt(args[4]);
            int start_present = 0, end_present = 0;
            nodes start_node = null, end_node = null;
            System.out.println("Start:" + start_x + "," + start_y + "  End:" + end_x + "," + end_y);
            node_counter++;
            //Routine to check existence of points
            for (int j = 0; j < m.node.size(); j++) {
                if ((start_x == m.node.get(j).node_x) && (start_y == m.node.get(j).node_y)) {
                    start_node = m.node.get(j);
                    start_present = 1;
                }
            }
            if (start_present == 1) {
                System.out.println("Start is already a Node!");
            } else {
                System.out.println("Start node added..");
                nodes n = new nodes();
                n.node_x = start_x;
                n.node_y = start_y;
                n.poly_size = 1;
                n.polygon_id = -1;
                m.node.add(n);
                start_node = n;
                //node.get(node_counter).polygon_id=polygon_counter;
                m.map_input_nodes(n);
                node_counter++;
                //p.addPoint(x,y);
            }

            for (int j = 0; j < m.node.size(); j++) {
                if ((end_x == m.node.get(j).node_x) && (end_y == m.node.get(j).node_y)) {
                    end_present = 1;
                    end_node = m.node.get(j);
                }
            }
            if (end_present == 1) {
                System.out.println("End is already a Node!");
            } else {
                System.out.println("End node added..");
                end_node = new nodes();
                end_node.node_x = end_x;
                end_node.node_y = end_y;
                //end_node.polygon_id=polygon_counter;
                end_node.poly_size = 1;
                m.node.add(end_node);
                m.map_input_nodes(end_node);
            }


            // Printing Visibility Graph
            for (int i = 0; i < m.node.size(); i++) {
                System.out.println("Node (" + m.node.get(i).node_x + "," + m.node.get(i).node_y + ")");
                System.out.println("Visible Nodes");
                for (int j = 0; j < m.node.get(i).visible_nodes.size(); j++) {
                    System.out.println(m.node.get(i).visible_nodes.get(j).node_x + "," + m.node.get(i).visible_nodes.get(j).node_y);
                }
            }
            Vector<nodes> solution = new Vector<nodes>();
            Vector<Vector<nodes>> partial_path = new Vector<Vector<nodes>>();
            Vector<Vector<nodes>> log_nodes = new Vector<Vector<nodes>>();
            Vector<Integer> log_queue_size = new Vector<Integer>();
            boolean success = true;
            search_base s = new search_base();
            if (args[5].equals("breadth")) {
                s = new breadth_first();
            } else if (args[5].equals("depth")) {
                s = new depth_first();
            } else if (args[5].equals("best")) {
                s = new best_first();
            } else if (args[5].equals("bidir")) {
                s = new bidir();
            } else if (args[5].equals("beam")) {
                s = new beam_search();
            } else if (args[5].equals("id")) {
                s = new iter_deep();
            } else if (args[5].equals("astar")) {
                s = new a_star();
            } else if (args[5].equals("idastar")) {
                s = new ida_star();
            }
            success = s.search(start_node, end_node, solution, partial_path, log_nodes, log_queue_size);
            System.out.println(success);
            // Writing Solution into File
            BufferedWriter out = new BufferedWriter(new FileWriter(args[6]));
            if (success) {

                for (int i = 0; i < solution.size(); i++) {
                    System.out.println("[" + solution.get(i).node_x + "," + solution.get(i).node_y + "]");
                    out.write("[");
                    String aString = Integer.toString(solution.get(i).node_x);
                    out.write(aString);
                    out.write(",");
                    aString = Integer.toString(solution.get(i).node_y);
                    out.write(aString);
                    out.write("] ");
                }
                out.close();
                System.out.println("The data has been written");
            } else {
                out.write("No Solution Exists.");
            }


            //--------------- Writing into Path File -------------------//
            BufferedWriter path_file = new BufferedWriter(new FileWriter(args[7]));
            for (int i = 0; i < partial_path.size(); i++) {
                for (int j = 0; j < partial_path.get(i).size(); j++) {
                    System.out.println("[" + partial_path.get(i).get(j).node_x + "," + partial_path.get(i).get(j).node_y + "]");
                    path_file.write("[");
                    String aString = Integer.toString(partial_path.get(i).get(j).node_x);
                    System.out.println("x=" + aString);
                    path_file.write(aString);
                    path_file.write(",");
                    aString = Integer.toString(partial_path.get(i).get(j).node_y);
                    System.out.println("y=" + aString);
                    path_file.write(aString);
                    path_file.write("] ");
                }
                path_file.write("\r\n");
            }
            path_file.close();
            System.out.println("The data has been written");


            //--------Writing into Log File---------------//
            BufferedWriter log_file = new BufferedWriter(new FileWriter(args[8]));
            String aString;
            for (int i = 0; i < log_nodes.size(); i++) {
                for (int j = 0; j < log_nodes.get(i).size(); j++) {
                    if (j == 1) {
                        log_file.write(" ");
                    }
                    System.out.println("[" + log_nodes.get(i).get(j).node_x + "," + log_nodes.get(i).get(j).node_y + "]");
                     log_file.write("[");
                    aString = Integer.toString(log_nodes.get(i).get(j).node_x);
                    System.out.println("x=" + aString);
                    log_file.write(aString);
                    log_file.write(",");
                    aString = Integer.toString(log_nodes.get(i).get(j).node_y);
                    System.out.println("y=" + aString);
                    log_file.write(aString);
                    log_file.write("] ");
                }
                //  log_file.write("\tSize:");
                aString = Integer.toString(log_queue_size.get(i));
                log_file.write(aString);
                log_file.write("\r\n");
            }
            log_file.close();
            System.out.println("The data has been written");
            //------Routine Complete--------------//

        } catch (Exception e) {
            System.out.println("Catch Block Entered. You're screwed!" + e.toString());
        }
    }
}

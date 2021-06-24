import java.awt.Color;
import edu.princeton.cs.algs4.Picture;

public class QuadTree {
    
    // Axis-aligned rectangle helper class to store each node's subregion represented as
    // a (x_min, y_min) (x_max, y_max) pair
    private class Rectangle {
        public int xmin, xmax, ymin, ymax;
        public Rectangle(int xmin, int xmax, int ymin, int ymax) {
            this.xmin = xmin;
            this.xmax = xmax;
            this.ymin = ymin;
            this.ymax = ymax;
        }
    } 
   
    private class Node {
        private Color avg;        // the average color of this subregion
        private Rectangle rect;   // this node's rectangle  
        private double var;       // the variance between the colors in this region (a.k.a error)
        Node nw, ne, sw, se;      // pointers to childrens. Ini at null
        
        public Node(int xmin, int ymin, int xmax, int ymax, Color avg, double var) {
            this.rect = new Rectangle(xmin, xmax, ymin, ymax);
            this.var  = var;
            this.avg  = avg;
        }
    }

    private Node root;

    public QuadTree(Picture pic, double tol) { 
        root = construct(root, pic, tol);
    }
}
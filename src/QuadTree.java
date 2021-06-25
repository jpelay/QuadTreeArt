import java.awt.Color;
import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;

public class QuadTree {
    
    private Node root;
    private double tol; 
   
    private class Node {
        private Color avg;           // the average color of this subregion
        private double var;          // the variance between the colors in this region (a.k.a error)
        private int x, y;            // the upper left coor of this nodes rectangle  
        private int width, height;   // the width and height of this rectangle
        private Node nw, ne, sw, se; // pointers to childrens. Ini at null
        
        public Node(int x, int y, int width, int height) {
            this.x      = x;
            this.y      = y;
            this.width  = width;
            this.height = height;
        }

        public void computeAverageColor(Picture pic) {
            long r = 0;
            long g = 0;
            long b = 0;
    
            for (int i = this.x; i < this.height + this.x; i++) {
                for (int j = this.y; j < this.width + this.y; j++) {
                    Color c = pic.get(i, j);
                    r += c.getRed();
                    g += c.getGreen();
                    b += c.getBlue();
                }
            }
            long area = this.width*this.height;
            this.avg = new Color(r/area, g/area, b/area);
        }

        public void computeVarianceColor(Picture pic) {
            long sum = 0;
            for (int i = this.x; i < this.height + this.x; i++) {
                for (int j = this.y; j < this.width + this.y; j++) {
                    Color c = pic.get(i, j);
                    sum += c.getRed();
                    sum += c.getGreen();
                    sum += c.getBlue();
                }
            }
            var = (double) sum / (3*width*height);
        }
    }

    public QuadTree(Picture pic, double tol) { 
       root = build(pic, root, 0, 0, pic.width(), pic.height());
    }

    private Node build(Picture pic, Node h, int x, int y, int width, int height) {
        h = new Node(x, y, width, height);
        h.computeAverageColor(pic);
        h.computeVarianceColor(pic);
        StdOut.println(h.var);
        int newWidth  = width / 2;
        int newHeight = height / 2;
        if (h.var < tol && (width >= 1 && height >= 1)) {
            h.nw = build(pic, h.nw, x, y, newWidth, newHeight);
            h.ne = build(pic, h.nw, x + newWidth, y, newWidth, newHeight);
            h.sw = build(pic, h.nw, x, y + newHeight, newWidth, newHeight);
            h.se = build(pic, h.nw, x + newWidth, y + newHeight, newWidth, newHeight);
        }
        return  h;
    }
}
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
    
            for (int i = this.x; i < this.width + this.x; i++) {
                for (int j = this.y; j < this.height + this.y; j++) {
                    Color c = pic.get(i, j);
                    r += c.getRed();
                    g += c.getGreen();
                    b += c.getBlue();
                }
            }
            long area = this.width*this.height;
            // Since the values will be between [0,255] we won't lose data
            int rv = (int)(r/area);
            int gv = (int)(g/area);
            int bv = (int)(b/area);
            this.avg = new Color(rv,gv,bv);
        }

        public void computeVarianceColor(Picture pic) {
            long sum = 0;
            for (int i = this.x; i < this.width + this.x; i++) {
                for (int j = this.y; j < this.height+ this.y; j++) {
                    Color c = pic.get(i, j);
                    sum += Math.abs(this.avg.getRed() - c.getRed());
                    sum += Math.abs(this.avg.getGreen() - c.getGreen());
                    sum += Math.abs(this.avg.getBlue() - c.getBlue());
                }
            }
            var = (double) sum / (3*width*height);
        }

        public boolean isLeaf() {
            return nw == null && ne == null && se == null && sw == null;
        }
    }

    public QuadTree(Picture pic, double tol) { 
       this.tol = tol;
        root = build(pic, root, 0, 0, pic.width(), pic.height(), 0);
    }

    private Node build(Picture pic, Node h, int x, int y, int width, int height, int depth) {
        h = new Node(x, y, width, height);
        h.computeAverageColor(pic);
        h.computeVarianceColor(pic);
        //StdOut.println(h.var);
        int newWidth  = width / 2;
        int newHeight = height / 2;
        int widthOffset = (width + 1) / 2;
        int heightOffset = (height + 1) / 2;
        if (h.var > tol && (width > 1 && height > 1) && depth < 10) { 
            h.nw = build(pic, h.nw, x, y, newWidth, newHeight, depth + 1);
            h.ne = build(pic, h.ne, x + newWidth, y, widthOffset, newHeight, depth + 1);
            h.sw = build(pic, h.sw, x, y + newHeight, newWidth, heightOffset, depth + 1);
            h.se = build(pic, h.se, x + newWidth, y + newHeight, widthOffset, heightOffset,depth + 1);
        }
        return  h;
    }

    public void draw() {
        Picture newPic = new Picture(root.width, root.height);
        draw(root, newPic);
        newPic.show();
        newPic.save("../img/out/out.png");
    }

    private void draw(Node h, Picture pic) {
        if (h.isLeaf()) {
            for (int i = h.x; i < h.width + h.x; i++) {
                for (int j = h.y; j < h.height + h.y; j++) {
                    pic.set(i, j, h.avg);
                }
            }
        } else {
            draw(h.nw, pic);
            draw(h.ne, pic);
            draw(h.se, pic);
            draw(h.sw, pic);
        }
    }

    public static void main(String[] args) {
        Picture pic = new Picture(args[0]);
        double tol = Double.parseDouble(args[1]);
        QuadTree qt = new QuadTree(pic, tol);
        qt.draw();
    }
}
import java.awt.Color;
import java.util.Queue;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class QuadTree {
    
    private Node root;  // the root of the tree
   
    private class Node implements Comparable<Node> {
        private Color avg;           // the average color of this subregion
        private int level;           // the level of this node   
        private double var;          // the variance between the colors in this region (a.k.a error)
        private int x, y;            // the upper left coor of this nodes rectangle  
        private int width, height;   // the width and height of this rectangle
        private Node nw, ne, sw, se; // pointers to childrens. Ini at null
        
        public Node(int x, int y, int width, int height, int level) {
            this.x      = x;
            this.y      = y;
            this.width  = width;
            this.height = height;
            this.level  = level;
        }
        
        public Node(Picture pic, int x, int y, int width, int height, int level) {
            this.x      = x;
            this.y      = y;
            this.width  = width;
            this.height = height;
            this.level  = level;
            computeAverageColor(pic);
            computeVarianceColor(pic);
        }

        public void computeAverageColor(Picture pic) {
            long r = 0;
            long g = 0;
            long b = 0;
    
            for (int i = this.x; i < this.width + this.x; i++) {
                for (int j = this.y; j < this.height + this.y; j++) {
                    Color c = pic.get(i, j);
                    r += c.getRed()*c.getRed();
                    g += c.getGreen()*c.getGreen();
                    b += c.getBlue()*c.getBlue();
                }
            }
            long area = this.width*this.height;
            // Since the values will be between [0,255] we won't lose data
            int ravg = (int)Math.sqrt(r/area);
            int gavg = (int)Math.sqrt(g/area);
            int bavg = (int)Math.sqrt(b/area);
            this.avg = new Color(ravg,gavg,bavg);
        }

        public void computeVarianceColor(Picture pic) {
            double r     = 0;
            double g     = 0;
            double b     = 0;
            double rsqrd = 0;
            double gsqrd = 0;
            double bsqrd = 0;

            for (int i = this.x; i < this.width + this.x; i++) {
                for (int j = this.y; j < this.height+ this.y; j++) {
                    Color c = pic.get(i, j);
                    r += c.getRed();
                    g += c.getGreen();
                    b += c.getBlue();
                    rsqrd += (c.getRed()*c.getRed());
                    gsqrd += (c.getGreen()*c.getGreen());
                    bsqrd += (c.getBlue()*c.getBlue());
                }
            }
            long area = this.width*this.height;
            this.var = (rsqrd - (r*r)/area) + (gsqrd - (g*g)/area) +    (bsqrd - (b*b)/area);
        }

        public boolean isLeaf() {
            return nw == null && ne == null && se == null && sw == null;
        }
        // reverse order
        public int compareTo(Node that) {
            if (this.var > that.var) return -1;
            if (this.var < that.var) return  1;
            else                     return  0;
        }
    }
    /**
     * Generate a new Quadtree by splitting every region whose variance is larger than {@code tol}
     * or at a maximum depth of 10. Take in mind that for this program to generate pleasing images
     * tolerance needs to be a value large enough e.g (1000 <= {@code tol} <= 1000000)
     * @param pic the source picture
     * @param tol the tolerance
     */
    public QuadTree(Picture pic, double tol) { 
        root = buildWithTol(pic, tol, root, 0, 0, pic.width(), pic.height(), 0);
    }

    public QuadTree(Picture pic, int iter) {
        buildIterative(pic, iter);
    }    
    /** Recursive procedure to generate the Quadtree. If the variance of the region is greater than
     * {@code tol} we split the region in exactly 4 subregions. Otherwise this node will be a leaf.
     * @param pic    the source picture
     * @param tol    the maximum variance accepted
     * @param h      the current node to be created
     * @param x      coordinate of this nodes rectangle
     * @param y      coordinate of this nodes rectangle
     * @param width  of this nodes rectangle
     * @param height of this nodes rectangle
     * @param depth  of the current node
     */
    private Node buildWithTol(Picture pic, double tol, Node h, int x, int y, int width, int height, int depth) {
        h = new Node(x, y, width, height,depth);
        h.computeAverageColor(pic);
        h.computeVarianceColor(pic);
        int newWidth  = width / 2;
        int newHeight = height / 2;
        int widthOffset = (width + 1) / 2;
        int heightOffset = (height + 1) / 2;
        if (h.var > tol && (width > 1 && height > 1) && depth < 10) { 
            h.nw = buildWithTol(pic, tol, h.nw, x, y, newWidth, newHeight, depth + 1);
            h.ne = buildWithTol(pic, tol, h.ne, x + newWidth, y, widthOffset, newHeight, depth + 1);
            h.sw = buildWithTol(pic, tol, h.sw, x, y + newHeight, newWidth, heightOffset, depth + 1);
            h.se = buildWithTol(pic, tol, h.se, x + newWidth, y + newHeight, widthOffset, heightOffset,depth + 1);
        }
        return  h;
    }
    // split the node with the maximum variance in the priority queue
    private void split(PriorityQueue<Node> pq, Picture pic) {
        Node cur = pq.poll();
        int newWidth  = cur.width / 2;
        int newHeight = cur.height / 2;
        int widthOffset = (cur.width + 1) / 2;
        int heightOffset = (cur.height + 1) / 2;

        cur.nw = new Node(pic, cur.x, cur.y, newWidth, newHeight, cur.level + 1);
        cur.ne = new Node(pic, cur.x + newWidth, cur.y, widthOffset, newHeight, cur.level + 1);
        cur.sw = new Node(pic, cur.x, cur.y + newHeight, newWidth, heightOffset, cur.level + 1);
        cur.se = new Node(pic, cur.x + newWidth, cur.y + newHeight, widthOffset, heightOffset,cur.level + 1);

        pq.add(cur.nw);
        pq.add(cur.ne);
        pq.add(cur.sw);
        pq.add(cur.se);
    }
    /**
     * Build a Quadtree using a max priority queue holding the leaves and using their
     * variance as score function. We do a maximum number of {@code iter} splits and generate
     * a new frame every five steps of the loop.
     * @param pic  the source image
     * @param iter the number of iterations
     */
    private void buildIterative(Picture pic, int iter) {
        Picture newPic = new Picture(pic.width(), pic.height());
        root = new Node(pic, 0, 0, pic.width(), pic.height(), 0);
        PriorityQueue<Node> pq = new PriorityQueue<Node>();
        pq.add(root);
        int frame = 0;
        for (int i = 0; i < iter; i++) {
            if (i % 5 == 0) {
                draw(newPic, "../img/out/frameno"+frame+".png");
                frame++;
            }
            split(pq,pic);
        }
        draw(newPic, "../img/out/frameno"+frame+".png");   
    }

    private void draw(Picture newPic, String name) {
        drawRecursive(root, newPic);
        newPic.save(name);
    }

    /**
     * Render the Quadtree
     */
    public void draw() {
        Picture newPic = new Picture(root.width, root.height);
        drawRecursive(root, newPic);
        newPic.show();
        newPic.save("../img/out/out.png");
    }

    /**
     * Render the frames of the Quadtree using a breadth first traversal of the tree.
     */
    public void drawFrames() {
        Picture newPic = new Picture(root.width, root.height);
        Queue<Node>  q = new LinkedList<>();
        int curlev  = -1;
        q.add(root);        
        while (!q.isEmpty()) {
            Node cur = q.remove();
            if (curlev != cur.level) {
                System.out.println(Math.sqrt(cur.level));
                newPic.save("../img/out/frameno"+cur.level+".png");
                curlev = cur.level;
            }
            draw(cur, newPic);
            if (!cur.isLeaf()) {
                q.add(cur.nw);
                q.add(cur.ne);
                q.add(cur.sw);
                q.add(cur.se);
            }
        }
    }

    private void drawRecursive(Node h, Picture pic) {
        if (h.isLeaf()) {
            for (int i = h.x; i < h.width + h.x; i++) {
                for (int j = h.y; j < h.height + h.y; j++) {
                    pic.set(i, j, h.avg);
                }
            }
            pic.drawRectangle(h.x, h.y, h.width, h.height, h.avg.darker());
        } else {
            drawRecursive(h.nw, pic);
            drawRecursive(h.ne, pic);
            drawRecursive(h.se, pic);
            drawRecursive(h.sw, pic);
        }
    }
    private void draw(Node h, Picture pic) {        
        for (int i = h.x; i < h.width + h.x; i++) {
            for (int j = h.y; j < h.height + h.y; j++) {
                pic.set(i, j, h.avg);
            }
        }
        pic.drawRectangle(h.x, h.y, h.width, h.height, h.avg.darker());
    }
    public static void main(String[] args) {
        Picture pic = new Picture(args[0]);
        int iter = Integer.parseInt(args[1]);
        QuadTree qt = new QuadTree(pic, iter);
        qt.draw();
    }
}
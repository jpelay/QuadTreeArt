
import edu.princeton.cs.algs4.Picture;

public class App {
    public static void main(String[] args) {
        Picture pic = new Picture(args[0]);
        System.out.println("Picture width " + pic.width() + " Picture height " + pic.height());
        pic.show();
    }
}

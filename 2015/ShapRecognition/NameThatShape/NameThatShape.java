import java.util.*;
import java.lang.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;

class NameThatShape {
    private static char[][] CIRCLE;
    private static char[][] SQUARE;
    private static char[][] TRIANGLE;
    private static final int WIDTH = 25;
    private static final int HEIGHT = 25;

    // Initialize pattern shapes
    static {
        CIRCLE = new char[HEIGHT][HEIGHT];
        SQUARE = new char[HEIGHT][WIDTH];
        TRIANGLE = new char[HEIGHT][WIDTH];
        drawCircle(CIRCLE);
        drawSquare(SQUARE);
        drawTriangle(TRIANGLE);
        //showImage(CIRCLE);
        //showImage(SQUARE);
        //showImage(TRIANGLE);
    }

    private static void showImage(char[][] bw) {
        int W = bw[0].length;
        int H = bw.length;
        for(int h = 0; h < H; h++) {
            for(int w = 0; w < W; w++) 
                debug("%c", bw[h][w]);
            debug("\n");
        }
    }

    private static void drawImage(BufferedImage image, char[][] bw) {
        int W = bw[0].length;
        int H = bw.length;
        Raster raster = image.getData();
        for(int h = 0; h < H; h++) {
            for(int w = 0; w < W; w++) {
                int s = raster.getSample(w, h, 0);
                bw[h][w] = s < 128 ? 'B' : 'W';
            }
        }
    }

    private static void drawCircle(char[][] bw) {
        int W = bw[0].length;
        int H = bw.length;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, W, H);
        g.setColor(Color.BLACK);
        g.drawOval(0, 0, W-1, H-1);
        g.dispose();
        drawImage(img, bw);
    }

    private static void drawSquare(char[][] bw) {
        int W = bw[0].length;
        int H = bw.length;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, W, H);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, W-1, H-1);
        g.dispose();
        drawImage(img, bw);
    }

    private static void drawTriangle(char[][] bw) {
        int W = bw[0].length;
        int H = bw.length;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, W, H);
        g.setColor(Color.BLACK);
        g.drawLine(0, H-1, W-1, H-1);
        g.drawLine(0, H-1, W/2-1, 0);
        g.drawLine(W/2, 0, W-1, H-1);
        g.dispose();
        drawImage(img, bw);
    }

    /**
     * Resize the input image to the size of pattern shapes for recognition.
     *
     * @param image the input image to resize
     * @param minX the min X bound of the shape in the input image
     * @param minY the min Y bound of the shape in the input image
     * @param maxX the max X bound of the shape in the input image
     * @param maxY the max Y bound of the shape in the input image
     */
    private char[][] resize(char[][] image, int minX, int minY, int maxX, int maxY) {
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        double yIncr = 1.0 * height / HEIGHT;
        double xIncr = 1.0 * width / WIDTH;
        showImage(image);
        //debug("src image minX=%d, minY=%d, maxX=%d, maxY=%d, width=%d, height=%d\n", minX, minY, maxX, maxY, width, height);
        char[][] bw = new char[HEIGHT][WIDTH];
        for(int y = 0; y < HEIGHT; y++) {
            int y0 = Math.min(maxY, (int)Math.floor(minY + y * yIncr));
            int y1 = Math.min(maxY, (int)Math.floor(minY + (y+1) * yIncr));
            //debug("(y0, y1)=(%d, %d), (from, to)=(%f, %f\n", y0, y1, minY + y * yIncr, minY + (y + 1) * yIncr);
            for(int x = 0; x < WIDTH; x++) {
                int x0 = Math.min(maxX, (int)Math.floor(minX + x * xIncr));
                int x1 = Math.min(maxX, (int)Math.floor(minX + (x+1) * xIncr));
                double s = 0;
                for(int j = y0; j <= y1; j++) {
                    for(int i = x0; i <= x1; i++) 
                        s += image[j][i] == 'B' ? 0 : 255;
                }
                int pixels = (x1-x0+1)*(y1-y0+1);
                bw[y][x] = s == pixels * 255 ? 'W' : 'B';
            }
        }
        debug("Resized input image\n");
        showImage(bw);
        return bw;
    }

    /**
     * The score is computed based on the similarity between the pattern shape and the resized input image.
     *
     * @param bw the resized input image
     * @param shape the pattern shape to compare
     */
    private int score(char[][] bw, char[][] shape) {
        int score = 0;
        for(int y = 0; y < HEIGHT; y++) {
            for(int x = 0; x < WIDTH; x++) 
                score += bw[y][x] == shape[y][x] ? 1 : 0;
        }       
        return score;
    }

    private Scanner mSC;
    public NameThatShape(InputStream in) {
        mSC = new Scanner(in);
    }

    public void solve() {
        int instances = mSC.nextInt();
        for(int i = 0; i < instances; i++) {
            /// read image
            int W = mSC.nextInt();
            int H = mSC.nextInt();
            char[][] image = new char[H][W];
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            for(int y = 0; y < H; y++) {
                String row = mSC.next();
                for(int x = 0; x < W; x++) {
                    image[y][x] = row.charAt(x);
                    if(image[y][x] == 'B') {
                        minX = x < minX ? x : minX;
                        minY = y < minY ? y : minY;
                        maxX = x > maxX ? x : maxX;
                        maxY = y > maxY ? y : maxY;
                    }
                }
            }
            
            // resize
            char[][] bw = resize(image, minX, minY, maxX, maxY);
            
            // recognize
            String solution = "Circle";
            int score = score(bw, CIRCLE);
            int scoreSquare = score(bw, SQUARE);
            int scoreTriangle = score(bw, TRIANGLE);
            if(scoreSquare > score) {
                solution = "Square";
                score = scoreSquare;
            }
            if(scoreTriangle > score) {
                solution = "Triangle";
                score = scoreTriangle;
            }
            System.out.println(solution);
        }
    }

    private static boolean DEBUG = Boolean.parseBoolean(System.getProperty("DEBUG", "false"));
    private static void debug(String fmt, Object... args) {
        if(DEBUG)
            System.err.printf(fmt, args);
    }

    public static void main(String[] args) {
        NameThatShape solver = new NameThatShape(System.in);
        solver.solve();
    }
}

package src.engine.core.rendering;


import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * This class is the drawing window.
 * It contains the image buffer and offers methods for drawing textured and untextured triangles.
 */
public class DrawingWindow extends JPanel {
    private GraphicsConfiguration graphicsConf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private BufferedImage imageBuffer;
    private Graphics graphics;

    int debugOffset = 500;


    public DrawingWindow(int width, int height){

        this.setSize(width,height);
        imageBuffer = graphicsConf.createCompatibleImage(
                this.getWidth(), this.getHeight());

        graphics = imageBuffer.getGraphics();

        ((Graphics2D) graphics).setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

    }

    /***
     * This method draws the image buffer after every object in this render cycle is drawn.
     *
     * The technique is called double buffering. It prevents flickering.
     */
    public void redraw() {
        this.getGraphics().drawImage(imageBuffer, 0, 0, this);
    }

    /***
     * This method draws a triangle on the image buffer.
     *
     * This method is used for untextured triangles.
     * @param triangle The projected triangle to draw.
     */


    /***
     * This method clears the image buffer before every render cycle.
     */
    public void clear(){
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }

    public void drawRow(int x){
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics.fillRect(x, 0, 1, 300);

    }

    public void drawPlayer(int x, int y, float angle) {

        float dx = Camera.getInstance().rotX;
        float dy = Camera.getInstance().rotZ;

        graphics.setColor(Color.red);
        graphics.fillOval(x + debugOffset, y, 10, 10);
        graphics.drawLine(x+debugOffset + 5,y + 5, (int) (x + debugOffset + dx + 5 ), (int) (y+dy + 5));
    }

    public void drawMap(float[][] map){

        int size = (int) RenderPipeline.gridSize;

        for(int i = 0; i< map.length; i++){
            for(int j = 0; j < map.length; j++){
                if(map[i][j] != 0)
                    graphics.setColor(Color.blue);
                else
                    graphics.setColor(Color.CYAN);
                graphics.fillRect(debugOffset + i*size + 1, j*size +1, size-1, size-1);
            }
        }
    }

    public void drawLine(float x, float y, float x2, float y2){
        graphics.setColor(Color.RED);
        graphics.drawLine((int) (x + debugOffset + 5), (int) (y + 5), (int) (x + debugOffset + 5-x2), (int) (y2 + 5));
    }




    /***
     * This method draws a textured triangle on the image buffer.
     * @param triangle The projected triangle to draw.
     * @param textureTriangle The corresponding texture triangle with uv coordinates.
     * @param texture The texture to draw.
     */
  /*  public void drawTriangle(Triangle triangle, Triangle textureTriangle, BufferedImage texture) {

        Graphics2D g2d = (Graphics2D) graphics;

        float colorDarken = Math.max(Math.min(1.0f, triangle.brightness * 1.5f), 0.2f);


        // Loop through the bounding box of the triangle and render each pixel
        int minX = (int) Math.floor(Math.min(triangle.vertices[0].x, Math.min(triangle.vertices[1].x, triangle.vertices[2].x)));
        int maxX = (int) Math.ceil(Math.max(triangle.vertices[0].x, Math.max(triangle.vertices[1].x, triangle.vertices[2].x)));
        int minY = (int) Math.floor(Math.min(triangle.vertices[0].y, Math.min(triangle.vertices[1].y, triangle.vertices[2].y)));
        int maxY = (int) Math.ceil(Math.max(triangle.vertices[0].y, Math.max(triangle.vertices[1].y, triangle.vertices[2].y)));

        for (int y = minY; y <= maxY; y+= 2) {
            for (int x = minX; x <= maxX; x+= 2) {

                Vector3 barycentric = RenderMaths.getBarycentricCoordinates(triangle.vertices[0], triangle.vertices[1], triangle.vertices[2], new Vector3(x , y , 0));

                if (barycentric.x < 0 || barycentric.y < 0 || barycentric.z < 0) {
                    continue; // Pixel is outside the triangle
                }

                // Calculate texture coordinates of the pixel
                float u = barycentric.x * textureTriangle.vertices[0].x + barycentric.y * textureTriangle.vertices[1].x + barycentric.z * textureTriangle.vertices[2].x;
                float v = barycentric.x * textureTriangle.vertices[0].y + barycentric.y * textureTriangle.vertices[1].y + barycentric.z * textureTriangle.vertices[2].y;

                Color color = new Color(texture.getRGB((int) (u * texture.getWidth()), (int) (v * texture.getHeight())));
                color = new Color((int) (color.getRed() * colorDarken), (int) (color.getGreen() * colorDarken), (int) (color.getBlue() * colorDarken));

                // Draw the pixel
                g2d.setColor(color);
                g2d.fillRect(x, y, 3, 3);
            }
        }
    }*/

}

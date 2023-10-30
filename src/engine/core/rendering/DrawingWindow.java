package src.engine.core.rendering;


import src.engine.core.matutils.RenderMaths;
import src.engine.core.matutils.Triangle;
import src.engine.core.matutils.Vector3;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * This class is the drawing window.
 * It contains the image buffer and offers methods for drawing textured and untextured triangles.
 */
public class DrawingWindow extends JPanel {
    private GraphicsConfiguration graphicsConf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private BufferedImage imageBuffer;
    private Graphics graphics;

    int debugOffset = 0;
    public int maxAccuracy;
    public int minAccuracy;


    public DrawingWindow(int width, int height, int textureMaxAccuracy, int textureMinAccuracy){

        this.setSize(width,height);
        imageBuffer = graphicsConf.createCompatibleImage(
                this.getWidth(), this.getHeight());

        graphics = imageBuffer.getGraphics();

        ((Graphics2D) graphics).setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

        this.maxAccuracy = textureMaxAccuracy;
        this.minAccuracy = textureMinAccuracy;

    }

    public void blue(){
        graphics.setColor(Color.blue);
    }

    public void setColor(int r, int g, int b){
        graphics.setColor(new Color(r,g,b));
    }

    public void drawPoint(float x, float y, float x2, float y2){
        graphics.fillOval((int) x, (int) y, (int) x2, (int) y2);
    }

    public void red(){
        graphics.setColor(Color.red);
    }
    public void green(){
        graphics.setColor(Color.green);
    }

    public void drawRect(int x, int y, int x2, int y2){
        graphics.fillRect(x,y,x2, y2);
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



    public synchronized void drawTriangle(Triangle triangle){
        graphics.setColor(triangle.color);




        graphics.fillPolygon( new Polygon(
                new int[]{(int) triangle.vertices[0].x, (int) triangle.vertices[1].x, (int) triangle.vertices[2].x},
                new int[]{(int) triangle.vertices[0].y, (int) triangle.vertices[1].y, (int) triangle.vertices[2].y},
                3
        ));
    }

    public synchronized void drawTriangle(Triangle triangle, Color color){
        graphics.setColor(color);




        graphics.fillPolygon( new Polygon(
                new int[]{(int) triangle.vertices[0].x, (int) triangle.vertices[1].x, (int) triangle.vertices[2].x},
                new int[]{(int) triangle.vertices[0].y, (int) triangle.vertices[1].y, (int) triangle.vertices[2].y},
                3
        ));
    }

    public void drawTriangleOutline(Triangle triangle){
        graphics.setColor(triangle.color);

        // set stroke
        ((Graphics2D) graphics).setStroke(new BasicStroke(2));

        graphics.drawPolygon( new Polygon(
                new int[]{(int) triangle.vertices[0].x, (int) triangle.vertices[1].x, (int) triangle.vertices[2].x},
                new int[]{(int) triangle.vertices[0].y, (int) triangle.vertices[1].y, (int) triangle.vertices[2].y},
                3
        ));
    }

    public void drawTriangleOutline(Triangle triangle, Color color, int stroke){
        graphics.setColor(color);

        // set stroke
        ((Graphics2D) graphics).setStroke(new BasicStroke(stroke));

        graphics.drawPolygon( new Polygon(
                new int[]{(int) triangle.vertices[0].x, (int) triangle.vertices[1].x, (int) triangle.vertices[2].x},
                new int[]{(int) triangle.vertices[0].y, (int) triangle.vertices[1].y, (int) triangle.vertices[2].y},
                3
        ));
    }



    /***
     * This method draws a textured triangle on the image buffer.
     * @param triangle The projected triangle to draw.
     * @param textureTriangle The corresponding texture triangle with uv coordinates.
     * @param texture The texture to draw.
     */
    public void drawTriangleOld(Triangle triangle, Triangle textureTriangle, BufferedImage texture) {

        Graphics2D g2d = (Graphics2D) graphics;

        float colorDarken = Math.max(Math.min(1.0f, triangle.brightness * 1.5f), 0.2f);


        // Loop through the bounding box of the triangle and render each pixel
        int minX = (int) Math.floor(Math.min(triangle.vertices[0].x, Math.min(triangle.vertices[1].x, triangle.vertices[2].x)));
        int maxX = (int) Math.ceil(Math.max(triangle.vertices[0].x, Math.max(triangle.vertices[1].x, triangle.vertices[2].x)));
        int minY = (int) Math.floor(Math.min(triangle.vertices[0].y, Math.min(triangle.vertices[1].y, triangle.vertices[2].y)));
        int maxY = (int) Math.ceil(Math.max(triangle.vertices[0].y, Math.max(triangle.vertices[1].y, triangle.vertices[2].y)));



        for (int y = minY; y <= maxY; y+= 1) {
            for (int x = minX; x <= maxX; x+= 1) {

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
                g2d.fillRect(x, y, 1, 1);
            }
        }
    }

    public void drawTriangleImproved(Triangle triangle, Triangle textureTriangle, BufferedImage texture) {

        System.out.println("drawTriangleImproved");
        int accuracy = maxAccuracy;

        // calculate distance from camera to triangle (midpoint of triangle)
        float distance = triangle.distance;

        System.out.println(distance);

        // the bigger the distance the less accurate the triangle is drawn
        accuracy = (int) (accuracy/(distance/10.0f));
        // accuracy must be at least 1
        accuracy = Math.max(minAccuracy, accuracy);
        accuracy = Math.min(maxAccuracy, accuracy);
       
        Graphics2D g2d = (Graphics2D) graphics;

        float colorDarken = Math.max(Math.min(1.0f, triangle.brightness * 1.5f), 0.2f);


        // Loop through the bounding box of the triangle and render each pixel
        int minX = (int) Math.floor(Math.min(triangle.vertices[0].x, Math.min(triangle.vertices[1].x, triangle.vertices[2].x)));
        int maxX = (int) Math.ceil(Math.max(triangle.vertices[0].x, Math.max(triangle.vertices[1].x, triangle.vertices[2].x)));
        int minY = (int) Math.floor(Math.min(triangle.vertices[0].y, Math.min(triangle.vertices[1].y, triangle.vertices[2].y)));
        int maxY = (int) Math.ceil(Math.max(triangle.vertices[0].y, Math.max(triangle.vertices[1].y, triangle.vertices[2].y)));

        // divide bounding box width and height / 100 and round up
       // accuracy += (int) Math.ceil(Math.max(maxX - minX, maxY - minY) / 100.0f);
        
        
        int xInkrement = Math.max(1, (maxX - minX) / accuracy) + 1;
        int yInkrement = Math.max(1, (maxY - minY) / accuracy) + 1;


         //drawTriangle(triangle, new Color(102, 84, 61, 255));

         int lastY = 0;
         int lastX = 0;

        for (int y = minY; y <= maxY; y+= yInkrement) {
            for (int x = minX; x <= maxX; x+= xInkrement) {

                Vector3 barycentric = RenderMaths.getBarycentricCoordinates(triangle.vertices[0], triangle.vertices[1], triangle.vertices[2], new Vector3(x , y , 0));

                if (barycentric.x < 0 || barycentric.y < 0 || barycentric.z < 0) {
                    continue; // Pixel is outside the triangle
                }

                lastY = y;
                lastX = x;

                // Calculate texture coordinates of the pixel
                float u = barycentric.x * textureTriangle.vertices[0].x + barycentric.y * textureTriangle.vertices[1].x + barycentric.z * textureTriangle.vertices[2].x;
                float v = barycentric.x * textureTriangle.vertices[0].y + barycentric.y * textureTriangle.vertices[1].y + barycentric.z * textureTriangle.vertices[2].y;

                Color color = new Color(texture.getRGB((int) (u * texture.getWidth()), (int) (v * texture.getHeight())));
                color = new Color((int) (color.getRed() * colorDarken), (int) (color.getGreen() * colorDarken), (int) (color.getBlue() * colorDarken));

                // Draw the pixel
                g2d.setColor(color);


                g2d.fillRect(x - xInkrement,y - yInkrement, xInkrement , yInkrement );
            }
           // g2d.fillRect(lastX, lastY, xInkrement , yInkrement );
        }
        //g2d.fillRect(lastX, lastY, xInkrement , yInkrement );




        int thickness = (int) (accuracy/10.0f);
     //  if(distance < 10)
       // drawTriangleOutline(triangle,new Color(255, 0, 0, 255), 1);
    }

    public void drawTriangleImprovedOutline(Triangle triangle, Triangle textureTriangle, BufferedImage texture) {

        System.out.println("drawTriangleImproved");
        int accuracy = maxAccuracy;

        // calculate distance from camera to triangle (midpoint of triangle)
        float distance = triangle.distance;

        System.out.println(distance);

        // the bigger the distance the less accurate the triangle is drawn
        accuracy = (int) (accuracy/(distance/10.0f));
        // accuracy must be at least 1
        accuracy = Math.max(minAccuracy, accuracy);
        accuracy = Math.min(maxAccuracy, accuracy);

        Graphics2D g2d = (Graphics2D) graphics;

        float colorDarken = Math.max(Math.min(1.0f, triangle.brightness * 1.5f), 0.2f);


        // Loop through the bounding box of the triangle and render each pixel
        int minX = (int) Math.floor(Math.min(triangle.vertices[0].x, Math.min(triangle.vertices[1].x, triangle.vertices[2].x)));
        int maxX = (int) Math.ceil(Math.max(triangle.vertices[0].x, Math.max(triangle.vertices[1].x, triangle.vertices[2].x)));
        int minY = (int) Math.floor(Math.min(triangle.vertices[0].y, Math.min(triangle.vertices[1].y, triangle.vertices[2].y)));
        int maxY = (int) Math.ceil(Math.max(triangle.vertices[0].y, Math.max(triangle.vertices[1].y, triangle.vertices[2].y)));

        // divide bounding box width and height / 100 and round up
        // accuracy += (int) Math.ceil(Math.max(maxX - minX, maxY - minY) / 100.0f);


        int xInkrement = Math.max(1, (maxX - minX) / accuracy) + 1;
        int yInkrement = Math.max(1, (maxY - minY) / accuracy) + 1;


        //drawTriangle(triangle, new Color(102, 84, 61, 255));

        int lastY = 0;
        int lastX = 0;

        for (int y = minY; y <= maxY; y+= yInkrement) {
            for (int x = minX; x <= maxX; x+= xInkrement) {

                Vector3 barycentric = RenderMaths.getBarycentricCoordinates(triangle.vertices[0], triangle.vertices[1], triangle.vertices[2], new Vector3(x , y , 0));

                if (barycentric.x < 0 || barycentric.y < 0 || barycentric.z < 0) {
                    continue; // Pixel is outside the triangle
                }

                lastY = y;
                lastX = x;

                // Calculate texture coordinates of the pixel
                float u = barycentric.x * textureTriangle.vertices[0].x + barycentric.y * textureTriangle.vertices[1].x + barycentric.z * textureTriangle.vertices[2].x;
                float v = barycentric.x * textureTriangle.vertices[0].y + barycentric.y * textureTriangle.vertices[1].y + barycentric.z * textureTriangle.vertices[2].y;

                Color color = new Color(texture.getRGB((int) (u * texture.getWidth()), (int) (v * texture.getHeight())));
                color = new Color((int) (color.getRed() * colorDarken), (int) (color.getGreen() * colorDarken), (int) (color.getBlue() * colorDarken));

                // Draw the pixel
                g2d.setColor(color);


                g2d.fillRect(x - xInkrement,y - yInkrement, xInkrement , yInkrement );
            }
            // g2d.fillRect(lastX, lastY, xInkrement , yInkrement );
        }
        //g2d.fillRect(lastX, lastY, xInkrement , yInkrement );




        int thickness = (int) (accuracy/30.0f);
        //  if(distance < 10)
        drawTriangleOutline(triangle,new Color(255, 0, 0, 255), thickness);
    }





}

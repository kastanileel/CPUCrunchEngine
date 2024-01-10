package src.engine.core.rendering;


import  src.engine.core.gamemanagement.GameComponents;
import src.engine.core.matutils.RenderMaths;
import src.engine.core.matutils.Triangle;
import src.engine.core.matutils.Vector3;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * This class is the drawing window.
 * It contains the image buffer and offers methods for drawing textured and untextured triangles.
 */
public class DrawingWindow extends JPanel {

    public enum WindowStates {
        DEATHSCREEN, PAUSESCREEN, STARTSCREEN, INGAMESCREEN
    }
    private GraphicsConfiguration graphicsConf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private BufferedImage imageBuffer;
    private Graphics graphics;
    public static GameComponents.PlayerMovement.WeaponType weaponType = GameComponents.PlayerMovement.WeaponType.MACHINE_GUN;
    public static boolean snipe = false;
    public static int playerHealth;
    public static int currentAmmo = 0;
    public int maxAccuracy;
    public int minAccuracy;
    public static int level;
    public static WindowStates windowState = WindowStates.STARTSCREEN;

    private boolean colorFade = true;
    private float colorSwitchCooldown;
    private Color arenaColor = new Color(130, 0, 255);

    public DrawingWindow(int width, int height, int textureMaxAccuracy, int textureMinAccuracy) {

        this.setSize(width, height);
        imageBuffer = graphicsConf.createCompatibleImage(
                this.getWidth(), this.getHeight());

        graphics = imageBuffer.getGraphics();

        ((Graphics2D) graphics).setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

        this.maxAccuracy = textureMaxAccuracy;
        this.minAccuracy = textureMinAccuracy;

    }

    public void drawPoint(float x, float y, float x2, float y2) {
        graphics.fillOval((int) x, (int) y, (int) x2, (int) y2);
    }


    /***
     * This method draws the image buffer after every object in this render cycle is drawn.
     *
     * The technique is called double buffering. It prevents flickering.
     */
    public void redraw() {


        // draw ui elements
        this.applyUI();
        this.getGraphics().drawImage(imageBuffer, 0, 0, this);
    }

    private void applyUI(){

        switch (windowState) {
            case DEATHSCREEN:
                graphics.setColor(new Color(0,0,0,235));
                graphics.fillRect(0,0,this.getWidth(), this.getHeight());
                graphics.setColor(Color.RED);
                Font font = new Font("Arial", Font.BOLD, (int)(this.getWidth() * 0.07));
                graphics.setFont(font);
                graphics.drawString("YOU DIED", getWidth() / 2 - (int)(this.getWidth() * 0.15), getHeight() / 2 + (int)(this.getWidth() * 0.05));
                break;
            case PAUSESCREEN:
                graphics.setColor(new Color(0, 0, 0, 128));
                graphics.fillRect(0, 0, getWidth(), getHeight());
                graphics.setColor(Color.WHITE);
                font = new Font("Arial", Font.BOLD, (int) (this.getWidth() * 0.03));
                graphics.setFont(font);
                graphics.drawString("Paused", getWidth() / 2 - (int) (this.getWidth() * 0.052), getHeight() / 2 - (int) (this.getWidth() * 0.05));
                font = new Font("Arial", Font.BOLD, (int) (this.getWidth() * 0.022));
                graphics.setFont(font);
                graphics.drawString("Hotkeys:", (int) (getWidth() * 0.05), (int) (getHeight() * 0.1));
                font = new Font("Arial", Font.PLAIN, (int) (this.getWidth() * 0.022));
                graphics.setFont(font);
                int i = 1;
                graphics.drawString("Turn off/on music - m", (int) (getWidth() * 0.05), (int) (getHeight() * 0.1 + i++ * this.getWidth() * 0.03));
                graphics.drawString("Quitgame - q", (int) (getWidth() * 0.05), (int) (getHeight() * 0.1 + i++ * this.getWidth() * 0.03));
                graphics.drawString("Resume - p", (int) (getWidth() * 0.05), (int) (getHeight() * 0.1 + i++ * this.getWidth() * 0.03));
            case INGAMESCREEN:
                graphics.setColor(Color.red);
                //draw ammo count
                font = new Font("Arial", Font.PLAIN, (int) (this.getWidth() * 0.05));
                graphics.setFont(font);
                graphics.drawString(currentAmmo + "/\u221E", (int) (this.getWidth() * 0.15), (int) (this.getHeight() * 0.855));

                graphics.setColor(Color.white);
                drawHealthBar(playerHealth);

                drawLevelCount();
                graphics.setColor(Color.white);
                graphics.setFont(font);
                graphics.drawString(Integer.toString(playerHealth), (int)(this.getWidth() * 0.055),(int)(this.getHeight() * 0.855));

                drawCrosshair();
                break;
            case STARTSCREEN:
                graphics.setColor(new Color(0, 0, 0, 128));
                graphics.fillRect(0, 0, getWidth(), getHeight());
                graphics.setColor(Color.WHITE);
                font = new Font("Arial", Font.BOLD, (int) (this.getWidth() * 0.03));
                graphics.setFont(font);
                graphics.drawString("If you skip this amazing startscreen by", getWidth() / 2 - (int) (this.getWidth() * 0.24), getHeight() / 2 - (int) (this.getHeight() * 0.05));
                graphics.drawString("pressing a key im go to fucking murder you :)", getWidth() / 2 - (int) (this.getWidth() * 0.3), getHeight() / 2 - (int) (this.getHeight() * 0.05 - this.getHeight() * 0.05));
                break;
        }
    }

    public void clear() {
        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }

    public void drawCrosshair() {
        graphics.setColor(Color.white);
        switch (weaponType) {

            case PISTOL:
                graphics.drawLine(this.getWidth() / 2 - 10, this.getHeight() / 2, this.getWidth() / 2 + 10, this.getHeight() / 2);
                graphics.drawLine(this.getWidth() / 2, this.getHeight() / 2 - 10, this.getWidth() / 2, this.getHeight() / 2 + 10);
                break;
            case SHOTGUN:
                //draw holow circle crosshair to indicate random pellet spread
                graphics.drawOval(this.getWidth() / 2 - 40, this.getHeight() / 2 - 40, 80, 80);
                break;
            case MACHINE_GUN:
                //draw hollow crosshair to indicate slight random bullet spread
                graphics.drawLine(this.getWidth() / 2 - 25, this.getHeight() / 2, this.getWidth() / 2 - 10, this.getHeight() / 2);
                graphics.drawLine(this.getWidth() / 2 + 25, this.getHeight() / 2, this.getWidth() / 2 + 10, this.getHeight() / 2);
                graphics.drawLine(this.getWidth() / 2, this.getHeight() / 2 - 25, this.getWidth() / 2, this.getHeight() / 2 - 10);
                graphics.drawLine(this.getWidth() / 2, this.getHeight() / 2 + 25, this.getWidth() / 2, this.getHeight() / 2 + 10);
                break;
            case SNIPER:
                if (snipe) {
                    //draw crosshair
                    graphics.drawLine(0, this.getHeight() / 2, this.getWidth(), this.getHeight() / 2);
                    graphics.drawLine(this.getWidth() / 2, 0, this.getWidth() / 2, this.getHeight());
                    //draw scope circle
                    graphics.drawOval(this.getWidth() / 2 - 350, this.getHeight() / 2 - 350, 700, 700);
                    //limit FOV to simulate scope
                    graphics.setColor(Color.black);
                    graphics.fillRect(0, 0, this.getWidth() / 2 - 350, this.getHeight());
                    graphics.fillRect(this.getWidth() / 2 + 350, 0, this.getWidth() / 2 - 350, this.getHeight());
                }
                break;
        }
    }

    public synchronized void drawTriangleNoLighting(Triangle triangle) {

        // based on triangle brightness darken or lighten the color
        graphics.setColor(triangle.color);


        graphics.fillPolygon(new Polygon(
                new int[]{(int) triangle.vertices[0].x, (int) triangle.vertices[1].x, (int) triangle.vertices[2].x},
                new int[]{(int) triangle.vertices[0].y, (int) triangle.vertices[1].y, (int) triangle.vertices[2].y},
                3
        ));
    }


    public synchronized void drawTriangle(Triangle triangle) {

        // based on triangle brightness darken or lighten the color
        float colorDarken = Math.max(Math.min(1.0f, triangle.brightness * 1.5f), 0.2f);

        // darken the color
        Color color = new Color((int) (triangle.color.getRed() * colorDarken), (int) (triangle.color.getGreen() * colorDarken), (int) (triangle.color.getBlue() * colorDarken));
        graphics.setColor(color);


        graphics.fillPolygon(new Polygon(
                new int[]{(int) triangle.vertices[0].x, (int) triangle.vertices[1].x, (int) triangle.vertices[2].x},
                new int[]{(int) triangle.vertices[0].y, (int) triangle.vertices[1].y, (int) triangle.vertices[2].y},
                3
        ));
    }


    public void drawTriangleOutline(Triangle triangle) {

        float colorDarken = Math.max(Math.min(1.0f, triangle.brightness * 1.5f), 0.2f);

        // darken the color
        Color color = new Color((int) (triangle.color.getRed() * colorDarken), (int) (triangle.color.getGreen() * colorDarken), (int) (triangle.color.getBlue() * colorDarken));
        graphics.setColor(color);


        // set stroke
        ((Graphics2D) graphics).setStroke(new BasicStroke(2));

        graphics.drawPolygon(new Polygon(
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
    public void drawTriangleImproved(Triangle triangle, Triangle textureTriangle, BufferedImage texture) {

        int accuracy = maxAccuracy;

        // calculate distance from camera to triangle (midpoint of triangle)
        float distance = triangle.distance;


        // the bigger the distance the less accurate the triangle is drawn
        accuracy = (int) (accuracy / (distance / 10.0f));
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

        for (int y = minY; y <= maxY; y += yInkrement) {
            for (int x = minX; x <= maxX; x += xInkrement) {

                Vector3 barycentric = RenderMaths.getBarycentricCoordinates(triangle.vertices[0], triangle.vertices[1], triangle.vertices[2], new Vector3(x, y, 0));

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


                g2d.fillRect(x - xInkrement, y - yInkrement, xInkrement, yInkrement);
            }
            // g2d.fillRect(lastX, lastY, xInkrement , yInkrement );
        }
        //g2d.fillRect(lastX, lastY, xInkrement , yInkrement );


        int thickness = (int) (accuracy / 10.0f);
        //  if(distance < 10)
        // drawTriangleOutline(triangle,new Color(255, 0, 0, 255), 1);
    }

    public void drawTriangleImprovedOutline(Triangle triangle, Triangle textureTriangle, BufferedImage texture) {

        int accuracy = maxAccuracy;

        // calculate distance from camera to triangle (midpoint of triangle)
        float distance = triangle.distance;

        // the bigger the distance the less accurate the triangle is drawn
        accuracy = (int) (accuracy / (distance / 10.0f));
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

        for (int y = minY; y <= maxY; y += yInkrement) {
            for (int x = minX; x <= maxX; x += xInkrement) {

                Vector3 barycentric = RenderMaths.getBarycentricCoordinates(triangle.vertices[0], triangle.vertices[1], triangle.vertices[2], new Vector3(x, y, 0));

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


                g2d.fillRect(x - xInkrement, y - yInkrement, xInkrement, yInkrement);
            }
            // g2d.fillRect(lastX, lastY, xInkrement , yInkrement );
        }
        //g2d.fillRect(lastX, lastY, xInkrement , yInkrement );


        int thickness = (int) (accuracy / 30.0f);
        //  if(distance < 10)
        drawTriangleOutline(triangle, new Color(255, 0, 0, 255), thickness);
    }


    private void drawTriangleOutline(Triangle triangle, Color color, int stroke) {
        float colorDarken = Math.max(Math.min(1.0f, triangle.brightness * 1.2f), 0.05f);

        graphics.setColor(new Color((int) (color.getRed() * colorDarken), (int) (color.getGreen() * colorDarken), (int) (color.getBlue() * colorDarken)));


        // set stroke
        ((Graphics2D) graphics).setStroke(new BasicStroke(stroke));



        graphics.drawPolygon(new Polygon(
                new int[]{(int) triangle.vertices[0].x, (int) triangle.vertices[1].x, (int) triangle.vertices[2].x},
                new int[]{(int) triangle.vertices[0].y, (int) triangle.vertices[1].y, (int) triangle.vertices[2].y},
                3
        ));
    }
    public void drawThickLine(int x1, int y1, int x2, int y2, int thickness) {
        // Calculate the delta values to create the thickness effect
        int deltaX = x2 - x1;
        int deltaY = y2 - y1;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        double xm = distance / thickness;
        double xn = deltaY / xm;
        double yn = deltaX / xm;

        for (int i = 0; i < thickness; i++) {
            int newX1 = (int) (x1 - xn + (i * xn / thickness));
            int newY1 = (int) (y1 + yn - (i * yn / thickness));
            int newX2 = (int) (x2 - xn + (i * xn / thickness));
            int newY2 = (int) (y2 + yn - (i * yn / thickness));

            graphics.drawLine(newX1, newY1, newX2, newY2);
        }
    }
    public void drawBox(int x, int y, int width, int height) {
        graphics.drawRect(x, y, width, height);
    }

    public void drawHealthBar(int playerHealth) {
        int boxX = (int) (this.getWidth() * 0.05);
        int boxY = (int) (this.getHeight() * 0.782);
        int boxWidth = (int) (this.getWidth() * 0.085);
        int boxHeight = (int) (this.getHeight() * 0.08);

        // Draw the health bar line
        graphics.setColor(Color.RED);
        int lineX2 = boxX + (int) (boxWidth * playerHealth / 100.0);
        drawThickLine(boxX, boxY, lineX2, boxY, boxHeight);

        // Draw the screen redness
        graphics.setColor(new Color(255,0,0,100 -  playerHealth));
        graphics.fillRect(0,0,this.getWidth(), this.getHeight());

        // Draw the box
        graphics.setColor(Color.black);
        drawBox(boxX, boxY, boxWidth, boxHeight);
    }

    public void drawLevelCount(){
        graphics.setColor(Color.white);
        graphics.setFont(new Font("Arial", Font.PLAIN, 50));
        graphics.drawString("Level: " + level, this.getWidth() - 280, this.getHeight() - 150);
    }

    public void  drawTriangleCustomArena(Triangle triangle, float deltaTime){

        //System.out.println("arenaRender");
        //float brightness = triangle.brightness;
        // i want to interpolate the color from purple to cyan over blue


        // interpolate the color
        if(colorSwitchCooldown <= 0.0f) {
            arenaColor = changeColorOneStep(arenaColor);
            colorSwitchCooldown = 30.0f;
        }
        colorSwitchCooldown -= deltaTime;
       triangle.color =  new Color(arenaColor.getRed(), arenaColor.getGreen(), arenaColor.getBlue());


        drawTriangle(triangle);
        drawTriangleOutline(triangle, triangle.color, 3);
    }

    private Color changeColorOneStep(Color color){
        int blueMax = 255;
        int blueMin = 30;
        int redMax = 220;
        int redMin = 0;

        if(colorFade){
            if(color.getRed() == redMax  && color.getBlue() < blueMax){
                // increment blue
                color = new Color(color.getRed(), color.getGreen(), color.getBlue() + 1);
            }
            else if(color.getBlue() == blueMax && color.getRed() > redMin){
                // decrement red
                color = new Color(color.getRed() - 1, color.getGreen(), color.getBlue());
            }
            else if(color.getBlue() == blueMax && color.getRed() == redMin){
                colorFade = false;
            }
        }
        else{
            if(color.getBlue() == blueMax && color.getRed() < redMax){
                // increment blue
                color = new Color(color.getRed() + 1, color.getGreen(), color.getBlue());
            }
            else if(color.getRed() == redMax && color.getBlue() > blueMin){
                // decrement red
                color = new Color(color.getRed() , color.getGreen(), color.getBlue() -1);
            }
            else if(color.getRed() == redMax && color.getBlue() == blueMin){
                colorFade = true;
            }
        }



        return color;
    }
}


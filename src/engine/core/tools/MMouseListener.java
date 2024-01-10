package src.engine.core.tools;

import src.engine.configuration.Configurator;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

public class MMouseListener extends MouseAdapter {

    // You may want to maintain a singleton instance like in MKeyListener
    private static MMouseListener instance;
    private Frame frame; // The frame to which the listener is attached

    // Store the state of mouse buttons
    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;
    private int mouseX, mouseY; // Store the current mouse coordinates
    private int lastMouseX, lastMouseY; // Store the last mouse coordinates

    int width, height, x, y, maxEdgeDistance;

    float cooldown = 0.7f;

    float lastTimeMouseMoved = 0.0f;

    public static MMouseListener getInstance( ) {
        if (instance == null) instance = new MMouseListener();
        return instance;
    }

    private MMouseListener() {
        // Private constructor for singleton pattern
        width = Integer.parseInt(Configurator.getInstance().get("windowWidth"));
        height = Integer.parseInt(Configurator.getInstance().get("windowHeight"));

        maxEdgeDistance = 70;

    }

    public void attachToFrame(Frame frame) {
        this.frame = frame;
        frame.addMouseListener(this);
        frame.addMouseMotionListener(this);
        frame.addMouseWheelListener(this);

        hideCursor();

//mouseX = frame.getX() + width/2;
        //mouseY = frame.getY() + height/2;
       // lastMouseX = mouseX;
       // lastMouseY = mouseY;

        mouseX = width;
        mouseY = height;
        lastMouseX = 0;
        lastMouseY = 0;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            leftButtonPressed = true;
        } else if(e.getButton() == MouseEvent.BUTTON3) {
            rightButtonPressed = true;
        }
        hideCursor();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            leftButtonPressed = false;
        } else if(e.getButton() == MouseEvent.BUTTON3) {
            rightButtonPressed = false;
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int rotation = e.getWheelRotation();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

        mouseX = e.getX();
        mouseY = e.getY();

        lastTimeMouseMoved = 0.0f;

        doScreenEdgeCheck(e);

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();

        lastTimeMouseMoved = 0.0f;

        doScreenEdgeCheck(e);

    }


    public void update(float deltaTime) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        x = frame.getX();
        y = frame.getY();

        cooldown -= deltaTime;

        lastTimeMouseMoved += deltaTime;

        if(lastTimeMouseMoved > 0.3f){

            mouseX = x + width/2;
            mouseY = y + height/2;
            try {
                Robot robot = new Robot();
                robot.mouseMove(mouseX, mouseY);
                lastMouseY = mouseY;
                lastMouseX = mouseX;

                hideCursor();
            } catch (AWTException awtException) {
                awtException.printStackTrace();
            }
        }
    }

    private void doScreenEdgeCheck(MouseEvent e) {
        boolean edge = false;

        if (e.getX() < maxEdgeDistance) {
            mouseX = x + width/2 ;
            edge = true;
        } else if (e.getX() > width - maxEdgeDistance) {
            mouseX = x + width/2;
            edge = true;
        }
        if (e.getY() < maxEdgeDistance) {
            mouseY = y + height/2;
            edge = true;
        } else if (e.getY() > height - maxEdgeDistance) {
            mouseY = y + height/2;
            edge = true;
        }

        if (edge) {
            try {
                Robot robot = new Robot();
                robot.mouseMove(mouseX, mouseY);
                lastMouseY = mouseY;
                lastMouseX = mouseX;
            } catch (AWTException awtException) {
                awtException.printStackTrace();
            }
        }


    }

    public void hideCursor() {
        frame.setCursor(frame.getToolkit().createCustomCursor(
               new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "null"));
    }

    // Getter methods to access the mouse state
    public boolean isLeftButtonPressed() {
       return leftButtonPressed;
    }

    public boolean isRightButtonPressed() {
        return rightButtonPressed;
    }

    public int getMouseDeltaX() {
        if (cooldown <= 0)
            return mouseX - lastMouseX;
        return 0;
    }

    public int getMouseDeltaY() {
        if (cooldown <= 0)
            return mouseY - lastMouseY;
        return 0;
    }
}

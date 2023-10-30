package src.engine.core.inputsystem;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class MMouseListener extends MouseAdapter {

    // You may want to maintain a singleton instance like in MKeyListener
    private static MMouseListener instance;

    // Store the state of mouse buttons
    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;
    private int mouseX, mouseY; // Store the current mouse coordinates
    private int lastMouseX, lastMouseY; // Store the last mouse coordinates

    public static MMouseListener getInstance() {
        if (instance == null) instance = new MMouseListener();
        return instance;
    }

    private MMouseListener() {
        // Private constructor for singleton pattern
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            leftButtonPressed = true;
        } else if(e.getButton() == MouseEvent.BUTTON3) {
            rightButtonPressed = true;
        }
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
    public void mouseMoved(MouseEvent e) {

       lastMouseX = mouseX;
         lastMouseY = mouseY;
        mouseX = e.getX();
        mouseY = e.getY();

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX() - mouseX;
        mouseY = e.getY() - mouseY;
        // You can also implement other logic for dragging behavior if needed
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int rotation = e.getWheelRotation();
        // Implement logic based on the wheel rotation (positive for scroll up, negative for scroll down)
    }

    // Getter methods to access the mouse state
    public boolean isLeftButtonPressed() {
        boolean temp = leftButtonPressed;
        leftButtonPressed = false;
        return temp;
    }

    public boolean isRightButtonPressed() {
        return rightButtonPressed;
    }

    public int getMouseX() {
        int temp = mouseX - lastMouseX;
        lastMouseX = mouseX;
       return temp;
    }

    public int getMouseY() {
        int temp = mouseY - lastMouseY;
        lastMouseY = mouseY;
        return temp;
    }
}

package src.engine.core.rendering;

import javax.swing.*;

/**
 * This class is the main frame of the application.
 * It contains the drawing window.
 */
public class Frame extends JFrame
{
    private DrawingWindow panel = null;
    public Frame(int width, int height, int textureMaxAccuracy, int textureMinAccuracy)
    {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(width, height);
        panel = new DrawingWindow(width, height, textureMaxAccuracy, textureMinAccuracy);
        this.setContentPane(panel);
    }

    public DrawingWindow getPanel() {return panel;}
}

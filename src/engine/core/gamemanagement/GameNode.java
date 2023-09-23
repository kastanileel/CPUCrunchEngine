package src.engine.core.gamemanagement;

import java.awt.*;
import java.util.LinkedList;

public class GameNode {

    private int nodeID;
    private String name;
    private LinkedList<GameNode> children;
    private GameNode parent;

    public void add(GameNode node) {
        children.add(node);
        node.setParent(this);

        nodeID = ComponentManager.retriveUniqueId(name);
    }

    public void setParent(GameNode parent){
        this.parent = parent;
    }
    public GameNode(String name){
        this.name = name;
    }

}

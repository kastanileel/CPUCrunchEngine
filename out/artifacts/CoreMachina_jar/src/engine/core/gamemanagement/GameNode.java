package src.engine.core.gamemanagement;

import java.util.LinkedList;

public class GameNode {

    private int ID;
    private String name;
    private LinkedList<GameNode> children;
    private LinkedList<Integer> componentIDs;
    private GameNode parent;

    public void add(GameNode node) {
        children.add(node);
        node.setParent(this);

    }

    public int getID(){
        return ID;
    }

    public void add(GameComponents component){
        //componentIDs.add(component.getID());
    }

    public void subscribeToSystem(){
       // gameSystem.subscribe(this);
    }

    public void setParent(GameNode parent){
        this.parent = parent;
    }
    public GameNode(String name){

        this.name = name;


    }

}

package src.engine.core.matutils;

import java.awt.*;

public class Triangle{
    public Vector3[] vertices;
    public Color color;

    public int textureIndex;

    public float brightness = 0.0f;

    public Triangle(){
        vertices = new Vector3[]{
                new Vector3(),
                new Vector3(),
                new Vector3()
        };
    }

    public Triangle(Vector3 ... vertices){
        this.vertices = vertices;
    }

    public Triangle(Color color, float brightness, int textureIndex, Vector3 ... vertices){
        this.vertices = vertices;
        this.color = color;
        this.brightness = brightness;
        this.textureIndex = textureIndex;
    }



    public float midPointVal(){
        return (vertices[0].z + vertices[1].z + vertices[2].z)/3;
    }

    @Override
    public Triangle clone() {
        return new Triangle(
                color,
                brightness,
                textureIndex,
                vertices[0].clone(),
                vertices[1].clone(),
                vertices[2].clone());
    }
}

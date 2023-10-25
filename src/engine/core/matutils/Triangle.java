package src.engine.core.matutils;

import java.awt.*;

public class Triangle{
    public Vector3[] vertices;

    public Vector3 normal;
    public Color color;

    public int textureIndex;

    public int meshIndex = -1;

    public float brightness = 0.0f;

    public float distance = 0.0f;

    public int ide = -1;

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

    public Triangle(Color color, float brightness, int textureIndex, int ide, Vector3 ... vertices){
        this.vertices = vertices;
        this.color = color;
        this.brightness = brightness;
        this.textureIndex = textureIndex;
        this.ide = ide;
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
                ide,
                vertices[0].clone(),
                vertices[1].clone(),
                vertices[2].clone());
    }
}

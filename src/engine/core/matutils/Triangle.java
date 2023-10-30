package src.engine.core.matutils;

import src.engine.core.gamemanagement.GameComponents;

import java.awt.*;

public class Triangle{
    // contains 3 vertices => 1 triangle has 3 edges
    public Vector3[] vertices;

    // color of the triangle if no texture is applied
    public Color color;

    // used for storing the normal in between render pipeline stages
    public Vector3 normal;

    // index of the texture in the texture array in the render pipeline
    public int textureIndex;

    // index of the mesh in the mesh array in the render pipeline
    public int meshIndex = -1;

    public float brightness = 0.0f;

    // distance from the camera used for scaling the texture accuracy (further away => less accurate)
    public float distance = 0.0f;

    // the triangle will be rendered differently depending on the renderType
    public GameComponents.Rendering.RenderType renderType;

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

    public Triangle(Color color, float brightness, int textureIndex, GameComponents.Rendering.RenderType renderType, Vector3 ... vertices){
        this.vertices = vertices;
        this.color = color;
        this.brightness = brightness;
        this.textureIndex = textureIndex;
        this.renderType = renderType;
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
                renderType,
                vertices[0].clone(),
                vertices[1].clone(),
                vertices[2].clone());
    }
}

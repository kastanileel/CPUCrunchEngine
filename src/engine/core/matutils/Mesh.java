package src.engine.core.matutils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

public class Mesh {
    public Triangle[] triangles;
    public Triangle[] textureTriangles;
    public BufferedImage texture;


    public Mesh(String path) throws IOException {
        // Read a obj file and create a mesh from it
        BufferedReader reader = new BufferedReader (new FileReader(path));
        LinkedList<Vector3> vertices = new LinkedList<Vector3>();
        LinkedList<Triangle> triangles = new LinkedList<Triangle>();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            if (tokens[0].equals("v")) {
                vertices.add(new Vector3(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            } else if (tokens[0].equals("f")) {

                triangles.add(new Triangle(vertices.get(Integer.parseInt(tokens[1]) - 1), vertices.get(Integer.parseInt(tokens[2]) - 1), vertices.get(Integer.parseInt(tokens[3]) - 1)));
            }
        }
        this.triangles = triangles.toArray(new Triangle[0]);
    }

    public Mesh(String path, Color c) throws IOException {
        // Read a obj file and create a mesh from it
        BufferedReader reader = new BufferedReader (new FileReader(path));
        LinkedList<Vector3> vertices = new LinkedList<Vector3>();
        LinkedList<Triangle> triangles = new LinkedList<Triangle>();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            if (tokens[0].equals("v")) {
                vertices.add(new Vector3(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            } else if (tokens[0].equals("f")) {
                Triangle t = new Triangle(vertices.get(Integer.parseInt(tokens[1]) - 1), vertices.get(Integer.parseInt(tokens[2]) - 1), vertices.get(Integer.parseInt(tokens[3]) - 1));
                t.color = c;
                triangles.add(t);
            }
        }
        this.triangles = triangles.toArray(new Triangle[0]);
    }

    public Mesh(String objPath, String texturePath) throws IOException {

        try {
            texture = ImageIO.read(new File(texturePath));
        } catch (Exception e) {
            e.printStackTrace();
        }

        BufferedReader reader = new BufferedReader (new FileReader(objPath));
        LinkedList<Vector3> vertices = new LinkedList<Vector3>();
        LinkedList<Triangle> triangles = new LinkedList<Triangle>();
        LinkedList<Vector3> texturePoint = new LinkedList<Vector3>();
        LinkedList<Triangle> textureTriangles = new LinkedList<Triangle>();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            if (tokens[0].equals("v")) {
                vertices.add(new Vector3(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            }
            else if(tokens[0].equals("vt")){
                texturePoint.add(new Vector3(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), 0));
            }
            else if (tokens[0].equals("f")) {
                System.out.println(Arrays.toString(tokens));
                    // if polygon is a triangle
                if(tokens.length == 4) {
                    triangles.add(new Triangle(vertices.get(Integer.parseInt(tokens[1].split("/")[0]) - 1), vertices.get(Integer.parseInt(tokens[2].split("/")[0]) - 1), vertices.get(Integer.parseInt(tokens[3].split("/")[0]) - 1)));
                    textureTriangles.add(new Triangle(texturePoint.get(Integer.parseInt(tokens[1].split("/")[1]) - 1), texturePoint.get(Integer.parseInt(tokens[2].split("/")[1]) - 1), texturePoint.get(Integer.parseInt(tokens[3].split("/")[1]) - 1)));
                }
                //if polygon has 4 edges
                if(tokens.length == 5){
                    triangles.add(new Triangle(vertices.get(Integer.parseInt(tokens[1].split("/")[0]) - 1), vertices.get(Integer.parseInt(tokens[2].split("/")[0]) - 1), vertices.get(Integer.parseInt(tokens[3].split("/")[0]) - 1)));
                    triangles.add(new Triangle(vertices.get(Integer.parseInt(tokens[1].split("/")[0]) - 1), vertices.get(Integer.parseInt(tokens[3].split("/")[0]) - 1), vertices.get(Integer.parseInt(tokens[4].split("/")[0]) - 1)));
                    textureTriangles.add(new Triangle(texturePoint.get(Integer.parseInt(tokens[1].split("/")[1]) - 1), texturePoint.get(Integer.parseInt(tokens[2].split("/")[1]) - 1), texturePoint.get(Integer.parseInt(tokens[3].split("/")[1]) - 1)));
                    textureTriangles.add(new Triangle(texturePoint.get(Integer.parseInt(tokens[1].split("/")[1]) - 1), texturePoint.get(Integer.parseInt(tokens[3].split("/")[1]) - 1), texturePoint.get(Integer.parseInt(tokens[4].split("/")[1]) - 1)));
                }
            }
        }
        this.textureTriangles = textureTriangles.toArray(new Triangle[0]);
        this.triangles = triangles.toArray(new Triangle[0]);
    }

    public static int[] intArrayFromString(String str){
        str += " ";
        return Arrays.stream(str.substring(2, str.length()-1).split(" "))
                .map(String::trim).mapToInt(Integer::parseInt).toArray();
    }

    public static Iterable<Float> floatArrayFromString(String str){
        str += " ";
        LinkedList<Float> erg = new LinkedList<>();
        Arrays.stream(str.substring(2, str.length()-1).split(" "))
                .map(String::trim).map(Float::parseFloat).forEach(erg::add);
        return erg;
    }


}

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
        int j = 0;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            if(tokens.length == 1)
                continue;
            if(tokens[1].contains("//")){
                for(int i = 0; i < tokens.length; i++){
                    //if contains //
                    if(tokens[i].contains("//")){
                        // remove everything starting from //
                        System.out.println(tokens[i]);
                        tokens[i] = tokens[i].substring(0, tokens[i].indexOf("/"));
                        System.out.println(tokens[i]);
                    }
                }
            }
            if (tokens[0].equals("v")) {
                vertices.add(new Vector3(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            } else if (tokens[0].equals("f")) {
                System.out.println(j);
                triangles.add(new Triangle(vertices.get(Integer.parseInt(tokens[1]) - 1), vertices.get(Integer.parseInt(tokens[2]) - 1), vertices.get(Integer.parseInt(tokens[3]) - 1)));
            }
            j+= 1;
        }
        this.triangles = triangles.toArray(new Triangle[0]);
    }

  /*  public Mesh(String path, Color c) throws IOException {
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
    }*/

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


    // parsing only the mesh, obj has one color set in the constructor
    public Mesh(String path, Color objColor) throws IOException {
        // create Reader Object
        BufferedReader reader = new BufferedReader (new FileReader(path));

        // create vertices and triangles to fill
        LinkedList<Vector3> vertices = new LinkedList<Vector3>();
        LinkedList<Vector3> normals = new LinkedList<Vector3>();
        LinkedList<Triangle> triangles = new LinkedList<Triangle>();

        String line;
        while ((line = reader.readLine()) != null) {

            String[] tokens = line.split(" ");

            // because we dont want textures remove them

            if (tokens[0].equals("v")) {
                vertices.add(new Vector3(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            }
            else if (tokens[0].equals("vn")){
                normals.add(new Vector3(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            }
            else if (tokens[0].equals("f")) {
                boolean normal = false;
                // filter out exture coordinates
                for(int i = 0; i < tokens.length; i++){
                    //if contains //
                    String[] subParts = tokens[i].split("/");
                    if(subParts.length == 3){
                        normal = true;
                    }
                    if(subParts.length == 2 && subParts[1].isEmpty()){
                        normal = true;
                    }
                    if(normal)
                        tokens[i]  =  subParts[0] + "//" + (subParts.length == 3 ? subParts[2] : subParts[1]);
                    else
                        tokens[i] = subParts[0];

                }

                if(tokens.length == 4){
                    triangles.add(new Triangle(vertices.get(Integer.parseInt(tokens[1].split("//")[0]) - 1), vertices.get(Integer.parseInt(tokens[2].split("//")[0]) - 1), vertices.get(Integer.parseInt(tokens[3].split("//")[0]) - 1)));
                    if(normal)
                        triangles.getLast().normal = normals.get(Integer.parseInt(tokens[1].split("//")[2]) - 1);

                    // set color
                    triangles.getLast().color = objColor;
                }
                if(tokens.length == 5){
                    triangles.add(new Triangle(vertices.get(Integer.parseInt(tokens[1].split("//")[0]) - 1), vertices.get(Integer.parseInt(tokens[2].split("//")[0]) - 1), vertices.get(Integer.parseInt(tokens[3].split("//")[0]) - 1)));
                    triangles.add(new Triangle(vertices.get(Integer.parseInt(tokens[1].split("//")[0]) - 1), vertices.get(Integer.parseInt(tokens[3].split("//")[0]) - 1), vertices.get(Integer.parseInt(tokens[4].split("//")[0]) - 1)));
                    if(normal){
                        triangles.get(triangles.size() - 2).normal = normals.get(Integer.parseInt(tokens[1].split("//")[1]) - 1);
                        triangles.getLast().normal = normals.get(Integer.parseInt(tokens[1].split("//")[1]) - 1);
                    }

                    // set color
                    triangles.get(triangles.size() - 2).color = objColor;
                    triangles.getLast().color = objColor;
                }
            }
        }
        this.triangles = triangles.toArray(new Triangle[0]);
    }

    // parsing the mesh, adding the average color of the texture to each triangle
    public Mesh(String path, String texturePath, boolean optimized) throws IOException{

        try {
            texture = ImageIO.read(new File(texturePath));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // create Reader Object
        BufferedReader reader = new BufferedReader (new FileReader(path));

        // create vertices and triangles to fill
        LinkedList<Vector3> vertices = new LinkedList<Vector3>();
        LinkedList<Vector3> normals = new LinkedList<Vector3>();
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
            else if (tokens[0].equals("vn")){
                normals.add(new Vector3(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            }

            else if (tokens[0].equals("f")) {
                boolean normal = false;
                for(int i = 0; i < tokens.length; i++){
                    //if contains //
                    String[] subParts = tokens[i].split("/");
                    if(subParts.length == 3){
                        normal = true;
                    }
                    if(subParts.length == 2 && subParts[1].isEmpty()){
                        normal = true;
                    }
                }

                if(tokens.length == 4) {
                    triangles.add(new Triangle(vertices.get(Integer.parseInt(tokens[1].split("/")[0]) - 1), vertices.get(Integer.parseInt(tokens[2].split("/")[0]) - 1), vertices.get(Integer.parseInt(tokens[3].split("/")[0]) - 1)));
                    //textureTriangles.add(new Triangle(texturePoint.get(Integer.parseInt(tokens[1].split("/")[1]) - 1), texturePoint.get(Integer.parseInt(tokens[2].split("/")[1]) - 1), texturePoint.get(Integer.parseInt(tokens[3].split("/")[1]) - 1)));
                    // based on texture point iterate through texture and calculate average color

                    // calculate middle point of the texture Points
                    Vector3 t1 = texturePoint.get(Integer.parseInt(tokens[1].split("/")[1]) - 1).clone();
                    Vector3 t2 = texturePoint.get(Integer.parseInt(tokens[2].split("/")[1]) - 1).clone();
                    Vector3 t3 = texturePoint.get(Integer.parseInt(tokens[3].split("/")[1]) - 1).clone();

                    Vector3 middle = new Vector3((t1.x + t2.x + t3.x )/3, (t1.y + t2.y + t3.y )/3, 0);

                    Color c = new Color(texture.getRGB((int) (middle.x * texture.getWidth()), (int) (middle.y * texture.getHeight())));

                    triangles.getLast().color = c;

                    if(normal){
                        triangles.getLast().normal = normals.get(Integer.parseInt(tokens[1].split("/")[2]) - 1);
                    }



                }

                if(tokens.length == 5){
                    triangles.add(new Triangle(vertices.get(Integer.parseInt(tokens[1].split("/")[0]) - 1), vertices.get(Integer.parseInt(tokens[2].split("/")[0]) - 1), vertices.get(Integer.parseInt(tokens[3].split("/")[0]) - 1)));
                    triangles.add(new Triangle(vertices.get(Integer.parseInt(tokens[1].split("/")[0]) - 1), vertices.get(Integer.parseInt(tokens[3].split("/")[0]) - 1), vertices.get(Integer.parseInt(tokens[4].split("/")[0]) - 1)));

                    // calculate middle point of the texture Points
                    Vector3 t1 = texturePoint.get(Integer.parseInt(tokens[1].split("/")[1]) - 1).clone();
                    Vector3 t2 = texturePoint.get(Integer.parseInt(tokens[2].split("/")[1]) - 1).clone();
                    Vector3 t3 = texturePoint.get(Integer.parseInt(tokens[3].split("/")[1]) - 1).clone();
                    Vector3 t4 = texturePoint.get(Integer.parseInt(tokens[4].split("/")[1]) - 1).clone();

                    Vector3 middle = new Vector3((t1.x + t2.x + t3.x + t4.x)/4, (t1.y + t2.y + t3.y + t4.y)/4, 0);

                    Color c = new Color(texture.getRGB((int) (middle.x * texture.getWidth()), (int) (middle.y * texture.getHeight())));

                    triangles.getLast().color = c;
                    triangles.get(triangles.size() - 2).color =c;
                    if (normal) {
                        triangles.get(triangles.size() - 2).normal = normals.get(Integer.parseInt(tokens[1].split("/")[2]) - 1);
                        triangles.getLast().normal = normals.get(Integer.parseInt(tokens[1].split("/")[2]) - 1);
                    }



                }




            }



        }

        // iterate over triangles
        for(Triangle t: triangles){
            t.ide = 1;
        }


        this.triangles = triangles.toArray(new Triangle[0]);
    }


}

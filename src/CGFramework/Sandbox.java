package CGFramework;

/* 
 * Cologne University of Applied Sciences
 * Institute for Media and Imaging Technologies - Computer Graphics Group
 *
 * Copyright (c) 2014 Cologne University of Applied Sciences. All rights reserved.
 *
 * This source code is property of the Cologne University of Applied Sciences. Any redistribution
 * and use in source and binary forms, with or without modification, requires explicit permission. 
 */

import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.util.ArrayList;

import math.Mat4;
import math.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import util.*;

public class Sandbox {
    private final ShaderProgram shaderProgram;
    private Mat4 modelMatrix, viewMatrix, rotationX, rotationY, projectionMatrix;
    private int windowWidth, windowHeight;
    private float deltaX, deltaY;
    private float rotationScale = 0.01f;
    public static float fov = 60.0f;
    private float near = 0.01f;
    private float far = 500.0f;
    private float cameraSpeed;

    /**
     * **** Variablen fuer Praktikumsaufgaben *****
     */
    boolean enableSpecular;
    private Light light;
    private ArrayList<Model> modelList;
    private ArrayList<Planet> planetList;
    private final Mat4 einheitsMatrix;
    private boolean activateOrtho = false;
    private float globalSpeed = 1.5f;


    /**
     * ***********************
     */
    private boolean vSync = true;

    /**
     * @param width  The horizontal window size in pixels
     * @param height The vertical window size in pixels
     */
    public Sandbox(int width, int height) {
        windowWidth = width;
        windowHeight = height;
        // The shader program source files must be put into the same package as the Sandbox class file. This simplifies the 
        // handling in the lab exercise (i.e. for when uploading to Ilias or when correcting) since all code of one student
        // is kept in one package. In productive code the shaders would be put into the 'resource' directory.
        shaderProgram = new ShaderProgram(getPathForPackage() + "Color_vs.glsl", getPathForPackage() + "Color_fs.glsl");

        einheitsMatrix = new Mat4();
        modelMatrix = new Mat4();
        viewMatrix = Mat4.translation(0.0f, 0.0f, -3.0f);

        modelList = new ArrayList<>();
        planetList = new ArrayList<>();

        initLights();
        createMeshes();

        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
    }

    /**
     * @return The path to directory where the source file of this class is
     * located.
     */
    private String getPathForPackage() {
        String locationOfSources = "src";
        String packageName = this.getClass().getPackage().getName();
        String path = locationOfSources + File.separator + packageName.replace(".", File.separator) + File.separator;
        return path;
    }

    /**
     * @param deltaTime The time in seconds between the last two frames
     */
    public void update(float deltaTime) {
        cameraSpeed = 5.0f * deltaTime;
        inputListener();
    }

    public void draw() {   // runs after update         
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0f, 0f, 0, 1); //hintergrund schwarz

        if (activateOrtho) {
            projectionMatrix = Mat4.orthographic(-3f, 3f, 1.7f, -1.7f, near, far);
        } else {
            projectionMatrix = Mat4.perspective(fov, windowWidth, windowHeight, near, far);
        }

        glViewport(0, 0, windowWidth, windowHeight);

        this.drawMeshes(viewMatrix, projectionMatrix);
    }

    public void drawMeshes(Mat4 viewMatrix, Mat4 projMatrix) {  //runs in draw()      
        shaderProgram.useProgram();
        shaderProgram.setUniform("uView", viewMatrix);
        shaderProgram.setUniform("uProjection", projMatrix);

        shaderProgram.setUniform("lightPosition", light.getPosition());
        shaderProgram.setUniform("lightColor", light.getColor());

        if (enableSpecular) shaderProgram.setUniform("enableSpecular", 1);
        else shaderProgram.setUniform("enableSpecular", 0);

        glCullFace(GL_BACK);

        for(int i=0 ; i<planetList.size() ; i++) {
            Planet planet = planetList.get(i);
            planetCounterHandler(planet);
        }

        for(int i=0 ; i<planetList.size() ; i++) {
            Planet planet = planetList.get(i);
            if(planet.getName()=="SolarSun") shaderProgram.setUniform("renderSolarSun", 1);
            else shaderProgram.setUniform("renderSolarSun", 0);
            shaderProgram.setUniform("shininess", planet.getShininess());
            shaderProgram.setUniform("reflectivity", planet.getReflectivity());
            shaderProgram.setUniform("uModel", Transformation.createTransMat(modelMatrix, planet.getPosition(), planet.getSize()));
            shaderProgram.setUniform("modelColor", planet.getColor());
            planet.getMesh().draw();
        }
    }

    private void planetCounterHandler(Planet planet) {
        if(!planet.getName().equals("SolarSun")) {
            planet.setPlanetCounter(planet.getPlanetCounter() + 2);
            if (planet.getPlanetCounter() == planet.getTravelRoute().length - 2) planet.setPlanetCounter(0);
        }

        if (planet.isTraveling()) {
            Planet linkedPlanet = planet.getLinkedPlanet();
            if(linkedPlanet.getName().equals("SolarSun")) {
                if (!planet.isTravelingHorizontal()) {
                    planet.setPosition(new Vec3(
                            0,
                            planet.getTravelRoute()[planet.getPlanetCounter()+1],
                            planet.getTravelRoute()[planet.getPlanetCounter()]   ));

                } else {
                    planet.setPosition(new Vec3(
                            planet.getTravelRoute()[planet.getPlanetCounter()],
                            planet.getTravelRoute()[planet.getPlanetCounter()+1],
                            0));

                }
            } else {
                if (!planet.isTravelingHorizontal()) {
                    planet.setPosition(new Vec3(
                            linkedPlanet.getTravelRoute()[linkedPlanet.getPlanetCounter()],
                            linkedPlanet.getTravelRoute()[linkedPlanet.getPlanetCounter()+1]+planet.getTravelRoute()[planet.getPlanetCounter()+1],
                            planet.getTravelRoute()[planet.getPlanetCounter()]   ));


                } else {
                    planet.setPosition(new Vec3(
                            planet.getTravelRoute()[planet.getPlanetCounter()]+linkedPlanet.getTravelRoute()[linkedPlanet.getPlanetCounter()],
                            linkedPlanet.getTravelRoute()[linkedPlanet.getPlanetCounter() + 1],
                            planet.getTravelRoute()[planet.getPlanetCounter()+1]));

                }
            }

        }
    }

    private void createMeshes() {
        //shininess metal ca.10-20
        planetList.add(new Planet("SolarSun",Sphere.createMesh    (0.3f, 50, 50) , new Vec3(),    1f, Color.yellow()   , 100f, 0.5f,null));
        planetList.add(new Planet("Earth",Sphere.createMesh       (0.2f, 35, 35) , new Vec3(), 0.45f, Color.lightBlue(), 100f, 0.5f, findPlanet("SolarSun"),1f,0.008f*globalSpeed,true));
        planetList.add(new Planet("Earth-Moon",Sphere.createMesh  (0.2f, 35, 35) , new Vec3(), 0.13f, Color.grey()     , 100f, 0.5f, findPlanet("Earth"),0.14f,0.08f*globalSpeed,false));
        planetList.add(new Planet("Mars",Sphere.createMesh        (0.2f, 35, 35) , new Vec3(), 0.35f, Color.orange()   , 100f, 0.5f, findPlanet("SolarSun"),1.25f,0.012f*globalSpeed,true));
        planetList.add(new Planet("Deimos",Sphere.createMesh      (0.2f, 35, 35) , new Vec3(), 0.05f, Color.grey()     , 100f, 0.5f, findPlanet("Mars"),0.1f,0.048f*globalSpeed,false));
        planetList.add(new Planet("Phobos",Sphere.createMesh      (0.2f, 35, 35) , new Vec3(),  0.1f, Color.grey()     , 100f, 0.5f, findPlanet("Mars"),0.15f,0.032f*globalSpeed,true));
    }

    private void initLights() {
        light = new Light(new Vec3(0,0,0), new Vec3(1f, 1f, 1f));
    }

    private Planet findPlanet(String planetName) {
        for( Planet p : planetList) {
            if(p.getName().equals(planetName)) return p;
        }
        return null;
    }

    private void inputListener() {
        if (Mouse.isButtonDown(1)) {
            deltaX = (float) Mouse.getDX();
            deltaY = (float) Mouse.getDY();
            rotationX = Mat4.rotation(Vec3.yAxis(), deltaX * rotationScale);
            rotationY = Mat4.rotation(Vec3.xAxis(), -deltaY * rotationScale);
            viewMatrix = rotationY.mul(rotationX).mul(viewMatrix);
        }

        if (Mouse.isButtonDown(0)) {
            deltaX = (float) Mouse.getDX();
            deltaY = (float) Mouse.getDY();
            rotationX = Mat4.rotation(Vec3.yAxis(), deltaX * rotationScale);
            rotationY = Mat4.rotation(Vec3.xAxis(), -deltaY * rotationScale);
            modelMatrix = rotationY.mul(rotationX).mul(modelMatrix);
        }

        while (Keyboard.next()) { // recognizes just one press, holding button still results in one press
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.isKeyDown(Keyboard.KEY_V)) {
                    vSync = !vSync;
                    Display.setVSyncEnabled(vSync);
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
                    activateOrtho = !activateOrtho;
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_1)) {
                    enableSpecular = !enableSpecular;
                }
            }
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
            fov += 0.4f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
            fov -= 0.4f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                viewMatrix.mul(Mat4.translation(0.0f, 0.0f, cameraSpeed * 10));
            } else viewMatrix.mul(Mat4.translation(0.0f, 0.0f, cameraSpeed));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                viewMatrix.mul(Mat4.translation(0.0f, 0.0f, -cameraSpeed * 10));
            } else viewMatrix.mul(Mat4.translation(0.0f, 0.0f, -cameraSpeed));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            viewMatrix.mul(Mat4.translation(cameraSpeed, 0.0f, 0.0f));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            viewMatrix.mul(Mat4.translation(-cameraSpeed, 0.0f, -0.0f));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            viewMatrix.mul(Mat4.translation(0.0f, -cameraSpeed, 0.0f));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
            viewMatrix.mul(Mat4.translation(0.0f, +cameraSpeed, 0.0f));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            Main.exit();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
            Main.toggleFullscreen();
        }
    }


    public void onResize(int width, int height) {
        windowWidth = width;
        windowHeight = height;
    }



    public static float[] generateVertexNormals(float[] positions, int[] indices) {
        Face[] faces;
        Vec3[] vertexNormalsVectors = new Vec3[positions.length / 3];
        float[] vertexNormals;

        for (int i = 0; i < vertexNormalsVectors.length; i++) {
            vertexNormalsVectors[i] = new Vec3();
        }

        faces = new Face[indices.length / 3];

        System.out.println("faces size : " + faces.length);
        System.out.println("indices size : " + indices.length);
        System.out.println("positions size : " + positions.length);

        for (int i = 0; i < indices.length; i += 3) {
            faces[i / 3] = new Face(new Vec3(positions[indices[i] * 3], positions[(indices[i] * 3) + 1], positions[(indices[i] * 3) + 2]),
                    new Vec3(positions[indices[i + 1] * 3], positions[(indices[i + 1] * 3) + 1], positions[(indices[i + 1] * 3) + 2]),
                    new Vec3(positions[indices[i + 2] * 3], positions[(indices[i + 2] * 3) + 1], positions[(indices[i + 2] * 3) + 2]));

            faces[i / 3].addToIndicesList(indices[i], indices[i + 1], indices[i + 2]);
        }

        for (int i = 0; i < faces.length; i++) {
            for (int j = 0; j < 3; j++) {
                vertexNormalsVectors[faces[i].getIndicesList().get(j)].add(faces[i].getNormal());
                vertexNormalsVectors[faces[i].getIndicesList().get(j)] = vertexNormalsVectors[faces[i].getIndicesList().get(j)].normalize();
            }
        }

        vertexNormals = new float[vertexNormalsVectors.length * 3];

        for (int i = 0; i < vertexNormalsVectors.length; i++) {
            vertexNormals[i * 3] = vertexNormalsVectors[i].x;
            vertexNormals[(i * 3) + 1] = vertexNormalsVectors[i].y;
            vertexNormals[(i * 3) + 2] = vertexNormalsVectors[i].z;
        }

        return vertexNormals;
    }
}

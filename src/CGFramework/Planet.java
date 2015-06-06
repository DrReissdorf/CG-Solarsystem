package CGFramework;

import math.Vec3;
import util.Mesh;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by S on 04.06.2015.
 */
public class Planet extends Model {
    private Planet linkedPlanet;
    private int planetCounter = 0;
    private float[] travelRoute;
    private float radius;
    private float movingSpeed;
    private boolean isTraveling;
    private boolean isTravelingHorizontal;

    public Planet(String name, Mesh mesh, Vec3 position, float size, Vec3 color, float shininess, float reflectivity, Planet linkedPlanet, float radius, float movingSpeed, boolean isTravelingHorizontal) {
        super(name, mesh, position, size, color, shininess, reflectivity);
        this.linkedPlanet = linkedPlanet;
        this.radius = radius;
        this.movingSpeed = movingSpeed;
        this.isTraveling = true;
        this.travelRoute = createPlanetPosArray(radius,movingSpeed);
        this.isTravelingHorizontal = isTravelingHorizontal;
    }

    public Planet(String name, Mesh mesh, Vec3 position, float size, Vec3 color, float shininess, float reflectivity, Planet linkedPlanet) {
        super(name, mesh, position, size, color, shininess, reflectivity);
        this.linkedPlanet = linkedPlanet;
        this.isTraveling = false;
    }

    /**
     * @param radius      radius of the circle
     * @param movingSpeed speed of object
     * @return array that contains planet positions
     */
    private float[] createPlanetPosArray(float radius, float movingSpeed) {
        float x = 0;
        float[] positions = new float[(int) (((Math.PI * 2) / movingSpeed) * 2) + 1];
        if (positions.length % 2 != 0) positions = new float[(int) (((Math.PI * 2) / movingSpeed) * 2) + 2];

        for (int i = 0; i < positions.length; i += 2) {
            positions[i] = 0 + (float) sin(x) * radius;
            positions[i + 1] = 0 + (float) cos(x) * radius;
            x += movingSpeed;
        }

        return positions;
    }

    public Planet getLinkedPlanet() {
        return linkedPlanet;
    }

    public void setLinkedPlanet(Planet linkedPlanet) {
        this.linkedPlanet = linkedPlanet;
    }

    public int getPlanetCounter() {
        return planetCounter;
    }

    public void setPlanetCounter(int planetCounter) {
        this.planetCounter = planetCounter;
    }

    public float[] getTravelRoute() {
        return travelRoute;
    }

    public void setTravelRoute(float[] travelRoute) {
        this.travelRoute = travelRoute;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getMovingSpeed() {
        return movingSpeed;
    }

    public void setMovingSpeed(float movingSpeed) {
        this.movingSpeed = movingSpeed;
    }

    public boolean isTraveling() {
        return isTraveling;
    }

    public void setIsTraveling(boolean isTraveling) {
        this.isTraveling = isTraveling;
    }

    public boolean isTravelingHorizontal() {
        return isTravelingHorizontal;
    }

    public void setTravelingHorizontal(boolean travelingHorizontal) {
        this.isTravelingHorizontal = travelingHorizontal;
    }
}

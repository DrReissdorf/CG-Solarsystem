#version 150

in vec3 N;  //normalvector
in vec3 L;    //L=vector to light
in vec3 V; //V=vector to Camera
in vec3 distToLightVector;

out vec4 FragColor;

uniform vec3 lightPosition;
uniform vec3 modelColor;
uniform vec3 lightColor; //color, that represents the specular reflection of the light
uniform float shininess;
uniform float reflectivity; //how much the model is gonna reflect
uniform int enableSpecular; //if this is true, specular reflection is enabled
uniform int renderSolarSun;

void main(void) {
      vec3 unitNormal = normalize(N);
      vec3 unitVectorToCamera = normalize(V);

      float distanceToLight = sqrt(dot(L,L));

      /* ATTENUATION OF LIGHT SOURCE (NOT SUN) */
      float lightEndDist = 60;
      float lightStartDist = 0;
      float lightIntense;
      if(distanceToLight <= lightStartDist) lightIntense = 1;  //max helligkeit
      else if(distanceToLight >= lightEndDist) lightIntense = 0;
      else lightIntense = (lightEndDist-distanceToLight)/(lightEndDist-lightStartDist);
      /********************/

      float normalDotlight = max(dot(unitNormal, normalize(L)),0.2);
      /*  DIFFUSE LIGHTING LIGHT*/
      vec3 diffuseLighting = normalDotlight * lightColor * lightIntense;
      /********************/

      float specularFactorLight;
      if(enableSpecular == 1) {
        /* SPECULAR LIGHTING (BLINN-PHONG) */
        float specularIntensityLight=0.0;
        if(normalDotlight > 0.2) { // check if we are on the back of the model (where no reflection should be)
            vec3 lightAddCam = L+V;
            vec3 H = lightAddCam/sqrt(dot(lightAddCam,lightAddCam));
            specularIntensityLight = reflectivity * 1 * pow( dot(unitNormal,normalize(H)),shininess);
        }
        specularFactorLight = max(specularIntensityLight,0.0);

      } else {
            specularFactorLight = 0.0;
      }
      /*******************************/

        vec4 diffAndSpecLight;
        if(renderSolarSun==1) {
            diffAndSpecLight = vec4(diffuseLighting*modelColor, 1.0) + vec4(specularFactorLight*lightColor,1.0) + vec4(modelColor,1.0); //diff und specular fertig berechnet, jetzt auf fragcolor addieren
        } else diffAndSpecLight = vec4(diffuseLighting*modelColor, 1.0) + vec4(specularFactorLight*lightColor,1.0); //diff und specular fertig berechnet, jetzt auf fragcolor addieren
        FragColor = diffAndSpecLight;
}
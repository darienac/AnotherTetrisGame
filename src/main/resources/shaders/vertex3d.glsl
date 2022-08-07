#version 330

uniform mat4 uViewProjectionMatrix;
uniform mat4 uModelMatrix;
uniform mat4 uNormalMatrix;

in vec4 aVertex;
in vec4 aNormal;
in vec2 aTexCoord;

out vec3 vPosition;
out vec3 vNormal;
out vec2 vTexCoord;

out vec3 vTangent;
out vec3 vCotangent;

void main() {
    vec4 modelPosition = uModelMatrix * aVertex;

    vPosition = modelPosition.xyz;
    vNormal = (uNormalMatrix * aNormal).xyz;
    vTexCoord = aTexCoord * vec2(1.0, -1.0);

    gl_Position = uViewProjectionMatrix * modelPosition;
}
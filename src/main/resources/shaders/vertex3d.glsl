#version 330

uniform mat4 uViewProjectionMatrix;
uniform mat4 uModelMatrix;
uniform mat4 uNormalMatrix;

uniform float uAspect;

in vec4 aVertex;
in vec4 aNormal;
in vec2 aTexCoord;

out vec3 vVertex;
out vec3 vPosition;
out vec3 vNormal;
out vec2 vTexCoord;
out vec2 vScreenCoord;

void main() {
    vec4 modelPosition = uModelMatrix * aVertex;

    vVertex = aVertex.xyz;
    vPosition = modelPosition.xyz;
    vNormal = (uNormalMatrix * aNormal).xyz;
    vTexCoord = aTexCoord * vec2(1.0, -1.0);

    gl_Position = uViewProjectionMatrix * modelPosition;
    vScreenCoord = (gl_Position.xy / gl_Position.w * 0.5 * vec2(uAspect, 1.0)) + vec2(0.5, 0.5);
}
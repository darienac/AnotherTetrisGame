#version 330

uniform float uTime;
uniform float uAspect;

in vec2 aVertex;
in vec2 aTexCoord;

out vec2 vTexCoord;
out vec2 vScreenCoord;

void main()
{
    float time = uTime * 0.1;
    gl_Position = vec4(aVertex, 1.0, 1.0);
    vScreenCoord = aTexCoord;
    vTexCoord = vec2(aTexCoord.x * sin(time * 0.123) + aTexCoord.y * cos(time * 0.842), cos(time * 0.21) * aTexCoord.x + sin(time * 1.2) * aTexCoord.y);
}
#version 330

uniform sampler2D texture0;

uniform float uTime;
uniform float uAspect;

in vec2 vTexCoord;
in vec2 vScreenCoord;

out vec4 fragColor;

void main()
{
    float time = uTime * 2.0;
    vec2 texCoord = vTexCoord + vec2(0.0, sin(vScreenCoord.x * 300.0) * cos(time) * 0.02 + sin(vScreenCoord.x * 146.4) * 0.012);
    float intensity = texture(texture0, texCoord).r;
    fragColor = vec4(min(intensity, 0.5) - 0.35, min(intensity * 4.0 - 2.0, 0.0), intensity * 0.5, 1.0);
}
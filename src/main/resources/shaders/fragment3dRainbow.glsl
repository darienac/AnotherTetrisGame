#version 330

uniform sampler2D textureDiffuse;

uniform float uTime;
uniform float uMessageFade;

in vec3 vPosition;
in vec3 vNormal;
in vec2 vTexCoord;

out vec4 fragColor;

vec3 hsl2rgb(in vec3 c) {
    vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0,4.0,2.0),6.0)-3.0)-1.0, 0.0, 1.0);
    return c.z + c.y * (rgb-0.5)*(1.0-abs(2.0*c.z-1.0));
}

void main() {
    float offset = uTime + vTexCoord.x + vTexCoord.y;
    vec3 rainbowColor = hsl2rgb(vec3(offset, 1.0f, 0.8f));
    fragColor = texture(textureDiffuse, vTexCoord) * vec4(rainbowColor, uMessageFade);
}
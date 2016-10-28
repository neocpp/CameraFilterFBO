precision mediump float; //指定默认精度

varying vec2 vTextureCoord;
uniform sampler2D uTexture;

void main() {
    gl_FragColor = texture2D(uTexture, vTextureCoord);
}
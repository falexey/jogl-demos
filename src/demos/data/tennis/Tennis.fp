// Copyright (C) 2012 JogAmp Community. All rights reserved.
// Details see TennisES2.java

#ifdef GL_ES
  precision mediump float;
  precision mediump int;
#endif

uniform vec4 color;

varying vec3 normal;
varying vec4 position;
varying vec4 TexCoordOut;
varying vec3 lightDir;
varying float attenuation;

uniform sampler2D Texture;

void main()
{  
    
    vec4 diffuse = clamp(dot(lightDir, normal)/attenuation, 0.0, 1.0) + vec4(0.2f, 0.2f, 0.2f, 0.2f);  
        
    if (color[3] > 0)    
    gl_FragColor = color * diffuse;
    else
    gl_FragColor = vec4(texture2D(Texture, TexCoordOut).rgb, 1.0) * diffuse;
 
}

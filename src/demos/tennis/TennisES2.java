package demos.tennis;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLUniformData;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.PMVMatrix;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import java.nio.FloatBuffer;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.GLArrayDataServer;

import javax.media.opengl.GL;
import javax.swing.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.opengl.GLWindow;

/**
 * Tennis.java <BR>
 * author: Fofonov Alexey <P>
 */
 
public class TennisES2 implements GLEventListener {
  static {
	GLProfile.initSingleton();
  }
  
  private static GLWindow glWindow;
  private static float view_rotx = 0.0f; 								//View angles
  private static float view_roty = 0.0f;
  private float view_rotz = 0.0f;
  private float sx = 0.0f, sy = 0.0f;									//X, Y coords of Mydesk
  private float spx,spy;												//Speed of the ball
  private static float spz;
  private float BallCx = 0.0f, BallCy = 0.0f, BallCz = 0.0f;			//Coords of the ball
  private float EnDeskCx = 0.0f, EnDeskCy = 0.0f;						//X, Y coords of Endesk
  private int   cube=0, mydesk=0, endesk=0, ball=0, box=0;				//Flags of the existence 
  private int   swapInterval;
  private static TextureData[] textureData;
  private static Texture[] texture;
  private float Bax=0, Bay=0;											//Acceleration summands
  private static float Vec=3;											//Balls direction
  private static boolean CanF=false;									//Ready for play
  
  private static int PointerX = 0, PointerY = 0;
  
  private static int WindowW=0;
  private static int WindowH=0;	
  private static boolean TennisRunning = true;
  
  private float LPositionDX=0, NPositionDX=0;							//Mouse positions 
  private float LPositionDY=0, NPositionDY=0;							//
  private float DspeedX=0, DspeedY=0;									//Speed of Mydesk

  private static boolean mouseButtonDown = false;
  private static boolean control = true;
  private static int prevMouseX;
  private static int prevMouseY;
  
  private static ShaderState st = null;
  PMVMatrix pmvMatrix;
  PMVMatrix ObjPmvMatrix;
  GLUniformData pmvMatrixUniform;
  GLUniformData colorUniform;
  private FloatBuffer lightPos = Buffers.newDirectFloatBuffer( new float[] { -0.5f, 0.5f, 1.0f } );
  private GLUniformData colorU = null;
  private GLUniformData activeTexture = null;
 
  private static GLArrayDataServer CubeFront;
  private static GLArrayDataServer CubeBack;
  private static GLArrayDataServer CubeTop;
  private static GLArrayDataServer CubeBottom;
  private static GLArrayDataServer CubeLeft;
  private static GLArrayDataServer CubeRight;
  private static GLArrayDataServer Box;
  private static GLArrayDataServer Ball;
  private static GLArrayDataServer DeskFront, DeskBack, DeskSides, DeskTop, DeskBottom;
  
  public static final FloatBuffer red = Buffers.newDirectFloatBuffer( new float[] { 0.8f, 0.1f, 0.0f, 0.7f } );
  public static final FloatBuffer brown = Buffers.newDirectFloatBuffer( new float[] { 0.8f, 0.4f, 0.1f, 0.7f } );
  public static final FloatBuffer blue = Buffers.newDirectFloatBuffer( new float[] { 0.2f, 0.2f, 1.0f, 0.7f } );
  public static final FloatBuffer yellow = Buffers.newDirectFloatBuffer( new float[] { 0.8f, 0.75f, 0.0f, 1.0f } );
  public static final FloatBuffer white = Buffers.newDirectFloatBuffer( new float[] { 1.0f, 1.0f, 1.0f, 0.0f } );
  
  public static void main(String[] args) {
    // set argument 'NotFirstUIActionOnProcess' in the JNLP's application-desc tag for example
    // <application-desc main-class="demos.j2d.TextCube"/>
    //   <argument>NotFirstUIActionOnProcess</argument> 
    // </application-desc>
    // boolean firstUIActionOnProcess = 0==args.length || !args[0].equals("NotFirstUIActionOnProcess") ;
	  Display dpy = NewtFactory.createDisplay(null);
      Screen screen = NewtFactory.createScreen(dpy, 1);
      GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
      glWindow = GLWindow.create(screen, caps);
       
      glWindow.setTitle("TennisES2 demo");
      glWindow.setSize(640, 480);
      glWindow.setPosition(40, 40);
      glWindow.setUndecorated(false);
      glWindow.setAlwaysOnTop(false);
      glWindow.setFullscreen(false);
      glWindow.setPointerVisible(false);
      glWindow.confinePointer(false);
      
      Animator animator = new Animator(glWindow);   
      glWindow.setVisible(true);
      
      animator.setUpdateFPSFrames(60, System.err);
      animator.start();
      
    final TennisES2 tennis = new TennisES2();
    glWindow.addGLEventListener(tennis);
    
    glWindow.addKeyListener(new KeyAdapter() {
        public void keyTyped(KeyEvent e) {
            if(e.getKeyChar()=='f') {
                new Thread() {
                    public void run() {
                        glWindow.setFullscreen(!glWindow.isFullscreen());
                } }.start();
            } 
        }
        
        public void keyPressed(KeyEvent e) {
	        int kc = e.getKeyCode();
	        if(KeyEvent.VK_ESCAPE == kc) {
	        	TennisRunning = false;
	        } 
	        if(KeyEvent.VK_CONTROL == kc) {
	            control = false;
	        } 
	        if(KeyEvent.VK_SPACE == kc) {		//Ready for play
	        	if (CanF==false)
	        	{	
	        		if (Vec<0)
	        			spz=-0.07f;			
	        		else
	        			spz=0.07f;
	        	}
	        	CanF=true;
	    	}
	    }
	    
	    public void keyReleased(KeyEvent e) {    //Give the mouse control to the user    
	    	int kc = e.getKeyCode();
	        if(KeyEvent.VK_CONTROL == kc) {
	            control = true;
	        } 	        
	    }
    });
    
    glWindow.addMouseListener(new MouseAdapter() {
    	public void mousePressed(MouseEvent e) {
            prevMouseX = e.getX();
            prevMouseY = e.getY();
            
            mouseButtonDown = true;
          }
    	        
          public void mouseReleased(MouseEvent e) {
              mouseButtonDown = false;             
          }
          
          public void mouseMoved(final MouseEvent e) {
        	  PointerX = e.getX();
              PointerY = e.getY();
          }
    	        
          public void mouseDragged(MouseEvent e) {
        	  
            int x = e.getX();
            int y = e.getY();
            
            float thetaY = 360.0f * ( (float)(x-prevMouseX)/(float)WindowW);
            float thetaX = 360.0f * ( (float)(prevMouseY-y)/(float)WindowH);
    	        
            prevMouseX = x;
            prevMouseY = y;       
            
            view_rotx += thetaX;
            view_roty += thetaY;
          }
    });
    
    while (TennisRunning == true)
    {}
    
    animator.stop();
    glWindow.destroy();
    
  }
  
  public TennisES2(int swapInterval) {
	this.swapInterval = swapInterval;
  }

  public TennisES2() {
	this.swapInterval = 1;
  }

  public void init(GLAutoDrawable drawable) {
	System.err.println("Tennis: Init: "+drawable);
	// Use debug pipeline
	// drawable.setGL(new DebugGL(drawable.getGL()));

	GL2ES2 gl = drawable.getGL().getGL2ES2();

    System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
    System.err.println("INIT GL IS: " + gl.getClass().getName());
    System.err.println("GL_VENDOR: " + gl.glGetString(GL2.GL_VENDOR));
    System.err.println("GL_RENDERER: " + gl.glGetString(GL2.GL_RENDERER));
    System.err.println("GL_VERSION: " + gl.glGetString(GL2.GL_VERSION));
    
    textureData = new TextureData[5];
    texture = new Texture[5];
     
    //Load textures
    try {
        System.err.println("Loading texture...");
        textureData[0] = TextureIO.newTextureData(gl.getGLProfile(), 
        		getClass().getClassLoader().getResourceAsStream("demos/data/tennis/TennisTop.png"),
                false,
                TextureIO.PNG);
        textureData[1] = TextureIO.newTextureData(gl.getGLProfile(), 
        		getClass().getClassLoader().getResourceAsStream("demos/data/tennis/TennisBottom.png"),
        		false,
                TextureIO.PNG);
        textureData[2] = TextureIO.newTextureData(gl.getGLProfile(), 
        		getClass().getClassLoader().getResourceAsStream("demos/data/tennis/TennisMyDesk.png"),
        		false,
                TextureIO.PNG);
        textureData[3] = TextureIO.newTextureData(gl.getGLProfile(), 
        		getClass().getClassLoader().getResourceAsStream("demos/data/tennis/TennisEnDesk.png"),
        		false,
                TextureIO.PNG);
        textureData[4] = TextureIO.newTextureData(gl.getGLProfile(), 
        		getClass().getClassLoader().getResourceAsStream("demos/data/tennis/Stars.png"),
        		false,
                TextureIO.PNG);
        System.err.println("Texture0 estimated memory size = " + textureData[0].getEstimatedMemorySize());
        System.err.println("Texture1 estimated memory size = " + textureData[1].getEstimatedMemorySize());
        System.err.println("Texture2 estimated memory size = " + textureData[2].getEstimatedMemorySize());
        System.err.println("Texture3 estimated memory size = " + textureData[3].getEstimatedMemorySize());
        System.err.println("Stars estimated memory size = " + textureData[4].getEstimatedMemorySize());
        
        for(int i=0; i<5; i++) {
            gl.glActiveTexture(i);
            texture[i] = new Texture(GL.GL_TEXTURE_2D);
            texture[i].updateImage(gl, textureData[i]);
        }
      } catch (IOException e) {
        e.printStackTrace();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(bos));
        JOptionPane.showMessageDialog(null,
                                      bos.toString(),
                                      "Error loading texture",
                                      JOptionPane.ERROR_MESSAGE);
        throw new GLException(e);
        //return;
      }
    
    st = new ShaderState();
    // st.setVerbose(true);
    final ShaderCode vp0 = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, 1, this.getClass(),
            "demos/data/tennis", "shader/bin", "Tennis");
    final ShaderCode fp0 = ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, 1, this.getClass(),
            "demos/data/tennis", "shader/bin", "Tennis");
    final ShaderProgram sp0 = new ShaderProgram();
    sp0.add(gl, vp0, System.err);
    sp0.add(gl, fp0, System.err);
    st.attachShaderProgram(gl, sp0, true);
    
    pmvMatrix = new PMVMatrix(true);
    st.attachObject("pmvMatrix", pmvMatrix);
    pmvMatrixUniform = new GLUniformData("pmvMatrix", 4, 4, pmvMatrix.glGetPMvMvitMatrixf()); // P, Mv, Mvi and Mvit
    st.ownUniform(pmvMatrixUniform);
    st.uniform(gl, pmvMatrixUniform);
    
    GLUniformData lightU = new GLUniformData("lightPos", 3, lightPos);
    st.uniform(gl, lightU);

    colorU = new GLUniformData("color", 4, red);
    st.ownUniform(colorU);
    st.uniform(gl, colorU);
    
    activeTexture = new GLUniformData("Texture", 0);
    st.ownUniform(activeTexture);
    st.uniform(gl, activeTexture);
    
    ObjPmvMatrix = pmvMatrix;
    		
    //gl.glShadeModel(GL2.GL_SMOOTH);              	// Enable Smooth Shading
    gl.glClearDepth(1.0f);                      	// Depth Buffer Setup
    gl.glEnable(GL2.GL_DEPTH_TEST);             	// Enables Depth Testing
    gl.glDepthFunc(GL2.GL_LEQUAL);               	// The Type Of Depth Testing To Do
	            
    /* make the objects */
    if(0>=cube) {
        cube(gl);
        System.err.println("cube list created: "+cube);
    } else {
        System.err.println("cube list reused: "+cube);
    }
    
    if(0>=box) {
        box(gl);
        System.err.println("box list created: "+box);
    } else {
        System.err.println("box list reused: "+box);
    }
	            
    if(0>=mydesk) {
        desk(gl, 2);
        System.err.println("mydesk list created: "+mydesk);
    } else {
        System.err.println("mydesk list reused: "+mydesk);
    }
	            
    if(0>=endesk) {
        desk(gl, 3);
        System.err.println("endesk list created: "+endesk);
    } else {
        System.err.println("endesk list reused: "+endesk);
    }
    
    if(0>=ball) {
        ball(gl);
        System.err.println("ball list created: "+ball);
    } else {
        System.err.println("ball list reused: "+ball);
    }
    
    st.useProgram(gl, false);
	            
    gl.glEnable(GL2.GL_NORMALIZE);

  }

  public void dispose(GLAutoDrawable drawable) {
	System.err.println("Tennis: Dispose");
  }

  public void display(GLAutoDrawable drawable) {

    // Get the GL corresponding to the drawable we are animating
	GL2ES2 gl = drawable.getGL().getGL2ES2();
    
    if (mouseButtonDown == false && control == true)
    MovMydesk();
    MoveSphere();
    MoveEnDesk();

    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    // Special handling for the case where the GLJPanel is translucent
    // and wants to be composited with other Java 2D content
    if (GLProfile.isAWTAvailable() && 
        (drawable instanceof javax.media.opengl.awt.GLJPanel) &&
        !((javax.media.opengl.awt.GLJPanel) drawable).isOpaque() &&
        ((javax.media.opengl.awt.GLJPanel) drawable).shouldPreserveColorBufferIfTranslucent()) {
      gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
    } else {
      gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    }  
    
    st.useProgram(gl, true);
    
    gl.glDisable(GL2.GL_DEPTH_TEST);
    
    DrawBox(gl);
    
    gl.glEnable(GL2.GL_DEPTH_TEST);
       
    pmvMatrix.glPushMatrix();
    pmvMatrix.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
    pmvMatrix.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
    pmvMatrix.glRotatef(view_rotz, 0.0f, 0.0f, 1.0f);
    
    DrawCube(gl);
    DrawMyDesk(gl);
    DrawEnDesk(gl);
    DrawBall(gl);
    
    pmvMatrix.glPopMatrix();
    st.useProgram(gl, false); 
            
  }
  
  public void DrawBox(GL2ES2 gl)
  {
	  
	  ObjPmvMatrix.glPushMatrix();
	  ObjPmvMatrix.glTranslatef(0f, 0f, 0f);
	  ObjPmvMatrix.glRotatef(0f, 0f, 0f, 1f);
	  ObjPmvMatrix.update();
	  st.uniform(gl, pmvMatrixUniform);
		
	  colorU.setData(white);
	  st.uniform(gl, colorU);
		
	  gl.glActiveTexture(GL.GL_TEXTURE0);
	  texture[4].enable(gl);
	  texture[4].bind(gl);
	  activeTexture.setData(0);
	  st.uniform(gl, activeTexture);
	    
	  draw(gl, Box, GL2.GL_QUADS);
	  
	  ObjPmvMatrix.glPopMatrix();
	  	
  }
  
  public void DrawCube(GL2ES2 gl)
  {
	  
	  ObjPmvMatrix.glPushMatrix();
	  ObjPmvMatrix.glTranslatef(0f, 0f, 0f);
	  ObjPmvMatrix.glRotatef(0f, 0f, 0f, 1f);
	  ObjPmvMatrix.update();
	  st.uniform(gl, pmvMatrixUniform);
	    
	  colorU.setData(brown);
	  st.uniform(gl, colorU);
	  
	  draw(gl, CubeFront, GL2.GL_QUADS);
	  draw(gl, CubeBack, GL2.GL_QUADS);
	  draw(gl, CubeLeft, GL2.GL_QUADS);
	  draw(gl, CubeRight, GL2.GL_QUADS);
	  
	  colorU.setData(white);
	  st.uniform(gl, colorU);
	  
	  gl.glActiveTexture(GL.GL_TEXTURE0);
	  texture[0].enable(gl);
	  texture[0].bind(gl);
	  activeTexture.setData(0);
	  st.uniform(gl, activeTexture);
	  texture[0].disable(gl);
	  draw(gl, CubeTop, GL2.GL_QUADS);
	  
	  gl.glActiveTexture(GL.GL_TEXTURE0);
	  texture[1].enable(gl);
	  texture[1].bind(gl);
	  activeTexture.setData(0);
	  st.uniform(gl, activeTexture);
	  texture[1].disable(gl);
	  draw(gl, CubeBottom, GL2.GL_QUADS);
	  
	  ObjPmvMatrix.glPopMatrix();
	  
  }
  
  public void DrawBall(GL2ES2 gl)
  {
	  
	  ObjPmvMatrix.glPushMatrix();
	  ObjPmvMatrix.glTranslatef(BallCx, BallCy, BallCz);
	  ObjPmvMatrix.glRotatef(0f, 0f, 0f, 1f);
	  ObjPmvMatrix.update();
	  st.uniform(gl, pmvMatrixUniform);
	    
	  colorU.setData(yellow);
	  st.uniform(gl, colorU);
	  
	  draw(gl, Ball, GL2.GL_QUAD_STRIP);
	  
	  ObjPmvMatrix.glPopMatrix();
	  
  }
  
  public void DrawMyDesk(GL2ES2 gl)
  {
	  
	  ObjPmvMatrix.glPushMatrix();
	  ObjPmvMatrix.glTranslatef(sx, sy, 3.0f);
	  ObjPmvMatrix.glRotatef(0f, 0f, 0f, 1f);
	  ObjPmvMatrix.update();
	  st.uniform(gl, pmvMatrixUniform);
	    
	  colorU.setData(red);
	  st.uniform(gl, colorU);
	  
	  draw(gl, DeskTop, GL2.GL_QUAD_STRIP);
	  draw(gl, DeskBottom, GL2.GL_QUAD_STRIP);
	  draw(gl, DeskSides, GL2.GL_QUADS);
	  
	  gl.glActiveTexture(GL.GL_TEXTURE0);
	  texture[2].enable(gl);
	  texture[2].bind(gl);
	  activeTexture.setData(0);
	  st.uniform(gl, activeTexture);
	  texture[2].disable(gl);
	  
	  colorU.setData(white);
	  st.uniform(gl, colorU);
	  
	  draw(gl, DeskFront, GL2.GL_QUAD_STRIP);
	  draw(gl, DeskBack, GL2.GL_QUAD_STRIP);
	    
	  ObjPmvMatrix.glPopMatrix();
	  
  }
  
  public void DrawEnDesk(GL2ES2 gl)
  {
	  
	  ObjPmvMatrix.glPushMatrix();
	  ObjPmvMatrix.glTranslatef(EnDeskCx, EnDeskCy, -3.0f);
	  ObjPmvMatrix.glRotatef(0f, 0f, 0f, 1f);
	  ObjPmvMatrix.update();
	  st.uniform(gl, pmvMatrixUniform);
	    
	  colorU.setData(blue);
	  st.uniform(gl, colorU);
	  
	  draw(gl, DeskTop, GL2.GL_QUAD_STRIP);
	  draw(gl, DeskBottom, GL2.GL_QUAD_STRIP);
	  draw(gl, DeskSides, GL2.GL_QUADS);
	  
	  gl.glActiveTexture(GL.GL_TEXTURE0);
	  texture[3].enable(gl);
	  texture[3].bind(gl);
	  activeTexture.setData(0);
	  st.uniform(gl, activeTexture);
	  texture[3].disable(gl);
	  
	  colorU.setData(white);
	  st.uniform(gl, colorU);
	  
	  draw(gl, DeskFront, GL2.GL_QUAD_STRIP);
	  draw(gl, DeskBack, GL2.GL_QUAD_STRIP);	 
	  
	  ObjPmvMatrix.glPopMatrix();
	  
  }

  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) 
  {
	  
	System.err.println("Tennis: Reshape "+x+"/"+y+" "+width+"x"+height);
	GL2ES2 gl = drawable.getGL().getGL2();

	gl.setSwapInterval(swapInterval);

	float h = (float)height / (float)width;
	 
	WindowW = width;
	WindowH = height;
	
	st.useProgram(gl, true);
    pmvMatrix.glMatrixMode(PMVMatrix.GL_PROJECTION);
    pmvMatrix.glLoadIdentity();
    
    if (h<1)
    	pmvMatrix.glFrustumf(-1.0f, 1.0f, -h, h, 1.0f, 60.0f);
	else
	{
		h = 1.0f/h;
		pmvMatrix.glFrustumf(-h, h, -1.0f, 1.0f, 1.0f, 60.0f);
	}
    
    pmvMatrix.glMatrixMode(PMVMatrix.GL_MODELVIEW);
    pmvMatrix.glLoadIdentity();
    pmvMatrix.glTranslatef(0.0f, 0.0f, -6.0f);
    st.uniform(gl, pmvMatrixUniform);
    st.useProgram(gl, false);	           
	
  }
  
  public static void cube(GL2ES2 gl)
  {
	
	CubeFront = GLArrayDataServer.createGLSLInterleaved(8, GL.GL_FLOAT, false, 8, GL.GL_STATIC_DRAW);
    CubeFront.addGLSLSubArray("vertices", 3, GL.GL_ARRAY_BUFFER);
    CubeFront.addGLSLSubArray("normals", 3, GL.GL_ARRAY_BUFFER);
    CubeFront.addGLSLSubArray("texcoords", 2, GL.GL_ARRAY_BUFFER);
    
    CubeBack = GLArrayDataServer.createGLSLInterleaved(8, GL.GL_FLOAT, false, 8, GL.GL_STATIC_DRAW);
    CubeBack.addGLSLSubArray("vertices", 3, GL.GL_ARRAY_BUFFER);
    CubeBack.addGLSLSubArray("normals", 3, GL.GL_ARRAY_BUFFER);
    CubeBack.addGLSLSubArray("texcoords", 2, GL.GL_ARRAY_BUFFER);
    
    CubeTop = GLArrayDataServer.createGLSLInterleaved(8, GL.GL_FLOAT, false, 8, GL.GL_STATIC_DRAW);
    CubeTop.addGLSLSubArray("vertices", 3, GL.GL_ARRAY_BUFFER);
    CubeTop.addGLSLSubArray("normals", 3, GL.GL_ARRAY_BUFFER);
    CubeTop.addGLSLSubArray("texcoords", 2, GL.GL_ARRAY_BUFFER);
    
    CubeBottom = GLArrayDataServer.createGLSLInterleaved(8, GL.GL_FLOAT, false, 8, GL.GL_STATIC_DRAW);
    CubeBottom.addGLSLSubArray("vertices", 3, GL.GL_ARRAY_BUFFER);
    CubeBottom.addGLSLSubArray("normals", 3, GL.GL_ARRAY_BUFFER);
    CubeBottom.addGLSLSubArray("texcoords", 2, GL.GL_ARRAY_BUFFER);
    
    CubeLeft = GLArrayDataServer.createGLSLInterleaved(8, GL.GL_FLOAT, false, 8, GL.GL_STATIC_DRAW);
    CubeLeft.addGLSLSubArray("vertices", 3, GL.GL_ARRAY_BUFFER);
    CubeLeft.addGLSLSubArray("normals", 3, GL.GL_ARRAY_BUFFER);
    CubeLeft.addGLSLSubArray("texcoords", 2, GL.GL_ARRAY_BUFFER);
    
    CubeRight = GLArrayDataServer.createGLSLInterleaved(8, GL.GL_FLOAT, false, 8, GL.GL_STATIC_DRAW);
    CubeRight.addGLSLSubArray("vertices", 3, GL.GL_ARRAY_BUFFER);
    CubeRight.addGLSLSubArray("normals", 3, GL.GL_ARRAY_BUFFER);
    CubeRight.addGLSLSubArray("texcoords", 2, GL.GL_ARRAY_BUFFER);
    
    float normal[] = new float[3];
    float tcoords[] = new float[2];

	/* draw left sides */
	
		normal[0] = 1.0f; normal[1] = 0.0f; normal[2] = 0.0f;
		vert(CubeLeft, -2.0f, -1.5f, -3.0f, normal, tcoords);
		vert(CubeLeft, -2.0f,  1.5f, -3.0f, normal, tcoords);
		vert(CubeLeft, -2.0f,  1.5f,  3.0f, normal, tcoords);
		vert(CubeLeft, -2.0f, -1.5f,  3.0f, normal, tcoords);
		
		normal[0] = -1.0f; normal[1] = 0.0f; normal[2] = 0.0f;
		vert(CubeLeft, -2.05f, -1.55f, -3.0f, normal, tcoords);
		vert(CubeLeft, -2.05f,  1.55f, -3.0f, normal, tcoords);
		vert(CubeLeft, -2.05f,  1.55f,  3.0f, normal, tcoords);
		vert(CubeLeft, -2.05f, -1.55f,  3.0f, normal, tcoords);
		
		CubeLeft.seal(true);
	
	if (texture[0] != null) {
	      TextureCoords coords = texture[0].getImageTexCoords();
	
	/* draw up sides */
	
		normal[0] = 0.0f; normal[1] = -1.0f; normal[2] = 0.0f;
		tcoords[0] = coords.left(); tcoords[1] = coords.top();
		vert(CubeTop, -2.0f, 1.5f, -3.0f, normal, tcoords);
		tcoords[0] = coords.left(); tcoords[1] = coords.bottom();
		vert(CubeTop, -2.0f, 1.5f,  3.0f, normal, tcoords);
		tcoords[0] = coords.right(); tcoords[1] = coords.bottom();
		vert(CubeTop, 2.0f, 1.5f,  3.0f, normal, tcoords);
		tcoords[0] = coords.right(); tcoords[1] = coords.top();
		vert(CubeTop, 2.0f, 1.5f, -3.0f, normal, tcoords);	
	
		tcoords[0] = coords.right(); tcoords[1] = coords.bottom();
		normal[0] = 0.0f; normal[1] = 1.0f; normal[2] = 0.0f;
		vert(CubeTop, -2.05f, 1.55f, -3.0f, normal, tcoords);
		vert(CubeTop, -2.05f, 1.55f,  3.0f, normal, tcoords);
		vert(CubeTop,  2.05f, 1.55f,  3.0f, normal, tcoords);
		vert(CubeTop,  2.05f, 1.55f, -3.0f, normal, tcoords);
		
		CubeTop.seal(true);

	}
	
	/* draw right sides */
	
		normal[0] = -1.0f; normal[1] = 0.0f; normal[2] = 0.0f;
		vert(CubeRight, 2.0f, -1.5f, -3.0f, normal, tcoords);
		vert(CubeRight, 2.0f,  1.5f, -3.0f, normal, tcoords);
		vert(CubeRight, 2.0f,  1.5f,  3.0f, normal, tcoords);
		vert(CubeRight, 2.0f, -1.5f,  3.0f, normal, tcoords);
		
		normal[0] = 1.0f; normal[1] = 0.0f; normal[2] = 0.0f;
		vert(CubeRight, 2.05f, -1.55f, -3.0f, normal, tcoords);
		vert(CubeRight, 2.05f,  1.55f, -3.0f, normal, tcoords);
		vert(CubeRight, 2.05f,  1.55f,  3.0f, normal, tcoords);
		vert(CubeRight, 2.05f, -1.55f,  3.0f, normal, tcoords);
		
		CubeRight.seal(true);
	
	if (texture[1] != null) {
	      TextureCoords coords = texture[1].getImageTexCoords();
	
	/* draw down sides */
	
	    normal[0] = 0.0f; normal[1] = 1.0f; normal[2] = 0.0f;
	    tcoords[0] = coords.left(); tcoords[1] = coords.top();
		vert(CubeBottom, -2.0f, -1.5f, -3.0f, normal, tcoords);
		tcoords[0] = coords.left(); tcoords[1] = coords.bottom();
		vert(CubeBottom, -2.0f, -1.5f,  3.0f, normal, tcoords);
		tcoords[0] = coords.right(); tcoords[1] = coords.bottom();
		vert(CubeBottom, 2.0f, -1.5f,  3.0f, normal, tcoords);
		tcoords[0] = coords.right(); tcoords[1] = coords.top();
		vert(CubeBottom, 2.0f, -1.5f, -3.0f, normal, tcoords);
		
		normal[0] = 0.0f; normal[1] = -1.0f; normal[2] = 0.0f;
		vert(CubeBottom, -2.05f, -1.55f, -3.0f, normal, tcoords);
		vert(CubeBottom, -2.05f, -1.55f,  3.0f, normal, tcoords);
		vert(CubeBottom,  2.05f, -1.55f,  3.0f, normal, tcoords);
		vert(CubeBottom,  2.05f, -1.55f, -3.0f, normal, tcoords);
		
		CubeBottom.seal(true);
	
	}
	
	/* draw back sides */
	
		normal[0] = 0.0f; normal[1] = 0.0f; normal[2] = -1.0f;
		
		vert(CubeBack, -2.05f, 1.55f, -3.0f, normal, tcoords);
		vert(CubeBack,  2.05f, 1.55f, -3.0f, normal, tcoords);
		vert(CubeBack,  2.0f, 1.5f, -3.0f, normal, tcoords);
		vert(CubeBack, -2.0f, 1.5f, -3.0f, normal, tcoords);
		
		vert(CubeBack, -2.05f, -1.55f, -3.0f, normal, tcoords);
		vert(CubeBack,  2.05f, -1.55f, -3.0f, normal, tcoords);
		vert(CubeBack,  2.0f, -1.5f, -3.0f, normal, tcoords);
		vert(CubeBack, -2.0f, -1.5f, -3.0f, normal, tcoords);
		
		vert(CubeBack, -2.05f, -1.55f, -3.0f, normal, tcoords);
		vert(CubeBack, -2.05f, 1.55f, -3.0f, normal, tcoords);
		vert(CubeBack, -2.0f, 1.5f, -3.0f, normal, tcoords);
		vert(CubeBack, -2.0f, -1.5f, -3.0f, normal, tcoords);
		
		vert(CubeBack, 2.05f, -1.55f, -3.0f, normal, tcoords);
		vert(CubeBack, 2.05f, 1.55f, -3.0f, normal, tcoords);
		vert(CubeBack, 2.0f, 1.5f, -3.0f, normal, tcoords);
		vert(CubeBack, 2.0f, -1.5f, -3.0f, normal, tcoords);	
		
		CubeBack.seal(true);
	
	/* draw front sides */
	
		normal[0] = 0.0f; normal[1] = 0.0f; normal[2] = 1.0f;
		
		vert(CubeFront, -2.05f, 1.55f, 3.0f, normal, tcoords);
		vert(CubeFront,  2.05f, 1.55f, 3.0f, normal, tcoords);
		vert(CubeFront,  2.0f, 1.5f, 3.0f, normal, tcoords);
		vert(CubeFront, -2.0f, 1.5f, 3.0f, normal, tcoords);
		
		vert(CubeFront, -2.05f, -1.55f, 3.0f, normal, tcoords);
		vert(CubeFront,  2.05f, -1.55f, 3.0f, normal, tcoords);
		vert(CubeFront,  2.0f, -1.5f, 3.0f, normal, tcoords);
		vert(CubeFront, -2.0f, -1.5f, 3.0f, normal, tcoords);
		
		vert(CubeFront, -2.05f, -1.55f, 3.0f, normal, tcoords);
		vert(CubeFront, -2.05f, 1.55f, 3.0f, normal, tcoords);
		vert(CubeFront, -2.0f, 1.5f, 3.0f, normal, tcoords);
		vert(CubeFront, -2.0f, -1.5f, 3.0f, normal, tcoords);
		
		vert(CubeFront, 2.05f, -1.55f, 3.0f, normal, tcoords);
		vert(CubeFront, 2.05f, 1.55f, 3.0f, normal, tcoords);
		vert(CubeFront, 2.0f, 1.5f, 3.0f, normal, tcoords);
		vert(CubeFront, 2.0f, -1.5f, 3.0f, normal, tcoords);
		
		CubeFront.seal(true);
	  
  }
  
  public static void box(GL2ES2 gl)	//Usually "box" mean box, but there only one side is enough
  {
	
	Box = GLArrayDataServer.createGLSLInterleaved(8, GL.GL_FLOAT, false, 4, GL.GL_STATIC_DRAW);
	Box.addGLSLSubArray("vertices", 3, GL.GL_ARRAY_BUFFER);
	Box.addGLSLSubArray("normals", 3, GL.GL_ARRAY_BUFFER);
	Box.addGLSLSubArray("texcoords", 2, GL.GL_ARRAY_BUFFER);
	
	float normal[] = new float[3];
	float tcoords[] = new float[2];
	
	if (texture[4] != null) {
	      gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
	      gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
	      
	/* draw the box */
	
	    normal[0] = 0.0f; normal[1] = 0.0f; normal[2] = 1.0f;
	    tcoords[0] = 0.0f; tcoords[1] = 0.0f;
		vert(Box, -16.0f, -16.0f, -10.0f, normal, tcoords);
		tcoords[0] = 0.0f; tcoords[1] = 4.0f;
		vert(Box, -16.0f, 16.0f, -10.0f, normal, tcoords);
		tcoords[0] = 4.0f; tcoords[1] = 4.0f;
		vert(Box, 16.0f, 16.0f, -10.0f, normal, tcoords);
		tcoords[0] = 4.0f; tcoords[1] = 0.0f;
		vert(Box, 16.0f, -16.0f, -10.0f, normal, tcoords);
		
			Box.seal(true);
	
	}
	 
  }
  
  public static void desk(GL2ES2 gl, int two_or_three)
  {
	  
	int i;
	float temp1;
	
	DeskFront = GLArrayDataServer.createGLSLInterleaved(8, GL.GL_FLOAT, false, 50, GL.GL_STATIC_DRAW);
	DeskFront.addGLSLSubArray("vertices", 3, GL.GL_ARRAY_BUFFER);
	DeskFront.addGLSLSubArray("normals", 3, GL.GL_ARRAY_BUFFER);
	DeskFront.addGLSLSubArray("texcoords", 2, GL.GL_ARRAY_BUFFER);
	
	DeskBack = GLArrayDataServer.createGLSLInterleaved(8, GL.GL_FLOAT, false, 50, GL.GL_STATIC_DRAW);
	DeskBack.addGLSLSubArray("vertices", 3, GL.GL_ARRAY_BUFFER);
	DeskBack.addGLSLSubArray("normals", 3, GL.GL_ARRAY_BUFFER);
	DeskBack.addGLSLSubArray("texcoords", 2, GL.GL_ARRAY_BUFFER);
	
	DeskTop = GLArrayDataServer.createGLSLInterleaved(8, GL.GL_FLOAT, false, 50, GL.GL_STATIC_DRAW);
	DeskTop.addGLSLSubArray("vertices", 3, GL.GL_ARRAY_BUFFER);
	DeskTop.addGLSLSubArray("normals", 3, GL.GL_ARRAY_BUFFER);
	DeskTop.addGLSLSubArray("texcoords", 2, GL.GL_ARRAY_BUFFER);
	
	DeskBottom = GLArrayDataServer.createGLSLInterleaved(8, GL.GL_FLOAT, false, 50, GL.GL_STATIC_DRAW);
	DeskBottom.addGLSLSubArray("vertices", 3, GL.GL_ARRAY_BUFFER);
	DeskBottom.addGLSLSubArray("normals", 3, GL.GL_ARRAY_BUFFER);
	DeskBottom.addGLSLSubArray("texcoords", 2, GL.GL_ARRAY_BUFFER);
	
	DeskSides = GLArrayDataServer.createGLSLInterleaved(8, GL.GL_FLOAT, false, 8, GL.GL_STATIC_DRAW);
	DeskSides.addGLSLSubArray("vertices", 3, GL.GL_ARRAY_BUFFER);
	DeskSides.addGLSLSubArray("normals", 3, GL.GL_ARRAY_BUFFER);
	DeskSides.addGLSLSubArray("texcoords", 2, GL.GL_ARRAY_BUFFER);
	
	float normal[] = new float[3];
	float tcoords[] = new float[2];

	if (texture[two_or_three] != null) {
	
	/* draw the front */
	
	    normal[0] = 0.0f; normal[1] = 0.0f; normal[2] = 1.0f;
		for (i=0; i<25; i++)
		{
			temp1 = (float) Math.pow(Math.sin(i/24.0f*Math.PI), 0.4d);
			 
			tcoords[0] = (i-12)/40.0f; tcoords[1] = temp1/4 + 0.75f;
			vert(DeskFront, (i-12)/40.0f, temp1/10 + 0.1f , 0.01f + temp1/25, normal, tcoords);
			tcoords[0] = (i-12)/40.0f; tcoords[1] = -temp1/4 + 0.25f;
			vert(DeskFront, (i-12)/40.0f, -temp1/10 - 0.1f, 0.01f + temp1/25, normal, tcoords);	
		}
		
		DeskFront.seal(true);
	
	/* draw the back */
	
		normal[0] = 0.0f; normal[1] = 0.0f; normal[2] = -1.0f;
		for (i=0; i<25; i++)
		{
			temp1 = (float) Math.pow(Math.sin(i/24.0f*Math.PI), 0.4d);
			
			tcoords[0] = (i-12)/40.0f; tcoords[1] = temp1/4 + 0.75f;
			vert(DeskBack, (i-12)/40.0f, temp1/10 + 0.1f , -0.01f - temp1/25, normal, tcoords);
			tcoords[0] = (i-12)/40.0f; tcoords[1] = -temp1/4 + 0.25f;
			vert(DeskBack, (i-12)/40.0f, -temp1/10 - 0.1f, -0.01f - temp1/25, normal, tcoords);
			
		}
		
		DeskBack.seal(true);

	}		
	
	/* draw the top side */
	
		normal[0] = 0.0f; normal[1] = 1.0f; normal[2] = 0.0f;
		for (i=0; i<25; i++)
		{
			temp1 = (float) Math.pow(Math.sin(i/24.0f*Math.PI), 0.4d);
			
			vert(DeskTop, (i-12)/40.0f, temp1/10 + 0.1f , -0.01f - temp1/25, normal, tcoords);
			vert(DeskTop, (i-12)/40.0f, temp1/10 + 0.1f , 0.01f + temp1/25, normal, tcoords);
		}
		
		DeskTop.seal(true);
			
	/* draw the bottom side */

		normal[0] = 0.0f; normal[1] = -1.0f; normal[2] = 0.0f;
		for (i=0; i<25; i++)
		{	
			temp1 = (float) Math.pow(Math.sin(i/24.0f*Math.PI), 0.4d);
			
			vert(DeskBottom, (i-12)/40.0f, -temp1/10 - 0.1f, 0.01f + temp1/25, normal, tcoords);
			vert(DeskBottom, (i-12)/40.0f, -temp1/10 - 0.1f, -0.01f - temp1/25, normal, tcoords);	
		}
		
		DeskBottom.seal(true);
	
	/* draw the left and right sides */
	
			normal[0] = -1.0f; normal[1] = 0.0f; normal[2] = 0.0f;
			vert(DeskSides, (-12)/40.0f, -0.1f, 0.01f, normal, tcoords);
			vert(DeskSides, (-12)/40.0f, +0.1f, 0.01f, normal, tcoords);
			vert(DeskSides, (-12)/40.0f, +0.1f, -0.01f, normal, tcoords);
			vert(DeskSides, (-12)/40.0f, -0.1f, -0.01f, normal, tcoords);	
			
			normal[0] = 1.0f; normal[1] = 0.0f; normal[2] = 0.0f;
			vert(DeskSides, (+12)/40.0f, -0.1f, 0.01f, normal, tcoords);
			vert(DeskSides, (+12)/40.0f, +0.1f, 0.01f, normal, tcoords);
			vert(DeskSides, (+12)/40.0f, +0.1f, -0.01f, normal, tcoords);
			vert(DeskSides, (+12)/40.0f, -0.1f, -0.01f, normal, tcoords);
			
		DeskSides.seal(true);

  }
  
  public static void ball(GL2ES2 gl)
  {
	  
	int i,j;
	float y1,y2,r1,r2,x,z;
	
	Ball = GLArrayDataServer.createGLSLInterleaved(8, GL.GL_FLOAT, false, 840, GL.GL_STATIC_DRAW);
	Ball.addGLSLSubArray("vertices", 3, GL.GL_ARRAY_BUFFER);
	Ball.addGLSLSubArray("normals", 3, GL.GL_ARRAY_BUFFER);
	Ball.addGLSLSubArray("texcoords", 2, GL.GL_ARRAY_BUFFER);
	
	float normal[] = new float[3];
	float tcoords[] = new float[2];
	
	tcoords[0] = 0f; tcoords[1] = 0f;

	/* draw the ball */
	
		for (i=0; i<20; i++)
		{
			y1 = (float) Math.cos((i)/20.0f*Math.PI)/10;
			y2 = (float) Math.cos((i+1)/20.0f*Math.PI)/10;
			r1 = (float) Math.sqrt(Math.abs(0.01f-y1*y1));
			r2 = (float) Math.sqrt(Math.abs(0.01f-y2*y2));
			
			for (j=0; j<21; j++)
			{
				x = (float) (r1*Math.cos((float)j/21*2.0f*Math.PI));
				z = (float) (r1*Math.sin((float)j/21*2.0f*Math.PI));
				normal[0] = 10*x; normal[1] = 10*y1; normal[2] = 10*z;
				vert(Ball, x, y1, z, normal, tcoords);
				
				x = (float) (r2*Math.cos((float)j/21*2.0f*Math.PI));
				z = (float) (r2*Math.sin((float)j/21*2.0f*Math.PI));
				normal[0] = 10*x; normal[1] = 10*y2; normal[2] = 10*z;
				vert(Ball, x, y2, z, normal, tcoords);
			}
		}
		
		Ball.seal(true);
	
  }
  
  public void MoveSphere()
  {
	  
	// Ball out

		if ((BallCz>3)||(BallCz<-3))
		{
			
			Vec=BallCz;
			
			BallCx = 0.0f;
			BallCy = 0.0f;
			BallCz = 0.0f;

			spz=0;
			spx=0;
			spy=0;

			CanF=false;
			
			Bax=0;
			Bay=0;

		}

	// Ball rebound

		if ((spz<0)&&(BallCz+spz<-2.8)&&(BallCx+spx<EnDeskCx+0.3)&&(BallCx+spx>EnDeskCx-0.3)&&(BallCy+spy<EnDeskCy+0.2)&&(BallCy+spy>EnDeskCy-0.2))
		{
		
			spz=-spz+0.002f;
			spx=spx+(BallCx-EnDeskCx)/10;
			spy=spy+(BallCy-EnDeskCy)/10;	
		
		}

		if ((spz>0)&&(BallCz+spz>2.8)&&(BallCx+spx<sx+0.3)&&(BallCx+spx>sx-0.3)&&(BallCy+spy<sy+0.2)&&(BallCy+spy>sy-0.2))
		{
			
			spz=-spz-0.002f;
			spx=spx+(BallCx-sx)/10;
			spy=spy+(BallCy-sy)/10;
			
			Bax=DspeedX/100;
			Bay=DspeedY/100;

		}

		if ((BallCx+spx<-1.9)||(BallCx+spx>1.9))
		spx=-spx;

		if ((BallCy+spy<-1.4)||(BallCy+spy>1.4))
		spy=-spy;

	// Ball acceleration

		spx=spx+Bax;
		spy=spy+Bay;

	// Ball move

	    if (CanF==true)
	    	
	    BallCx += spx;
		BallCy += spy;
		BallCz += spz;
		
	//Less the acceleration

		Bax=Bax-Bax/100;
		Bay=Bay-Bay/100;
	  
  }
  
  public void MoveEnDesk()
  {

	  //Just follow for the ball
	  
  float sx,sy;
  double gip=Math.sqrt((BallCx-EnDeskCx)*(BallCx-EnDeskCx)+(BallCy-EnDeskCy)*(BallCy-EnDeskCy));

  if (gip<0.07)
  {
  sx=Math.abs((BallCx-EnDeskCx));
  sy=Math.abs((BallCy-EnDeskCy));
  }
  else
  {
  sx=Math.abs((BallCx-EnDeskCx))/((float) gip)*0.07f;
  sy=Math.abs((BallCy-EnDeskCy))/((float) gip)*0.07f;
  }

  if ((BallCx-EnDeskCx>0)&&(EnDeskCx+sx<=1.7)) 
	  EnDeskCx += sx;

  if ((BallCx-EnDeskCx<0)&&(EnDeskCx-sx>=-1.7))
	  EnDeskCx -= sx;

  if ((BallCy-EnDeskCy>0)&&(EnDeskCy+sy<=1.3))
	  EnDeskCy += sy;

  if ((BallCy-EnDeskCy<0)&&(EnDeskCy-sy>=-1.3))
	  EnDeskCy -= sy;

  }
  
  public void MovMydesk() {
 	  
	  LPositionDX = sx;
	  LPositionDY = sy;

      sx = sx + (float)(PointerX-WindowW/2)/300.0f;
      sy = sy + (float)(WindowH/2-PointerY)/300.0f;  
      
      //Check cube borders
      
      if (sx<-1.7f || sx>1.7f)
    	{ 
    	  if (sx>0) sx = 1.7f;
    	  else     sx = -1.7f;
    	}
      
      if (sy<-1.3 || sy>1.3)
      	{ 
    	  if (sy>0) sy = 1.3f;
    	  else     sy = -1.3f;
    	}
    	
      //Return the mouse back from screen borders
      glWindow.warpPointer(WindowW/2, WindowH/2);

    	NPositionDX=sx;
    	NPositionDY=sy;

    	DspeedX=NPositionDX-LPositionDX;
    	DspeedY=NPositionDY-LPositionDY;      
      
    }
  
  static void vert(GLArrayDataServer array, float x, float y, float z, float n[]) {
      array.putf(x);
      array.putf(y);
      array.putf(z);
      array.putf(n[0]);
      array.putf(n[1]);
      array.putf(n[2]);
  }
  
  static void vert(GLArrayDataServer array, float x, float y, float z, float n[], float t[]) {
      array.putf(x);
      array.putf(y);
      array.putf(z);
      array.putf(n[0]);
      array.putf(n[1]);
      array.putf(n[2]);
      array.putf(t[0]);
      array.putf(t[1]);
  }
  
  private void draw(GL2ES2 gl, GLArrayDataServer array, int mode) {
      array.enableBuffer(gl, true);
      gl.glDrawArrays(mode, 0, array.getElementCount());
      array.enableBuffer(gl, false);
  }
  
}

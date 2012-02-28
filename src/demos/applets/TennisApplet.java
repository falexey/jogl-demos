package demos.applets;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;

import demos.tennis.Tennis;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import javax.media.opengl.GLAnimatorControl;

/** Shows how to deploy an applet using JOGL. This demo must be
    referenced from a web page via an &lt;applet&gt; tag. */

public class TennisApplet extends Applet {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private GLAnimatorControl animator;

  public void init() {
    System.err.println("TennisApplet: init() - begin");
    GLProfile.initSingleton();
    setLayout(new BorderLayout());
    GLCanvas canvas = new GLCanvas();
    canvas.addGLEventListener(new Tennis());
    canvas.setSize(getSize());
    add(canvas, BorderLayout.CENTER);
    animator = new FPSAnimator(canvas, 60);
    
    Toolkit t = Toolkit.getDefaultToolkit();
    Image i = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    Cursor noCursor = t.createCustomCursor(i, new Point(0, 0), "none");
    canvas.setCursor(noCursor);
    
    System.err.println("TennisApplet: init() - end");  
  }

  public void start() {
    System.err.println("TennisApplet: start() - begin");
    animator.start();
    System.err.println("TennisApplet: start() - end");
  }

  public void stop() {
    // FIXME: do I need to do anything else here?
    System.err.println("TennisApplet: stop() - begin");
    animator.stop();
    System.err.println("TennisApplet: stop() - end");
  }

  public void destroy() {
    System.err.println("TennisApplet: destroy() - X");
  }
}

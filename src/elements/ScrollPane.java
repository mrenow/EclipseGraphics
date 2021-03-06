package elements;

import static core.MainProgram.*;

import static util.DB.*;

import events.KeyEvents;
import events.KeyListener;
import events.ScrollEvents;
import events.ScrollListener;
import processing.core.PVector;

//Default usage of srcoll pane involves placing a large sized component into a smaller space. Scrolling in x, y or both must be specified.
//Draws a transparent grey rectangle for the scrollbar 
public class ScrollPane extends Container implements ScrollListener, KeyListener {

	// x,y,xy
	public static final byte SCROLL_Y = 0;
	public static final byte SCROLL_X = 1;
	public static final byte SCROLL_XY = 2;
	
	public static final float DEFAULT_SCROLL_SPEED = 10;
	
	byte mode;
	float panewidth;
	float paneheight;
	
	float scrollspeed;
	// may implement velocity scrolling later, currently is useless.
	/*
	 * float scrollvel; float SCROLLDRAG;
	 */

	PVector offset = new PVector(0, 0);

	public ScrollPane(float x, float y, float w, float h, byte mode, Container p) {
		super(x, y, w, h, p);
		this.mode = mode;
		panewidth = w;
		paneheight = h;
		
		scrollspeed = DEFAULT_SCROLL_SPEED;
		ScrollEvents.add(this);
		KeyEvents.add(this);
		setBackgroundColor(p3.color(200,200,200));
	}
	public ScrollPane(float x, float y, byte mode, Container p, Element ... children) {
		super(x,y,p,children);
		this.mode = mode;
		panewidth = w;
		paneheight = h;
		
		scrollspeed = DEFAULT_SCROLL_SPEED;
		ScrollEvents.add(this);
		KeyEvents.add(this);
		setBackgroundColor(p3.color(200,200,200));
	}
	public ScrollPane(byte mode, Container p, Element ... children) {
		this(0,0,mode,p,children);
	}
	
	// If scroll for direction is deactivated, it will simply be cut off.
	public void setPaneWidth(float width) {
		panewidth = max(width,getWidth());		
	}
	
	public void setPaneHeight(float height) {
		paneheight = max(height,getHeight());
	}

	public void elementScrolled(int value) {
		DB_U(this, "scrolled", value);
		switch (mode) {
		case 0:
			movePane(0, value*scrollspeed);
			break;
		case 1:
			movePane(value*scrollspeed, 0);
			break;
		case 2:
			if (KeyEvents.key[SHIFT]) {
				movePane(value*scrollspeed, 0);
			} else {
				movePane(0, value*scrollspeed);
			}
			break;
		}
	}
	public void movePane(float x, float y) {
		PVector oldoffset = offset.copy();
		offset.x += x;
		offset.y += y;
		offset.x = constrain(offset.x, 0, panewidth - getWidth());
		offset.y = constrain(offset.y, 0, paneheight - getHeight());
		for (Element e : children) {
			e.pos.x += oldoffset.x - offset.x ;
			e.pos.y += oldoffset.y - offset.y;
		}
		
		
		requestUpdate();
	}

	public void elementHovered() {
	}

	public void elementUnhovered() {
	}

	public void keyPressed() {
		if (cursorhover) {
			if (KeyEvents.key[UP]) {

				movePane(0, -4);
			}
			if (KeyEvents.key[DOWN]) {
				movePane(0, 4);
			}
			if (KeyEvents.key[LEFT]) {
				movePane(-4, 0);
			}
			if (KeyEvents.key[RIGHT]) {
				movePane(4, 0);
			}
			requestUpdate();
		}
	}

	public void keyReleased() {
	}

	public void keyTyped() {
	}

	protected void update() {
		if (backgroundcolor != 0) {
			g.background(backgroundcolor);
		}
		
		
		drawChildren();
		
		g.noStroke();
		g.fill(0, 0, 0, 60);
		float w = getWidth();
		float h = getHeight();
		
		
		switch (mode) {
		case 0:
			g.rect(w - 12, 2 + offset.y * (h - 4) / paneheight, 10, h * h / paneheight);
			break;
		case 1:
			g.rect(2 + offset.x * (w - 4) / panewidth, 2, w * w / panewidth, 10);
			break;
		case 2:
			g.rect(2, 2 + offset.y * (h - 4) / paneheight, 10, (h - 4) * (h - 4) / paneheight);
			g.rect(2 + offset.x * (w - 4) / panewidth, 2, (w - 4) * (w - 4) / panewidth, 10);
			break;
		}
	}
	
	
}
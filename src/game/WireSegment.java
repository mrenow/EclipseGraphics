package game;

import static core.MainProgram.*;
import static util.DB.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import processing.core.PVector;
import util.DisjointSet;
import util.LLinkedList;
import util.SparseQuadTree;

public class WireSegment extends DisjointSet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	//assigned depending on gameArea context.
	public static SparseQuadTree<WireSegment> container;
	public static LLinkedList<WireSegment> potentialdisconnects = new LLinkedList<WireSegment>(); 
	
	public static final byte METAL = 0;
	public static final byte N_TYPE = 1;
	public static final byte P_TYPE = 2;
	public static final byte VIA = 3;
	public static final byte N_GATE = 4;
	public static final byte P_GATE = 5;
	public static final byte POWER = 6;

	public static final byte[] SILICON_LAYER = {P_TYPE,N_TYPE,N_GATE,P_GATE};
	public static final byte[] METAL_LAYER = {METAL,POWER};	
	public static final byte[] VIA_LAYER = {VIA};	
	

	
	// individual info
	
	byte mode;

	int x;
	int y;
	
	//order 1 check for membership of lists.
	public boolean updatablecurrent;
	public boolean updatablenext;

	
	// Set Info
	public static int WIRE_OFF = Integer.MAX_VALUE;
	
	public LLinkedList<Gate> gates;
	public LLinkedList<WireSegment> connections;
	// Pointer to an int for instant update
	// Priority value for signal propagation logic
	protected int active = Integer.MAX_VALUE;
	
	public boolean checkdisconnects = false;
	public int getActive(){
		if(!isAncestor()) {
			return ((WireSegment) parent).getActive();
		} else {
			return active;
		}
	}
	public void setActive(int val) {
		if(!isAncestor()) {
			((WireSegment) parent).setActive(val);
		} else {
			active = val;
		}
	}
	

	public WireSegment(byte mode, int x, int y) {
		this.mode = mode;
		this.x = x;
		this.y = y;
		this.gates = new LLinkedList<Gate>();
		this.connections = new LLinkedList<WireSegment>();
		this.parent = this;
		

		// oinfo = new ObjectInfo(x, y);
	}
	
	
	
	protected void clearInfo() {
		/*gates.clear();
		connections.clear();
		active = WIRE_OFF;
		*/
		for (WireSegment w : getAdjacent()) {
			remove(w);
		}
	}
	
	
	
	

	// this becomes parent to that
	public void addInfo(DisjointSet that) {
		
		DB_U("added",that,"->",this);
		
		if(this == that) {
			DB_E("INFO OF THIS ADDED TO THIS IN", this, ", PANIC");
			return;
		}
		WireSegment child = (WireSegment) that;
		this.gates.add(child.gates);
		this.connections.add(child.connections);
		/*
		if (this.gates == null || this.gates.empty()) {
			child.gates = this.gates;
		} else {
			this.gates.add(child.gates);
		}
		if (this.connections == null || this.gates.empty()) {
			child.connections = this.connections;
		} else {
			this.connections.add(child.connections);
		}*/
	}
 
	// remove that from this
	@Override
	public void subInfo(DisjointSet that) {
		WireSegment child = (WireSegment) that;
		if(!gates.contains(child.gates)) {
			DB_E("CHILD DID NOT CONTAIN EXPECTED INFORMATION", x, y);
		}else {
			
			this.gates.remove(child.gates);
			this.connections.remove(child.connections);
			this.active = WIRE_OFF;
		}
		
	}
	
	public void updateSetInfo() {
		for(WireSegment w: getAdjacent()) {
			if(canConnect(w) && w != parent && !this.isSameSet(w)) {
				this.add(w);
			}			
		}
	}
	

	public boolean isActive() {
		return getActive() != Integer.MAX_VALUE;
	}
	// Wire is always permissive
	public boolean isPermissive() {
		return true;
		
	}
	
	public boolean canConnect(WireSegment that) {
		if(this.x == that.x && this.y == that.y)
			return (this.mode == VIA || that.mode == VIA);
		return (this.mode == that.mode && !(this.mode == VIA || that.mode == VIA));
	}
	public void updateConnections() {
		ListIterator<WireSegment> iter = new LLinkedList<WireSegment>(getAdjacent()).iterator();
		WireSegment w;
		connections.clear();
		gates.clear();
		
		while(iter.hasNext()) {
			w = iter.next();
			if(w instanceof Gate || w instanceof Power) {
				
				//connection
				if(canMakeConnection(w)) {
					connections.add(w);
					w.connections.add(this);
				}
				//gate input
				if(canMakeGate(w)) {
					gates.add((Gate)w);
					((Gate)w).inputs.add(this);	
				}
			}
			else if(canConnect(w)) {
				if(!isSameSet(w)) {
					if(isAncestor()) {
						w.add(this);
					}else {
						w.makeAncestor(this);
					}
				} else {
					// adjacent wires which are not directly connected by a parent-child relation
					// are possible spots for future disconnects. These will be inspected
					// and fixed before the next time the circuit is run.
					potentialdisconnects.addFirst(w);
					w.checkdisconnects = true;
					potentialdisconnects.addFirst(this);
					checkdisconnects = true;
				}
			}
		}
		DB_U("Connections Updateded");
	}
	public boolean canMakeConnection(WireSegment that) {

		if((that.mode == VIA || this.mode == VIA)) {
		//Via on block
			return this.x == that.x && this.y == that.y;
		}
		//reduces type to P or N
		int this_mode = this.mode%3;
		int that_mode = that.mode%3;
		
		return this_mode == that_mode;
	}
	public boolean canMakeInput(WireSegment that) {
		if(that instanceof Gate ) return false;
		int this_mode = this.mode%3;
		int that_mode = that.mode%3;
		
		if(this_mode == METAL || that_mode == METAL) {
			return false;	
		}
		return (this_mode != that_mode);
	}
	public boolean canMakeGate(WireSegment that) {
		if(!(that instanceof Gate) || (this instanceof Gate)) return false;
		
		int this_mode = this.mode%3;
		int that_mode = that.mode%3;
		
		if(this_mode == METAL || that_mode == METAL) {
			return false;	
		}
		
		return this_mode != that_mode;
	}
	
	
	
	
	public String toString() {
		StringBuilder s = new StringBuilder() ;
	
		switch(mode) {
		case  METAL: s.append("m"); break;
		case P_TYPE: s.append("p"); break;
		case N_TYPE: s.append("n"); break;
		case P_GATE: s.append("P"); break;
		case N_GATE: s.append("N"); break; 
		case    VIA: s.append("v"); break;
		}
		s.append("{");
		s.append(x);
		s.append(",");
		s.append(y);
		s.append("}");
		return s.toString();
	}
	private LLinkedList<WireSegment> getAdjacent(Direction d) {
		
		switch (d) {
		case NORTH:
			return container.get(x, y - 1);
		case EAST:
			return container.get(x + 1, y);
		case SOUTH:
			return container.get(x, y + 1);
		case WEST:
			return container.get(x - 1, y);
		case IN:
			LLinkedList<WireSegment> l = container.get(x, y);
			l.remove(this);
			return l;
		}
		return null;
	}
	
	public LLinkedList<WireSegment> getAdjacent(){
		LLinkedList<WireSegment> out = new LLinkedList<WireSegment>();
		for(Direction d: Direction.values()) {
			out.add(getAdjacent(d));
		}
		return out;
	}
	
	public WireSegment getParent() {
		return (WireSegment)parent;	
	}
	public void delete() {
		for (WireSegment w : getAdjacent()) {
			if(this.parent == w) {
				w.remove(this);
			} else if(w.parent == this) {
				this.remove(w);
			} else {
				if(w.isGate()) {
					((Gate)w).inputs.remove(this);
					((Gate)w).connections.remove(this);
				}else{
					w.connections.remove(this);
				}
				
			}
		}
		if(checkdisconnects) {
			potentialdisconnects.removeAll(this);
		}
	}
	
	public boolean isGate() {
		return false;
		
	}
	
	public void updateActive( LLinkedList<WireSegment> current, LLinkedList<WireSegment> next) {
		if(!isAncestor()) {
			((WireSegment)getAncestor()).updateActive(current,next);
			return;
		}
		//search for lowest neighbor
		int min = WIRE_OFF;
		for(WireSegment adj : connections) {
			if(adj.getActive() < min) {
				min = adj.getActive();	
			}
		}
		//update wire state and queue updates for appropriate neighbors.
		if(getActive() < min || min == WIRE_OFF) {
			setActive(WIRE_OFF);
			for(WireSegment output : connections) {
				if(output.getActive() != WIRE_OFF && output.isPermissive() && !output.updatablecurrent) {
					current.add(output);
					output.updatablecurrent = true;
				
				}
			}
		} else {
			setActive(min + 1);
			for(WireSegment output : connections) {
				if(output.getActive() == WIRE_OFF && output.isPermissive() && !output.updatablecurrent) {
					current.add(output);
					output.updatablecurrent = true;
				}
			}
		}
		for(Gate input : gates) {
			if(!input.updatablenext) {
				next.add(input);
				input.updatablenext = true;
			}
		}
		
		updatablecurrent = false;
	}
	
	
	public void updatePowered(){return;}
	
	public String modeToString() {
		switch(mode) {
		case WireSegment.METAL:
			return "METAL";
		case WireSegment.N_TYPE:
			return "N_TYPE";
		case WireSegment.P_TYPE:
			return "P_TYPE";
		case WireSegment.VIA:
			return "VIA";
		case WireSegment.N_GATE:
			return "N_GATE";
		case WireSegment.P_GATE:
			return "P_GATE";
		case WireSegment.POWER:
			return "POWER";
		default:
			return "INVALID";
		}
	}
	public byte[] getLayer() {
		switch(mode) {
		case WireSegment.METAL:
			return METAL_LAYER;
		case WireSegment.N_TYPE:
			return SILICON_LAYER;
		case WireSegment.P_TYPE:
			return SILICON_LAYER;
		case WireSegment.VIA:
			return VIA_LAYER;
		case WireSegment.N_GATE:
			return SILICON_LAYER;
		case WireSegment.P_GATE:
			return SILICON_LAYER;
		case WireSegment.POWER:
			return METAL_LAYER;
		}
		return null;
	}
}

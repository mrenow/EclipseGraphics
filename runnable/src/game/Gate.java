package game;
import static core.MainProgram.*;
import static util.DB.*;
import util.LinkedList;




// An input connection triggers a gate, affecting output connections.
// gates can only exist as single element sets. 
//
public class Gate extends WireSegment{
	
	
	LinkedList<WireSegment> inputs;
	
	boolean permissive = (mode == P_GATE);
	
	public Gate(byte mode, int x, int y) {
		super(mode, x,y);
		inputs = new LinkedList<WireSegment>();
	}
	
	public boolean isPermissive() {
		//N gate becomes permissive when powered
		return permissive;
	}
	
	public void updatePowered() {
		permissive = (mode == N_GATE) == powered();
	}
	
	
	//gates act as their own segments, their parent is always themselves.
	@Override
	public void updateConnections() {
		// P gates receive input from P and connect to N
		inputs.clear();
		connections.clear();
	
		for(WireSegment w : getAdjacent()) {

			
			// silicon connections
			if(canMakeConnection(w)) {
				w.connections.add(this);
				connections.add(w);
			} 
			//gate input
			if(canMakeInput(w)) {
				w.gates.add(this);
				inputs.add(w);					
			}
			if(canMakeGate(w)) {
				gates.add((Gate)w);
				((Gate)w).inputs.add(this);						
			}
	
			/*
			if((mode == N_GATE && w.mode%3 == P_TYPE) || (mode == P_GATE && w.mode%3 == N_TYPE)) {
				w.connections.add(this);
				connections.add(w);
			}
			//gate input
			if((mode == P_GATE && w.mode%3 == P_TYPE) || (mode == N_GATE && w.mode%3 == N_TYPE)) {
				w.gates.add(this);
				inputs.add(w);					
			}*/
		}
	}

	


	
	
	
	public void delete() {
		for (WireSegment w : getAdjacent()) {
			w.connections.remove(this);
			w.gates.remove(this);
		}
		if(checkdisconnects) {
			potentialdisconnects.removeAll(this);
		}
	}
	
	public void updateActive(LinkedList<WireSegment> current, LinkedList<WireSegment> next) {
		//if segment is gate, do not permit flow based on gate type
		if(isPermissive()) super.updateActive(current,next);
		else{
			for (Gate g : gates) {
				if(g.powered()&& !g.updatablenext) {
					next.add(g);
					g.updatablenext = true;
				}
			}
			for (WireSegment connection : connections) {
				if(connection.getActive() != WIRE_OFF && !connection.updatablecurrent) {
					current.add(connection);	
					connection.updatablecurrent = true;
				}
			}
			setActive(WIRE_OFF);
			updatablecurrent = false;
		}
	}
	
	
	

	public boolean powered() {
		for(WireSegment w : inputs) {
			if(w.getActive() != WIRE_OFF) return true;
		}
		return false;
	}
	@Override
	public boolean isGate() {
		return true;
	}
	
}
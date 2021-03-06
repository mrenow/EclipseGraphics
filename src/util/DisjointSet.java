package util;

import static util.DB.DB_E;

// 

/* ancestors will contain information about the set as a whole such as:
 * - group size
 * and in the case of a wire segment
 * - list of gates it can toggle
 * - list of gated wire connections
 * - powered/unpowered
 */
public abstract class DisjointSet{
	
	//pointer to parent
    protected DisjointSet parent = this;
    
	//combines two sets if they are different
	// attaches that to this.
    public void add(DisjointSet that) {
		if(!isSameSet(that)) {
			that.parent = this;
			this.addInfo(that);
		}
	}
    public boolean remove(DisjointSet that) {
    	if(that.parent != this)	return false;
    	that.parent = that;
    	this.subInfo(that);
    	return true;
    }
    
	
	//Ancestor is highest object in the heirachy.
	public DisjointSet getAncestor() {
		if(isAncestor()) return this;
		return parent.getAncestor();
	}
	public void makeAncestor(DisjointSet s) {
		DisjointSet oldparent = parent;
		parent.remove(this);
		s.add(this);
		if(this != oldparent) {
			oldparent.makeAncestor(this);
		}
	}

	/* public void makeAncestor(DisjointSet s) {
		this.clearInfo();
		DisjointSet oldparent = parent;
		s.add(this);
		if(this != oldparent) {
			oldparent.makeAncestor(this);
		}
		updateSetInfo();
	}*/
	public boolean isSameSet(DisjointSet s) {
		return s.getAncestor() == this.getAncestor();
	}
	
	//Object is the ancestor if parent == self.
	public boolean isAncestor() {return this == parent;}

	public abstract void addInfo(DisjointSet that);
	public abstract void subInfo(DisjointSet that);	
	public abstract void updateSetInfo();
	protected abstract void clearInfo();
}



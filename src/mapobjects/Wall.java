package mapobjects;

import java.awt.Graphics2D;

public class Wall extends MapObject{
	private boolean hiddenObject;
	
	public Wall(int x,int y,boolean v,boolean d,boolean c,String p){
		super(x,y,v,d,c,p);
	}
	
	@Override
	public void draw(Graphics2D g2d){	
	}

	@Override
	public void update(){
	}
	
	public void addHiddenObject(){}
	
	public void destroy(){
		//show hidden Object
	}
}
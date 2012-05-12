package mapobjects;

import imageloader.ImageLoader;

import java.awt.Graphics2D;

public class Enemy  extends MoveableObject{
	private boolean hiddenObject = false;
	
	public Enemy(int x,int y,boolean v,boolean d,boolean c,String p){
		super(x,y,v,d,c,p);
	}
	
	@Override
	public void move(){
		// also move hidden Object
	}
    
	@Override
	public void draw(Graphics2D g2d,ImageLoader gr){	
		g2d.drawImage(gr.getImage(imageUrl),posX,posY,null);
	}

	@Override
	public void update(){
	}
	
	// hidden object released on die
	public void addHiddenObject(){}
	
	public void die(){}
}

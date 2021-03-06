
package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.ImageIcon;

import map.Map;
import mapobjects.Player;
import singleplayer.Campaign;
import singleplayer.TransitionUnit;
import singleplayer.WorldMapUnit;

/*
 * loads single-player campaign and updates the current map object
 */
public class LevelManagerUnit extends GraphicalGameUnit {

	private Campaign campaign;
	private Map currentMap;
	private WorldMapUnit worldMapUnit;
	private Player player;

	// OffsetVariablen für die zu Zeichnende Map
	private int mapOffsetX=0;
	private int mapOffsetY=0;
	//Booleans wenn map kleiner als bereich bei initalisierung auf true
	private boolean mapXSmaller = false;
	private boolean mapYSmaller = false;
	BufferedImage mapCanvas;

	// Might be necessary to protect unit from KeyEvent inferno
	private boolean unitRunning = false;

	public LevelManagerUnit() {
		initComponent();
	}

	@Override
	public void drawComponent(Graphics g) {	
		if (unitRunning) {
			g.setColor(Color.black);
			g.fillRect(0, 0, GameConstants.FRAME_SIZE_X, GameConstants.FRAME_SIZE_Y);
			mapCanvas = new BufferedImage(currentMap.getWidth(), currentMap.getHeight(), BufferedImage.TYPE_INT_ARGB);
			currentMap.drawMap((Graphics2D) mapCanvas.getGraphics());
			g.drawImage(mapCanvas, this.mapOffsetX, this.mapOffsetY, currentMap.getWidth(), currentMap.getHeight(), null);
		}
	}

	@Override
	public void handleKeyPressed(KeyEvent e) {
		if (unitRunning) {
			int key = e.getKeyCode();
			if (key == KeyEvent.VK_ESCAPE) {
				getNavigator().set(UnitState.BASE_MENU_UNIT);
			}
			if (key == KeyEvent.VK_UP) {
				player.direction.UP.set(true);
			}

			if (key == KeyEvent.VK_DOWN) {
				player.direction.DOWN.set(true);
			}

			if (key == KeyEvent.VK_LEFT) {
				player.direction.LEFT.set(true);
			}

			if (key == KeyEvent.VK_RIGHT) {
				player.direction.RIGHT.set(true);
			}

			if (key == KeyEvent.VK_SPACE) {
				player.layBomb(currentMap.getCollisionMap());
			}
		}
	}

	@Override
	public void handleKeyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_UP) {
			player.direction.UP.set(false);
		}

		if (key == KeyEvent.VK_DOWN) {
			player.direction.DOWN.set(false);
		}

		if (key == KeyEvent.VK_LEFT) {
			player.direction.LEFT.set(false);
		}

		if (key == KeyEvent.VK_RIGHT) {
			player.direction.RIGHT.set(false);
		}

	}

	@Override
	public void initComponent() {		
		try {
			campaign = Campaign.readCampaignFromFile("campaign1.txt");
			worldMapUnit = new WorldMapUnit(campaign.getWorldMap());			

		} catch (FileNotFoundException e) {
			System.err.println("Error loading Campaign: Campaign not found!");
			e.printStackTrace();
			terminateLevelManager();
		} catch (IOException e) {
			System.err.println("Error loading Campaign: IOException!");
			e.printStackTrace();
			terminateLevelManager();
		}
	}

	private void terminateLevelManager() {
		getNavigator().set(UnitState.BASE_MENU_UNIT);
		getNavigator().removeGameUnit(UnitState.LEVEL_MANAGER_UNIT);		
	}

	@Override
	public void updateComponent() {
		if (unitRunning) {
			if (!currentMap.isFinished()) {
				currentMap.update();
				updateOffset();
			} else {
				unitRunning = false;			
				
				if (currentMap.playerSucced()) {
					
					BufferedImage message = new BufferedImage(
							GameConstants.FRAME_SIZE_X,GameConstants.FRAME_SIZE_X, BufferedImage.TYPE_INT_ARGB);
					Image tmp = new ImageIcon("graphics/gui/YouWin.png").getImage();
					message.createGraphics().drawImage(tmp, 0, GameConstants.FRAME_SIZE_X/8
							, GameConstants.FRAME_SIZE_X,tmp.getHeight(null), null);
					
					if (!campaign.updateCounters()) {					
						if (campaign.isFinished()) {
							TransitionUnit trans = new TransitionUnit(UnitState.BASE_MENU_UNIT, message);
							trans.setNavigator(getNavigator());
							getNavigator().removeGameUnit(UnitState.LEVEL_MANAGER_UNIT);		
							getNavigator().addGameUnit(trans, UnitState.TEMPORARY_UNIT);
							getNavigator().set(UnitState.TEMPORARY_UNIT);
						} else {
							// level completed, show world map
							worldMapUnit.setNavigator(getNavigator());
							TransitionUnit trans = new TransitionUnit( UnitState.TEMPORARY_UNIT, message, worldMapUnit);
							trans.setNavigator(getNavigator());
							getNavigator().addGameUnit(trans, UnitState.TEMPORARY_UNIT);
							getNavigator().set(UnitState.TEMPORARY_UNIT);
						}				
					}
				} else {
					
					BufferedImage message = new BufferedImage(
							GameConstants.FRAME_SIZE_X,GameConstants.FRAME_SIZE_X, BufferedImage.TYPE_INT_ARGB);
					Image tmp = new ImageIcon("graphics/gui/YouLose.png").getImage();
					message.createGraphics().drawImage(tmp, 0, GameConstants.FRAME_SIZE_X/8
							, GameConstants.FRAME_SIZE_X,tmp.getHeight(null), null);
					
					TransitionUnit trans = new TransitionUnit( UnitState.LEVEL_MANAGER_UNIT, message);
					trans.setNavigator(getNavigator());
					getNavigator().addGameUnit(trans, UnitState.TEMPORARY_UNIT);
					getNavigator().set(UnitState.TEMPORARY_UNIT);
				}
			}	
		} else {
			changeCurrentMap();
			unitRunning = true;
		}
	}

	
	private void changeCurrentMap() {
		currentMap = campaign.getCurrentMap();
		player = currentMap.getMapPlayer();
		player.direction.UP.set(false);
		player.direction.DOWN.set(false);
		player.direction.LEFT.set(false);
		player.direction.RIGHT.set(false);
		initOffset();
	}

	/*
	 * initializes offset
	 */

	public void initOffset() {
		if(currentMap.getWidth()<GameConstants.FRAME_SIZE_X){
			mapOffsetX = (GameConstants.FRAME_SIZE_X-currentMap.getWidth())/2;
			mapXSmaller = true;
		}else{
			if(player.getPosX()-GameConstants.TILE_SIZE>= GameConstants.FRAME_SIZE_X){
				if(player.getPosX()>=currentMap.getWidth()-GameConstants.FRAME_SIZE_X){
					mapOffsetX = -(currentMap.getWidth()-GameConstants.FRAME_SIZE_X);
				}else{
					mapOffsetX = -(player.getPosX()-GameConstants.FRAME_SIZE_X/2);
				}
			}
		}

		if(currentMap.getHeight()<GameConstants.FRAME_SIZE_Y){
			mapOffsetY = (GameConstants.FRAME_SIZE_Y-currentMap.getHeight())/2;  //wenn Map kleiner offset auf halben leerbereich setzen
			mapYSmaller = true;
		}else{
			if(player.getPosY()- GameConstants.TILE_SIZE >= GameConstants.FRAME_SIZE_Y){
				if(player.getPosY()>=currentMap.getWidth()-GameConstants.FRAME_SIZE_Y){
					mapOffsetY = -(currentMap.getWidth()-GameConstants.FRAME_SIZE_Y);
				}else{
					mapOffsetY = -(player.getPosY()-GameConstants.FRAME_SIZE_Y/2);
				}
			}
		}
	}

	/*
	 * updates the current Offset
	 */
	public void updateOffset(){
		if(!mapXSmaller){
			if(player.getPosX()>GameConstants.FRAME_SIZE_X/2-25){
				if(player.getPosX()-GameConstants.FRAME_SIZE_X/2+25<currentMap.getWidth()-GameConstants.FRAME_SIZE_X){
					this.mapOffsetX = -(player.getPosX()-GameConstants.FRAME_SIZE_X/2)-GameConstants.TILE_SIZE/2;
				}else{
					this.mapOffsetX= -(currentMap.getWidth()-GameConstants.FRAME_SIZE_X);
				}
			}else{
				this.mapOffsetX=0;
			}
		}

		if(!mapYSmaller){
			if(player.getPosY()>GameConstants.FRAME_SIZE_Y/2-25){
				if(player.getPosY()-GameConstants.FRAME_SIZE_Y/2+25<currentMap.getHeight()-GameConstants.FRAME_SIZE_Y){
					this.mapOffsetY = -(player.getPosY()-GameConstants.FRAME_SIZE_Y/2)-GameConstants.TILE_SIZE/2;
				}else{
					this.mapOffsetY= -(currentMap.getHeight()-GameConstants.FRAME_SIZE_Y);
				}
			}else{
				this.mapOffsetY=0;
			}
		}
	}

}

package pathfinder.ihm;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Frame extends Application{

	private int size=200;
	private int scale =5;
	private Cell[][] matriceCells = new Cell[size][size];
	private List<Cell> allCells = new ArrayList<Cell>();
	private Cell startCell;
	private Cell finishCell;
	private double gameSpeedSecs = 0.0001;

	private boolean searching;

	private Loop loop;

	private List<Cell> path = new ArrayList<Cell>();

	@Override
	public void start(Stage stage) throws Exception {
		BorderPane pane = new BorderPane();
		Canvas canvas = new Canvas((size*scale)+size,(size*scale)+size);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		canvas.setOnMousePressed(e->{
			if(startCell != null && finishCell != null){
				e.consume();
				return;
			}
			int x = (int) e.getX();
			int y = (int) e.getY();
			for(Cell inLstCells : allCells){
				if(x-inLstCells.getX() >= 0 && x-inLstCells.getX()<= scale && y-inLstCells.getY()>=0 && y-inLstCells.getY()<=scale){
					if(inLstCells.isPath()){
						gc.setFill(Color.RED);
						gc.fillRect(inLstCells.getX(), inLstCells.getY(), scale, scale);
						inLstCells.setPathPoint(true);
						if(startCell == null){
							startCell = inLstCells;
							startCell.setPathPoint(true);
						}
						else{
							finishCell = inLstCells;
							finishCell.setPathPoint(true);
						}
						if(finishCell != null){
							if(finishCell.equals(startCell)){
								finishCell = null;
								e.consume();
								return;
							}
						}
					}
				}
			}
		});
		makeMaze(gc);
		Scene scene = new Scene(pane,800,800,Color.BLACK);
		scene.setOnKeyReleased(e->{
			if(e.getCode().equals(KeyCode.ENTER)){
				findPath(gc);
			}
			else if(e.getCode().equals(KeyCode.BACK_SPACE)){
				randomFindPath(gc);
			}
			else if(e.getCode().equals(KeyCode.R)){
				loop.stop();
				gc.setFill(Color.BLACK);
				if(startCell != null){
					gc.fillRect(startCell.getX(), startCell.getY(), scale, scale);
				}
				if(finishCell != null){
					gc.fillRect(finishCell.getX(), finishCell.getY(), scale, scale);
				}
				startCell = null;
				finishCell = null;
				for(Cell inLstCell : allCells){
					if(inLstCell.isPath()){
						gc.setFill(Color.BLACK);
						gc.fillRect(inLstCell.getX(), inLstCell.getY(), scale,scale);
						inLstCell.setPathUsed(false);
					}
				}
				path.clear();
			}
			else if(e.getCode().equals(KeyCode.S)){
				loop.stop();
				for(Cell inPath : path){
					gc.setFill(Color.BLUE);
					gc.fillRect(inPath.getX(), inPath.getY(), scale, scale);
				}
			}
			else if(e.getCode().equals(KeyCode.D)){
				for(Cell inAllCell : allCells){
					gc.clearRect(inAllCell.getX(),inAllCell.getY(), scale, scale);
				}
				allCells.clear();
				makeMaze(gc);
			}
		});
		pane.setCenter(canvas);
		stage = new Stage();
		stage.setScene(scene);
		stage.setTitle("Path Finder");
		stage.show();
	}
	private void makeMaze(GraphicsContext gc){
		for(int i=0;i<size-1;i++){
			for(int y=0;y<size-1;y++){
				Cell cell = new Cell((i*scale)+i,(y*scale)+y,i,y);
				matriceCells[i][y]= cell;
				allCells.add(cell);
				int rdm = new Random().nextInt(3);
				if(rdm%2 == 0){
					cell.setPath(true);
				}
				else{
					cell.setPath(false);
				}
			}
		}
		for(Cell inAllCells : allCells){
			if(!inAllCells.isPath()){
				gc.setFill(Color.WHITE);
				gc.fillRect(inAllCells.getX(), inAllCells.getY(), scale, scale);
			}
		}
	}
	private void findPath(GraphicsContext gc){
		if(startCell == null || finishCell == null){
			return;
		}
		System.out.println("start x :"+startCell.getMatriceX()+" y : "+startCell.getMatriceY());
		System.out.println("finish x :"+finishCell.getMatriceX()+" y : "+finishCell.getMatriceY());
		searching = true;
		loop = new Loop((long)(gameSpeedSecs * 1000000000L), elapsed->{
			Cell cell = null;
			Cell cellToGo = null;
			int actualX =0;
			int actualY=0;
			int xToGo =0;
			int yToGo =0;
			while(searching){
				for(int i=0; i<size-1;i++){
					for(int y=0;y<size-1;y++){
						if(matriceCells[i][y].equals(startCell) && cell == null){
							cell = matriceCells[i][y];
							actualX = i;
							actualY = y;
						}
						if(matriceCells[i][y].equals(finishCell) && cellToGo == null){
							cellToGo = matriceCells[i][y];
							xToGo =i;
							yToGo =y;
						}
					}
				}
				boolean makingPath = true;
				boolean blocked = false;
				while(makingPath){
					try{
					boolean doing = false;
					if(actualX>xToGo && !doing){
						doing = true;
						if(actualX != 0 && actualX != size && matriceCells[actualX-1][actualY]!= null){
							if(matriceCells[actualX-1][actualY].isPath() && !matriceCells[actualX-1][actualY].isPathUsed()){
								path.add(matriceCells[actualX-1][actualY]);
								matriceCells[actualX-1][actualY].setPathUsed(true);
								actualX--;
							}
							else if(!matriceCells[actualX-1][actualY].isPath() || matriceCells[actualX-1][actualY].isPathUsed()){
								boolean added = false;
								boolean doingInside = false;
								if(matriceCells[actualX][actualY-1].isPath() && !matriceCells[actualX][actualY-1].isPathUsed() && actualY-1>0){
									path.add(matriceCells[actualX][actualY-1]);
									matriceCells[actualX][actualY-1].setPathUsed(true);
									actualY--;
									added = true;
									doingInside = true;
								}
								else if(matriceCells[actualX][actualY+1].isPath()  &&!matriceCells[actualX][actualY+1].isPathUsed() && !doingInside && actualY+1<size-1){
									path.add(matriceCells[actualX][actualY+1]);
									matriceCells[actualX][actualY+1].setPathUsed(true);
									actualY++;
									added = true;
									doingInside = true;
								}
								else if(matriceCells[actualX+1][actualY].isPath() && !matriceCells[actualX+1][actualY].isPathUsed() && ! doingInside && actualX+1<size-1){
									path.add(matriceCells[actualX+1][actualY]);
									matriceCells[actualX+1][actualY].setPathUsed(true);
									actualX++;
									added = true;
									doingInside = true;
								}
								if(!added){
									System.out.println("Blocked 1");
									makingPath = false;
									cell = null;
									cellToGo = null;
									blocked = true;
									break;
								}
							}
							else{
								System.out.println("Blocked 1Bis");
								makingPath = false;
								cell = null;
								cellToGo = null;
								blocked = true;
								break;
							}
						}
					}
					else if(actualX<xToGo && !doing){
						doing = true;
						if(actualX!= 0 && actualX != size && matriceCells[actualX+1][actualY] != null){
							if(matriceCells[actualX+1][actualY].isPath() && !matriceCells[actualX+1][actualY].isPathUsed()){
								path.add(matriceCells[actualX+1][actualY]);
								matriceCells[actualX+1][actualY].setPathUsed(true);
								actualX++;
							}
							else if(!matriceCells[actualX+1][actualY].isPath() || matriceCells[actualX+1][actualY].isPathUsed()){
								boolean added = false;
								boolean doingInside = false;
								if(matriceCells[actualX][actualY-1].isPath()&& !matriceCells[actualX][actualY-1].isPathUsed() && actualY-1>0){
									path.add(matriceCells[actualX][actualY-1]);
									matriceCells[actualX][actualY-1].setPathUsed(true);
									actualY--;
									added = true;
									doingInside = true;
								}
								else if(matriceCells[actualX][actualY+1].isPath() && !matriceCells[actualX][actualY+1].isPathUsed() && !doingInside && actualY+1<size-1){
									path.add(matriceCells[actualX][actualY+1]);
									matriceCells[actualX][actualY+1].setPathUsed(true);
									actualY++;
									added = true;
									doingInside = true;
								}
								else if(matriceCells[actualX-1][actualY].isPath() && !matriceCells[actualX-1][actualY].isPathUsed() && ! doingInside && actualX-1>0){
									path.add(matriceCells[actualX-1][actualY]);
									matriceCells[actualX-1][actualY].setPathUsed(true);
									actualX++;
									added = true;
									doingInside = true;
								}
								if(!added){
									System.out.println("Blocked 2");
									makingPath = false;
									cell = null;
									cellToGo = null;
									blocked = true;
									break;
								}
							}
							else{
								System.out.println("Blocked 2Bis");
								makingPath = false;
								cell = null;
								cellToGo = null;
								blocked = true;
								break;
							}
						}
					}
					else if(actualY>yToGo && !doing){
						doing = true;
						if(actualY != 0 && actualY != size && matriceCells[actualX][actualY-1] != null){
							if(matriceCells[actualX][actualY-1].isPath() && !matriceCells[actualX][actualY-1].isPathUsed()){
								path.add(matriceCells[actualX][actualY-1]);
								matriceCells[actualX][actualY-1].setPathUsed(true);
								actualY--;
							}
							else if(!matriceCells[actualX][actualY-1].isPath() || matriceCells[actualX][actualY-1].isPathUsed()){
								boolean added = false;
								boolean doingInside = false;
								if(matriceCells[actualX+1][actualY].isPath()&& !matriceCells[actualX+1][actualY].isPathUsed() && actualX+1<size-1){
									path.add(matriceCells[actualX+1][actualY]);
									matriceCells[actualX+1][actualY].setPathUsed(true);
									actualX++;
									doingInside = true;
									added = true;
								}
								else if(matriceCells[actualX-1][actualY].isPath()&& !matriceCells[actualX-1][actualY].isPathUsed() && !doingInside && actualX-1>0){
									path.add(matriceCells[actualX-1][actualY]);
									matriceCells[actualX-1][actualY].setPathUsed(true);
									actualX--;
									doingInside = true;
									added = true;
								}
								else if(matriceCells[actualX][actualY+1].isPath() && !matriceCells[actualX][actualY+1].isPathUsed() && !doingInside && actualY+1<size-1){
									path.add(matriceCells[actualX][actualY+1]);
									matriceCells[actualX][actualY+1].setPathUsed(true);
									actualY++;
									doingInside = true;
									added = true;
								}
								if(!added){
									System.out.println("Blocked 3");
									makingPath = false;
									cell = null;
									cellToGo = null;
									blocked = true;
									break;
								}
							}
							else{
								System.out.println("Blocked 3Bis");
								makingPath = false;
								cell = null;
								cellToGo = null;
								blocked = true;
								break;
							}
						}
					}
					else if(actualY<yToGo && !doing){
						doing = true;
						if(actualY != 0 && actualY != size && matriceCells[actualX][actualY+1] != null){
							if(matriceCells[actualX][actualY+1].isPath() && !matriceCells[actualX][actualY+1].isPathUsed()){
								path.add(matriceCells[actualX][actualY+1]);
								matriceCells[actualX][actualY+1].setPathUsed(true);
								actualY++;
							}
							else if(!matriceCells[actualX][actualY+1].isPath() || matriceCells[actualX][actualY+1].isPathUsed()){
								boolean added = false;
								boolean doingInside = false;
								if(matriceCells[actualX+1][actualY].isPath() && !matriceCells[actualX+1][actualY].isPathUsed() && actualX+1<size-1){
									path.add(matriceCells[actualX+1][actualY]);
									matriceCells[actualX+1][actualY].setPathUsed(true);
									actualX++;
									doingInside = true;
									added = true;
								}
								else if(matriceCells[actualX-1][actualY].isPath() && !matriceCells[actualX-1][actualY].isPathUsed() && !doingInside && actualX-1>0){
									path.add(matriceCells[actualX-1][actualY]);
									matriceCells[actualX-1][actualY].setPathUsed(true);
									actualX--;
									doingInside = true;
									added = true;
								}
								else if(matriceCells[actualX][actualY-1].isPath() && !matriceCells[actualX][actualY-1].isPathUsed() && !doingInside && actualY-1>0){
									path.add(matriceCells[actualX][actualY-1]);
									matriceCells[actualX][actualY-1].setPathUsed(true);
									actualY++;
									doingInside = true;
									added = true;
								}
								if(!added){
									System.out.println("Blocked 4");
									makingPath = false;
									cell = null;
									cellToGo = null;
									blocked = true;
									break;
								}
							}
							else{
								System.out.println("Blocked 4Bis");
								makingPath = false;
								cell = null;
								cellToGo = null;
								blocked = true;
								break;
							}
						}
					}
					else if(actualX == xToGo && actualY == yToGo){
						makingPath = false;
					}
					else{
						System.out.println("Blocked 5");
						makingPath = false;
						cell = null;
						cellToGo = null;
						blocked = true;
						break;
					}
				}
					catch(Exception e){
						e.printStackTrace();
						return;
					}
				}
				if(blocked){
					for(Cell inPath : path){
						for(Cell inAllCells : allCells){
							if(inPath.equals(inAllCells)){
								gc.setFill(Color.RED);
								gc.fillRect(inAllCells.getX(), inAllCells.getY(), scale, scale);
							}
						}
						inPath.setPathUsed(false);
					}
					for(Cell inAllCells : allCells){
						if(inAllCells.isPathUsed() && !inAllCells.equals(startCell) && !inAllCells.equals(finishCell)){
							gc.setFill(Color.ORANGE);
							gc.fillRect(inAllCells.getX(), inAllCells.getY(), scale, scale);
						}
						else if(!inAllCells.isPathUsed() && !inAllCells.equals(startCell) && !inAllCells.equals(finishCell) && inAllCells.isPath()){
							gc.setFill(Color.BLACK);
							gc.fillRect(inAllCells.getX(), inAllCells.getY(), scale, scale);
						}
					}
					if(path.size()>0){
						path.get(path.size()-1).setPathUsed(true);
					}
					path.clear();
					blocked = false;
					cell = null;
					cellToGo=null;
					return;
				}
				else{
					for(Cell inPath : path){
						for(Cell inAllCells : allCells){
							if(inPath.equals(inAllCells)){
								gc.setFill(Color.RED);
								gc.fillRect(inAllCells.getX(), inAllCells.getY(), scale, scale);
							}
						}
					}
				}
				if(cell != null && cellToGo != null){
					searching = false;
				}
			}
			try {
				loop.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		loop.start();
	}
	private void randomFindPath(GraphicsContext gc) {
		if(startCell == null || finishCell == null){
			return;
		}
		List<Cell[]> lstPathFound = new ArrayList<Cell[]>();
		for(int i=0;i<1000;i++){
			System.out.println(i);
			List<Cell> path = new ArrayList<Cell>();
			Cell cell = null;
			Cell cellToGo = null;
			int actualX =0;
			int actualY=0;
			int xToGo =0;
			int yToGo =0;
			for(int j=0; j<size-1;j++){
				for(int y=0;y<size-1;y++){
					if(matriceCells[j][y].equals(startCell) && cell == null){
						cell = matriceCells[j][y];
						actualX = j;
						actualY = y;
					}
					if(matriceCells[j][y].equals(finishCell) && cellToGo == null){
						cellToGo = matriceCells[j][y];
						xToGo =j;
						yToGo =y;
					}
				}
			}
			boolean searching = true;
			int compt =0;
			while(searching){
				int rdm = new Random().nextInt(4);
				switch(rdm){
				case 0:{
					if(actualX-1<0){
						break;
					}
					if(matriceCells[actualX-1][actualY] == null){
						break;
					}
					if(matriceCells[actualX-1][actualY].isPath() && !matriceCells[actualX-1][actualY].isPathUsed()){
						System.out.println("ici");
						path.add(matriceCells[actualX-1][actualY]);
						matriceCells[actualX-1][actualY].setPathUsed(true);
						actualX--;
					}
					break;
				}
				case 1:{
					if(actualX+1>size){
						break;
					}
					if(matriceCells[actualX+1][actualY] == null){
						break;
					}
					if(matriceCells[actualX+1][actualY].isPath() && !matriceCells[actualX+1][actualY].isPathUsed()){
						path.add(matriceCells[actualX+1][actualY]);
						matriceCells[actualX+1][actualY].setPathUsed(true);
						actualX++;
					}
					break;
				}
				case 2:{
					if(actualY-1<0){
						break;
					}
					if(matriceCells[actualX][actualY-1] == null){
						break;
					}
					if(matriceCells[actualX][actualY-1].isPath() && !matriceCells[actualX][actualY-1].isPathUsed()){
						path.add(matriceCells[actualX][actualY-1]);
						matriceCells[actualX][actualY-1].setPathUsed(true);
						actualY--;
					}
					break;
				}
				case 3:{
					if(actualY+1>size){
						break;
					}
					if(matriceCells[actualX][actualY+1] == null){
						break;
					}
					if(matriceCells[actualX][actualY+1].isPath() && !matriceCells[actualX][actualY+1].isPathUsed()){
						path.add(matriceCells[actualX][actualY+1]);
						matriceCells[actualX][actualY+1].setPathUsed(true);
						actualY++;
					}
					break;
				}
				default :{
					break;
				}
				}
				if(compt >500){
					searching = false;
					break;
				}
				compt++;
				if(actualX == xToGo && actualY == yToGo){
					searching = false;
				}
			}
			lstPathFound.add(path.toArray(new Cell[path.size()]));
			for(Cell inPath : path){
				inPath.setPathUsed(false);
			}
			System.out.println("i");
		}
		Cell[] bestPath = null;
		for(Cell[] inLstPathFound : lstPathFound){
			if(bestPath == null){
				bestPath = inLstPathFound;
			}
			else{
				if(inLstPathFound.length>bestPath.length){
					bestPath = inLstPathFound;
				}
			}
		}
		for(Cell inBestPath : bestPath){
			gc.setFill(Color.RED);
			gc.fillRect(inBestPath.getX(), inBestPath.getY(), scale, scale);
		}
	}
}
class Loop extends AnimationTimer{

	private long lastTime = IDEALFRAMERATENS;
	private Consumer<Long> doEveryUpdate;
	private long updateGraphicsEvery;

	public Loop(long updateEveryNS, Consumer<Long> f){
		this.updateGraphicsEvery = updateEveryNS;
		this.doEveryUpdate = f;
	}

	@Override
	public void handle(long currentTime) {
		long elapsedTime = currentTime - lastTime;
		if(elapsedTime < updateGraphicsEvery){
			return;
		}
		else{
			lastTime = currentTime;
			doEveryUpdate.accept(elapsedTime);
		}
	}
	public final static long NANOSPERSECOND = 1000000000;
	public final static long IDEALFRAMERATENS = (long)(1 / 60.0 * NANOSPERSECOND);
	public void setUpdate(long update){
		this.updateGraphicsEvery = update;
	}

}
class Cell{
	private boolean path;
	private boolean pathPoint;
	private boolean pathUsed;
	private double x;
	private double y;

	private double matriceX;
	private double matriceY;

	public Cell(){

	}
	public Cell(double x,double y,double matriceX, double matriceY){
		this.x = x;
		this.y = y;
		this.matriceX =matriceX;
		this.matriceY = matriceY;
		this.pathPoint = false;
		this.pathUsed = false;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public boolean isPath() {
		return path;
	}

	public void setPath(boolean path) {
		this.path = path;
	}
	public boolean isPathPoint() {
		return pathPoint;
	}
	public void setPathPoint(boolean pathPoint) {
		this.pathPoint = pathPoint;
	}
	public boolean isPathUsed() {
		return pathUsed;
	}
	public void setPathUsed(boolean pathUsed) {
		this.pathUsed = pathUsed;
	}
	public double getMatriceX() {
		return matriceX;
	}
	public void setMatriceX(double matriceX) {
		this.matriceX = matriceX;
	}
	public double getMatriceY() {
		return matriceY;
	}
	public void setMatriceY(double matriceY) {
		this.matriceY = matriceY;
	}
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Cell x: "+getMatriceX()+" y: "+getMatriceY());
		return sb.toString();
	}
}

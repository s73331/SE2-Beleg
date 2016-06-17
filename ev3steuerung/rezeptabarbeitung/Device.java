package ev3steuerung.rezeptabarbeitung;

public abstract class Device {
	
	public boolean rotate(boolean mode,int speed, int angle){
		return true;
	}
	
	public boolean close(){
		return true;
	}
	public boolean register(){
		return true;
	}
	
	public boolean isPressed(){
		return true;
	}
	
	public boolean waitForPress(){
		return true;
	}
	
	public boolean waitForRelease(){
		return true;
	}

	public boolean forward(int speed) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
	}
	

}

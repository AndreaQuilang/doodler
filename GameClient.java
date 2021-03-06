import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;

public class GameClient extends JPanel implements Runnable, Constants {
	/**
	 * Main window
	 */
	
	/**
	 * Player position, speed etc.
	 */
	int x=10,y=10,xspeed=2,yspeed=2,prevX,prevY;
	
	/**
	 * Game timer, handler receives data from server to update game state
	 */
	Thread t=new Thread(this);
	
	/**
	 * Nice name!
	 */
	String name="Joseph";
	
	/**
	 * Player name of others
	 */
	String pname;
	
	/**
	 * Server to connect to
	 */
	String server="localhost";

	/**
	 * Flag to indicate whether this player has connected or not
	 */
	boolean connected=false;
	
	/**
	 * get a datagram socket
	 */
    DatagramSocket socket = new DatagramSocket();

	
    /**
     * Placeholder for data received from server
     */
	String serverData;
	
	/**	
	 * Offscreen image for double buffering, for some
	 * real smooth animation :)
	 */
	BufferedImage offscreen;
	int x1, y1, y2, x2;
	public static boolean isClear = false;
	public static boolean redChanged = false;
	public static boolean blueChanged = false;
	public static boolean blackChanged = false;
	private boolean receivedClear = false;
	private boolean clearItself = false;
	private boolean receivedRed = false;
	private boolean receivedBlack = false;
	private boolean receivedBlue = false;
	
	private Color colorSelected;
	
	/**
	 * Basic constructor
	 * @param server
	 * @param name
	 * @throws Exception
	 */
	public GameClient(String server,final String name) throws Exception{
		this.server=server;
		this.name=name;
		
		//set some timeout for the socket
		socket.setSoTimeout(100);
		this.setBackground(Color.BLACK);
		this.addMouseMotionListener(new MouseMotionListener() {

			public void mouseDragged(MouseEvent e) {
				x=e.getX();y=e.getY();
				if (prevX != x || prevY != y){
					send("PLAYER "+name+" "+x+" "+y);
				}
				 // Now Paint the line				
			}
			
			public void mousePressed(MouseEvent e) {
				 x1 = e.getX();
				 y1 = e.getY();
			}

			public void mouseMoved(MouseEvent me) {
				
			}
			
		});

		//tiime to play
		//t.start();		
	}
	
	public Thread getThread() {
		return this.t;
	}
	
	/**
	 * Helper method for sending data to server
	 * @param msg
	 */
	public void send(String msg){
		try{
			byte[] buf = msg.getBytes();
        	InetAddress address = InetAddress.getByName(server);
        	DatagramPacket packet = new DatagramPacket(buf, buf.length, address, PORT);
        	socket.send(packet);
        }catch(Exception e){}
		
	}
	
	/**
	 * The juicy part!
	 */
	public void run(){
		//create the buffer
		offscreen = (BufferedImage)this.createImage(640, 640);
		
		while(true){
			try{
				Thread.sleep(1);
			}catch(Exception ioe){}
						
			//Get the data from players
			byte[] buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try{
     			socket.receive(packet);
			}catch(Exception ioe){/*lazy exception handling :)*/}
			
			serverData=new String(buf);
			serverData=serverData.trim();

			//Study the following kids. 
			if (!connected && serverData.startsWith("CONNECTED")){
				connected=true;
				System.out.println("Connected.");
			}else if (!connected){
				System.out.println("Connecting..");				
				send("CONNECT "+name);
			}else if (connected && serverData.startsWith("CLEAR")) {
				receivedClear = true;
				clearPane();
			}else if (connected && serverData.startsWith("RED")) {
				receivedRed = true;
				changeColorRed(Color.RED);
			}else if (connected && serverData.startsWith("BLACK")) {
				receivedBlack = true;
				changeColorBlack(Color.BLACK);
			}else if (connected && serverData.startsWith("BLUE")) {
				receivedBlue = true;
				changeColorBlue(Color.BLUE);
			}
			else if (connected){
				//offscreen.getGraphics().clearRect(0, 0, 640, 480);
				if (serverData.startsWith("PLAYER")){
					String[] playersInfo = serverData.split(":");
					for (int i=0;i<playersInfo.length;i++){
						String[] playerInfo = playersInfo[i].split(" ");
						String pname =playerInfo[1];
						int x = Integer.parseInt(playerInfo[2]);
						int y = Integer.parseInt(playerInfo[3]);
						//draw on the offscreen image
						//offscreen.getGraphics().setColor(this.colorSelected);
						
						Graphics gd = offscreen.getGraphics();
						gd.setColor(this.colorSelected);
						gd.fillOval(x, y, 5, 5);		
						
					}
					//show the changes
					this.repaint();
					
				}			
			}			
		}
	}
	
	/**
	 * Repainting method
	 */
	public void paintComponent(Graphics g){
		g.drawImage(offscreen, 0, 0, null);
	}
	public void changeColorRed(Color c){
		this.colorSelected = c;
		if (!receivedRed) {
			send("RED " + name);
		}
		this.receivedRed = false;
	}
	public void changeColorBlue(Color c){
		this.colorSelected = c;
		if (!receivedBlue){
			send("BLUE " + name);
		}
		this.receivedBlue = false;
	}
	public void changeColorBlack(Color c){
		this.colorSelected = c;
		if (!receivedBlack) {
			send("BLACK " + name);
		}
		this.receivedBlack = false;
	}
		
	public void clearPane(){
		offscreen = (BufferedImage)this.createImage(640, 640);
		repaint();
		if (!receivedClear || clearItself) send("CLEAR " + name);
		clearItself = false;
		receivedClear = false;
	}
	
	public void setClearItself(boolean clearItself) {
		this.clearItself = clearItself;
	}
	
	class KeyHandler extends KeyAdapter{
		public void keyPressed(KeyEvent ke){
			prevX=x;prevY=y;
			switch (ke.getKeyCode()){
			case KeyEvent.VK_DOWN:y+=yspeed;break;
			case KeyEvent.VK_UP:y-=yspeed;break;
			case KeyEvent.VK_LEFT:x-=xspeed;break;
			case KeyEvent.VK_RIGHT:x+=xspeed;break;
			}
			if (prevX != x || prevY != y){
				send("PLAYER "+name+" "+x+" "+y);
			}	
		}
	}
}
   
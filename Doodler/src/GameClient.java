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
		
		this.addMouseMotionListener(new MouseMotionListener() {

			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseMoved(MouseEvent me) {
				x=me.getX();y=me.getY();
				if (prevX != x || prevY != y){
					send("PLAYER "+name+" "+x+" "+y);
				}
			}
			
		});
		//create the buffer
		offscreen=(BufferedImage)this.createImage(640, 480);

		//tiime to play
		t.start();		
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
			
			//if (!serverData.equals("")){
			//	System.out.println("Server Data:" +serverData);
			//}

			//Study the following kids. 
			if (!connected && serverData.startsWith("CONNECTED")){
				connected=true;
				System.out.println("Connected.");
			}else if (!connected){
				System.out.println("Connecting..");				
				send("CONNECT "+name);
			}else if (connected){
				offscreen.getGraphics().clearRect(0, 0, 640, 480);
				if (serverData.startsWith("PLAYER")){
					String[] playersInfo = serverData.split(":");
					for (int i=0;i<playersInfo.length;i++){
						String[] playerInfo = playersInfo[i].split(" ");
						String pname =playerInfo[1];
						int x = Integer.parseInt(playerInfo[2]);
						int y = Integer.parseInt(playerInfo[3]);
						//draw on the offscreen image
						offscreen.getGraphics().fillOval(x, y, 20, 20);
						offscreen.getGraphics().drawString(pname,x-10,y+30);					
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

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Client extends clientUI{
	private static final long serialVersionUID = 1L;
	InetAddress host;
	int port;
	Socket sc;
	ObjectOutputStream sendObj = null;
	ObjectInputStream obj = null;
	String username = null;
	List selectedRooms = new List();
	SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm:ss a");
	Client() throws IOException{
		for(int i=0;i<3;i++){
			try{
				Panel p = new Panel();
				JTextField hostIP = new JTextField(13);
				JTextField hostPort = new JTextField(6);
				hostIP.setText("localhost");hostPort.setText("1444");
				p.add(new JLabel("Enter IP: "));p.add(hostIP);
				p.add(new JLabel("PORT: "));p.add(hostPort);
				JOptionPane.showMessageDialog(null, p,"Enter credentials", JOptionPane.INFORMATION_MESSAGE);
				host = InetAddress.getByName(hostIP.getText());
				port = Integer.parseInt(hostPort.getText());
				sc = new Socket(host, port);
				break;
			}catch(UnknownHostException | ConnectException | NumberFormatException e){
				JOptionPane.showMessageDialog(null, (host + ":"+port+" is not FOUND!"), "Can't connecto to Server", JOptionPane.ERROR_MESSAGE);
				if(i==2)
					System.exit(0);
			}
		}
		sendObj = new ObjectOutputStream(sc.getOutputStream());
		obj = new ObjectInputStream(sc.getInputStream());
		boolean isUserOK = false;
		username = JOptionPane.showInputDialog(this, "Enter your user name", "Chat Application", JOptionPane.INFORMATION_MESSAGE);
		while(!isUserOK){
			if(username==null)continue;
			System.out.println(username);
			sendObj.writeObject(username);
			try {
				String respo = (String)obj.readObject();
				if(respo.equals("OK"))isUserOK = true;
				else username = JOptionPane.showInputDialog(this, "Username "+username+" already exists\n Enter another your user name", "Username Exists:-Chat Application", JOptionPane.WARNING_MESSAGE);
			} catch (ClassNotFoundException e) {e.printStackTrace();}
			sendObj.flush();
		}
		super.setTitle(username+" -- Chat App by V.Murali Krishna");
		readThread();
		createUI();
		JPanel ppp = new JPanel();
		ppp.add(new JLabel("<html>To send to a single person use <br><pre><font color = 'maroon'>[[username]]and your message</font></pre>" +
				"now your message will be sent to<br> that particular user</html>"));
		JOptionPane.showMessageDialog(null,ppp);
		addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent arg0) {}
			public void windowIconified(WindowEvent arg0) {}
			public void windowDeiconified(WindowEvent arg0) {}
			public void windowDeactivated(WindowEvent arg0) {}
			public void windowClosing(WindowEvent arg0) {
				try {
					closeClient();
				} catch (IOException e) {}
			}
			public void windowClosed(WindowEvent arg0) {}
			public void windowActivated(WindowEvent arg0) {}
		});

	}
	public void readThread(){
		Thread s = new Thread(){
			@Override
			public void run() {
				while(true){
					try {
						String command = null;
						final List response = (List)obj.readObject();
						command = response.getItem(0);
						if(command.equals("CHAT")){
							allMessages.append("["+sdfTime.format(new Date())+"] <"+response.getItem(1)+"> "+response.getItem(2)+"\n");
						}
						else if(command.equals("ONLINELIST")){
							new Thread(){
								public void run(){
									String ss = "";
									for(int i = 1; i<response.getItemCount(); i++){
										ss = ss+" @"+response.getItem(i)+"\n";
									}
									users.setText(ss);
								}
							}.start();
						}
						else if(command.equals("ROOMLIST")){
							new Thread(){
								public void run(){
									rooms.removeAll();
									rooms.setLayout(new GridLayout(response.getItemCount(),1));
									for(int i=1; i<response.getItemCount();i++){
										String rooM = response.getItem(i);
										Box pp = new Box(BoxLayout.X_AXIS);
										//pp.setLayout(new GridLayout(1,3));
										JButton btn = new JButton(rooM), join = new JButton("+"), remove = new JButton("-"),delete = new JButton("X");
										btn.setToolTipText("See who are in this room");
										join.setActionCommand(rooM);
										join.setToolTipText("Join in this room");
										remove.setActionCommand(rooM);
										remove.setToolTipText("getout from room");
										delete.setActionCommand(rooM);
										delete.setToolTipText("Delete this room");
										btn.setBackground(new Color(62,128,201));
										btn.setForeground(Color.white);
										join.setBackground(new Color(62,128,201));
										join.setForeground(Color.white);
										remove.setBackground(new Color(62,128,201));
										remove.setForeground(Color.white);
										delete.setBackground(new Color(62,128,201));
										delete.setForeground(Color.white);
										btn.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
												List clientReq = new List();
												clientReq.add("MEM_ROOM_REQ");
												clientReq.add(e.getActionCommand());
												try {
													sendObj.writeObject(clientReq);
												} catch (IOException e1) {e1.printStackTrace();}
											}
										});
										join.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
												try {
													joinInThisRoom(e.getActionCommand());
													updateRoomCheckBoxes();
												} catch (IOException e1) {e1.printStackTrace();}
											}
										});
										remove.addActionListener(new ActionListener() {
											@Override
											public void actionPerformed(ActionEvent e) {
												try{
													removeFromRoom(e.getActionCommand());
												}catch (IOException e1){e1.printStackTrace();}
											}
										});
										delete.addActionListener(new ActionListener() {
											@Override
											public void actionPerformed(ActionEvent e) {
												try{
													deleteRoom(e.getActionCommand());
												}catch (IOException e1){e1.printStackTrace();}
											}
										});
										pp.add(btn);pp.add(join);pp.add(remove);pp.add(delete);
										rooms.add(pp);
									}
									rooms.revalidate();
									rooms.repaint();
								}
							}.start();
						}
						else if(command.equals("MY_ROOMS")){
							selectedRooms.removeAll();
							
							new Thread(){
								public void run(){
									checkBox.removeAll();
									for(int i = 1; i<response.getItemCount();i++){
										JCheckBox b = new JCheckBox(response.getItem(i));
										b.addItemListener(new ItemListener() {
											@Override
											public void itemStateChanged(ItemEvent e) {
												JCheckBox z = (JCheckBox)e.getSource();
												if(z.isSelected()){
													selectedRooms.add(z.getActionCommand());
												}
												else if(!z.isSelected()){
													selectedRooms.remove(z.getActionCommand());
												}
											}
										});
										checkBox.add(b);
									}
									JButton b = new JButton("Send to these rooms");
									b.setBackground(new Color(62,128,201));
							        b.setForeground(Color.white);
									b.addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(ActionEvent arg0) {
											try {
												sendToSelectedRooms();
											} catch (IOException e) {}
										}
									});
									checkBox.add(b);
									checkBox.revalidate();
									checkBox.repaint();
								}
							}.start();
						}
						else if(command.equals("ERROR")){
							JOptionPane.showMessageDialog(null, response.getItem(1), "Error - Chat Application", JOptionPane.ERROR_MESSAGE);
						}
						else if(command.equals("WARNING")){
							JOptionPane.showMessageDialog(null, response.getItem(1), "Warning - Chat Application", JOptionPane.WARNING_MESSAGE);
						}
						else if(command.equals("INFORMATION")){
							JOptionPane.showMessageDialog(null, response.getItem(2), response.getItem(1), JOptionPane.INFORMATION_MESSAGE);
						}
					} catch (ClassNotFoundException | IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		s.start();
	}
	public void joinInThisRoom(String roomName) throws IOException{
		List clientReq = new List();
		clientReq.add("JOIN_IN_THIS_ROOM");
		clientReq.add(roomName);
		sendObj.writeObject(clientReq);
	}
	public void createRoom() throws IOException{
		String roomName = JOptionPane.showInputDialog(this, "Enter the room name", "Room name", JOptionPane.INFORMATION_MESSAGE);
		if(roomName != null){
			List clientReq = new List();
			clientReq.add("CREATE_ROOM");
			clientReq.add(roomName);
			sendObj.writeObject(clientReq);
		}
	}
	public void removeFromRoom(String room) throws IOException{
		List clientReq = new List();
		clientReq.add("REMOVE_FROM_ROOM");
		clientReq.add(room);
		sendObj.writeObject(clientReq);
	}
	public void deleteRoom(String room) throws IOException{
		String options[] = {"yes","no"};
		int n = JOptionPane.showOptionDialog(null, "<html>Are you sure want to delete <font color='blue'>"+room+"</font>?\nAll the members in the room will be\ndisconnected from this room", "confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		if(n==0){
			List clientReq = new List();
			clientReq.add("DELETE_ROOM");
			clientReq.add(room);
			sendObj.writeObject(clientReq);
		}
	}
	public void updateRoomCheckBoxes() throws IOException{
		List clientReq = new List();
		clientReq.add("MY_ROOMS");
		sendObj.writeObject(clientReq);
	}
	public void sendToSelectedRooms() throws IOException{
		String message = messageField.getText().toString();
		if(!message.equals("")){
			List clientReq = new List();
			clientReq.add("SEND_TO_SELECTED_ROOMS");
			for(int i = 0; i<selectedRooms.getItemCount();i++)clientReq.add(selectedRooms.getItem(i));
			clientReq.add(message);
			if(clientReq.getItemCount() > 2){
				sendObj.writeObject(clientReq);
				messageField.setText(null);
			}
			else
				JOptionPane.showMessageDialog(null, "Please select or join in a room", "Warning - Chat Application", JOptionPane.WARNING_MESSAGE);
		}
	}
	public void closeClient() throws IOException{
		List clientReq = new List();
		clientReq.add("EXIT");
		try {sendObj.writeObject(clientReq);} catch (IOException e) {}
		System.exit(0);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			List clientReq = new List();
			if (e.getSource() == btnSend || e.getSource() == messageField) {
				String message = messageField.getText().toString();
				if(!message.equals("")){
					if(message.startsWith("[[") && message.contains("]]")){
						clientReq.add("SEND_TO_THIS_PERSON");
						clientReq.add(message.substring(2,message.indexOf("]]")));//username to send
						clientReq.add(username); //this user name
						clientReq.add(message.substring(message.indexOf("]]")+2));//message
					}
					else{
						clientReq.add("CHAT_ALL");
						clientReq.add(message);
					}
					sendObj.writeObject(clientReq);
					messageField.setText(null);
				}
			} else if (e.getSource() == btnClose) {
				clientReq.add("EXIT");
				sendObj.writeObject(clientReq);
				System.exit(0);
			} else if(e.getSource() == createRoom){
				createRoom();
			} else if(e.getSource() == btnSendRoom){
				String message = messageField.getText().toString();
				if(!message.equals("")){
					clientReq.add("CHAT_TO_ROOMS");
					clientReq.add(message);
					sendObj.writeObject(clientReq);
					messageField.setText(null);
				}
			} else if(e.getSource() == creditsMenuItem){
				new credits();
			} else if(e.getSource() == closeMenuItem){
					closeClient();
			} else if(e.getSource() == logItem){
				SimpleDateFormat format = new SimpleDateFormat("HH-mm-ss_dd-MM-YYYY");
				String fileName = username+"_"+format.format(new Date())+".txt";
				FileWriter file = new FileWriter(fileName+".txt");
				file.write(allMessages.getText());
				file.flush();file.close();
				JOptionPane.showMessageDialog(null, "Log Successfully saved to\n"+fileName);
			} else if(e.getSource() == helpItem){
				JPanel p = new JPanel();
				p.add(new JLabel("<html>To send to a single person use <br><pre><font color = 'maroon'>[[username]]and your message</font></pre>" +
						"now your message will be sent to<br> that particular user</html>"));
				JOptionPane.showMessageDialog(null,p);
			}
		} catch (IOException e1) {}
	}
	public static void main(String args[]) throws IOException {
		new Client();
	}
	
}

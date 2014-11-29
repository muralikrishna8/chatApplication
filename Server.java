import java.awt.Color;
import java.awt.Cursor;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;


public class Server extends JFrame implements ActionListener{
	private static final long serialVersionUID = 1L;
	static HashMap<String, clientDetails> clients = new HashMap<String, clientDetails>();
	static HashMap<String, ArrayList<String>> rooms = new HashMap<String, ArrayList<String>>();// room with users in it
	static TextArea text;
	
	JMenuBar menu = new JMenuBar();
	JMenu file = new JMenu("File");
	JMenuItem save = new JMenuItem("Save log"),exit = new JMenuItem("Exit");
	@SuppressWarnings("resource")
	Server() throws IOException, ClassNotFoundException{
		super("Server -- Chat application");
		ServerSocket server = null;
		JPanel p = new JPanel();
		JTextField portT = new JTextField(6);
		p.add(new JLabel("Enter the port: "));
		portT.setText("1444");
		p.add(portT);
		int port = 1444;
		
		file.setMnemonic('F');
		save.setMnemonic('s');
		save.addActionListener(this);
		exit.setMnemonic('x');
		exit.addActionListener(this);
		file.add(save);
		file.add(new JSeparator());
		file.add(exit);
		menu.add(file);
		setJMenuBar(menu);
		
		while(true){
			try{
				JOptionPane.showMessageDialog(null,p,"PORT- Chat Application",-1);
				port = Integer.parseInt(portT.getText().toString());
				server = new ServerSocket(port);
				break;
			}catch(NumberFormatException e){
				JOptionPane.showMessageDialog(null, "Enter a integer with four digits");
			}catch (BindException e) {
				JOptionPane.showMessageDialog(null, "Port "+port+" is already in use");
			}
		}
		
		InetAddress ip = InetAddress.getByName("localhost");
		text = new TextArea(20,80);
		text.setBackground(Color.white);
		text.setEditable(false);
		text.setCursor(new Cursor(0));
		add(text);
		text.append("Server successfully running on "+ip+" with port "+port+" ... waiting for the clients...\n");
		pack();
		setResizable(false);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		while(true){
			Socket connection = server.accept();
			serveThread s = new serveThread(connection);
			s.start();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == save){
			String fileName = (String)JOptionPane.showInputDialog(null, "Enter a file name to save: ");
			if(fileName != null){
				FileWriter file;
				try {
					SimpleDateFormat format = new SimpleDateFormat("HH-mm-ss_dd-MM-YYYY");
					String newName = fileName+"_Server_"+format.format(new Date())+".txt";
					file = new FileWriter(newName);
					file.write(text.getText());
					file.flush();file.close();
					JOptionPane.showMessageDialog(null, "Log Successfully saved to\n"+newName);
				} catch (IOException e1) {e1.printStackTrace();}
				
			}
		}
		if(e.getSource() == exit){
			System.exit(0);
		}
	}
	public static void main(String args[]) throws IOException, ClassNotFoundException{
		new Server();
	}


}

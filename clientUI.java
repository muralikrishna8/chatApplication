import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

abstract class clientUI extends JFrame implements ActionListener{
	private static final long serialVersionUID = 1L;
	TextArea allMessages = new TextArea(30,10);
	TextArea users = new TextArea(30,18);
	TextField messageField =  new TextField(70);
	JPanel wholePanel = new JPanel();
	Panel bottom = new Panel(),panel = new Panel(),rightPanel = new Panel(), rooms = new Panel();
	Panel checkBox = new Panel(), onlinePeople = new Panel(), rightBottomPanel = new Panel();
	JButton btnSendRoom = new JButton("Send to all rooms"),createRoom = new JButton("Create Room");
	JButton btnSend = new JButton("Send to all"),btnClose=new JButton("Close");
	JScrollPane scroll = new JScrollPane(rooms);
	JScrollPane scrollAllMsg = new JScrollPane();
	MenuBar menuBar;
	Menu fileMenu, helpMenu;
	MenuItem closeMenuItem, creditsMenuItem, logItem, helpItem;
	public void createUI(){
		/*********** MENU **********/
		menuBar = new MenuBar();
		fileMenu = new Menu("File");
//		FileMenu.setMnemonic('F');
		logItem = new MenuItem("Save chat");
//		logItem.setMnemonic('S');
		logItem.addActionListener(this);
		//FileMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_MASK));
		closeMenuItem= new MenuItem("Exit");
//		closeMenuItem.setMnemonic('x');
		closeMenuItem.addActionListener(this);
		fileMenu.add(logItem);
//		FileMenu.add(new JSeparator());
		fileMenu.add(closeMenuItem);
		menuBar.add(fileMenu);
		
		helpMenu = new Menu("Help");
		helpItem = new MenuItem("Help");
		helpItem.addActionListener(this);
		helpMenu.add(helpItem);
		creditsMenuItem= new MenuItem("Credits");
//		creditsMenuItem.setMnemonic('c');
		creditsMenuItem.addActionListener(this);
		helpMenu.add(creditsMenuItem);
	
		menuBar.add(helpMenu);
		setMenuBar(menuBar);
		/*********** MENU **********/
		
        setLayout(new BorderLayout());
        
        Panel dd = new Panel();
        dd.setLayout(new GridLayout(2,1));
        btnSend.setBackground(new Color(62,128,201));
        btnSend.setForeground(Color.white);
        btnSend.setToolTipText("Send to all");
        btnSend.addActionListener(this);
        btnClose.addActionListener(this);
        btnClose.setCursor(new Cursor(12));
        btnClose.setToolTipText("Exit the chat application");
        btnSend.setCursor(new Cursor(12));
        btnSendRoom.setCursor(new Cursor(12));
        createRoom.setCursor(new Cursor(12));
        btnSendRoom.addActionListener(this);
        btnSendRoom.setBackground(new Color(62,128,201));
        btnSendRoom.setForeground(Color.white);
        btnSendRoom.setToolTipText("Send to all the persons who are in the rooms that you are in");
        createRoom.addActionListener(this);
        createRoom.setBackground(new Color(62,128,201));
        createRoom.setForeground(Color.white);
        createRoom.setToolTipText("Create a new room");
        messageField.addActionListener(this);
        allMessages.setEditable(false);
        allMessages.setBackground(Color.white);
        dd.add(allMessages);
        panel.add(messageField);
        panel.add(btnSend);
        panel.add(btnSendRoom);
        panel.add(createRoom);
        btnClose.setBackground(new Color(101, 151, 213));
        btnClose.setForeground(Color.white);
        panel.add(btnClose);
        bottom.setLayout(new GridLayout(2,1));
        bottom.add(panel);
        bottom.add(checkBox);
        dd.add(bottom);
        add(dd, BorderLayout.CENTER);

        rightPanel.setLayout(new GridLayout(2,1));
        onlinePeople.setLayout(new BorderLayout());
        onlinePeople.add(new JLabel("<html><h4><i><font color='green'>  People in Online</font></i></h4></html>"), BorderLayout.NORTH);
        users.setEditable(false);
        users.setForeground(Color.blue);
        users.setFont(new Font("Times New Roman", Font.ITALIC, 16));
        onlinePeople.add(users,BorderLayout.CENTER);
        rightPanel.add(onlinePeople);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        rightBottomPanel.setLayout(new BorderLayout());
        rightBottomPanel.add(new JLabel("<html><h4><i><font color='green'>Chat rooms</font></i></h4></html>"), BorderLayout.NORTH);
        rooms.setMaximumSize(new Dimension(40,100));
        rightBottomPanel.add(scroll);
        rightPanel.add(rightBottomPanel);
        add(rightPanel, BorderLayout.EAST);
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
	}
}

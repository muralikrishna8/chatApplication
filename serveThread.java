import java.awt.List;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

class serveThread implements Runnable{
	Socket connection;
	ObjectInputStream clientObj = null;
	ObjectOutputStream clientOutObj = null;
	InetAddress connectionIp;
	String username;
	clientDetails details;
	SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm:ss a");
	serveThread(Socket connection) throws IOException, ClassNotFoundException{
		this.connection = connection;
		connectionIp = connection.getInetAddress();
		clientObj = new ObjectInputStream(connection.getInputStream());
		clientOutObj = new ObjectOutputStream(connection.getOutputStream());
		boolean userOK = false;
		while(!userOK){
			username = (String)clientObj.readObject();
			if(!Server.clients.containsKey(username)){
				userOK = true;
				clientOutObj.writeObject("OK");
			}
			else
				clientOutObj.writeObject("false");
		}
		details = new clientDetails(username, clientOutObj);
		Server.clients.put(username, details);
		sendOnlinePeople();
		sendRoomsToAll();
		
	}
	public void sendToAll(String msg) throws IOException{
		List message = new List();
		message.add("CHAT");
		message.add(username);
		message.add(msg);
		for(clientDetails client : Server.clients.values()){
			client.clientOutObj.writeObject(message);
			client.clientOutObj.flush();
		}
	}
	public void sendOnlinePeople() throws IOException, ClassNotFoundException{
		List message = new List();
		message.add("ONLINELIST");
		for(clientDetails client: Server.clients.values()){
			message.add(client.username);
		}
		for(clientDetails client : Server.clients.values()){
			client.clientOutObj.writeObject(message);
			client.clientOutObj.flush();
		}
	}
	public void sendRoomsToAll() throws IOException{
		List message = new List();
		message.add("ROOMLIST");
		for(String room : Server.rooms.keySet())
			message.add(room);
		for(clientDetails client : Server.clients.values()){
			client.clientOutObj.writeObject(message);
			client.clientOutObj.flush();
		}
	}
	public void sendToTheStreams(ArrayList<ObjectOutputStream> outStreams, String msg) throws IOException{
		List message = new List();
		message.add("CHAT");
		message.add(username);
		message.add(msg);
		for(ObjectOutputStream outStream : outStreams){
			outStream.writeObject(message);
		}
	}
	public void warnOrError(String type, String message) throws IOException{
		List woe = new List();
		woe.add(type);
		woe.add(message);
		clientOutObj.writeObject(woe);
	}
	public void sendMyRooms(String username) throws IOException{
		List req = new List();
		req.add("MY_ROOMS");
		if(Server.clients.get(username)!=null){
			for(String room : Server.clients.get(username).rooms){
				req.add(room);
			}
		}
		Server.clients.get(username).clientOutObj.writeObject(req);
	}
	public void sendToPeopleInThisRoom(String room, String message) throws IOException{
		List req = new List();
		req.add("CHAT");
		req.add(username);
		req.add(message);
		if(Server.rooms.get(room)!=null){
			for(String user: Server.rooms.get(room)){
				Server.clients.get(user).clientOutObj.writeObject(req);
			}
		}
	}
	public void joinInRoom(String room) throws IOException{
		if(Server.rooms.containsKey(room)){
			ArrayList<String> users = Server.rooms.get(room);
			if(!users.contains(username)){
				users.add(username);
				Server.rooms.put(room, users);
				
				Server.clients.get(username).rooms.add(room);
				sendToPeopleInThisRoom(room,"Joined in the room ***** "+room+" *****");
			}
			else{
				warnOrError("WARNING","You are already in the room");
			}
		}
	}
	public void removeUserFromAllRooms(){
		for(String room : Server.rooms.keySet()){
			if(!Server.rooms.get(room).contains(username))continue;
			ArrayList<String> temp = new ArrayList<String>();
			temp = Server.rooms.get(room);
			temp.remove(username);
			Server.rooms.put(room, temp);
		}
	}
	public void removeMeFromRoom(String room) throws IOException{
		ArrayList<String> temp = new ArrayList<String>();
		if(Server.rooms.get(room).contains(username)){
			temp = Server.rooms.get(room);
			temp.remove(username);
			Server.rooms.put(room, temp);
		}
		if(Server.clients.get(username).rooms.contains(room)){
			Server.clients.get(username).rooms.remove(room);
		}
		sendMyRooms(username);
	}
	public String time(){
		return sdfTime.format(new Date());
	}
	public void run(){		
		Server.text.append("["+time()+"] :: got connection from client : "+username+connectionIp+"\n");
		try {sendToAll("has joined");} catch (IOException e1) {}
		try{ // if the user closes the window for catching this error...
			while(!connection.isClosed()){
				String command = null;
				try {
					List clientReq = null;
					clientReq = (List)clientObj.readObject();
					command = clientReq.getItem(0);
					
					if (command.equals("CHAT_ALL")) {
						sendToAll(clientReq.getItem(1));
					}
					else if(command.equals("SEND_TO_THIS_PERSON")){
						if(Server.clients.containsKey(clientReq.getItem(1))){
							List message = new List();
							message.add("CHAT");
							message.add(username);
							message.add(clientReq.getItem(3));
							Server.clients.get(clientReq.getItem(1)).clientOutObj.writeObject(message);
							Server.clients.get(clientReq.getItem(2)).clientOutObj.writeObject(message);
						}
					}
					else if(command.equals("CHAT_TO_ROOMS")){
						ArrayList<String> rooms = Server.clients.get(username).rooms;
						ArrayList<ObjectOutputStream> tempOutStreams = new ArrayList<ObjectOutputStream>();
						if(rooms.size() == 0){
							warnOrError("ERROR", "Join in a room first");
						}
						else{
							for(String room : rooms){
								ArrayList<String> users = Server.rooms.get(room);
								for(String user : users){
									if(!tempOutStreams.contains(Server.clients.get(user).clientOutObj)){
										tempOutStreams.add(Server.clients.get(user).clientOutObj);
									}
								}
							}
						}
						sendToTheStreams(tempOutStreams, clientReq.getItem(1));
					}
					else if(command.equals("CREATE_ROOM")){
						String roomName = clientReq.getItem(1);
						if(!Server.rooms.containsKey(roomName)){
							Server.text.append("["+time()+"] :: "+roomName+" (room)Added successfully by @"+username+"\n");
							sendToAll("Created the room ***** "+roomName+" *****");
							Server.rooms.put(roomName, new ArrayList<String>());
							Server.clients.get(username).ownerRoom.add(roomName);
							joinInRoom(roomName);
							sendRoomsToAll();
							sendMyRooms(username);
						}
						else{
							warnOrError("ERROR",roomName+"--Room already exists");
						}
					}
					else if(command.equals("JOIN_IN_THIS_ROOM")){
						String roomName = clientReq.getItem(1);
						joinInRoom(roomName);
					}
					else if(command.equals("MY_ROOMS")){
						sendMyRooms(username);
					}
					else if(command.equals("REMOVE_FROM_ROOM")){
						sendToPeopleInThisRoom(username,"Left the room ***** "+username+" *****");
						removeMeFromRoom(clientReq.getItem(1));// room
					}
					else if(command.equals("DELETE_ROOM")){
						String room = clientReq.getItem(1);
						if(Server.clients.get(username).ownerRoom.contains(room)){
							for(String user : Server.rooms.get(room)){
								Server.clients.get(user).rooms.remove(room);
								sendMyRooms(user);
							}
							Server.rooms.remove(room);
							Server.clients.get(username).ownerRoom.remove(room);
							sendRoomsToAll();
							sendToAll("The room ****** "+room+" ***** has been deleted by @"+ username);
						}else{
							warnOrError("ERROR", "You are not the owner of this room");
						}
					}
					else if(command.equals("MEM_ROOM_REQ")){
						String roomName = clientReq.getItem(1);
						String reply = "";
						for(String user : Server.rooms.get(roomName)){
							reply = reply + user + "\n";
						}
						List woe = new List();
						woe.add("INFORMATION");
						woe.add("Members of "+roomName+" (room)");
						woe.add(reply);
						clientOutObj.writeObject(woe);
					}
					else if(command.equals("SEND_TO_SELECTED_ROOMS")){
						ArrayList<ObjectOutputStream> tempOutStreams = new ArrayList<ObjectOutputStream>();
						for(int i = 1; i<clientReq.getItemCount()-1;i++){
							String room = clientReq.getItem(i);
							ArrayList<String> users = Server.rooms.get(room);
							for(String user : users){
								if(!tempOutStreams.contains(Server.clients.get(user).clientOutObj)){
									tempOutStreams.add(Server.clients.get(user).clientOutObj);
								}
							}
						}
						sendToTheStreams(tempOutStreams, clientReq.getItem(clientReq.getItemCount()-1));
					}
					/* This is for EXIT command */
					else if (command.equals("EXIT")) {
						sendToAll("("+ connectionIp + ") is now offline....");
						Server.text.append("["+time()+"] :: "+username + "("+ connectionIp + ") is closed\n");
						removeUserFromAllRooms();
						Server.clients.remove(username);
						sendOnlinePeople();
						connection.close();
					}
				} catch (IOException | ClassNotFoundException e) {}
			}
		}catch(NullPointerException e){
			e.printStackTrace();
			Server.text.append("["+time()+"] :: "+username+"("+connectionIp + ") closed the window.....\n");
			removeUserFromAllRooms();
			Server.clients.remove(username);
			try {sendOnlinePeople();} catch (ClassNotFoundException | IOException e1) {}
		}		
		
	}
	public void start(){
		Thread t = new Thread(this);
		t.start();
	}
}

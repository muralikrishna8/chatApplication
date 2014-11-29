import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class clientDetails {
	String username = null;
	ObjectOutputStream clientOutObj = null;
	ArrayList<String> rooms = new ArrayList<String>();
	ArrayList<String> ownerRoom = new ArrayList<String>();
	clientDetails(String username, ObjectOutputStream clientOutObj){
		this.username = username;
		this.clientOutObj = clientOutObj;
	}
}

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class credits extends JFrame {

	private static final long serialVersionUID = 1L;

	credits() {
		super("Credits -- Chat Application");
		String credit = "<html><h1><u>Chat Application</u>" +
				"</h1><br><center>Created and developed by <br><br>" +
				"<font color='blue'>V.Murali Krishna (B091444)</font><br><br>" +
				"<u><font color='#5d8fec'>vmurali1444@gmail.com</font></u></center></html>";
		JPanel pan = new JPanel();
		JLabel lab = new JLabel(credit);
		pan.add(lab);
		add(pan);
		setSize(300, 200);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}
}

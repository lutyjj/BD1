import javax.swing.*;

public class App {
	public static void main(String[] args) {
		/* Set OS default look and feel */
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* Start app */
		new MenuView();
	}
}

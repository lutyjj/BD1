import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MenuView extends JFrame implements ActionListener {
	Connection cn;

	JPanel contentPane = new JPanel(new GridBagLayout());

	JButton btnStudents = new JButton("Manage students");
	JButton btnGroups = new JButton("Manage groups");
	JButton btnStudentSchedule = new JButton("Generate student schedule");
	JButton btnProfessorSchedule = new JButton("Generate professor schedule");

	JMenuBar menuBar = new JMenuBar();
	JMenu fileMenu = new JMenu("File");
	JMenuItem fileExit = new JMenuItem("Exit");

	MenuView() {
		connect();
		init();
	}

	void init() {
		setPreferredSize(new Dimension(250, 200));
		setTitle("BD App");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		btnStudents.addActionListener(this);
		btnGroups.addActionListener(this);
		btnStudentSchedule.addActionListener(this);
		btnProfessorSchedule.addActionListener(this);
		fileExit.addActionListener(this);

		fileMenu.add(fileExit);
		menuBar.add(fileMenu);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 0;
		contentPane.add(btnStudents, gbc);
		gbc.gridy = 1;
		contentPane.add(btnGroups, gbc);
		gbc.gridy = 2;
		contentPane.add(btnStudentSchedule, gbc);
		gbc.gridy = 3;
		contentPane.add(btnProfessorSchedule, gbc);

		setJMenuBar(menuBar);
		setContentPane(contentPane);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/* Connects with database */
	void connect() {
		try {
			String user = JOptionPane.showInputDialog(this, "Enter DB user:",
					"User", JOptionPane.QUESTION_MESSAGE);
			String pass = JOptionPane.showInputDialog(this, "Enter DB password:",
					"Password", JOptionPane.QUESTION_MESSAGE);
			cn = DriverManager.getConnection(
					"jdbc:oracle:thin:@localhost:1521:xe", user, pass);
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		Object e = actionEvent.getSource();

		if (e == fileExit)
			System.exit(0);
		else if (e == btnStudents)
			new StudentsView(cn);
		else if (e == btnGroups)
			new GroupsView(cn);
		else if (e == btnStudentSchedule)
			new StudentChooser(cn);
		else if (e == btnProfessorSchedule)
			new ProfessorChooser(cn);
	}
}

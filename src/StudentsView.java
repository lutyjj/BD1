import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class StudentsView extends JFrame implements ActionListener {
	Connection cn;

	JPanel contentPane = new JPanel(new GridBagLayout());

	JTable tblStudent = new JTable();
	DefaultTableModel tblModel = new DefaultTableModel() {
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};
	JScrollPane scrollPane = new JScrollPane();

	JPanel btnPanel = new JPanel();
	JButton btnAddStudent;
	JButton btnDeleteStudent;

	StudentsView(Connection cn) {
		this.cn = cn;
		init();
	}

	void init() {
		setWindow();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		setTableColumns();
		tblStudent.setModel(tblModel);
		scrollPane.setViewportView(tblStudent);

		setButtons();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		contentPane.add(scrollPane, gbc);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 4, 0);
		gbc.gridy = 1;
		contentPane.add(btnPanel, gbc);

		updateTable();
		setContentPane(contentPane);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	void setWindow() {
		setSize(new Dimension(600, 500));
		setTitle("Student Management");
	}

	void setTableColumns() {
		tblModel.setColumnIdentifiers(
				new String[]{"Student ID", "First name", "Last name",
						"Faculty", "Field", "Starting Date"});

		scrollPane.setBorder(BorderFactory.createTitledBorder("Students"));
	}

	void setButtons() {
		btnAddStudent = new JButton("Add student");
		btnDeleteStudent = new JButton("Delete student");
		btnAddStudent.addActionListener(this);
		btnDeleteStudent.addActionListener(this);

		btnPanel.add(btnAddStudent);
		btnPanel.add(btnDeleteStudent);
	}

	void addStudent() {
		/* Get all student info from dialog */
		Object[] studentInfo = new StudentDialog(this, cn).getStudentInfo();

		if (studentInfo != null) {
			/* Insert info into DB via query */
			String sb = "insert into students" +
					"(STUDENT_ID, FIRST_NAME, LAST_NAME, FACULTY_ID, FIELD_NAME, STARTING_DATE) " +
					"VALUES ( " +
					"?, ?, ?, ?, ?, ?" +
					")";
			try (PreparedStatement st = cn.prepareStatement(sb)) {
				st.setString(1, (String) studentInfo[0]);
				st.setString(2, (String) studentInfo[1]);
				st.setString(3, (String) studentInfo[2]);
				st.setString(4, (String) studentInfo[3]);
				st.setString(5, (String) studentInfo[4]);
				java.util.Date d = (java.util.Date) studentInfo[5];
				java.sql.Date sd = new java.sql.Date(d.getTime());
				st.setDate(6, sd);
				st.executeQuery();
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	void deleteStudent() {
		/* Get selected student and delete it from DB via query */
		int row = tblStudent.getSelectedRow();
		if (row >= 0) {
			/* Column 0 - Student ID */
			Object idToDelete = tblModel.getValueAt(row, 0);
			try (Statement st = cn.createStatement()) {
				/* First, delete all occurrences in records */
				st.executeQuery("delete from RECORDS where STUDENT_ID = " + idToDelete);
				/* Second, delete the student itself */
				st.executeQuery("update STUDENTS set FACULTY_ID = null where STUDENT_ID = " + idToDelete);
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	void updateTable() {
		/* Reset table */
		this.tblModel.setRowCount(0);

		try (Statement st = cn.createStatement()) {
			/* Get all students via outer query */
			ResultSet rs = st.executeQuery("select * from students order by STUDENT_ID");
			/* If there are students in DB, insert them into table view */
			while (rs.next()) {
				/* Get all student info */
				String id = Integer.toString(rs.getInt("STUDENT_ID"));
				String first_name = rs.getString("FIRST_NAME");
				String last_name = rs.getString("LAST_NAME");
				String faculty_id = rs.getString("FACULTY_ID");
				String field_name = rs.getString("FIELD_NAME");
				java.sql.Date starting_date = rs.getDate("STARTING_DATE");
				if (faculty_id == null)
					faculty_id = "Not studying";
				if (field_name == null)
					field_name = "Not specified";

				tblModel.addRow(new String[]{id, first_name, last_name, faculty_id, field_name, starting_date.toString()});
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		Object e = actionEvent.getSource();
		if (e == btnAddStudent)
			addStudent();
		else if (e == btnDeleteStudent)
			deleteStudent();
		updateTable();
	}
}

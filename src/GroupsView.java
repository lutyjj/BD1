import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.DayOfWeek;

public class GroupsView extends JFrame implements ActionListener {
	Connection cn;

	JPanel contentPane = new JPanel(new GridBagLayout());

	JTable tblGroups = new JTable();
	DefaultTableModel tblModel = new DefaultTableModel() {
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};
	JScrollPane scrollPane = new JScrollPane();

	JPanel btnPanel = new JPanel();
	JButton btnAddGroup = new JButton("Add group");
	JButton btnDltGroup = new JButton("Delete group");
	JButton btnEditStudents = new JButton("Manage students");

	GroupsView(Connection cn) {
		this.cn = cn;
		init();
	}

	void init() {
		setSize(new Dimension(800, 600));
		setTitle("Groups management");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		tblModel.setColumnIdentifiers(
				new String[]{"Group ID", "Subject ID", "Form",
						"Student limit", "Professor", "Week parity", "Day", "Time"});

		tblGroups.setModel(tblModel);
		scrollPane.setViewportView(tblGroups);

		btnAddGroup.addActionListener(this);
		btnDltGroup.addActionListener(this);
		btnEditStudents.addActionListener(this);

		btnPanel.add(btnAddGroup);
		btnPanel.add(btnEditStudents);
		btnPanel.add(btnDltGroup);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		scrollPane.setBorder(BorderFactory.createTitledBorder("Groups"));
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

	void addGroup() {
		/* Get all group info from dialog */
		Object[] groupInfo = new GroupDialog(this, cn).getGroupInfo();

		if (groupInfo != null) {
			/* Insert info into DB via query */
			String sb = "insert into GROUPS" +
					"(GROUP_ID, SUBJECT_ID, PROFESSOR_ID, SUBJECT_FORM, DAY, TIME, PARITY, STUDENT_LIMIT) " +
					"VALUES ( " +
					"?, ?, ?, ?, ?, ?, ?, ?" +
					")";
			try (PreparedStatement st = cn.prepareStatement(sb)) {
				st.setInt(1, (int) groupInfo[0]);
				st.setInt(2, (int) groupInfo[1]);
				st.setInt(3, (int) groupInfo[2]);
				st.setString(4, (String) groupInfo[3]);
				DayOfWeek day = (DayOfWeek) groupInfo[4];
				st.setInt(5, day.getValue());
				st.setFloat(6, (float) groupInfo[5]);
				st.setString(7, (String) groupInfo[6]);
				st.setInt(8, (int) groupInfo[7]);
				st.executeQuery();
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	void deleteGroup() {
		/* Get selected group and delete it from DB via query */
		int row = tblGroups.getSelectedRow();
		if (row >= 0) {
			/* Column 0 - Group ID */
			Object idToDelete = tblModel.getValueAt(row, 0);
			try (Statement st = cn.createStatement()) {
				/* First, delete all occurrences in records */
				st.executeQuery("delete from RECORDS where GROUP_ID = " + idToDelete);
				/* Second, delete the group itself */
				st.executeQuery("delete from GROUPS where GROUP_ID = " + idToDelete);
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	void editGroup() {
		/* Get selected group and open edit view for it */
		int row = tblGroups.getSelectedRow();
		if (row >= 0) {
			int group_id = (int) tblModel.getValueAt(row, 0);
			new GroupStudentView(cn, group_id);
		}
	}

	void updateTable() {
		/* Reset table */
		this.tblModel.setRowCount(0);

		try (Statement st = cn.createStatement()) {
			/* Get all groups via outer query */
			ResultSet rs = st.executeQuery("select * from GROUPS order by GROUP_ID");
			/* If there are groups in DB, insert them into table view */
			while (rs.next()) {
				/* Get all group info */
				int group_id = rs.getInt("GROUP_ID");
				String subject_form = rs.getString("SUBJECT_FORM");
				int day = rs.getInt("DAY");
				float time = rs.getFloat("TIME");
				String parity = rs.getString("PARITY");
				int student_limit = rs.getInt("STUDENT_LIMIT");

				/* Get professor first and last name via inner query */
				int professor_id = rs.getInt("PROFESSOR_ID");
				String professor = "None";

				/* Get subject name via inner query */
				int subject_id = rs.getInt("SUBJECT_ID");
				String subject = "None";

				try (Statement id_st = cn.createStatement()) {
					ResultSet id_rs = id_st.executeQuery("select LAST_NAME, FIRST_NAME " +
							"from PROFESSORS where PROFESSOR_ID = " + professor_id);
					if (id_rs.next())
						professor = id_rs.getString("LAST_NAME") + " " +
								id_rs.getString("FIRST_NAME");

					id_rs = id_st.executeQuery("select SUBJECT_NAME " +
							"from SUBJECTS where SUBJECT_ID = " + subject_id);
					if (id_rs.next())
						subject = id_rs.getString(1);
				}

				tblModel.addRow(new Object[]{group_id, subject, subject_form, student_limit, professor, parity, day, time});
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		Object e = actionEvent.getSource();

		if (e == btnAddGroup)
			addGroup();
		else if (e == btnDltGroup)
			deleteGroup();
		else if (e == btnEditStudents)
			editGroup();
		updateTable();
	}
}

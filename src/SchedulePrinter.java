import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SchedulePrinter extends StudentsView implements ActionListener {
	SchedulePrinter(Connection cn) {
		super(cn);
	}

	public static void printSchedule(Connection cn, ResultSet rs) {
		(new SchedulePrinter(cn)).printSchedule(rs);
	}

	@Override
	void setWindow() {
		setSize(new Dimension(600, 500));
		setTitle("Schedule");
	}

	@Override
	void setTableColumns() {
		tblModel.setColumnIdentifiers(
				new String[]{"Group ID", "Subject ID", "Professor ID",
						"Parity", "Day", "Time", "Subject form", "Student limit"});

		scrollPane.setBorder(BorderFactory.createTitledBorder("Schedule"));
	}

	@Override
	void setButtons() {
		btnAddStudent = new JButton("OK");
		btnAddStudent.addActionListener(this);
		btnPanel.add(btnAddStudent);
	}

	void printSchedule(ResultSet rs) {
		this.tblModel.setRowCount(0);

		try {
			while (rs.next()) {
				String group_id = Integer.toString(rs.getInt("GROUP_ID"));
				String subj_id = rs.getString("SUBJECT_ID");
				String prof_id = rs.getString("PROFESSOR_ID");
				String parity = rs.getString("PARITY");
				String day = rs.getString("DAY");
				String time = rs.getString("TIME");
				String subj_form = rs.getString("SUBJECT_FORM");
				String stud_lim = rs.getString("STUDENT_LIMIT");

				tblModel.addRow(new String[]{group_id, subj_id, prof_id, parity, day, time, subj_form, stud_lim});
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		Object e = actionEvent.getSource();
		if (e == btnAddStudent) {
			dispose();
		}
	}
}

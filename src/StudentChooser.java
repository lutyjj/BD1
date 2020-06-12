import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StudentChooser extends StudentsView implements ActionListener {
	StudentChooser(Connection cn) {
		super(cn);
	}

	@Override
	void setButtons() {
		btnAddStudent = new JButton("OK");
		btnAddStudent.addActionListener(this);

		btnPanel.add(btnAddStudent);
	}

	@Override
	void setWindow() {
		setSize(new Dimension(600, 500));
		setTitle("Choose student");
	}

	void select() {
		int row = tblStudent.getSelectedRow();

		if (row >= 0) {
			Object studentId = tblModel.getValueAt(row, 0);

			try (Statement st = cn.createStatement()) {

				ResultSet rs = st.executeQuery("select * from GROUPS join RECORDS " +
						"on GROUPS.GROUP_ID = RECORDS.group_id " +
						"where RECORDS.STUDENT_ID = " + studentId +
						" order by GROUPS.GROUP_ID");
				SchedulePrinter.printSchedule(cn, rs);
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		Object e = actionEvent.getSource();
		if (e == super.btnAddStudent)
			select();
		updateTable();
	}
}

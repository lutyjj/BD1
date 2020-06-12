import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ProfessorChooser extends StudentChooser implements ActionListener {
	ProfessorChooser(Connection cn) {
		super(cn);
	}

	@Override
	void setWindow() {
		setSize(new Dimension(600, 500));
		setTitle("Choose professor");
	}

	@Override
	void setTableColumns() {
		tblModel.setColumnIdentifiers(
				new String[]{"Professor ID", "First name", "Last name",
						"Faculty ID", "Department"});

		scrollPane.setBorder(BorderFactory.createTitledBorder("Professors"));
	}

	@Override
	void select() {
		int row = tblStudent.getSelectedRow();

		if (row >= 0) {
			Object profId = tblModel.getValueAt(row, 0);

			try (Statement st = cn.createStatement()) {

				ResultSet rs = st.executeQuery("select * from GROUPS where PROFESSOR_ID = "
						+ profId + " order by DAY, TIME, PARITY");
				SchedulePrinter.printSchedule(cn, rs);
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	void updateTable() {
		/* Reset table */
		this.tblModel.setRowCount(0);

		try (Statement st = cn.createStatement()) {
			ResultSet rs = st.executeQuery("select * from professors order by PROFESSOR_ID");
			while (rs.next()) {
				String id = Integer.toString(rs.getInt("PROFESSOR_ID"));
				String first_name = rs.getString("FIRST_NAME");
				String last_name = rs.getString("LAST_NAME");
				String faculty_id = rs.getString("FACULTY_ID");
				String department_name = rs.getString("DEPARTMENT_NAME");

				tblModel.addRow(new String[]{id, first_name, last_name, faculty_id, department_name});
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}

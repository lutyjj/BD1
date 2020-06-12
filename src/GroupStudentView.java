import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;

public class GroupStudentView extends StudentsView {
	int group_id;

	GroupStudentView(Connection cn, int group_id) {
		super(cn);
		this.group_id = group_id;
		updateTable();
	}

	@Override
	void addStudent() {
		/* Insert student by index via query */
		try (Statement st = cn.createStatement()) {
			/* Calculate the amount of students in group via query*/
			ResultSet rs = st.executeQuery("select count(student_id) from records where group_id = " + group_id + " group by group_id");

			/* If there's students, check if we're not passing the limit of students in group,
			 * else (group is empty) - add new student */
			if (rs.next()) {
				int studentCount = rs.getInt(1);
				/* Get group student limit */
				rs = st.executeQuery("select STUDENT_LIMIT from GROUPS where GROUP_ID = " + group_id);
				if (rs.next())
					/* Check if we're not passing the limit */
					if (studentCount >= rs.getInt(1))
						throw new Exception("Too many students");
					else processStudent();
			} else processStudent();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void processStudent() throws SQLException {
		/* Get all available students from faculty where subject is taken */
		Statement st = cn.createStatement();
		ResultSet rs = st.executeQuery("select student_id, students.first_name, students.last_name from students " +
				"where faculty_id = (select faculty_id from subjects where subject_id = (select subject_id from " +
				"groups where group_id = " + group_id + "))");

		/* Store student names and student ids in separate arrays */
		ArrayList<String> students = new ArrayList<>();
		ArrayList<Integer> studentsId = new ArrayList<>();

		while (rs.next()) {
			studentsId.add(rs.getInt(1));
			students.add(rs.getString(2) + " " + rs.getString(3));
		}

		/* Show dialog with student names */
		String student = (String) JOptionPane.showInputDialog(
				this, "Select student:", "Add new student",
				JOptionPane.PLAIN_MESSAGE, null, students.toArray(), 0);

		/* Get array index of selected student */
		int studentIndex = students.indexOf(student);
		/* If no student was selected, abort */
		if (studentIndex < 0) return;

		/* Get student id based on index */
		int student_id = studentsId.get(studentIndex);

		/* Prepare statement to tie student to a group*/
		String sb = "insert into records" +
				"(RECORD_DATE, GROUP_ID, STUDENT_ID)" +
				"VALUES ( " +
				"?, ?, ?" +
				")";

		PreparedStatement stmt = cn.prepareStatement(sb);
		stmt.setDate(1, new Date(new java.util.Date().getTime()));
		stmt.setInt(2, group_id);
		stmt.setInt(3, student_id);

		/* Execute query to tie student to a group */
		stmt.executeQuery();
	}

	@Override
	void deleteStudent() {
		/* Get selected student and untie it from a group via query */
		int row = tblStudent.getSelectedRow();
		if (row >= 0) {
			/* Column 0 - Student ID */
			Object idToDelete = tblModel.getValueAt(row, 0);
			try (Statement st = cn.createStatement()) {
				/* Delete all student ties to a group */
				st.executeQuery("delete from records " +
						"where STUDENT_ID = " + idToDelete + " and GROUP_ID = " + group_id);
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
			/* Get all students tied to a group via outer query */
			ResultSet rs = st.executeQuery("select * from records natural join students " +
					"where group_id = " + group_id + " order by STUDENT_ID");
			/* If there are students in result, insert them into table view */
			while (rs.next()) {
				/* Get all student info */
				String id = Integer.toString(rs.getInt("STUDENT_ID"));
				String first_name = rs.getString("FIRST_NAME");
				String last_name = rs.getString("LAST_NAME");
				String faculty_id = rs.getString("FACULTY_ID");
				String field_name = rs.getString("FIELD_NAME");
				java.sql.Date starting_date = rs.getDate("STARTING_DATE");
				if (field_name == null)
					field_name = "Not specified";

				tblModel.addRow(new String[]{id, first_name, last_name, faculty_id, field_name, starting_date.toString()});
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}

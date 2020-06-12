import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;

enum SubjectForm {
	LECTURES("Lecture"),
	EXERCISES("Exercise"),
	LABS("Lab"),
	SEMINARIES("Seminary");

	public final String formName;

	SubjectForm(String formName) {
		this.formName = formName;
	}

	@Override
	public String toString() {
		return formName;
	}
}

enum SubjectParity {
	EVEN("Even"),
	UNEVEN("Uneven");

	public final String parity;

	SubjectParity(String parity) {
		this.parity = parity;
	}

	@Override
	public String toString() {
		return parity;
	}
}

public class GroupDialog extends JDialog implements ActionListener {
	Connection cn;
	Object[] groupInfo;

	JPanel contentPane = new JPanel(new GridBagLayout());
	JPanel studentPanel = new JPanel(new GridBagLayout());
	JPanel btnPanel = new JPanel();
	JButton btnOK = new JButton("OK");
	JButton btnCancel = new JButton("Cancel");

	JLabel lblGroupId = new JLabel("Group id:");
	JLabel lblSubject = new JLabel("Subject:");
	JLabel lblProfessor = new JLabel("Professor:");
	JLabel lblSubjectForm = new JLabel("Subject form:");
	JLabel lblDay = new JLabel("Day:");
	JLabel lblTime = new JLabel("Time:");
	JLabel lblParity = new JLabel("Parity:");
	JLabel lblStudentLimit = new JLabel("Student limit: ");

	JTextField txtGroupId = new JTextField(16);
	JTextField txtTime = new JTextField(5);
	JTextField txtStudentLimit = new JTextField(3);
	JComboBox<String> cbxSubject = new JComboBox<>();
	JComboBox<String> cbxProfessor = new JComboBox<>();
	DefaultComboBoxModel<String> cbxProfessorModel = new DefaultComboBoxModel<>();
	JComboBox<SubjectForm> cbxSubjectForm = new JComboBox<>(SubjectForm.values());
	DefaultComboBoxModel<String> cbxSubjectModel = new DefaultComboBoxModel<>();
	JComboBox<SubjectParity> cbxSubjectParity = new JComboBox<>(SubjectParity.values());
	JComboBox<DayOfWeek> cbxDay = new JComboBox<>(DayOfWeek.values());

	GroupDialog(Window parent, Connection cn) {
		super();
		this.cn = cn;
		init(parent);
	}

	void init(Window parent) {
		setModal(true);
		setPreferredSize(new Dimension(300, 300));
		setResizable(false);
		setTitle("Add student");

		btnOK.addActionListener(this);
		btnCancel.addActionListener(this);

		btnPanel.add(btnOK);
		btnPanel.add(btnCancel);

		fillSubjectCbx();
		cbxSubject.setModel(cbxSubjectModel);

		fillProfessorsCbx(cbxSubject.getSelectedItem().toString());
		cbxProfessor.setModel(cbxProfessorModel);

		cbxSubject.addActionListener(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 4, 0, 4);
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		studentPanel.add(lblGroupId, gbc);
		gbc.gridy = 1;
		studentPanel.add(lblSubject, gbc);
		gbc.gridy = 2;
		studentPanel.add(lblSubjectForm, gbc);
		gbc.gridy = 3;
		studentPanel.add(lblParity, gbc);
		gbc.gridy = 4;
		studentPanel.add(lblDay, gbc);
		gbc.gridy = 5;
		studentPanel.add(lblTime, gbc);
		gbc.gridy = 6;
		studentPanel.add(lblProfessor, gbc);
		gbc.gridy = 7;
		studentPanel.add(lblStudentLimit, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		studentPanel.add(txtGroupId, gbc);
		gbc.gridy = 1;
		studentPanel.add(cbxSubject, gbc);
		gbc.gridy = 2;
		studentPanel.add(cbxSubjectForm, gbc);
		gbc.gridy = 3;
		studentPanel.add(cbxSubjectParity, gbc);
		gbc.gridy = 4;
		studentPanel.add(cbxDay, gbc);
		gbc.gridy = 5;
		studentPanel.add(txtTime, gbc);
		gbc.gridy = 6;
		studentPanel.add(cbxProfessor, gbc);
		gbc.gridy = 7;
		studentPanel.add(txtStudentLimit, gbc);

		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		studentPanel.setBorder(BorderFactory.createTitledBorder("Student info"));
		contentPane.add(studentPanel, gbc);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 4, 0);
		gbc.gridy = 1;
		contentPane.add(btnPanel, gbc);

		getRootPane().setDefaultButton(btnOK);
		setContentPane(contentPane);
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	private void onOK() {
		try {
			/* Collect all group info */
			int group_id = Integer.parseInt(txtGroupId.getText());
			int professor_id = getProfessorId();
			int subject = getSubjectId();
			String subject_form = cbxSubjectForm.getSelectedItem().toString();
			DayOfWeek dayOfWeek = (DayOfWeek) cbxDay.getSelectedItem();
			float time = Float.parseFloat(txtTime.getText());
			String parity = cbxSubjectParity.getSelectedItem().toString();
			int student_limit = Integer.parseInt(txtStudentLimit.getText());

			/* Store in accessible object */
			groupInfo = new Object[]{group_id, subject, professor_id, subject_form, dayOfWeek, time, parity, student_limit};
			dispose();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void onCancel() {
		dispose();
	}

	private void fillSubjectCbx() {
		/* Fill subject list with available ones via query */
		try (Statement st = cn.createStatement()) {
			/* Clear previous list */
			cbxSubjectModel.removeAllElements();

			ResultSet rs = st.executeQuery("select distinct SUBJECT_NAME from SUBJECTS");
			while (rs.next())
				cbxSubjectModel.addElement(rs.getString(1));
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void fillProfessorsCbx(String subject) {
		/* Fill professors list with available ones via query */
		try (Statement st = cn.createStatement()) {
			/* Clear previous list */
			cbxProfessorModel.removeAllElements();

			ResultSet rs = st.executeQuery("select last_name from professors " +
					"inner join subjects on " +
					"subjects.faculty_id = professors.faculty_id " +
					"where subject_name = '" + subject + "'");
			while (rs.next())
				cbxProfessorModel.addElement(rs.getString(1));
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private int getProfessorId() {
		/* Get selected professor */
		String professor = cbxProfessor.getSelectedItem().toString();
		System.out.println(professor);
		/* Get professor id by professor last name via query */
		try (Statement st = cn.createStatement()) {
			ResultSet rs = st.executeQuery("select PROFESSOR_ID from PROFESSORS where LAST_NAME = '" + professor + "'");
			if (rs.next())
				return rs.getInt(1);
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}

		/* Return -1 if not found */
		return -1;
	}

	private int getSubjectId() {
		/* Get selected subject name */
		String subject_name = cbxSubject.getSelectedItem().toString();
		int professor_id = getProfessorId();

		/* Get subject id by subject name via query */
		try (Statement st = cn.createStatement()) {
			ResultSet rs = st.executeQuery("select SUBJECT_ID from SUBJECTS " +
					"join professors on " +
					"subjects.faculty_id = professors.faculty_id " +
					"where professors.professor_id = " + professor_id + " " +
					"and subjects.subject_name = '" + subject_name + "'");
			if (rs.next())
				return rs.getInt(1);
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}

		/* Return -1 if not found */
		return -1;
	}

	public Object[] getGroupInfo() {
		return this.groupInfo;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();

		if (o == btnOK) onOK();
		else if (o == btnCancel) onCancel();
		else if (o == cbxSubject) fillProfessorsCbx(cbxSubject.getSelectedItem().toString());
	}
}

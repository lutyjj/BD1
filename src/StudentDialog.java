import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class StudentDialog extends JDialog implements ActionListener {
	Connection cn;
	Object[] studentInfo;

	JPanel contentPane = new JPanel(new GridBagLayout());
	JPanel studentPanel = new JPanel(new GridBagLayout());
	JPanel btnPanel = new JPanel();
	JButton btnOK = new JButton("OK");
	JButton btnCancel = new JButton("Cancel");

	JLabel lblId = new JLabel("Student id:");
	JLabel lblFirstName = new JLabel("First name:");
	JLabel lblLastName = new JLabel("Last name:");
	JLabel lblFacultyId = new JLabel("Faculty:");
	JLabel lblField = new JLabel("Field:");
	JLabel lblStartingDate = new JLabel("Starting date:");

	JTextField txtId = new JTextField(16);
	JTextField txtFirstName = new JTextField(16);
	JTextField txtLastName = new JTextField(16);
	JTextField txtField = new JTextField(16);
	JComboBox<String> cbxFaculty = new JComboBox<>();
	DefaultComboBoxModel<String> cbxFacultyModel = new DefaultComboBoxModel<>();

	UtilDateModel model = new UtilDateModel();
	JDatePanelImpl datePanel = new JDatePanelImpl(model);
	JDatePickerImpl datePicker = new JDatePickerImpl(datePanel);

	StudentDialog(Window parent, Connection cn) {
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

		fillFacultiesCbx();
		cbxFaculty.setModel(cbxFacultyModel);

		btnPanel.add(btnOK);
		btnPanel.add(btnCancel);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 4, 0, 4);
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		studentPanel.add(lblId, gbc);
		gbc.gridy = 1;
		studentPanel.add(lblFirstName, gbc);
		gbc.gridy = 2;
		studentPanel.add(lblLastName, gbc);
		gbc.gridy = 3;
		studentPanel.add(lblFacultyId, gbc);
		gbc.gridy = 4;
		studentPanel.add(lblField, gbc);
		gbc.gridy = 5;
		studentPanel.add(lblStartingDate, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		studentPanel.add(txtId, gbc);
		gbc.gridy = 1;
		studentPanel.add(txtFirstName, gbc);
		gbc.gridy = 2;
		studentPanel.add(txtLastName, gbc);
		gbc.gridy = 3;
		studentPanel.add(cbxFaculty, gbc);
		gbc.gridy = 4;
		studentPanel.add(txtField, gbc);
		gbc.gridy = 5;
		studentPanel.add(datePicker, gbc);

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
		/* Collect all student info */
		String id = txtId.getText();
		String first_name = txtFirstName.getText();
		String last_name = txtLastName.getText();
		String faculty_id = (String) cbxFaculty.getSelectedItem();
		String field = txtField.getText();
		Date selectedDate = (Date) datePicker.getModel().getValue();

		/* Store in accessible object */
		studentInfo = new Object[]{id, first_name, last_name, faculty_id, field, selectedDate};
		dispose();
	}


	private void onCancel() {
		dispose();
	}

	void fillFacultiesCbx() {
		/* Fill faculties list with available ones via query*/
		try (Statement st = cn.createStatement()) {
			/* Clear previous list */
			cbxFacultyModel.removeAllElements();

			ResultSet rs = st.executeQuery("select distinct FACULTY_ID from FACULTIES");
			while (rs.next())
				cbxFacultyModel.addElement(rs.getString(1));
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public Object[] getStudentInfo() {
		return this.studentInfo;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		Object o = actionEvent.getSource();

		if (o == btnOK) onOK();
		else if (o == btnCancel) onCancel();
	}
}

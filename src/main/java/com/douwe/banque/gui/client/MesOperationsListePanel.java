package com.douwe.banque.gui.client;

import com.douwe.banque.data.Operation;
import com.douwe.banque.gui.common.UserInfo;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.JXDatePicker;

/**
 *
 * @author Vincent Douwe<douwevincent@yahoo.fr>
 */
public class MesOperationsListePanel extends JPanel {

    private JComboBox<String> comptes;
    private JComboBox<String> operations;
    private JXDatePicker startDate;
    private JXDatePicker endDate;
    private JTable operationTable;
    private DefaultTableModel tableModel;
    private JButton filtreBtn;
    private final String accountQuery = "select accountNumber from account where customer_id=?";
    private Connection conn;

    public MesOperationsListePanel() throws Exception {
        setLayout(new BorderLayout());
        JPanel hautPanel = new JPanel(new GridLayout(2, 1));
        JPanel pan = new JPanel(new FlowLayout(FlowLayout.CENTER));
        Label lbl;
        pan.add(lbl = new Label("LA LISTE DE MES OPERATIONS"));
        lbl.setFont(new Font("Times New Roman", Font.ITALIC, 18));
        hautPanel.add(pan);
        JPanel filtrePanel = new JPanel();
        filtrePanel.setLayout(new FlowLayout());
        filtreBtn = new JButton("Filtrer");
        comptes = new JComboBox<String>();
        operations = new JComboBox<String>();
        operations.addItem("");
        operations.addItem(Operation.credit.toString());
        operations.addItem(Operation.debit.toString());
        operations.addItem(Operation.transfer.toString());
        operations.addItem(Operation.cloture.toString());
        startDate = new JXDatePicker();
        endDate = new JXDatePicker();
        filtrePanel.add(new JLabel("Compte"));
        filtrePanel.add(comptes);
        filtrePanel.add(new JLabel("Opération"));
        filtrePanel.add(operations);
        filtrePanel.add(new JLabel("Date Début"));
        filtrePanel.add(startDate);
        filtrePanel.add(new JLabel("Date Fin"));
        filtrePanel.add(endDate);
        filtrePanel.add(filtreBtn);
        hautPanel.add(filtrePanel);
        tableModel = new DefaultTableModel(new Object[]{"Opération", "Compte", "Date", "Utilisateur", "Description"}, 0);
        operationTable = new JTable(tableModel);
        add(BorderLayout.BEFORE_FIRST_LINE, hautPanel);
        add(BorderLayout.CENTER, new JScrollPane(operationTable));
        filtreBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    String selectedCompte = (String) comptes.getSelectedItem();
                    String selectedOperation = (String) operations.getSelectedItem();
                    Date debut = startDate.getDate();
                    Date fin = endDate.getDate();
                    StringBuilder builder = new StringBuilder("select operations.*, account.accountNumber, users.username from operations, account, users where operations.account_id=account.id and operations.user_id=users.id");
                    if ((selectedCompte != null) && !("".equals(selectedCompte))) {
                        builder.append(" and accountNumber = '");
                        builder.append(selectedCompte);
                        builder.append("'");
                    }
                    if ((selectedOperation != null) && !("".equals(selectedOperation))) {
                        System.out.println("ddddd  ddd dd d "+selectedOperation);
                        int index = Operation.valueOf(selectedOperation).ordinal();
                        builder.append(" and operationType = ");
                        builder.append(index);
                    }
                    if (debut != null) {
                        builder.append(" and dateOperation >= '");
                        builder.append(debut.getTime());
                        builder.append("'");
                    }
                    if (fin != null) {
                        builder.append(" and dateOperation <= '");
                        builder.append(fin.getTime());
                        builder.append("'");
                    }
                    conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
                    PreparedStatement pStat = conn.prepareStatement(builder.toString());
                    ResultSet rs = pStat.executeQuery();
                    tableModel.setNumRows(0);
                    while (rs.next()) {                        
                        tableModel.addRow(new Object[]{Operation.values()[rs.getInt("operationType")],
                            rs.getString("accountNumber"),
                            rs.getDate("dateOperation"),
                            rs.getString("username"),
                            rs.getString("description")});
                    }
                    pStat.close();
                    conn.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Impossible de filtrer vos données");
                    Logger.getLogger(MesOperationsListePanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        comptes.addItem("");
        conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
        PreparedStatement st2 = conn.prepareStatement(accountQuery);
        st2.setInt(1, UserInfo.getCustomerId());
        ResultSet rs2 = st2.executeQuery();
        while(rs2.next()){
            comptes.addItem(rs2.getString("accountNumber"));
        }
        st2.close();
        PreparedStatement pStat = conn.prepareStatement("select operations.*, account.accountNumber, users.username from operations, account, users where operations.account_id=account.id and operations.user_id=users.id");
        ResultSet rs = pStat.executeQuery();
        while (rs.next()) {
            tableModel.addRow(new Object[]{rs.getInt("operationType"),
                rs.getString("accountNumber"),
                rs.getDate("dateOperation"),
                rs.getString("username"),
                rs.getString("description")});
        }
        pStat.close();
        conn.close();
    }
}

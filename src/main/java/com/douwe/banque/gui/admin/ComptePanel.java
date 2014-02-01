package com.douwe.banque.gui.admin;

import com.douwe.banque.data.AccountType;
import com.douwe.banque.data.Operation;
import com.douwe.banque.gui.MainMenuPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Vincent Douwe<douwevincent@yahoo.fr>
 */
public class ComptePanel extends JPanel {

    private JButton nouveauBtn;
    private JButton supprimerBtn;
    private JButton modifierBtn;
    private JButton filtreBtn;
    private JTable compteTable;
    private DefaultTableModel tableModel;
    private JTextField nameText;
    private JTextField numberText;
    private JComboBox<AccountType> type;
    private Connection conn;
    private MainMenuPanel parent;

    public ComptePanel(MainMenuPanel parentFrame) {
        try {
            this.parent = parentFrame;
            setLayout(new BorderLayout());
            JPanel haut = new JPanel();
            haut.setLayout(new FlowLayout(FlowLayout.CENTER));
            JLabel lbl;
            haut.add(lbl = new JLabel("LA LISTE DES COMPTES DE MA BANQUE POPULAIRE"));
            lbl.setFont(new Font("Times New Roman", Font.ITALIC, 18));
            add(BorderLayout.BEFORE_FIRST_LINE, haut);
            JPanel contenu = new JPanel();
            contenu.setLayout(new BorderLayout());
            JPanel bas = new JPanel();
            bas.setLayout(new FlowLayout());
            nouveauBtn = new JButton("Nouveau");
            supprimerBtn = new JButton("Supprimer");
            modifierBtn = new JButton("Modifier");
            filtreBtn = new JButton("Filtrer");
            filtreBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    try {
                        String client = nameText.getText();
                        String accountNumber = numberText.getText();
                        AccountType ty = (AccountType) type.getSelectedItem();
                        StringBuilder query = new StringBuilder("select account.*, customer.name from account, customer where customer.id = account.customer_id and account.status = ?");
                        if ((client != null) && !("".equals(client))) {
                            query.append("and name like '%");
                            query.append(client);
                            query.append("%'");
                        }
                        if ((accountNumber != null) && !("".equals(accountNumber))) {
                            query.append("and accountNumber like '%");
                            query.append(client);
                            query.append("%'");
                        }
                        if (ty != null) {
                            query.append("and type = ");
                            query.append(ty.ordinal());
                        }
                        conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
                        PreparedStatement st = conn.prepareStatement(query.toString());
                        st.setInt(1, 0);
                        ResultSet rs = st.executeQuery();
                        tableModel.setRowCount(0);
                        while (rs.next()) {
                            tableModel.addRow(new Object[]{rs.getInt("id"),
                                rs.getString("accountNumber"),
                                rs.getDouble("balance"),
                                rs.getDate("dateCreation"),
                                rs.getInt("type") == 0 ? AccountType.deposit.toString() : AccountType.saving.toString(),
                                rs.getString("name")});
                        }
                        rs.close();
                        st.close();
                        conn.close();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, "Impossible d'appliquer le filtre");
                        Logger.getLogger(ComptePanel.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            });
            nouveauBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    parent.setContenu(new NouveauComptePanel(parent));
                }
            });
            modifierBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    int selected = compteTable.getSelectedRow();
                    if (selected >= 0) {
                        parent.setContenu(new NouveauComptePanel(parent,(Integer)tableModel.getValueAt(selected, 0)));
                    } else {
                        JOptionPane.showMessageDialog(null, "Aucun compte selectionné");
                    }
                }
            });
            supprimerBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    int selected = compteTable.getSelectedRow();
                    if (selected >= 0) {
                        try {
                            String accountNumber = (String) tableModel.getValueAt(selected, 1);
                            System.out.println("fdddfdf "+accountNumber);
                            conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
                            conn.setAutoCommit(false);
                            PreparedStatement pst = conn.prepareStatement("update account set status = ? where accountNumber = ?");
                            pst.setInt(1, 1);
                            pst.setString(2, accountNumber);
                            PreparedStatement st = conn.prepareStatement("insert into operations(operationType,dateOperation,description,account_id, user_id) values(?,?,?,?,?)");
                            st.setInt(1, Operation.cloture.ordinal());
                            st.setDate(2, new Date(new java.util.Date().getTime()));
                            st.setString(3, "Cloture du compte " + accountNumber);
                            st.setInt(4, (Integer) tableModel.getValueAt(selected, 0));
                            st.setInt(5, 1);
                            st.executeUpdate();   
                            pst.executeUpdate();
                            conn.commit();
                            pst.close();
                            conn.close();
                            tableModel.removeRow(selected);
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Impossible de supprimer ce compte");
                            Logger.getLogger(ComptePanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Aucune ligne de la table n'est selectionnee");
                    }

                }
            });
            bas.add(nouveauBtn);
            bas.add(modifierBtn);
            bas.add(supprimerBtn);
            JPanel filtrePanel = new JPanel();
            filtrePanel.setLayout(new FlowLayout());
            filtrePanel.add(new JLabel("Nom Client"));
            filtrePanel.add(nameText = new JTextField());
            nameText.setPreferredSize(new Dimension(100, 25));
            filtrePanel.add(new JLabel("Numero Compte"));
            filtrePanel.add(numberText = new JTextField());
            numberText.setPreferredSize(new Dimension(100, 25));
            filtrePanel.add(new JLabel("Type Compte"));
            filtrePanel.add(type = new JComboBox<AccountType>());
            type.setPreferredSize(new Dimension(100, 25));
            type.addItem(null);
            type.addItem(AccountType.deposit);
            type.addItem(AccountType.saving);
            filtrePanel.add(filtreBtn);
            contenu.add(BorderLayout.AFTER_LAST_LINE, bas);
            contenu.add(BorderLayout.BEFORE_FIRST_LINE, filtrePanel);
            tableModel = new DefaultTableModel(new Object[]{"id", "Numero Compte", "Solde", "Date création", "Type", "Client"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            compteTable = new JTable(tableModel);
            compteTable.removeColumn(compteTable.getColumnModel().getColumn(0));
            contenu.add(BorderLayout.CENTER, new JScrollPane(compteTable));
            add(BorderLayout.CENTER, contenu);
            conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
            PreparedStatement pst = conn.prepareStatement("select account.*, customer.name from account, customer where account.customer_id = customer.id and account.status = ?");
            pst.setInt(1, 0);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getInt("id"),
                    rs.getString("accountNumber"),
                    rs.getDouble("balance"),
                    rs.getDate("dateCreation"),
                    rs.getInt("type") == 0 ? AccountType.deposit.toString() : AccountType.saving.toString(),
                    rs.getString("name")});
            }
            rs.close();
            pst.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(ComptePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

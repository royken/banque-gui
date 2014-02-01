package com.douwe.banque.gui.admin;

import com.douwe.banque.data.AccountType;
import com.douwe.banque.gui.MainMenuPanel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.BorderLayout;
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
import javax.swing.JTextField;

/**
 *
 * @author Vincent Douwe<douwevincent@yahoo.fr>
 */
public class NouveauComptePanel extends JPanel {

    private JTextField numberText;
    private JTextField balanceText;
    private JComboBox<AccountType> typeText;
    private JTextField customerText;
    private JButton btnEnregistrer;
    private int id = -1;
    private Connection conn;
    private MainMenuPanel parent;

    public NouveauComptePanel(MainMenuPanel parentFrame, int account_id) {
        this(parentFrame);
        this.id = account_id;
        if (this.id > 0) {
            btnEnregistrer.setText("Modifier");
            try {
                conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
                PreparedStatement pst = conn.prepareStatement("select account.*, customer.name  from account, customer where account.id = ? and account.customer_id = customer.id");
                pst.setInt(1, id);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    numberText.setText(rs.getString("accountNumber"));
                    balanceText.setText("" + rs.getDouble("balance"));
                    balanceText.setEnabled(false);
                    customerText.setEnabled(false);
                    customerText.setText(rs.getString("name"));
                    typeText.setSelectedItem(AccountType.values()[rs.getInt("type")]);
                }
                rs.close();
                pst.close();
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(NouveauComptePanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public NouveauComptePanel(MainMenuPanel parentFrame) {
        this.parent = parentFrame;
        setLayout(new BorderLayout(10, 10));
        JPanel haut = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel lbl;
        haut.add(lbl = new JLabel("AJOUT D'UN NOUVEAU COMPTE DANS MA BANQUE POPULAIRE"));
        lbl.setFont(new Font("Times New Roman", Font.ITALIC, 18));
        add(BorderLayout.BEFORE_FIRST_LINE, haut);
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("right:max(40dlu;p), 12dlu, 180dlu:", ""));
        builder.append("Numéro Compte", numberText = new JTextField());
        builder.append("Solde initial", balanceText = new JTextField());
        builder.append("Type de Compte", typeText = new JComboBox<AccountType>());
        typeText.addItem(AccountType.deposit);
        typeText.addItem(AccountType.saving);
        builder.append("Titulaire", customerText = new JTextField());
        builder.append(btnEnregistrer = new JButton("Enrégistrer"));
        add(BorderLayout.CENTER, builder.getPanel());
        btnEnregistrer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (id > 0) {
                    try {
                        // here I'm updating an account
                        String number = numberText.getText();
                        AccountType type = (AccountType) typeText.getSelectedItem();
                        if ((number == null) || ("".equals(number))) {
                            JOptionPane.showMessageDialog(null, "Le numéro du compte n'est pas specifie");
                            return;
                        }
                        if (type == null) {
                            JOptionPane.showMessageDialog(null, "Le type du compte n'est pas specifie");
                            return;
                        }
                        conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
                        PreparedStatement pst = conn.prepareStatement("update account set type=? , accountNumber=? where id =?");
                        pst.setInt(1, type.ordinal());
                        pst.setString(2, number);
                        pst.setInt(3, id);
                        pst.executeUpdate();
                        pst.close();
                        conn.close();
                        parent.setContenu(new ComptePanel(parent));
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, "Impossible de mettre à jour le compte");
                        Logger.getLogger(NouveauComptePanel.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else {
                    try {
                        // I need to get the customer_id
                        String customer = customerText.getText();
                        String number = numberText.getText();
                        String balanceT = balanceText.getText();
                        AccountType type = (AccountType) typeText.getSelectedItem();
                        double balance;
                        if ((customer == null) || ("".equals(customer))) {
                            JOptionPane.showMessageDialog(null, "Le nom du client n'est pas specifie");
                            return;
                        }
                        if ((number == null) || ("".equals(number))) {
                            JOptionPane.showMessageDialog(null, "Le numéro du compte n'est pas specifie");
                            return;
                        }
                        if ((balanceT == null) || ("".equals(balanceT))) {
                            JOptionPane.showMessageDialog(null, "Le solde initial du compte n'est pas specifie");
                            return;
                        }
                        if (type == null) {
                            JOptionPane.showMessageDialog(null, "Le type du compte n'est pas specifie");
                            return;
                        }
                        try {
                            balance = Double.valueOf(balanceT);
                        } catch (NumberFormatException nfe) {
                            JOptionPane.showMessageDialog(null, "Le solde compte doit être un nombre positif");
                            return;
                        }
                        if (balance < 0) {
                            JOptionPane.showMessageDialog(null, "Le solde compte doit être un nombre positif");
                            return;
                        }
                        conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
                        PreparedStatement pst = conn.prepareStatement("select id from customer where name = ?");
                        pst.setString(1, customer);
                        ResultSet rs = pst.executeQuery();
                        if (rs.next()) {
                            int customer_id = rs.getInt("id");
                            PreparedStatement st = conn.prepareStatement("insert into account(accountNumber,balance,dateCreation,type, customer_id) values(?,?,?,?,?)");
                            st.setString(1, number);
                            st.setDouble(2, balance);
                            st.setDate(3, new Date(new java.util.Date().getTime()));
                            st.setInt(4, type.ordinal());
                            st.setInt(5, customer_id);
                            st.executeUpdate();
                            st.close();
                        } else {
                            JOptionPane.showMessageDialog(null, "Le client spécifié n'existe pas");
                            return;
                        }
                        rs.close();
                        pst.close();
                        conn.close();
                        parent.setContenu(new ComptePanel(parent));
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, "Impossible d'enregistrer le compte");
                        Logger.getLogger(NouveauComptePanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }
}

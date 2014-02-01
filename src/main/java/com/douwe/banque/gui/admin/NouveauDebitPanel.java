package com.douwe.banque.gui.admin;

import com.douwe.banque.data.Operation;
import com.douwe.banque.gui.MainMenuPanel;
import com.douwe.banque.gui.common.EmptyPanel;
import com.douwe.banque.gui.common.UserInfo;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Vincent Douwe<douwevincent@yahoo.fr>
 */
public class NouveauDebitPanel extends JPanel {

    private JTextField accountText;
    private JTextField amountText;
    private JButton btnEnregistrer;
    private Connection conn;
    private MainMenuPanel parent;

    public NouveauDebitPanel(MainMenuPanel parentFrame) {
        setLayout(new BorderLayout(10, 10));
        this.parent = parentFrame;
        JPanel haut = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel lbl;
        haut.add(lbl = new JLabel("DÉBIT DE COMPTE DANS MA BANQUE POPULAIRE"));
        lbl.setFont(new Font("Times New Roman", Font.ITALIC, 18));
        add(BorderLayout.BEFORE_FIRST_LINE, haut);
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("right:max(40dlu;p), 12dlu, 180dlu:", ""));
        builder.append("Numéro Compte", accountText = new JTextField());
        builder.append("Montant", amountText = new JTextField());
        builder.append(btnEnregistrer = new JButton("Enrégistrer"));
        add(BorderLayout.CENTER, builder.getPanel());
        btnEnregistrer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    String account = accountText.getText();
                    String amountS = amountText.getText();
                    double amount;
                    if ((account == null) || ("".equals(account))) {
                        JOptionPane.showMessageDialog(null, "Le numéro de compte est obligatoire");
                        return;
                    }
                    if ((amountS == null) || ("".equals(amountS))) {
                        JOptionPane.showMessageDialog(null, "Le montant est obligatoire");
                        return;
                    }
                    try {
                        amount = Double.valueOf(amountS);
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Le montant doit etre un nombre positif");
                        return;
                    }
                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(null, "Le montant doit etre un nombre positif");
                        return;
                    }
                    conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
                    PreparedStatement pst1 = conn.prepareStatement("select * from account where accountNumber = ?");
                    pst1.setString(1, account);
                    ResultSet rs = pst1.executeQuery();
                    if (rs.next()) {
                        if (rs.getDouble("balance") <= amount) {
                            JOptionPane.showMessageDialog(null, "Le solde du compte n'est pas suffisant pour cette opération");
                        } else {
                            conn.setAutoCommit(false);
                            PreparedStatement pst2 = conn.prepareStatement("update account set balance = balance - ? where accountNumber = ?");
                            PreparedStatement pst3 = conn.prepareStatement("insert into operations(operationType, dateOperation,description,account_id, user_id) values (?,?,?,?,?)");
                            pst2.setDouble(1, amount);
                            pst2.setString(2, account);
                            pst2.executeUpdate();
                            pst3.setInt(1, Operation.debit.ordinal());
                            pst3.setDate(2, new Date(new java.util.Date().getTime()));
                            pst3.setString(3, "Debit de " + amount + " du compte " + account);
                            pst3.setInt(4, rs.getInt("id"));
                            pst3.setInt(5, UserInfo.getUserId());
                            pst3.executeUpdate();
                            conn.commit();
                            pst2.close();
                            pst3.close();
                            JOptionPane.showMessageDialog(null, "Opération réalisée avec succès");
                            parent.setContenu(EmptyPanel.emptyPanel());
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Le compte specifier n'existe pas");
                    }
                    rs.close();
                    pst1.close();
                    conn.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Impossible de procéder à l'opération de débit");
                    Logger.getLogger(NouveauDebitPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
}

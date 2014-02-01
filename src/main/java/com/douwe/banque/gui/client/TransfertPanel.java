package com.douwe.banque.gui.client;

import com.douwe.banque.data.Operation;
import com.douwe.banque.gui.common.UserInfo;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Vincent Douwe<douwevincent@yahoo.fr>
 */
public class TransfertPanel extends JPanel {

    private JComboBox<String> source;
    private JTextField destination;
    private JTextField amount;
    private JButton transferBtn;
    private Connection conn;

    public TransfertPanel() throws Exception {
        setLayout(new BorderLayout());
        JPanel pan = new JPanel(new FlowLayout(FlowLayout.CENTER));
        Label lbl;
        pan.add(lbl = new Label("NOUVEAU TRANSFERT DE COMPTE A COMPTE"));
        lbl.setFont(new Font("Times New Roman", Font.ITALIC, 18));
        add(BorderLayout.BEFORE_FIRST_LINE, pan);
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("right:max(40dlu;p), 12dlu, 180dlu:",""));
        builder.append("Compte Départ",source = new JComboBox<String>());
        builder.append("Compte Destination", destination = new JTextField());
        builder.append("Montant", amount = new JTextField());
        builder.append(transferBtn = new JButton("Transferer"));
        transferBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String init = (String) source.getSelectedItem();
                String dest = destination.getText();
                String amt = amount.getText();
                if ("".equalsIgnoreCase(init)) {
                    JOptionPane.showMessageDialog(null, "Le compte source est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
                } else if ("".equalsIgnoreCase(dest)) {
                    JOptionPane.showMessageDialog(null, "Le compte destination est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
                } else if ("".equalsIgnoreCase(amt)) {
                    JOptionPane.showMessageDialog(null, "Le montant est obligatoire", "Erreur", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        double value = Double.valueOf(amt);
                        conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
                        ResultSet rss;
                        PreparedStatement bst = conn.prepareStatement("select balance from account where accountNumber = ?");
                        bst.setString(1, init);
                        rss = bst.executeQuery();
                        if (rss.next()) {
                            double current = rss.getDouble("balance");
                            if (current >= value) {
                                PreparedStatement outst = conn.prepareStatement("select * from account where accountNumber = ?");
                                outst.setString(1, dest);
                                rss = outst.executeQuery();
                                if (rss.next()) {                                    
                                        conn.setAutoCommit(false);
                                        PreparedStatement crst = conn.prepareStatement("update account set balance = balance + ? where accountNumber = ?");
                                        PreparedStatement dtst = conn.prepareStatement("update account set balance = balance - ? where accountNumber = ?");
                                        PreparedStatement op1st = conn.prepareStatement("insert into operations (operationType, dateOperation,description,account_id,user_id) values(?,?,?,?,?)");
                                        crst.setDouble(1, value);
                                        crst.setString(2, dest);
                                        crst.executeUpdate();
                                        dtst.setDouble(1, value);
                                        dtst.setString(2, init);
                                        dtst.executeUpdate();
                                        op1st.setInt(1, Operation.credit.ordinal());
                                        op1st.setDate(2, new java.sql.Date(new Date().getTime()));
                                        op1st.setString(3, "Credit du compte " + dest + " du montant " + amt);
                                        op1st.setInt(4, rss.getInt("id"));
                                        op1st.setInt(5, UserInfo.getUserId());
                                        op1st.executeUpdate();
                                        op1st.setInt(1, Operation.debit.ordinal());
                                        op1st.setDate(2, new java.sql.Date(new Date().getTime()));
                                        op1st.setString(3, "Debit du compte " + init + " du montant " + amt);
                                        op1st.setInt(4, rss.getInt("id"));
                                        op1st.setInt(5, UserInfo.getUserId());
                                        op1st.executeUpdate();
                                        conn.commit();
                                        crst.close();
                                        dtst.close();
                                        op1st.close();
                                        JOptionPane.showMessageDialog(null, "Operation de transfert realisee avec succes");
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Le compte " + dest + " n'existe pas", "Erreur", JOptionPane.ERROR_MESSAGE);                                    
                                }
                                outst.close();
                            } else {
                                JOptionPane.showMessageDialog(null, "Le compte " + init + " ne dispose pas d'un solde suffisant", "Erreur", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        rss.close();
                        bst.close();

                    } catch (NumberFormatException ps) {
                        JOptionPane.showMessageDialog(null, "Le montant doit etre un nombre", "Erreur", JOptionPane.ERROR_MESSAGE);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, "Erreur lors du transfert", "Erreur", JOptionPane.ERROR_MESSAGE);
                        try {
                            conn.rollback();
                        } catch (SQLException ex1) {
                            Logger.getLogger(TransfertPanel.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    }
                    try {
                        conn.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(TransfertPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }


                }

            }
        });
        add(BorderLayout.CENTER, builder.getPanel());
        source.addItem("");
        conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
        PreparedStatement pst = conn.prepareStatement("select accountNumber from account where customer_id = ?");
        pst.setInt(1, UserInfo.getCustomerId());
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            source.addItem(rs.getString("accountNumber"));
        }
        pst.close();
        conn.close();
    }
}
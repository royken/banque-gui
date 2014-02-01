package com.douwe.banque.gui.admin;

import com.douwe.banque.data.RoleType;
import com.douwe.banque.gui.MainMenuPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
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
public class UtilisateurPanel extends JPanel {

    private JButton nouveauBtn;
    private JButton supprimerBtn;
    private JButton initialiserPasswdBtn;
    private JButton filtreBtn;
    private JTextField loginText;
    private JComboBox<RoleType> role;
    private JTable utilisateurTable;
    private DefaultTableModel tableModel;
    private Connection conn;
    private MainMenuPanel parent;

    public UtilisateurPanel(MainMenuPanel parentFrame) {
        try {
            setLayout(new BorderLayout());
            this.parent = parentFrame;
            JPanel haut = new JPanel();
            haut.setLayout(new FlowLayout(FlowLayout.CENTER));
            JLabel lbl;
            haut.add(lbl = new JLabel("LA LISTE DES UTILISATEURS DE MA BANQUE POPULAIRE"));
            lbl.setFont(new Font("Times New Roman", Font.ITALIC, 18));
            add(BorderLayout.BEFORE_FIRST_LINE, haut);
            JPanel contenu = new JPanel();
            contenu.setLayout(new BorderLayout());
            JPanel bas = new JPanel();
            bas.setLayout(new FlowLayout());
            nouveauBtn = new JButton("Nouveau");
            supprimerBtn = new JButton("Supprimer");
            initialiserPasswdBtn = new JButton("Reinitialiser Mot de Passe");
            filtreBtn = new JButton("Filtrer");
            filtreBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    try {
                        String username = loginText.getText();
                        RoleType roleN = (RoleType) role.getSelectedItem();
                        StringBuilder query = new StringBuilder("select * from users where status = ?");
                        if ((username != null) && !("".equals(username))) {
                            query.append("and username like '%");
                            query.append(username);
                            query.append("%'");
                        }
                        if (roleN != null) {
                            query.append("and role = ");
                            query.append(roleN.ordinal());
                        }
                        conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
                        PreparedStatement pst = conn.prepareStatement(query.toString());
                        pst.setInt(1, 0);
                        ResultSet rs = pst.executeQuery();
                        tableModel.setRowCount(0);
                        while (rs.next()) {
                            tableModel.addRow(new Object[]{rs.getInt("id"),
                                rs.getString("username"),
                                RoleType.values()[rs.getInt("role")]});
                        }
                        rs.close();
                        pst.close();
                        conn.close();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, "Impossible de filtrer vos données");
                        Logger.getLogger(UtilisateurPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            nouveauBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    parent.setContenu(new NouveauUtilisateurPanel(parent));
                }
            });
            initialiserPasswdBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    int selected = utilisateurTable.getSelectedRow();
                    if (selected >= 0) {
                        try {
                            conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
                            PreparedStatement pst = conn.prepareStatement("update users set passwd = ? where id = ?");
                            pst.setString(1, "admin");
                            pst.setInt(2, (Integer) tableModel.getValueAt(selected, 0));
                            pst.executeUpdate();
                            pst.close();
                            conn.close();
                            JOptionPane.showMessageDialog(null, "Le mot de passe est reinitialisé à 'admin'");
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Impossible de reinitialiser le mot de passe");
                            Logger.getLogger(UtilisateurPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Aucun utilisateur n'est selectionné");
                    }
                }
            });
            supprimerBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    int selected = utilisateurTable.getSelectedRow();
                    if (selected >= 0) {
                        try {
                            conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
                            PreparedStatement pst = conn.prepareStatement("update users set status = ? where id = ?");
                            pst.setInt(1, 1);
                            pst.setInt(2, (Integer) tableModel.getValueAt(selected, 0));
                            pst.executeUpdate();
                            pst.close();
                            conn.close();
                            tableModel.removeRow(selected);
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Impossible de supprimer cet utilisateur");
                            Logger.getLogger(UtilisateurPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Aucun utilisateur n'est selectionné");
                    }
                }
            });
            bas.add(nouveauBtn);
            bas.add(initialiserPasswdBtn);
            bas.add(supprimerBtn);
            JPanel filtrePanel = new JPanel();
            filtrePanel.setLayout(new FlowLayout());
            filtrePanel.add(new JLabel("Nom Client"));
            filtrePanel.add(loginText = new JTextField());
            loginText.setPreferredSize(new Dimension(100, 25));
            filtrePanel.add(new JLabel("Type Compte"));
            filtrePanel.add(role = new JComboBox<RoleType>());
            role.setPreferredSize(new Dimension(100, 25));
            role.addItem(null);
            role.addItem(RoleType.customer);
            role.addItem(RoleType.employee);
            role.addItem(RoleType.admin);
            filtrePanel.add(filtreBtn);
            contenu.add(BorderLayout.AFTER_LAST_LINE, bas);
            contenu.add(BorderLayout.BEFORE_FIRST_LINE, filtrePanel);
            tableModel = new DefaultTableModel(new Object[]{"id", "Login", "Role"}, 0);
            utilisateurTable = new JTable(tableModel);
            utilisateurTable.removeColumn(utilisateurTable.getColumnModel().getColumn(0));
            contenu.add(BorderLayout.CENTER, new JScrollPane(utilisateurTable));
            add(BorderLayout.CENTER, contenu);
            conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
            PreparedStatement pst = conn.prepareStatement("select * from users where status = ?");
            pst.setInt(1, 0);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getInt("id"),
                    rs.getString("username"),
                    RoleType.values()[rs.getInt("role")]});
            }
            pst.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(UtilisateurPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
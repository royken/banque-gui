package com.douwe.banque.gui.admin;

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
public class ClientPanel extends JPanel {

    private JButton nouveauBtn;
    private JButton supprimerBtn;
    private JButton modifierBtn;
    private JButton filtreBtn;
    private JTable clientTable;
    private DefaultTableModel tableModel;
    private JTextField nameText;
    private Connection conn;
    private MainMenuPanel parent;

    public ClientPanel(MainMenuPanel parentFrame) {
        try {
            setLayout(new BorderLayout());
            this.parent = parentFrame;
            JPanel haut = new JPanel();
            haut.setLayout(new FlowLayout(FlowLayout.CENTER));
            JLabel lbl;
            haut.add(lbl = new JLabel("LA LISTE DES CLIENTS DE MA BANQUE POPULAIRE"));
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
                    String name = nameText.getText();
                    //if ((name != null) && !("".equals(name))) {
                        try {
                            conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
                            PreparedStatement pst = conn.prepareStatement("select * from customer where status = ? and name like ?");
                            pst.setInt(1, 0);
                            pst.setString(2, "%" + name + "%");
                            ResultSet rs = pst.executeQuery();
                            tableModel.setRowCount(0);
                            while (rs.next()) {
                                tableModel.addRow(new Object[]{rs.getInt("id"),
                                    rs.getString("name"),
                                    rs.getString("emailAddress"),
                                    rs.getString("phoneNumber")
                                });
                            }
                            rs.close();
                            pst.close();
                            conn.close();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Impossible de filtrer vos données");
                            Logger.getLogger(ClientPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                //}
            });
            nouveauBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    parent.setContenu(new NouveauClientPanel(parent));
                }
            });
            modifierBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    int selected = clientTable.getSelectedRow();
                    if (selected >= 0){
                        parent.setContenu(new NouveauClientPanel(parent, (Integer)tableModel.getValueAt(selected, 0)));
                    }else{
                        JOptionPane.showMessageDialog(null, "Aucun client n'est selectionné");
                    }
                }
            });
            supprimerBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    int selected = clientTable.getSelectedRow();
                    if (selected >=0){
                        try {
                            conn = DriverManager.getConnection("jdbc:sqlite:banque.db");                            
                            PreparedStatement psmt = conn.prepareStatement("update customer set status = ? where id = ?");
                            psmt.setInt(1, 1);
                            psmt.setInt(2, (Integer)tableModel.getValueAt(selected, 0));                            
                            if(psmt.executeUpdate() > 0){
                                tableModel.removeRow(selected);
                            }
                            psmt.close();
                            conn.close();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Erreur lors de la suppression du client "+ tableModel.getValueAt(selected, 1));
                            Logger.getLogger(ClientPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }else{
                        JOptionPane.showMessageDialog(null, "Aucune donnée n'est selectionnée");
                    }
                }
            });
            bas.add(nouveauBtn);
            bas.add(modifierBtn);
            bas.add(supprimerBtn);
            JPanel filtrePanel = new JPanel();
            filtrePanel.setLayout(new FlowLayout());
            filtrePanel.add(new JLabel("Nom"));
            filtrePanel.add(nameText = new JTextField());
            nameText.setPreferredSize(new Dimension(100, 25));
            filtrePanel.add(filtreBtn);
            contenu.add(BorderLayout.AFTER_LAST_LINE, bas);
            contenu.add(BorderLayout.BEFORE_FIRST_LINE, filtrePanel);
            tableModel = new DefaultTableModel(new Object[]{"id", "Nom", "Adresse Email", "Téléphone"}, 0);
            clientTable = new JTable(tableModel);
            clientTable.removeColumn(clientTable.getColumnModel().getColumn(0));
            contenu.add(BorderLayout.CENTER, new JScrollPane(clientTable));
            add(BorderLayout.CENTER, contenu);
            conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
            PreparedStatement pst = conn.prepareStatement("select * from customer where status = ?");
            pst.setInt(1, 0);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("emailAddress"),
                    rs.getString("phoneNumber")
                });
            }
            rs.close();
            pst.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(ClientPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

package com.douwe.banque.gui.admin;

import com.douwe.banque.data.RoleType;
import com.douwe.banque.gui.MainMenuPanel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.BorderLayout;
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
import javax.swing.JTextField;

/**
 *
 * @author Vincent Douwe<douwevincent@yahoo.fr>
 */
public class NouveauClientPanel extends JPanel {

    private JTextField nameText;
    private JTextField emailText;
    private JTextField phoneText;
    private JButton btnEnregistrer;
    private int id = -1;
    private MainMenuPanel parent;
    private Connection conn;

    public NouveauClientPanel(MainMenuPanel parentFrame, int id) {
        this(parentFrame);
        this.id = id;
        if (id > 0) {
            try {
                conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
                PreparedStatement ps = conn.prepareStatement("select * from customer where id = ?");
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    nameText.setText(rs.getString("name"));
                    emailText.setText(rs.getString("emailAddress"));
                    phoneText.setText(rs.getString("phoneNumber"));
                }
                rs.close();
                ps.close();
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(NouveauClientPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public NouveauClientPanel(MainMenuPanel parentFrame) {
        this.parent = parentFrame;
        setLayout(new BorderLayout(10, 10));
        JPanel haut = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel lbl;
        haut.add(lbl = new JLabel("AJOUT D'UN NOUVEAU CLIENT DANS MA BANQUE POPULAIRE"));
        lbl.setFont(new Font("Times New Roman", Font.ITALIC, 18));
        add(BorderLayout.BEFORE_FIRST_LINE, haut);
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("right:max(40dlu;p), 12dlu, 180dlu:", ""));
        builder.append("Nom", nameText = new JTextField());
        builder.append("Adresse Email", emailText = new JTextField());
        builder.append("Numéro de Téléphone", phoneText = new JTextField());
        builder.append(btnEnregistrer = new JButton("Enrégistrer"));
        add(BorderLayout.CENTER, builder.getPanel());
        btnEnregistrer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (id <= 0) {
                    try {
                        String name = nameText.getText();
                        String email = emailText.getText();
                        String phone = phoneText.getText();
                        if ((name == null) || ("".equals(name))) {
                            JOptionPane.showMessageDialog(null, "Le nom est obligatoire");
                            return;
                        }
                        if ((email == null) || ("".equals(email))) {
                            JOptionPane.showMessageDialog(null, "L'adresse email est obligatoire");
                            return;
                        }
                        if ((phone == null) || ("".equals(phone))) {
                            JOptionPane.showMessageDialog(null, "Le numéro de téléphone est obligatoire");
                            return;
                        }
                        conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
                        conn.setAutoCommit(false);
                        PreparedStatement pst = conn.prepareStatement("insert into customer(name,emailAddress, phoneNumber, user_id) values (?,?,?,?)");
                        PreparedStatement st = conn.prepareStatement("insert into users(username, passwd, role) values (?,?,?)");
                        String val = name.replaceAll(" ", "").toLowerCase();
                        st.setString(1, val);
                        st.setString(2, "admin");
                        st.setInt(3, RoleType.customer.ordinal());
                        st.executeUpdate();
                        ResultSet rrr = st.getGeneratedKeys();
                        if (rrr.next()) {
                            pst.setString(1, name);
                            pst.setString(2, email);
                            pst.setString(3, phone);
                            pst.setInt(4, rrr.getInt(1));
                            pst.executeUpdate();
                            conn.commit();
                        } else {
                            conn.rollback();
                        }
                        rrr.close();
                        st.close();
                        pst.close();
                        conn.close();
                        JOptionPane.showMessageDialog(null, "Un compte avec login " + val + " et mot de passe 'admin' a été créé");
                        parent.setContenu(new ClientPanel(parent));
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, "Impossible de créer le compte");
                        Logger.getLogger(NouveauClientPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        String name = nameText.getText();
                        String email = emailText.getText();
                        String phone = phoneText.getText();
                        if ((name == null) || ("".equals(name))) {
                            JOptionPane.showMessageDialog(null, "Le nom est obligatoire");
                            return;
                        }
                        if ((email == null) || ("".equals(email))) {
                            JOptionPane.showMessageDialog(null, "L'adresse email est obligatoire");
                            return;
                        }
                        if ((phone == null) || ("".equals(phone))) {
                            JOptionPane.showMessageDialog(null, "Le numéro de téléphone est obligatoire");
                            return;
                        }
                        conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
                        PreparedStatement pst = conn.prepareStatement("update customer set name =?, emailAddress=?, phoneNumber=? where id = ?");
                        pst.setString(1, name);
                        pst.setString(2, email);
                        pst.setString(3, phone);
                        pst.setInt(4, id);
                        pst.executeUpdate();
                        pst.close();
                        conn.close();
                        parent.setContenu(new ClientPanel(parent));
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, "Impossible de mettre a jour votre compte");
                        Logger.getLogger(NouveauClientPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }
}
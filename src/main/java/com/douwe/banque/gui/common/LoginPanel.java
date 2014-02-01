package com.douwe.banque.gui.common;

import com.douwe.banque.data.RoleType;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Vincent Douwe<douwevincent@yahoo.fr>
 */
public class LoginPanel extends JPanel{
    
    private JTextField loginText;
    private JPasswordField passwdText;
    private JButton btnLogin;
    private Connection conn;
    
    public LoginPanel(){
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(80, 350, 80, 300));
        JPanel haut = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel lbl;
        haut.add(lbl = new JLabel("<html>Entrer login et mot de passe <br/> pour profiter des services de la banque populaire"));
        lbl.setFont(new Font("Times New Roman", Font.ITALIC, 18));
        add(BorderLayout.BEFORE_FIRST_LINE, haut);
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("right:max(40dlu;p), 12dlu, 180dlu:", ""));
        builder.append("Login", loginText = new JTextField());
        builder.append("Mot de passe", passwdText = new JPasswordField());
        builder.append(btnLogin = new JButton("Login"));
        add(BorderLayout.CENTER, builder.getPanel());
        btnLogin.addActionListener(new ActionListener() {

             public void actionPerformed(ActionEvent ae) {                 
                 try {
                     String username = loginText.getText();
                     String passwd = new String(passwdText.getPassword());
                     if((username == null) || ("".equals(username))){
                         JOptionPane.showMessageDialog(null, "Le login est obligatoire");
                         passwdText.setText("");
                         return;
                     }
                     if("".equals(passwd)){
                         JOptionPane.showMessageDialog(null, "Le mot de passe est obligatoire");
                         passwdText.setText("");
                         return;
                     }
                     conn = DriverManager.getConnection("jdbc:sqlite:banque.db");
                     PreparedStatement pst = conn.prepareStatement("select * from users where username = ? and status = ?");
                     pst.setString(1, username.toLowerCase());
                     pst.setInt(2, 0);
                     ResultSet rs = pst.executeQuery();
                     if(rs.next()){
                         if (passwd.equals(rs.getString("passwd"))){
                             UserInfo.setUsername(username);
                             UserInfo.setRole(RoleType.values()[rs.getInt("role")]);
                             UserInfo.setUserId(rs.getInt("id"));
                             UserInfo.setLogged(true);
                             if(UserInfo.getRole().equals(RoleType.customer)){
                                 PreparedStatement pp = conn.prepareStatement("select customer.id from users, customer where users.id = customer.user_id and username = ?");
                                 pp.setString(1, username.toLowerCase());
                                 ResultSet dd = pp.executeQuery();
                                 if(dd.next()){
                                     UserInfo.setCustomerId(dd.getInt(1));
                                 }
                                 dd.close();
                                 pp.close();
                             }
                             success();                         
                         }else{                         
                             JOptionPane.showMessageDialog(null, "Login ou mot de passe incorrect");
                             passwdText.setText("");
                         }                     
                     }else{
                         JOptionPane.showMessageDialog(null, "Login ou mot de passe incorrect");
                         passwdText.setText("");
                     }
                     rs.close();
                     pst.close();
                     conn.close();
                 } catch (SQLException ex) {
                     JOptionPane.showMessageDialog(null, "Impossible de vérifier vos coordonnées");
                     passwdText.setText("");                     
                     Logger.getLogger(LoginPanel.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
    }
    
    public void success(){
        
    }
    
}

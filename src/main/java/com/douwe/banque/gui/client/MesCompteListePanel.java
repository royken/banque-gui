package com.douwe.banque.gui.client;

import com.douwe.banque.data.AccountType;
import com.douwe.banque.gui.common.UserInfo;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Label;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Vincent Douwe<douwevincent@yahoo.fr>
 */
public class MesCompteListePanel extends JPanel{
    
    private JTable compteTable;
    private DefaultTableModel model;
    private static final String request = "select * from account where customer_id=?";
    private Connection conn;
    public MesCompteListePanel() throws Exception{
        setLayout(new BorderLayout(10,10));
        JPanel pan = new JPanel(new FlowLayout(FlowLayout.CENTER));
        Label lbl;
        pan.add(lbl = new Label("LA LISTE DE MES COMPTES"));
        lbl.setFont(new Font("Times New Roman", Font.ITALIC, 18));
        add(BorderLayout.BEFORE_FIRST_LINE, pan);
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        model = new DefaultTableModel(new String[]{"No Compte","Type Compte","Balance"}, 0);
        compteTable = new JTable(model);        
        add(BorderLayout.CENTER, new JScrollPane(compteTable));        
        conn = DriverManager.getConnection("jdbc:sqlite:banque.db", "", "");
        PreparedStatement pStmt = conn.prepareStatement(request);
        pStmt.setInt(1, UserInfo.getCustomerId());
        ResultSet rs = pStmt.executeQuery();
        while(rs.next()){
            model.addRow(new Object[]{rs.getString("accountNumber"), AccountType.values()[rs.getInt("type")], rs.getDouble("balance")});
        }
        pStmt.close();
        conn.close();
    }
    
}

package views;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;

import base.Controller;
import base.FrameController;
import base.Model;
import base.View;

/**
 * 
 * @author martin
 *
 */
public class QueryView extends View {
    private static final long serialVersionUID = 1351720760786229085L;
    private JFrame frame;
    private JTable content;
    private JButton updateButton;
    public QueryView(Model model) {
        super(model);
        setLayout(new GridLayout(1,2));
        content=new JTable(model.getQueryResult(), model.getColumnNames());
        JPanel left=new JPanel();
        left.setLayout(new BorderLayout());
        JList<String> auswahl=new JList<String>(model.getQueryChoices());
        left.add(auswahl, BorderLayout.CENTER);
        updateButton=new JButton("aktualisieren");
        left.add(updateButton, BorderLayout.SOUTH);
        add(left);
        add(content);
        setPreferredSize(new Dimension(500,500));
        frame=new JFrame("SQL Abfragefenster"+model.getDeviceID());
        frame.add(this);
        frame.pack();
        frame.setVisible(false);
    }
    public void addFrameListener(FrameController controller) {
        frame.addWindowListener(controller);
    }
    public void addQueryUpdateListener(Controller controller) {
        updateButton.addActionListener(controller);
    }
    public void update() {
        if(model.isQueriesOpened()) {
            frame.setVisible(true);
            content=new JTable(model.getQueryResult(), model.getColumnNames());
        } else {
            frame.setVisible(false);
        }
    }
}

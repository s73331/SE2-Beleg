package monitoringtool.views;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import monitoringtool.base.Controller;
import monitoringtool.base.FrameController;
import monitoringtool.base.Model;
import monitoringtool.base.View;


/**
 * 
 * @author martin
 *
 */
public class QueryView extends JPanel implements View {
    private static final long serialVersionUID = 1351720760786229085L;
    private JFrame frame;
    private JScrollPane content;
    private JButton updateButton;
    private Model model;
    private JList<String> auswahl;
    public QueryView(Model model) {
        this.model=model;
        setLayout(new GridLayout(1,2));
        JPanel left=new JPanel();
        left.setLayout(new BorderLayout());
        auswahl=new JList<String>(model.getQueries());
        auswahl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        left.add(auswahl, BorderLayout.CENTER);
        updateButton=new JButton("aktualisieren");
        left.add(updateButton, BorderLayout.SOUTH);
        add(left);
        content=new JScrollPane(new JTable());
        add(content);
        setPreferredSize(new Dimension(800,800));
        frame=new JFrame("SQL Abfragefenster - Gerät "+model.getDeviceID());
        frame.add(this);
        frame.pack();
        update();
    }
    public JList<String> getList() {
        return auswahl;
    }
    public void addFrameListener(FrameController controller) {
        frame.addWindowListener(controller);
    }
    public void addQueryUpdateListener(Controller controller) {
        updateButton.addActionListener(controller);
    }
    public void addListSelectionListener(ListSelectionListener controller) {
        auswahl.addListSelectionListener(controller);
    }
    public void update() {
        if(model.isQueriesOpened()) try {
            frame.setVisible(true);
            remove(content);
            ResultSet resultSet=model.getQueryResult();
            ResultSetMetaData metaData = resultSet.getMetaData();
            // names of columns
            Vector<String> columnNames = new Vector<String>();
            int columnCount = metaData.getColumnCount();
            for (int column = 1; column <= columnCount; column++) {
                columnNames.add(metaData.getColumnName(column));
            }
            // data of the table
            Vector<Vector<Object>> data = new Vector<Vector<Object>>();
            while (resultSet.next()) {
                Vector<Object> vector = new Vector<Object>();
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    vector.add(resultSet.getObject(columnIndex));
                }
            data.add(vector);
            }
            content=new JScrollPane(new JTable(new DefaultTableModel(data,columnNames)));
            add(content);
            frame.pack();
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        else {
            frame.setVisible(false);
        }
    }
}

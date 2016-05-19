package monitoringtool.controllers;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import monitoringtool.base.Model;
import monitoringtool.views.QueryView;

public class QueryChoiceController implements ListSelectionListener {
    private Model model;
    private JList<String> list;
    private QueryView queryView;
    public QueryChoiceController(QueryView queryView,Model model) {
        this.model=model;
        this.queryView=queryView;
        this.list=queryView.getList();
    }
    public void valueChanged(ListSelectionEvent e) {
        model.setCurrentQuery(list.getSelectedValue());
        model.updateQuery();
        queryView.update();
    }

}

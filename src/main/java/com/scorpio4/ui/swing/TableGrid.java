package com.scorpio4.ui.swing;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */
import javax.swing.*;
import javax.swing.table.TableColumn;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * scorpio4 (c) 2013
 * Module: com.scorpio4.ui.swing
 * @author lee
 * Date  : 8/11/2013
 * Time  : 6:07 PM
 */
public class TableGrid extends JTable {

    public TableGrid() {
    }

    public TableGrid(Set<String> columns) {
        init(columns);
    }

    public TableGrid(Set<String> columns, Collection<Map<String,Object>> results) {
        init(columns);
        render(results);
    }

    public void init(Set<String> columns) {
        for(String column:columns) {
            TableColumn tableColumn = new TableColumn();
            tableColumn.setHeaderValue(column);
            addColumn(tableColumn);
        }
        invalidate();
    }

    public void render(Collection<Map<String,Object>> results) {
        for(Map result:results) {
            // add row
            // add column values to row
        }
    }
}

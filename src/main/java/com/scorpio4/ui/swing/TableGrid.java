package com.scorpio4.ui.swing;
/*
 *   Scorpio4 - CONFIDENTIAL
 *   Unpublished Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *   NOTICE:  All information contained herein is, and remains the property of Lee Curtis. The intellectual and technical concepts contained
 *   herein are proprietary to Lee Curtis and may be covered by Australian, U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 *   from Lee Curtis.  Access to the source code contained herein is hereby forbidden to anyone except current Lee Curtis employees, managers or contractors who have executed
 *   Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 *   The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes
 *   information that is confidential and/or proprietary, and is a trade secret, of Lee Curtis.   ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE,
 *   OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF LEE CURTIS IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 *   LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 *   TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.
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
 * User  : lee
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

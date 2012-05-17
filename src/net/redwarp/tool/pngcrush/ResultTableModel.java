/*
 * Copryright (C) 2012 Redwarp
 * 
 * This file is part of PNGCrush Wrapper.
 * PNGCrush Wrapper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * PNGCrush Wrapper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with PNGCrush Wrapper.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.redwarp.tool.pngcrush;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class ResultTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -4059036865713964056L;
	
	List<OperationStatus> operations;
	String[] columnsName = new String[]{
			"File name", "Status", "Reduction"
	};

	public ResultTableModel() {
		operations = new ArrayList<OperationStatus>();
		
		
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return operations.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		OperationStatus operation = operations.get(row);
		if (col == 0) {
			return operation.getFile().getName();
		} else if (col == 1) {
			switch (operation.getStatus()) {
			case PENDING:
				return "Pending...";
			case INPROGRESS:
				return "Crunching...";
			case ERROR:
				return "Error !";
			default:
				return "Finish !";
			}
		} else {
			return (String.format("%.2f", operation.getReduction())
			+ " % reduction\n");
		}
	}
	
	public void notifyChange(OperationStatus operation){
		int row = operations.indexOf(operation);
		if(row != -1){
			fireTableCellUpdated(row, 1);
			fireTableCellUpdated(row, 2);
		}
	}
	
	@Override
	public String getColumnName(int column) {
		return columnsName[column];
	}

	public OperationStatus addFile(File f) {
		OperationStatus fileStatus = new OperationStatus(f);
		operations.add(fileStatus);
		int rowIndex = operations.size() - 1;
		fireTableRowsInserted(rowIndex, rowIndex);

		return fileStatus;
	}
}

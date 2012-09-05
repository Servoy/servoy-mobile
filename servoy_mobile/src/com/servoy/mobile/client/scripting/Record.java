package com.servoy.mobile.client.scripting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Record extends Scope 
{
	private final Map<String, Object> columns = new HashMap<String, Object>();
	private final Map<String, Integer> columnsTypes = new HashMap<String, Integer>();
	
	public Record(int id, String name, Date date) {
		columns.put("order_id", Integer.valueOf(id));
		columns.put("name", name);
		columns.put("order_date", date);
		columnsTypes.put("order_id", Integer.valueOf(4)); // IColumnTypes.INTEGER
		columnsTypes.put("name", Integer.valueOf(12)); // IColumnTypes.TEXT
		columnsTypes.put("order_date", Integer.valueOf(93)); // IColumnTypes.DATETIME
		exportColumns();
	}
	
	public void setValue(String name, Object value) {
		if (columns.containsKey(name)) {
			Object put = columns.put(name, value);
			if (put != null && !put.equals(value)) {
				// fire change.
				System.err.println("changed!!");
			}
		}
	}

	public Object getValue(String name) {
		return columns.get(name);
	}
	
	public int getVariableType(String variable) {
		Integer type = columnsTypes.get(variable);
		if (type != null) return type.intValue();
		return -4; // IColumnTypes.MEDIA;
	}
	
	public void setVariableType(String variable, int type) {
		// ignore can't be set from scripting
	}

	private void exportColumns() {
		for (String column : columns.keySet()) {
			exportProperty(column);
		}
	}
}

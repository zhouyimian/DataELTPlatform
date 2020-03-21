package com.km.data.common.element;



public interface Record {

    public void addColumn(Column column);

    public void setColumn(int i, final Column column);

    public Column getColumn(int i);

    public Object getColumnValue(String columnName);

    public String toString();

    public int getColumnNumber();

    public int getByteSize();


}

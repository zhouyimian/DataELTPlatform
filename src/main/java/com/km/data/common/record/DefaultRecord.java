package com.km.data.common.record;


import com.alibaba.fastjson.JSON;
import com.km.data.common.element.Column;
import com.km.data.common.element.Record;
import com.km.data.common.exception.DataETLException;
import com.km.data.common.util.ClassSize;
import com.km.data.common.util.FrameworkErrorCode;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class DefaultRecord implements Record {

	private static final int RECORD_AVERGAE_COLUMN_NUMBER = 16;

	private List<Column> columns;

	private int byteSize;

	private Map<String,Integer> columnNameToIndex;

	// 首先是Record本身需要的内存
	private int memorySize = ClassSize.DefaultRecordHead;

	public DefaultRecord() {
		this.columns = new ArrayList<Column>(RECORD_AVERGAE_COLUMN_NUMBER);
		this.columnNameToIndex = new HashMap<>();
	}

	@Override
	public void addColumn(Column column) {
		columns.add(column);
		incrByteSize(column);
		if(column.getColumnName()!=null&&column.getColumnName().length()!=0){
		    columnNameToIndex.put(column.getColumnName(),columns.size()-1);
        }
	}

	@Override
	public Column getColumn(int i) {
		if (i < 0 || i >= columns.size()) {
			return null;
		}
		return columns.get(i);
	}

    @Override
    public Object getColumnValue(String columnName) {
	    Integer index = columnNameToIndex.get(columnName);
	    if(index==null)
	        return null;
	    return columns.get(index).getRawData();
    }

    @Override
	public void setColumn(int i, final Column column) {
		if (i < 0) {
			throw DataETLException.asDataETLException(FrameworkErrorCode.ARGUMENT_ERROR,
					"不能给index小于0的column设置值");
		}

		if (i >= columns.size()) {
			expandCapacity(i + 1);
		}

		decrByteSize(getColumn(i));
		this.columns.set(i, column);
		incrByteSize(getColumn(i));
		updateMap();
	}

    private void updateMap() {
	    columnNameToIndex.clear();
	    for(int i = 0;i<columns.size();i++){
	        String columnName = columns.get(i).getColumnName();
	        if(columnName==null||columnName.length()==0){
	            continue;
            }else{
	            columnNameToIndex.put(columnName,i);
            }
        }
    }

    @Override
	public String toString() {
		Map<String, Object> json = new HashMap<String, Object>();
		json.put("size", this.getColumnNumber());
		json.put("data", this.columns);
		return JSON.toJSONString(json);
	}

	@Override
	public int getColumnNumber() {
		return this.columns.size();
	}

	@Override
	public int getByteSize() {
		return byteSize;
	}

	public int getMemorySize(){
		return memorySize;
	}

	private void decrByteSize(final Column column) {
		if (null == column) {
			return;
		}

		byteSize -= column.getByteSize();

		//内存的占用是column对象的头 再加实际大小
		memorySize = memorySize -  ClassSize.ColumnHead - column.getByteSize();
	}

	private void incrByteSize(final Column column) {
		if (null == column) {
			return;
		}

		byteSize += column.getByteSize();

		//内存的占用是column对象的头 再加实际大小
		memorySize = memorySize + ClassSize.ColumnHead + column.getByteSize();
	}

	private void expandCapacity(int totalSize) {
		if (totalSize <= 0) {
			return;
		}

		int needToExpand = totalSize - columns.size();
		while (needToExpand-- > 0) {
			this.columns.add(null);
		}
	}



}

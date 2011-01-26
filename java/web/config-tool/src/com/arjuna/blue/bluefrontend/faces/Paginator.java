package com.arjuna.blue.bluefrontend.faces;

import java.util.HashMap;

/* Test class to help out with pagination */

public class Paginator
{
	private HashMap objectList;
	private int numberOfRows = 10;
	private int firstRowIndex;
	
	
	public Paginator()
	{
		
	}
	
	public Paginator(HashMap objectList)
	{
		this.objectList = objectList;
	}
	
	public void setObjectList(HashMap objectList)
	{
		this.objectList = objectList;
	}
	
	public int getNumberOfRows()
	{
		return this.numberOfRows;
	}
	
	public int scrollFirst()
	{
		return 0;
	}
	
	public int scrollLast()
	{
		firstRowIndex = objectList.size() - numberOfRows;
		
		if(firstRowIndex < 0)
		{
			firstRowIndex = 0;
		}
		
		return firstRowIndex;
	}
	
	public int scrollPrevious()
	{
		firstRowIndex -= numberOfRows;
		
		if(firstRowIndex < 0)
		{
			firstRowIndex = 0;
		}
		
		return firstRowIndex;
	}
	
	public int scrollNext()
	{
		firstRowIndex += numberOfRows;
		
		if(firstRowIndex > objectList.size())
		{
			firstRowIndex = objectList.size() - numberOfRows;
			
			if(firstRowIndex < 0)
			{
				firstRowIndex = 0;
			}
		}
		
		return firstRowIndex;
	}
	
	public int entryCount()
	{
		return objectList.size();
	}
	
	
}

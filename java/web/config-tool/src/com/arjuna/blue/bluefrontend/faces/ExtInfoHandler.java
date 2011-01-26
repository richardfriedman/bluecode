package com.arjuna.blue.bluefrontend.faces;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.arjuna.blue.bluefrontend.xml.ObjectXMLBuilder;

public class ExtInfoHandler
{

	private HostExtInfo hostExtInfo;
	private HostExtInfo modifyHostExtInfo;
	private ServiceExtInfo serviceExtInfo;
	private ServiceExtInfo modifyServiceExtInfo;
	
	private BlueConfigXMLFileStore hostExtFileStore;
	private BlueConfigXMLFileStore serviceExtFileStore;
	
	private Paginator paginator;
	
	private DataModel hostExtInfoModel;
	private DataModel serviceExtInfoModel;
	
	private String addResult;
	private String modResult;
	private String delResult;
	private String selectResult;
	
	private int firstRowIndex;
	
	private static int HOST_EXT_INFO = 0;
	private static int SERVICE_EXT_INFO = 1;
	
	private FacesContext context;
	private AccountHandler handler;
	
	public ExtInfoHandler()
	{
		hostExtInfo = new HostExtInfo();
		serviceExtInfo = new ServiceExtInfo();
		hostExtFileStore = new BlueConfigXMLFileStore(ObjectXMLBuilder.HOSTEXTINFO);
		serviceExtFileStore = new BlueConfigXMLFileStore(ObjectXMLBuilder.SERVICEEXTINFO);
		paginator = new Paginator();
	}
	
	public void setHostExtInfo(HostExtInfo hostExtInfo)
	{
		this.hostExtInfo = hostExtInfo;
	}
	
	public HostExtInfo getHostExtInfo()
	{
		return this.hostExtInfo;
	}
	
	public void setModifyHostExtInfo(HostExtInfo modifyHostExtInfo)
	{
		this.modifyHostExtInfo = modifyHostExtInfo;
	}
	
	public HostExtInfo getModifyHostExtInfo()
	{
		return this.modifyHostExtInfo;
	}
	
	public void setServiceExtInfo(ServiceExtInfo serviceExtInfo)
	{
		this.serviceExtInfo = serviceExtInfo;
	}
	
	public ServiceExtInfo getServiceExtInfo()
	{
		return this.serviceExtInfo;
	}
	
	public void setModifyServiceExtInfo(ServiceExtInfo modifyServiceExtInfo)
	{
		this.modifyServiceExtInfo = modifyServiceExtInfo;
	}
	
	public ServiceExtInfo getModifyServiceExtInfo()
	{
		return this.modifyServiceExtInfo;
	}
	
	public int getHostExtInfoCount()
	{
		return hostExtFileStore.getObjectCount(ObjectXMLBuilder.HOSTEXTINFO);
	}
	
	public int getServiceExtInfoCount()
	{
		return serviceExtFileStore.getObjectCount(ObjectXMLBuilder.SERVICEEXTINFO);
	}

	public int getFirstRowIndex()
	{
		return firstRowIndex;
	}
	
	public int getRowCount()
	{
		return paginator.getNumberOfRows();
	}
	
	public synchronized String scrollFirst(ActionEvent e)
	{
		if(isHostExtInfoCommand(e))
			paginator.setObjectList(hostExtFileStore.getObjectHashMap());
		else
			paginator.setObjectList(serviceExtFileStore.getObjectHashMap());
				
		firstRowIndex = paginator.scrollFirst();
		return "success";
	}
	
	public synchronized String scrollPrevious(ActionEvent e)
	{
		if(isHostExtInfoCommand(e))
			paginator.setObjectList(hostExtFileStore.getObjectHashMap());
		else
			paginator.setObjectList(serviceExtFileStore.getObjectHashMap());
				
		firstRowIndex = paginator.scrollPrevious();
		return "success";
	}
	
	public synchronized String scrollNext(ActionEvent e)
	{
		if(isHostExtInfoCommand(e))
			paginator.setObjectList(hostExtFileStore.getObjectHashMap());
		else
			paginator.setObjectList(serviceExtFileStore.getObjectHashMap());
				
		firstRowIndex = paginator.scrollNext();
		return "success";
	}
	
	public synchronized String scrollLast(ActionEvent e)
	{
		if(isHostExtInfoCommand(e))
			paginator.setObjectList(hostExtFileStore.getObjectHashMap());
		else
			paginator.setObjectList(serviceExtFileStore.getObjectHashMap());
			
		firstRowIndex = paginator.scrollLast();
		return "success";
	}
	
	public synchronized boolean isScrollFirstDisabled()
	{
		return firstRowIndex == 0;
	}
	
	public synchronized boolean isScrollLastHostDisabled()
	{
		return firstRowIndex >= hostExtFileStore.getObjectCount(ObjectXMLBuilder.HOSTEXTINFO) - paginator.getNumberOfRows();
	}

	public synchronized boolean isScrollLastServiceDisabled()
	{
		return firstRowIndex >= serviceExtFileStore.getObjectCount(ObjectXMLBuilder.SERVICEEXTINFO) - paginator.getNumberOfRows();
	}
	
	public synchronized void addExtInfo(ActionEvent e)
	{
		int extType = 0;
		String result;
		
		try
		{
			extType = Integer.valueOf((String)(e.getComponent().getAttributes().get("extType")));
		}
		catch(NumberFormatException f)
		{
			addResult = "failure";
			return;
		}
		
		if(extType == HOST_EXT_INFO)
		{
			if(!hostExtFileStore.checkObjectNameExists(hostExtInfo.getName(),ObjectXMLBuilder.HOSTEXTINFO))
			{
				result = hostExtFileStore.addObject(hostExtInfo,ObjectXMLBuilder.HOSTEXTINFO);
				addResult = result;
				
				if(!result.equals("add-success"))
				{
					hostExtInfo.setId(-1);
					hostExtInfo.setIsModifiable(false);
					return;
				}
				
				hostExtInfo = new HostExtInfo();					
			}
		}
		else if(extType == SERVICE_EXT_INFO)
		{
			result = serviceExtFileStore.addObject(serviceExtInfo,ObjectXMLBuilder.SERVICEESCALATION);
			addResult = result;
			
			if(result.equals("add-success"))
			{
				serviceExtInfo = new ServiceExtInfo();
				if(Utils.inWizard())
				{
					addResult = "wizard-serviceextinfo-add";
					try
					{
						handler.finishWizard();
					}
					catch(Exception ex)
					{}
				}
			}
		}
	}
	
	public synchronized void deleteExtInfo(ActionEvent e)
	{
		int extType;
		int extId;
		String result;
		
		try
		{
			context = FacesContext.getCurrentInstance();
			
			Map props = context.getExternalContext().getRequestParameterMap();
			extType = Integer.valueOf((String)props.get("extInfoType"));
			extId = Integer.valueOf((String)props.get("extInfoId"));
			
			if(extType == HOST_EXT_INFO)
			{
				result = hostExtFileStore.deleteObject(extId,ObjectXMLBuilder.HOSTEXTINFO);
				delResult = result;
				
				if(result.equals("delete-success"))
					hostExtInfo = new HostExtInfo();
			}
			else if(extType == SERVICE_EXT_INFO)
			{
				result = serviceExtFileStore.deleteObject(extId,ObjectXMLBuilder.SERVICEEXTINFO);
				delResult = result;
				if(result.equals("delete-success"))
					serviceExtInfo = new ServiceExtInfo();
			}
		}
		catch(Exception ex)
		{
			delResult= "failure";
		}
	}
	
	public synchronized void modifyExtInfo(ActionEvent e)
	{
		int extType;
		String result;
		
		try
		{
			extType = Integer.valueOf((String)(e.getComponent().getAttributes().get("extType")));
		}
		catch(NumberFormatException f)
		{
			modResult = "failure";
			return;
		}
				
		if(extType == HOST_EXT_INFO)
		{
			result = hostExtFileStore.modifyObject(modifyHostExtInfo,ObjectXMLBuilder.HOSTEXTINFO);
			modResult = result;
			
			if(result.equals("modify-success"))
				modifyHostExtInfo = new HostExtInfo();
		}
		else if(extType == SERVICE_EXT_INFO)
		{
			result = serviceExtFileStore.modifyObject(modifyServiceExtInfo,ObjectXMLBuilder.SERVICEEXTINFO);
			modResult = result;
			
			if(result.equals("modify-success"))
				modifyServiceExtInfo = new ServiceExtInfo();
		}
	}
	
	
	public synchronized String addResult()
	{
		return this.addResult;
	}
	
	public synchronized String delResult()
	{
		return this.delResult;
	}
	
	public synchronized String modResult()
	{
		return this.modResult;
	}
	
	public synchronized void select(ActionEvent e)
	{
		int extInfoType;
		
		try
		{
			extInfoType = Integer.valueOf((String)e.getComponent().getAttributes().get("ext_info_type"));
			
			if(extInfoType == 0)
			{
				modifyHostExtInfo = (HostExtInfo)hostExtInfoModel.getRowData();
				selectResult = "select-hostexti";
			}
			else if(extInfoType == 1)
			{
				modifyServiceExtInfo = (ServiceExtInfo)serviceExtInfoModel.getRowData();
				selectResult = "select-serviceexti";
			}
		}
		catch(Exception ex)
		{
			selectResult = "failure";
		}
	}
	
	public synchronized String selectResult()
	{
		return this.selectResult;
	}
	
	public DataModel getHostExtInfoDetails()
	{
		hostExtInfoModel = new ListDataModel(hostExtFileStore.getStoredObjects(ObjectXMLBuilder.HOSTEXTINFO));
		return hostExtInfoModel;		
	}
	
	public DataModel getServiceExtInfoDetails()
	{
		serviceExtInfoModel = new ListDataModel(serviceExtFileStore.getStoredObjects(ObjectXMLBuilder.SERVICEEXTINFO));
		return serviceExtInfoModel;
	}
	
	private boolean isHostExtInfoCommand(ActionEvent e)
	{
		int extType;
		
		try
		{
			extType = Integer.valueOf((String)e.getComponent().getAttributes().get("extType"));
		}
		catch(Exception ex)
		{
			throw new IllegalStateException("Unknown ExtInfo Type");
		}
		
		if(extType == HOST_EXT_INFO)
			return true;
		
		return false;
	}
	
}

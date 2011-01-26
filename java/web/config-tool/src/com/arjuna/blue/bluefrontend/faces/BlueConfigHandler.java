package com.arjuna.blue.bluefrontend.faces;

import javax.faces.event.ActionEvent;

import com.arjuna.blue.bluefrontend.xml.ObjectXMLBuilder;

public class BlueConfigHandler 
{
	private BlueConfig blueConfig;
	private BlueConfigXMLFileStore fileStore;
	private String saveResult;
		
	public BlueConfigHandler()
	{
		
		blueConfig = new BlueConfig();
		fileStore = new BlueConfigXMLFileStore(ObjectXMLBuilder.BLUECONFIG);
	}
	
	/* Set our BlueConfig instance */
	public void setBlueConfig(BlueConfig blueConfig)
	{
		this.blueConfig  = blueConfig;
	}
	
	/* Get our BlueConfig instance */
	public BlueConfig getBlueConfig()
	{
		return this.blueConfig;
	}
	
	/* Return the result of our save operation */	
	public synchronized String saveResult()
	{
		return saveResult;
	}
	
	/* Save the configuration of our main config file */	
	public synchronized void saveOptions(ActionEvent e)
	{
		
		int stage;
		
		try
		{
			stage = Integer.valueOf((String)e.getComponent().getAttributes().get("stage"));
		}
		catch(NumberFormatException ex)
		{
			saveResult = "failure";
			return;
		}
		
		switch(stage)
		{
			case 1:
					if(fileStore.storeMainBlueConfig(blueConfig).equals("success"))
						saveResult = "success-add-1";
					
					return;
					
			case 2:
					if(fileStore.storeMainBlueConfig(blueConfig).equals("success"))
						saveResult = "success-add-2";
					
					return;
							
			case 3:
					if(fileStore.storeMainBlueConfig(blueConfig).equals("success"))
						saveResult = "success-add-3";
					
					return;
			
			case 4:

					if(fileStore.storeMainBlueConfig(blueConfig).equals("success"))
					{
						if(Utils.inWizard())
						{
							saveResult = "wizard-config-add";
							return;
					}
					saveResult = "success-add-4";
				}
		}
	
	}
}

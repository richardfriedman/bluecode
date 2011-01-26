package org.blue.star.plugins.salesforce;
/*****************************************************************************
*
* Blue Star, a Java Port of .
* Last Modified : 23/01/07
*
* License:
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 2 as
* published by the Free Software Foundation.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*
*****************************************************************************/
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.SessionHeader;
import com.sforce.soap.partner.SforceServiceLocator;
import com.sforce.soap.partner.SoapBindingStub;
import com.sforce.soap.partner.fault.LoginFault;
import com.sforce.soap.partner.fault.UnexpectedErrorFault;

/**
 * @author Mark
 */
public class SalesForceConnector
{
	private String user = null;
	private String password = null;
	private String url = null;

	/**
	 * @param url
	 * @param user
	 * @param pass
	 */
	public SalesForceConnector(String url, String user, String pass)
	{
		this.url = url;
		this.user = user;
		this.password = pass;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public SoapBindingStub login()
	{
		SforceServiceLocator locator = new SforceServiceLocator();
		if ((this.url == null) || (this.url.length() == 0))
			this.url = locator.getSoapAddress();

		System.out.println("URL: " + this.url);
		SoapBindingStub binding;
		try
		{
			binding = (SoapBindingStub) locator.getSoap(new URL(this.url));
		} catch (MalformedURLException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (ServiceException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		if (binding == null)
			throw new RuntimeException("ERROR: unknown error creating binding to soap service");

		binding.setTimeout(60000);
		LoginResult loginResult = null;
		int maxTries = 5;

		for (int retries = 1; retries <= maxTries; retries++)
			try
			{
				loginResult = binding.login(this.user, this.password);
			} catch (LoginFault lf)
			{
				throw new RuntimeException("LoginFault: " + lf.getExceptionMessage());
			} catch (UnexpectedErrorFault uef)
			{
				throw new RuntimeException("UnexpectedErrorFault: " + uef.getExceptionMessage());
			} catch (RemoteException re)
			{
				if (retries < maxTries)
				{
					if (retries == 1)
					{
						re.printStackTrace();
					}
					try
					{
						Thread.sleep(15000L);
					} catch (InterruptedException e)
					{
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				} else
				{
					re.printStackTrace();
					throw new RuntimeException("RemoteException: " + re.getMessage());
				}
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new RuntimeException("Login Exception: " + ex.getMessage());
			}

		try
		{
			locator.createCall();
			binding = (SoapBindingStub) locator.getSoap(new URL(loginResult.getServerUrl()));
		} catch (ServiceException se)
		{
			throw new RuntimeException("ERROR: creating binding to soap service, error was: \n" + se.getMessage());
		} catch (MalformedURLException URLex)
		{
			throw new RuntimeException(URLex.getMessage());
		}
		SessionHeader sh = new SessionHeader();
		sh.setSessionId(loginResult.getSessionId());
		binding.setHeader("SforceService", "SessionHeader", sh);
		binding._setProperty("UseOutboundCompression", new Boolean(true));
		binding._setProperty("UseInboundCompression", new Boolean(true));
		// binding.setTimeout(0);
		binding.setMaintainSession(true);

		return binding;
	}
}

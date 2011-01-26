package org.blue.star.plugins.snmp;

/*
public class Snmpextends java.lang.Objectimplements Session, CommandResponderThe Snmp class is the core of SNMP4J. It provides functions to send and receive SNMP PDUs. All SNMP PDU types can be send. Confirmed PDUs can be sent synchronously and asynchronously. 

The Snmp class is transport protocol independent. Support for a specific TransportMapping instance is added by calling the addTransportMapping(TransportMapping transportMapping) method or creating a Snmp instance by using the non-default constructor with the corresponding transport mapping. Transport mappings are used for incoming and outgoing messages. 

To setup a default SNMP session for UDP transport and with SNMPv3 support the following code snippet can be used: 


   Address targetAddress = GenericAddress.parse("udp:127.0.0.1/161");
   TransportMapping transport = new DefaultUdpTransportMapping();
   snmp = new Snmp(transport);
   USM usm = new USM(SecurityProtocols.getInstance(),
                     new OctetString(MPv3.createLocalEngineID()), 0);
   SecurityModels.getInstance().addSecurityModel(usm);
   transport.listen();
 How a synchronous SNMPv3 message with authentication and privacy is then sent illustrates the following code snippet: 


   // add user to the USM
   snmp.getUSM().addUser(new OctetString("MD5DES"),
                         new UsmUser(new OctetString("MD5DES"),
                                     AuthMD5.ID,
                                     new OctetString("MD5DESUserAuthPassword"),
                                     PrivDES.ID,
                                     new OctetString("MD5DESUserPrivPassword")));
   // create the target
   UserTarget target = new UserTarget();
   target.setAddress(targetAddress);
   target.setRetries(1);
   target.setTimeout(5000);
   target.setVersion(SnmpConstants.version3);
   target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
   target.setSecurityName(new OctetString("MD5DES"));

   // create the PDU
   PDU pdu = new ScopedPDU();
   pdu.add(new VariableBinding(new OID("1.3.6")));
   pdu.setType(PDU.GETNEXT);

   // send the PDU
   ResponseEvent response = snmp.send(pdu, target);
   // extract the response PDU (could be null if timed out)
   PDU responsePDU = response.getResponse();
   // extract the address used by the agent to send the response:
   Address peerAddress = response.getPeerAddress();
 An asynchronous SNMPv1 request is sent by the following code: 

   // setting up target
   CommunityTarget target = new CommunityTarget();
   target.setCommunity(new OctetString("public"));
   target.setAddress(targetAddress);
   target.setRetries(2);
   target.setTimeout(1500);
   target.setVersion(SnmpConstants.version1);
   // creating PDU
   PDU pdu = new PDU();
   pdu.add(new VariableBinding(new OID(new int[] {1,3,6,1,2,1,1,1})));
   pdu.add(new VariableBinding(new OID(new int[] {1,3,6,1,2,1,1,2})));
   pdu.setType(PDU.GETNEXT);
   // sending request
   ResponseListener listener = new ResponseListener() {
     public void onResponse(ResponseEvent event) {
       // Always cancel async request when response has been received
       // otherwise a memory leak is created! Not canceling a request
       // immediately can be useful when sending a request to a broadcast
       // address.
       ((Snmp)event.getSource()).cancel(event.getRequest(), this);
       System.out.println("Received response PDU is: "+event.getResponse());
     }
   };
   snmp.sendPDU(pdu, target, null, listener);
 
Traps (notifications) and other SNMP PDUs can be received by adding the folling code to the first code snippet above: 
   CommandResponder trapPrinter = new CommandResponder() {
     public synchronized void processPdu(CommandResponderEvent e) {
       PDU command = e.getPdu();
       if (command != null) {
         System.out.println(command.toString());
       }
     }
   };
   snmp.addCommandResponder(trapPrinter);
*/
public class Snmp
{

}

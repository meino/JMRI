//SRCPVisitor.java

package jmri.jmris.srcp.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;

/* This class provides an interface between the JavaTree/JavaCC 
 * parser for the SRCP protocol and the JMRI back end.
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision$
 */

public class SRCPVisitor implements SRCPParserVisitor {

  private String outputString=null;

  public String getOutputString(){
       return outputString;
  }

  // note that the isSupported function has the side
  // effect of setting an error message to outputString if
  // it returns false.
  private boolean isSupported(int bus,String devicegroup){
          // get the system memo coresponding to the bus.
          // and ask it what is supported
          try {
             jmri.jmrix.SystemConnectionMemo memo = 
                          (jmri.jmrix.SystemConnectionMemo)InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class).get(bus-1);
             if(memo!=null) {
                log.debug("devicegroup " + devicegroup);
                if(devicegroup.equals("FB")) {
                   if(memo.provides(jmri.SensorManager.class)) {
                     return true;
                   } else {
                      // respond this isn't supported
                      outputString="422 ERROR unsupported device group";
                   }
                } else if(devicegroup.equals("GA")) {
                   if(memo.provides(jmri.TurnoutManager.class)) {
                      return true;
                   } else {
                      // respond this isn't supported
                      outputString="422 ERROR unsupported device group";
                   }
                } else if(devicegroup.equals("GL")) {
                   if(memo.provides(jmri.ThrottleManager.class)){
                      return true;
                   } else {
                      // respond this isn't supported
                      outputString="422 ERROR unsupported device group";
                   }
                } else if(devicegroup.equals("POWER")) {
                   if(memo.provides(jmri.PowerManager.class)){
                      return true;
                   } else {
                      // respond this isn't supported
                      outputString="422 ERROR unsupported device group";
                   }
                } else if(devicegroup.equals("SM")) {
                   if(memo.provides(jmri.ProgrammerManager.class)){
                      return true;
                   } else {
                      // respond this isn't supported
                      outputString="422 ERROR unsupported device group";
                   }
                } else {
                   // respond this isn't supported
                   outputString="422 ERROR unsupported device group";
 
                }
             } else {
               // no memo registered for this bus.
               outputString="416 ERROR no data";
             }
          } catch(java.lang.IndexOutOfBoundsException obe) {
              outputString="412 ERROR wrong value";
          }
     return false;
  }

  public Object visit(SimpleNode node, Object data)
  {
    log.debug("Generic Visit " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASThandshakecommand node,Object data)
  {
    log.debug("Handshake Mode Command " );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTcommand node,Object data)
  {
    log.debug("Command " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }

  public Object visit(ASTgo node,Object data)
  {
    log.debug("Go " + node.jjtGetValue() );
    jmri.jmris.srcp.JmriSRCPServiceHandler handle = (jmri.jmris.srcp.JmriSRCPServiceHandler)data;
    // The GO command should switch the server into runmode, but
    // only if the client has set the protocol version.  (if no mode
    // is set, the default is command mode).
    if(handle.getClientVersion().startsWith("0.8")) {
       handle.setRunMode();
       outputString="200 OK GO " + ((jmri.jmris.srcp.JmriSRCPServiceHandler)data).getSessionNumber();
    } else {
       outputString="402 ERROR insufficient data";
    }
       return data;
  }

  public Object visit(ASThandshake_set node,Object data)
  {
    log.debug("Handshake Mode SET " );
    jmri.jmris.srcp.JmriSRCPServiceHandler handle = (jmri.jmris.srcp.JmriSRCPServiceHandler)data;
    if(node.jjtGetChild(0).getClass()==ASTprotocollitteral.class) {
               String version=(String)((SimpleNode)node.jjtGetChild(1)).jjtGetValue();
               if(version.startsWith("0.8")) {
                  handle.setClientVersion(version);
                  outputString="201 OK PROTOCOL SRCP";
               } else {
                  outputString="400 ERROR unsupported protocol";
               }
            }
     else if(node.jjtGetChild(0).getClass()==ASTconnectionlitteral.class){
               String mode = (String)((SimpleNode)node.jjtGetChild(1)).jjtGetValue();
               outputString="202 OK CONNECTIONMODEOK";
               if(mode.equals("COMMAND"))
                  handle.setCommandMode(true);
               else if(mode.equals("INFO"))
                  handle.setCommandMode(false);
               else                  
                  outputString="401 ERROR unsupported connection mode";
    } else {
         outputString="500 ERROR out of resources";
    }
    return data;
  }



  public Object visit(ASTget node, Object data)
  {
    log.debug("Get " +((SimpleNode)node.jjtGetChild(1)).jjtGetValue());
    int bus = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(0)).jjtGetValue()));
    if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("POWER") 
         && isSupported(bus,"POWER") ) {
       // This is a message asking for the power status
       try {
       ((jmri.jmris.ServiceHandler)data).getPowerServer().sendStatus(
                           InstanceManager.powerManagerInstance().getPower());
       } catch(jmri.JmriException je) {
             // We shouldn't have any errors here.
             // If we do, something is horibly wrong.
       } catch(java.io.IOException ie) {
       }
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("GA") 
         && isSupported(bus,"GA") ) {
       // This is a message asking for the status of a "General Accessory".
       int address = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
       // our implementation ignores the port, but maybe we shouldn't to 
       // follow the letter of the standard.
       //int port = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(3)).jjtGetValue()));
       try {
       ((jmri.jmris.srcp.JmriSRCPTurnoutServer)((jmri.jmris.ServiceHandler)data).getTurnoutServer()).sendStatus(bus,address);
       } catch(java.io.IOException ie) {
       }
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("FB")
         && isSupported(bus,"FB") ) {
       // This is a message asking for the status of a FeedBack sensor.
       int address = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
       try {
       ((jmri.jmris.srcp.JmriSRCPSensorServer)((jmri.jmris.ServiceHandler)data).getSensorServer()).sendStatus(bus,address);
       } catch(java.io.IOException ie) {
       }
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("SM")
         && isSupported(bus,"SM") ) {
      // This is a Service Mode read request.
      int modeno=jmri.Programmer.REGISTERMODE;
      if(node.jjtGetChild(3).getClass()==ASTcv.class)
         modeno=jmri.Programmer.DIRECTBYTEMODE;
      else if(node.jjtGetChild(3).getClass()==ASTcvbit.class)
         modeno=jmri.Programmer.DIRECTBITMODE;
     
      int cv = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(4)).jjtGetValue()));
       //try {
       ((jmri.jmris.srcp.JmriSRCPProgrammerServer)((jmri.jmris.ServiceHandler)data).getProgrammerServer()).readCV(modeno,cv);
       //} catch(java.io.IOException ie) {
       //}
       
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("GL")
         && isSupported(bus,"GL") ) {
      // This is a Generic Loco request
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("TIME")) 
    {
      // This is a Time request
       try {
       ((jmri.jmris.ServiceHandler)data).getTimeServer().sendTime();
       } catch(java.io.IOException ie) {
       }

    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("SERVER"))
    {
       // for the GET <bus> SERVER request, we return the current server 
       // state.  In JMRI, we always return "Running".
       outputString="100 INFO 0 SERVER RUNNING";
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("DESCRIPTION"))
    {
       // for the GET <bus> DESCRIPTION request, what we return depends on
       // the number of arguments passed.
       SimpleNode descriptionnode = (SimpleNode)node.jjtGetChild(1);
       int children=descriptionnode.jjtGetNumChildren();
       if(children==0) {
          // with no arguments, we send a list of supported groups.
          if(bus==0) {
             // the groups supported by bus 0 are fixed
             outputString="100 INFO 0 DESCRIPTION SERVER SESSION TIME";
          } else {
             outputString="100 INFO " +bus;
             // get the system memo coresponding to the bus.
             // and ask it what is supported
             try {
                jmri.jmrix.SystemConnectionMemo memo = 
                          (jmri.jmrix.SystemConnectionMemo)InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class).get(bus-1);
                if(memo!=null) {
                  outputString = outputString + " DESCRIPTION";
                  if(memo.provides(jmri.SensorManager.class))
                     outputString=outputString + " FB";
                  if(memo.provides(jmri.TurnoutManager.class))
                     outputString=outputString + " GA";
                  if(memo.provides(jmri.ThrottleManager.class))
                     outputString=outputString + " GL";
                  if(memo.provides(jmri.PowerManager.class))
                     outputString=outputString + " POWER";
                  if(memo.provides(jmri.ProgrammerManager.class))
                     outputString=outputString + " SM";
                } else {
                  // no memo registered for this bus.
                  outputString="416 ERROR no data";
                }
            } catch(java.lang.IndexOutOfBoundsException obe) {
                  outputString="412 ERROR wrong value";
            }
          }
       } else if(children==1) {
          // with one argument, we respond with data only for device groups
          // that have no addresses.
          String devicegroup = (String)((SimpleNode)descriptionnode.jjtGetChild(0)).jjtGetValue();
          outputString="100 INFO " +bus;
          log.debug("devicegroup " + devicegroup);
          if(devicegroup.equals("FB") && isSupported(bus,devicegroup) ) {
             outputString="419 ERROR list too short";
          } else if(devicegroup.equals("GA") && isSupported(bus,devicegroup) ) {
             outputString="419 ERROR list too short";
          } else if(devicegroup.equals("GL") && isSupported(bus,devicegroup) ) {
             outputString="419 ERROR list too short";
          } else if(devicegroup.equals("POWER") && isSupported(bus,devicegroup) ) {
             // we are supposed to return the init string,
             // and the POWER group has no parameters, so
             // just return POWER
             outputString=outputString +" POWER";
          } else if(devicegroup.equals("SM") && isSupported(bus,devicegroup) ) {
             outputString="419 ERROR list too short";
          } else {
             // respond this isn't supported
             outputString="422 ERROR unsupported device group";
          }  // end if(chidren==1)
       } else if(children==2) {
          outputString="100 INFO " +bus;
          // get the system memo coresponding to the bus.
          // and ask it what is supported
          // with 2 arguments, we send a description of a specific device.
          jmri.jmrix.SystemConnectionMemo memo = 
                 (jmri.jmrix.SystemConnectionMemo)InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class).get(bus-1);
          if(memo!=null) {
             String devicegroup = (String)((SimpleNode)descriptionnode.jjtGetChild(0)).jjtGetValue();
             String address = (String)((SimpleNode)descriptionnode.jjtGetChild(1)).jjtGetValue();
             if(devicegroup.equals("FB") && isSupported(bus,devicegroup) ) {
                 jmri.SensorManager mgr = memo.get(jmri.SensorManager.class);
                 try{
                    String searchName = mgr.createSystemName(address,
                                        memo.getSystemPrefix());
                    if(mgr.getSystemNameList().contains(searchName)){
                       // add the initialization parameter list.
                       // we don't expect parameters, so just return
                       // the bus and address.
                       outputString=outputString + " FB " +address;
                    } else {
                       // the device wasn't found.
                       outputString = "412 ERROR wrong value";
                    }
                 } catch(jmri.JmriException je) {
                     // the device wasn't found.
                     outputString = "412 ERROR wrong value";
                 }
              } else if(devicegroup.equals("GA") && isSupported(bus,devicegroup) ) {
                 jmri.TurnoutManager mgr = memo.get(jmri.TurnoutManager.class);
                 try{
                    String searchName = mgr.createSystemName(address,                                       memo.getSystemPrefix());
                    if(mgr.getSystemNameList().contains(searchName)){
                       // add the initialization parameter list.
                       // the only other required parameter is
                       // the protocol, and we treat all of our
                       // turnouts as NMRA-DCC turnouts, so return
                       // the fixed "N" protocol value.
                       // other valid options are:
                       //    "M" (Mareklin/Motorola format)
                       //    "S" (Selectrix Format)
                       //    "P" (Protocol by server)
                       outputString=outputString + " GA " +address +" N";
                    } else {
                       // the device wasn't found.
                       outputString = "412 ERROR wrong value";
                    }
                 } catch(jmri.JmriException je) {
                    // the device wasn't found.
                    outputString = "412 ERROR wrong value";
                 }
              } else if(devicegroup.equals("GL") && isSupported(bus,devicegroup)  ) {
                 // outputString=outputString + " GL " +address;
                 // this one needs some tought on how to proceed,
                 // since the throttle manager differs from 
                 // other JMRI managers.
                 // for now, just say no data.
                 outputString="416 ERROR no data";
              } else if(devicegroup.equals("POWER")&& isSupported(bus,devicegroup)  ) {
                 outputString="418 ERROR list too long";
              } else if(devicegroup.equals("SM") && isSupported(bus,devicegroup)  ) {
                 //outputString=outputString + " SM " +address;
                 // this one needs some tought on how to proceed                                 // since we have both service mode and ops mode
                 // programmers, but the service mode programmer is
                 // not addressed on DCC systems.
                 // for now, just say no data.
                 outputString="416 ERROR no data";
              }
          } // end if(children==2)
       } else {
          outputString="418 ERROR list too long";
       } // end of DESCRIPTION device group.
    } else {
       outputString="422 ERROR unsupported device group";
    }
    return data;
  }


  public Object visit(ASTset node, Object data)
  {
    SimpleNode target = (SimpleNode)node.jjtGetChild(1);

    log.debug("Set " + target.jjtGetValue());
    int bus = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(0)).jjtGetValue()));

    if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("POWER")
         && isSupported(bus,"POWER") ) {
       try {
       ((jmri.jmris.ServiceHandler)data).getPowerServer().parseStatus(
                  ((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
       } catch(java.io.IOException ie) {
       } catch(jmri.JmriException je) {
             // We shouldn't have any errors here.
             // If we do, something is horibly wrong.
       }
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("GA")
         && isSupported(bus,"GA") ) {
       int address = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
       int port = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(3)).jjtGetValue()));
       // we expect to get both the value and delay, but JMRI only cares about
       // the port which indicates which output of a pair we are using.
       // leave the values below commented out, unless we decide to use them 
       // later.
       //int value = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(4)).jjtGetValue()));
       //int delay = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(5)).jjtGetValue()));

       try {
       ((jmri.jmris.srcp.JmriSRCPTurnoutServer)((jmri.jmris.ServiceHandler)data).getTurnoutServer()).parseStatus(bus,address,port);
       } catch(jmri.JmriException je) {
             // We shouldn't have any errors here.
             // If we do, something is horibly wrong.
       } catch(java.io.IOException ie) {
       }
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("FB")
         && isSupported(bus,"FB") ) {
       int address = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
       int value = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(3)).jjtGetValue()));
       try {
       ((jmri.jmris.srcp.JmriSRCPSensorServer)((jmri.jmris.ServiceHandler)data).getSensorServer()).parseStatus(bus,address,value);
       } catch(jmri.JmriException je) {
             // We shouldn't have any errors here.
             // If we do, something is horibly wrong.
       } catch(java.io.IOException ie) {
       }
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("SM")
         && isSupported(bus,"SM") ) {
      // This is a Service Mode write request
      int modeno=jmri.Programmer.REGISTERMODE;
      if(node.jjtGetChild(3).getClass()==ASTcv.class)
         modeno=jmri.Programmer.DIRECTBYTEMODE;
      else if(node.jjtGetChild(3).getClass()==ASTcvbit.class)
         modeno=jmri.Programmer.DIRECTBITMODE;
      int cv = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(4)).jjtGetValue()));
      int value = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(5)).jjtGetValue()));
     
       //try {
       ((jmri.jmris.srcp.JmriSRCPProgrammerServer)((jmri.jmris.ServiceHandler)data).getProgrammerServer()).writeCV(modeno,cv,value);
       //} catch(java.io.IOException ie) {
       //}
       
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("GL")
         && isSupported(bus,"GL") ) {
      // This is a Generic Loco request
    }
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("TIME"))
    {
      // This is a Time request
       try {
         jmri.jmris.srcp.JmriSRCPTimeServer ts=(jmri.jmris.srcp.JmriSRCPTimeServer)(((jmri.jmris.ServiceHandler)data).getTimeServer());
         int julDay = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
       int hour = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(3)).jjtGetValue()));
       int minute = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(4)).jjtGetValue()));
       int second = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(4)).jjtGetValue()));
         
         // set the time
         ts.parseTime(julDay,hour,minute,second);
         // and start the clock.
         ts.startTime();
         ts.sendTime();
       } catch(java.io.IOException ie) {
       }
    } else {
       outputString="422 ERROR unsupported device group";
    }
    return data;
  }


  public Object visit(ASTterm node, Object data)
  {
    log.debug("TERM " +((SimpleNode)node.jjtGetChild(1)).jjtGetValue());
    if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("SERVER"))
    {
       // for the TERM <bus> SERVER request, the protocol requries that
       // we terminate all connections and reset the state to the initial
       // state.  Since we may have a local GUI controlling things, we
       // ignore the request, but send the proper return value to the
       // requesting client.
       outputString="200 OK";
       return data;
    } else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("SESSION")){
       // for the TERM <bus> SERVER request, the protocol requries that
       // we terminate all connections and reset the state to the initial
       // state.  Since we may have a local GUI controlling things, we
       // ignore the request, but send the proper return value to the
       // requesting client.
       outputString="102 TERM 0 SESSION " + ((jmri.jmris.srcp.JmriSRCPServiceHandler)data).getSessionNumber();  // we need to set session IDs.
       return data;
    }
    
    return node.childrenAccept(this,data);
  }

  public Object visit(ASTcheck node, Object data)
  {
    log.debug("CHECK " +((SimpleNode)node.jjtGetChild(1)).jjtGetValue());
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTverify node,java.lang.Object data)
  {
    log.debug("VERIFY " +((SimpleNode)node.jjtGetChild(1)).jjtGetValue());
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTreset node,java.lang.Object data)
  {
    log.debug("RESET " +((SimpleNode)node.jjtGetChild(1)).jjtGetValue());
    if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("SERVER"))
    {
       // for the RESET <bus> SERVER request, the protocol requries that
       // we re-initialize the server.  Since we may have a local GUI 
       // controlling things, we ignore the request, but send a prohibited
       // response to the requesting client.
       outputString="413 ERROR temporarily prohibited";
       return data;
    } else {
       outputString="422 ERROR unsupported device group";
    }
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTinit node,java.lang.Object data)
  {
    int bus = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(0)).jjtGetValue()));
    log.debug("INIT " +((SimpleNode)node.jjtGetChild(1)).jjtGetValue());
    if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("POWER")
         && isSupported(bus,"POWER") ) {
        /* Power really has nothing to do in JMRI */
        outputString="200 OK";
    } 
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("GA") 
         && isSupported(bus,"GA") ) {
        /* Initilize a new accessory */
       int address = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
       String protocol = ((String)((SimpleNode)node.jjtGetChild(3)).jjtGetValue());
       try {
       ((jmri.jmris.srcp.JmriSRCPTurnoutServer)((jmri.jmris.ServiceHandler)data).getTurnoutServer()).initTurnout(bus,address,protocol);
       } catch(jmri.JmriException je) {
             // We shouldn't have any errors here.
             // If we do, something is horibly wrong.
       } catch(java.io.IOException ie) {
       }
       
    } 
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("GL")
         && isSupported(bus,"GL") ) {
        /* Initilize a new locomotive */
       //int address = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
    } 
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("TIME")) {
        /* Initilize fast clock ratio */
       try {
        /* the two parameters form a ration of modeltime:realtime */
       int modeltime = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue()));
       int realtime = Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(3)).jjtGetValue()));
         jmri.jmris.srcp.JmriSRCPTimeServer ts=(jmri.jmris.srcp.JmriSRCPTimeServer)(((jmri.jmris.ServiceHandler)data).getTimeServer());
         ts.parseRate(modeltime,realtime);
         ts.sendRate();
       } catch(java.io.IOException ie) {
       }
    } 
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("SM")
         && isSupported(bus,"SM") ) {
        /* Initilize service mode */
        outputString="200 OK";
    } 
    else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("FB")
         && isSupported(bus,"FB") ) {
        /* Initilize feedback on a particular bus */
        outputString="200 OK";
    } 

    return data;
  }
  public Object visit(ASTcomment node,java.lang.Object data)
  {
    log.debug("COMMENT " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTgl node, Object data)
  {
    log.debug("GL " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTsm node, Object data)
  {
    log.debug("SM " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTga node, Object data)
  {
    log.debug("GA" +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTfb node, Object data)
  {
    log.debug("FB " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTtime node, Object data)
  {
    log.debug("TIME " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTpower node, Object data)
  {
    log.debug("POWER " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTserver node, Object data)
  {
    log.debug("SERVER " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTsession node, Object data)
  {
    log.debug("SESION " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTlock node, Object data)
  {
    log.debug("LOCK " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTwait_cmd node, Object data)
  {
    log.debug("Received WAIT CMD " + node.jjtGetValue() );
    if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("TIME")) {
       long julday=Long.parseLong((String)((SimpleNode)node.jjtGetChild(2)).jjtGetValue());
       int Hour=Integer.parseInt((String)((SimpleNode)node.jjtGetChild(3)).jjtGetValue());
       int Minute=Integer.parseInt((String)((SimpleNode)node.jjtGetChild(4)).jjtGetValue());
       int Second=Integer.parseInt((String)((SimpleNode)node.jjtGetChild(5)).jjtGetValue());
       ((jmri.jmris.srcp.JmriSRCPTimeServer)((jmri.jmris.ServiceHandler)data).getTimeServer()).setAlarm(julday,Hour,Minute,Second);
      
    } else if(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("FB")) {
         outputString="425 ERROR not supported";
    } else {
         outputString="423 ERROR unsupported operation";
    }
    return data;
  }
  public Object visit(ASTbus node, Object data)
  {
    log.debug("Received Bus " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTaddress node, Object data)
  {
    log.debug("Received Address " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTvalue node, Object data)
  {
    log.debug("Received Value " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTzeroaddress node, Object data)
  {
    log.debug("Received Address " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTnonzeroaddress node, Object data)
  {
    log.debug("Received Address " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTport node, Object data)
  {
    log.debug("Received Port " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTdevicegroup node, Object data)
  {
    log.debug("Received Bus " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTonoff node, Object data)
  {
    log.debug("Received ON/OFF " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTdescription node, Object data)
  {
    log.debug("Description " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTdelay node, Object data)
  {
    log.debug("Delay " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTtimeout node, Object data)
  {
    log.debug("Timeout " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTzeroone node, Object data)
  {
    log.debug("ZeroOne " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTserviceversion node, Object data)
  {
    log.debug("Service Version " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTconnectionmode node, Object data)
  {
    log.debug("Connection Mode " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTcvno node, Object data)
  {
    log.debug("CV Number " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTprogmode node, Object data)
  {
    log.debug("Programming Mode Production" +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTcv node, Object data)
  {
    log.debug("CV Programming Mode " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTcvbit node, Object data)
  {
    log.debug("CVBIT Programming Mode " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTreg node, Object data)
  {
    log.debug("REG Programming Mode " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  
  public Object visit(ASTprotocol node, Object data)
  {
    log.debug("Protocol Production " +node.jjtGetValue() );
    //return node.childrenAccept(this,data);
    return data;
  }

  public Object visit(ASTdrivemode node, Object data)
  {
    log.debug("Drivemode Production " +node.jjtGetValue() );
    return data;
  }

  public Object visit(ASTfunctionmode node, Object data)
  {
    log.debug("Functionmode Production " +node.jjtGetValue() );
    return data;
  }

  public Object visit(ASTconnectionlitteral node, Object data)
  {
    log.debug("Connectionlitteral Production " +node.jjtGetValue() );
    return data;
  }

  public Object visit(ASTprotocollitteral node, Object data)
  {
    log.debug("Protocol Litteral Production " +node.jjtGetValue() );
    return data;
  }

  static Logger log = LoggerFactory.getLogger(SRCPVisitor.class.getName());

}
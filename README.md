See An [Gurux](http://www.gurux.org/ "Gurux") for an overview.

With gurux.serial component you can send data easily syncronously or asyncronously using serial port connection.

Join the Gurux Community or follow [@Gurux](https://twitter.com/guruxorg "@Gurux") for project updates.

Open Source GXSerial media component, made by Gurux Ltd, is a part of GXMedias set of media components, which programming interfaces help you implement communication by chosen connection type. Our media components also support the following connection types: network.

For more info check out [Gurux](http://www.gurux.org/ "Gurux").

With gurux.serial component you can send data easily syncronously or asyncronously.

We are updating documentation on Gurux web page. 

If you have problems you can ask your questions in Gurux [Forum](http://www.gurux.org/forum).

You can get source codes from http://www.github.com/gurux or if you use Maven add this to your POM-file:

```java
<dependency>
  <groupId>org.gurux</groupId>
  <artifactId>gurux.serial</artifactId>
  <version1.0.26</version>
</dependency>
```

Simple example
=========================== 
Before use you must set following settings:
* PortName
* BaudRate
* DataBits
* Parity
* StopBits

It is also good to add listener and start to listen following events.
* onError
* onReceived
* onMediaStateChange
* onTrace
* onPropertyChanged

```java

GXSerial cl = new GXSerial();
cl.setPortName(gurux.serial.GXSerial.getPortNames()[0]);
cl.setBaudRate(9600);
cl.setDataBits(8);
cl.setParity(Parity.ODD);
cl.setStopBits(StopBits.ONE);
cl.open();

```

Data is send with send command:

```java
cl.Send("Hello World!");
```
In default mode received data is coming as asynchronously from OnReceived event.

Event listener is adding class that you want to use to listen media events and derive class from IGXMediaListener.

```java
*/
 Media listener.
*/
public class GXSerialListener implements IGXMediaListener
{
	/** 
    Represents the method that will handle the error event of a Gurux component.

    @param sender The source of the event.
    @param ex An Exception object that contains the event data.
    */
    @Override
    void onError(Object sender, RuntimeException ex)
    {
		//Show occured error.
    }

    /** 
    Media component sends received data through this method.

    @param sender The source of the event.
    @param e Event arguments.
    */
    @Override
    void onReceived(Object sender, ReceiveEventArgs e)
    {
		//Handle received asyncronous data here.
    }

    /** 
    Media component sends notification, when its state changes.

    @param sender The source of the event.    
    @param e Event arguments.
    */
    @Override
    void onMediaStateChange(Object sender, MediaStateEventArgs e)
    {
		//Media is open or closed.
    }

    /** 
    Called when the Media is sending or receiving data.

    @param sender
    @param e
    @see IGXMedia.Trace Traceseealso>
    */
    @Override
    void onTrace(Object sender, TraceEventArgs e)
    {
		//Send and received data is shown here.
    }
    
    /** 
    Represents the method that will handle the System.ComponentModel.INotifyPropertyChanged.PropertyChanged
    event raised when a property is changed on a component.
    
    @param sender The source of the event.
    @param sender e A System.ComponentModel.PropertyChangedEventArgs that contains the event data.
    */
    @Override
    void onPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
		//Example port name is change.
    }      
}

```

Listener is registered calling addListener method.
```java
cl.addListener(this);

```

Data can be also send as syncronous if needed.

```java
synchronized (cl.getSynchronous())
{
    String reply = "";    
    ReceiveParameters<byte[]> p = new ReceiveParameters<byte[]>(byte[].class);    
    //End of Packet.
    p.setEop('\n'); 
    //How long reply is waited.   
    p.setWaitTime(1000);          
    cl.send("Hello World!", null);
    if (!cl.receive(p))
    {
        throw new RuntimeException("Failed to receive response..");
    }
}
```

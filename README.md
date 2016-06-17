#SE2-Beleg  
hey folks
#MQTT protocol Monitoring Tool -> EV3-Steuerung
channel is device id (e.g. "STP1001")  
"hello" - sent by Monitoring Tool, when it connects to mqtt broker  
"manual fix" - sent by Monitoring Tool, when the user pressed the button "Entstören"  
"emergency shutdown" - sent by Monitoring Tool, when the user pressed the button "Not-Aus"  
"debug true" - sent by Monitoring Tool, if the user toggled the button to on  
"debug false" - sent by Monitoring Tool, if the user toggled the button to off, it received a debug message while in off-mode or it receives a state message  
##Monitoring Tool requests emergency shutdown
Monitoring Tool sends "emergency shutdown"

#MQTT protocol EV3 -> Moitoring Tool
channel is device id (e.g. "STP1001") 
When "hello" is recieved - send immediately your current State back (IDLE,PROC,MAINT,DOWN)
When "manual fix" is recvieved - set fix [EV3_Brick] to true so it can get snacked by MAINT
When "emergency shutdown" is recieved - TODO
When "debug true" is recieved - Turns on Debug State -> send Debug messages
When "debug false" is recieved - Turns off Debug State -> no more Debug messages
"PROC", "MAINT", "IDLE", "DOWN" - sent by EV3-Steuerung, when it switches to the according state or a "hello" was received  
"debug <message>" sent by EV3-Steuerung when in Debug State to show debug message to user  

#MQTT protocol EV3 -> MES
channel is Device-specific Channel (e.g. "vwp/STP1001") 
When "confirm" is recieved - set confirm [EV3_Brick] to true so it can get snacked from IDLE or MAINT
When "sleep" is recvieved - set sleep [EV3_Brick] to true so it can get snacked from IDLE
When "produce:<rezeptid>" is recieved - set produce [EV3_Brick] to true and load up the next recipe in nextRecipe [EV3_Brick] so it can get snacked from IDLE to call new Proc(<Recipe>);

channel is MES Channel (e.g. "vwp/stiserver") (not fully updated but working)
Send StateIND
Send TaskIND
Send Register
Send Shutting down

"PROC", "MAINT", "IDLE", "DOWN" - sent by EV3-Steuerung, when it switches to the according state or a "hello" was received  
"debug <message>" sent by EV3-Steuerung when in Debug State to show debug message to user  



#Logging rules
##fatal
heavy user fail e.g. deviceID not in database
##error
heavy system fail e.g. database not accessible
##warn
light fail or non-defined behaviour e.g. unrecognized mqtt message received
##info
document program logic, what is done, e.g. "updating sql information"
##debug
debug method calls and print exceptions

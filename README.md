#SE2-Beleg  
hey folks
#MQTT protocol Monitoring Tool - EV3-Steuerung
channel is device id (e.g. "STP1001")
"hello" - sent by Monitoring Tool, when it connects to mqtt broker
"manual fix" - sent by Monitoring Tool, when the user pressed the button "Entstören"
"emergency shutdown" - sent by Monitoring Tool, when the user pressed the button "Not-Aus"
"debug true" - sent by Monitoring Tool, if the user toggled the button to on
"debug false" - sent by Monitoring Tool, if the user toggled the button to off, it received a debug message while in off-mode or it receives a state message
"PROC", "MAINT", "IDLE", "DOWN" - sent by EV3-Steuerung, when it switches to the according state or a "hello" was received
"debug <message>" sent by EV3-Steuerung to show debug message to user
##Monitoring Tool requests emergency shutdown
Monitoring Tool sends "emergency shutdown"
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

package ev3steuerung;

import ev3steuerung.rezeptabarbeitung.*;
import ev3steuerung.rezeptabarbeitung.Flag.CheckFor;
import ev3steuerung.rezeptabarbeitung.Flag.DevicePort;
import ev3steuerung.rezeptabarbeitung.Flag.SpinMode;
import ev3steuerung.rezeptabarbeitung.Flag.WaitMode;

import javax.json.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.HashMap;

public class JSONloader {

    private MessageDigest md5Digest;
    private String recipeID;
    private String checksum;
    private EV3_Brick ev3;

    public JSONloader() {

        this.ev3 = EV3_Brick.getInstance();
        //MD5 Erzeuger initalisieren
        this.md5Digest = null;
        try {
            this.md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private String getFileChecksum(MessageDigest digest, File file) throws IOException {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

    private Device[] getPortAssignment(JsonObject rezeptJSONData) throws IllegalArgumentException {

        String[] requiredMotorPort = {"A", "B", "C", "D"};
        String[] requiredSensorPort = {"S1", "S2", "S3", "S4"};
        Device[] init = {null, null, null, null, null, null, null, null};
        //repräsentiert EV3 Portanschlüsse: {A,B,C,D,S1,S2,S3,S4};

        Device d = null;
        int i;

        motorLoop:
        for (String s : requiredMotorPort) {              //alle MotorenPorts suchen
            if (!rezeptJSONData.containsKey("Port" + s))  //Ist Key überhaupt da
            {
                ev3.getMqttHelper().debug("Kein valides Rezept");
                ev3.getMqttHelper().debug("MotorPort " + s + " nicht definiert");
                throw new IllegalArgumentException("Rezept nicht verarbeitbar");
            } else if (rezeptJSONData.isNull("Port" + s)) {
                continue motorLoop;                        //Bei null Belegung garnix machen}
            } else if (rezeptJSONData.get("Port" + s).getValueType() != JsonValue.ValueType.STRING) {
                ev3.getMqttHelper().debug("Kein valides Rezept");
                ev3.getMqttHelper().debug("MotorPort " + s + " kein String oder null");
                throw new IllegalArgumentException("Rezept nicht verarbeitbar");
            } else {
                switch (rezeptJSONData.getString("Port" + s)) {  //Belegung suchen
                    case "LargeMotor":
                        d = new LargeMotor(s);
                        i = (s.equals("A")) ? 0 : (s.equals("B")) ? 1 : (s.equals("C")) ? 2 : 3;
                        init[i] = d;
                        break;
                    case "MediumMotor":
                        d = new MediumMotor(s);
                        i = (s.equals("A")) ? 0 : (s.equals("B")) ? 1 : (s.equals("C")) ? 2 : 3;
                        init[i] = d;
                        break;
                    default:
                        ev3.getMqttHelper().debug("Kein valides Rezept");
                        ev3.getMqttHelper().debug("MotorPort ungültig belegt");
                        throw new IllegalArgumentException("Rezept nicht verarbeitbar");
                }
            }
        }

        sensorLoop:
        for (String s : requiredSensorPort) {
            if (!rezeptJSONData.containsKey("Port" + s)) {
                ev3.getMqttHelper().debug("Kein valides Rezept");
                ev3.getMqttHelper().debug("SensorPort " + s + " nicht definiert");
                throw new IllegalArgumentException("Rezept nicht verarbeitbar");
            } else if (rezeptJSONData.isNull("Port" + s)) {
                continue sensorLoop; //Bei null Belegung garnix machen
            } else if (rezeptJSONData.get("Port" + s).getValueType() != JsonValue.ValueType.STRING) {
                ev3.getMqttHelper().debug("Kein valides Rezept");
                ev3.getMqttHelper().debug("SensorPort " + s + " kein String oder null");
                throw new IllegalArgumentException("Rezept nicht verarbeitbar");
            } else {
                switch (rezeptJSONData.getString("Port" + s)) {
                    case "TouchSensor":
                        d = new TouchSensor(s);
                        i = (s.equals("S1")) ? 4 : (s.equals("S2")) ? 5 : (s.equals("S3")) ? 6 : 7;
                        init[i] = d;
                        break;
                    case "ColorSensor":
                        d = new ColorSensor(s);
                        i = (s.equals("S1")) ? 4 : (s.equals("S2")) ? 5 : (s.equals("S3")) ? 6 : 7;
                        init[i] = d;
                        break;
                    default:
                        ev3.getMqttHelper().debug("Kein valides Rezept");
                        ev3.getMqttHelper().debug("SensorPort ungültig belegt");
                        throw new IllegalArgumentException("Rezept nicht verarbeitbar");
                }
            }
        }

        return init;
    }

    private boolean checkJSONVal(JsonObject val, String key, JsonValue.ValueType type) {

        if (!val.containsKey(key) || val.isNull(key)) {
            ev3.getMqttHelper().debug("Kein valides Rezept");
            ev3.getMqttHelper().debug(key + " ist nicht vorhanden oder leer");
            return false;
        } else if (val.get(key).getValueType() != type) {
            ev3.getMqttHelper().debug("Kein valides Rezept");
            ev3.getMqttHelper().debug(key + " ist nicht vom Type " + type.toString());
            return false;
        } else {
            return true;
        }
    }

    private DevicePort checkDeviceCall(CheckFor mode, String Port, Device[] allocation) throws IllegalArgumentException {
        DevicePort devicePortS = null;
        DevicePort devicePortM = null;

        //angegebenen Port überprüfen ob er mit der Belegung übereinstimmt
        switch (Port) {
            case "S1":
                devicePortS = DevicePort.SensorS1;
                break;
            case "S2":
                devicePortS = DevicePort.SensorS2;
                break;
            case "S3":
                devicePortS = DevicePort.SensorS3;
                break;
            case "S4":
                devicePortS = DevicePort.SensorS4;
                break;
            case "SpinMotorA":
                devicePortM = DevicePort.MotorA;
                break;
            case "SpinMotorB":
                devicePortM = DevicePort.MotorB;
                break;
            case "SpinMotorC":
                devicePortM = DevicePort.MotorC;
                break;
            case "SpinMotorD":
                devicePortM = DevicePort.MotorD;
                break;
            default:
                ev3.getMqttHelper().debug("Kein valides Rezept");
                ev3.getMqttHelper().debug("SpinBefehl enthält falschen Port für Sensor");
                throw new IllegalArgumentException("Rezept nicht verarbeitbar");
        }

        if (mode == CheckFor.Motor && devicePortM != null) {
            if (!(allocation[devicePortM.ordinal()] instanceof LargeMotor ||
                    allocation[devicePortM.ordinal()] instanceof MediumMotor)) {
                ev3.getMqttHelper().debug("Kein valides Rezept");
                ev3.getMqttHelper().debug("SpinBefehl für nicht mit Motor belegten Port");
                throw new IllegalArgumentException("Rezept nicht verarbeitbar");
            } else {
                return devicePortM;
            }
        } else if (mode == CheckFor.Sensor && devicePortS != null) {
            if (!(allocation[devicePortS.ordinal()] instanceof TouchSensor)) {
                ev3.getMqttHelper().debug("Kein valides Rezept");
                ev3.getMqttHelper().debug("SpinBefehl: Angegebener Sensor ist kein Touchsensor");
                throw new IllegalArgumentException("Rezept nicht verarbeitbar");
            } else {
                return devicePortS;
            }
        } else {
            ev3.getMqttHelper().debug("Kein valides Rezept");
            ev3.getMqttHelper().debug("SpinBefehl: Falscher Port angegeben");
            throw new IllegalArgumentException("Rezept nicht verarbeitbar");
        }
    }

    public String getHash(File file){

        checksum =  null;
        md5Digest.reset();
        try {
            checksum = getFileChecksum(md5Digest, file);
        } catch (IOException e) {
            e.printStackTrace();
            //todo Abbruch
            ev3.getMqttHelper().debug("konnte MD5 Algo nicht laden");
            return null;
        }

        return checksum;

    }

    public boolean buildDeuque (File file,HashMap<String, Object> recipesMap )

    {
    	
    	//Baut aus JSON-Rezept ein ArrayDeque und fügt es in die HashMap ein
    	
    	
        ArrayDeque<Object[]> recipeDeque = new ArrayDeque<Object[]>();
        Device[] init = {null, null, null, null, null, null, null, null};
        JsonObject rezeptJSONData;
        recipeID = null;
        String requiredID = "RecipeID"; //JSONbelegung für ID


        recipeDeque.clear();
        //Get the checksum MD5
        this.checksum = getHash(file);

        //Datei einlesen
        JsonReader reader = null;
        try {
            reader = Json.createReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //todo Abbruch
            ev3.getMqttHelper().debug("Datei nicht gefunden");
            return false;
        }

        //JSON als Objekt laden
        JsonStructure jsonst = null;
        try {
            jsonst = reader.read();
            // unsere Rezepte sind immer im "Root" ein Object
            rezeptJSONData = (JsonObject) jsonst;
        } catch (Exception e) {
            //e.printStackTrace();
            ev3.getMqttHelper().debug("Kein valides JSON");
            return false;
        }

        //RezeptID auslesen

        if (!checkJSONVal(rezeptJSONData, requiredID, JsonValue.ValueType.STRING))
        	return false;
        recipeID = rezeptJSONData.getString(requiredID);

        //Prüfen ob RezeptID doppelt ist
        if (recipesMap.containsKey(recipeID)) {
            ev3.getMqttHelper().debug("RezeptID bereits vergeben");
            ev3.getMqttHelper().debug("Rezept wird ignoriert");
            return false;
        }

        //1.Element im Deque bauen
        //Belegung der Ports als Device Array erhalten
        try {
            init = getPortAssignment(rezeptJSONData);
            recipeDeque.addLast(init.clone());
        } catch (IllegalArgumentException e) {
        	return false;
        }

        //Eigentliches Rezept holen
        JsonArray recipe = null;
        if (!checkJSONVal(rezeptJSONData, "Recipe", JsonValue.ValueType.ARRAY))
        	return false;
        recipe = rezeptJSONData.getJsonArray("Recipe");

        //Rezept auslesen, validieren und Übersetzen
        recipeInstructionLoop:
        for (JsonValue val : recipe) {

            if (val.getValueType() != JsonValue.ValueType.OBJECT) {
                ev3.getMqttHelper().debug("Kein valides Rezept");
                ev3.getMqttHelper().debug("Anweisung ist kein Objekt");
                return false;
            }

            JsonObject instruction = (JsonObject) val;
            boolean workMode = (instruction.size() > 1) ? Flag.PARALLEL : Flag.SEQENZIELL;

            //entscheiden welche Art der nächste Deque eintrag ist(Spin|Wait)
            Object[] dequeEntry;
            int spinCounter = 0;   //Zähler für ANZ parallele Spins
            if (instruction.containsKey("SpinMotorA") ||
                    instruction.containsKey("SpinMotorB") ||
                    instruction.containsKey("SpinMotorC") ||
                    instruction.containsKey("SpinMotorD")) {
                dequeEntry = new Spin[instruction.size()];
            } else if (instruction.containsKey("Wait")) {
                dequeEntry = new Wait[1];
            } else {
                ev3.getMqttHelper().debug("Kein valides Rezept");
                ev3.getMqttHelper().debug("Anweisung kein anerkannter Befehl");
                return false;
            }


            recipeCommandLoop:
            for (String commandName : instruction.keySet()) {

                if (workMode == Flag.PARALLEL && !commandName.contains("SpinMotor")) {
                    ev3.getMqttHelper().debug("Kein valides Rezept");
                    ev3.getMqttHelper().debug("Mehrere Befehle in einen Schritt die keine Spins sind");
                    return false;
                } else if (commandName.contains("SpinMotor")) {
                    //Prüfen ob alle notwendige Einstellungen da sind!
                    if (!checkJSONVal(instruction, commandName, JsonValue.ValueType.OBJECT)) {
                    	return false;
                    }
                    JsonObject spinCommandJSON = instruction.getJsonObject(commandName);
                    DevicePort devicePortM;

                    try {
                        devicePortM = checkDeviceCall(CheckFor.Motor, commandName, init);
                    } catch (IllegalArgumentException e) {
                    	return false;
                    }

                    //Checks ob Parameter vorhanden sind
                    Spin spinCommandDeque;

                    if (!checkJSONVal(spinCommandJSON, "Speed", JsonValue.ValueType.NUMBER))
                    	return false;

                    if (spinCommandJSON.containsKey("Till")) {
                        if (!checkJSONVal(spinCommandJSON, "Till", JsonValue.ValueType.STRING))
                        	return false;
                        if (!checkJSONVal(spinCommandJSON, "Sensor", JsonValue.ValueType.STRING))
                        	return false;

                        //angegebenen Sensor überprüfen
                        DevicePort devicePortS;
                        try {
                            devicePortS = checkDeviceCall(CheckFor.Sensor, spinCommandJSON.getString("Sensor"), init);
                        } catch (IllegalArgumentException e) {
                        	return false;
                        }

                        if (spinCommandJSON.getString("Till").equals("pressed")) {
                            spinCommandDeque = new Spin(spinCommandJSON.getInt("Speed"), devicePortM, devicePortS, SpinMode.Spin_Till_Pressed);
                        } else if (spinCommandJSON.getString("Till").equals("released")) {
                            spinCommandDeque = new Spin(spinCommandJSON.getInt("Speed"), devicePortM, devicePortS, SpinMode.Spin_Till_Released);
                        } else {
                            ev3.getMqttHelper().debug("Kein valides Rezept");
                            ev3.getMqttHelper().debug("SpinBefehl: Parameter Till falsch");
                            return false;
                        }
                    } else {

                        //"Einfacher" Spinbefehl mit angegebener Gradzahl
                        if (!checkJSONVal(spinCommandJSON, "Angle", JsonValue.ValueType.NUMBER))
                        	return false;
                        spinCommandDeque = new Spin(spinCommandJSON.getInt("Angle"), spinCommandJSON.getInt("Speed"), devicePortM, SpinMode.Turn_to_Angle);
                    }

                    dequeEntry[spinCounter] = spinCommandDeque;
                    spinCounter++;

                    //WAIT COMMANDS
                } else if (commandName.equals("Wait")) {

                    Wait waitCommandDeque;

                    if (!checkJSONVal(instruction, commandName, JsonValue.ValueType.OBJECT))
                    	return false;

                    JsonObject waitCommandJSON = instruction.getJsonObject(commandName);
                    if (waitCommandJSON.containsKey("Time")) {
                        if (checkJSONVal(waitCommandJSON, "Time", JsonValue.ValueType.NUMBER))
                            waitCommandDeque = new Wait(waitCommandJSON.getInt("Time"), WaitMode.Wait_Time);
                        else
                        	return false;

                    } else if (checkJSONVal(waitCommandJSON, "Sensor", JsonValue.ValueType.STRING)
                            && checkJSONVal(waitCommandJSON, "Till", JsonValue.ValueType.STRING)) {
                        DevicePort devicePortS;
                        try {
                            devicePortS = checkDeviceCall(CheckFor.Sensor, waitCommandJSON.getString("Sensor"), init);
                        } catch (IllegalArgumentException e) {
                        	return false;
                        }
                        if (waitCommandJSON.getString("Till").equals("pressed")) {
                            waitCommandDeque = new Wait(devicePortS, WaitMode.Wait_for_Press);
                        } else if (waitCommandJSON.getString("Till").equals("released")) {
                            waitCommandDeque = new Wait(devicePortS, WaitMode.Wait_for_Release);
                        } else {
                            ev3.getMqttHelper().debug("Kein valides Rezept");
                            ev3.getMqttHelper().debug("WaitBefehl: Parameter Till falsch");
                            return false;
                        }
                    } else {
                        ev3.getMqttHelper().debug("Kein valides Rezept");
                        ev3.getMqttHelper().debug("WaitBefehl: Parameter falsch");
                        return false;
                    }

                    dequeEntry[0] = waitCommandDeque;
                }
            }
            recipeDeque.addLast(dequeEntry.clone());
        }

        recipesMap.put(recipeID, checksum);
        recipesMap.put(recipeID + "_filename", file.getName());
        recipesMap.put(recipeID + "_deque", recipeDeque);
        
        return true;
    }

    public HashMap loadRecipes() {

        //HashMap zur Speicherung aller Rezepte
        //später nur noch hash vergleich ob änderung passiert ist => neuladen sonst cache benutzen
        //d.h. in einer Map verbindung zwischen Hash, Dateiname und RezeptID  abspeicher

        //RezeptSpeicher:
        //Key = Rezept ID
        //Value = MD5 -Hash
        //Key = ID+"Deque"
        //Value = Das Vollständige Rezept als Deque Objekt


        HashMap<String, Object> recipesMap = new HashMap<String, Object>();

        //Alle Dateien im Unterordner
        File recipesDir = new File("./recipes");
        if( !(recipesDir.exists() && recipesDir.canRead()) ){
            ev3.getMqttHelper().debug("recipes-Ordner nicht vorhanden oder kein Lesezugriff");
            return null;
        }

        //Jedes JSON im Ordner durchlaufen
        recipeFileLoop:
        for (File file : recipesDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File d) {
                return d.getName().toLowerCase().endsWith(".json");
            }
        })) {

            if(buildDeuque(file, recipesMap)){
            	//HashMap wurden erfolgreich 3 Key einträge hinzugefügt
            	//   recipeID    		- Der hash der Datei die das Rezept dieser ID enthält 
            	//	 recipeID_filename  - Dateiname 
            	//   recipeID_deque 	- erstelltes ArrayDegue = abarbeitbares Rezept
                ev3.getMqttHelper().debug(file +" erfolgreich eingelesen");
            }

        }
        
        return recipesMap;
        
    }
}

package iiis.systems.os.blockdb;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {
    public static JSONObject readJsonFile(String fileP)
    {
    	try
    	{
    		String tmp=new String(Files.readAllBytes(Paths.get(fileP)));
    		return new JSONObject(tmp);	
    	}catch(IOException err){return null;}
    }
    public static boolean writeJsonFile(String fileP, JSONObject obj)
    {
    	boolean flag=true;
    	try
    	{
    		Files.write(Paths.get(fileP),obj.toString().getBytes());
    	}catch(IOException err){flag=false;}
    	return flag;
    }
}

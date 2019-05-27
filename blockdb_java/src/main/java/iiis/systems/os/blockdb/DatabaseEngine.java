package iiis.systems.os.blockdb;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.util.HashMap;

public class DatabaseEngine {
    private static DatabaseEngine instance = null;
	static final String logFile="log.txt";
	static final int blockN=50; //the blocksize limit
	private JSONObject transLog;
    public static DatabaseEngine getInstance() {
        return instance;
    }

    synchronized public static void setup(String dataDir) {
        instance = new DatabaseEngine(dataDir);
    }

    private HashMap<String, Integer> balances = new HashMap<>();
    private int logLength = 0;
    private String dataDir;

    DatabaseEngine(String dataDir) {
        this.dataDir = dataDir;
        transLog=Util.readJsonFile(dataDir+logFile);
        if(transLog==null)
        {
        	transLog=new JSONObject();
        	transLog.put("numBlocks", 0);
        	transLog.put("Transactions",new JSONArray());
        	Util.writeJsonFile(dataDir+logFile,transLog);
        }
        int numBlocks=transLog.getInt("numBlocks");
        JSONArray transactions=transLog.getJSONArray("Transactions");
        logLength=transactions.length();
        for(int i=1;i<=numBlocks;i++) {
        	JSONObject block=Util.readJsonFile(dataDir+i+".json");
        	JSONArray blockTransactions=block.getJSONArray("Transactions");
	        for(Object trans:blockTransactions)
	        	applyTrans((JSONObject) trans);
        }
        for(Object transaction:transactions)
        	applyTrans((JSONObject) transaction);
    }

    private int getOrZero(String userId) {
        if (balances.containsKey(userId)) {
            return balances.get(userId);
        } else {
            return 0;
        }
    }
    
    private void applyTrans(JSONObject trans) {
    	String type=trans.getString("Type");
        int value=trans.getInt("Value");
    	String userID;
    	int balance;
    	switch (type)
    	{
    		case "PUT":
                userID=trans.getString("UserID");
    			balances.put(userID,value);
                break;
    		case "DEPOSIT":
                userID=trans.getString("UserID");
		        balance=getOrZero(userID);
		        balances.put(userID,balance+value);
                break;
    		case "WITHDRAW":
                userID=trans.getString("UserID");
		        balance=getOrZero(userID);
		        balances.put(userID,balance-value);
                break;
    		case "TRANSFER":
    			String fromID=trans.getString("FromID");
    			String toID=trans.getString("ToID");
    			int fromBalance=getOrZero(fromID);
    			int toBalance=getOrZero(toID);
    			balances.put(fromID,fromBalance-value);
    			balances.put(toID,toBalance+value);
                break;

    		default:
    			System.out.println("ERR: UNKNOWN TRANS TYPE");
    	}
    }
    
    private void addTransaction(int op,String userID,String fromID,String toID,int value) {
    	JSONObject trans=new JSONObject();
    	switch (op)
    	{
    		case putOp:
    			trans.put("Type","PUT");
    			trans.put("UserID",userID);
    			trans.put("Value",value);
                break;
    		case depositOp:
    			trans.put("Type","DEPOSIT");
    			trans.put("UserID",userID);
    			trans.put("Value",value);
                break;
    		case withdrawOp:
    			trans.put("Type","WITHDRAW");
    			trans.put("UserID",userID);
    			trans.put("Value",value);
                break;
    		case transferOp:
    			trans.put("Type","TRANSFER");
    			trans.put("FromID",fromID);
    			trans.put("ToID",toID);
    			trans.put("Value",value);
                break;
    		default:
    			System.out.println("ERR: UNKNOWN TRANS TYPE");
    	}
    	transLog.getJSONArray("Transactions").put(trans);
    	logLength++;
    	if(logLength==blockN)
    	{
    		int num=transLog.getInt("numBlocks")+1;
    		JSONObject block=new JSONObject();
    		block.put("BlockID",num);
    		block.put("PrevHash","00000000");
    		block.put("Transactions",transLog.getJSONArray("Transactions"));
    		block.put("Nonce","00000000");
    		Util.writeJsonFile(dataDir+num+".json",block);
    		transLog.put("numBlocks",num);
    		transLog.put("Transactions",new JSONArray());
            logLength=0;
    	}
        Util.writeJsonFile(dataDir+logFile,transLog);
    }

    synchronized public int get(String userId) {
        return getOrZero(userId);
    }

    synchronized public boolean put(String userId, int value) {
        //logLength++;
        if(value<0)return false;
        addTrans(putOp,userId,null,null,value);
        balances.put(userId, value);
        return true;
    }

    synchronized public boolean deposit(String userId, int value) {
        //logLength++;
        if(value<0)return false;
        addTrans(depositOp,userId,null,null,value);
        int balance = getOrZero(userId);
        balances.put(userId, balance + value);
        return true;
    }

    synchronized public boolean withdraw(String userId, int value) {
        //logLength++;
        if(value<0)return false;
        int balance = getOrZero(userId);
        if(balance<value)return false;
        addTrans(withdrawOp,userId,null,null,value);
        balances.put(userId, balance - value);
        return true;
    }

    synchronized public boolean transfer(String fromId, String toId, int value) {
        //logLength++;
        if(value<0)return false;
        int fromBalance = getOrZero(fromId);
        int toBalance = getOrZero(toId);
        if(fromBalance<value)return false;
        addTrans(transferOp,null,fromId,toId,value);
        balances.put(fromId, fromBalance - value);
        balances.put(toId, toBalance + value);
        return true;
    }

    public int getLogLength() {
        return logLength;
    }
    private final int putOp=1;
    private final int depositOp=2;
    private final int withdrawOp=3;
    private final int transferOp=4;
}


/**
*  XML RPC Server in java
*
* (c) 2016 Diwas Timilsina
**/

import java.util.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import org.apache.xmlrpc.webserver.WebServer;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.XmlRpcException;
import java.sql.*;
import java.io.*;

import java.util.concurrent.ConcurrentLinkedQueue;


public class Server {

  private static final String SEARCH_QUERY = "SELECT * FROM books WHERE " + "topic = ? COLLATE NOCASE";

  private static final String SERVER_RESPONSE = "\n++++++++++++++ Server Response+++++++++++++\n";
  private static final String SERVER_RESPONSE_END = "+++++++++++++++++++++++++++++++++++++++++++\n";

  //date is used by the log
  private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  private static Date date;

  private static String dbFilePath;

  private static Queue<String> log = new ConcurrentLinkedQueue<String>();


  // search operation by Client
  public Object[] search(String topic){
    Connection database = openDataBase();
    Vector<String> returnData = new Vector<String>();
    returnData.addElement(SERVER_RESPONSE);
    try {
      PreparedStatement stmt = database.prepareStatement(SEARCH_QUERY);
      stmt.setString(1, topic);
      ResultSet search = stmt.executeQuery();
      while (search.next()){
        String result = "\n";
        result += "ID: " + search.getInt("ID")+"\n";
        result += "Title: "+ search.getString("TITLE")+"\n";
        returnData.addElement(result);
      }
      stmt.close();
      database.close();
    } catch(Exception e) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      System.exit(0);
    }
    if (returnData.size() == 1){
      returnData.addElement("Sorry, we couldn't Find the item you are looking for!\n");
    }
    returnData.addElement(SERVER_RESPONSE_END);
    return returnData.toArray();
  }

  // lookup operation by Client
  public Object lookup(int item_number){
    String result = SERVER_RESPONSE;

      Connection database = openDataBase();
      Statement stmt = null;
      ResultSet search = null;

      try {
    	  stmt = database.createStatement();
    	  search = stmt.executeQuery("SELECT * FROM BOOKS WHERE ID is " + item_number + ";");

    	  result += "ID: " + search.getInt("ID")+"\n";
    	  result += "Title: "+ search.getString("TITLE")+"\n";
    	  result += "Topic: "+ search.getString("TOPIC")+"\n";
    	  result += "Price: "+ search.getFloat("PRICE")+"\n";
    	  result += "In Stock: ";
    	  result += (search.getInt("STOCK") > 0) ? "Yes\n" : "No\n";

      }catch(Exception e) {
    	  System.err.println( e.getClass().getName() + ": " + e.getMessage());
    	  result = SERVER_RESPONSE + "\nSorry, We Couldn't find the item that you are looking for!\n";
      }finally {

    	  if (stmt != null){
    	      try { stmt.close(); } catch (SQLException e) { System.err.println("Failed to close lookup statement"); }
    	  }

    	  if (database != null) {
    	      try { database.close(); } catch (SQLException e) { System.err.println("Failed to close database in lookup statement"); }
    	  }

    	  if (search != null){
    	      try { search.close(); } catch (SQLException e) { System.err.println("Failed to close lookup search statement"); }
	      }
      }

    result += SERVER_RESPONSE_END;
    return result;
  }

  // buy the item associated with the item number
  public Object buy(int item_number) {
     String result = SERVER_RESPONSE;
     Connection database = openDataBase();
     Statement stmt = null;

  	try {
  	    stmt = database.createStatement();
  	    int success = stmt.executeUpdate("UPDATE books SET stock = stock - 1  WHERE ID = " + item_number + " AND stock > 0;");
  	    database.commit();

  	    if (success > 0) {
      		updateLog(item_number);
      		result += "Successfully purchased item " + item_number + ".\n";
  	    } else {
  		    result += "Failed to purchase item " + item_number + ". (Item out of stock)\n";
  	    }
  	} catch (SQLException e) {
  	    result += "Failed to purchase item " + item_number + ". (SQL failure)\n";
  	} finally {
  	    if (stmt != null){
  		      try { stmt.close(); } catch (SQLException e) { }
  	    }

  	    if (database != null) {
  		      try { database.close(); } catch (SQLException e) { }
  	    }
  	}

  	result += SERVER_RESPONSE_END;
  	return result;
  }


  // update the log file
  private static void updateLog(int item_number){
      date = new Date();
      log.add("item: "+item_number+" was purchased on "+dateFormat.format(date));
  }

  //print the current log
  private static void log(){
      for (String line : log) {
	       System.out.println(line);
      }
  }

  //update the price
  private void update(int item_number, float price){
    Connection database = openDataBase();
    Statement stmt = null;
    try {
      stmt = database.createStatement();
      int num_changed = stmt.executeUpdate("UPDATE BOOKS set PRICE = "+ price +" where ID"+"="+item_number+";");
      database.commit();
      stmt.close();
      database.close();

      if (num_changed == 0) {
	       System.out.println("No records changed.");
	       return;
      }
    } catch (SQLException e) {
	     System.err.println("Failed to update price");
	     return;
    }
    println("update Successfull");
  }

  //restock the books
  private void restock(int item_number, int stockCount){
    Connection database = openDataBase();
    Statement stmt = null;
    try {
      stmt = database.createStatement();
      int num_changed = stmt.executeUpdate("UPDATE BOOKS set STOCK = " + stockCount + " where ID = "+item_number+";");
      database.commit();
      stmt.close();
      database.close();

      if (num_changed == 0) {
	       System.out.println("No records changed.");
	       return;
      }
    } catch (SQLException e) {
	     System.err.println("Couldn't restock");
	     return;
    }
    println("restocking Successfull");
  }

  // print statement made easier
  public static void println(String print_text){
    System.out.println(print_text);
  }
  // print statement made easier
  public static void println(int print_int){
    System.out.println(print_int);
  }

  // open the existing database or create a new database if
  // needed
  public Connection openDataBase(){
    Connection c = null;
    try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
      c.setAutoCommit(false);
    } catch ( Exception e ) {
      System.err.println("Failed to open database");
    }
    return c;
  }


  public static void main(String[] args){
      if (args.length < 2) {
	       System.err.println("usage: java Server <port> <sqlite db file>");
	       System.exit(1);
      }

      int port = -1;
      try {
	       port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
	       System.err.println("A port number must be an integer.");
	       System.exit(1);
      }

      Server.dbFilePath = args[1];


    try {
      println("Appempting to Start the Server ...");
      PropertyHandlerMapping phm = new PropertyHandlerMapping();
      XmlRpcServer xmlRpcServer;
      WebServer server = new WebServer(port);
      xmlRpcServer = server.getXmlRpcServer();
      phm.addHandler("sample", Server.class);
      xmlRpcServer.setHandlerMapping(phm);

      server.start();

      println("Started successfully.");
      println("Accepting requests. (Halt program to stop.)");

      Server new_server = new Server();
      Scanner s;
      String func;

      try{
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input;

        while((input=br.readLine())!=null){
          try {
            s = new Scanner(input);
            func = s.next();
            if (func.equals("log")){
		            log();
            }else if (func.equals("update")){
		            new_server.update(s.nextInt(),(float)s.nextInt());
            }else if (func.equals("restock")){
		            new_server.restock(s.nextInt(),s.nextInt());
            }else {
              println("Invalid Input, can only support log, update, or restock\n");
            }
          } catch (Exception e){
            println("Invalid Input, can only support log, update, or restock\n");
            continue;
          }
        }
      }catch(Exception e){
        System.err.println( e.getClass().getName() + ": " + e.getMessage());
      }
    } catch (Exception exception){
      println("Server error: " + exception);
    }
  }

}

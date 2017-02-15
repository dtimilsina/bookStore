/**
  *  XML RPC Client in java
  *
  *  Clients can make the following calls:
  *  1) lookup and buy books based on book unique id
  *  2) search books based on category of books
  *
  *  Author: 2016 Diwas Timilsina
**/

import java.util.*;
import java.io.*;
import java.net.URL;
import org.apache.xmlrpc.*;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class Client {

    /*
     * Search call from the client
     * @param client, client instance who is going to nake the RPC call
     * @param topic, category of the books that the client wants to lookup
    */

    public static void serverSearch(XmlRpcClient client, String topic){
      String [] params = {topic};
      try {
        Object [] result = (Object []) client.execute("sample.search",params);
        for (int i =0 ; i < result.length ;i++){
          println((String)result[i]);
        }
      } catch (Exception e){
        System.err.println("Client: " + e);
      }
    }

    /*
     * Lookup call from the client
     * @param client, client instance who is going to nake the RPC call
     * @param id, id of the book that the client wants to lookup
    */

    public static void serverLookup(XmlRpcClient client, int id){
      Integer [] params = {id};
      try{
        Object result = (Object) client.execute("sample.lookup",params);
        println((String)result);
      } catch (Exception e){
        System.err.println("Client: " + e);
      }
    }

    /*
     *  Buy call from the client
     *  @param client, client instance who is going to nake the RPC call
     *  @param id, id of the book that is to be purchased
    */

    public static void serverBuy(XmlRpcClient client, int id){
      Integer [] params = {id};
      try {
        Object result = (Object) client.execute("sample.buy",params);
        println((String)result);
      } catch (Exception e) {
        System.err.println("Client: " + e);
      }
    }

    // print statement made easier
    public static void println(String print_text){
      System.out.println(print_text);
    }

    // print statement made easier
    public static void println(int print_int){
      System.out.println(print_int);
    }


    /**
    *  Main function for client
    */

    public static void main (String [] args) {

        // Client needs to specify the server address and the port for connection
      	if (args.length < 2) {
      	    System.err.println("usage: java Client <server address> <port>");
      	    System.exit(1);
      	}

        // configure client
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        XmlRpcClient client=null;

        try {
            config.setServerURL(new URL("http://" + args[0] + ":" + args[1]));
            client = new XmlRpcClient();
            client.setConfig(config);
        } catch (Exception e) {
            // in case client is not able to establish connection with the server
            println("Client error: "+ e);
            java.lang.System.exit(1);
        }


        Scanner s;
        String func;
        try{

            // read input from the client standard input
		        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		        String input;

		        while((input=br.readLine())!=null){

              // while there is input from the client,
              // look for the supported functions (search, lookup, buy)
              // then make RPC call to the server
              try {
                s = new Scanner(input);
                func = s.next();
			          if (func.equals("search")){
                  serverSearch(client,(s.nextLine()).substring(1));
                }else if (func.equals("lookup")){
                  serverLookup(client,s.nextInt());
                }else if (func.equals("buy")){
                  serverBuy(client,s.nextInt());
                }else {
                  println("Invalid Input, can only support search, lookup, or buy");
                }

              } catch (Exception e){
                  // in case when the input is invalid
                  println("Invalid Input, can only support search, lookup, or buy");
                continue;
              }
		        }

      	}catch(Exception e){
          // if the server failure occurs inform the client with some useful message
      		System.err.println( e.getClass().getName() + ": " + e.getMessage());
      	}
    }
}

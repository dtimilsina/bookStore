/**
  *  XML RPC Client in java
  *
  * (c) 2016 Diwas Timilsina
**/

import java.util.*;
import java.io.*;
import java.net.URL;
import org.apache.xmlrpc.*;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class Client {

    // print statement made easier
    public static void println(String print_text){
      System.out.println(print_text);
    }
    // print statement made easier
    public static void println(int print_int){
      System.out.println(print_int);
    }

    // search call
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

    // lookup call
    public static void serverLookup(XmlRpcClient client, int id){
      Integer [] params = {id};
      try{
        Object result = (Object) client.execute("sample.lookup",params);
        println((String)result);
      } catch (Exception e){
        System.err.println("Client: " + e);
      }
    }

    // buy call
    public static void serverBuy(XmlRpcClient client,int id){
      Integer [] params = {id};
      try {
        Object result = (Object) client.execute("sample.buy",params);
        println((String)result);
      } catch (Exception e) {
        System.err.println("Client: " + e);
      }
    }

    public static void main (String [] args) {
      	if (args.length < 2) {
      	    System.err.println("usage: java Client <server address> <port>");
      	    System.exit(1);
      	}

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        XmlRpcClient client=null;

        try {
            config.setServerURL(new URL("http://" + args[0] + ":" + args[1]));
            client = new XmlRpcClient();
            client.setConfig(config);
        } catch (Exception e) {
            println("Client error: "+ e);
            java.lang.System.exit(1);
        }

        Scanner s;
        String func;
        try{
		        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		        String input;

		        while((input=br.readLine())!=null){
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
                  println("Invalid Input, can only support search, lookup, or buy");
                continue;
              }
		        }
      	}catch(Exception e){
      		System.err.println( e.getClass().getName() + ": " + e.getMessage());
      	}
    }
}

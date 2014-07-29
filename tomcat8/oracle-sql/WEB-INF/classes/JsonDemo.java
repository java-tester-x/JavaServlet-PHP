import java.io.*;

 import javax.json.*;
 import javax.json.stream.*;
 import javax.json.stream.JsonParser.*;

 public class JsonDemo {
     private String address = "./json_demo.js";

     public static void main(String[] args) throws Exception {
         JsonDemo demo = new JsonDemo();
         demo.write();
         demo.read();
     }

     public void write() throws Exception{
         JsonBuilderFactory factory = Json.createBuilderFactory(null);
         JsonArray jsonArray = factory.createArrayBuilder()
             .add(factory.createObjectBuilder()
                 .add("username", "zhijia,.zhang")
                 .add("email", "jiahut@gmail.com"))
             .add(factory.createObjectBuilder()
                 .add("username", "lishi")
                 .add("email", "lishi@demo.com"))
             .build();
         try (JsonWriter jsonWriter = Json.createWriter(new FileWriter(address))) {
               jsonWriter.writeArray(jsonArray);
         }
     }

     public void read() throws Exception{
         JsonParserFactory factory = Json.createParserFactory(null);
         StringBuffer stb = new StringBuffer();
         String str = null;

         try(BufferedReader rd = new BufferedReader(new FileReader(address))){
             while((str = rd.readLine()) != null){
                 stb.append(str);
             }
         }

         JsonParser parser = factory.createParser(new StringReader(stb.toString()));
         while (parser.hasNext()) {
           Event event = parser.next();
           switch (event) {
             case KEY_NAME: {
               System.out.print(parser.getString() + "="); break;
             }
             case VALUE_STRING: {
               System.out.println(parser.getString()); break;
             }
           } // end switch
         }// end while
     }
 }
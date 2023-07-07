package assembler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) throws IOException{
    List<String> list = new ArrayList<>();
    list.add("a");
    list.add("b");
    list.add("c");
    int p = list.indexOf("a");
    String str = "";
    try{
      str += list.get(p);
    }catch (Exception e){
      System.out.println("Did not found");
    }
    System.out.println(str);
  }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TaskManagementSystem;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientSide {

    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 8800);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Scanner in = new Scanner(socket.getInputStream());
                Scanner in2 = new Scanner(System.in);){

            System.out.println("Connected to server");
            
            String line;
            boolean running = true;
            while (running) {

                while (in.hasNextLine()) {
                    line = in.nextLine();
                    System.out.println(line);

                    
                    if (line.startsWith("Enter") || 
                        line.startsWith("Assigned to who"))  {
                        
                        if (in2.hasNextLine()) { 
                            String input = in2.nextLine();
                            out.println(input); 
                            
                            if(input.equals("0")){
                                break;
                            }
                        }
                    }
                }
                running = false;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

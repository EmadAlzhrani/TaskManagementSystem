/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TaskManagementSystem;


import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.lang.NumberFormatException;


public class Server {

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(8800);

        System.out.println("Server waiting Connection...");
        while (true) {
            Socket client = server.accept();

            ServerThread m = new ServerThread(client);
            m.start();
        }
    }
}

class ServerThread extends Thread {

    Socket client;

    public ServerThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        String url = "jdbc:oracle:thin:@localhost:1521:XE";
        String username = "system";
        String password = "emad";
        try (PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                InputStream inStream = client.getInputStream();
                Scanner in = new Scanner(inStream);
                Connection conn = DriverManager.getConnection(url, username, password);) {
            System.out.println(Thread.currentThread());

            OUTER:
            while (true) {
                String menu = "Menu:\n"
                        + "1. View Tasks\n"
                        + "2. Add Task\n"
                        + "3. Delete Task\n"
                        + "4. Assign/Unassign tasks\n"
                        + "0. Exit";
                out.println(menu);
                out.println("Enter a choice: ");
                String choice = in.next();
                in.nextLine();
                switch (choice) {
                    case "0":
                        out.println("Connection closing...");
                        break OUTER;
                    case "1": {
                        PreparedStatement result = conn.prepareStatement("SELECT * FROM tasks");
                        ResultSet rs = result.executeQuery();
                        out.println("-------------------------");
                        while (rs.next()) {
                            out.println("Task ID: " + rs.getInt(1));
                            out.println("Task: " + rs.getNString(2));
                            out.println("Assigned to: " + rs.getString(3));
                            out.println("Deadline: " + rs.getDate(4));
                        }
                        out.println("-------------------------");
                        break;
                    }
                    case "2": {
                        out.println("Enter task ID: ");
                        String input = in.nextLine().trim();
                        int taskid;
                        try {
                            taskid = Integer.parseInt(input);
                            if(taskid<1){
                                out.println("Invalid input. Please enter a number.");
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            out.println("Invalid input. Please enter a number.");
                            continue;
                        }
                        out.println("Enter the task: ");
                        String taskName = in.nextLine();
                        
                        out.println("Assigned to who: ");
                        String name = in.nextLine();
                        
                        out.println("Enter the deadline for example(yyyy-MM-dd): ");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String stringDate = in.nextLine();
                        
                        if (!stringDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                            out.println("Invalid date format. Please use yyyy-MM-dd format.");
                            continue;
                        }
                        java.util.Date date = dateFormat.parse(stringDate);
                        Date sqlDate = new Date(date.getTime());
                        java.util.Date date2 = new java.util.Date();
                        if(sqlDate.before(date2)){
                            out.println("Invalid Date");
                            continue;
                        }
                        String command = "INSERT INTO tasks(taskid, taskname, assigned, deadline) VALUES(?, ?, ?, ?)";
                        PreparedStatement statement = conn.prepareStatement(command);
                        statement.setInt(1, taskid);
                        statement.setString(2, taskName);
                        statement.setString(3, name);
                        statement.setDate(4, sqlDate);
                        try {
                            statement.executeUpdate();
                            out.println("Task added.");
                        } catch (SQLException e) {
                            out.println("The task id number is already used.");
                        }
                        break;
                    }
                    case "3": {
                        String command = "DELETE FROM tasks WHERE taskid=?";
                        PreparedStatement statement = conn.prepareStatement(command);
                        
                        out.println("Enter task ID you want to delete: ");
                        int taskid = Integer.parseInt(in.nextLine().trim());
                        statement.setInt(1, taskid);
                        
                        int result = statement.executeUpdate();
                        if (result > 0) {
                            out.println("Task deleted.");
                        } else {
                            out.println("Error deleting task.");
                        }
                        break;
                    }
                    case "4": {
                        String command = "UPDATE tasks SET assigned=? WHERE taskid=?";
                        PreparedStatement statement = conn.prepareStatement(command);
                        out.println("Enter task ID you want to changed assigned: ");
                        
                        int taskid;
                        try {
                            taskid = Integer.parseInt(in.nextLine().trim());
                        } catch (NumberFormatException e) {
                            out.println("Invalid input. Please enter a number.");
                            continue;
                        }
                        
                        out.println("Assigned to who?: ");
                        String name = in.nextLine();
                        
                        statement.setString(1, name);
                        statement.setInt(2, taskid);
                        int result = statement.executeUpdate();
                        if (result > 0) {
                            out.println("Change is successful.");
                        } else {
                            out.println("Error updating task.");
                        }
                        break;
                    }
                    default: {
                        out.println("Not an option, try again.");
                        break;
                    }

                }
            }
        } catch (IOException e) {
            System.out.println("ServerThread error: " + e.getMessage());
        } catch (SQLException ex) {
            System.out.println(ex);
        } catch (ParseException ex) {
            System.out.println(ex);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}

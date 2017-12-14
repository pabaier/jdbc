import java.sql.*;
import edu.cofc.grader.*;
import java.util.Scanner;

public class JDBCTest {
    public static final String CANCEL = "x"; 

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        // hides reflection warning
        System.err.close();
        System.setErr(System.out);

        // added sslmode=require for this error:
        // org.postgresql.util.PSQLException: FATAL: no pg_hba.conf entry for host "153.9.254.97", user "apazcnnonlphuu", database "ddk4jv2r4nkpts", SSL off
        String dbURL = "jdbc:postgresql://ec2-54-221-204-161.compute-1.amazonaws.com:5432/ddk4jv2r4nkpts?sslmode=require";        
        String user = "apazcnnonlphuu";
        String pass = "5994c348a2250296c48f0eb5eeedd52ea45a960cbc7ebbe9902b57dc9d5d72e2";
        
        try {
            Class.forName("org.postgresql.Driver");
            Connection myConnection  = DriverManager.getConnection(dbURL, user, pass);
            Statement myStatement = myConnection.createStatement();
            String input = "";
            while(!input.equalsIgnoreCase("q")) {
                System.out.print("What would you like to do? (type '" + CANCEL + "' any time to cancel) ");
                input = in.nextLine();
                boolean res = false;
                switch(input) {
                    case "i":
                        System.out.println("insert");
                        res = insert(myStatement);
                        if(res)
                            System.out.println("insert successful!");
                        else
                            System.out.println("insert aborted!");
                        break;
                    case "r":
                        System.out.println("remove");
                        res = delete(myStatement);
                        if(res)
                            System.out.println("delete successful!");
                        else
                            System.out.println("delete aborted!");
                        break;
                    case "u":
                        System.out.println("update");
                        res = update(myStatement);
                        if(res)
                            System.out.println("update successful!");
                        else
                            System.out.println("update aborted!");
                        break;
                    case "p":
                        printTable(myStatement, "employees");
                        break;
                    case "pr":
                        printResult(query(myStatement, "employees", "id > 100"));
                        break;
                    case "h":
                        usage();
                        break;
                    case CANCEL:
                        return;
                    default:
                        usage();
                        break;
                }
                System.out.println();
                printTable(myStatement, "employees");
                System.out.println();
            }
            
        }
        catch(SQLException | ClassNotFoundException e) {
            System.out.println(e);
        }
        
    }

    public static boolean delete(Statement s) {
        Scanner in = new Scanner(System.in);
        System.out.print("From which table would you like to delete an entry? ");
        String table = in.nextLine();
        if(table.equals(CANCEL)) return false;
        System.out.print("Enter the ID of the entry you would like to remove: ");
        int id = 0;
        try{
            id = in.nextInt();
            in.nextLine();
        }
        catch(Exception e) {
            return false;
        }

        String delete = "DELETE FROM " + table + " WHERE id = " + id;

        try {
            s.execute(delete);
            return true;
        }
        catch(SQLException e) {
            System.out.println(e);
            return false;
        }
    }
    
    public static boolean update(Statement s) {
        Scanner in = new Scanner(System.in);
        System.out.print("Which table would you like to update? ");
        String table = in.nextLine();
        if(table.equals(CANCEL)) return false;
        System.out.print("What is the ID of the entry you want to update? ");
        int id = 0;
        try {
            id = in.nextInt();
            in.nextLine();
        }
        catch(Exception e) {
            return false;
        }
        System.out.print("What attribute do you want to update? ");
        String attribute = in.nextLine();
        if(attribute.equals(CANCEL)) return false;

        if(attribute.equals("age")) {
            System.out.print("What is the new value? ");
            int value = 0;
            try {
                value = in.nextInt();
                in.nextLine();
            }
            catch(Exception e) {
                return false;
            }
            try {
                String update = "UPDATE " + table + " SET " + attribute + 
                                " = " + value + " WHERE id = " + id;
                s.executeUpdate(update);
                return true;
            }
            catch(Exception e) {
                System.out.println(e);
                return false;
            }
        }
        else {
            System.out.print("What is the new value? ");
            String value = in.nextLine();
            if(value.equals(CANCEL)) return false;
            try {
                String update = "UPDATE " + table + " SET " + attribute + 
                                " = '" + value + "' WHERE id = " + id;
                s.executeUpdate(update);
                return true;
            }
            catch(Exception e) {
                System.out.println(e);
                return false;
            }
        }    
    }

    public static boolean insert(Statement s) {
        Scanner in = new Scanner(System.in);
        System.out.print("Enter the table name: ");
        String table = in.nextLine();
        if(table.equals(CANCEL)) return false;
        System.out.print("Enter the first name: ");
        String first = in.nextLine();
        if(first.equals(CANCEL)) return false;
        System.out.print("Enter the last name: ");
        String last = in.nextLine();
        if(last.equals(CANCEL)) return false;
        System.out.print("Enter the age: ");
        int age = 0;
        try {
            age = in.nextInt();
        }
        catch(Exception e) {
            return false;
        }
        return insert(s, table, first, last, age);
    }

    // creates an entry into the table
    public static boolean insert(Statement s, String table, String fname, String lname, int age) {
        try {
            ResultSet r = s.executeQuery("SELECT max(ID) from " + table);
            r.next();
            int id = r.getInt(1);
            id ++;
            String insert = "INSERT INTO " + table + " " +
            "VALUES (" + id + ", '" + fname + "', '" + lname + "', " + age + ")";
            s.executeUpdate(insert);
            return true;
        }
        catch(SQLException e) {return false;}
    }

    public static void printResult(ResultSet r) {
        if(r == null) return;
        ResultSetMetaData md = null;
        try {
            md = r.getMetaData();
            for(int i = 1; i <= md.getColumnCount(); i++) {
                System.out.println(md.getColumnName(i) + " " + md.getColumnType(i));
            }
        }
        catch(SQLException e) {
            System.out.println("printResult " + e);
        }
    }

    public static ResultSet query(Statement s, String table, String condition) {
        String query = "select first from " + table + " WHERE " + condition;
        try {
            return s.executeQuery(query);
        }
        catch(SQLException e) {
            System.out.println("query method: " + e);
            return null;
        }
    }

    // query table, parse and print results
    public static void printTable(Statement s, String table) {
        String tableHeaderFormat = "| %-5s | %-11s | %-11s | %-5s |\n";
        String tableDataFormat = "| %-5d | %-11s | %-11s | %-5d |\n";
        System.out.format(tableHeaderFormat, "id",
                                            "first name",
                                            "last name", 
                                            "age");
        try {
            ResultSet r = s.executeQuery("select * from " + table);
            while(r.next()) {
                // you can get values by column name or column number (starting from 1!)
                System.out.format(tableDataFormat, r.getInt(1), r.getString(2), r.getString("last"), r.getInt("age"));
            }
        }
        catch(SQLException e) {}
    }

    // creates table - can only run once
    public static void createTable(Statement s, String name) {
            String sql = "CREATE TABLE " + name +
            " (id INTEGER not NULL, " +
            " first VARCHAR(255), " + 
            " last VARCHAR(255), " + 
            " age INTEGER, " + 
            " PRIMARY KEY ( id ))";
            try {
                s.executeUpdate(sql);
            }
            catch(SQLException e){}
    }

    public static void usage() {
        System.out.println("Here are the command options:");
        System.out.println("    p - prints the table");
        System.out.println("    c - creates a new entry");
        System.out.println("    r - removes an entry");
        System.out.println("    u - updates an entry");
        System.out.println("    h - prints the usage");
        System.out.println();
    }

}
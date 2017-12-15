import java.io.IOException;
import java.sql.*;
import edu.cofc.grader.*;
import java.util.Scanner;
import java.util.Arrays;
import java.util.List;

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
            boolean play = true;
            while(play) {
                System.out.print("What would you like to do? (type '" + CANCEL + "' any time to cancel) ");
                input = in.nextLine();
                play = parser(myStatement, input);
                System.out.println();
            } 
        }
        catch(SQLException | ClassNotFoundException e) {
            System.out.println(e);
        }
        
    }

    public static boolean parser(Statement myStatement, String input) {
        boolean res = false;
        List<String> inputFlags = Arrays.asList(input.split(" "));
        switch(inputFlags.get(0)) {
            case "c":
                System.out.println("create");
                res = insert(myStatement);
                if(res)
                    System.out.println("create successful!");
                else
                    System.out.println("create aborted!");
                break;
            case "d":
                System.out.println("delete");
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
            case "r":
                printTable(myStatement, "employees");
                break;
            case "h":
                usage();
                break;
            case "o":
                String arg1;
                String arg2;
                QueryBuilder qb = new QueryBuilder();
                qb.setTable("employees");
                try {
                    arg1 = inputFlags.get(1);
                    arg2 = inputFlags.get(2);
                }
                catch(Exception e) {
                    printTable(myStatement, "employees");
                    break;
                }
                qb.setOrderBy(arg1 + " " + arg2);
                printResult(query(myStatement, qb.build()));
                break;
            case "clear":
                clear();
                break;
            case CANCEL:
                return false;
            default:
                usage();
                break;
        }
        return true;
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
        
        try {
            ResultSetMetaData md = r.getMetaData();
            System.out.println("\nTabel: " + md.getTableName(1));
            int[] columns = new int[md.getColumnCount()];
            StringBuilder tableHeadFormat = new StringBuilder();
            StringBuilder tableDataFormat = new StringBuilder();
            Object[] columnNames = new String[columns.length];
            for(int i = 1; i <= md.getColumnCount(); i++) {
                tableHeadFormat.append("| ");
                tableDataFormat.append("| ");
                columns[i-1] = md.getColumnType(i);
                if(md.getColumnType(i) == Types.INTEGER) {
                    tableHeadFormat.append("%-5s");
                    tableDataFormat.append("%-5d");
                }
                else {
                    tableHeadFormat.append("%-11s");
                    tableDataFormat.append("%-11s");
                }
                columnNames[i-1] = md.getColumnName(i);
                tableHeadFormat.append(" ");
                tableDataFormat.append(" ");
            }
            tableHeadFormat.append("|\n");
            tableDataFormat.append("|\n");
            System.out.format(tableHeadFormat.toString(), columnNames);
            
            while(r.next()) {
                Object[] output = new Object[columns.length];
                for(int i = 0; i < columns.length; i++) {
                    switch(columns[i]) {
                        case Types.INTEGER:
                            output[i] = r.getInt(i + 1);
                            break;
                        case Types.VARCHAR:
                            output[i] = r.getString(i + 1);
                            break;
                        default:
                            output[i] = " - ";
                    }
                }
                System.out.format(tableDataFormat.toString(), output);
            }
        }
        catch(SQLException e) {
            System.out.println("printResult " + e);
        }
    }

    public static ResultSet query(Statement s, String query) {
        try {
            return s.executeQuery(query);
        }
        catch(SQLException e) {
            System.out.println("query method: " + e);
            return null;
        }
    }

    // prints the entire table
    public static void printTable(Statement s, String table) {
        QueryBuilder qb = new QueryBuilder();
        ResultSet r = query(s, qb.setTable(table)
                                 .setOrderBy("id asc")
                                 .build());
        printResult(r);
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

    public static void clear() {
        // String ANSI_CLS = "\033c";
        String ANSI_CLS = "\u001b[2J";
        String ANSI_HOME = "\u001b[H";
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.flush();
    }

    public static void usage() {
        System.out.println("Here are the command options:");
        System.out.println("    c - creates a new entry");
        System.out.println("    r - prints the table");
        System.out.println("    u - updates an entry");
        System.out.println("    d - deletes an entry");
        System.out.println("    o [order column] [asc/desc] - orders the table");
        System.out.println("             |          -> Ascending or descending");
        System.out.println("             |             -> (Default if omitted is ascending)");
        System.out.println("             -> the column by which to order");
        // System.out.println("    f [field1] [field1] ... [fieldN] - filters the table by field");
        // System.out.println("             |          -> Ascending or descending");
        // System.out.println("             |             -> (Default if omitted is ascending)");
        // System.out.println("             -> the column by which to order");
        System.out.println("    h - prints the usage");
        System.out.println();
    }



}
import com.google.common.collect.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public class logcomp {

    private static String DB_DRIVER;
    private static String DB_CONNECTION;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static int typSize;
    private static int specSize;
    private static int catSize;

    public static void main(String[] argv) {

        work_Database();

    }

    private static void work_Database(){
        Generator gen = new Generator();
        Statement st;
        String sql;
        ResultSet rs;
        SortedSetMultimap<String, Integer> tablesID = TreeMultimap.create();
        getProperties();
        Connection conn = getDBConnection();

        try {
            //--------------- Truncate primary keys ---------------
            st = conn.createStatement();
            st.executeUpdate("TRUNCATE \"Customers\"," +
                    " \"Employees\", " +
                    "\"Shippers\", \"Orders\", " +
                    "\"OrderDetails\", \"Categories\"," +
                    " \"Suppliers\", " +
                    "\"Products\", \"OrderProducts\" RESTART IDENTITY;");
            st.close();

            //--------------- Tables' variables and foreign keys ---------------
            int customer_FK, employee_FK, shipper_FK, order_FK, product_FK, supplier_FK, category_FK;
            String companyName, city, street, phone, contactName, firstName, lastName, position, orderDate, receiveDate = "",
                    productName, categoryName;
            int house, fullPrice, productCount, unitPrice;

            //--------------- Customers table generating ---------------
            st = conn.createStatement();
            for (int i = 0; i < typSize; i++) {
                companyName = gen.generateString(0, 255);
                city = gen.generateString(0, 255);
                street = gen.generateString(0, 255);
                house = gen.generateInt(0, 1000);
                phone = gen.generatePhoneNumber();
                contactName = gen.generateString(0, 255);

                sql = "INSERT INTO \"Customers\"(CustomerID, CompanyName, City, Street, House, Phone, ContactName)" +
                        " VALUES ('" + (i + 1) + "','"
                        + companyName + "','" + city + "','" + street + "','" + house + "','" + phone + "','" + contactName + "');";
                st.executeUpdate(sql);
            }

            st.close();

            //--------------- Customers table primary keys saving ---------------
            st = conn.createStatement();
            rs = st.executeQuery("SELECT CustomerID FROM \"Customers\";");
            while (rs.next())
                tablesID.get("CustomerID").add(rs.getInt("CustomerID"));
            rs.close();
            st.close();

            //--------------- Employees table generating ---------------
            st = conn.createStatement();
            for (int i = 0; i < typSize; i++) {
                firstName = gen.generateString(0, 255);
                lastName = gen.generateString(0, 255);
                position = gen.generateEnum();
                phone = gen.generatePhoneNumber();

                sql = "INSERT INTO \"Employees\"(EmployeeID, FirstName, LastName, Position, Phone)" +
                        " VALUES ('" + (i + 1) + "','"
                        + firstName + "','" + lastName + "','" + position + "','" + phone + "');";
                st.executeUpdate(sql);
            }

            st.close();

            //--------------- Employees table primary keys saving ---------------
            st = conn.createStatement();
            rs = st.executeQuery("SELECT EmployeeID FROM \"Employees\";");
            while (rs.next())
                tablesID.get("EmployeeID").add(rs.getInt("EmployeeID"));
            rs.close();
            st.close();

            //--------------- Orders table generating ---------------
            st = conn.createStatement();
            for (int i = 0; i < typSize; i++) {
                customer_FK = (int)(Math.floor(Math.random() * tablesID.get("CustomerID").size()) + 1);
                employee_FK = (int)(Math.floor(Math.random() * tablesID.get("EmployeeID").size()) + 1);
                orderDate = gen.generateDate("2000-01-01 00:00:00", "2050-01-01 00:00:00");
                while (orderDate.compareTo(receiveDate) > 0)
                    receiveDate = gen.generateDate("2000-01-01 00:00:00", "2050-01-01 00:00:00");

                sql = "INSERT INTO \"Orders\"(OrderID, CustomerID, EmployeeID, OrderDate, ReceiveDate)" +
                        " VALUES ('" + (i + 1) + "','"
                        + customer_FK + "','" + employee_FK + "','" + orderDate + "','" + receiveDate + "');";
                st.executeUpdate(sql);
            }

            st.close();

            //--------------- Orders table primary keys saving ---------------
            st = conn.createStatement();
            rs = st.executeQuery("SELECT OrderID FROM \"Orders\";");
            while (rs.next())
                tablesID.get("OrderID").add(rs.getInt("OrderID"));
            rs.close();
            st.close();

            //--------------- Shippers table generating ---------------
            st = conn.createStatement();
            for (int i = 0; i < typSize; i++) {
                companyName = gen.generateString(0, 255);
                city = gen.generateString(0, 255);
                street = gen.generateString(0, 255);
                house = gen.generateInt(0, 1000);
                phone = gen.generatePhoneNumber();
                contactName = gen.generateString(0, 255);

                sql = "INSERT INTO \"Shippers\"(ShipperID, CompanyName, City, Street, House, Phone, ContactName)" +
                        " VALUES ('" + (i + 1) + "','"
                        + companyName + "','" + city + "','" + street + "','" + house + "','" + phone + "','" + contactName + "');";
                st.executeUpdate(sql);
            }

            st.close();

            //--------------- Shippers table primary keys saving ---------------
            st = conn.createStatement();
            rs = st.executeQuery("SELECT ShipperID FROM \"Shippers\";");
            while (rs.next())
                tablesID.get("ShipperID").add(rs.getInt("ShipperID"));
            rs.close();
            st.close();

            //--------------- Suppliers table generating ---------------
            st = conn.createStatement();
            for (int i = 0; i < typSize; i++) {
                companyName = gen.generateString(0, 255);
                city = gen.generateString(0, 255);
                street = gen.generateString(0, 255);
                house = gen.generateInt(0, 1000);
                phone = gen.generatePhoneNumber();
                contactName = gen.generateString(0, 255);

                sql = "INSERT INTO \"Suppliers\"(SupplierID, CompanyName, City, Street, House, Phone, ContactName)" +
                        " VALUES ('" + (i + 1) + "','"
                        + companyName + "','" + city + "','" + street + "','" + house + "','" + phone + "','" + contactName + "');";
                st.executeUpdate(sql);
            }

            st.close();

            //--------------- Suppliers table primary keys saving ---------------
            st = conn.createStatement();
            rs = st.executeQuery("SELECT SupplierID FROM \"Suppliers\";");
            while (rs.next())
                tablesID.get("SupplierID").add(rs.getInt("SupplierID"));
            rs.close();
            st.close();

            //--------------- Categories table generating ---------------
            st = conn.createStatement();
            for (int i = 0; i < catSize; i++) {
                categoryName = gen.generateString(0, 255);

                sql = "INSERT INTO \"Categories\"(CategoryID, CategoryName)" +
                        " VALUES ('" + (i + 1) + "','" + categoryName + "');";
                st.executeUpdate(sql);
            }

            st.close();

            //--------------- Categories table primary keys saving ---------------
            st = conn.createStatement();
            rs = st.executeQuery("SELECT CategoryID FROM \"Categories\";");
            while (rs.next())
                tablesID.get("CategoryID").add(rs.getInt("CategoryID"));
            rs.close();
            st.close();

            //--------------- Products table generating ---------------
            st = conn.createStatement();
            for (int i = 0; i < specSize; i++) {
                productName = gen.generateString(0, 255);
                supplier_FK = (int)(Math.floor(Math.random() * tablesID.get("SupplierID").size()) + 1);
                category_FK = (int)(Math.floor(Math.random() * tablesID.get("CategoryID").size()) + 1);
                unitPrice = gen.generateInt(0, 1000000);

                sql = "INSERT INTO \"Products\"(ProductID, ProductName, SupplierID, CategoryID, UnitPrice)" +
                        " VALUES ('" + (i + 1) + "','"
                        + productName + "','" + supplier_FK + "','" + category_FK + "','" + unitPrice + "');";
                st.executeUpdate(sql);
            }

            st.close();

            //--------------- Products table primary keys saving ---------------
            st = conn.createStatement();
            rs = st.executeQuery("SELECT ProductID FROM \"Products\";");
            while (rs.next())
                tablesID.get("ProductID").add(rs.getInt("ProductID"));
            rs.close();
            st.close();

            //--------------- OrderDetails table generating ---------------
            st = conn.createStatement();
            Set<Integer> order_FKs = new LinkedHashSet<Integer>();
            for (int i = 0; i < typSize; i++) {
                while (true) {
                    order_FK = (int) (Math.floor(Math.random() * tablesID.get("OrderID").size()) + 1);
                    Integer tmp = new Integer(order_FK);
                    if (!order_FKs.contains(tmp)) {
                            order_FKs.add(tmp);
                            break;
                    }
                }
                shipper_FK = (int)(Math.floor(Math.random() * tablesID.get("ShipperID").size()) + 1);
                city = gen.generateString(0, 255);
                street = gen.generateString(0, 255);
                house = gen.generateInt(0, 1000);
                fullPrice = gen.generateInt(0, 10000000);

                sql = "INSERT INTO \"OrderDetails\"(OrderID, ShipperID, City, Street, House, FullPrice)" +
                        " VALUES ('" + order_FK + "','"
                        + shipper_FK + "','" + city + "','" + street + "','" + house + "','" + fullPrice + "');";
                st.executeUpdate(sql);
            }

            st.close();

            //--------------- OrderProducts table generating ---------------
            st = conn.createStatement();
            for (int i = 0; i < specSize; i++) {
                order_FK = (int)(Math.floor(Math.random() * tablesID.get("OrderID").size()) + 1);
                product_FK = (int)(Math.floor(Math.random() * tablesID.get("ProductID").size()) + 1);
                productCount = gen.generateInt(0, 1000);

                sql = "INSERT INTO \"OrderProducts\"(NoteID, OrderID, ProductID, ProductCount)" +
                        " VALUES ('" + (i + 1) + "','"
                        + order_FK + "','" + product_FK + "','" + productCount + "');";
                st.executeUpdate(sql);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static Connection getDBConnection() {

        Connection dbConnection = null;

        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try {
            dbConnection = DriverManager.getConnection(
                    DB_CONNECTION, DB_USER, DB_PASSWORD);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return dbConnection;
    }

    private static void getProperties() {

        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("src/main/resources/Prop.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        DB_DRIVER = prop.getProperty("Driver");
        DB_CONNECTION = prop.getProperty("Database");
        DB_USER = prop.getProperty("Username");
        DB_PASSWORD = prop.getProperty("Password");
        typSize = Integer.parseInt(prop.getProperty("TypicalSize"));
        specSize = Integer.parseInt(prop.getProperty("SpecialSize"));
        catSize = Integer.parseInt(prop.getProperty("CategorySize"));
    }

}


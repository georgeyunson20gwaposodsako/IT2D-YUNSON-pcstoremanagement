package pcstore;

import Config.Config;
import java.sql.*;
import java.util.Scanner;

public class Main {

    public static final String ADMIN_PASSWORD = "admin123";
    private static int loggedInCustomerId = -1;

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            Config config = new Config();

            System.out.print("Are you an admin? (Y/N): ");
            String choice = sc.nextLine();

            if (choice.equalsIgnoreCase("Y")) {
                System.out.print("Enter admin password: ");
                String password = sc.nextLine();
                if (password.equals(ADMIN_PASSWORD)) {
                    adminMenu(sc, config);
                } else {
                    System.out.println("Invalid password.");
                }
            } else if (choice.equalsIgnoreCase("N")) {
                customerAuth(sc, config);
            } else {
                System.out.println("Invalid choice. Please enter Y or N.");
            }
        }
    }

    // ---------------- ADMIN MENU ----------------
    private static void adminMenu(Scanner sc, Config config) {
        int choice;
        do {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Add New PC Build");
            System.out.println("2. View All PC Builds");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    addPCBuild(sc, config);
                    break;
                case 2:
                    viewAllBuilds(config);
                    break;
                case 3:
                    System.out.println("Exiting admin menu...");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } while (choice != 3);
    }

    private static void addPCBuild(Scanner sc, Config config) {
        try (Connection conn = Config.connect()) {
            if (conn == null) {
                System.out.println("Database connection failed.");
                return;
            }

            System.out.println("\n--- ADD NEW PC BUILD ---");
            System.out.print("Enter Build Name: ");
            String name = sc.nextLine();

            System.out.print("Enter Specifications: ");
            String specs = sc.nextLine();

            System.out.print("Enter Price: ");
            double price = sc.nextDouble();
            sc.nextLine();

            PreparedStatement pst = conn.prepareStatement(
              "INSERT INTO tbl_pcbuilds (build_name, description, price) VALUES (?, ?, ?)");

            pst.setString(1, name);
            pst.setString(2, specs);
            pst.setDouble(3, price);

            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ New PC Build added successfully!");
            } else {
                System.out.println("❌ Failed to add PC build.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewAllBuilds(Config config) {
        try (Connection conn = Config.connect()) {
            if (conn == null) {
                System.out.println("Database connection failed.");
                return;
            }

            System.out.println("\n--- ALL PC BUILDS ---");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM tbl_pcbuilds");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("ID: " + rs.getInt("build_id"));
               System.out.println("Name: " + rs.getString("build_name"));
               System.out.println("Description: " + rs.getString("description"));
               System.out.println("Price: " + rs.getDouble("price"));

                System.out.println("------------------------------");
            }

            if (!found) {
                System.out.println("No PC builds found.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------------- CUSTOMER AUTH ----------------
    private static void customerAuth(Scanner sc, Config config) {
        OUTER:
        while (true) {
            System.out.println("\n--- CUSTOMER ACCESS ---");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.print("Enter your choice: ");
            int option = sc.nextInt();
            sc.nextLine();
            switch (option) {
                case 1:
                    if (loginCustomer(sc, config)) {
                        customerMenu(sc, config);
                        break OUTER;
                    }
                    break;
                case 2:
                    registerCustomer(sc, config);
                    customerMenu(sc, config);
                    break OUTER;
                default:
                    System.out.println("Invalid choice. Try again.");
                    break;
            }
        }
    }

    // ---------------- LOGIN ----------------
    private static boolean loginCustomer(Scanner sc, Config config) {
        try (Connection conn = Config.connect()) {
            if (conn == null) {
                System.out.println("Database connection failed.");
                return false;
            }

            System.out.print("Enter Email: ");
            String email = sc.nextLine();
            System.out.print("Enter Password: ");
            String password = sc.nextLine();

            PreparedStatement pst = conn.prepareStatement(
                    "SELECT c_id, c_fname FROM tbl_customer WHERE c_email = ? AND c_password = ?");
            pst.setString(1, email);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                loggedInCustomerId = rs.getInt("c_id");
                String name = rs.getString("c_fname");
                System.out.println("\n✅ Login successful! Welcome back, " + name + "!");
                return true;
            } else {
                System.out.println("❌ Invalid email or password. Try again.");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    // ---------------- REGISTER ----------------
    private static void registerCustomer(Scanner sc, Config config) {
        System.out.println("\n--- REGISTRATION ---");

        try (Connection conn = Config.connect()) {
            if (conn == null) {
                System.out.println("Database connection failed.");
                return;
            }

            String email;
            while (true) {
                System.out.print("Enter Email: ");
                email = sc.nextLine();

                PreparedStatement checkEmail = conn.prepareStatement(
                        "SELECT * FROM tbl_customer WHERE c_email = ?");
                checkEmail.setString(1, email);
                ResultSet rs = checkEmail.executeQuery();

                if (rs.next()) {
                    System.out.println("⚠️ Email already registered. Please use another email.");
                } else {
                    break;
                }
            }

            System.out.print("Enter First Name: ");
            String fname = sc.nextLine();
            System.out.print("Enter Last Name: ");
            String lname = sc.nextLine();
            System.out.print("Enter Password: ");
            String password = sc.nextLine();

            PreparedStatement pst = conn.prepareStatement(
                    "INSERT INTO tbl_customer (c_fname, c_lname, c_email, c_password) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, fname);
            pst.setString(2, lname);
            pst.setString(3, email);
            pst.setString(4, password);

            int rows = pst.executeUpdate();
            if (rows > 0) {
                ResultSet keys = pst.getGeneratedKeys();
                if (keys.next()) {
                    loggedInCustomerId = keys.getInt(1);
                }
                System.out.println("✅ Registration successful! Welcome, " + fname + "!");
            } else {
                System.out.println("❌ Registration failed.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------------- CUSTOMER MENU ----------------
    private static void customerMenu(Scanner sc, Config config) {
        int choice;
        do {
            System.out.println("\n--- CUSTOMER MENU ---");
            System.out.println("1. View and Purchase PC Builds");
            System.out.println("2. Logout");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    viewAndPurchaseBuilds(sc, config);
                    break;
                case 2:
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } while (choice != 2);
    }

    // ---------------- VIEW & PURCHASE BUILDS ----------------
    private static void viewAndPurchaseBuilds(Scanner sc, Config config) {
        try (Connection conn = Config.connect()) {
            if (conn == null) {
                System.out.println("Database connection failed.");
                return;
            }

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM tbl_pcbuilds");

            System.out.println("\n--- AVAILABLE PC BUILDS ---");
            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.println(rs.getInt("build_id") + ". " + rs.getString("build_name"));
                  System.out.println("Description: " + rs.getString("description"));
                 System.out.println("Price: " + rs.getDouble("price"));
                System.out.println("-----------------------------");
            }

            if (!found) {
                System.out.println("No builds available yet.");
                return;
            }

            System.out.print("Enter the build ID to purchase (0 to go back): ");
            int buildId = sc.nextInt();
            sc.nextLine();

            if (buildId == 0) return;

            PreparedStatement select = conn.prepareStatement("SELECT * FROM tbl_pcbuilds WHERE build_id = ?");
            select.setInt(1, buildId);
            ResultSet build = select.executeQuery();

            if (build.next()) {
                System.out.println("You selected: " + build.getString("build_name"));
                System.out.print("Would you like to buy this PC? (Y/N): ");
                String confirm = sc.nextLine();

                if (confirm.equalsIgnoreCase("Y")) {
                    System.out.println("✅ Purchase successful! Thank you for buying!");
                } else {
                    System.out.println("Returning to menu...");
                }
            } else {
                System.out.println("Invalid build ID.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

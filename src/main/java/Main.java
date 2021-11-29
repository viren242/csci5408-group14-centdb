import queries.Queries;
import user.User;
import utilities.ConsoleReader;

public class Main {

    public static ConsoleReader reader = new ConsoleReader();
    public static Boolean userLoggedIn = false;

    public static void main(String[] args) {
        System.out.println("Welcome to the Database Management System!");

        while (true) {
            System.out.println("\n\nPlease select an option:");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");

            int option = reader.readInt();

            switch (option) {
                case 1:
                    userLoggedIn = User.login();
                    break;
                case 2:
                    User.register();
                    break;
                case 3:
                    break;
                default:
                    System.out.println("Invalid option!");
                    break;
            }

            if (option == 3 || userLoggedIn) {
                break;
            }
        }

        if(userLoggedIn) {
            while (true) {
                System.out.println("\n\nPlease select an option:");
                System.out.println("1. Write Queries");
                System.out.println("2. Export");
                System.out.println("3. Data Model");
                System.out.println("4. Analytics");
                System.out.println("5. Logout and Exit");

                int option = reader.readInt();

                switch (option) {
                    case 1:
                        Queries.menu();
                        break;
                    case 2:
//                        export page
                        break;
                    case 3:
//                        data model page
                        break;
                    case 4:
//                        analytics page
                        break;
                    case 5:
                        userLoggedIn = false;
                        break;
                    default:
                        System.out.println("Invalid option!");
                        break;
                }

                if(option == 5 || !userLoggedIn) {
                    break;
                }
            }
        }

        System.out.println("\n\nExiting...");
    }
}

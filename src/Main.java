import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

class User {
    private Long id;
    private String username;
    private String email;
    private String password;

    public User(Long id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}



public class Main {
    private static final String URL = "jdbc:postgresql://localhost:5432/localdatabase";
    private static final String userName = "postgres";
    private static final String password = "1234";
    public static void main(String[] args) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        try (Connection connection = DriverManager.getConnection(URL, userName, password)){
            while (true){
                System.out.println("PRESS [1] TO ADD USER"); // Добавить пользоваетля
                System.out.println("PRESS [2] TO LIST USERS"); // Список пользоваетлей
                System.out.println("PRESS [3] TO DELETE USER"); // Удалить пользоваетля
                System.out.println("PRESS [4] TO UPDATE USER"); // Обновить пользоваетля
                System.out.println("PRESS [5] TO EXIT"); // Выход из программы
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("Введите имя пользователя: ");
                        String userName = scanner.nextLine();
                        System.out.print("Введите почту: ");
                        String email = scanner.nextLine();
                        System.out.print("Введите пароль: ");
                        String password = scanner.nextLine();
                        if(userName.isEmpty() || email.isEmpty() || password.isEmpty()){
                            System.out.println("Одно из значении было пусто. Введите еще раз");
                            continue;
                        }else{
                            User user = new User(null, userName,email, password);
                            insertRecordUser(user, connection);
                            break;
                        }
                    case 2:
                        System.out.println("\n=== Список пользователей ===");
                        ArrayList<User> users = getUsers(connection);
                        if (users.isEmpty()) {
                            System.out.println("Список пуст");
                        } else {
                            for (User u : users) {
                                System.out.println("id: " + u.getId() + ", Имя пользователя: " + u.getUsername() + ", почта: " + u.getEmail());
                            }
                        }
                        break;
                    case 3:
                        System.out.println("Ввдите индентифкационный номер пользователя (id): ");
                        try {
                            int idUser = Integer.parseInt(scanner.nextLine());
                            if (idUser > 0) {
                                deleteUser(idUser, connection);
                            } else {
                                System.out.println("ID должен быть положительным числом!");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Введите корректное число!\n" + e.getMessage());
                        }
                        break;
                    case 4:
                        System.out.println("Введите индентифкационный номер пользователя (id) которого вы хотите обновить: ");
                        try{
                            int idUpdateUser = Integer.parseInt(scanner.nextLine());
                            if(!findUserById(idUpdateUser, connection)){
                                System.out.println("Пользователь с ID " + idUpdateUser + " не найден!");
                                break;
                            }
                            System.out.println("Пользователь найден!\n");
                            System.out.print("Введите новое имя пользователя: ");
                            String newUserName = scanner.nextLine();
                            System.out.print("Введите новую почту: ");
                            String newEmail = scanner.nextLine();
                            System.out.print("Введите новый пароль: ");
                            String NewPassword = scanner.nextLine();

                            if (newUserName.isEmpty() || newEmail.isEmpty() || NewPassword.isEmpty()) {
                                System.out.println("Поля не могут быть пустыми!");
                            }else{
                                updateUser(idUpdateUser, newUserName, newEmail, NewPassword, connection);
                            }
                        }catch (NumberFormatException e){
                            System.out.println("Введите число!\n" + e.getMessage());
                        }
                        break;
                    case 5:
                        System.out.println("Выход");
                        return;
                    default:
                        System.out.println("Невеный выбор! Попробуйте снова");
                }
            }
        }
    }


    private static ArrayList<User> getUsers(Connection connection) throws SQLException {
        ArrayList<User> users = new ArrayList<>();
        final String query = "select * from users";
        try(Statement statement = connection.createStatement()){
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()){
                Long id = resultSet.getLong("id");
                String name = resultSet.getString("username");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");

                users.add(new User(id,name, email,password));
            }

        }
        return users;
    }


    private static void deleteUser(int index, Connection connection)throws SQLException{
        final String queryFind = "select * from users where id = ?";
        final String query = "delete from users where id = ?";
        try(PreparedStatement preparedStatementFind = connection.prepareStatement(queryFind)){
            preparedStatementFind.setInt(1, index);
            try(ResultSet resultSet = preparedStatementFind.executeQuery()){
                if(resultSet.next()){
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
                        preparedStatement.setInt(1, index);
                        preparedStatement.executeUpdate();

                        System.out.println("User was deleted!");
                    }
                }else{
                    System.out.println("Пользатель с " + index + " не найден. Попробуйте снова!");
                }
            }
        }

    }

    private static boolean findUserById(int id, Connection connection)throws SQLException{
        final String check_user_update = "Select COUNT(*) from users where id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(check_user_update)){
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next() && resultSet.getInt(1) > 0;
        }
    }
    private static void updateUser(int id, String newUsername, String NewUserEmail, String newUserPassword, Connection connection)throws SQLException{
        final String query = "UPDATE users SET username = ?, email = ?, password = ? WHERE id = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, newUsername);
            statement.setString(2, NewUserEmail);
            statement.setString(3, newUserPassword);
            statement.setInt(4, id);
            statement.executeUpdate();

            System.out.println("Пользователь с id: " + id + " был обновлен!");

        }
    }

    private static void insertRecordUser(User user, Connection connection)throws SQLException{
        final String CHECK_USER_SQL = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?";
        final String INSERT_SQL_USER = "INSERT INTO users (username, email, password) VALUES (?, ?, ?);";

        try(PreparedStatement preparedStatementCheckToDublicate = connection.prepareStatement(CHECK_USER_SQL);){
            preparedStatementCheckToDublicate.setString(1, user.getUsername());
            preparedStatementCheckToDublicate.setString(2, user.getEmail());
            try(ResultSet resultSet = preparedStatementCheckToDublicate.executeQuery()){
                if(resultSet.next() && resultSet.getInt(1) > 0){
                    System.out.println("Пользователь с таким username или email уже существует!");
                    return;
                }
            }
            try(PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL_USER)) {
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getEmail());
                preparedStatement.setString(3, user.getPassword());

                preparedStatement.executeUpdate();

                System.out.println("inserted successful");
            }
        }
    }
    public static void connectDataBase(String url, String userName, String password){
        try(Connection connection = DriverManager.getConnection(url, userName, password);){
            if(connection != null){
                System.out.println("Connection is successful");
            }else{
                System.out.println("Failed connection");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
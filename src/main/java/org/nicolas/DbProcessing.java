package org.nicolas;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс работы с базой данных
 */
public class DbProcessing {

    private Connection connection;
    private PreparedStatement psUpdate;
    private PreparedStatement psInsert;
    private PreparedStatement psDelete;
    private PreparedStatement psSelect;
    private String dbPath;

    /**
     * Получить соединение
     * @return текущее соединение
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Инициализация всех запросов
     */
    public void initQueries() {
        String sqlUpdate = "UPDATE departments set Description = ? where id = ?";
        String sqlInsert = "INSERT INTO departments (DepCode, DepJob, Description) VALUES (?, ?, ?)";
        String sqlDelete = "DELETE FROM departments WHERE id = ?";
        String sqlSelect = "SELECT * FROM departments";

        try {
            psSelect = connection.prepareStatement(sqlSelect);
            psDelete = connection.prepareStatement(sqlDelete);
            psInsert = connection.prepareStatement(sqlInsert);
            psUpdate = connection.prepareStatement(sqlUpdate);

        } catch (SQLException e) {
            MainClass.logger.fatal("Ошибка инициализации базы данных");
            MainClass.logger.debug(e.getMessage());
            throw new RuntimeException("Выход из программы!");
        }
    }

    public DbProcessing(String dbPath) {
        this.dbPath = dbPath;
        connect();

        initQueries();
    }

    /**
     * Получить список всех отделов
     *
     * @return список отделов
     * @throws SQLException ошибки при получении записей
     */
    public List<Department> selectAllRecords() throws SQLException {

        ResultSet res = psSelect.executeQuery();

        List<Department> departmentList = new ArrayList<>();

        if (res == null) {
            return departmentList;
        }

        while (res.next()) {
            Integer id = res.getInt("id");
            String depCode = res.getString("depCode");
            String depJob = res.getString("depJob");
            String description = res.getString("description");

            Department department = new Department(id, depCode, depJob, description);
            departmentList.add(department);

        }

        return departmentList;
    }

    /**
     * Удалить записи по списку ID
     *
     * @param ids список Integer
     * @throws SQLException ошибки при удалении
     */
    public void deleteRecords(List<Integer> ids) throws SQLException {

        for (Integer id : ids) {
            psDelete.setInt(1, id);
            psDelete.addBatch();
        }

        psDelete.executeBatch();

    }

    /**
     * Обновить записи по списку
     *
     * @param items список отделов для обновления
     * @throws SQLException ошибки при обновлении
     */
    public void updateRecords(List<Department> items) throws SQLException {
        for (Department item : items) {
            psUpdate.setString(1, item.getDescription());
            psUpdate.setInt(2, item.getId());

            psUpdate.addBatch();
        }

        psUpdate.executeBatch();
    }

    /**
     * Вставить записи в базу данных
     * @param items список отделов
     * @throws SQLException ошибки при вставке
     */
    public void insertRecords(List<Department> items) throws SQLException {
        for (Department item : items) {
            psInsert.setString(1, item.getDepCode());
            psInsert.setString(2, item.getDepJob());
            psInsert.setString(3, item.getDescription());

            psInsert.addBatch();
        }

        psInsert.executeBatch();
    }

    /**
     * Выполнить соединение к базе данных
     */
    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            connection.setAutoCommit(false);

        } catch (ClassNotFoundException | SQLException e) {
            String errMess = "Ошибка инициализации базы данных";
            MainClass.logger.fatal(errMess);
            throw new RuntimeException(errMess);
        }
    }

    /**
     * Выполнить закрытие соединения к базе данных
     */
    public void disconnect() {
        try {
            MainClass.logger.debug("Выполняем закрытие соединения");
            connection.close();
            MainClass.logger.debug("Соединение закрыто");
        } catch (SQLException e) {
            String errMess = "Ошибка закрытия соединения к базе данных";
            MainClass.logger.warn(errMess);
        }
    }

}

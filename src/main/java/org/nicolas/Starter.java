package org.nicolas;

import org.apache.log4j.*;
import org.nicolas.MainClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Класс инициализации приложения
 */
public class Starter {

    private String dbName;
    private String logFileName;

    /**
     * Прочитать и вернуть параметры
     * @return параметры приложения
     */
    private Properties initProps() {
        FileInputStream is;

        try {
            is = new FileInputStream("./app.properties");
        } catch (FileNotFoundException e) {
            MainClass.logger.fatal("Ошибка получения настроек приложения!");
            return null;
        }

        Properties prop = new Properties();

        try {
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
            MainClass.logger.fatal("Ошибка получения настроек приложения!");
            return null;
        }

        try {
            is.close();
        } catch (IOException e) {
            MainClass.logger.fatal("Системная ошибка при закрытиии файла настроек приложения");
            return null;
        }

        return prop;
    }

    /**
     * Инициализация логгирования в файл
     * @return успех инициализации
     */
    private boolean initLog() {

        RollingFileAppender appender;
        try {
            appender = new RollingFileAppender(
                    new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"), logFileName);
            appender.setEncoding("UTF-8");
            appender.setMaxFileSize("5MB");
            appender.setMaxBackupIndex(10);
            appender.setThreshold(Level.DEBUG);
        } catch (IOException e) {
            MainClass.logger.fatal("Ошибка инициализации файла логгирования");
            return false;
        }

        Logger.getRootLogger().addAppender(appender);

        return true;
    }

    /**
     * Инициализация приложения
     * @return успех инициализации
     */
    public boolean appInit() {

        Properties prop = initProps();
        if (prop == null) {
            return false;
        }

        dbName = prop.getProperty("dbName");
        logFileName = prop.getProperty("logFileName");

        if (!initLog()) {
            return false;
        }

        File dbFile = new File(dbName);
        if (!dbFile.exists()) {
            MainClass.logger.fatal("Файл базы данных отсутствует");
            return false;
        }

        return true;
    }

    /**
     * Получить имя базы данных
     * @return строка с путем к базе данных
     */
    public String getDbName() {
        return dbName;
    }
}

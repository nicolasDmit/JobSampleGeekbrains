package org.nicolas;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;

public class MainClass {

    final static Logger logger = Logger.getLogger(MainClass.class);
    private static final String SYNC = "sync";
    private static final String EXPORT = "export";

    public static void main(String[] args) {

        logger.setLevel(Level.DEBUG);

        if (args == null || args.length == 0) {
            System.out.println("Задайте режим работы!");
            return;
        }

        if (SYNC.equals(args[0])) {
            if (args.length == 1) {
                System.out.println("Укажите файл синхронизации!");
                return;
            }

            File syncFile = new File(args[1]);
            if (!syncFile.exists() || syncFile.isDirectory()) {
                System.out.println("Ошибка получения файла синхронизации");
                return;
            }

            Starter starter = new Starter();
            if (!starter.appInit()) {
                logger.fatal("Ошибка инициализации приложения");
                return;
            }

            DepartmentModel departmentModel = new DepartmentModel(starter.getDbName());

            departmentModel.syncDataFromXml(args[1]);
            return;
        }

        if (EXPORT.equals(args[0])) {
            if (args.length == 1) {
                System.out.println("Укажите файл выгрузки!");
                return;
            }

            Starter starter = new Starter();
            if (!starter.appInit()) {
                logger.fatal("Ошибка инициализации приложения");
                return;
            }

            DepartmentModel departmentModel = new DepartmentModel(starter.getDbName());
            departmentModel.saveDbToXml(args[1]);
        }

    }
}

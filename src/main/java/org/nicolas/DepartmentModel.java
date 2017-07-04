package org.nicolas;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс работы с данными
 */
public class DepartmentModel {

    private DbProcessing dbProcessing;

    public DepartmentModel(String dbPath) {
        this.dbProcessing = new DbProcessing(dbPath);
    }

    /**
     * Создаем узел отдел
     *
     * @param doc         DOM документ
     * @param depCode     код отдела
     * @param depJob      название должности
     * @param description описание
     * @return узел документа
     */
    private Element createDepartmentNode(Document doc, String depCode, String depJob, String description) {
        Element departmentElement = doc.createElement("org.nicolas.Department");
        Element depCodeNode = doc.createElement("depCode");
        Element depJobNode = doc.createElement("depJob");
        Element descriptionNode = doc.createElement("description");

        depCodeNode.setTextContent(StringEscapeUtils.escapeXml11(depCode));
        depJobNode.setTextContent(StringEscapeUtils.escapeXml11(depJob));
        descriptionNode.setTextContent(StringEscapeUtils.escapeXml11(description));

        departmentElement.appendChild(depCodeNode);
        departmentElement.appendChild(depJobNode);
        departmentElement.appendChild(descriptionNode);

        return departmentElement;
    }

    /**
     * Выгрузить базу в XML документ
     */
    public void saveDbToXml(String exportPlace) {

        MainClass.logger.info("Начало выгрузки базы в XML файл: " + exportPlace);

        List<Department> departmentList = null;
        try {
            MainClass.logger.debug("Получение данных из базы");
            departmentList = dbProcessing.selectAllRecords();
            MainClass.logger.debug("Получено " + departmentList.size() + " записей");
        } catch (SQLException e) {
            String errMess = "Ошибка получения данных из базы данных";
            MainClass.logger.fatal(errMess);
            MainClass.logger.debug(e.getMessage());
            throw new RuntimeException(errMess);
        } finally {
            dbProcessing.disconnect();
        }

        MainClass.logger.debug("Подготовка документа к выгрузке");
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;

        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            MainClass.logger.fatal("Ошибка создания XML документа!");
            MainClass.logger.debug(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("Departments");
        doc.appendChild(rootElement);

        MainClass.logger.info("Заполнение документа данными");

        for (Department department : departmentList) {

            Element departmentElement = createDepartmentNode(
                    doc, department.getDepCode(), department.getDepJob(), department.getDescription()
            );

            rootElement.appendChild(departmentElement);
        }

        MainClass.logger.debug("Заполнение завершено");

        try {
            MainClass.logger.debug("Подготовка к выгрузке");

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(exportPlace));

            transformer.transform(source, result);

            MainClass.logger.info("Выгрузка завершена");
        } catch (TransformerException e) {
            MainClass.logger.fatal("Ошибка выгрузки XML файла");
            MainClass.logger.debug(e.getMessage());

            throw new RuntimeException(e.getMessage());
        }

    }

    /**
     * синхронизация базы данных с XML файлом
     *
     * @param xmlFilePath путь к файлу синхронизации
     */
    public void syncDataFromXml(String xmlFilePath) {
        File fXmlFile = new File(xmlFilePath);

        MainClass.logger.info("Выполнение операции синхронизации данных");

        if (!fXmlFile.exists()) {
            MainClass.logger.fatal("Файл импорта не доступен");
            throw new RuntimeException("Файл импорта не доступен");
        }

        MainClass.logger.debug("Создание документа выгрузки");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        Document doc = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(fXmlFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            MainClass.logger.fatal("Ошибка создания документа выгрузки");
            MainClass.logger.debug(e.getMessage());

            throw new RuntimeException(e);
        }

        doc.getDocumentElement().normalize();

        String rootName = doc.getDocumentElement().getTagName();

        if (!"Departments".equals(rootName)) {
            MainClass.logger.fatal("Ошибка разбора XML файла");
            MainClass.logger.debug("файл не содержит ноду Departments");
            throw new RuntimeException("Ошибка разбора XML файла");
        }

        NodeList nodeList = doc.getElementsByTagName("org.nicolas.Department");

        if (nodeList.getLength() == 0) {
            MainClass.logger.warn("Файл импорта пуст");
            throw new RuntimeException("Файл импорта пуст");
        }

        List<Department> fileDepsList = new ArrayList<>();

        MainClass.logger.info("Начало синхронизации");

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node depNode = nodeList.item(i);
            if (depNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            NodeList depDetails = depNode.getChildNodes();
            if (depDetails.getLength() == 0) {
                continue;
            }

            String depCode = null;
            String depJob = null;
            String description = "";

            for (int j = 0; j < depDetails.getLength(); j++) {
                String nodeName = depDetails.item(j).getNodeName();
                Node curNode = depDetails.item(j);

                switch (nodeName) {
                    case "depCode":
                        depCode = StringEscapeUtils.unescapeXml(curNode.getTextContent());
                        break;
                    case "depJob":
                        depJob = StringEscapeUtils.unescapeXml(curNode.getTextContent());
                        break;
                    case "description":
                        description = StringEscapeUtils.unescapeXml(curNode.getTextContent());
                        break;
                }

            }

            if (depCode != null && depJob != null) {
                Department department = new Department(null, depCode, depJob, description);
                fileDepsList.add(department);
            } else {
                MainClass.logger.debug("Ошибочная нода с незаполненными параметрами: " + depCode + " | " + depJob);
            }
        }

        MainClass.logger.info("Получение данных из БД");

        List<Department> dbDepsList = null;
        try {
            dbDepsList = dbProcessing.selectAllRecords();
        } catch (SQLException e) {
            dbProcessing.disconnect();
            MainClass.logger.fatal("Ошибка чтения базы данных");
            MainClass.logger.debug(e.getMessage());

            throw new RuntimeException("Ошибка чтения базы данных");
        }

        MainClass.logger.debug("Получено " + dbDepsList.size() + " записей");

        // 1. Создаем хэш мап отделов из файла и из базы даных
        Map<Integer, Department> fromFileDeps = new HashMap<>();
        Map<Integer, Department> fromDbDeps = new HashMap<>();

        List<Integer> toDelete = new ArrayList<>();
        List<Department> toInsert = new ArrayList<>();
        List<Department> toUpdate = new ArrayList<>();

        for (Department item : dbDepsList) {
            fromDbDeps.put(item.hashCode(), item);
        }

        for (Department item : fileDepsList) {

            if (fromFileDeps.get(item.hashCode()) != null) {
                MainClass.logger.warn("Дубликат отдела в файле XML, запись пропущена");
                MainClass.logger.debug(item.getDepCode() + " | " + item.getDepJob());
                continue;
            }

            fromFileDeps.put(item.hashCode(), item);
        }

        MainClass.logger.info("Заполнение данных для синхронизации");
        //ищем удаленные
        for (Integer item : fromDbDeps.keySet()) {
            if (fromFileDeps.get(item) == null) {
                Department dep = fromDbDeps.get(item);
                toDelete.add(dep.getId());
                MainClass.logger.debug("На удаление: " + dep.toString());
            } else { //или обновленные
                Department ffItem = fromFileDeps.get(item);
                Department fbItem = fromDbDeps.get(item);

                if (!ffItem.getDescription().equals(fbItem.getDescription())) {
                    ffItem.setId(fbItem.getId());
                    toUpdate.add(ffItem);
                    MainClass.logger.debug("На обновление: " + ffItem.toString());
                }
            }
        }

        //ищем новые
        for (Integer item : fromFileDeps.keySet()) {
            if (fromDbDeps.get(item) == null) {
                toInsert.add(fromFileDeps.get(item));
                MainClass.logger.debug("Новый элемент: " + fromFileDeps.get(item).toString());
            }
        }

        try {
            if (!toDelete.isEmpty()) {
                MainClass.logger.info("Выполняем удаление " + toDelete.size() + " записей");
                dbProcessing.deleteRecords(toDelete);
            }

            if (!toInsert.isEmpty()) {
                MainClass.logger.info("Выполняем вставку " + toInsert.size() + " записей");
                dbProcessing.insertRecords(toInsert);
            }

            if (!toUpdate.isEmpty()) {
                MainClass.logger.debug("Выполняем обновление " + toUpdate.size() + " записей");
                dbProcessing.updateRecords(toUpdate);
            }

            dbProcessing.getConnection().commit();
            MainClass.logger.info("Операция завершена");

        } catch (SQLException e) {
            MainClass.logger.fatal("Ошибка выполнения операции");
            MainClass.logger.debug(e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            dbProcessing.disconnect();
        }

    }

    /**
     * Генерация тестовых данных
     */
    public void generateData() {
        List<Department> depList = new ArrayList<>();
        for (int i = 0; i < 1500; i++) {
            Department depItem = new Department(
                    null, "depCode" + (i + 1), "depJob" + (i + 1), "comeDesc"
            );

            depList.add(depItem);
        }

        try {
            dbProcessing.insertRecords(depList);
            dbProcessing.getConnection().commit();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            dbProcessing.disconnect();
        }

    }

}

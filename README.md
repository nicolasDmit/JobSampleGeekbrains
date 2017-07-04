# JobSampleGeekbrains
Solved example from interview task for Geekbrains students Java level 3.

Данный код - пример решения реального тестового задания полученного на собеседовании.
Решение выполнено в максимально простом варианте, чтобы начинающий программист уровня Junior мог понять и, возможно, повторить решение.
В качестве СУБД использован SQLite, чтобы не заморачиваться на дампы и дополнительные сервера.

После сборки проекта порядок настройки и использования программы следуюущий:

* Настройки проекта лежат в файле app.properties, данный файл лежит вне пакета, для удобства настройки проекта:
dbName - путь и имя файла базы данных
logFileName - путь и имя файла лога

* Для выгрузки базы данных в XML файл приложение запускается с параметрами:
java -jar <имя файла собранного проекта> export <имя и путь к файлу выгрузки>

* Для синхронизации приложение запускается с параметрами:
java -jar <имя файла собранного проекта> sync <имя и путь к файлу синхронизации>

Собственно, само задание:

Тестовое задание

Создать java приложение, выполняющее две основные функции:
1) выгрузка содержимого таблицы БД в XML файл;
2) синхронизация содержимого таблицы БД по заданному XML файлу.

Общие требования
•	Основные этапы операций должны логироваться в лог файл. На экран должна выводиться только краткая информация о результате прохождения операции.
•	Название и местонахождение файла для логирования, а также параметры соединения с БД должны задаваться в файле настроек приложения. Формат файла – java properties.

Требования к таблице в БД
В БД должна быть одна таблица со следующей структурой:
ID int  – суррогатный ключ генерируемый при вставке записи в таблицу;
DepCode String(длина 20 символов) – код отдела;
DepJob String(длина 100 символов) – название должности в отделе;
Description String (длина 255 символов) – комментарий.
Первичный ключ – ID.
Натуральный ключ состоит из двух полей – DepCode, DepJob. Соответственно в таблице не должно быть двух и более записей с одинаковым сочетанием DepCode и DepJob.

Требования к функции выгрузки содержимого таблицы в XML файл
•	Пользователь должен иметь возможность указывать имя файла, в который будет осуществлена выгрузка.
•	Формат получаемого файла должен быть – XML.
•	В полученный файл должны выгружаться все поля таблицы, перечисленные выше, кроме поля ID.

Требования к функции синхронизации содержимого таблицы c заданным XML файлом 
•	Пользователь должен иметь возможность указывать имя файла по содержимому которого будет осуществляться синхронизация данных в таблице.
•	Структура загружаемого файла должна быть такой же, как и у выгружаемого в функции выше. Т.е. приложение должно корректно загрузить выгруженный ранее файл.
•	Синхронизация должна проходить по натуральному ключу таблицы с минимальным числом обращений к БД. Т.е. удаляться должны только те записи, которые отсутствуют в XML файле. Добавляться соответственно только новые. Измененные должны обновляться.
•	В случае если в XML файле есть две записи с одним натуральным ключом, то приложение должно выдать соответствующую ошибку.
•	Весь процесс синхронизации должен осуществляться в одной транзакции, т.е. если в процессе синхронизации произошла ошибка, то данные в БД должны остаться нетронутые.

Требования к используемым технологиям
•	Доступ к СУБД должен осуществляться посредством JDBC.
•	Обновление данных в БД должно осуществляться с помощью SQL выражений, а не CONCUR_UPDATABLE режима.
•	Логирование в файл должно осуществляться посредством библиотеки log4j
•	В алгоритме синхронизации должен быть использован класс HashMap или HashSet. Для контроля ключей рекомендуем создать специальный класс, в котором реализовать методы хеширования и equals.
•	Чтение XML файла должно осуществляться с помощью технологии XML DOM. Запись – на ваш выбор.
•	Для работы с файлом настроек приложения необходимо использовать класс Properties.
•	Задание выполняемой команды (функции), а также имени файла рекомендуем сделать через параметры командной строки. Пример: test.bat sync test.xml

Требования к оформлению
1.	Должен быть предоставлен собранный и готовый к запуску проект в виде zip архива. Архив должен обязательно содержать:
•	файл readme.txt, в котором нужно кратко описать, что необходимо выполнить перед запуском проекта;
•	бакап БД;
•	файл настроек приложения;
•	bat скрипт запуска приложения.
2.	Должны быть предоставлены все исходные коды в виде zip архива. Исходный код Java должен быть аккуратно оформлен и документирован в соответствии с технологией JavaDoc.

Неуказанные детали реализуйте по вашему выбору.

## Демо приложение

Проект загружает данные из файла source.csv в базу данных

Толерантен к ошибкам в формате данных, логирует их.

Пример файла application.properties для настройки приложения

```$bash
# Путь к файл с данными
source.path=/opt/source.csv
# Путь куда нужно скопировать файл с данными после обработки
target.path=/home/centos

# Параметры подключения к БД
# Используется H2, чтобы ничего не настраивать
# По умолчанию БД будет создана в рабочем каталоге с именем demo-db
spring.datasource.url=jdbc:h2:./demo-db
spring.datasource.username=sa
spring.datasource.password=
```

Логирование настроено с помощью slf4j + logback в консоль и файл demo.log (без ротации)

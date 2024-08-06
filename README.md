# MyNotes Application

## Overview

The application is designed for creating and managing notes. This is its first version. Its main goal is to overcome the difficulties associated with using "Saved Messages". Now, everything that might be of interest can be cataloged, and you won't have to scroll through the feed to find a desired recipe, article, or list of planned purchases or tasks.

## Features

### Note Creation

A note consists of a name, a link, and text. When creating a note, the only mandatory field is the name; the link and text content fields are optional.

### Media Files

You can add media files to a note, such as voice messages, audio, video, and photos. Media files can be added to an existing note or you can create a new note based on the uploaded file. To do this, simply upload the file, and the bot will then offer to create a new note with the file(s) included.

### Forwarding Messages

You can forward messages from channels to the bot. In this case, a direct link to the message in the channel will be added to the corresponding field in the new note. Similarly, you can save a YouTube video or an article from any other resource. The only requirement for this feature to work correctly is that the message must start with "https". Currently, forwarding messages from private chats does not work, as Telegram prohibits direct links to messages in private chats. Implementing proper display of such messages poses certain challenges, but this feature will be added in the future.

### Editing Notes

Additionally, we were unable to implement a suitable method for editing notes through the bot, so you can edit notes via the website - [https://myvisitcard.freemyip.com](https://myvisitcard.freemyip.com).

## Future Work

Work on the application is ongoing.

---

## Приложение MyNotes

### Обзор

Приложение предназначено для создания и управления заметками. Это первая версия. Его основная задача — преодолеть трудности, связанные с использованием "Сохраненных сообщений". Теперь всё, что может быть интересно, можно каталогизировать и в дальнейшем не листать ленту в поисках нужного рецепта, статьи или списка запланированных покупок или дел.

### Создание заметок

Заметка состоит из имени, какой-либо ссылки и текста. При создании заметки обязательным полем для ввода является только имя, поля ссылки и текстового контента можно опустить.

### Медиафайлы

К заметке можно добавлять медиафайлы, такие как голосовые сообщения, аудио, видео и фото. Медиафайлы можно добавлять как к уже существующей заметке, так и создать новую заметку на основе загруженного файла. Для этого нужно просто загрузить файл, а затем бот предложит создать новую заметку, в которую будет добавлен этот файл или файлы.

### Пересылка сообщений

Можно пересылать в бота сообщения из каналов. В этом случае прямая ссылка на сообщение в канале будет добавлена в соответствующее поле в новой заметке. Точно так же можно сохранить, например, видео с YouTube или статью с любого другого ресурса. Единственным условием для корректной работы этой возможности является наличие "https" в начале сообщения. Временно не работает пересылка сообщений из приватных чатов, так как Telegram запрещает прямые ссылки на сообщения в приватных чатах. Реализация нормального отображения таких сообщений представляет определённые трудности, но в будущем такая возможность тоже появится.

### Редактирование заметок

Также не удалось реализовать приемлемый способ редактирования заметки через бота, поэтому отредактировать заметку можно через сайт - [https://myvisitcard.freemyip.com](https://myvisitcard.freemyip.com).

## Будущие работы

Работа над приложением продолжается.

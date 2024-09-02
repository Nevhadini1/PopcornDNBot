package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.util.*;

public class MyTelegramBot extends TelegramLongPollingBot {
    @Override
    public String getBotUsername() {
        return BotConfig.BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }
    private final String FILE_NAME = "subscribers.txt";
    private Set<Long> confirmedSubscribers = new HashSet<>();

    private Map<Long, String> userState = new HashMap<>();
    private Map<Long, List<Movie>> userMovies = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        long chatId = update.hasMessage() ? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId();

        if (!isUserSubscribed(chatId)) {
            // Якщо користувач не підписаний або відписався, обмежуємо доступ
            sendSubscriptionConfirmationMessage(chatId);
            return;
        }

        if (update.hasCallbackQuery()) {
            // Обробка CallbackQuery
            String callData = update.getCallbackQuery().getData();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();

            if (callData.equals("confirm_subscription")) {
                // Перевіряємо підписку ще раз після натискання кнопки "Підтвердити підписку"
                if (isUserSubscribed(chatId)) {
                    confirmUserSubscription(chatId);
                    sendMessage(chatId, "Спасибо! Вы подтвердили подписку. Теперь вы можете использовать бота.");
                    try {
                        execute(BotUtils.createInlineMenu(chatId));  // Перехід до головного меню
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    sendMessage(chatId, "Подписка не найдена. Пожалуйста, убедитесь, что вы подписаны на канал.");
                }
            } else if (callData.equals("main_menu")) {
                try {
                    execute(BotUtils.createInlineMenu(chatId));
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (callData.startsWith("movie_prev_") || callData.startsWith("movie_next_") || callData.startsWith("movie_page_")) {
                int newIndex = Integer.parseInt(callData.split("_")[2]);
                editMovieDetailsWithNavigation(chatId, messageId, userMovies.get(chatId), newIndex);
            } else if (callData.equals("search_by_title")) {
                userState.put(chatId, "search_by_title");
                sendMessage(chatId, "Введите название фильма:");
            } else if (callData.equals("search_film_by_id")) {
                userState.put(chatId, "search_film_by_id");
                sendMessage(chatId, "Введите идентификатор фильма:");
            } else if (callData.equals("search_serial_by_id")) {
                userState.put(chatId, "search_serial_by_id");
                sendMessage(chatId, "Введите идентификатор сериала:");
            }
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            // Обробка текстових повідомлень
            String messageText = update.getMessage().getText();

            if (messageText.equals("/start")) {
                userState.put(chatId, "main_menu");
                try {
                    execute(BotUtils.createInlineMenu(chatId));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (userState.containsKey(chatId)) {
                if (userState.get(chatId).equals("search_by_title")) {
                    List<Movie> movies = TMDBClient.searchMovieByTitle(messageText);

                    if (movies != null && !movies.isEmpty()) {
                        sendMovieDetailsWithInitialMedia(chatId, movies.get(0), movies, 0);
                        userMovies.put(chatId, movies);
                    } else {
                        sendMessage(chatId, "Фильм не найден.");
                        userState.put(chatId, "main_menu");
                        try {
                            execute(BotUtils.createInlineMenu(chatId));
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else if (userState.get(chatId).equals("search_film_by_id")) {
                    String jsonResponse = TMDBClient.searchMovieById(messageText);

                    if (jsonResponse != null) {
                        Movie movie = parseMovieForId(jsonResponse);
                        sendMovieDetailsWithInitialMedia(chatId, movie, List.of(movie), 0);
                    } else {
                        sendMessage(chatId, "Фильм не найден.");
                    }
                    userState.put(chatId, "main_menu");
                    try {
                        execute(BotUtils.createInlineMenu(chatId));
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                } else if (userState.get(chatId).equals("search_serial_by_id")) {
                    String jsonResponse = TMDBClient.searchSerialById(messageText);

                    if (jsonResponse != null) {
                        Movie tvShow = parseMovieForId(jsonResponse);
                        sendMovieDetailsWithInitialMedia(chatId, tvShow, List.of(tvShow), 0);
                    } else {
                        sendMessage(chatId, "Сериал не найден.");
                    }
                    userState.put(chatId, "main_menu");
                    try {
                        execute(BotUtils.createInlineMenu(chatId));
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    // Метод для відправки повідомлення з проханням підтвердити підписку
    private void sendSubscriptionRequest(long chatId, String channelUsername) {
        String messageText = "Пожалуйста, подпишитесь на канал " + channelUsername + " и нажмите кнопку ниже, чтобы подтвердить подписку.";
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Подтвердить подписку");
        inlineKeyboardButton.setCallbackData("confirm_subscription");
        rowInline.add(inlineKeyboardButton);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private Movie parseMovieForId(String jsonResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        Movie movie = new Movie();

        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // Оскільки відповідь містить дані безпосередньо, а не у масиві 'results'
            movie.setId(rootNode.path("id").asInt());
            movie.setTitle(rootNode.path("title").asText());
            movie.setRating(rootNode.path("vote_average").asDouble());
            movie.setOverview(rootNode.path("overview").asText());
            movie.setPosterPath(rootNode.path("poster_path").asText());
            movie.setYear(rootNode.path("release_date").asText().split("-")[0]);  // Беремо тільки рік

            // Отримання URL трейлера
            String trailerUrl = getMovieTrailer(movie.getId());
            movie.setTrailerUrl(trailerUrl);

            // Парсинг жанрів
            List<String> genres = new ArrayList<>();
            JsonNode genresNode = rootNode.path("genres");
            if (genresNode.isArray()) {
                for (JsonNode genreNode : genresNode) {
                    genres.add(genreNode.path("name").asText());
                }
            }
            movie.setGenres(genres);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return movie;
    }
    public void editMovieDetailsWithNavigation(long chatId, int messageId, List<Movie> movies, int currentIndex) {
        if (currentIndex < 0 || currentIndex >= movies.size()) {
            sendMessage(chatId, "Неверный индекс фильма.");
            return;
        }

        Movie movie = movies.get(currentIndex);

        // Форматування повідомлення з деталями фільму
        StringBuilder messageText = new StringBuilder();
        messageText.append("Название: ").append(movie.getTitle()).append("\n");
        messageText.append("Год: ").append(movie.getYear()).append("\n");
        messageText.append("ID: ").append(movie.getId()).append("\n");
        messageText.append("Жанры: ").append(String.join(", ", movie.getGenres())).append("\n");
        messageText.append("Описание: ").append(movie.getOverview()).append("\n");
        messageText.append("Трейлер: ").append(movie.getTrailerUrl()).append("\n");

        // Створення кнопок для навігації
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Стрілка "Назад"
        if (currentIndex > 0) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton prevButton = new InlineKeyboardButton();
            prevButton.setText("⬅️");
            prevButton.setCallbackData("movie_prev_" + (currentIndex - 1));
            row.add(prevButton);
            rows.add(row);
        }

        // Кнопки для переходу на конкретні сторінки
        List<InlineKeyboardButton> paginationRow = new ArrayList<>();
        for (int i = 0; i < movies.size(); i++) {
            InlineKeyboardButton pageButton = new InlineKeyboardButton();
            pageButton.setText(String.valueOf(i + 1));
            pageButton.setCallbackData("movie_page_" + i);
            paginationRow.add(pageButton);
        }
        rows.add(paginationRow);

        // Стрілка "Вперед"
        if (currentIndex < movies.size() - 1) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText("➡️");
            nextButton.setCallbackData("movie_next_" + (currentIndex + 1));
            row.add(nextButton);
            rows.add(row);
        }


        markup.setKeyboard(rows);

        // Оновлення медіа (обкладинка) та тексту
        EditMessageMedia editMessageMedia = new EditMessageMedia();
        editMessageMedia.setChatId(String.valueOf(chatId));
        editMessageMedia.setMessageId(messageId);

        // Створення InputMediaPhoto для обкладинки
        String caption = truncateCaption(messageText.toString());

        InputMediaPhoto inputMediaPhoto = new InputMediaPhoto();
        inputMediaPhoto.setMedia("https://image.tmdb.org/t/p/w500/" + movie.getPosterPath());
        inputMediaPhoto.setCaption(caption);  // Встановлюємо обрізаний caption

        editMessageMedia.setMedia(inputMediaPhoto);

        List<InlineKeyboardButton> menuRow = new ArrayList<>();
        InlineKeyboardButton menuButton = new InlineKeyboardButton();
        menuButton.setText("Главное меню");
        menuButton.setCallbackData("main_menu");
        menuRow.add(menuButton);
        rows.add(menuRow);// Встановлення InputMediaPhoto

        editMessageMedia.setReplyMarkup(markup);

        try {
            execute(editMessageMedia);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public String truncateCaption(String caption) {
        int maxLength = 1024;

        // Якщо довжина caption перевищує дозволену, обрізаємо її і додаємо три крапки
        if (caption.length() > maxLength) {
            return caption.substring(0, maxLength - 3) + "...";
        } else {
            return caption;
        }
    }



    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public String getMovieTrailer(int movieId) {
            String url = "https://api.themoviedb.org/3/movie/" + movieId + "/videos?api_key=" + "c8991c40035803f029afc21de002b751" + "&language=en-US";
            try {
                HttpResponse<kong.unirest.JsonNode> response = Unirest.get(url).asJson();
                if (response.getStatus() == 200) {
                    JSONArray results = response.getBody().getObject().getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject video = results.getJSONObject(i);
                        if ("Trailer".equals(video.getString("type")) && "YouTube".equals(video.getString("site"))) {
                            return "https://www.youtube.com/watch?v=" + video.getString("key");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null; // Якщо трейлер не знайдено або сталася помилка
        }
    public void sendMovieDetailsWithInitialMedia(long chatId, Movie movie, List<Movie> movies, int currentIndex) {
        // Форматування повідомлення з деталями фільму
        StringBuilder messageText = new StringBuilder();
        messageText.append("Название: ").append(movie.getTitle()).append("\n");
        messageText.append("Год: ").append(movie.getYear()).append("\n");
        messageText.append("ID: ").append(movie.getId()).append("\n");
        messageText.append("Жанры: ").append(String.join(", ", movie.getGenres())).append("\n");
        messageText.append("Описание: ").append(movie.getOverview()).append("\n");
        messageText.append("Трейлер: ").append(movie.getTrailerUrl()).append("\n");

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setPhoto(new InputFile("https://image.tmdb.org/t/p/w500/" + movie.getPosterPath()));
        sendPhoto.setCaption(messageText.toString());

        // Створення кнопок для навігації
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Стрілка "Назад"
        if (currentIndex > 0) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton prevButton = new InlineKeyboardButton();
            prevButton.setText("⬅️");
            prevButton.setCallbackData("movie_prev_" + (currentIndex - 1));
            row.add(prevButton);
            rows.add(row);
        }

        // Кнопки для переходу на конкретні сторінки
        List<InlineKeyboardButton> paginationRow = new ArrayList<>();
        for (int i = 0; i < movies.size(); i++) {
            InlineKeyboardButton pageButton = new InlineKeyboardButton();
            pageButton.setText(String.valueOf(i + 1));
            pageButton.setCallbackData("movie_page_" + i);
            paginationRow.add(pageButton);
        }
        rows.add(paginationRow);

        // Стрілка "Вперед"
        if (currentIndex < movies.size() - 1) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText("➡️");
            nextButton.setCallbackData("movie_next_" + (currentIndex + 1));
            row.add(nextButton);
            rows.add(row);
        }

        List<InlineKeyboardButton> menuRow = new ArrayList<>();
        InlineKeyboardButton menuButton = new InlineKeyboardButton();
        menuButton.setText("Главное меню");
        menuButton.setCallbackData("main_menu");
        menuRow.add(menuButton);
        rows.add(menuRow);

        markup.setKeyboard(rows);
        sendPhoto.setReplyMarkup(markup);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Завантаження підтверджених підписок із файлу при старті бота
    private void sendSubscriptionConfirmationMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Вам нужно подтвердить подписку на наш канал @PopcornDN для использования этого бота.");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton confirmButton = new InlineKeyboardButton();
        confirmButton.setText("Подтвердить подписку");
        confirmButton.setCallbackData("confirm_subscription");
        rowInline.add(confirmButton);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private boolean isUserSubscribed(long chatId) {
        try {
            // Замініть "YOUR_CHANNEL_ID" на ID вашого каналу (можна отримати через @username або через API)
            ChatMember chatMember = execute(new GetChatMember("@PopcornDN", chatId));
            String status = chatMember.getStatus();

            // Якщо користувач є учасником каналу, адміністратором або творцем
            if (status.equals("member") || status.equals("administrator") || status.equals("creator")) {
                return true;
            } else {
                return false;
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return false;
        }
    }
    private void confirmUserSubscription(long chatId) {
        confirmedSubscribers.add(chatId); // Додаємо користувача до списку підтверджених підписників
        saveSubscribers(); // Зберігаємо оновлений список у файл
    }
    private void saveSubscribers() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Long chatId : confirmedSubscribers) {
                bw.write(chatId.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

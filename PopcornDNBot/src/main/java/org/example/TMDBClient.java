package org.example;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TMDBClient {

    private static final String API_KEY = "c8991c40035803f029afc21de002b751";
    private static final String BASE_URL = "https://api.themoviedb.org/3";

//    public static List<Movie> searchMovieByTitle(String title) {
//        List<Movie> results = new ArrayList<>();
//
//        // Пошук фільмів
//        String movieUrl = BASE_URL + "/search/movie";
//        HttpResponse<JsonNode> movieResponse = Unirest.get(movieUrl)
//                .queryString("api_key", API_KEY)
//                .queryString("query", title)
//                .queryString("language", "ru-RU")
//                .asJson();
//
//        if (movieResponse.getStatus() == 200) {
//            JSONObject body = movieResponse.getBody().getObject();
//            JSONArray movieResults = body.getJSONArray("results");
//
//            // Обробка до 5 фільмів
//            for (int i = 0; i < Math.min(movieResults.length(), 5); i++) {
//                JSONObject movieJson = movieResults.getJSONObject(i);
//                Movie movie = new Movie();
//
//                movie.setId(movieJson.getInt("id"));
//                movie.setTitle(getJsonStringSafely(movieJson, "title"));
//                movie.setRating(movieJson.getDouble("vote_average"));
//                movie.setOverview(getJsonStringSafely(movieJson, "overview"));
//                movie.setPosterPath(getJsonStringSafely(movieJson, "poster_path"));
//                movie.setYear(getJsonStringSafely(movieJson, "release_date").split("-")[0]);
//
//                // Парсинг жанрів
//                List<String> genres = new ArrayList<>();
//                JSONArray genreArray = movieJson.getJSONArray("genre_ids");
//                for (int j = 0; j < genreArray.length(); j++) {
//                    genres.add(getGenreNameById(genreArray.getInt(j)));
//                }
//                movie.setGenres(genres);
//
//                // Додавання трейлера
//                String trailerUrl = getMovieTrailer(movie.getId());
//                movie.setTrailerUrl(trailerUrl != null ? trailerUrl : "Трейлер недоступен");
//
//                results.add(movie);
//            }
//        }
//
//        // Пошук серіалів
//        String tvUrl = BASE_URL + "/search/tv";
//        HttpResponse<JsonNode> tvResponse = Unirest.get(tvUrl)
//                .queryString("api_key", API_KEY)
//                .queryString("query", title)
//                .queryString("language", "ru-RU")
//                .asJson();
//
//        if (tvResponse.getStatus() == 200) {
//            JSONObject body = tvResponse.getBody().getObject();
//            JSONArray tvResults = body.getJSONArray("results");
//
//            // Обробка до 5 серіалів
//            for (int i = 0; i < Math.min(tvResults.length(), 5); i++) {
//                JSONObject tvJson = tvResults.getJSONObject(i);
//                Movie tvShow = new Movie();
//                tvShow.setId(tvJson.getInt("id"));
//                tvShow.setTitle(getJsonStringSafely(tvJson, "name")); // Для серіалів використовуємо поле "name"
//                tvShow.setRating(tvJson.getDouble("vote_average"));
//                tvShow.setOverview(getJsonStringSafely(tvJson, "overview"));
//                tvShow.setPosterPath(getJsonStringSafely(tvJson, "poster_path"));
//                tvShow.setYear(getJsonStringSafely(tvJson, "first_air_date").split("-")[0]); // Для серіалів використовуємо "first_air_date"
//
//                // Парсинг жанрів
//                List<String> genres = new ArrayList<>();
//                JSONArray genreArray = tvJson.getJSONArray("genre_ids");
//                for (int j = 0; j < genreArray.length(); j++) {
//                    genres.add(getGenreNameById(genreArray.getInt(j)));
//                }
//                tvShow.setGenres(genres);
//
//                // Додавання трейлера
//                String trailerUrl = getMovieTrailer(tvShow.getId());
//                tvShow.setTrailerUrl(trailerUrl != null ? trailerUrl : "Трейлер недоступен");
//
//                results.add(tvShow);
//            }
//        }
//
//        return results;
//    }

//    public static List<Movie> searchMovieByTitle(String title) {
//        List<Movie> results = new ArrayList<>();
//
//        // Пошук фільмів
//        String movieUrl = BASE_URL + "/search/movie";
//        HttpResponse<JsonNode> movieResponse = Unirest.get(movieUrl)
//                .queryString("api_key", API_KEY)
//                .queryString("query", title)
//                .queryString("language", "ru-RU")
//                .asJson();
//
//        if (movieResponse.getStatus() == 200) {
//            JSONObject body = movieResponse.getBody().getObject();
//            JSONArray movieResults = body.getJSONArray("results");
//
//            // Обробка до 5 фільмів
//            for (int i = 0; i < Math.min(movieResults.length(), 5); i++) {
//                JSONObject movieJson = movieResults.getJSONObject(i);
//                Movie movie = new Movie();
//
//                // Перевірка наявності необхідних полів
//                if (movieJson.has("id") &&
//                        movieJson.has("title") &&
//                        movieJson.has("vote_average") &&
//                        movieJson.has("overview") &&
//                        movieJson.has("poster_path") &&
//                        movieJson.has("release_date")) {
//
//                    movie.setId(movieJson.getInt("id"));
//                    movie.setTitle(getJsonStringSafely(movieJson, "title"));
//                    movie.setRating(movieJson.getDouble("vote_average"));
//                    movie.setOverview(getJsonStringSafely(movieJson, "overview"));
//                    movie.setPosterPath(getJsonStringSafely(movieJson, "poster_path"));
//                    movie.setYear(getJsonStringSafely(movieJson, "release_date").split("-")[0]);
//
//                    // Парсинг жанрів
//                    List<String> genres = new ArrayList<>();
//                    if (movieJson.has("genre_ids")) {
//                        JSONArray genreArray = movieJson.getJSONArray("genre_ids");
//                        for (int j = 0; j < genreArray.length(); j++) {
//                            genres.add(getGenreNameById(genreArray.getInt(j)));
//                        }
//                        movie.setGenres(genres);
//                    } else {
//                        continue; // Пропускаємо цей фільм, якщо жанри недоступні
//                    }
//
//                    // Додавання трейлера
//                    String trailerUrl = getMovieTrailer(movie.getId());
//                    movie.setTrailerUrl(trailerUrl != null ? trailerUrl : "Трейлер недоступен");
//
//                    results.add(movie);
//                }
//            }
//        }
//
//        // Пошук серіалів
//        String tvUrl = BASE_URL + "/search/tv";
//        HttpResponse<JsonNode> tvResponse = Unirest.get(tvUrl)
//                .queryString("api_key", API_KEY)
//                .queryString("query", title)
//                .queryString("language", "ru-RU")
//                .asJson();
//
//        if (tvResponse.getStatus() == 200) {
//            JSONObject body = tvResponse.getBody().getObject();
//            JSONArray tvResults = body.getJSONArray("results");
//
//            // Обробка до 5 серіалів
//            for (int i = 0; i < Math.min(tvResults.length(), 5); i++) {
//                JSONObject tvJson = tvResults.getJSONObject(i);
//                Movie tvShow = new Movie();
//
//                // Перевірка наявності необхідних полів
//                if (tvJson.has("id") &&
//                        tvJson.has("name") &&
//                        tvJson.has("vote_average") &&
//                        tvJson.has("overview") &&
//                        tvJson.has("poster_path") &&
//                        tvJson.has("first_air_date")) {
//
//                    tvShow.setId(tvJson.getInt("id"));
//                    tvShow.setTitle(getJsonStringSafely(tvJson, "name")); // Для серіалів використовуємо поле "name"
//                    tvShow.setRating(tvJson.getDouble("vote_average"));
//                    tvShow.setOverview(getJsonStringSafely(tvJson, "overview"));
//                    tvShow.setPosterPath(getJsonStringSafely(tvJson, "poster_path"));
//                    tvShow.setYear(getJsonStringSafely(tvJson, "first_air_date").split("-")[0]); // Для серіалів використовуємо "first_air_date"
//
//                    // Парсинг жанрів
//                    List<String> genres = new ArrayList<>();
//                    if (tvJson.has("genre_ids")) {
//                        JSONArray genreArray = tvJson.getJSONArray("genre_ids");
//                        for (int j = 0; j < genreArray.length(); j++) {
//                            genres.add(getGenreNameById(genreArray.getInt(j)));
//                        }
//                        tvShow.setGenres(genres);
//                    } else {
//                        continue; // Пропускаємо цей серіал, якщо жанри недоступні
//                    }
//
//                    // Додавання трейлера
//                    String trailerUrl = getMovieTrailer(tvShow.getId());
//                    tvShow.setTrailerUrl(trailerUrl != null ? trailerUrl : "Трейлер недоступен");
//
//                    results.add(tvShow);
//                }
//            }
//        }
//
//        return results;
//    }
public static List<Movie> searchMovieByTitle(String title) {
    List<Movie> results = new ArrayList<>();

    // Пошук фільмів
    String movieUrl = BASE_URL + "/search/movie";
    HttpResponse<JsonNode> movieResponse = Unirest.get(movieUrl)
            .queryString("api_key", API_KEY)
            .queryString("query", title)
            .queryString("language", "ru-RU")
            .asJson();

    if (movieResponse.getStatus() == 200) {
        JSONObject body = movieResponse.getBody().getObject();
        JSONArray movieResults = body.getJSONArray("results");

        // Обробка до 5 фільмів
        for (int i = 0; i < Math.min(movieResults.length(), 5); i++) {
            JSONObject movieJson = movieResults.getJSONObject(i);
            Movie movie = new Movie();

            String posterPath = getJsonStringSafely(movieJson, "poster_path");
            if (posterPath == null || posterPath.isEmpty()) {
                continue; // Пропускаємо фільми без постера
            }

            movie.setId(movieJson.getInt("id"));
            movie.setTitle(getJsonStringSafely(movieJson, "title"));
            movie.setRating(movieJson.getDouble("vote_average"));
            movie.setOverview(truncateOverview(getJsonStringSafely(movieJson, "overview")));
            movie.setPosterPath(posterPath);
            movie.setYear(getJsonStringSafely(movieJson, "release_date").split("-")[0]);

            // Парсинг жанрів
            List<String> genres = new ArrayList<>();
            JSONArray genreArray = movieJson.getJSONArray("genre_ids");
            for (int j = 0; j < genreArray.length(); j++) {
                genres.add(getGenreNameById(genreArray.getInt(j)));
            }
            movie.setGenres(genres);

            // Додавання трейлера
            String trailerUrl = getMovieTrailer(movie.getId());
            movie.setTrailerUrl(trailerUrl != null ? trailerUrl : "Трейлер недоступен");

            results.add(movie);
        }
    }

    // Пошук серіалів
    String tvUrl = BASE_URL + "/search/tv";
    HttpResponse<JsonNode> tvResponse = Unirest.get(tvUrl)
            .queryString("api_key", API_KEY)
            .queryString("query", title)
            .queryString("language", "ru-RU")
            .asJson();

    if (tvResponse.getStatus() == 200) {
        JSONObject body = tvResponse.getBody().getObject();
        JSONArray tvResults = body.getJSONArray("results");

        // Обробка до 5 серіалів
        for (int i = 0; i < Math.min(tvResults.length(), 5); i++) {
            JSONObject tvJson = tvResults.getJSONObject(i);
            Movie tvShow = new Movie();

            String posterPath = getJsonStringSafely(tvJson, "poster_path");
            if (posterPath == null || posterPath.isEmpty()) {
                continue; // Пропускаємо серіали без постера
            }

            tvShow.setId(tvJson.getInt("id"));
            tvShow.setTitle(getJsonStringSafely(tvJson, "name")); // Для серіалів використовуємо поле "name"
            tvShow.setRating(tvJson.getDouble("vote_average"));
            tvShow.setOverview(truncateOverview(getJsonStringSafely(tvJson, "overview")));
            tvShow.setPosterPath(posterPath);
            tvShow.setYear(getJsonStringSafely(tvJson, "first_air_date").split("-")[0]); // Для серіалів використовуємо "first_air_date"

            // Парсинг жанрів
            List<String> genres = new ArrayList<>();
            JSONArray genreArray = tvJson.getJSONArray("genre_ids");
            for (int j = 0; j < genreArray.length(); j++) {
                genres.add(getGenreNameById(genreArray.getInt(j)));
            }
            tvShow.setGenres(genres);

            // Додавання трейлера
            String trailerUrl = getMovieTrailer(tvShow.getId());
            tvShow.setTrailerUrl(trailerUrl != null ? trailerUrl : "Трейлер недоступен");

            results.add(tvShow);
        }
    }

    return results;
}
    private static String truncateOverview(String overview) {
        if (overview != null && overview.length() > 300) {
            return overview.substring(0, 297) + "...";
        } else {
            return overview;
        }
    }


    public static String getMovieTrailer(int movieId) {
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

    private static String getGenreNameById(int genreId) {
        switch (genreId) {
            case 28:
                return "Боевик";         // Action
            case 12:
                return "Приключения";    // Adventure
            case 16:
                return "Мультфильм";     // Animation
            case 35:
                return "Комедия";        // Comedy
            case 80:
                return "Криминал";       // Crime
            case 99:
                return "Документальный"; // Documentary
            case 18:
                return "Драма";          // Drama
            case 10751:
                return "Семейный";       // Family
            case 14:
                return "Фэнтези";        // Fantasy
            case 36:
                return "Исторический";   // History
            case 27:
                return "Ужасы";          // Horror
            case 10402:
                return "Музыка";         // Music
            case 9648:
                return "Мистика";        // Mystery
            case 10749:
                return "Мелодрама";      // Romance
            case 878:
                return "Фантастика";     // Science Fiction
            case 10770:
                return "ТВ фильм";       // TV Movie
            case 53:
                return "Триллер";        // Thriller
            case 10752:
                return "Военный";        // War
            case 37:
                return "Вестерн";        // Western
            default:
                return "Неизвестный";    // Unknown
        }
    }

    public static String searchMovieById(String movieId) {
        String url = BASE_URL + "/movie/" + movieId; // Базовий URL для пошуку фільму за ID

        HttpResponse<JsonNode> response = Unirest.get(url)
                .queryString("api_key", API_KEY) // Додавання API ключа
                .queryString("language", "ru-RU") // Встановлення мови відповіді
                .asJson();

        System.out.println("Запит URL: " + response.getHeaders().getFirst("Request-Url"));
        System.out.println("Статус код: " + response.getStatus());
        System.out.println("Тіло відповіді: " + response.getBody());

        if (response.getStatus() == 200) {
            return response.getBody().toString(); // Повернення тіла відповіді у вигляді рядка JSON
        } else {
            return null; // Повернення null, якщо запит не був успішним
        }
    }

    public static String searchSerialById(String tvShowId) {
        String url = BASE_URL + "/tv/" + tvShowId;

        HttpResponse<JsonNode> response = Unirest.get(url)
                .queryString("api_key", API_KEY)
                .queryString("language", "ru-RU")
                .asJson();

        System.out.println("Запит URL: " + response.getHeaders().getFirst("Request-Url"));
        System.out.println("Статус код: " + response.getStatus());
        System.out.println("Тіло відповіді: " + response.getBody());

        if (response.getStatus() == 200) {
            return response.getBody().toString();
        } else {
            return null;
        }
    }

    private static String getJsonStringSafely(JSONObject jsonObject, String key) {
        try {
            if (!jsonObject.isNull(key)) {
                return jsonObject.getString(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}


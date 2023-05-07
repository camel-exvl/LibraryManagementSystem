package web;

import java.util.logging.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import entities.Book;
import library.LibraryManagementSystem;
import library.LibraryManagementSystemImpl;
import queries.ApiResult;
import queries.BookQueryConditions;
import queries.BookQueryResults;
import queries.SortOrder;
import utils.ConnectConfig;
import utils.DatabaseConnector;

@SpringBootApplication
@Controller
public class WebServiceApplication {

    private static final Logger log = Logger.getLogger(WebServiceApplication.class.getName());
    private static LibraryManagementSystem library;
    private static BookQueryConditions bookQueryConditions = new BookQueryConditions();

    public static void run(String[] args) {
        try {
            // parse connection config from "resources/application.yaml"
            ConnectConfig conf = new ConnectConfig();
            log.info("Success to parse connect config. " + conf.toString());
            // connect to database
            DatabaseConnector connector = new DatabaseConnector(conf);
            boolean connStatus = connector.connect();
            if (!connStatus) {
                log.severe("Failed to connect database.");
                System.exit(1);
            }
            library = new LibraryManagementSystemImpl(connector);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    // release database connection handler
                    if (connector.release()) {
                        log.info("Success to release connection.");
                        System.out.println("Success to release connection.");
                    } else {
                        log.warning("Failed to release connection.");
                        System.out.println("Failed to release connection.");
                    }
                }
            });
            SpringApplication.run(WebServiceApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/query/queryBook")
    public String queryBook(@RequestParam(name = "queryBookSelect", required = false) String queryBookSelect,
            @RequestParam(name = "queryBookSearch", required = false) String queryBookInput,
            @RequestParam(name = "queryBookSearchRangeMin", required = false) String queryBookSearchRangeMin,
            @RequestParam(name = "queryBookSearchRangeMax", required = false) String queryBookSearchRangeMax,
            @RequestParam(name = "queryBookSortBy", required = false) String queryBookSortBy,
            @RequestParam(name = "queryBookSortOrder", required = false) String queryBookSortOrder,
            @RequestParam(name = "queryBookSearchTwice", required = false) String queryBookSearchTwice,
            Model model) {
        if (queryBookSelect != null) {
            try {
                model.addAttribute("queryBookSelect", queryBookSelect);
                model.addAttribute("queryBookSearchInput", queryBookInput);
                model.addAttribute("queryBookSearchRangeMinInput", queryBookSearchRangeMin);
                model.addAttribute("queryBookSearchRangeMaxInput", queryBookSearchRangeMax);
                model.addAttribute("queryBookSortBy", queryBookSortBy);
                model.addAttribute("queryBookSortOrder", queryBookSortOrder);
                if (queryBookSearchTwice == null) {
                    bookQueryConditions = new BookQueryConditions();
                }
                switch (queryBookSelect) {
                    case "category":
                        bookQueryConditions.setCategory(queryBookInput);
                        break;
                    case "title":
                        bookQueryConditions.setTitle(queryBookInput);
                        break;
                    case "press":
                        bookQueryConditions.setPress(queryBookInput);
                        break;
                    case "publishYear":
                        bookQueryConditions.setMinPublishYear(Integer.parseInt(queryBookSearchRangeMin));
                        bookQueryConditions.setMaxPublishYear(Integer.parseInt(queryBookSearchRangeMax));
                        break;
                    case "author":
                        bookQueryConditions.setAuthor(queryBookInput);
                        break;
                    case "price":
                        bookQueryConditions.setMinPrice(Double.parseDouble(queryBookSearchRangeMin));
                        bookQueryConditions.setMaxPrice(Double.parseDouble(queryBookSearchRangeMax));
                        break;
                }
                bookQueryConditions.setSortBy(Book.SortColumn.valueOf(queryBookSortBy));
                bookQueryConditions.setSortOrder(SortOrder.valueOf(queryBookSortOrder));
                ApiResult result = library.queryBook(bookQueryConditions);
                if (result.ok) {
                    model.addAttribute("Books", ((BookQueryResults) result.payload).getResults());
                } else {
                    throw new Exception(result.message);
                }
            } catch (Exception e) {
                log.warning(e.getMessage());
            }
        } else {
            try {
                model.addAttribute("queryBookSelect", "category");
                model.addAttribute("queryBookSearchInput", "");
                model.addAttribute("queryBookSearchRangeMinInput", "");
                model.addAttribute("queryBookSearchRangeMaxInput", "");
                model.addAttribute("queryBookSortBy", "BOOK_ID");
                model.addAttribute("queryBookSortOrder", "ASC");
            } catch (Exception e) {
                log.warning(e.getMessage());
            }
        }
        return "query/queryBook";
    }
}
package web;

import java.util.logging.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import entities.Book;
import entities.Borrow;
import library.LibraryManagementSystem;
import library.LibraryManagementSystemImpl;
import queries.ApiResult;
import queries.BookQueryConditions;
import queries.BookQueryResults;
import queries.BorrowHistories;
import queries.CardList;
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
        try {
            if (queryBookSelect != null) {
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
                model.addAttribute("message", "查询成功");
            } else {
                model.addAttribute("queryBookSelect", "category");
                model.addAttribute("queryBookSearchInput", "");
                model.addAttribute("queryBookSearchRangeMinInput", "");
                model.addAttribute("queryBookSearchRangeMaxInput", "");
                model.addAttribute("queryBookSortBy", "BOOK_ID");
                model.addAttribute("queryBookSortOrder", "ASC");
            }
        } catch (Exception e) {
            log.warning(e.getMessage());
            model.addAttribute("message", e.getMessage());
        }
        return "query/queryBook";
    }

    @GetMapping("/query/queryBorrow")
    public String queryBorrow(@RequestParam(name = "queryBorrowSearch", required = false) String queryBorrowInput,
            Model model) {
        try {
            if (queryBorrowInput != null) {
                model.addAttribute("queryBorrowSearchInput", queryBorrowInput);
                ApiResult result = library.showBorrowHistory(Integer.parseInt(queryBorrowInput));
                if (result.ok) {
                    model.addAttribute("Borrows", ((BorrowHistories) result.payload).getItems());
                } else {
                    throw new Exception(result.message);
                }
                model.addAttribute("message", "查询成功");
            } else {
                model.addAttribute("queryBorrowSearchInput", "");
            }
        } catch (Exception e) {
            log.warning(e.getMessage());
            model.addAttribute("message", e.getMessage());
        }
        return "query/queryBorrow";
    }

    @GetMapping("/query/queryCard")
    public String queryCard(@RequestParam(name = "queryCardSearch", required = false) String queryCardInput,
            Model model) {
        try {
            if (queryCardInput != null) {
                ApiResult result = library.showCards();
                if (result.ok) {
                    model.addAttribute("Cards", ((CardList) result.payload).getCards());
                } else {
                    throw new Exception(result.message);
                }
                model.addAttribute("message", "查询成功");
            }
        } catch (Exception e) {
            log.warning(e.getMessage());
            model.addAttribute("message", e.getMessage());
        }
        return "query/queryCard";
    }

    @GetMapping("/borrowBook")
    public String borrowBook(@RequestParam(name = "mode", required = false) String mode,
            @RequestParam(name = "borrowCardID", required = false) String borrowCardID,
            @RequestParam(name = "borrowBookID", required = false) String borrowBookID,
            @RequestParam(name = "returnCardID", required = false) String returnCardID,
            @RequestParam(name = "returnBookID", required = false) String returnBookID,
            Model model) {
        try {
            if (mode != null) {
                model.addAttribute("borrowBookBorrowCardIDInput", borrowCardID);
                model.addAttribute("borrowBookBorrowBookIDInput", borrowBookID);
                model.addAttribute("borrowBookReturnCardIDInput", returnCardID);
                model.addAttribute("borrowBookReturnBookIDInput", returnBookID);
                if (mode.equals("borrow")) {
                    Borrow borrow = new Borrow(Integer.parseInt(borrowCardID), Integer.parseInt(borrowBookID));
                    borrow.resetBorrowTime();
                    ApiResult result = library.borrowBook(borrow);
                    if (result.ok) {
                        model.addAttribute("message", "借书成功");
                    } else {
                        throw new Exception(result.message);
                    }
                } else if (mode.equals("return")) {
                    Borrow borrow = new Borrow(Integer.parseInt(returnCardID), Integer.parseInt(returnBookID));
                    borrow.resetReturnTime();
                    ApiResult result = library.returnBook(borrow);
                    if (result.ok) {
                        model.addAttribute("message", "还书成功");
                    } else {
                        throw new Exception(result.message);
                    }
                }
            } else {
                model.addAttribute("borrowBookBorrowCardIDInput", "");
                model.addAttribute("borrowBookBorrowBookIDInput", "");
                model.addAttribute("borrowBookReturnCardIDInput", "");
                model.addAttribute("borrowBookReturnBookIDInput", "");
            }
        } catch (Exception e) {
            log.warning(e.getMessage());
            model.addAttribute("message", e.getMessage());
        }
        return "borrowBook";
    }

    @GetMapping("/manage/manageBook")
    public String manageBook(
            @RequestParam(name = "manageBookManageSelect", required = false) String manageBookManageSelect,
            @RequestParam(name = "manageBookManageID", required = false) String manageBookManageID,
            @RequestParam(name = "manageBookManageCategory", required = false) String manageBookManageCategory,
            @RequestParam(name = "manageBookManageTitle", required = false) String manageBookManageTitle,
            @RequestParam(name = "manageBookManagePress", required = false) String manageBookManagePress,
            @RequestParam(name = "manageBookManagePublisherYear", required = false) String manageBookManagePublisherYear,
            @RequestParam(name = "manageBookManageAuthor", required = false) String manageBookManageAuthor,
            @RequestParam(name = "manageBookManagePrice", required = false) String manageBookManagePrice,
            @RequestParam(name = "manageBookManageStock", required = false) String manageBookManageStock,
            Model model) {
        try {
            if (manageBookManageSelect != null) {
                model.addAttribute("manageBookManageSelect", manageBookManageSelect);
                model.addAttribute("manageBookManageID", manageBookManageID);
                model.addAttribute("manageBookManageCategory", manageBookManageCategory);
                model.addAttribute("manageBookManageTitle", manageBookManageTitle);
                model.addAttribute("manageBookManagePress", manageBookManagePress);
                model.addAttribute("manageBookManagePublisherYear", manageBookManagePublisherYear);
                model.addAttribute("manageBookManageAuthor", manageBookManageAuthor);
                model.addAttribute("manageBookManagePrice", manageBookManagePrice);
                model.addAttribute("manageBookManageStock", manageBookManageStock);
                if (manageBookManageSelect.equals("new")) {
                    Book book = new Book(manageBookManageCategory, manageBookManageTitle, manageBookManagePress,
                            Integer.parseInt(manageBookManagePublisherYear), manageBookManageAuthor,
                            Double.parseDouble(manageBookManagePrice), Integer.parseInt(manageBookManageStock));
                    ApiResult result = library.storeBook(book);
                    if (result.ok) {
                        model.addAttribute("message", "入库成功");
                    } else {
                        throw new Exception(result.message);
                    }
                } else if (manageBookManageSelect.equals("stock")) {
                    ApiResult result = library.incBookStock(Integer.parseInt(manageBookManageID),
                            Integer.parseInt(manageBookManageStock));
                    if (result.ok) {
                        model.addAttribute("message", "修改成功");
                    } else {
                        throw new Exception(result.message);
                    }
                } else if (manageBookManageSelect.equals("update")) {
                    Book book = new Book(manageBookManageCategory, manageBookManageTitle, manageBookManagePress,
                            Integer.parseInt(manageBookManagePublisherYear), manageBookManageAuthor,
                            Double.parseDouble(manageBookManagePrice), 0);
                    book.setBookId(Integer.parseInt(manageBookManageID));
                    ApiResult result = library.modifyBookInfo(book);
                    if (result.ok) {
                        model.addAttribute("message", "修改成功");
                    } else {
                        throw new Exception(result.message);
                    }
                } else if (manageBookManageSelect.equals("delete")) {
                    ApiResult result = library.removeBook(Integer.parseInt(manageBookManageID));
                    if (result.ok) {
                        model.addAttribute("message", "删除成功");
                    } else {
                        throw new Exception(result.message);
                    }
                }
            } else {
                model.addAttribute("manageBookManageSelect", "new");
                model.addAttribute("manageBookManageID", "");
                model.addAttribute("manageBookManageCategory", "");
                model.addAttribute("manageBookManageTitle", "");
                model.addAttribute("manageBookManagePress", "");
                model.addAttribute("manageBookManagePublisherYear", "");
                model.addAttribute("manageBookManageAuthor", "");
                model.addAttribute("manageBookManagePrice", "");
                model.addAttribute("manageBookManageStock", "");
            }
        } catch (Exception e) {
            log.warning(e.getMessage());
            model.addAttribute("message", e.getMessage());
        }
        return "manage/manageBook";
    }
}

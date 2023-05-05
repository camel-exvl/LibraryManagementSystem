import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import entities.Book;
import entities.Borrow;
import entities.Card;
import entities.Card.CardType;
import queries.ApiResult;
import queries.BookQueryConditions;
import queries.BookQueryResults;
import queries.BorrowHistories;
import queries.BorrowHistories.Item;
import queries.CardList;
import utils.DBInitializer;
import utils.DatabaseConnector;

public class LibraryManagementSystemImpl implements LibraryManagementSystem {

    private final DatabaseConnector connector;

    public LibraryManagementSystemImpl(DatabaseConnector connector) {
        this.connector = connector;
    }

    @Override
    public ApiResult storeBook(Book book) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM book WHERE book_id = ? FOR UPDATE");
            stmt.setInt(1, book.getBookId());
            ResultSet rs = stmt.executeQuery();
            commit(conn);
            if (rs.next()) {
                return new ApiResult(false, "Book already exists");
            }
            stmt = conn.prepareStatement(
                    "INSERT INTO book (category, title, press, publish_year, author, price, stock) VALUES (?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, book.getCategory());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getPress());
            stmt.setInt(4, book.getPublishYear());
            stmt.setString(5, book.getAuthor());
            stmt.setDouble(6, book.getPrice());
            stmt.setInt(7, book.getStock());
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            commit(conn);
            if (rs.next()) {
                book.setBookId(rs.getInt(1));
            }
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult incBookStock(int bookId, int deltaStock) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM book WHERE book_id = ? FOR UPDATE");
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            commit(conn);
            if (!rs.next()) {
                return new ApiResult(false, "Book not found");
            }
            int stock = rs.getInt("stock");
            if (stock + deltaStock < 0) {
                return new ApiResult(false, "Stock not enough");
            }
            stmt = conn.prepareStatement("UPDATE book SET stock = ? WHERE book_id = ?");
            stmt.setInt(1, stock + deltaStock);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult storeBook(List<Book> books) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM book WHERE book_id = ? FOR UPDATE");
            for (Book book : books) {
                stmt.setInt(1, book.getBookId());
                ResultSet rs = stmt.executeQuery();
                commit(conn);
                if (rs.next()) {
                    return new ApiResult(false, "Book already exists");
                }
            }
            stmt = conn.prepareStatement(
                    "INSERT INTO book (category, title, press, publish_year, author, price, stock) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            for (Book book : books) {
                stmt.setString(1, book.getCategory());
                stmt.setString(2, book.getTitle());
                stmt.setString(3, book.getPress());
                stmt.setInt(4, book.getPublishYear());
                stmt.setString(5, book.getAuthor());
                stmt.setDouble(6, book.getPrice());
                stmt.setInt(7, book.getStock());
                stmt.addBatch();
            }
            stmt.executeBatch();
            ResultSet rs = stmt.getGeneratedKeys();
            commit(conn);
            for (Book book : books) {
                if (rs.next()) {
                    book.setBookId(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult removeBook(int bookId) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM borrow WHERE book_id = ? AND return_time = 0 FOR UPDATE");
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            commit(conn);
            if (rs.next()) {
                return new ApiResult(false, "Book is borrowed");
            }
            stmt = conn.prepareStatement("DELETE FROM book WHERE book_id = ?");
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult modifyBookInfo(Book book) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE book SET category = ?, title = ?, press = ?, publish_year = ?, author = ?, price = ? WHERE book_id = ?");
            stmt.setString(1, book.getCategory());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getPress());
            stmt.setInt(4, book.getPublishYear());
            stmt.setString(5, book.getAuthor());
            stmt.setDouble(6, book.getPrice());
            stmt.setInt(7, book.getBookId());
            stmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult queryBook(BookQueryConditions conditions) {
        Connection conn = connector.getConn();
        List<Book> books = new ArrayList<>();
        try {
            StringBuffer sql = new StringBuffer(
                    "SELECT * FROM book WHERE (category = ? OR ? IS NULL) AND (title = ? OR ? IS NULL) AND (press = ? OR ? IS NULL) AND (publish_year >= ? OR ? IS NULL) AND (publish_year <= ? OR ? IS NULL) AND (author = ? OR ? IS NULL) AND (price >= ? OR ? IS NULL) AND (price <= ? OR ? IS NULL)");
            if (conditions.getSortBy() != Book.SortColumn.BOOK_ID) {
                sql.append(
                        " ORDER BY " + conditions.getSortBy().toString() + " " + conditions.getSortOrder().toString()
                                + " , book_id ASC");
            } else {
                sql.append(" ORDER BY book_id " + conditions.getSortOrder().toString());
            }
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, conditions.getCategory());
            pstmt.setString(2, conditions.getCategory());
            pstmt.setString(3, conditions.getTitle());
            pstmt.setString(4, conditions.getTitle());
            pstmt.setString(5, conditions.getPress());
            pstmt.setString(6, conditions.getPress());
            pstmt.setInt(7, conditions.getMinPublishYear());
            pstmt.setInt(8, conditions.getMinPublishYear());
            pstmt.setInt(9, conditions.getMaxPublishYear());
            pstmt.setInt(10, conditions.getMaxPublishYear());
            pstmt.setString(11, conditions.getAuthor());
            pstmt.setString(12, conditions.getAuthor());
            pstmt.setDouble(13, conditions.getMinPrice());
            pstmt.setDouble(14, conditions.getMinPrice());
            pstmt.setDouble(15, conditions.getMaxPrice());
            pstmt.setDouble(16, conditions.getMaxPrice());
            ResultSet rs = pstmt.executeQuery();
            commit(conn);
            while (rs.next()) {
                Book book = new Book();
                book.setBookId(rs.getInt("book_id"));
                book.setCategory(rs.getString("category"));
                book.setTitle(rs.getString("title"));
                book.setPress(rs.getString("press"));
                book.setPublishYear(rs.getInt("publish_year"));
                book.setAuthor(rs.getString("author"));
                book.setPrice(rs.getDouble("price"));
                book.setStock(rs.getInt("stock"));
                books.add(book);
            }
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, new BookQueryResults(books));
    }

    @Override
    public ApiResult borrowBook(Borrow borrow) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM book WHERE book_id = ? AND stock > 0 FOR UPDATE");
            pstmt.setInt(1, borrow.getBookId());
            pstmt.addBatch();
            pstmt = conn.prepareStatement(
                    "SELECT * FROM borrow WHERE card_id = ? AND book_id = ? AND return_time = 0 FOR UPDATE");
            pstmt.setInt(1, borrow.getCardId());
            pstmt.setInt(2, borrow.getBookId());
            pstmt.addBatch();
            pstmt.executeBatch();
            ResultSet rs = pstmt.getResultSet();
            if (!rs.next()) {
                return new ApiResult(false, "No such book or no stock");
            }
            rs.next();
            if (!rs.next()) {
                return new ApiResult(false, "Have borrowed this book");
            }
            pstmt = conn.prepareStatement(
                    "INSERT INTO borrow (card_id, book_id, borrow_time, return_time) VALUES (?, ?, ?, ?)");
            pstmt.setInt(1, borrow.getCardId());
            pstmt.setInt(2, borrow.getBookId());
            pstmt.setLong(3, borrow.getBorrowTime());
            pstmt.setLong(4, borrow.getReturnTime());
            pstmt.executeUpdate();
            pstmt = conn.prepareStatement("UPDATE book SET stock = stock - 1 WHERE book_id = ?");
            pstmt.setInt(1, borrow.getBookId());
            pstmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult returnBook(Borrow borrow) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE borrow SET return_time = ? WHERE card_id = ? AND book_id = ? AND return_time = 0");
            pstmt.setLong(1, borrow.getReturnTime());
            pstmt.setInt(2, borrow.getCardId());
            pstmt.setInt(3, borrow.getBookId());
            pstmt.executeUpdate();
            pstmt = conn.prepareStatement("UPDATE book SET stock = stock + 1 WHERE book_id = ?");
            pstmt.setInt(1, borrow.getBookId());
            pstmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult showBorrowHistory(int cardId) {
        Connection conn = connector.getConn();
        List<Item> items = new ArrayList<>();
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM borrow NATURAL JOIN book WHERE card_id = ? ORDER BY borrow_time DESC, book_id ASC");
            pstmt.setInt(1, cardId);
            ResultSet rs = pstmt.executeQuery();
            commit(conn);
            while (rs.next()) {
                Book book = new Book(rs.getString("category"), rs.getString("title"),
                        rs.getString("press"), rs.getInt("publish_year"), rs.getString("author"),
                        rs.getDouble("price"), rs.getInt("stock"));
                book.setBookId(rs.getInt("book_id"));
                Borrow borrow = new Borrow(rs.getInt("book_id"), rs.getInt("card_id"));
                borrow.setBorrowTime(rs.getLong("borrow_time"));
                borrow.setReturnTime(rs.getLong("return_time"));
                Item item = new Item(rs.getInt("card_id"), book, borrow);
                items.add(item);
            }
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, new BorrowHistories(items));
    }

    @Override
    public ApiResult registerCard(Card card) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM card WHERE name = ? AND department = ? AND type = ? FOR UPDATE");
            pstmt.setString(1, card.getName());
            pstmt.setString(2, card.getDepartment());
            pstmt.setString(3, card.getType().getStr());
            pstmt.executeQuery();
            ResultSet rs = pstmt.getResultSet();
            commit(conn);
            if (rs.next()) {
                return new ApiResult(false, "Card already exists");
            }
            pstmt = conn.prepareStatement(
                    "INSERT INTO card (name, department, type) VALUES (?, ?, ?)");
            pstmt.setString(1, card.getName());
            pstmt.setString(2, card.getDepartment());
            pstmt.setString(3, card.getType().getStr());
            pstmt.executeQuery();
            rs = pstmt.getGeneratedKeys();
            commit(conn);
            if (rs.next()) {
                card.setCardId(rs.getInt(1));
            }
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult removeCard(int cardId) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM borrow WHERE card_id = ? AND return_time = 0 FOR UPDATE");
            pstmt.setInt(1, cardId);
            ResultSet rs = pstmt.executeQuery();
            commit(conn);
            if (rs.next()) {
                return new ApiResult(false, "Card has borrowed books");
            }
            pstmt = conn.prepareStatement(
                    "DELETE FROM card WHERE card_id = ?");
            pstmt.setInt(1, cardId);
            pstmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult showCards() {
        Connection conn = connector.getConn();
        List<Card> cards = new ArrayList<>();
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM card");
            ResultSet rs = pstmt.executeQuery();
            commit(conn);
            while (rs.next()) {
                Card card = new Card(rs.getInt("card_id"), rs.getString("name"),
                        rs.getString("department"), CardType.valueOf(rs.getString("type")));
                cards.add(card);
            }
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, new CardList(cards));
    }

    @Override
    public ApiResult resetDatabase() {
        Connection conn = connector.getConn();
        try {
            Statement stmt = conn.createStatement();
            DBInitializer initializer = connector.getConf().getType().getDbInitializer();
            stmt.addBatch(initializer.sqlDropBorrow());
            stmt.addBatch(initializer.sqlDropBook());
            stmt.addBatch(initializer.sqlDropCard());
            stmt.addBatch(initializer.sqlCreateCard());
            stmt.addBatch(initializer.sqlCreateBook());
            stmt.addBatch(initializer.sqlCreateBorrow());
            stmt.executeBatch();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void commit(Connection conn) {
        try {
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

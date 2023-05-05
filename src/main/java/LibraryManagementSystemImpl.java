import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
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
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM book WHERE category = ? AND title = ? AND press = ? AND publish_year = ? AND author = ?");
            stmt.setString(1, book.getCategory());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getPress());
            stmt.setInt(4, book.getPublishYear());
            stmt.setString(5, book.getAuthor());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                throw new Exception("Book already exists");
            }
            stmt = conn.prepareStatement(
                    "INSERT INTO book (category, title, press, publish_year, author, price, stock) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, book.getCategory());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getPress());
            stmt.setInt(4, book.getPublishYear());
            stmt.setString(5, book.getAuthor());
            stmt.setDouble(6, book.getPrice());
            stmt.setInt(7, book.getStock());
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                book.setBookId(rs.getInt(1));
            } else {
                throw new Exception("Failed to get book id");
            }
            commit(conn);
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
            if (!rs.next()) {
                throw new Exception("Book not exists");
            }
            int stock = rs.getInt("stock");
            if (stock + deltaStock < 0) {
                throw new Exception("Stock not enough");
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
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM book WHERE category = ? AND title = ? AND press = ? AND publish_year = ? AND author = ?");
            for (Book book : books) {
                stmt.setString(1, book.getCategory());
                stmt.setString(2, book.getTitle());
                stmt.setString(3, book.getPress());
                stmt.setInt(4, book.getPublishYear());
                stmt.setString(5, book.getAuthor());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    throw new Exception("Book already exists");
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
            for (Book book : books) {
                if (rs.next()) {
                    book.setBookId(rs.getInt(1));
                } else {
                    throw new Exception("Failed to get book id");
                }
            }
            commit(conn);
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
            if (rs.next()) {
                throw new Exception("Book is borrowed");
            }
            stmt = conn.prepareStatement("DELETE FROM book WHERE book_id = ?");
            stmt.setInt(1, bookId);
            Integer affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new Exception("Book not exists");
            }
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
                    "SELECT * FROM book WHERE (category = ? OR ? IS NULL) AND (title like ? OR ? IS NULL) AND (press like ? OR ? IS NULL) AND (publish_year >= ? OR ? IS NULL) AND (publish_year <= ? OR ? IS NULL) AND (author like ? OR ? IS NULL) AND (price >= ? OR ? IS NULL) AND (price <= ? OR ? IS NULL)");
            if (conditions.getSortBy() != Book.SortColumn.BOOK_ID) {
                sql.append(
                        " ORDER BY " + conditions.getSortBy().toString() + " " + conditions.getSortOrder().toString()
                                + " , book_id ASC");
            } else {
                sql.append(" ORDER BY book_id " + conditions.getSortOrder().toString());
            }
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            if (conditions.getCategory() == null) {
                pstmt.setNull(1, Types.VARCHAR);
                pstmt.setNull(2, Types.VARCHAR);
            } else {
                pstmt.setString(1, conditions.getCategory());
                pstmt.setString(2, conditions.getCategory());
            }
            if (conditions.getTitle() == null) {
                pstmt.setNull(3, Types.VARCHAR);
                pstmt.setNull(4, Types.VARCHAR);
            } else {
                pstmt.setString(3, '%' + conditions.getTitle() + '%');
                pstmt.setString(4, '%' + conditions.getTitle() + '%');
            }
            if (conditions.getPress() == null) {
                pstmt.setNull(5, Types.VARCHAR);
                pstmt.setNull(6, Types.VARCHAR);
            } else {
                pstmt.setString(5, '%' + conditions.getPress() + '%');
                pstmt.setString(6, '%' + conditions.getPress() + '%');
            }
            if (conditions.getMinPublishYear() == null) {
                pstmt.setNull(7, Types.INTEGER);
                pstmt.setNull(8, Types.INTEGER);
            } else {
                pstmt.setInt(7, conditions.getMinPublishYear());
                pstmt.setInt(8, conditions.getMinPublishYear());
            }
            if (conditions.getMaxPublishYear() == null) {
                pstmt.setNull(9, Types.INTEGER);
                pstmt.setNull(10, Types.INTEGER);
            } else {
                pstmt.setInt(9, conditions.getMaxPublishYear());
                pstmt.setInt(10, conditions.getMaxPublishYear());
            }
            if (conditions.getAuthor() == null) {
                pstmt.setNull(11, Types.VARCHAR);
                pstmt.setNull(12, Types.VARCHAR);
            } else {

                pstmt.setString(11, '%' + conditions.getAuthor() + '%');
                pstmt.setString(12, '%' + conditions.getAuthor() + '%');
            }
            if (conditions.getMinPrice() == null) {
                pstmt.setNull(13, Types.DOUBLE);
                pstmt.setNull(14, Types.DOUBLE);
            } else {
                pstmt.setDouble(13, conditions.getMinPrice());
                pstmt.setDouble(14, conditions.getMinPrice());
            }
            if (conditions.getMaxPrice() == null) {
                pstmt.setNull(15, Types.DOUBLE);
                pstmt.setNull(16, Types.DOUBLE);
            } else {
                pstmt.setDouble(15, conditions.getMaxPrice());
                pstmt.setDouble(16, conditions.getMaxPrice());
            }
            ResultSet rs = pstmt.executeQuery();
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
            commit(conn);
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
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                throw new Exception("No such book or no stock");
            }
            pstmt = conn.prepareStatement(
                    "SELECT * FROM borrow WHERE card_id = ? AND book_id = ? AND return_time = 0 FOR UPDATE");
            pstmt.setInt(1, borrow.getCardId());
            pstmt.setInt(2, borrow.getBookId());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                throw new Exception("This book has been borrowed by this card");
            }
            pstmt = conn.prepareStatement(
                    "INSERT INTO borrow (card_id, book_id, borrow_time, return_time) VALUES (?, ?, ?, ?)");
            pstmt.setInt(1, borrow.getCardId());
            pstmt.setInt(2, borrow.getBookId());
            pstmt.setLong(3, borrow.getBorrowTime());
            pstmt.setLong(4, 0);
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
                    "SELECT * FROM borrow WHERE card_id = ? AND book_id = ? AND return_time = 0 FOR UPDATE");
            pstmt.setInt(1, borrow.getCardId());
            pstmt.setInt(2, borrow.getBookId());
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                throw new Exception("No such borrow record");
            }
            if (borrow.getReturnTime() <= rs.getLong("borrow_time")) {
                throw new Exception("Return time is earlier than borrow time");
            }
            pstmt = conn.prepareStatement(
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
            commit(conn);
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
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                throw new Exception("This card has been registered");
            }
            pstmt = conn.prepareStatement(
                    "INSERT INTO card (name, department, type) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, card.getName());
            pstmt.setString(2, card.getDepartment());
            pstmt.setString(3, card.getType().getStr());
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                card.setCardId(rs.getInt(1));
            } else {
                throw new Exception("Failed to get card id");
            }
            commit(conn);
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
            if (rs.next()) {
                throw new Exception("This card has borrowed books");
            }
            pstmt = conn.prepareStatement(
                    "DELETE FROM card WHERE card_id = ?");
            pstmt.setInt(1, cardId);
            Integer affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new Exception("No such card");
            }
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
                    "SELECT * FROM card ORDER BY card_id ASC");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Card card = new Card(rs.getInt("card_id"), rs.getString("name"),
                        rs.getString("department"), CardType.values(rs.getString("type")));
                cards.add(card);
            }
            commit(conn);
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

package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class LibraryManagerTest {


    private  LibraryManager libraryManager;
    private UserService userService;
    private NotificationService notificationService;
    private Map<String, Integer> bookInventory;
    private Map<String, String> borrowedBooks;

    @BeforeEach
    void setUp() throws NoSuchFieldException,
            IllegalAccessException {
        notificationService = mock(NotificationService.class);
        userService = mock(UserService.class);
        libraryManager = new LibraryManager(notificationService, userService);

        Field bookInventoryField = libraryManager.getClass().getDeclaredField("bookInventory");
        bookInventoryField.setAccessible(true);
        bookInventory = (Map<String, Integer>) bookInventoryField.get(libraryManager);
        bookInventoryField.set(libraryManager, bookInventory);
        libraryManager.addBook("book1", 7);
        libraryManager.addBook("book2", 0);
        libraryManager.addBook("book3", 18);
        libraryManager.addBook("book4", -7);

        Field borrowedBooksField = libraryManager.getClass().getDeclaredField("borrowedBooks");
        borrowedBooksField.setAccessible(true);
        borrowedBooksField.set(libraryManager, new HashMap<>());
        borrowedBooks = (Map<String, String>) borrowedBooksField.get(libraryManager);
        borrowedBooks.put("book2", "user1");
        borrowedBooks.put("book1", "user1");
    }


    @Test
    void borrowBookReturnsFalseIfUserIsUnactive() {
        when(userService.isUserActive("user4")).thenReturn(false);
        assertFalse(libraryManager.borrowBook("book1", "user4"));
    }

    @ParameterizedTest
    @CsvSource({"user1, book2", "user1, book4"})
    void borrowBookReturnsFalseWhenTheBooksAreOver(String bookId, String userId) {
        when(userService.isUserActive(userId)).thenReturn(true);
        assertFalse(libraryManager.borrowBook(bookId, userId));
    }

    @ParameterizedTest
    @CsvSource({"book1, user1"})
    void borrowBookReturnsTrueWhenWeBorrowBookSuccessfully(String bookId, String userId, int expectedCount) {

    }

    @Test
    void returnBookReturnsFalseWhenBookIdNotInBorrowedBooks() {
        assertFalse(libraryManager.returnBook("book3", "user1"));
    }

    @Test
    void returnBookReturnsFalseWhenUserIdNotEqualsWithIdFromMap() {
        assertFalse(libraryManager.returnBook("book2", "user2"));
    }

    @ParameterizedTest
    @CsvSource({"book2, user1, 1", "book1, user1, 8"})
    void returnBookReturnsTrueWhenBookWasBorrowed(String bookId, String userId, int expectedCount) {
        assertTrue(libraryManager.returnBook(bookId, userId));
        assertEquals(expectedCount, bookInventory.get(bookId));
        assertFalse(borrowedBooks.containsKey(bookId));
    }

    @Test
    void calculateDynamicLateFeeShouldThrowExceptionIfOverdueDaysLessThen0() {
        Exception exceptionActual = assertThrows(IllegalArgumentException.class,
                () -> libraryManager.calculateDynamicLateFee(-7, true, true));
        assertEquals("Overdue days cannot be negative.", exceptionActual.getMessage());
    }

    @ParameterizedTest
    @CsvSource({"1, 0.5", "0, 0", "15, 7.5"})
    void calculateDynamicLateFeeWhenIsNotBestsellerAndIsNotPremiumMember(int overdueDays, double expected) {
        assertEquals(expected, libraryManager.calculateDynamicLateFee(overdueDays, false, false));
    }

    @ParameterizedTest
    @CsvSource({"1, 0.75", "0, 0", "15, 11.25"})
    void calculateDynamicLateFeeWhenIsBestsellerAndIsNotPremiumMember(int overdueDays, double expected) {
        assertEquals(expected, libraryManager.calculateDynamicLateFee(overdueDays, true, false));
    }

    @ParameterizedTest
    @CsvSource({"1, 0.4", "0, 0", "15, 6"})
    void calculateDynamicLateFeeWhenIsNotBestsellerAndIsPremiumMember(int overdueDays, double expected) {
        assertEquals(expected, libraryManager.calculateDynamicLateFee(overdueDays, false, true));
    }

    @ParameterizedTest
    @CsvSource({"1, 0.6", "0, 0", "15, 9", "7, 4.2"})
    void calculateDynamicLateFeeWhenIsBestsellerAndIsPremiumMember(int overdueDays, double expected) {
        assertEquals(expected, libraryManager.calculateDynamicLateFee(overdueDays, true, true));
    }
}

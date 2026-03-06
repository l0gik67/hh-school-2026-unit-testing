package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryManagerTest {

    @Mock
    private UserService userService;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private  LibraryManager libraryManager;

    @BeforeEach
    void setUp() {
        libraryManager.addBook("book1", 7);
        libraryManager.addBook("book2", 1);

        when(userService.isUserActive("user1")).thenReturn(true);
        libraryManager.borrowBook("book2", "user1");
    }

    @Test
    void borrowBookReturnsFalseIfUserIsUnactive() {
        when(userService.isUserActive("user4")).thenReturn(false);
        int countBooksBeforeBorrowing = libraryManager.getAvailableCopies("book1");

        assertFalse(libraryManager.borrowBook("book1", "user4"));
        verify(notificationService).notifyUser("user4", "Your account is not active.");

        int countBooksAfterBorrowing = libraryManager.getAvailableCopies("book1");
        assertEquals(countBooksBeforeBorrowing, countBooksAfterBorrowing);
    }

    @Test
    void borrowBookReturnsFalseWhenTheBooksAreOver() {
        int countBooksBeforeBorrowing = libraryManager.getAvailableCopies("book2");

        assertFalse(libraryManager.borrowBook("book2", "user1"));

        int countBooksAfterBorrowing = libraryManager.getAvailableCopies("book2");
        assertEquals(countBooksBeforeBorrowing, countBooksAfterBorrowing);
    }

    @Test
    void borrowBookReturnsTrueWhenWeBorrowBookSuccessfully() {
        int countBooksBeforeBorrowing = libraryManager.getAvailableCopies("book1");

        assertTrue(libraryManager.borrowBook("book1", "user1"));
        verify(notificationService).notifyUser("user1", "You have borrowed the book: book1");

        int countBooksAfterBorrowing = libraryManager.getAvailableCopies("book1");
        assertEquals(countBooksBeforeBorrowing - 1, countBooksAfterBorrowing);
    }

    @Test
    void returnBookReturnsFalseWhenBookIdNotInBorrowedBooks() {
        int countBooksBeforeBorrowing = libraryManager.getAvailableCopies("book3");

        assertFalse(libraryManager.returnBook("book3", "user1"));

        int countBooksAfterBorrowing = libraryManager.getAvailableCopies("book3");
        assertEquals(countBooksBeforeBorrowing, countBooksAfterBorrowing);
    }

    @Test
    void returnBookReturnsFalseWhenUserIdNotEqualsWithIdFromMap() {
        int countOfBooksBeforeReturn = libraryManager.getAvailableCopies("book2");

        assertFalse(libraryManager.returnBook("book2", "user2"));

        int countOfBooksAfterReturn = libraryManager.getAvailableCopies("book2");
        assertEquals(countOfBooksBeforeReturn, countOfBooksAfterReturn);
    }

    @Test
    void returnBookReturnsTrueWhenBookWasBorrowed() {
        int countOfBooksBeforeReturn = libraryManager.getAvailableCopies("book2");

        assertTrue(libraryManager.returnBook("book2", "user1"));
        verify(notificationService).notifyUser("user1", "You have returned the book: book2");

        int countOfBooksAfterReturn = libraryManager.getAvailableCopies("book2");
        assertEquals(countOfBooksBeforeReturn + 1, countOfBooksAfterReturn);
    }

    @Test
    void calculateDynamicLateFeeShouldThrowExceptionIfOverdueDaysLessThen0() {
        Exception exceptionActual = assertThrows(IllegalArgumentException.class,
                () -> libraryManager.calculateDynamicLateFee(-7, true, true));
        assertEquals("Overdue days cannot be negative.", exceptionActual.getMessage());
    }

    @DisplayName("Testing calculate dynamic fee with different parameters")
    @ParameterizedTest(name = "overdueDays = {0}, isBestseller = {2}, isPremiumMember = {3}, expectedFee = {1}")
    @CsvSource({
            "1, 0.5, false, false",
            "1, 0.75, true, false",
            "1, 0.4, false, true",
            "1, 0.6, true, true",
            "0, 0, true, true"
    })
    void calculateDynamicLateFee(int overdueDays, double expectedFee, boolean isBestseller, boolean isPremiumMember) {
        double actualFee = libraryManager.calculateDynamicLateFee(overdueDays, isBestseller, isPremiumMember);
        assertEquals(expectedFee, actualFee);
    }
}

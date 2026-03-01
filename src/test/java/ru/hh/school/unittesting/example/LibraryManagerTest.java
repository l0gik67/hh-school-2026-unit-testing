package ru.hh.school.unittesting.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.hh.school.unittesting.homework.LibraryManager;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class LibraryManagerTest {


    private final LibraryManager libraryManager = new LibraryManager(null, null);


    @BeforeEach
    void setUp() throws NoSuchFieldException,
            IllegalAccessException {
        Field bookInventoryField = libraryManager.getClass().getDeclaredField("bookInventory");
        bookInventoryField.setAccessible(true);
        bookInventoryField.set(libraryManager, new HashMap<>());
        libraryManager.addBook("book1", 7);
        libraryManager.addBook("book2", 0);
        libraryManager.addBook("book3", 18);

        Field borrowedBooksField = libraryManager.getClass().getDeclaredField("borrowedBooks");
        borrowedBooksField.setAccessible(true);
        borrowedBooksField.set(libraryManager, new HashMap<>());
        Map<String, String> borrowedBooks = (Map<String, String>) borrowedBooksField.get(libraryManager);
        borrowedBooks.put("book2", "user1");
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

package de.codecentric.psd.worblehat.web.controller;

import de.codecentric.psd.worblehat.domain.Book;
import de.codecentric.psd.worblehat.domain.BookService;
import de.codecentric.psd.worblehat.domain.Borrowing;
import de.codecentric.psd.worblehat.web.formdata.BorrowBookFormData;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Controller for BorrowingBook
 */
@RequestMapping("/borrow")
@Controller
public class BorrowBookController {

    private static final String BORROW_PAGE = "borrow";
    private final BookService bookService;

    public BorrowBookController(BookService bookService) {
        this.bookService = Objects.requireNonNull(bookService);
    }

    @RequestMapping(method = RequestMethod.GET)
    public void setupForm(final ModelMap model) {
        model.put("borrowFormData", new BorrowBookFormData());
    }

    @Transactional
    @RequestMapping(method = RequestMethod.POST)
    public String processSubmit(@ModelAttribute("borrowFormData") @Valid BorrowBookFormData borrowFormData,
                                BindingResult result) {
        if (result.hasErrors()) {
            return BORROW_PAGE;
        }
        Set<Book> books = bookService.findBooksByIsbn(borrowFormData.getIsbn());
        if (books.isEmpty()) {
            result.rejectValue("isbn", "noBookExists");
            return BORROW_PAGE;
        }
        Optional<Borrowing> borrowing = bookService.borrowBook(borrowFormData.getIsbn(), borrowFormData.getEmail());

        return borrowing
                .map(b -> "home")
                .orElseGet(() -> {
                    result.rejectValue("isbn", "noBorrowableBooks");
                    return BORROW_PAGE;
                });
    }

    @ExceptionHandler(Exception.class)
    public String handleErrors(Exception ex, HttpServletRequest request) {
        return "home";
    }
}

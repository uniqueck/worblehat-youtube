package de.codecentric.psd.worblehat.web.controller;

import javax.servlet.http.HttpServletRequest;

import de.codecentric.psd.worblehat.domain.Book;
import de.codecentric.psd.worblehat.domain.BookAlreadyBorrowedException;
import de.codecentric.psd.worblehat.domain.BookRepository;
import de.codecentric.psd.worblehat.web.command.BookBorrowFormData;
import de.codecentric.psd.worblehat.web.validator.ValidateBorrowBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for BorrowingBook
 */
@RequestMapping("/borrow")
@Controller
public class BorrowBookController {

	private BookRepository bookRepository;
	private ValidateBorrowBook validator = new ValidateBorrowBook();

	@Autowired
	public BorrowBookController(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	@RequestMapping(method = RequestMethod.GET)
	public void setupForm(final ModelMap model) {
		model.put("borrowFormData", new BookBorrowFormData());
	}

	@Transactional
	@RequestMapping(method = RequestMethod.POST)
	public String processSubmit(ModelMap modelMap,
			@ModelAttribute("borrowFormData") BookBorrowFormData cmd,
			BindingResult result) {

		validator.validate(cmd, result);
		if (result.hasErrors()) {
			modelMap.put("borrowFormData", cmd);
			return "/borrow";
		}
		Book book = bookRepository.findBorrowableBook(cmd.getIsbn());
		if(book == null) {
			result.rejectValue("isbn", "notBorrowable");
			modelMap.put("borrowFormData", cmd);
			return "/borrow";
		}
		try {
			book.borrow(cmd.getEmail());
		} catch (BookAlreadyBorrowedException e) {
			result.reject("internalError");
			modelMap.put("borrowFormData", cmd);
			return "/borrow";
		}

		return "/home";
	}

	@ExceptionHandler(Exception.class)
	public String handleErrors(Exception ex, HttpServletRequest request) {
		return "/home";
	}
}
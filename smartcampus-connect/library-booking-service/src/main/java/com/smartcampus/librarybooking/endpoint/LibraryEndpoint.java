package com.smartcampus.librarybooking.endpoint;

import com.smartcampus.library.GetBookAvailabilityRequest;
import com.smartcampus.library.GetBookAvailabilityResponse;
import com.smartcampus.librarybooking.entity.Book;
import com.smartcampus.librarybooking.exception.BookNotFoundSoapException;
import com.smartcampus.librarybooking.service.LibraryService;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/**
 * â•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گ
 * R8 â€” SOAP ENDPOINT: Library Book Availability Service
 * â•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گ
 *
 * This class implements the SOAP web service endpoint for checking
 * book availability in the SmartCampus Library system.
 *
 * <h3>SOAP Operations</h3>
 * <ul>
 *   <li><b>getBookAvailability</b> â€” accepts a {@code bookId} and returns
 *       the book's title, author, and availability status.</li>
 * </ul>
 *
 * <h3>WSDL</h3>
 * <p>Auto-generated at: {@code http://localhost:8084/ws/library.wsdl}</p>
 *
 * <h3>R8 Rubric Requirements Satisfied</h3>
 * <ol>
 *   <li><b>Successful SOAP call</b> â€” when {@code bookId} exists (e.g., 1),
 *       a valid {@code GetBookAvailabilityResponse} is returned.</li>
 *   <li><b>Deliberately-triggered SOAP Fault</b> â€” when {@code bookId}
 *       does not exist (e.g., 999), a {@link BookNotFoundSoapException}
 *       is thrown, which Spring WS converts into a standard
 *       {@code <soap:Fault>} response with {@code faultcode=soap:Client}
 *       and a descriptive {@code faultstring}.</li>
 * </ol>
 *
 * <h3>Fault Handling Flow</h3>
 * <pre>
 *   Client sends: bookId=999
 *     â†’ LibraryEndpoint.getBookAvailability()
 *       â†’ LibraryService.getBookById(999) â†’ throws RuntimeException
 *         â†’ Caught in endpoint â†’ throws BookNotFoundSoapException
 *           â†’ Spring WS SoapFaultAnnotationExceptionResolver
 *             â†’ Generates SOAP Fault response:
 *                &lt;soap:Fault&gt;
 *                  &lt;faultcode&gt;soap:Client&lt;/faultcode&gt;
 *                  &lt;faultstring&gt;Book with ID 999 was not found&lt;/faultstring&gt;
 *                &lt;/soap:Fault&gt;
 * </pre>
 * â•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گâ•گ
 */
@Endpoint
public class LibraryEndpoint {

    private static final String NAMESPACE_URI = "http://smartcampus.com/library";

    private final LibraryService libraryService;

    // Constructor injection (no Lombok, consistent with project style)
    public LibraryEndpoint(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    /**
     * R8 â€” SOAP Operation: Get Book Availability.
     *
     * <p>Checks whether a book is available for borrowing by its ID.</p>
     *
     * <h3>Successful Response (bookId exists)</h3>
     * <pre>
     * {@code
     * <GetBookAvailabilityResponse>
     *   <bookId>1</bookId>
     *   <title>Spring Boot in Action</title>
     *   <author>Craig Walls</author>
     *   <available>true</available>
     * </GetBookAvailabilityResponse>
     * }
     * </pre>
     *
     * <h3>SOAP Fault Response (bookId does NOT exist)</h3>
     * <pre>
     * {@code
     * <soap:Fault>
     *   <faultcode>soap:Client</faultcode>
     *   <faultstring>Book with ID 999 was not found</faultstring>
     * </soap:Fault>
     * }
     * </pre>
     *
     * @param request the SOAP request containing the bookId to look up
     * @return the book availability response
     * @throws BookNotFoundSoapException if the bookId does not exist,
     *         resulting in a standard SOAP Fault being returned to the client
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getBookAvailabilityRequest")
    @ResponsePayload
    public GetBookAvailabilityResponse getBookAvailability(
            @RequestPayload GetBookAvailabilityRequest request) {

        // â”€â”€ R8: SOAP FAULT HANDLING â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // Attempt to retrieve the book from the database.
        // If the book does NOT exist, LibraryService.getBookById() throws
        // a RuntimeException. We catch it and re-throw as our custom
        // BookNotFoundSoapException, which is annotated with @SoapFault.
        //
        // Spring WS's SoapFaultAnnotationExceptionResolver detects the
        // @SoapFault annotation and automatically generates a properly
        // formatted <soap:Fault> response instead of a generic error.
        Book book;
        try {
            book = libraryService.getBookById(request.getBookId());
        } catch (RuntimeException e) {
            // Deliberately trigger a SOAP Fault for non-existent book IDs.
            // This satisfies R8: "demonstrate a deliberately-triggered SOAP Fault"
            throw new BookNotFoundSoapException(request.getBookId());
        }

        // â”€â”€ R8: SUCCESSFUL SOAP RESPONSE â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // If the book exists, build and return the standard SOAP response.
        // This satisfies R8: "demonstrate a successful SOAP call"
        GetBookAvailabilityResponse response = new GetBookAvailabilityResponse();
        response.setBookId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setAvailable(book.isAvailable());
        return response;
    }
}

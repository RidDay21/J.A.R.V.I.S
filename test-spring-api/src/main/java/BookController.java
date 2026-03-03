import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @GetMapping
    public String getAllBooks() {
        return "Books list";
    }

    @GetMapping("/{id}")
    public String getBook(@PathVariable int id) {
        return "Book " + id;
    }
}
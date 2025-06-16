package tech.mogami.facilitator.web.html;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * HomeController is the controller for the home page of the Mogami Facilitator web application.
 */
@Controller
public class HomeController {

    /**
     * Redirects the root URL to the Swagger UI.
     *
     * @return Redirects to the Swagger UI index page.
     */
    @GetMapping("/")
    public String redirectToSwaggerUi() {
        return "redirect:/swagger-ui/index.html";
    }

}

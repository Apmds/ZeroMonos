package pt.ua.tqs.hw1.boundary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import pt.ua.tqs.hw1.service.MunicipalityClient;

import static java.lang.invoke.MethodHandles.lookup;

@Controller
public class WebController {

    static final Logger log = LoggerFactory.getLogger(lookup().lookupClass());

    private MunicipalityClient municipalityClient;

    public WebController(MunicipalityClient municipalityClient) {
        this.municipalityClient = municipalityClient;
    }

    @GetMapping("/")
    public String index() {
        log.info("Main page HTML");

        return "index";
    }

    @GetMapping("/user")
    public String userInterface(Model model) {
        log.info("User page HTML");

        model.addAttribute("options", municipalityClient.getMunicipalities());
        return "user_interface";
    }
    
    @GetMapping("/staff")
    public String staffInterface(Model model) {
        log.info("Staff page HTML");

        model.addAttribute("options", municipalityClient.getMunicipalities());
        return "staff_interface";
    }
}
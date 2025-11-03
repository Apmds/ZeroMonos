package pt.ua.tqs.hw1.boundary;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import pt.ua.tqs.hw1.service.MunicipalityClient;

@Controller
public class WebController {

    private MunicipalityClient municipalityClient;

    public WebController(MunicipalityClient municipalityClient) {
        this.municipalityClient = municipalityClient;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/user")
    public String userInterface(Model model) {
        model.addAttribute("options", municipalityClient.getMunicipalities());
        return "user_interface";
    }
    
    @GetMapping("/staff")
    public String staffInterface(Model model) {
        model.addAttribute("options", municipalityClient.getMunicipalities());
        return "staff_interface";
    }
}
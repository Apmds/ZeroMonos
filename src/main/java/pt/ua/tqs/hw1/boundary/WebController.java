package pt.ua.tqs.hw1.boundary;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import pt.ua.tqs.hw1.service.MunicipalityClient;

@Controller
public class WebController {

    private MunicipalityClient municipalityClient = new MunicipalityClient();

    @GetMapping("/")
    public String index() {
        return "index.html";
    }

    @GetMapping("/user")
    public String user_interface(Model model) {
        model.addAttribute("options", municipalityClient.getMunicipalities());
        return "user_interface";
    }
    
    @GetMapping("/staff")
    public String staff_interface(Model model) {
        model.addAttribute("options", municipalityClient.getMunicipalities());
        return "staff_interface.html";
    }
}
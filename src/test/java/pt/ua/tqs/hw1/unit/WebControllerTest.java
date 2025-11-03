package pt.ua.tqs.hw1.unit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pt.ua.tqs.hw1.boundary.WebController;
import pt.ua.tqs.hw1.service.MunicipalityClient;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebController.class)
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MunicipalityClient municipalityClient;

    @Test
    void index() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void userInterface() throws Exception {
        when(municipalityClient.getMunicipalities()).thenReturn(List.of("Aveiro", "Porto"));

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(view().name("user_interface"))
                .andExpect(model().attributeExists("options"))
                .andExpect(model().attribute("options", List.of("Aveiro", "Porto")));
    }

    @Test
    void staffInterface() throws Exception {
        when(municipalityClient.getMunicipalities()).thenReturn(List.of("Lisboa", "Braga"));

        mockMvc.perform(get("/staff"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff_interface"))
                .andExpect(model().attributeExists("options"))
                .andExpect(model().attribute("options", List.of("Lisboa", "Braga")));
    }
}

package com.galvanize.simple_autos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AutosController.class)
public class AutosControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AutosService autosService;

    ObjectMapper objectMapper = new ObjectMapper();

    // Search for automobiles:
    //- GET /api/autos returns all autos when any autos exist
    @Test
    void getAutosReturns_wholeListNoParams() throws Exception {
        List<Automobile> automobiles = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            automobiles.add(new Automobile(1967+i, "Ford", "Mustang", "AABB"+i));
        }
        when(autosService.getAutos()).thenReturn(new AutosList(automobiles));
        mockMvc.perform(get("/api/autos"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.automobiles", hasSize(5)));
    }

    //- GET /api/autos returns 204 when No automobiles found
    @Test
    void getAutosReturns_noContentWithNoParams() throws Exception {
        when(autosService.getAutos()).thenReturn(new AutosList());
        mockMvc.perform(get("/api/autos"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    //- GET /api/autos returns?color=blue returns blue cars
    @Test
        void getAutosReturnsWithColorSearchParam() throws Exception {
            List<Automobile> automobiles = new ArrayList<>();
            for(int i = 0; i < 5; i++) {
                automobiles.add(new Automobile(1967+i, "Ford", "Mustang", "AABB"+i));
            }
            when(autosService.getAutos(anyString(), isNull()))
                    .thenReturn(new AutosList(automobiles));
            mockMvc.perform(get("/api/autos?color=RED"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.automobiles", hasSize(5)));
        }
    //- GET /api/autos returns?make=Chevrolet returns Chevrolets
    @Test
        void getAutosReturnsWithMakeSearchParam() throws Exception {
            List<Automobile> automobiles = new ArrayList<>();
            for(int i = 0; i < 5; i++) {
                automobiles.add(new Automobile(1967+i, "Ford", "Mustang", "AABB"+i));
            }
            when(autosService.getAutos(isNull(), anyString()))
                    .thenReturn(new AutosList(automobiles));
            mockMvc.perform(get("/api/autos?make=Ford"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.automobiles", hasSize(5)));
        }
    //- GET /api/autos returns?make=Chevrolet&color=orange returns orange chevies
    @Test
    void getAutosReturnsWithSearchParams() throws Exception {
        List<Automobile> automobiles = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            automobiles.add(new Automobile(1967+i, "Ford", "Mustang", "AABB"+i));
        }
        when(autosService.getAutos(anyString(), anyString()))
                .thenReturn(new AutosList(automobiles));
        mockMvc.perform(get("/api/autos?color=RED&make=Ford"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.automobiles", hasSize(5)));
    }

    //Add an automobile:
    //- POST /api/autos returns 200 when Automobile added successfully
    @Test
    void addAutoPostValidReturnsAuto() throws Exception {
        Automobile automobile = new Automobile(1980, "Mustang", "Ford", "AABBCD");
        when(autosService.addAutomobile(any(Automobile.class))).thenReturn(automobile);
        mockMvc.perform(post("/api/autos").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(automobile)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("make").value("Ford"));
    }

    //- POST /api/autos returns 400 when a Bad request is sent (not proper format or data in sent schema)
    @Test
    void addAutoPostBadRequest() throws Exception {
        when(autosService.addAutomobile(any(Automobile.class))).thenThrow(InvaldAutoException.class);
        String json = "{\"year\":1980,\"make\":\"Ford\",\"model\":\"Mustang\",\"color\":null,\"owner\":null,\"vin\":\"AABBCD\"}";
        mockMvc.perform(post("/api/autos").contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    //Find an automobile by its vin:
    //- GET /api/autos/{vin} returns 200 and returns the auto with the matching vin
    @Test
    void getAutoWithVinReturnsAuto() throws Exception {
        Automobile automobile = new Automobile(1980, "Mustang", "Ford", "AABBCD");
        when(autosService.getAuto(anyString())).thenReturn(automobile);
        mockMvc.perform(get("/api/autos/"+automobile.getVin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("vin").value(automobile.getVin()));
    }

    //- GET /api/autos/{vin} returns 204 when there is no matching vin so result is Vehicle not found
    @Test
    void getAutoWithVinBadRequestReturns204() throws Exception {
        when(autosService.getAuto(anyString())).thenThrow(AutoNotFoundException.class);
        mockMvc.perform(get("/api/autos/NOTFOUND"))
                .andExpect(status().isNoContent());
    }

    //Update owner, or color of vehicle:
    //- PATCH /api/autos/{vin} returns 200 when the Automobile updated successfully
    @Test
    void updateAutoPatchWithObjectReturnsAuto() throws Exception {
        Automobile automobile = new Automobile(1980, "Mustang", "Ford", "AABBCD");
        when(autosService.updateAuto(anyString(), anyString(), anyString())).thenReturn(automobile);
        mockMvc.perform(patch("/api/autos/"+automobile.getVin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"color\":\"Red\",\"owner\":\"Bob\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("color").value("Red"))
                .andExpect(jsonPath("owner").value("Bob"));
    }

    //- PATCH /api/autos/{vin} returns 204 when there is no matching vin so result is Vehicle not found so it is not updated
    @Test
    void updateAutoPatchBadRequestReturns204() throws Exception {
        when(autosService.updateAuto(anyString(), anyString(), anyString())).thenThrow(AutoNotFoundException.class);
        mockMvc.perform(patch("/api/autos/NOTFOUND")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"color\":\"Red\",\"owner\":\"Bob\"}"))
                .andExpect(status().isNoContent());
    }

    //- PATCH /api/autos/{vin} returns 400 when a Bad request is sent (not proper format or data in sent schema)
    @Test
    void updateAutoPatchBadRequestReturns400() throws Exception {
        //Automobile automobile = new Automobile(1980, "Mustang", "Ford", "AABBCD");
        when(autosService.updateAuto(anyString(), anyString(), anyString())).thenThrow(InvaldAutoException.class);
        mockMvc.perform(patch("/api/autos/BADREQUEST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"color\":\"Red\",\"owner\":\"Bob\"}"))
                .andExpect(status().isBadRequest());
    }

    //delete an automobile by its vin:
    //- DELETE /api/autos/{vin} returns 202 Automobile delete request accepted and auto is deleted
    @Test
    void deleteAutoWithVinExistsReturns202() throws Exception {
        mockMvc.perform(delete("/api/autos/AABBCD"))
                .andExpect(status().isAccepted());
        verify(autosService).deleteAuto(anyString());
    }

    //- DELETE /api/autos/{vin} returns 204 when there is no matching vin so result is Vehicle not found so it is not deleted
    @Test
    void deleteAutoWithVinNotExistsReturnsNoContent() throws Exception {
        doThrow(new AutoNotFoundException()).when(autosService).deleteAuto(anyString());
        mockMvc.perform(delete("/api/autos/AABBCD"))
                .andExpect(status().isNoContent());
    }
}

package com.galvanize.simple_autos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutosServiceTest {

    private AutosService autosService;

    @Mock
    AutosRepository autosRepository;

    @BeforeEach
    void setUp() {
        autosService = new AutosService(autosRepository);
    }

    @Test
    void getAutosNoArgumentsReturnsList() {
        Automobile automobile = new Automobile(1967, "Mustang", "Ford", "AABBCC");
        when(autosRepository.findAll()).thenReturn(Arrays.asList(automobile));
        AutosList autosList = autosService.getAutos();
        assertThat(autosList).isNotNull();
        assertThat(autosList.isEmpty()).isFalse();
    }

    @Test
    void getAutoSearchReturnsList() {
        Automobile automobile = new Automobile(1967, "Mustang", "Ford", "AABBCC");
        automobile.setColor("Black");
        when(autosRepository.findByColorContainsAndMakeContains(anyString(), anyString()))
                .thenReturn(Arrays.asList(automobile));
        AutosList autosList = autosService.getAutos("Black", "Ford");
        assertThat(autosList).isNotNull();
        assertThat(autosList.isEmpty()).isFalse();
    }

    @Test
    void getAutoWithVin() {
        Automobile automobile = new Automobile(1967, "Mustang", "Ford", "AABB1122");
        automobile.setColor("Black");
        when(autosRepository.findByVin(anyString()))
                .thenReturn(Optional.of(automobile));
        Automobile auto = autosService.getAuto(automobile.getVin());
        assertThat(auto).isNotNull();
        assertThat(auto.getVin()).isEqualTo(automobile.getVin());
    }

    @Test
    void addAutomobileValidReturnsAuto() {
        Automobile automobile = new Automobile(1967, "Mustang", "Ford", "AABBCC");
        automobile.setColor("Black");
        when(autosRepository.save(any(Automobile.class)))
                .thenReturn(automobile);
        Automobile auto = autosService.addAutomobile(automobile);
        assertThat(auto).isNotNull();
        assertThat(auto.getMake()).isEqualTo("Ford");
    }

    //test for addAutomobileInvalidReturnsException

    @Test
    void updateAutoPatchReturnsAuto() {
        Automobile automobile = new Automobile(1967, "Mustang", "Ford", "AABB1122");
        automobile.setColor("Black");
        when(autosRepository.findByVin(anyString()))
                .thenReturn(Optional.of(automobile));
        when(autosRepository.save(any(Automobile.class))).thenReturn(automobile);
        Automobile auto = autosService.updateAuto(automobile.getVin(), "Black", "Anyone");
        assertThat(auto).isNotNull();
        assertThat(auto.getVin()).isEqualTo(automobile.getVin());
    }

    @Test
    void deleteAutoByVin() {
        Automobile automobile = new Automobile(1967, "Mustang", "Ford", "DDCC1122");
        automobile.setColor("Black");
        when(autosRepository.findByVin(anyString()))
                .thenReturn(Optional.of(automobile));
        autosService.deleteAuto(automobile.getVin());
        verify(autosRepository).delete(any(Automobile.class));
    }

    @Test
    void deleteAutoByVinNotExists() {
        when(autosRepository.findByVin(anyString()))
                .thenReturn(Optional.empty());
        assertThatExceptionOfType(AutoNotFoundException.class)
                .isThrownBy(() -> { autosService.deleteAuto("NOTEXISTS-VIN"); });
    }
}
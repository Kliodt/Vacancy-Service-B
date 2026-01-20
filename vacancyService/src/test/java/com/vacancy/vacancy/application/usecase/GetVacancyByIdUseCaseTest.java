package com.vacancy.vacancy.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vacancy.vacancy.application.exception.EntityNotFoundException;
import com.vacancy.vacancy.domain.model.Vacancy;
import com.vacancy.vacancy.domain.port.VacancyPersistencePort;

/**
 * Unit тесты для GetVacancyByIdUseCase
 * Проверяют получение вакансии по ID и обработку ошибок
 */
@ExtendWith(MockitoExtension.class)
class GetVacancyByIdUseCaseTest {

    @Mock
    private VacancyPersistencePort vacancyPersistencePort;

    @InjectMocks
    private GetVacancyByIdUseCase getVacancyByIdUseCase;

    private Vacancy testVacancy;

    @BeforeEach
    void setUp() {
        testVacancy = new Vacancy("Java Developer", "Develop Java applications");
        testVacancy.setId(1L);
        testVacancy.setOrganizationId(1L);
        testVacancy.setSalary(100000);
        testVacancy.setCity("Moscow");
    }

    @Test
    void testExecute_Success() {
        // Arrange
        when(vacancyPersistencePort.findById(1L)).thenReturn(Optional.of(testVacancy));

        // Act
        Vacancy result = getVacancyByIdUseCase.execute(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Java Developer", result.getTitle());
        verify(vacancyPersistencePort).findById(1L);
    }

    @Test
    void testExecute_NotFound() {
        // Arrange
        when(vacancyPersistencePort.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> getVacancyByIdUseCase.execute(999L));
        assertEquals("Вакансия не найдена", ex.getMessage());
    }

    @Test
    void testExecute_WithUnusedParameter() {
        // Arrange
        when(vacancyPersistencePort.findById(1L)).thenReturn(Optional.of(testVacancy));

        // Act
        Vacancy result = getVacancyByIdUseCase.execute(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }
}

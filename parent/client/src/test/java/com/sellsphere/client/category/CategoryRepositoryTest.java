package com.sellsphere.client.category;

import com.sellsphere.common.entity.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, scripts = "classpath:sql/categories.sql")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void givenAliasAndEnabledCategory_whenFindByAliasAndEnabledIsTrue_thenCategoryShouldBeFound() {
        // Given
        String alias = "computers";

        // When
        Optional<Category> category = categoryRepository.findByAliasAndEnabledIsTrue(alias);

        // Then
        assertThat(category).isPresent();
        assertThat(category.get().getName()).isEqualTo("Computers");
    }

    @Test
    void whenFindAllByParentIsNull_thenRootCategoriesShouldBeFound() {
        // When
        List<Category> categories = categoryRepository.findAllByParentIsNull();

        // Then
        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).getName()).isEqualTo("Electronics");
    }

    @Test
    void givenCategoryName_whenFindByName_thenCategoryShouldBeFound() {
        // Given
        String name = "CPU Processors Unit";

        // When
        Category category = categoryRepository.findByName(name);

        // Then
        assertThat(category).isNotNull();
        assertThat(category.getAlias()).isEqualTo("computer_processors");
    }
}
package com.sellsphere.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "footer_sections")
public class FooterSection extends IdentifiedEntity {

    @Min(1)
    @Max(3)
    @Column(name = "section_number", nullable = false)
    private Integer sectionNumber;

    @Column(name = "section_header", nullable = false)
    private String sectionHeader;

    @OneToMany(mappedBy = "footerSection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("itemNumber asc")
    private List<FooterItem> footerItems = new ArrayList<>();

    public void addFooterItem(FooterItem footerItem) {
        footerItems.add(footerItem);
        footerItem.setFooterSection(this);
    }

    public void removeFooterItem(FooterItem footerItem) {
        footerItems.remove(footerItem);
        footerItem.setFooterSection(null);
    }

}
package com.team01.ticketing.performance;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Entity
@Table(name = "performance")
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Column(name = "kopis_id", length = 50, unique = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String kopisId;
    @Column(nullable = false, length = 200)
    @NotBlank
    @Size(max = 200)
    private String title;
    @Column(length = 50)
    @Size(max = 50)
    private String genre;
    @Column(name = "poster_url", length = 500)
    @Size(max = 500)
    private String posterUrl;
    @Column(name = "venue_name", length = 200)
    @Size(max = 200)
    private String venueName;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;

    protected Performance() {
    }

    public Long getId() {
        return id;
    }

    public String getKopisId() {
        return kopisId;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getVenueName() {
        return venueName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}

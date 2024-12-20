package com.nistapp.uda.index.models;

import javax.persistence.*;
import java.time.Instant;

/**
 * Entity class representing the statuses table in the database.
 * This class manages different status types with their categories and metadata.
 */
@Entity
@Table(name = "statuses")
public class Status {
    // Primary key field with auto-increment strategy
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Integer id;

    // Human-readable name of the status
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    // Detailed description of the status stored as TEXT
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Category classification for the status
    @Column(name = "category", length = 50)
    private String category;

    // Ordering position within the category
    @Column(name = "order")
    private Integer order;

    // Flag indicating if the status is currently active
    @Column(name = "is_active")
    private Boolean isActive = true;

    // Timestamp for creation time
    @Column(name = "created_at")
    private Instant createdAt;

    // Timestamp for last update time
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Automatically sets creation and update timestamps before entity persistence
     */
    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Automatically updates the update timestamp before entity update
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getter and Setter methods

    /**
     * Gets the unique identifier of the status
     * @return Integer id of the status
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the status
     * @param id Integer value to set as id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the name of the status
     * @return String name of the status
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the status
     * @param name String value to set as name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the status
     * @return String description of the status
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the status
     * @param description String value to set as description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the category of the status
     * @return String category of the status
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category of the status
     * @param category String value to set as category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Gets the order value of the status
     * @return Integer order value
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * Sets the order value of the status
     * @param order Integer value to set as order
     */
    public void setOrder(Integer order) {
        this.order = order;
    }

    /**
     * Gets the active status
     * @return Boolean indicating if status is active
     */
    public Boolean getIsActive() {
        return isActive;
    }

    /**
     * Sets the active status
     * @param active Boolean value to set active status
     */
    public void setIsActive(Boolean active) {
        isActive = active;
    }

    /**
     * Gets the creation timestamp
     * @return Instant representing creation time
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp
     * @param createdAt Instant value to set as creation time
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last update timestamp
     * @return Instant representing last update time
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last update timestamp
     * @param updatedAt Instant value to set as update time
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

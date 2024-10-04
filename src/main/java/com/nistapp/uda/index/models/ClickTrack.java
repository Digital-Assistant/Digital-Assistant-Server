package com.nistapp.uda.index.models;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

import javax.persistence.*;
import java.time.Instant;

/**
 * Represents a click track entity, which stores information about user interactions.
 *
 * @author [Your Name]
 * @since [Version]
 */
@Entity
@Table(name = "ClickTrack")
@Indexed(index = "prod_clickTrack")
public class ClickTrack extends PanacheEntityBase {
	/**
	 * Unique identifier for the click track entity.
	 */
	@Id // Primary key for the entity
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate the ID
	@Column(name = "id", nullable = false, unique = true, length = 11) // Define the column properties
	@GenericField // Mark the field as searchable
	private long id; // Store the ID value

	/**
	 * User session ID associated with the click track.
	 *
	 * @note This field is used to track user interactions across multiple requests.
	 */
	@GenericField // Mark the field as searchable
	@Column(length = 500) // Define the column length
	private String usersessionid; // Store the user session ID value

//	@GenericField
	@Column(length = 200, name="event_name")
//	@FullTextField(analyzer = "english")
	@KeywordField
	private String clicktype; // store the event name

	@GenericField
	@Column(length = 2000, name="event_value")
	private String clickedname; // store the event value

	@GenericField
	@Column(name="sequence_id")
	private long recordid; // store the recording sequence id

	@KeywordField
	@Column(length = 5000, name="domain")
	private String domain; // store the domain from where the event occured

	@Column(name = "createdat", nullable = false)
	@GenericField(name = "createdat_sort", sortable = Sortable.YES)
	private long createdat; // store the created timestamp

	/**
	 * Saves the current time in milliseconds to the createdat field
	 * before persisting the entity.
	 */
	@PrePersist
	public void preSave() {
		this.createdat = Instant.now().toEpochMilli();
	}

	/**
	 * Returns the ID value of the click track.
	 *
	 * @return The ID value of the click track.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the ID value of the click track.
	 *
	 * @param id The ID value of the click track.
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Gets the user session ID value.
	 *
	 * @return The user session ID value.
	 */
	public String getUsersessionid() {
		return usersessionid;
	}

	/**
	 * Sets the user session ID value.
	 *
	 * @param usersessionid The user session ID value.
	 */
	public void setUsersessionid(String usersessionid) {
		this.usersessionid = usersessionid;
	}

	/**
	 * Returns the type of the click event.
	 *
	 * @return The type of the click event.
	 */
	public String getClicktype() {
		return clicktype;
	}

	/**
	 * Sets the type of the click event.
	 *
	 * @param clicktype The type of the click event.
	 */
	public void setClicktype(String clicktype) {
		this.clicktype = clicktype;
	}

	/**
	 * Returns the name of the element that was clicked.
	 *
	 * @return The name of the element that was clicked.
	 */
	public String getClickedname() {
		return clickedname;
	}

	/**
	 * Sets the name of the element that was clicked.
	 *
	 * @param clickedname The name of the element that was clicked.
	 */
	public void setClickedname(String clickedname) {
		this.clickedname = clickedname;
	}

	/**
	 * Returns the recording sequence ID associated with the click track.
	 *
	 * @return The recording sequence ID.
	 */
	public long getRecordid() {
		return recordid;
	}

	/**
	 * Set the recording sequence ID associated with the click track.
	 *
	 * @param recordid The recording sequence ID.
	 */
	public void setRecordid(long recordid) {
		this.recordid = recordid;
	}

	/**
	 * Return the timestamp for when the click track was created.
	 *
	 * @return A millisecond-precision timestamp.
	 */
	public long getCreatedat() {
		return createdat;
	}

	/**
	 * Set the timestamp for when the click track was created.
	 *
	 * @param createdat A millisecond-precision timestamp.
	 */
	public void setCreatedat(long createdat) {
		this.createdat = createdat;
	}

    /**
     * Returns the domain associated with the click track.
     *
     * @return The domain name.
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Sets the domain associated with the click track.
     *
     * @param domain The domain name.
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }
}

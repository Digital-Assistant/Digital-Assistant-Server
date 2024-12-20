CREATE TABLE `statuses` (
                            `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique identifier for the status',
                            `name` VARCHAR(100) NOT NULL COMMENT 'Human-readable name of the status',
                            `description` TEXT COMMENT 'Detailed description of the status',
                            `category` VARCHAR(50) COMMENT 'Category to which the status belongs',
                            `order` INT COMMENT 'Order of the status within its category',
                            `is_active` BOOLEAN DEFAULT TRUE COMMENT 'Indicates whether the status is active or inactive',
                            `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp indicating when the status was created',
                            `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Timestamp indicating when the status was last updated'
);


INSERT INTO `statuses` (name, description, category, `order`, is_active)
VALUES
    ('Draft', 'Article is in draft stage', 'sequenceList', 1, TRUE),
    ('Pending Review', 'Article is pending review by editor', 'sequenceList', 2, TRUE),
    ('Under Review', 'Article is under review by editor', 'sequenceList', 3, TRUE),
    ('Revision Required', 'Article requires revisions', 'sequenceList', 4, TRUE),
    ('Approved', 'Article is approved for publication', 'sequenceList', 5, TRUE),
    ('Published', 'Article is published', 'sequenceList', 6, TRUE),
    ('Unpublished', 'Article is unpublished', 'sequenceList', 7, TRUE),
    ('Archived', 'Article is archived', 'sequenceList', 8, TRUE);
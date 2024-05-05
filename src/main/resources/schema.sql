CREATE TABLE IF NOT EXISTS Campaigns (
    campaign_id INT AUTO_INCREMENT PRIMARY KEY,
    campaign_name VARCHAR(255) NOT NULL,
    short_name VARCHAR(255),
    campaign_description VARCHAR(1000),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    max_attempts INT NOT NULL,
    pass_percentage INT NOT NULL,
    is_active boolean NOT NULL,
    is_archive boolean NOT NULL
);
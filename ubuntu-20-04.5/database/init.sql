-- Wedding Database Schema
CREATE TABLE IF NOT EXISTS guests (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    plus_one BOOLEAN DEFAULT FALSE,
    dietary_restrictions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rsvps (
    id SERIAL PRIMARY KEY,
    guest_id INTEGER REFERENCES guests(id) ON DELETE CASCADE,
    status VARCHAR(20) CHECK (status IN ('attending', 'not_attending', 'maybe')) NOT NULL,
    plus_one_attending BOOLEAN DEFAULT FALSE,
    message TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS wedding_events (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    event_date TIMESTAMP NOT NULL,
    venue_name VARCHAR(200),
    venue_address TEXT,
    dress_code VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data for testing
INSERT INTO guests (first_name, last_name, email, phone, plus_one) VALUES
('John', 'Smith', 'john.smith@email.com', '+1-555-0101', true),
('Jane', 'Doe', 'jane.doe@email.com', '+1-555-0102', false),
('Bob', 'Johnson', 'bob.johnson@email.com', '+1-555-0103', true),
('Alice', 'Brown', 'alice.brown@email.com', '+1-555-0104', false),
('Charlie', 'Wilson', 'charlie.wilson@email.com', '+1-555-0105', true)
ON CONFLICT (email) DO NOTHING;

INSERT INTO wedding_events (name, description, event_date, venue_name, venue_address, dress_code) VALUES
('Wedding Ceremony', 'The main wedding ceremony', '2024-06-15 16:00:00', 'Beautiful Gardens', '123 Garden Lane, City, State', 'Formal'),
('Reception', 'Wedding reception and dinner', '2024-06-15 18:00:00', 'Grand Ballroom', '456 Celebration Ave, City, State', 'Cocktail')
ON CONFLICT DO NOTHING;

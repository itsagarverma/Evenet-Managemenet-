CREATE TABLE IF NOT EXISTS contact_settings (
  id BIGINT PRIMARY KEY CHECK (id = 1),
  email VARCHAR(254),
  phone VARCHAR(40),
  whatsapp VARCHAR(40),
  whatsapp_url VARCHAR(500),
  instagram VARCHAR(500),
  facebook VARCHAR(500),
  website VARCHAR(500),
  address VARCHAR(500)
);

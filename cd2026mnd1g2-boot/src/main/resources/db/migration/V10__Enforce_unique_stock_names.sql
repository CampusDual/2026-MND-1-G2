ALTER TABLE stock ADD COLUMN normalized_name VARCHAR(160);

UPDATE stock SET normalized_name = LOWER(TRIM(name));

ALTER TABLE stock ALTER COLUMN normalized_name SET NOT NULL;
ALTER TABLE stock ADD CONSTRAINT uk_stock_normalized_name UNIQUE (normalized_name);

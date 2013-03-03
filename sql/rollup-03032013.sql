ALTER TABLE card ADD COLUMN lastoffer_id character varying(255);

ALTER TABLE card DROP COLUMN lastofferstart;
ALTER TABLE card DROP COLUMN lastofferend;

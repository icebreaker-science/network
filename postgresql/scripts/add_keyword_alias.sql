ALTER TABLE keyword
    ADD COLUMN refers_to text;

ALTER TABLE keyword 
   ADD CONSTRAINT fk_keyword
   FOREIGN KEY (refers_to)
   REFERENCES keyword(name);
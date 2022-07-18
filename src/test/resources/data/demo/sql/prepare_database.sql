DECLARE
    C INT;
BEGIN
    SELECT COUNT(*)
    INTO C
    FROM USER_TABLES
    WHERE TABLE_NAME = UPPER('TEST_USER');
    IF
            C = 1 THEN
        EXECUTE IMMEDIATE 'DROP TABLE TEST_USER';
    END IF;
END;
/

CREATE TABLE TEST_USER
(
    ID        number(19) primary key,
    FULL_NAME varchar(255),
    AGE       number(2),
    URL       varchar(80)
)
/

INSERT INTO TEST_USER (ID, FULL_NAME, AGE, URL)
VALUES (1, 'Steve Jobs', 60, null)
/
INSERT INTO TEST_USER (ID, FULL_NAME, AGE, URL)
VALUES (12, 'Elon Mask', 50, null)
/

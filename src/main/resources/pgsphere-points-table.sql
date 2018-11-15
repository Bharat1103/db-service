/* Setting the default output format to degrees */
SELECT set_sphere_output('DEG');
/* Creating a table with spoint as its field type */
CREATE TABLE points ( i int PRIMARY KEY, p spoint );
INSERT INTO points VALUES (1, '( 0d, 0d)');
INSERT INTO points VALUES (2, '(10d, 0d)');
INSERT INTO points VALUES (3, '( 0d,10d)');
/* View the successful insertions */
select * from points;
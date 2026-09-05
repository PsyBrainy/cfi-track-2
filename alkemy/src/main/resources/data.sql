-- Crear un script SQL básico (data.sql) en la carpeta resources que inserte al menos un usuario con rol
-- ADMIN al levantar la aplicación, para poder hacer pruebas en el sistema

INSERT INTO  Users (
Name,Last_Name,Email,Date_Created,Last_Login,Is_Active,City,Province,Country,
Birthdate,Address,DNI,Postal_Code,Employment,Gender,Phone_Number,Password, Role
) values (

'Juan', 'Alkemy', 'admin@admin.com', '2020-02-20',
'2020-02-20', TRUE, 'San Carlos', 'Mendoza', 'Argentina',
'1999-11-11', 'Calle Falsa 123', '12345678', '12345',
'independiente', 'Masculino', '1234567890', 'admin', 'ADMIN'


         ) ON CONFLICT DO NOTHING;
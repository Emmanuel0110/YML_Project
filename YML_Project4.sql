DROP DATABASE IF EXISTS yml_store ;
CREATE SCHEMA yml_store ;
USE yml_store ;

CREATE TABLE tree (
  id INT PRIMARY KEY AUTO_INCREMENT,
  filePath VARCHAR(255) NOT NULL
);

CREATE TABLE type (
  id INT PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

INSERT INTO type(id, name) VALUES(0, 'STRING');
INSERT INTO type(id, name) VALUES(1, 'NUMBER');
INSERT INTO type(id, name) VALUES(2, 'DATE');

CREATE TABLE attribute (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  type_id INT NOT NULL,
  FOREIGN KEY (type_id) REFERENCES type(id)
);

CREATE TABLE node (
  id INT PRIMARY KEY AUTO_INCREMENT,
  attribute_id INT,
  tree_id INT NOT NULL,
  FOREIGN KEY (attribute_id) REFERENCES attribute(id),
  FOREIGN KEY (tree_id) REFERENCES tree(id)  
);

CREATE TABLE string_value (
  id INT NOT NULL ,
  value VARCHAR(255) NOT NULL,
  FOREIGN KEY (id) REFERENCES node(id)
);

CREATE TABLE number_value (
  id INT NOT NULL ,
  value DECIMAL(11,6) NOT NULL,
  FOREIGN KEY (id) REFERENCES node(id)
);

CREATE TABLE date_value (
  id INT NOT NULL ,
  value date NOT NULL,
  FOREIGN KEY (id) REFERENCES node(id)
);


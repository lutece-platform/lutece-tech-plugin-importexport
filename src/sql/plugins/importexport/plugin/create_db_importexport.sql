DROP TABLE IF EXISTS importexport_export_config;
CREATE TABLE importexport_export_config
(
	id INT NOT NULL,
	table_name VARCHAR(255),
	output_file_name VARCHAR(255),
	xsl_stylesheet_id int,
	plugin VARCHAR(255),
	PRIMARY KEY (id)
);

DROP TABLE IF EXISTS importexport_export_config_columns;
CREATE TABLE importexport_export_config_columns
(
    id_config INT NOT NULL,
	column_name VARCHAR(255),
	PRIMARY KEY (id_config,column_name)
);
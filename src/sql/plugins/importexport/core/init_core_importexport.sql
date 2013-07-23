--
-- Init  table core_admin_right
--
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url, documentation_url) 
VALUES ('IMPORTDATA_MANAGEMENT','importexport.adminFeature.importdata_management.name',2,'jsp/admin/plugins/importexport/ManageImportData.jsp','importexport.adminFeature.importdata_management.description',0,'importexport','CONTENT','images/admin/skin/plugins/importexport/importexport.png', '');

--
-- Init  table core_user_right
--
INSERT INTO core_user_right (id_right,id_user) VALUES ('IMPORTDATA_MANAGEMENT',1);
INSERT INTO core_datastore VALUES ( 'importexport.daemon.importDaemon.onStartUp', 'false' );
INSERT INTO core_datastore VALUES ( 'importexport.daemon.importDaemon.interval', '86400' );

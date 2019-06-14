// Export of keyspace syndeia_cloud_repositories
CREATE KEYSPACE IF NOT EXISTS appperimeter_cloud_hostip
WITH durable_writes = true
     AND replication = {
'class' : 'SimpleStrategy',
'replication_factor' : 1
};

CREATE TABLE IF NOT EXISTS appperimeter_cloud_hostip.app_host_ip (
id timeuuid,
app_sha256 text,
ip int,
PRIMARY KEY (app_sha256, ip)
) ;
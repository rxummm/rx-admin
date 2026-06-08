CREATE TABLE IF NOT EXISTS sys_slow_query (
  id BIGINT AUTO_INCREMENT COMMENT "ID",
  sql_text TEXT COMMENT "SQL",
  params TEXT COMMENT "params",
  cost_time_ms BIGINT NOT NULL COMMENT "cost(ms)",
  query_type VARCHAR(20) DEFAULT "" COMMENT "type",
  mapper_method VARCHAR(200) DEFAULT "" COMMENT "mapper",
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT "create_time",
  PRIMARY KEY (id),
  KEY idx_cost_time (cost_time_ms),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT="slow_query";

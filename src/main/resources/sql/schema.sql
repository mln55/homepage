DROP TABLE IF EXISTS post;
DROP TABLE IF EXISTS category;

CREATE TABLE category (
  category_idx BIGINT AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  parent_category_idx BIGINT,
  create_at DATETIME NOT NULL DEFAULT NOW(),
  update_at DATETIME,
  PRIMARY KEY(category_idx)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE post (
  post_idx BIGINT AUTO_INCREMENT,
  category_idx BIGINT,
  title VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  hit BIGINT NOT NULL DEFAULT 0,
  visible BIT(1) NOT NULL,
  create_at DATETIME NOT NULL DEFAULT NOW(),
  update_at DATETIME,
  PRIMARY KEY (post_idx)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE category ADD CONSTRAINT fk_category_parent_category_idx FOREIGN KEY (parent_category_idx) REFERENCES category (category_idx)
ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE category ADD UNIQUE unique_category_name_and_parent_category_idx (name, parent_category_idx);

ALTER TABLE post ADD CONSTRAINT fk_post_category_idx FOREIGN KEY (category_idx) REFERENCES category (category_idx)
ON UPDATE CASCADE ON DELETE RESTRICT;

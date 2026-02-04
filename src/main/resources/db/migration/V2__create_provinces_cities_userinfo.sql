-- ============================================
-- V2: 创建省市表和用户信息表
-- ============================================

-- 省份表
CREATE TABLE provinces (
  id         serial PRIMARY KEY,
  name       varchar(64) NOT NULL UNIQUE,
  created_at timestamptz NOT NULL DEFAULT now()
);

-- 城市表
CREATE TABLE cities (
  id          serial PRIMARY KEY,
  province_id int         NOT NULL REFERENCES provinces(id) ON DELETE CASCADE,
  name        varchar(64) NOT NULL,
  created_at  timestamptz NOT NULL DEFAULT now(),
  UNIQUE(province_id, name)
);

-- 索引
CREATE INDEX idx_cities_province ON cities(province_id);

-- 用户信息表
CREATE TABLE user_info (
  id                   bigint PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  gender               varchar(16) CHECK (gender IN ('male', 'female', 'other')),
  birthday             date,
  signature            varchar(255),
  location_province_id int REFERENCES provinces(id),
  location_city_id     int REFERENCES cities(id),
  hometown_province_id int REFERENCES provinces(id),
  hometown_city_id     int REFERENCES cities(id),
  created_at           timestamptz NOT NULL DEFAULT now(),
  updated_at           timestamptz NOT NULL DEFAULT now()
);

-- 索引
CREATE INDEX idx_userinfo_hometown ON user_info(hometown_province_id, hometown_city_id);
CREATE INDEX idx_userinfo_location ON user_info(location_province_id, location_city_id);

-- 触发器：自动更新 updated_at
CREATE TRIGGER update_user_info_updated_at BEFORE UPDATE ON user_info
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();


-- PostgreSQL: enable citext for case-insensitive email uniqueness
CREATE EXTENSION IF NOT EXISTS citext;

-- Create function to auto-update updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE users (
  id            bigserial PRIMARY KEY,
  nick_name     varchar(32) NOT NULL UNIQUE,
  password_hash text        NOT NULL,
  avatar        text,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE contacts (
  id            bigserial PRIMARY KEY,
  account_id    bigint      NOT NULL REFERENCES users(id),
  email         citext      UNIQUE,
  phone         varchar(32) UNIQUE,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_contacts_account_id ON contacts (account_id);

CREATE TABLE verifications (
  id             bigserial PRIMARY KEY,
  account_id     bigint      NOT NULL REFERENCES users(id),
  method         varchar(16) NOT NULL,
  verified       boolean     NOT NULL DEFAULT false,
  student_id     varchar(32),
  real_name      varchar(64),
  verified_email citext,
  created_at     timestamptz NOT NULL DEFAULT now(),
  updated_at     timestamptz NOT NULL DEFAULT now(),
  UNIQUE (account_id),
  CHECK (method IN ('sso', 'school_email')),
  CHECK (
    (method = 'sso' AND student_id IS NOT NULL AND real_name IS NOT NULL AND verified_email IS NULL)
    OR
    (method = 'school_email' AND verified_email IS NOT NULL AND student_id IS NULL AND real_name IS NULL)
  )
);

-- Create triggers to auto-update updated_at
CREATE TRIGGER update_users_updated_at
  BEFORE UPDATE ON users
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_contacts_updated_at
  BEFORE UPDATE ON contacts
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_verifications_updated_at
  BEFORE UPDATE ON verifications
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

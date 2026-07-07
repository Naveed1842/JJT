-- Convenience denorm: profile photo FK on children + avatar FK on users
-- Enables single-row read without JOIN for public children page

ALTER TABLE children
    ADD COLUMN profile_photo_media_id UUID REFERENCES media_files(id);

ALTER TABLE users
    ADD COLUMN avatar_media_id UUID REFERENCES media_files(id);

CREATE INDEX idx_children_photo ON children(profile_photo_media_id) WHERE profile_photo_media_id IS NOT NULL;
CREATE INDEX idx_users_avatar   ON users(avatar_media_id) WHERE avatar_media_id IS NOT NULL;

-- Create database
CREATE DATABASE social_media;
USE social_media;

-- Users table: Stores user information for login/register and profile details
CREATE TABLE users (
    user_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(15) UNIQUE,
    password_hash VARCHAR(255) NOT NULL, -- Store hashed passwords
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    bio TEXT,
    profile_picture_url VARCHAR(255), -- URL to profile picture
    is_private BOOLEAN DEFAULT FALSE, -- For private accounts
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Posts table: Stores user posts
CREATE TABLE posts (
    post_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    content TEXT NOT NULL, -- Post text
    image_url VARCHAR(255), -- URL to post image (optional)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Comments table: Stores comments on posts
CREATE TABLE comments (
    comment_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Likes table: Stores likes on posts and comments
CREATE TABLE likes (
    like_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    post_id BIGINT UNSIGNED, -- Nullable if liking a comment
    comment_id BIGINT UNSIGNED, -- Nullable if liking a post
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE,
    CONSTRAINT check_like_type CHECK (post_id IS NOT NULL XOR comment_id IS NOT NULL) -- Ensures like is for post OR comment
);

-- Stories table: Stores user stories (temporary content)
CREATE TABLE stories (
    story_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    content_url VARCHAR(255) NOT NULL, -- URL to story media (image/video)
    expires_at TIMESTAMP NOT NULL, -- When story expires
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Followers table: Stores follower/following relationships
CREATE TABLE followers (
    follower_id BIGINT UNSIGNED NOT NULL, -- User who is following
    followed_id BIGINT UNSIGNED NOT NULL, -- User being followed
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, followed_id),
    FOREIGN KEY (follower_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (followed_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Follow Requests table: Stores follow requests for private accounts
CREATE TABLE follow_requests (
    request_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    requester_id BIGINT UNSIGNED NOT NULL, -- User sending request
    target_id BIGINT UNSIGNED NOT NULL, -- User receiving request
    status ENUM('pending', 'accepted', 'rejected') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (target_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE (requester_id, target_id) -- Prevent duplicate requests
);
`
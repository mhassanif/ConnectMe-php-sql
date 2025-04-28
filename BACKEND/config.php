<?php
// Database configuration
$host = 'localhost';      // MySQL server
$dbname = 'connect_me';   // Database name (use the database you created)
$username = 'root';       // Default MySQL username
$password = '';           // Default MySQL password (empty for XAMPP)

$conn = mysqli_connect($host, $username, $password);

// Check connection
if (!$conn) {
    die("Connection failed: " . mysqli_connect_error());
}

// Create database if it doesn't exist
$sql = "CREATE DATABASE IF NOT EXISTS $dbname";
if (mysqli_query($conn, $sql)) {
    // Select the database
    mysqli_select_db($conn, $dbname);
    
    // Create users table if it doesn't exist
    $sql = "CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTO_INCREMENT,
        name TEXT,
        username TEXT,
        phone TEXT,
        email TEXT,
        password TEXT,
        is_online INTEGER DEFAULT 0,
        profile_picture TEXT
    )";
    mysqli_query($conn, $sql);
    
    // Create followers table if it doesn't exist
    $sql = "CREATE TABLE IF NOT EXISTS followers (
        id INTEGER PRIMARY KEY AUTO_INCREMENT,
        follower_id INTEGER,
        following_id INTEGER,
        FOREIGN KEY(follower_id) REFERENCES users(id),
        FOREIGN KEY(following_id) REFERENCES users(id)
    )";
    mysqli_query($conn, $sql);
} else {
    echo "Error creating database: " . mysqli_error($conn);
}

// Function to sanitize user input
function sanitize_input($data) {
    global $conn;
    $data = trim($data);
    $data = stripslashes($data);
    $data = htmlspecialchars($data);
    $data = mysqli_real_escape_string($conn, $data);
    return $data;
}
?>
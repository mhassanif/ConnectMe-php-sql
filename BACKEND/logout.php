<?php
// Include database connection
require_once 'config.php'; // Ensure this file connects to your MySQL database

// Set response header to JSON (optional if you need a JSON response, else can be removed)
header('Content-Type: application/json');

// Start session
session_start();

// Check if the user is logged in (adjust according to your session management)
if (isset($_SESSION['user_id'])) {
    $user_id = $_SESSION['user_id']; // Get the user ID from the session

    // Ensure user ID is valid
    if ($user_id > 0) {
        // Update user as offline (set 'is_online' to 0 in the database)
        $stmt = mysqli_prepare($conn, "UPDATE users SET is_online = 0 WHERE id = ?");
        
        if ($stmt === false) {
            echo json_encode(['success' => false, 'message' => 'Failed to prepare query']);
            exit;
        }

        mysqli_stmt_bind_param($stmt, "i", $user_id);
        
        if (mysqli_stmt_execute($stmt)) {
            // Successfully updated user status, now destroy the session
            session_destroy(); // Destroy session to log the user out
            echo json_encode(['success' => true, 'message' => 'Logout successful']);
        } else {
            // Log the error if query execution fails
            error_log('Logout failed for user ID ' . $user_id . ': ' . mysqli_error($conn));
            echo json_encode(['success' => false, 'message' => 'Logout failed']);
        }

        // Close the prepared statement
        mysqli_stmt_close($stmt);
    } else {
        echo json_encode(['success' => false, 'message' => 'Invalid user ID']);
    }
} else {
    echo json_encode(['success' => false, 'message' => 'No active session found']);
}

// Close the database connection
mysqli_close($conn);
?>

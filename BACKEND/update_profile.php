<?php
// Include database connection
require_once 'config.php';

// Set response header to JSON
header('Content-Type: application/json');

// Check if the request method is POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    
    // Get user ID
    $user_id = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;
    
    if ($user_id <= 0) {
        echo json_encode(['success' => false, 'message' => 'Invalid user ID']);
        exit;
    }
    
    // Get and sanitize user input
    $name = isset($_POST['name']) ? sanitize_input($_POST['name']) : null;
    $username = isset($_POST['username']) ? sanitize_input($_POST['username']) : null;
    $phone = isset($_POST['phone']) ? sanitize_input($_POST['phone']) : null;
    $email = isset($_POST['email']) ? sanitize_input($_POST['email']) : null;
    
    // Check if username is already taken by another user
    if ($username !== null) {
        $stmt = mysqli_prepare($conn, "SELECT id FROM users WHERE username = ? AND id != ?");
        mysqli_stmt_bind_param($stmt, "si", $username, $user_id);
        mysqli_stmt_execute($stmt);
        mysqli_stmt_store_result($stmt);
        
        if (mysqli_stmt_num_rows($stmt) > 0) {
            echo json_encode(['success' => false, 'message' => 'Username already exists']);
            exit;
        }
    }
    
    // Check if email is already taken by another user
    if ($email !== null) {
        $stmt = mysqli_prepare($conn, "SELECT id FROM users WHERE email = ? AND id != ?");
        mysqli_stmt_bind_param($stmt, "si", $email, $user_id);
        mysqli_stmt_execute($stmt);
        mysqli_stmt_store_result($stmt);
        
        if (mysqli_stmt_num_rows($stmt) > 0) {
            echo json_encode(['success' => false, 'message' => 'Email already exists']);
            exit;
        }
    }
    
    // Build the update query
    $updates = [];
    $types = "";
    $params = [];
    
    if ($name !== null) {
        $updates[] = "name = ?";
        $types .= "s";
        $params[] = $name;
    }
    
    if ($username !== null) {
        $updates[] = "username = ?";
        $types .= "s";
        $params[] = $username;
    }
    
    if ($phone !== null) {
        $updates[] = "phone = ?";
        $types .= "s";
        $params[] = $phone;
    }
    
    if ($email !== null) {
        $updates[] = "email = ?";
        $types .= "s";
        $params[] = $email;
    }
    
    // Handle profile picture upload
    if (isset($_FILES['profile_picture']) && $_FILES['profile_picture']['error'] === UPLOAD_ERR_OK) {
        $upload_dir = 'uploads/';
        
        // Create directory if it doesn't exist
        if (!file_exists($upload_dir)) {
            mkdir($upload_dir, 0777, true);
        }
        
        $file_name = time() . '_' . basename($_FILES['profile_picture']['name']);
        $target_file = $upload_dir . $file_name;
        
        if (move_uploaded_file($_FILES['profile_picture']['tmp_name'], $target_file)) {
            $updates[] = "profile_picture = ?";
            $types .= "s";
            $params[] = $target_file;
        }
    }
    
    if (!empty($updates)) {
        $query = "UPDATE users SET " . implode(", ", $updates) . " WHERE id = ?";
        $types .= "i";
        $params[] = $user_id;
        
        $stmt = mysqli_prepare($conn, $query);
        
        // Bind parameters dynamically
        $bind_params = array($stmt, $types);
        foreach ($params as $key => $value) {
            $bind_params[] = &$params[$key];
        }
        call_user_func_array('mysqli_stmt_bind_param', $bind_params);
        
        if (mysqli_stmt_execute($stmt)) {
            echo json_encode(['success' => true, 'message' => 'Profile updated successfully']);
        } else {
            echo json_encode(['success' => false, 'message' => 'Failed to update profile: ' . mysqli_error($conn)]);
        }
    } else {
        echo json_encode(['success' => false, 'message' => 'No fields to update']);
    }
    
} else {
    echo json_encode(['success' => false, 'message' => 'Invalid request method']);
}
?>
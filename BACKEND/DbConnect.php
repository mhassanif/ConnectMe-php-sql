<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

include_once dirname(__FILE__) . "/Constants.php";

class DbConnect {
    private $con;

    function __construct() {}

    function connect() {
        $this->con = mysqli_connect(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
        
        if (!$this->con) {
            die("Connection failed: " . mysqli_connect_error());
        } else {
            echo "Database Connected Successfully!";
        }

        return $this->con;
    }

    function createUser($name, $email, $roll, $dp) {
        $stmt = $this->con->prepare("INSERT INTO `users` (`name`, `roll`, `email`, `dp`) VALUES (?, ?, ?, ?);");
        $stmt->bind_param("ssss", $name, $roll, $email, $dp);
        if ($stmt->execute()) {
            return true;
        } else {
            return false;
        }
    }
}

// Now we call the function
$db = new DbConnect();
$db->connect();
?>

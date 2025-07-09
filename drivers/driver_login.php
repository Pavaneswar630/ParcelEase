<?php
session_start();
header("Content-Type: application/json");
include "../dbconnect.php";

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
    exit;
}

$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';

if (empty($email) || empty($password)) {
    echo json_encode(["status" => "error", "message" => "Email and password required"]);
    exit;
}

$query = "SELECT driver_id, password, status FROM drivers WHERE email = ?";
$stmt = $conn->prepare($query);
$stmt->bind_param("s", $email);
$stmt->execute();
$stmt->store_result();
$stmt->bind_result($driver_id, $db_password, $status);
$stmt->fetch();

if ($stmt->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Invalid email or password"]);
    exit;
}

// Check if the driver is approved
if ($status === "Pending") {
    echo json_encode(["status" => "error", "message" => "Your account is pending approval."]);
    exit;
} elseif ($status === "Not Available") {
    echo json_encode(["status" => "error", "message" => "You are currently not available for deliveries."]);
    exit;
}

// Authentication successful, store driver session
$_SESSION['driver_id'] = $driver_id;

echo json_encode(["status" => "success", "message" => "Login successful", "driver_id" => $driver_id]);

$stmt->close();
$conn->close();
?>
